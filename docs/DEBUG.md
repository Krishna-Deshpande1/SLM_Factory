# Harness audit (2026-10-09)

Four independent read-only reviews of `Benchmark-Harness/paper_table5/` (core run logic; energy maths; builds, model
conversion and pool handling; device control, reporting, tests and docs), cross-checked against the result files and
raw tool output. This lists what was found, what was fixed the same day, what is still open, and what was verified
correct.

**Bottom line.** The headline measurements are sound: llama.cpp throughput matches `llama-bench`'s own samples to
0.01 t/s for every configuration, and an independent recomputation of energy from the raw battery traces reproduces
the stored values to within 0.1%. One bug very likely caused the phone crashes, and one timing definition overstated
MNN GPU decode on the newer MNN by 6–8%. Both are fixed, and the affected results were recomputed from raw data.

## 1. Bugs that affected results or stability (fixed)

| # | Bug | Effect | Fix |
|---|---|---|---|
| 1 | **A "stopped" benchmark kept running.** The stop command was `pkill -f <work>/run.sh; pkill <exe>` in one shell call; `pkill -f` matches the invoking shell's own command line and killed it before the second `pkill` ran (`run_paper_table5.py`, swap guard and timeout paths). | After the swap guard "stopped" a model that did not fit, it kept swapping while the next configuration started. This is the most likely cause of the two hard resets (Qwen3.5-2B MNN F16, Qwen3.5-4B Q8_0) and of the next configuration being blamed for the old process's swap. | `stop_benchmark()`: one adb call per kill, by process name first, verified with `pidof`, escalating to `SIGKILL`. Leftover `llama-bench` / `llm_bench` are also killed at session start. |
| 2 | **MNN decode window left out sampling time.** Decode was timed as `[end − decode_us, end]`. At MNN head, `decode_us` excludes per-token sampling and the read-back of the GPU's logits. | Head-build MNN GPU decode was ~6–8% too fast and its µJ/token ~7% too low; pinned-build rows were off by ~1%. It also raised a false "phone suspended" warning on every head MNN GPU run. | Decode window is now wall clock, `[begin + prefill_us, end]`, the same definition as `llm_bench`'s "decode wall speed (incl. sampling)" and the paper's first-to-last time. All 12 existing MNN results were recomputed from their raw `PB_MARK` lines and traces (e.g. Qwen3.5-2B Q4 GPU 30.27 → 27.98 t/s). The suspend check only warns above 1.15× for MNN. |
| 3 | **Every failure was final on resume.** `done()` only checked that a result file existed. | A transient failure ("not enough free RAM right now", a timeout, a crash, a dropped connection) was never retried. | Only successes and the static "does not fit in RAM" results count as done. |
| 4 | **MNN F16 ×2 memory factor applied to the GPU backend.** The evidence for it is CPU-only. | Qwen3.5-2B F16 on MNN GPU was recorded as not fitting without being tried. | Factor applies to the CPU backend only (the free-RAM check and the swap guard still protect the phone). |
| 5 | **Energy-repetition estimate could be 50× off.** Repetitions for the energy reading are sized from the model's file size; on an unoptimized path (SmolLM2-135M Q8_0 on the pinned OpenCL backend: 9.9 s per prefill repetition, estimated 0.17 s) it asked for 173 repetitions, ~28 minutes of GPU load that heated the battery to 38.8 °C. | Wasted time and a hot phone; no wrong number. | Capped at 40 repetitions; a short run is topped up from the measured speed. |
| 6 | **Cool-down gate passed with no reading.** If adb returned nothing, temperature was `None` and the gate saw "no problems". | A run could start without the phone being checked. Not observed in the results. | A missing reading counts as not ready. |
| 7 | **"Stopped cooling" rule accepted a warming battery** (`max − current ≤ 0.2 °C`). | A run could start while heat was still spreading from the chip. | Requires the window to be flat (`max − min ≤ 0.2 °C`). |
| 8 | **Charge-counter energy marked valid.** The S23's charge counter moves in 0.1%-of-capacity steps about every 30 s; `perfetto:charge` values ranged from 0 to 684,184 µJ/token, all "valid", and that method was first in the default preference order. | No effect on the S23 (its profile pins `perfetto:current`), but a phone without a profile would have reported junk. | Counter methods are invalid below 10 counter steps per phase; current-based methods come first. |
| 9 | **Energy with no idle baseline marked valid** (net value empty, gross negative). | Edge case (`--pre-idle-seconds 0` plus recovery). | Recorded as invalid with a reason; the session-start idle window is now also written to the per-session record that `--recover-energy` reads. |
| 10 | **A new session deleted an earlier crashed session's trace.** | The afternoon Qwen3.5 session of 2026-10-08 lost its energy this way. | A stale trace and sample file are renamed on the phone instead of deleted. |
| 11 | **Wake-lock helper could be left running** when its start check failed and the harness fell back to screen-on. | Phone kept awake indefinitely. | The helper is stopped in that fallback path. |
| 12 | Perfetto buffer used `DISCARD` with write-into-file (caps a trace at ~19 h and flags data loss). | None yet. | `RING_BUFFER`. |
| 13 | MNN's OpenCL tuning cache folder (`tmp/`) did not exist in the pinned binary folder, so every GPU run re-tuned in its warm-up repetition (12.8 s for SmolLM2). | Extra heat before the measured repetitions; the repetition itself is discarded. | The folder is created at session start. |
| 14 | `report.py`: a later failed result replaced an earlier successful one for the same configuration. | A transient failure could hide a good measurement. | Successful results are preferred. |

