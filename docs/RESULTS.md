# Table 5 replication on a Galaxy S23

Measured 2026-10-07; generated 2026-10-07T09:08 by `Benchmark-Harness/paper_table5/results_md.py` from `results/galaxy_s23_pinned_20261007`.

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
| Qwen2.5-1.5B | CPU | llama.cpp | 133.7 ± 1.2 | 157.2 | 0.85× | 157.2–417.3 | 27.2 ± 0.4 | 34.5 | 0.79× | 34.5–55.7 |
|  | CPU | MNN | 138.8 ± 4.1 | 259.5 | 0.53× | 228.3–349.5 | 35.4 ± 0.1 | 45.7 | 0.77× | 19.0–49.5 |
|  | GPU | llama.cpp | 126.1 ± 0.1 | 365.7 | 0.34× | 365.7–754.8 | 16.1 ± 0.1 | 31.8 | 0.50× | 31.8–50.3 |
|  | GPU | MNN | 230.8 ± 0.3 | 272.5 | 0.85× | 272.5–434.1 | 11.8 ± 0.1 | 26.2 | 0.45× | 12.3–45.8 |
| Llama-3.2-1B | CPU | llama.cpp | 179.6 ± 2.2 | 166.3 | 1.08× | 166.3–386.3 | 32.6 ± 0.6 | 41.9 | 0.78× | 41.9–76.1 |
|  | CPU | MNN | 191.3 ± 6.8 | 321.9 | 0.59× | 29.8–324.8 | 44.4 ± 0.4 | 55.4 | 0.80× | 3.2–66.6 |
|  | GPU | llama.cpp | 173.8 ± 0.1 | 450.1 | 0.39× | 450.1–986.8 | 18.7 ± 0.2 | 22.5 | 0.83× | 22.5–64.4 |
|  | GPU | MNN | 313.2 ± 1.3 | 371.3 | 0.84× | 371.3–692.1 | 17.9 ± 0.5 | 44.4 | 0.40× | 16.8–60.7 |

## Energy (µJ/token), w4

S23: whole-phone battery energy (Perfetto battery current × voltage) **net of idle power** measured right before each run. Paper: SoC-only energy from Qualcomm powercap counters on rooted phones, reported for the Xiaomi 17 / OnePlus 15 only. The two are not the same quantity (see *Differences* below), so the paper's values are context, not a pass/fail target.

| Model | Backend | Framework | Prefill S23 | Prefill paper (X17 / OP15) | Decode S23 | Decode paper (X17 / OP15) |
|---|---|---|--:|--:|--:|--:|
| Qwen2.5-1.5B | CPU | llama.cpp | n/a | 3.3e4 / 1.5e4 | n/a | 1.2e5 / 9.7e4 |
|  | CPU | MNN | n/a | 2.3e4 / 1.5e4 | n/a | 1.3e5 / 9.2e4 |
|  | GPU | llama.cpp | n/a | 1.0e4 / 1.1e4 | n/a | 1.1e5 / 1.4e5 |
|  | GPU | MNN | n/a | 8.6e3 / 1.7e4 | n/a | 9.4e4 / 9.9e4 |
| Llama-3.2-1B | CPU | llama.cpp | 3.4e4 | 1.1e4 / 1.6e4 | 2.4e5 | 1.4e5 / 7.4e4 |
|  | CPU | MNN | 3.1e4 | 8.7e3 / 1.5e4 | 2.0e5 | 7.9e4 / 6.6e4 |
|  | GPU | llama.cpp | 2.5e4 | 6.3e3 / 5.4e3 | 2.8e5 | 1.4e5 / 1.1e5 |
|  | GPU | MNN | 1.6e4 | 6.9e3 / 1.3e4 | 2.0e5 | 5.5e4 / 7.9e4 |

