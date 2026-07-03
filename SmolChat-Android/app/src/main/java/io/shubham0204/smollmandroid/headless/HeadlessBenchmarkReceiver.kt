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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Receives `com.smollmandroid.RUN_PROMPT` broadcasts (e.g. from ADB) and delegates to
 * [HeadlessBenchmarkService] which runs inference in a foreground service so the process
 * is not killed mid-run.
 *
 * Required extras:
 *   model_path — absolute path to a .gguf file on device
 *   prompt     — text to send for inference
 *   run_id     — unique string used to correlate log lines
 *
 * Example ADB command:
 *   adb shell am broadcast -a com.smollmandroid.RUN_PROMPT \
 *     -n io.shubham0204.smollmandroid/.headless.HeadlessBenchmarkReceiver \
 *     --es model_path /sdcard/Download/model-q4_k_m.gguf \
 *     --es prompt "What is the capital of France?" \
 *     --es run_id run_001
 */
class HeadlessBenchmarkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BROADCAST_RECEIVER", "onReceive called with action: ${intent.action}")

        val modelPath = intent.getStringExtra("model_path")
        val prompt = intent.getStringExtra("prompt")
        val runId = intent.getStringExtra("run_id")

        if (modelPath == null || prompt == null || runId == null) {
            Log.d("RUN_ERROR", "run_id=${runId ?: "unknown"} reason=missing_extras" +
                " (need model_path, prompt, run_id)")
            return
        }

        Log.d("BROADCAST_RECEIVER", "received run_id=$runId")

        val serviceIntent = Intent(context, BenchmarkService::class.java).apply {
            putExtra("model_path", modelPath)
            putExtra("prompt", prompt)
            putExtra("run_id", runId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