## 2. Findings that explain odd numbers (no code bug)

- **MNN GPU decode on the pinned build is slow for every model, worst for small ones** (SmolLM2-135M: 9.1 t/s on
  GPU vs 224 on CPU). The cost is a fixed ~3–4 ms per layer regardless of model width (SmolLM2-135M 30 layers →
  109 ms/token; Llama-1B 16 layers → 56–69 ms). Pinned MNN (510ac8f) never sets its OpenCL command-batching option
  for decode (`Llm::tuning()` returns early for OpenCL); MNN head does, and reaches 28 t/s on a 2B model. It is a
  property of the paper's MNN version, not of the export or the harness. Confirming test: run SmolLM2-135M with the
  head binaries.
- **llama.cpp F16 on the CPU is very slow** (Qwen3.5-2B: 9.3 t/s prefill). The CPU build follows llama.cpp's Android
  instructions (`-march=armv8.7a`, no `+fp16`), so F16 weights are converted to F32 for every matmul; MNN uses fp16
  kernels. The numbers are correct for this build but F16 on llama.cpp CPU is not like-for-like with MNN.
- **llama.cpp drops two warm-ups, MNN one.** `llama-bench` runs its own warm-up before the repetition loop and the
  harness also discards repetition 0; on CPU that discarded repetition is consistently the fastest. Throughput is
  not biased, but the README's "1 warm-up" understates it.
- **Qwen3.5-0.8B F16 on MNN GPU** segfaults during the second response (the first completes), a runtime crash in
  MNN head, not the export.
- **Battery-gauge calibration bounds absolute energy to roughly ±10–20%.** Over whole sessions the integrated HAL
  current is 11–17% below the drop in the charge counter. Comparisons between configurations are unaffected; absolute
  µJ/token should carry that uncertainty. Voltage also updates only about every 32 s (≤ ~3.5% effect).

## 3. Open issues (not fixed)

**Could affect a future run**
- **Original phone settings are kept only in memory.** After a hard kill (not Ctrl+C), the next session reads the
  altered state as the "originals" (airplane mode, Do Not Disturb, screen timeout, mobile data) and restores to
  that. `--recover-energy` restores none of them and turns Do Not Disturb off unconditionally.
- **Swap guard looks processes up by name.** With the kill fix a leftover is unlikely, but the guard should read the
  PID of its own run.
- **`--recover-energy` only handles the newest session**, and stops whichever `perfetto` process it finds.
- **The `head` ref is a moving `master`.** The Qwen3.5-0.8B F16/Q4_0 GGUFs were converted at llama.cpp `24e4183`,
  the rest at `46baf1f`, and the head binaries are `9c2e0e4`; the manifest records the wrong commit for reused
  files and the wrong MNN converter version for the pinned exports (they were made with MNN 3.4.0).
- **`run_pool.py` wipes and re-pushes the phone's binaries on every call**, which would break a session running at
  the same time.
- **`--no-controls` still switches the screen off** before each run, with no wake lock.
- **Gate baseline** for the CPU frequency caps is taken immediately after the phone setup, without letting the caps
  settle; `passed_by` (limit vs "stopped cooling") is not saved in the result.
- Exceptions other than a lost connection (including Ctrl+C) skip the end-of-session energy step and leave the
  detached benchmark running on the phone.

**Reporting and docs**
- `results_md.py` has hard-coded text that is now wrong for the screen-off results ("screen at minimum brightness",
  "Q4_K_M (supplementary)" when there is no such section) and labels Q8_0/F16 rows as "other 4-bit types" if pool
  folders are passed; its Sessions table can show sessions from another folder.