Markers: `FAIL` = did not run (e.g. out of memory; see the configuration's JSON), `–` = not run yet, `n/a` = no valid energy, `*` = energy recorded but invalid, `^` = started before the cool-down gate passed, `~` = GPU run whose weight type has no OpenCL matmul kernel.

## Run conditions

| Configuration | Battery °C at each run start | CPU busy during runs | Finished |
|---|--:|--:|---|
| qwen2.5-1.5b__Q4_0__llama.cpp__cpu | 31.6–31.8 | 36%–58% | 2026-10-07T06:17 |
| qwen2.5-1.5b__Q4__mnn__cpu | 31.8–32.1 | 53%–56% | 2026-10-07T07:45 |
| qwen2.5-1.5b__Q4_0__llama.cpp__gpu | 31.8–31.8 | 15%–25% | 2026-10-07T07:02 |
| qwen2.5-1.5b__Q4__mnn__gpu | 31.9–32.2 | 17%–23% | 2026-10-07T08:25 |
| llama3.2-1b__Q4_0__llama.cpp__cpu | 31.6–31.8 | 46%–52% | 2026-10-07T03:21 |
| llama3.2-1b__Q4__mnn__cpu | 31.8–31.8 | 48%–58% | 2026-10-07T04:44 |
| llama3.2-1b__Q4_0__llama.cpp__gpu | 31.7–31.8 | 15%–25% | 2026-10-07T04:01 |
| llama3.2-1b__Q4__mnn__gpu | 31.8–31.8 | 17%–22% | 2026-10-07T05:19 |

Across all runs the battery was at 31.6–32.2 °C (median 31.8 °C) when a timed run started.

## Sessions

| Session | Models | Screen | Radios | Cool-down gate |
|---|---|---|---|---|
| 2026-10-07T02:22 | llama3.2-1b, qwen2.5-1.5b (Q4_0, Q4) | on, minimum brightness | mobile data, Bluetooth and location off (Wi-Fi kept for adb) | ≤ 28.0 °C, or stopped cooling (300 s) |

## Differences from the paper's protocol

1. **Energy is whole-phone, net of idle, not SoC-only.** The paper reads Qualcomm powercap counters, which need root; this phone is unrooted and has no Power Stats rails, so energy comes from the battery fuel gauge (`perfetto:current`, chosen by `energy_probe.py`, run `SM-S911U_20261004_235137`). Idle power (screen at minimum brightness, Wi-Fi, OS) is measured for 12 s before every run and subtracted, which removes the constant draw but not the extra DRAM, regulator and board power the workload causes, so these values should read higher than SoC-only energy.
2. **Wireless adb.** Energy needs the USB cable unplugged (it powers the phone), so adb runs over Wi-Fi and Wi-Fi stays on. A screen-off phone with no USB connection suspends every few seconds and freezes the benchmark; the first sessions kept the screen on at minimum brightness, later ones keep it off with a partial wake lock held by the adb shell (`tools/PbWake.java`), as the paper's screen-off protocol. Airplane mode is used when the phone keeps Wi-Fi on in it; otherwise mobile data, Bluetooth and location are switched off. Each session's setting is in *Sessions* above.
3. **Cool-down gate.** The paper cools the phone below 28 °C before each run. Where this phone rests above that (31.6–32.2 °C with the screen on in the first sessions), the gate also passes once the battery temperature has stopped falling (≤ 0.2 °C over 5 minutes, at most 32.5 °C), i.e. the previous run's heat has dissipated; the CPU frequency caps must also be back at their maximum. Start temperatures are listed above.
4. **Battery level.** Battery energy is current × voltage at the battery terminals, so a lower charge (lower voltage) does not bias it directly; the phone ran from about 60% down to 12% during the first session without entering battery saver.
5. **llama.cpp CPU uses 4 threads.** `llama-bench`'s default on this phone is 8 threads (one per core), which puts work on the three Cortex-A510 little cores and makes every thread wait for them: Llama-3.2-1B Q4_0 prefill measured 68 tokens/s at 8 threads standalone and 20.8 tokens/s during a session, against 180 tokens/s at 4 threads (`-t 1,4,8` on 2026-10-07). MNN's default is 4 threads.
6. **Phone generation.** The S23 is not in the paper; the Xiaomi 14 column is its closest comparison. Its Adreno 740 is also a generation older than the Adreno 750 that llama.cpp's Adreno-optimized OpenCL kernels were validated on, which is the likely reason llama.cpp GPU rows trail the paper the most.
7. **MNN conversion** uses MNN 3.4.0's converter (28 commits after `510ac8f`) with `llmexport` defaults (quant block 64, no HQQ), since the paper gives no export settings.
