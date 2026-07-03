/*
 * Copyright (C) 2024 Shubham Panchal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shubham0204.smollmandroid.headless

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import io.shubham0204.smollm.SmolLM
import io.shubham0204.smollmandroid.R
import io.shubham0204.smollmandroid.data.Chat
import io.shubham0204.smollmandroid.llm.SmolLMManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

/**
 * Foreground service that loads a GGUF model and runs a single inference pass, logging all
 * six instrumentation metrics tagged with the caller-supplied run_id. Keeps the process alive
 * for the full duration so Android cannot kill it mid-inference.
 *
 * Uses [CompletableDeferred] to bridge [SmolLMManager]'s callback-based API into coroutines,
 * which avoids dispatcher mismatch issues that can silently swallow results when callbacks are
 * delivered on Dispatchers.Main.
 */
class BenchmarkService : Service() {

    private val smolLMManager: SmolLMManager by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "benchmark_channel"
        private const val NOTIF_ID = 9002

        private val CURRENT_SYSFS_PATHS = listOf(
            "/sys/class/power_supply/battery/current_now",
            "/sys/class/power_supply/Battery/current_now",
            "/sys/class/power_supply/bms/current_now",
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val modelPath = intent?.getStringExtra("model_path")
        val prompt    = intent?.getStringExtra("prompt")
        val runId     = intent?.getStringExtra("run_id")

        if (modelPath == null || prompt == null || runId == null) {
            Log.d("RUN_ERROR", "run_id=${runId ?: "unknown"} reason=missing_extras")
            stopSelf(startId)
            return START_NOT_STICKY
        }

        // startForeground must be called within 5 s of startForegroundService().
        startForeground(
            NOTIF_ID,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Running benchmark: $runId")
                .setOngoing(true)
                .setSilent(true)
                .build(),
        )

        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.d("RUN_ERROR", "run_id=$runId reason=unexpected_error message=${throwable.message}")
            Log.e("BenchmarkService", "Unhandled exception", throwable)
            stopSelf(startId)
        }

        serviceScope.launch(exceptionHandler) {
            runBenchmark(modelPath, prompt, runId)
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Core pipeline ──────────────────────────────────────────────────────────

    private suspend fun runBenchmark(modelPath: String, prompt: String, runId: String) {
        if (!File(modelPath).exists()) {
            Log.d("RUN_ERROR", "run_id=$runId reason=model_path_not_found path=$modelPath")
            return
        }

        // Reload only when the model path changes or SmolLMManager has been unloaded.
        val needsLoad = smolLMManager.currentModelPath != modelPath
                     || !smolLMManager.isInstanceLoaded.get()
        if (needsLoad) {
            Log.d("BENCHMARK", "run_id=$runId loading model from $modelPath")
            smolLMManager.unload()

            // isTask=true skips loading conversation history from the DB.
            val loadDeferred = CompletableDeferred<Result<Unit>>()
            smolLMManager.load(
                chat      = Chat(isTask = true),
                modelPath = modelPath,
                params    = SmolLM.InferenceParams(),
                onError   = { e -> loadDeferred.complete(Result.failure(e)) },
                onSuccess = {    loadDeferred.complete(Result.success(Unit)) },
            )
            loadDeferred.await().getOrElse { e ->
                Log.d("RUN_ERROR", "run_id=$runId reason=model_load_failed message=${e.message}")
                return
            }
        } else {
            Log.d("BENCHMARK", "run_id=$runId model already loaded, skipping reload")
        }

        // ── Power / thermal monitoring ─────────────────────────────────────────
        val currentSamples   = mutableListOf<Long>()
        val thermalThrottled = AtomicBoolean(false)
        val batteryManager   = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val powerManager     = getSystemService(Context.POWER_SERVICE)   as PowerManager
        val chargeAtStart    = batteryManager.getLongProperty(
            BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER
        )

        val monitorJob: Job = serviceScope.launch {
            while (true) {
                val sample = readCurrentUa(batteryManager)
                if (sample > 0L) currentSamples.add(sample)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (powerManager.currentThermalStatus >= PowerManager.THERMAL_STATUS_MODERATE) {
                        thermalThrottled.set(true)
                    }
                }
                delay(100)
            }
        }

        // ── Inference ──────────────────────────────────────────────────────────
        Log.d("BENCHMARK", "run_id=$runId starting inference")
        val promptDispatchTimeMs = System.currentTimeMillis()
        val inferenceDeferred = CompletableDeferred<Result<SmolLMManager.SmolLMResponse>>()
        smolLMManager.getResponse(
            query                      = prompt,
            promptDispatchTimeMs       = promptDispatchTimeMs,
            responseTransform          = { it },
            onPartialResponseGenerated = {},
            onSuccess  = { r -> inferenceDeferred.complete(Result.success(r)) },
            onCancelled = {   inferenceDeferred.complete(
                Result.failure(Exception("Inference cancelled"))) },
            onError    = { e -> inferenceDeferred.complete(Result.failure(e)) },
            saveToDb   = false,
        )

        // Await inference before cancelling the power monitor.
        val response = inferenceDeferred.await().getOrElse { e ->
            monitorJob.cancel()
            Log.d("RUN_ERROR", "run_id=$runId reason=inference_failed message=${e.message}")
            return
        }
        monitorJob.cancel()

        // ── Log metrics ────────────────────────────────────────────────────────
        val coldLoadMs   = smolLMManager.getColdLoadTimeMs()
        val avgCurrentUa = computeAvgCurrentUa(
            currentSamples, chargeAtStart, response.generationTimeSecs, batteryManager
        )

        Log.d("COLD_LOAD", "run_id=$runId value=${coldLoadMs ?: 0}")
        Log.d("TTFT",      "run_id=$runId value=${response.ttftMs}")
        Log.d("TPS",       "run_id=$runId value=${response.generationSpeed}")
        Log.d("MEMORY",    "run_id=$runId value=${response.peakRssKb}")
        Log.d("POWER",     "run_id=$runId value=${
            if (avgCurrentUa == Long.MIN_VALUE) "unsupported" else avgCurrentUa}")
        Log.d("THERMAL",   "run_id=$runId value=${thermalThrottled.get()}")
        Log.d("RUN_DONE",  "run_id=$runId response=${response.response}")
    }

    // ── Power helpers ──────────────────────────────────────────────────────────

    private fun readCurrentUa(batteryManager: BatteryManager): Long {
        val apiVal = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (apiVal != Long.MIN_VALUE) return abs(apiVal)
        for (path in CURRENT_SYSFS_PATHS) {
            try { return abs(File(path).readText().trim().toLong()) } catch (_: Exception) {}
        }
        return Long.MIN_VALUE
    }

    private fun computeAvgCurrentUa(
        samples: List<Long>,
        chargeAtStartUah: Long,
        durationSecs: Int,
        batteryManager: BatteryManager,
    ): Long {
        val valid = samples.filter { it > 0L }
        if (valid.isNotEmpty()) return valid.average().toLong()
        if (chargeAtStartUah != Long.MIN_VALUE && durationSecs > 0) {
            val chargeAtEnd = batteryManager.getLongProperty(
                BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER
            )
            if (chargeAtEnd != Long.MIN_VALUE) {
                val deltaUah = chargeAtStartUah - chargeAtEnd
                if (deltaUah > 0) return deltaUah * 3600L / durationSecs
            }
        }
        return Long.MIN_VALUE
    }
}