- `docs/RESULTS_ANALYSIS.md` still says the full-charge GPU test is untested; it was run and refutes the
  battery-level explanation (see section 4). A few quoted ranges are slightly off (paper implied power for Llama-1B
  is 0.9–10.7 W, not 0.9–13.8 W; "GPU wins prefill on every phone" has one exception).
- `docs/RESULTS.md` shows the Qwen2.5-7B GPU/MNN rows as "not run yet"; the GPU run was attempted and hung the phone.
- README: the example commands omit `--llama-threads 4`; `--min-battery`, `--smallest-first`, `--recover-energy`
  and the folded energy repetitions are not documented.
- `devices.json` has two OnePlus 15 profile ids (one phone would become two report columns); its S23 note quotes a
  29.9–31.2 °C screen-on resting temperature where the recorded gates show 31.6–32.2 °C.
- `pool.py` picks up one non-model string from the SLM_Factory pool file and looks it up on Hugging Face on every
  load; offline, that aborts.

**Test coverage (`tests/fake_phone_test.py`)**
- The simulated phone is wired, so the wireless paths are never exercised: wake lock, airplane-mode logic,
  reconnect, settings restore.
- Not covered: the free-RAM check (the fake has no `MemAvailable`), `--recover-energy`, `--pool-file`, head-version
  output, the battery limit, the "stopped cooling" gate, and `results_md.py`.
- The fake accepts any `pkill` chain, which is why bug 1 went unnoticed. One "check" is a no-op expression.

## 4. Experiments run while investigating

- **GPU decode vs screen, energy trace and battery level** (`results/gpu_decode_*_experiment/`, 28 runs, Llama-1B
  Q4_0, every run started at ≤ 28 °C). Screen on vs off changes GPU decode by 2–5%; the energy trace does not slow
  it (two traces: MNN +15%); full charge (94–97%) gives the same speed as 8–55% (llama.cpp 18.2, MNN 17.7–17.8 t/s).
  The GPU ran at its maximum clock throughout. None of these reproduces the 10–20% slower GPU decode of the first
  screen-off session; given bug 1 and the leftover-sampler bug fixed earlier, a leftover process from a preceding
  aborted run is now the most likely explanation, untested.
- **Phone reboots.** Both were hardware resets (`PonReason.HARD_RESET = 1`, crash records tagged `KP`), each within
  two minutes of a swap-guard stop, with Samsung RAM Plus (8 GB of swap on storage) enabled. RAM Plus is now off and
  bug 1 is fixed. The other drops were the phone switching Wi-Fi networks, which ends Wireless debugging; the
  connection now uses `adb tcpip 5555`, which does not depend on it.

## 5. Verified correct

- llama.cpp timed regions and repetition handling: prefill is `-p 256` at depth 0, decode is `-n 256 -d 256` with the
  context restore outside the timer; per-repetition values match `llama-bench`'s `samples_ts` to 0.01 t/s for every
  configuration, CPU and GPU, both builds.
- MNN: `PB_MARK` brackets exactly `response()`; the first response is dropped by both `llm_bench` and the harness;
  every decode produced 256 tokens on both MNN versions (EOS is ignored).
- Energy: Perfetto timestamps and `PB_MARK` are the same clock (`CLOCK_BOOTTIME`) in the same units; current is in
  mA and negative on discharge, voltage in µV, and the unit chain to µJ/token is right. An independent integration
  of the raw traces gave 34,193 vs 34,186 (prefill) and 238,918 vs 238,823 µJ/token (decode) for Llama-1B on
  llama.cpp CPU; every other configuration is within 0.1%. Shifting all windows by 0.25 s changes results by at
  most 2.9%. The 10 s idle windows are flat and contribute under 0.5% error.
- Model files: Q4_0 files are Q4_0 except the output/tied-embedding matrix (Q6_K, or Q8_0 for Gemma 3 and SmolLM2);
  `Q4_0-PURE` is pure; Q8_0 and F16 are uniform. The pinned OpenCL backend has a Q6_K matmul, so default Q4_0 runs
  fully on the GPU. All 28 MNN exports use the same recipe (block 64, no HQQ); the seven Qwen3.5 exports are
  text-only with `is_visual` false, and the plain text runtime handles their position scheme.
- `docs/RESULTS.md` regenerates identically from its results folder; paper reference values map to the right phone
  columns; the wake-lock helper's argument filling matches `acquireWakeLock` from Android 5 to 14+.
