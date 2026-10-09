# Table 5 replication on a Galaxy S23

Measured 2026-10-07; generated 2026-10-09T01:56 by `Benchmark-Harness/paper_table5/results_md.py` from `results/galaxy_s23_pinned_20261007_screenoff`.

Reference: *arXiv 2607.05475*, Table 5 (256-token prefill and 256-token decode, w4, llama.cpp and MNN on CPU and OpenCL GPU). Paper values are from `paper_reference.json` (its Table 9, which holds Table 5's rows per phone).

## Setup

- **Phone:** Samsung Galaxy S23 (SM-S911U, Snapdragon 8 Gen 2 / SM8550, Adreno 740, 8 GB), Android 16, unrooted, on battery over wireless adb.
- **Builds:** llama.cpp `eadc418` and MNN `510ac8f`, the paper's versions (Table 3), built by `build_binaries.py`; tools `llama-bench` and `llm_bench`.
- **Models:** the paper's four instruct models; llama.cpp GGUF **Q4_0** (main rows) and Q4_K_M (supplementary), MNN 4-bit with `llmexport` defaults.
- **Protocol:** 1 warm-up + 3 recorded repetitions, mean ± std; llama.cpp `pp256` and `tg256` at depth 256; MNN `-p 256 -n 256 -kv true` with EOS ignored.
- **The paper's closest phone is the Xiaomi 14** (Snapdragon 8 Gen 3, one generation newer), so throughput is compared with that column; *ratio* = S23 / Xiaomi 14. The paper's range over its four phones (Xiaomi 17, OnePlus 15, Xiaomi 15, Xiaomi 14) is shown for context.

## Throughput (tokens/s), w4

| Model | Backend | Framework | Prefill S23 | Prefill Xiaomi 14 (paper) | ratio | paper range | Decode S23 | Decode Xiaomi 14 (paper) | ratio | paper range |
|---|---|---|--:|--:|--:|--:|--:|--:|--:|--:|
| Qwen2.5-1.5B | CPU | llama.cpp | 140.7 ± 1.3 | 157.2 | 0.90× | 157.2–417.3 | 28.8 ± 0.5 | 34.5 | 0.84× | 34.5–55.7 |
|  | CPU | MNN | 147.1 ± 5.0 | 259.5 | 0.57× | 228.3–349.5 | 36.0 ± 0.2 | 45.7 | 0.79× | 19.0–49.5 |
|  | GPU | llama.cpp | 126.2 ± 0.1 | 365.7 | 0.35× | 365.7–754.8 | 13.5 ± 0.2 | 31.8 | 0.42× | 31.8–50.3 |
|  | GPU | MNN | 232.3 ± 1.7 | 272.5 | 0.85× | 272.5–434.1 | 9.4 ± 0.3 | 26.2 | 0.36× | 12.3–45.8 |
| Qwen2.5-7B | CPU | llama.cpp | FAIL | 34.1 | – | 34.1–85.4 | FAIL | 8.7 | – | 8.7–14.3 |
|  | CPU | MNN | – | 50.8 | – | 30.0–80.3 | – | 7.2 | – | 7.2–12.2 |
|  | GPU | llama.cpp | – | 84.6 | – | 84.6–171.6 | – | 5.4 | – | 5.4–12.2 |
|  | GPU | MNN | – | 62.4 | – | 62.4–102.1 | – | 11.7 | – | 8.9–12.7 |
| Llama-3.2-1B | CPU | llama.cpp | 190.1 ± 2.2 | 166.3 | 1.14× | 166.3–386.3 | 34.6 ± 0.6 | 41.9 | 0.82× | 41.9–76.1 |
|  | CPU | MNN | 202.7 ± 7.0 | 321.9 | 0.63× | 29.8–324.8 | 45.6 ± 0.3 | 55.4 | 0.82× | 3.2–66.6 |
|  | GPU | llama.cpp | 174.4 ± 0.3 | 450.1 | 0.39× | 450.1–986.8 | 16.8 ± 0.2 | 22.5 | 0.75× | 22.5–64.4 |
|  | GPU | MNN | 315.3 ± 0.2 | 371.3 | 0.85× | 371.3–692.1 | 14.4 ± 0.2 | 44.4 | 0.32× | 16.8–60.7 |
| Llama-3.2-3B | CPU | llama.cpp | 64.0 ± 0.4 | 58.6 | 1.09× | 58.6–119.7 | 14.4 ± 0.3 | 17.1 | 0.84× | 17.1–30.5 |
|  | CPU | MNN | 70.9 ± 2.2 | 113.1 | 0.63× | 66.1–123.4 | 18.0 ± 0.1 | 23.7 | 0.76× | 12.4–24.9 |
|  | GPU | llama.cpp | 66.3 ± 0.1 | 161.1 | 0.41× | 161.1–357.6 | 10.4 ± 0.1 | 11.1 | 0.94× | 11.1–25.2 |
|  | GPU | MNN | 122.2 ± 0.1 | 145.3 | 0.84× | 145.3–248.7 | 8.5 ± 0.1 | 17.1 | 0.50× | 12.1–25.4 |

## Energy (µJ/token), w4

S23: whole-phone battery energy (Perfetto battery current × voltage) **net of idle power** measured right before each run. Paper: SoC-only energy from Qualcomm powercap counters on rooted phones, reported for the Xiaomi 17 / OnePlus 15 only. The two are not the same quantity (see *Differences* below), so the paper's values are context, not a pass/fail target.

| Model | Backend | Framework | Prefill S23 | Prefill paper (X17 / OP15) | Decode S23 | Decode paper (X17 / OP15) |
|---|---|---|--:|--:|--:|--:|
| Qwen2.5-1.5B | CPU | llama.cpp | 4.6e4 | 3.3e4 / 1.5e4 | 2.8e5 | 1.2e5 / 9.7e4 |
|  | CPU | MNN | 4.0e4 | 2.3e4 / 1.5e4 | 2.5e5 | 1.3e5 / 9.2e4 |
|  | GPU | llama.cpp | 3.4e4 | 1.0e4 / 1.1e4 | 2.9e5 | 1.1e5 / 1.4e5 |
|  | GPU | MNN | 2.3e4 | 8.6e3 / 1.7e4 | 2.8e5 | 9.4e4 / 9.9e4 |
| Qwen2.5-7B | CPU | llama.cpp | FAIL | 1.1e5 / 7.9e4 | FAIL | 5.0e5 / 4.4e5 |
|  | CPU | MNN | – | 9.8e4 / 1.0e5 | – | 4.6e5 / 3.9e5 |
|  | GPU | llama.cpp | – | 4.0e4 / 6.5e4 | – | 3.2e5 / 5.7e5 |
|  | GPU | MNN | – | 4.1e4 / 9.1e4 | – | 3.1e5 / 5.1e5 |
| Llama-3.2-1B | CPU | llama.cpp | 3.4e4 | 1.1e4 / 1.6e4 | 2.4e5 | 1.4e5 / 7.4e4 |
|  | CPU | MNN | 3.1e4 | 8.7e3 / 1.5e4 | 2.0e5 | 7.9e4 / 6.6e4 |
|  | GPU | llama.cpp | 2.4e4 | 6.3e3 / 5.4e3 | 2.6e5 | 1.4e5 / 1.1e5 |
|  | GPU | MNN | 1.5e4 | 6.9e3 / 1.3e4 | 2.0e5 | 5.5e4 / 7.9e4 |
| Llama-3.2-3B | CPU | llama.cpp | 9.5e4 | 5.4e4 / 4.1e4 | 5.4e5 | 2.9e5 / 2.1e5 |
|  | CPU | MNN | 8.6e4 | 6.8e4 / 4.4e4 | 4.9e5 | 2.1e5 / 1.8e5 |
|  | GPU | llama.cpp | 7.0e4 | 2.2e4 / 2.1e4 | 5.3e5 | 1.8e5 / 2.5e5 |
|  | GPU | MNN | 4.3e4 | 2.2e4 / 3.5e4 | 4.8e5 | 2.0e5 / 2.0e5 |

Markers: `FAIL` = did not run (e.g. out of memory; see the configuration's JSON), `–` = not run yet, `n/a` = no valid energy, `*` = energy recorded but invalid, `^` = started before the cool-down gate passed, `~` = GPU run whose weight type has no OpenCL matmul kernel.

## Run conditions

| Configuration | Battery °C at each run start | CPU busy during runs | Finished |
|---|--:|--:|---|
| qwen2.5-1.5b__Q4_0__llama.cpp__cpu | 28.0–28.0 | 30%–53% | 2026-10-07T13:59 |
| qwen2.5-1.5b__Q4__mnn__cpu | 28.0–28.0 | 50%–51% | 2026-10-07T14:29 |
| qwen2.5-1.5b__Q4_0__llama.cpp__gpu | 28.0–28.0 | 11%–26% | 2026-10-07T14:13 |
| qwen2.5-1.5b__Q4__mnn__gpu | 27.9–28.0 | 17%–20% | 2026-10-07T14:43 |
| qwen2.5-7b__Q4_0__llama.cpp__cpu | 27.8–27.8 | 34%–34% | 2026-10-07T18:36 |
| llama3.2-1b__Q4_0__llama.cpp__cpu | 24.9–27.9 | 40%–46% | 2026-10-07T13:03 |
| llama3.2-1b__Q4__mnn__cpu | 27.9–28.0 | 50%–52% | 2026-10-07T13:25 |
| llama3.2-1b__Q4_0__llama.cpp__gpu | 28.0–28.0 | 11%–27% | 2026-10-07T13:12 |
| llama3.2-1b__Q4__mnn__gpu | 28.0–28.0 | 13%–18% | 2026-10-07T13:39 |
| llama3.2-3b__Q4_0__llama.cpp__cpu | 27.8–28.0 | 40%–51% | 2026-10-07T15:07 |
| llama3.2-3b__Q4__mnn__cpu | 28.0–28.0 | 36%–50% | 2026-10-07T15:38 |
| llama3.2-3b__Q4_0__llama.cpp__gpu | 27.9–28.0 | 10%–21% | 2026-10-07T15:23 |
| llama3.2-3b__Q4__mnn__gpu | 27.9–28.0 | 10%–16% | 2026-10-07T15:52 |

Across all runs the battery was at 24.9–28.0 °C (median 28.0 °C) when a timed run started.

## Configurations that did not run

- `qwen2.5-7b__Q4_0__llama.cpp__cpu`: does not fit in RAM: 1623 MB of the benchmark process was swapped out (stopped at > 512 MB)

## Sessions

| Session | Models | Screen | Radios | Cool-down gate |
|---|---|---|---|---|
| 2026-10-07T12:55 | llama3.2-1b, qwen2.5-1.5b (Q4_0, Q4) | off, CPU kept awake by a partial wake lock | airplane mode already on (Wi-Fi kept on in it) | ≤ 28.0 °C, or stopped cooling (300 s) |
| 2026-10-07T14:44 | llama3.2-3b, qwen2.5-7b (Q4_0, Q4) | off, CPU kept awake by a partial wake lock | airplane mode already on (Wi-Fi kept on in it) | ≤ 28.0 °C, or stopped cooling (300 s) |
| 2026-10-07T18:18 | qwen2.5-7b (Q4_0, Q4) | off, CPU kept awake by a partial wake lock | airplane mode already on (Wi-Fi kept on in it) | ≤ 28.0 °C, or stopped cooling (300 s) |

## Differences from the paper's protocol

1. **Energy is whole-phone, net of idle, not SoC-only.** The paper reads Qualcomm powercap counters, which need root; this phone is unrooted and has no Power Stats rails, so energy comes from the battery fuel gauge (`perfetto:current`, chosen by `energy_probe.py`, run `SM-S911U_20261004_235137`). Idle power (screen at minimum brightness, Wi-Fi, OS) is measured for 12 s before every run and subtracted, which removes the constant draw but not the extra DRAM, regulator and board power the workload causes, so these values should read higher than SoC-only energy.
2. **Wireless adb.** Energy needs the USB cable unplugged (it powers the phone), so adb runs over Wi-Fi and Wi-Fi stays on. A screen-off phone with no USB connection suspends every few seconds and freezes the benchmark; the first sessions kept the screen on at minimum brightness, later ones keep it off with a partial wake lock held by the adb shell (`tools/PbWake.java`), as the paper's screen-off protocol. Airplane mode is used when the phone keeps Wi-Fi on in it; otherwise mobile data, Bluetooth and location are switched off. Each session's setting is in *Sessions* above.
3. **Cool-down gate.** As in the paper, every run starts with the battery at or below 28 °C (and the CPU frequency caps back at their maximum). With the screen off the phone rests at about 24.4 °C, so the gate passes on the paper's rule; a fallback (pass once the battery has stopped falling, ≤ 0.2 °C over 5 minutes, at most 32.5 °C) exists for a phone that rests above 28 °C, and was needed in the earlier screen-on runs, where the phone rested at 31.6–32.2 °C. Start temperatures are listed above.
4. **Battery level.** Battery energy is current × voltage at the battery terminals, so a lower charge does not bias it directly; sessions stop below 20% charge (`--min-battery`).
5. **llama.cpp CPU uses 4 threads.** `llama-bench`'s default on this phone is 8 threads (one per core), which puts work on the three Cortex-A510 little cores and makes every thread wait for them: Llama-3.2-1B Q4_0 prefill measured 68 tokens/s at 8 threads standalone and 20.8 tokens/s during a session, against 180 tokens/s at 4 threads (`-t 1,4,8` on 2026-10-07). MNN's default is 4 threads.
6. **Phone generation.** The S23 is not in the paper; the Xiaomi 14 column is its closest comparison. Its Adreno 740 is also a generation older than the Adreno 750 that llama.cpp's Adreno-optimized OpenCL kernels were validated on, which is the likely reason llama.cpp GPU rows trail the paper the most.
7. **MNN conversion** uses MNN 3.4.0's converter (28 commits after `510ac8f`) with `llmexport` defaults (quant block 64, no HQQ), since the paper gives no export settings.
