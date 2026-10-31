# Results: four phones in the paper's Table 5 layout

Generated 2026-10-27T13:07 by `Benchmark-Harness/paper_table5/results_all_md.py` from `results/slm_pool_<phone>_*`. Reference: arXiv 2607.05475, Table 5: 256-token prefill and 256-token decode; throughput in tokens/s, energy in µJ/token (tables) or mJ/token (figures; 1 mJ = 1000 µJ).

## Phones and status

| Phone | Hardware | Configurations | Measured | With energy | With peak memory | Cannot run | Not run | Energy method | Screen during runs |
|---|---|--:|--:|--:|--:|--:|--:|---|---|
| Galaxy S23 | Snapdragon 8 Gen 2 (SM8550), Adreno 740, 8 GB (6.9 GiB usable), Android 14 | 104 | 82 | 81 | 82 | 22 | 0 | `perfetto:current` | off + wake lock |
| OnePlus 15 | Snapdragon 8 Elite Gen 5 (SM8850), 16 GB (14.8 GiB usable), Android 16 | 104 | 93 | 93 | 93 | 11 | 0 | `perfetto:current` | off + wake lock |
| OnePlus 8 Pro | Snapdragon 865 (SM8250), Adreno 650, 12 GB (11.2 GiB usable), Android 13 | 104 | 79 | 74 | 79 | 17 | 8 | `bm:current_now` | on, minimum brightness; panel off (system awake) |
| Redmi 9A | Helio G25 (MT6762), 8x Cortex-A53, no OpenCL, 4 GB (3.7 GiB usable), 32-bit Android 11 | 52 | 34 | 34 | 34 | 18 | 0 | `sysfs:current_avg` | off + wake lock |

- **Configurations:** 9 models × 3 precisions × 2 engines × 2 backends without Qwen3.5-4B F16 (never prepared) = 104 per GPU phone; 52 on the Redmi 9A, which has no OpenCL. The Galaxy S23's extra Qwen3.5-4B F16 rows (all four do not fit) are left out so every phone has the same table.
- **Runs:** 1 warm-up + 3 recorded repetitions (mean ± std; slow configurations sometimes 1), llama.cpp with 4 threads, MNN with its default 4; MNN decode timed on the wall clock; every run starts at ≤ 28 °C battery with the CPU frequency caps at their maximum.
- **Energy** is whole-phone battery energy during the timed work minus the idle power measured just before it. The paper's is SoC-only (Qualcomm powercap counters, root), reported for the Xiaomi 17 and OnePlus 15, so ours reads higher (section 6). None of our phones is rooted.
- **Builds:** `pinned` = the paper's llama.cpp `eadc418` / MNN `510ac8f` (Qwen3, SmolLM2, Gemma 3); `head` = llama.cpp `9c2e0e4` / MNN `a8ec497` (Qwen3.5; text decoder only).

## 1. The paper's models next to the paper's four phones

### Throughput (tokens/s), 4-bit

| Model | Backend | Framework | Prefill S23 | OP15 | paper X17 | OP15 | X15 | X14 | Decode S23 | OP15 | paper X17 | OP15 | X15 | X14 |
|---|---|---|--:|--:|--:|--:|--:|--:|--:|--:|--:|--:|--:|--:|
| Qwen2.5-1.5B | CPU | llama.cpp | 140.7 ± 1.3 | 162.5 ± 0.4 | 417.3 | 299.9 | 329.2 | 157.2 | 28.8 ± 0.5 | 41.9 ± 0.1 | 51.6 | 55.7 | 53.9 | 34.5 |
|  | CPU | MNN | 147.1 ± 5.0 | 137.8 ± 45.3 | 349.5 | 228.3 | 259.4 | 259.5 | 35.6 ± 0.2 | 40.6 ± 0.8 | 19.0 | 47.5 | 49.5 | 45.7 |
|  | GPU | llama.cpp | 126.2 ± 0.1 | 763.7 ± 1.9 | 569.7 | 754.8 | 680.9 | 365.7 | 13.5 ± 0.2 | 35.2 ± 0.1 | 38.8 | 50.3 | 48.6 | 31.8 |
|  | GPU | MNN | 232.3 ± 1.7 | 474.4 ± 0.5 | 392.2 | 434.1 | 406.1 | 272.5 | 9.3 ± 0.3 | 27.9 ± 0.2 | 39.1 | 12.3 | 45.8 | 26.2 |
| Llama-3.2-1B | CPU | llama.cpp | 190.1 ± 2.2 | 216.8 ± 0.1 | 326.8 | 287.6 | 386.3 | 166.3 | 34.6 ± 0.6 | 49.0 ± 0.0 | 76.1 | 72.2 | 70.1 | 41.9 |
|  | CPU | MNN | 202.7 ± 7.0 | 184.6 ± 59.3 | 29.8 | 184.8 | 324.8 | 321.9 | 45.1 ± 0.3 | 51.7 ± 1.0 | 3.2 | 61.6 | 66.6 | 55.4 |
|  | GPU | llama.cpp | 174.4 ± 0.3 | 1101.1 ± 1.9 | 744.0 | 986.8 | 862.1 | 450.1 | 16.8 ± 0.2 | 40.1 ± 0.1 | 46.1 | 64.4 | 57.1 | 22.5 |
|  | GPU | MNN | 315.3 ± 0.2 | 686.1 ± 0.3 | 692.1 | 669.8 | 534.4 | 371.3 | 14.2 ± 0.2 | 38.8 ± 0.0 | 16.8 | 21.6 | 60.7 | 44.4 |
| Llama-3.2-3B | CPU | llama.cpp | 64.0 ± 0.4 | 77.8 ± 0.2 | 119.7 | 109.8 | 114.8 | 58.6 | 14.4 ± 0.3 | 21.5 ± 0.0 | 29.9 | 30.5 | 27.1 | 17.1 |
|  | CPU | MNN | 70.9 ± 2.2 | 66.5 ± 22.0 | 123.4 | 66.1 | 90.8 | 113.1 | 17.9 ± 0.1 | 21.6 ± 0.0 | 12.4 | 24.9 | 24.0 | 23.7 |
|  | GPU | llama.cpp | 66.3 ± 0.1 | 423.2 ± 0.5 | 224.8 | 357.6 | 306.6 | 161.1 | 10.4 ± 0.1 | 20.4 ± 0.0 | 20.0 | 25.0 | 25.2 | 11.1 |
|  | GPU | MNN | 122.2 ± 0.1 | 259.1 ± 0.1 | 201.2 | 248.7 | 205.3 | 145.3 | 8.4 ± 0.1 | 24.6 ± 0.1 | 24.4 | 12.1 | 25.4 | 17.1 |
| Qwen2.5-7B | CPU | llama.cpp | – | 36.9 ± 0.1 | 85.4 | 58.6 | 55.1 | 34.1 | – | 11.0 ± 0.0 | 12.6 | 14.3 | 13.2 | 8.7 |
|  | CPU | MNN | – | 29.9 ± 10.4 | 80.3 | 30.0 | 58.4 | 50.8 | – | 10.5 ± 0.1 | 9.6 | 12.2 | 12.2 | 7.2 |
|  | GPU | llama.cpp | – | 198.1 ± 3.1 | 137.5 | 171.6 | 141.8 | 84.6 | – | 11.3 ± 0.0 | 10.2 | 12.2 | 11.7 | 5.4 |
|  | GPU | MNN | – | 97.4 ± 2.8 | 78.4 | 102.1 | 86.2 | 62.4 | – | 11.5 ± 0.0 | 8.9 | 10.6 | 12.7 | 11.7 |

X17 = Xiaomi 17, OP15 = OnePlus 15, X15 = Xiaomi 15, X14 = Xiaomi 14 (paper values). OnePlus 8 Pro and Redmi 9A did not run the paper's models (only the SLM_Factory pool, sections 3–4). Qwen2.5-7B does not fit in the S23's 8 GB (–). OnePlus 15 paper-model rows: 1 warm-up + 3 repetitions, wake/sleep key pair before each run; four cells (Llama-3.2-1B and Qwen2.5-1.5B llama.cpp CPU, Qwen2.5-1.5B and Qwen2.5-7B MNN CPU) are reruns after a slow-governor first pass or a swap-guard stop (first-pass prefill: 92.3, 28.8, 111.7 tokens/s and failed).

### Energy (µJ/token), 4-bit

| Model | Backend | Framework | Prefill S23 | OP15 | paper X17 | OP15 | Decode S23 | OP15 | paper X17 | OP15 |
|---|---|---|--:|--:|--:|--:|--:|--:|--:|--:|
| Qwen2.5-1.5B | CPU | llama.cpp | 4.6e4 | 1.2e4 | 3.3e4 | 1.5e4 | 2.8e5 | 9.5e4 | 1.2e5 | 9.7e4 |
|  | CPU | MNN | 4.0e4 | 1.8e4 | 2.3e4 | 1.5e4 | 2.6e5 | 8.3e4 | 1.3e5 | 9.2e4 |
|  | GPU | llama.cpp | 3.4e4 | 8.1e3 | 1.0e4 | 1.1e4 | 2.9e5 | 1.3e5 | 1.1e5 | 1.4e5 |
|  | GPU | MNN | 2.3e4 | 1.1e4 | 8.6e3 | 1.7e4 | 2.9e5 | 1.1e5 | 9.4e4 | 9.9e4 |
| Llama-3.2-1B | CPU | llama.cpp | 3.4e4 | 9.3e3 | 1.1e4 | 1.6e4 | 2.4e5 | 8.1e4 | 1.4e5 | 7.4e4 |
|  | CPU | MNN | 3.1e4 | 1.3e4 | 8.7e3 | 1.5e4 | 2.1e5 | 6.2e4 | 7.9e4 | 6.6e4 |
|  | GPU | llama.cpp | 2.4e4 | 4.2e3 | 6.3e3 | 5.4e3 | 2.6e5 | 1.1e5 | 1.4e5 | 1.1e5 |
|  | GPU | MNN | 1.5e4 | 8.1e3 | 6.9e3 | 1.3e4 | 2.0e5 | 8.5e4 | 5.5e4 | 7.9e4 |
| Llama-3.2-3B | CPU | llama.cpp | 9.5e4 | 2.9e4 | 5.4e4 | 4.1e4 | 5.4e5 | 2.1e5 | 2.9e5 | 2.1e5 |
|  | CPU | MNN | 8.6e4 | 3.9e4 | 6.8e4 | 4.4e4 | 4.9e5 | 1.5e5 | 2.1e5 | 1.8e5 |
|  | GPU | llama.cpp | 7.0e4 | 1.4e4 | 2.2e4 | 2.1e4 | 5.3e5 | 2.4e5 | 1.8e5 | 2.5e5 |
|  | GPU | MNN | 4.3e4 | 2.2e4 | 2.2e4 | 3.5e4 | 4.9e5 | 2.1e5 | 2.0e5 | 2.0e5 |
| Qwen2.5-7B | CPU | llama.cpp | – | 6.2e4 | 1.1e5 | 7.9e4 | – | 4.2e5 | 5.0e5 | 4.4e5 |
|  | CPU | MNN | – | 1.1e5 | 9.8e4 | 1.0e5 | – | 3.4e5 | 4.6e5 | 3.9e5 |
|  | GPU | llama.cpp | – | 3.5e4 | 4.0e4 | 6.5e4 | – | 4.8e5 | 3.2e5 | 5.7e5 |
|  | GPU | MNN | – | 4.9e4 | 4.1e4 | 9.1e4 | – | 4.4e5 | 3.1e5 | 5.1e5 |

Energy is whole-phone net of idle (ours) against SoC-only (paper), so ours should read higher; on the OnePlus 15 most cells read lower instead (section 2).

## 2. How our phones compare with the paper

![Throughput vs paper](figures/all_t5_throughput_vs_paper.png)

*Prefill (top) and decode (bottom) throughput per framework and backend for the phones that ran the paper's models, next to the median of the paper's four phones; the dark bracket is the paper's range.*

![Ratio vs paper](figures/all_t5_ratio_vs_paper.png)

*Each row is one configuration: our throughput divided by the paper's value for the same phone (OnePlus 15) or its closest phone (S23: Xiaomi 14). The shaded band is ±25%.*

- **Galaxy S23** (proxy: the Xiaomi 14, one chip generation newer): 13 of 24 throughput cells within ±25%; median ratio 0.74 for prefill, 0.77 for decode.
- **OnePlus 15** (the same phone: the OnePlus 15 is one of the paper's four): 21 of 32 throughput cells within ±25%; median ratio 1.00 for prefill, 0.85 for decode. Energy per token against the paper's own OnePlus 15 values: median 0.73× for prefill, 0.95× for decode; ours is whole-phone net of idle and the paper's SoC-only, so ours should not read lower, yet prefill does in most cells. The paper's SoC counters and our idle-subtracted battery reading split the phone's power differently, so treat the absolute comparison as approximate.
- **OnePlus 8 Pro and Redmi 9A** are not in the paper and have no close proxy (the Snapdragon 865 is four generations older than the Xiaomi 14; the Helio G25 is an entry-level chip), so they are compared with the other phones on the pool in sections 3–4.

## 3. SLM_Factory model pool on every phone

Pool: `config/android_pool.py` on SLM_Factory `main`. Quantizations: llama.cpp Q4_0 / Q8_0 / F16, MNN 4 / 8 / 16-bit. **Storage** is the size of the model files on the phone (GGUF file or MNN export directory), in MB (10^6 bytes), the same on every phone. **Peak memory** is the inference process's largest total memory (PSS from `dumpsys meminfo`, GPU buffers included) during one 256 + 256-token run, measured by `memory_usage.py`; the GPU share is in brackets; † = crashed after completing the workload. `–` = not measured on that phone. FAIL = the configuration cannot run there; the reason is in the memory column and section 7.

![Prefill throughput, all phones](figures/all_prefill_tps.png)

![Decode throughput, all phones](figures/all_decode_tps.png)

*Every configuration: one column per phone, one row per precision, models smallest to largest; one line per engine and backend, all panels on one log scale. A gap means that configuration could not run (does not fit, crashed, no OpenCL). Shaded columns are Qwen3.5, which runs on the newer builds, so jumps there partly reflect the build.*

![Storage](figures/all_storage.png)

![Peak memory, all phones](figures/all_peak_memory.png)

![Memory vs storage](figures/all_memory_vs_storage.png)

*Storage per model and precision (identical on every phone), peak memory during inference per phone, and the two against each other: points above the grey line need more memory than the model's file size (GPU runs allocate buffers for weights, KV cache and work space, and llama.cpp GPU keeps a CPU copy too).*

![Decode vs model size](figures/all_decode_vs_model_size.png)

*Decode speed against model file size per phone. Decode reads all weights once per token, so points on a grey line share one effective memory bandwidth.*

### 3.1 Galaxy S23

Snapdragon 8 Gen 2 (SM8550), Adreno 740, 8 GB (6.9 GiB usable), Android 14. 104 configurations: 82 measured (81 with energy, 82 with peak memory), 22 cannot run, 0 not run.

| Model | Backend | Framework | Quant. | Storage MB | Peak memory MB | Prefill t/s | Prefill µJ/token | Decode t/s | Decode µJ/token |
|---|---|---|---|--:|--:|--:|--:|--:|--:|
| SmolLM2-135M | CPU | llama.cpp | Q4_0 | 92 | 220 | 793.4 ± 0.3 | 7.9e3 | 204.5 ± 1.5 | 4.2e4 |
|  | CPU | MNN | Q4 | 87 | 162 | 1518.0 ± 6.4 | 4.7e3 | 224.3 ± 1.7 | 4.0e4 |
|  | GPU | llama.cpp | Q4_0 | 92 | 747 (656 GPU) | 409.7 ± 1.7 | 8.4e3 | 39.6 ± 0.5 | 7.1e4 |
|  | GPU | MNN | Q4 | 87 | 259 (135 GPU) | 1725.1 ± 97.8 | 1.8e3 | 9.1 ± 0.1 | 1.0e5 |
|  | CPU | llama.cpp | Q8_0 | 145 | 326 | 786.1 ± 0.2 | 8.2e3 | 168.6 ± 1.7 | 5.2e4 |
|  | CPU | MNN | Q8 | 154 | 230 | 1508.5 ± 73.7 | 5.0e3 | 174.9 ± 5.0 | 5.1e4 |
|  | GPU | llama.cpp | Q8_0 | 145 | 821 (709 GPU) | 25.8 ± 0.0 | 1.3e5 | 67.3 ± 3.1 | 8.7e4 |
|  | GPU | MNN | Q8 | 154 | 329 (203 GPU) | 1753.2 ± 18.2 | 1.9e3 | 9.1 ± 0.1 | 1.2e5 |
|  | CPU | llama.cpp | F16 | 271 | 309 | 119.7 ± 0.0 | 4.7e4 | 68.5 ± 1.5 | 1.1e5 |
|  | CPU | MNN | F16 | 272 | 340 | 976.4 ± 159.6 | 6.2e3 | 111.8 ± 4.8 | 7.6e4 |
|  | GPU | llama.cpp | F16 | 271 | 1,221 (835 GPU) | 23.3 ± 0.0 | 1.5e5 | 64.7 ± 2.3 | 1.1e5 |
|  | GPU | MNN | F16 | 272 | crashed | FAIL | FAIL | FAIL | FAIL |
| Gemma-3-270M | CPU | llama.cpp | Q4_0 | 241 | 534 | 799.2 ± 0.1 | 8.3e3 | 132.5 ± 2.8 | 7.0e4 |
|  | CPU | MNN | Q4 | 176 | 273 | 1510.8 ± 147.8 | 4.6e3 | 107.7 ± 2.2 | 8.1e4 |
|  | GPU | llama.cpp | Q4_0 | 241 | 1,498 (1,235 GPU) | 360.8 ± 5.4 | 1.0e4 | 33.3 ± 1.9 | 9.3e4 |
|  | GPU | MNN | Q4 | 176 | 379 (213 GPU) | 1622.0 ± 7.5 | 1.9e3 | 9.0 ± 0.1 | 1.2e5 |
|  | CPU | llama.cpp | Q8_0 | 292 | 634 | 790.2 ± 0.1 | 9.0e3 | 117.8 ± 1.4 | 8.1e4 |
|  | CPU | MNN | Q8 | 310 | 567 | 1465.2 ± 127.9 | 4.8e3 | 82.2 ± 1.5 | 1.1e5 |
|  | GPU | llama.cpp | Q8_0 | 292 | 1,546 (1,284 GPU) | 26.8 ± 0.1 | 1.2e5 | 35.6 ± 0.5 | 1.1e5 |
|  | GPU | MNN | Q8 | 310 | 515 (349 GPU) | 1588.5 ± 6.6 | 2.0e3 | 8.9 ± 0.1 | 1.4e5 |
|  | CPU | llama.cpp | F16 | 543 | 601 | 126.7 ± 0.0 | 4.5e4 | 38.4 ± 0.7 | 2.0e5 |
|  | CPU | MNN | F16 | 544 | 1,487 | 1082.3 ± 43.3 | 6.0e3 | 56.2 ± 1.7 | 1.6e5 |
|  | GPU | llama.cpp | F16 | 543 | 2,486 (1,535 GPU) | 24.4 ± 0.0 | 1.4e5 | 29.3 ± 0.9 | 1.4e5 |
|  | GPU | MNN | F16 | 544 | crashed | FAIL | FAIL | FAIL | FAIL |
| SmolLM2-360M | CPU | llama.cpp | Q4_0 | 229 | 508 | 366.4 ± 4.1 | 1.8e4 | 95.2 ± 2.6 | 8.9e4 |
|  | CPU | MNN | Q4 | 229 | 323 | 554.2 ± 44.3 | 1.2e4 | 115.2 ± 1.4 | 7.9e4 |
|  | GPU | llama.cpp | Q4_0 | 229 | 961 (803 GPU) | 218.6 ± 0.6 | 1.7e4 | 49.5 ± 3.1 | 1.1e5 |
|  | GPU | MNN | Q4 | 229 | 433 (276 GPU) | 763.1 ± 5.8 | 5.2e3 | 9.1 ± 1.1 | 1.4e5 |
|  | CPU | llama.cpp | Q8_0 | 386 | 824 | 348.1 ± 7.2 | 1.9e4 | 75.2 ± 1.0 | 1.2e5 |
|  | CPU | MNN | Q8 | 410 | 499 | 553.0 ± 37.0 | 1.2e4 | 79.9 ± 0.9 | 1.1e5 |
|  | GPU | llama.cpp | Q8_0 | 386 | 1,324 (960 GPU) | 8.9 ± 0.0 | 3.8e5 | 31.6 ± 0.4 | 1.5e5 |
|  | GPU | MNN | Q8 | 410 | 614 (457 GPU) | 734.8 ± 4.1 | 5.3e3 | 8.6 ± 0.3 | 1.7e5 |
|  | CPU | llama.cpp | F16 | 726 | 779 | 42.1 ± 0.0 | 1.3e5 | 25.9 ± 0.6 | 2.9e5 |
|  | CPU | MNN | F16 | 727 | 826 | 405.1 ± 13.7 | 1.5e4 | 40.8 ± 1.9 | 2.0e5 |
|  | GPU | llama.cpp | F16 | 726 | 2,179 (1,300 GPU) | 7.7 ± 0.0 | 4.4e5 | 26.0 ± 0.3 | 1.8e5 |
|  | GPU | MNN | F16 | 727 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3-0.6B | CPU | llama.cpp | Q4_0 | 469 | 832 | 232.0 ± 2.0 | 2.7e4 | 49.8 ± 1.1 | 1.6e5 |
|  | CPU | MNN | Q4 | 378 | 548 | 370.8 ± 17.7 | 1.7e4 | 72.7 ± 0.8 | 1.3e5 |
|  | GPU | llama.cpp | Q4_0 | 469 | 1,506 (1,201 GPU) | 132.6 ± 0.2 | 2.7e4 | 14.9 ± 0.0 | 1.9e5 |
|  | GPU | MNN | Q4 | 378 | 696 (454 GPU) | 558.9 ± 1.4 | 8.0e3 | 9.7 ± 1.3 | 1.7e5 |
|  | CPU | llama.cpp | Q8_0 | 805 | 1,390 | 216.4 ± 3.0 | 2.9e4 | 47.1 ± 0.4 | 1.9e5 |
|  | CPU | MNN | Q8 | 676 | 856 | 368.0 ± 14.0 | 1.8e4 | 49.8 ± 0.3 | 1.8e5 |
|  | GPU | llama.cpp | Q8_0 | 805 | 1,970 (1,458 GPU) | 6.3 ± 0.0 | 5.4e5 | 26.5 ± 0.6 | 1.9e5 |
|  | GPU | MNN | Q8 | 676 | 1,005 (753 GPU) | 539.7 ± 1.6 | 8.4e3 | 8.4 ± 0.1 | 2.0e5 |
|  | CPU | llama.cpp | F16 | 1,509 | 1,626 | 29.6 ± 0.0 | 1.8e5 | 16.4 ± 0.2 | 4.3e5 |
|  | CPU | MNN | F16 | 1,198 | 1,721 | 240.3 ± 17.3 | 2.4e4 | 26.5 ± 3.3 | 3.4e5 |
|  | GPU | llama.cpp | F16 | 1,509 | 3,602 (2,016 GPU) | 5.3 ± 0.0 | 6.6e5 | 22.2 ± 0.3 | 2.7e5 |
|  | GPU | MNN | F16 | 1,198 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-0.8B | CPU | llama.cpp | Q4_0 | 513 | 1,114 | 291.4 ± 5.8 | – | 43.0 ± 0.9 | 2.0e5 |
|  | CPU | MNN | Q4 | 440 | 691 | 273.4 ± 10.3 | 2.2e4 | 73.6 ± 0.1 | 1.2e5 |
|  | GPU | llama.cpp | Q4_0 | 513 | 1,310 (764 GPU) | 157.2 ± 1.4 | 2.2e4 | 13.4 ± 0.1 | 2.5e5 |
|  | GPU | MNN | Q4 | 440 | 712 (510 GPU) | 454.4 ± 7.5 | 7.5e3 | 46.1 ± 0.3 | 1.1e5 |
|  | CPU | llama.cpp | Q8_0 | 834 | 1,734 | 260.2 ± 2.3 | 2.6e4 | 41.2 ± 0.3 | 2.2e5 |
|  | CPU | MNN | Q8 | 816 | 1,345 | 273.9 ± 8.9 | 2.3e4 | 47.0 ± 0.2 | 2.0e5 |
|  | GPU | llama.cpp | Q8_0 | 834 | 2,164 (1,769 GPU) | 162.5 ± 1.1 | 2.2e4 | 15.2 ± 0.2 | 2.8e5 |
|  | GPU | MNN | Q8 | 816 | 1,083 (886 GPU) | 379.1 ± 7.0 | 1.0e4 | 35.1 ± 0.1 | 1.8e5 |
|  | CPU | llama.cpp | F16 | 1,558 | 1,641 | 26.4 ± 0.3 | 1.8e5 | 13.7 ± 0.2 | 5.3e5 |
|  | CPU | MNN | F16 | 1,521 | 3,135 | 217.0 ± 5.6 | 2.8e4 | 20.9 ± 0.7 | 3.8e5 |
|  | GPU | llama.cpp | F16 | 1,558 | 5,376 (3,239 GPU) | 159.1 ± 1.7 | 2.7e4 | 11.2 ± 0.1 | 4.0e5 |
|  | GPU | MNN | F16 | 1,521 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3-1.7B | CPU | llama.cpp | Q4_0 | 1,229 | 2,140 | 119.2 ± 1.1 | 5.3e4 | 24.4 ± 0.5 | 3.3e5 |
|  | CPU | MNN | Q4 | 1,081 | 1,290 | 137.6 ± 4.2 | 4.4e4 | 31.3 ± 0.3 | 3.0e5 |
|  | GPU | llama.cpp | Q4_0 | 1,229 | 3,069 (1,876 GPU) | 106.8 ± 0.1 | 3.9e4 | 12.2 ± 0.0 | 3.5e5 |
|  | GPU | MNN | Q4 | 1,081 | 1,443 (1,099 GPU) | 219.1 ± 0.4 | 2.5e4 | 8.5 ± 0.1 | 3.0e5 |
|  | CPU | llama.cpp | Q8_0 | 2,165 | 3,787 | 106.5 ± 1.1 | 6.3e4 | 20.5 ± 0.2 | 4.2e5 |
|  | CPU | MNN | Q8 | 1,942 | 2,397 | 132.4 ± 2.6 | 4.7e4 | 20.4 ± 0.1 | 4.0e5 |
|  | GPU | llama.cpp | Q8_0 | 2,165 | 4,429 (2,655 GPU) | 2.0 ± 0.0 | 1.5e6 | 16.3 ± 0.2 | 3.7e5 |
|  | GPU | MNN | Q8 | 1,942 | 2,309 (1,960 GPU) | 211.1 ± 0.2 | 2.8e4 | 8.0 ± 0.1 | 4.4e5 |
|  | CPU | llama.cpp | F16 | 4,070 | 4,196 | 9.2 ± 0.1 | 5.5e5 | 5.7 ± 0.4 | 1.1e6 |
|  | CPU | MNN | F16 | 3,447 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | F16 | 4,070 | 7,795 (4,268 GPU) | 1.7 ± 0.0 | 2.2e6 | 11.2 ± 0.1 | 6.0e5 |
|  | GPU | MNN | F16 | 3,447 | does not fit | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-2B | CPU | llama.cpp | Q4_0 | 1,237 | 2,522 | 131.9 ± 1.4 | 4.9e4 | 21.2 ± 0.4 | 3.7e5 |
|  | CPU | MNN | Q4 | 1,076 | 1,236 | 134.4 ± 3.5 | 4.7e4 | 34.1 ± 0.6 | 3.0e5 |
|  | GPU | llama.cpp | Q4_0 | 1,237 | 2,247 (1,280 GPU) | 108.8 ± 0.8 | 2.9e4 | 11.3 ± 0.2 | 3.5e5 |
|  | GPU | MNN | Q4 | 1,076 | 1,458 (1,154 GPU) | 251.9 ± 2.6 | 1.5e4 | 28.0 ± 0.1 | 2.3e5 |
|  | CPU | llama.cpp | Q8_0 | 2,077 | 4,141 | 112.3 ± 8.4 | 6.1e4 | 19.4 ± 0.5 | 4.5e5 |
|  | CPU | MNN | Q8 | 2,016 | 2,685 | 128.1 ± 1.6 | 5.0e4 | 20.9 ± 0.1 | 4.2e5 |
|  | GPU | llama.cpp | Q8_0 | 2,077 | 3,650 (2,984 GPU) | 113.0 ± 2.3 | 3.5e4 | 11.3 ± 0.1 | 4.8e5 |
|  | GPU | MNN | Q8 | 2,016 | 3,315 (2,531 GPU) | 203.1 ± 3.9 | 2.3e4 | 19.0 ± 0.0 | 3.9e5 |
|  | CPU | llama.cpp | F16 | 3,897 | 3,907 | 9.4 ± 0.1 | 4.9e5 | 5.2 ± 0.3 | 1.2e6 |
|  | CPU | MNN | F16 | 3,780 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | F16 | 3,897 | load failed | FAIL | FAIL | FAIL | FAIL |
|  | GPU | MNN | F16 | 3,780 | does not fit | FAIL | FAIL | FAIL | FAIL |
| Qwen3-4B | CPU | llama.cpp | Q4_0 | 2,370 | 4,513 | 44.5 ± 0.5 | 1.3e5 | 10.8 ± 0.2 | 6.8e5 |
|  | CPU | MNN | Q4 | 2,521 | 2,782 | 53.5 ± 1.9 | 1.1e5 | 14.1 ± 0.1 | 6.0e5 |
|  | GPU | llama.cpp | Q4_0 | 2,370 | 5,400 (3,209 GPU) | 40.4 ± 0.2 | 1.1e5 | 8.7 ± 0.1 | 6.5e5 |
|  | GPU | MNN | Q4 | 2,521 | 2,966 (2,438 GPU) | 88.4 ± 0.0 | 6.9e4 | 6.7 ± 0.0 | 6.2e5 |
|  | CPU | llama.cpp | Q8_0 | 4,280 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q8 | 4,532 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | Q8_0 | 4,280 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | MNN | Q8 | 4,532 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | llama.cpp | F16 | 8,051 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | F16 | 8,052 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | F16 | 8,051 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | MNN | F16 | 8,052 | does not fit | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-4B | CPU | llama.cpp | Q4_0 | 2,612 | 4,893 | 50.8 ± 1.5 | 1.2e5 | 10.0 ± 0.2 | 7.6e5 |
|  | CPU | MNN | Q4 | 2,389 | 2,609 | 49.9 ± 1.3 | 1.1e5 | 14.6 ± 0.4 | 5.8e5 |
|  | GPU | llama.cpp | Q4_0 | 2,612 | 3,775 (2,594 GPU) | 47.5 ± 0.3 | 8.5e4 | 6.5 ± 0.0 | 7.4e5 |
|  | GPU | MNN | Q4 | 2,389 | 3,033 (2,523 GPU) | 114.5 ± 0.2 | 4.0e4 | 15.3 ± 0.0 | 5.6e5 |
|  | CPU | llama.cpp | Q8_0 | 4,611 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q8 | 4,491 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | Q8_0 | 4,611 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | MNN | Q8 | 4,491 | does not fit | FAIL | FAIL | FAIL | FAIL |

`–` energy (1 rows): `qwen3.5-0.8b__Q4_0__llama.cpp__cpu__head` (prefill of the 2026-10-12 energy rerun ran 17x slower than every other measurement of this configuration (16.1 tokens/s vs 277-291 in the 2026-10-08 run and 269 by hand on 2026-10-14) with decode unchanged; the prefill throughput is from the 2026-10-08 run and prefill energy is not available. The anomalous run is kept in anomalies/.).

Prefill slower than decode (10 llama.cpp GPU rows): `smollm2-135m__Q8_0__llama.cpp__gpu`, `smollm2-135m__F16__llama.cpp__gpu`, `gemma3-270m__Q8_0__llama.cpp__gpu`, `gemma3-270m__F16__llama.cpp__gpu`, `smollm2-360m__Q8_0__llama.cpp__gpu`, `smollm2-360m__F16__llama.cpp__gpu`, `qwen3-0.6b__Q8_0__llama.cpp__gpu`, `qwen3-0.6b__F16__llama.cpp__gpu`, `qwen3-1.7b__Q8_0__llama.cpp__gpu`, `qwen3-1.7b__F16__llama.cpp__gpu`. A slow OpenCL prefill path of that llama.cpp build for these weight types on this GPU (section 5).

### 3.2 OnePlus 15

Snapdragon 8 Elite Gen 5 (SM8850), 16 GB (14.8 GiB usable), Android 16. 104 configurations: 93 measured (93 with energy, 93 with peak memory), 11 cannot run, 0 not run.

| Model | Backend | Framework | Quant. | Storage MB | Peak memory MB | Prefill t/s | Prefill µJ/token | Decode t/s | Decode µJ/token |
|---|---|---|---|--:|--:|--:|--:|--:|--:|
| SmolLM2-135M | CPU | llama.cpp | Q4_0 | 92 | 220 | 1032.0 ± 1.8 | 1.3e3 | 260.9 ± 0.2 | 1.2e4 |
|  | CPU | MNN | Q4 | 87 | 170 | 1027.0 ± 3.5 | 1.2e3 | 205.6 ± 0.3 | 9.9e3 |
|  | GPU | llama.cpp | Q4_0 | 92 | 791 (694 GPU) | 3634.0 ± 11.1 | 1.1e3 | 93.2 ± 2.9 | 3.2e4 |
|  | GPU | MNN | Q4 | 87 | 274 (125 GPU) | 3666.1 ± 12.2 | 9.0e2 | 42.2 ± 0.1 | 3.6e4 |
|  | CPU | llama.cpp | Q8_0 | 145 | 326 | 1045.1 ± 0.5 | 1.4e3 | 226.1 ± 0.6 | 1.3e4 |
|  | CPU | MNN | Q8 | 154 | 238 | 1087.3 ± 8.3 | 1.6e3 | 184.9 ± 0.1 | 1.6e4 |
|  | GPU | llama.cpp | Q8_0 | 145 | 846 (748 GPU) | 1895.8 ± 11.2 | 1.4e3 | 99.3 ± 0.5 | 3.4e4 |
|  | GPU | MNN | Q8 | 154 | 342 (192 GPU) | 3625.6 ± 11.4 | 9.0e2 | 40.0 ± 0.4 | 3.8e4 |
|  | CPU | llama.cpp | F16 | 271 | 309 | 260.4 ± 2.1 | 8.7e3 | 118.5 ± 0.5 | 2.7e4 |
|  | CPU | MNN | F16 | 272 | 352 (1 GPU) | 845.1 ± 261.3 | 2.0e3 | 126.7 ± 0.4 | 1.8e4 |
|  | GPU | llama.cpp | F16 | 271 | 1,265 (872 GPU) | 2429.8 ± 7.0 | 1.0e3 | 97.5 ± 1.1 | 3.2e4 |
|  | GPU | MNN | F16 | 272 | crashed | FAIL | FAIL | FAIL | FAIL |
| Gemma-3-270M | CPU | llama.cpp | Q4_0 | 241 | 535 | 1090.2 ± 1.2 | 1.8e3 | 185.6 ± 0.8 | 1.6e4 |
|  | CPU | MNN | Q4 | 176 | 285 | 1278.4 ± 302.1 | 1.4e3 | 116.0 ± 0.2 | 2.0e4 |
|  | GPU | llama.cpp | Q4_0 | 241 | 1,538 (1,272 GPU) | 2874.9 ± 4.4 | 1.3e3 | 73.2 ± 2.8 | 3.6e4 |
|  | GPU | MNN | Q4 | 176 | 391 (204 GPU) | 3888.8 ± 4.1 | 9.0e2 | 27.4 ± 0.2 | 4.4e4 |
|  | CPU | llama.cpp | Q8_0 | 292 | 634 | 1105.3 ± 1.5 | 1.9e3 | 169.2 ± 0.1 | 2.1e4 |
|  | CPU | MNN | Q8 | 310 | 694 | 1329.3 ± 288.5 | 1.7e3 | 101.3 ± 0.3 | 2.7e4 |
|  | GPU | llama.cpp | Q8_0 | 292 | 1,591 (1,323 GPU) | 1551.0 ± 1.8 | 1.6e3 | 84.6 ± 1.1 | 3.8e4 |
|  | GPU | MNN | Q8 | 310 | 524 (338 GPU) | 3614.5 ± 10.1 | 8.4e2 | 23.8 ± 0.0 | 5.2e4 |
|  | CPU | llama.cpp | F16 | 543 | 602 | 282.4 ± 0.3 | 8.1e3 | 81.8 ± 0.1 | 4.5e4 |
|  | CPU | MNN | F16 | 544 | 1,528 | 861.4 ± 253.8 | 2.4e3 | 67.9 ± 0.0 | 4.3e4 |
|  | GPU | llama.cpp | F16 | 543 | 2,534 (1,574 GPU) | 1887.6 ± 4.2 | 1.5e3 | 55.8 ± 0.7 | 5.3e4 |
|  | GPU | MNN | F16 | 544 | crashed | FAIL | FAIL | FAIL | FAIL |
| SmolLM2-360M | CPU | llama.cpp | Q4_0 | 229 | 509 | 456.2 ± 2.7 | 4.0e3 | 124.5 ± 0.1 | 2.4e4 |
|  | CPU | MNN | Q4 | 229 | 340 | 488.3 ± 132.8 | 3.4e3 | 114.6 ± 0.4 | 2.3e4 |
|  | GPU | llama.cpp | Q4_0 | 229 | 1,009 (841 GPU) | 1829.9 ± 0.5 | 2.5e3 | 85.6 ± 0.1 | 4.4e4 |
|  | GPU | MNN | Q4 | 229 | 444 (267 GPU) | 1653.0 ± 3.2 | 2.6e3 | 39.3 ± 0.2 | 5.4e4 |
|  | CPU | llama.cpp | Q8_0 | 386 | 824 | 472.2 ± 0.4 | 4.3e3 | 98.4 ± 4.1 | 3.3e4 |
|  | CPU | MNN | Q8 | 410 | 509 | 523.8 ± 143.4 | 3.9e3 | 99.6 ± 0.3 | 3.0e4 |
|  | GPU | llama.cpp | Q8_0 | 386 | 1,326 (999 GPU) | 678.7 ± 0.6 | 3.1e3 | 79.6 ± 0.0 | 5.3e4 |
|  | GPU | MNN | Q8 | 410 | 624 (449 GPU) | 1562.0 ± 2.9 | 2.4e3 | 37.0 ± 0.3 | 6.4e4 |
|  | CPU | llama.cpp | F16 | 726 | 779 | 95.5 ± 0.1 | 2.5e4 | 50.3 ± 0.0 | 7.2e4 |
|  | CPU | MNN | F16 | 727 | 1,035 | 326.2 ± 112.0 | 7.2e3 | 58.4 ± 0.1 | 5.8e4 |
|  | GPU | llama.cpp | F16 | 726 | 2,225 (1,339 GPU) | 901.2 ± 5.5 | 3.0e3 | 59.0 ± 0.1 | 7.2e4 |
|  | GPU | MNN | F16 | 727 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3-0.6B | CPU | llama.cpp | Q4_0 | 469 | 833 | 309.4 ± 0.1 | 6.3e3 | 72.0 ± 0.1 | 4.7e4 |
|  | CPU | MNN | Q4 | 378 | 589 | 398.1 ± 93.3 | 5.7e3 | 79.0 ± 0.6 | 3.7e4 |
|  | GPU | llama.cpp | Q4_0 | 469 | 1,552 (1,237 GPU) | 1679.6 ± 10.0 | 2.3e3 | 61.7 ± 0.0 | 7.0e4 |
|  | GPU | MNN | Q4 | 378 | 707 (444 GPU) | 1252.2 ± 10.2 | 3.1e3 | 32.0 ± 0.1 | 6.3e4 |
|  | CPU | llama.cpp | Q8_0 | 805 | 1,291 | 315.4 ± 0.2 | 7.0e3 | 67.8 | 5.6e4 |
|  | CPU | MNN | Q8 | 676 | 1,029 (1 GPU) | 423.6 ± 97.3 | 6.2e3 | 66.4 ± 0.1 | 5.2e4 |
|  | GPU | llama.cpp | Q8_0 | 805 | 2,017 (1,496 GPU) | 471.6 ± 2.1 | 4.7e3 | 57.6 ± 0.4 | 7.3e4 |
|  | GPU | MNN | Q8 | 676 | 1,002 (741 GPU) | 1177.5 ± 5.1 | 3.5e3 | 25.6 ± 0.0 | 9.0e4 |
|  | CPU | llama.cpp | F16 | 1,509 | 1,626 | 67.2 ± 0.0 | 3.9e4 | 38.4 ± 0.0 | 1.2e5 |
|  | CPU | MNN | F16 | 1,198 | 2,081 | 184.0 ± 0.2 | 1.0e4 | 37.3 ± 0.1 | 9.5e4 |
|  | GPU | llama.cpp | F16 | 1,509 | 4,464 (2,055 GPU) | 627.4 ± 0.1 | 4.5e3 | 41.2 ± 0.1 | 1.1e5 |
|  | GPU | MNN | F16 | 1,198 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-0.8B | CPU | llama.cpp | Q4_0 | 513 | 1,115 | 306.9 ± 0.2 | 6.8e3 | 60.1 ± 0.0 | 6.0e4 |
|  | CPU | MNN | Q4 | 440 | 568 | 336.7 ± 0.8 | 4.9e3 | 80.0 ± 0.8 | 3.6e4 |
|  | GPU | llama.cpp | Q4_0 | 513 | 1,883 (1,507 GPU) | 1120.3 ± 11.7 | 3.5e3 | 61.4 ± 0.3 | 6.0e4 |
|  | GPU | MNN | Q4 | 440 | 733 (511 GPU) | 1291.8 ± 7.1 | 3.0e3 | 75.9 ± 5.7 | 4.1e4 |
|  | CPU | llama.cpp | Q8_0 | 834 | 1,735 | 316.0 ± 0.6 | 7.1e3 | 59.6 ± 0.1 | 6.8e4 |
|  | CPU | MNN | Q8 | 816 | 940 | 356.2 ± 0.3 | 5.7e3 | 69.2 ± 0.1 | 5.1e4 |
|  | GPU | llama.cpp | Q8_0 | 834 | 2,273 (1,818 GPU) | 1041.2 ± 1.8 | 3.0e3 | 53.0 ± 1.1 | 8.9e4 |
|  | GPU | MNN | Q8 | 816 | 1,108 (887 GPU) | 982.2 ± 54.0 | 4.5e3 | 52.6 ± 3.9 | 7.4e4 |
|  | CPU | llama.cpp | F16 | 1,558 | 1,641 | 61.0 ± 0.1 | 4.1e4 | 32.2 ± 0.0 | 1.5e5 |
|  | CPU | MNN | F16 | 1,521 | 3,068 | 249.8 ± 2.7 | 9.9e3 | 34.5 ± 0.0 | 1.0e5 |
|  | GPU | llama.cpp | F16 | 1,558 | 4,685 (2,519 GPU) | 847.3 ± 7.9 | 3.6e3 | 35.0 ± 0.1 | 1.3e5 |
|  | GPU | MNN | F16 | 1,521 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3-1.7B | CPU | llama.cpp | Q4_0 | 1,229 | 2,140 | 144.1 ± 0.2 | 1.3e4 | 34.9 ± 0.0 | 1.2e5 |
|  | CPU | MNN | Q4 | 1,081 | 1,286 | 102.8 ± 0.2 | 1.9e4 | 36.8 ± 0.1 | 8.6e4 |
|  | GPU | llama.cpp | Q4_0 | 1,229 | 3,113 (1,913 GPU) | 729.0 ± 5.5 | 8.2e3 | 31.4 ± 0.0 | 1.5e5 |
|  | GPU | MNN | Q4 | 1,081 | 1,443 (1,089 GPU) | 486.2 ± 1.7 | 9.6e3 | 25.6 ± 0.0 | 1.3e5 |
|  | CPU | llama.cpp | Q8_0 | 2,165 | 3,520 | 150.2 ± 0.5 | 1.7e4 | 30.8 ± 0.0 | 1.4e5 |
|  | CPU | MNN | Q8 | 1,942 | 2,716 | 135.7 ± 42.0 | 2.2e4 | 29.6 ± 0.0 | 1.5e5 |
|  | GPU | llama.cpp | Q8_0 | 2,165 | 5,169 (2,694 GPU) | 164.2 ± 0.3 | 1.7e4 | 28.1 ± 0.0 | 1.9e5 |
|  | GPU | MNN | Q8 | 1,942 | 2,479 (1,949 GPU) | 442.7 ± 1.6 | 1.1e4 | 22.8 ± 0.0 | 1.9e5 |
|  | CPU | llama.cpp | F16 | 4,070 | 4,115 | 15.9 ± 0.0 | 1.3e5 | 15.9 ± 0.0 | 3.3e5 |
|  | CPU | MNN | F16 | 3,447 | 5,220 | 79.4 ± 30.4 | 3.7e4 | 13.6 ± 0.1 | 2.5e5 |
|  | GPU | llama.cpp | F16 | 4,070 | 11,362 (4,307 GPU) | 225.0 ± 0.6 | 1.5e4 | 17.1 ± 0.0 | 2.8e5 |
|  | GPU | MNN | F16 | 3,447 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-2B | CPU | llama.cpp | Q4_0 | 1,237 | 2,523 | 150.5 ± 0.6 | 1.4e4 | 32.7 ± 0.0 | 1.4e5 |
|  | CPU | MNN | Q4 | 1,076 | 1,512 | 157.8 ± 0.3 | 1.5e4 | 37.2 ± 0.1 | 9.6e4 |
|  | GPU | llama.cpp | Q4_0 | 1,237 | 2,980 (2,220 GPU) | 641.4 ± 3.1 | 8.8e3 | 38.1 ± 0.1 | 1.3e5 |
|  | GPU | MNN | Q4 | 1,076 | 1,470 (1,156 GPU) | 668.9 ± 39.3 | 5.3e3 | 43.2 ± 2.3 | 1.0e5 |
|  | CPU | llama.cpp | Q8_0 | 2,077 | 4,142 | 159.1 ± 0.3 | 1.5e4 | 29.3 ± 0.1 | 1.5e5 |
|  | CPU | MNN | Q8 | 2,016 | 2,206 | 168.7 ± 1.0 | 1.8e4 | 31.4 ± 0.0 | 1.4e5 |
|  | GPU | llama.cpp | Q8_0 | 2,077 | 4,157 (3,037 GPU) | 550.5 ± 2.1 | 8.5e3 | 27.6 ± 0.0 | 1.9e5 |
|  | GPU | MNN | Q8 | 2,016 | 2,810 (2,096 GPU) | 457.6 ± 18.9 | 8.7e3 | 26.0 ± 0.2 | 1.8e5 |
|  | CPU | llama.cpp | F16 | 3,897 | 3,907 | 23.5 ± 0.0 | 1.1e5 | 14.7 ± 0.0 | 3.5e5 |
|  | CPU | MNN | F16 | 3,780 | 6,475 | 113.2 ± 0.5 | 2.2e4 | 14.1 ± 0.0 | 2.8e5 |
|  | GPU | llama.cpp | F16 | 3,897 | 10,039 (5,108 GPU) | 436.9 ± 0.8 | 7.6e3 | 16.4 ± 0.0 | 3.0e5 |
|  | GPU | MNN | F16 | 3,780 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3-4B | CPU | llama.cpp | Q4_0 | 2,370 | 4,561 (1 GPU) | 56.4 ± 0.1 | 4.1e4 | 16.3 ± 0.3 | 2.7e5 |
|  | CPU | MNN | Q4 | 2,521 | 2,772 | 41.0 ± 0.1 | 6.0e4 | 16.8 ± 0.1 | 2.0e5 |
|  | GPU | llama.cpp | Q4_0 | 2,370 | 5,647 (3,247 GPU) | 315.0 ± 0.1 | 1.9e4 | 16.7 ± 0.0 | 3.1e5 |
|  | GPU | MNN | Q4 | 2,521 | 2,964 (2,429 GPU) | 191.1 ± 0.3 | 3.2e4 | 19.7 ± 0.0 | 2.8e5 |
|  | CPU | llama.cpp | Q8_0 | 4,280 | 8,715 | 58.8 ± 0.0 | 4.8e4 | 13.8 ± 0.0 | 3.3e5 |
|  | CPU | MNN | Q8 | 4,532 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | Q8_0 | 4,280 | 9,544 (5,178 GPU) | 63.7 ± 0.1 | 4.5e4 | 12.7 ± 0.0 | 4.3e5 |
|  | GPU | MNN | Q8 | 4,532 | 4,975 (4,440 GPU) | 180.0 ± 0.3 | 3.4e4 | 12.6 ± 0.0 | 4.5e5 |
|  | CPU | llama.cpp | F16 | 8,051 | 8,212 | 8.9 ± 0.0 | 3.0e5 | 6.9 ± 0.2 | 7.9e5 |
|  | CPU | MNN | F16 | 8,052 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | F16 | 8,051 | 18,196 (8,534 GPU) | 88.2 ± 0.0 | 4.3e4 | 7.4 ± 0.0 | 6.7e5 |
|  | GPU | MNN | F16 | 8,052 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-4B | CPU | llama.cpp | Q4_0 | 2,612 | 5,263 | 59.8 ± 0.1 | 3.9e4 | 15.9 | 3.0e5 |
|  | CPU | MNN | Q4 | 2,389 | 2,609 | 62.5 ± 0.1 | 4.3e4 | 16.9 ± 0.1 | 1.8e5 |
|  | GPU | llama.cpp | Q4_0 | 2,612 | 4,633 (3,614 GPU) | 280.6 ± 0.2 | 2.1e4 | 14.5 ± 1.0 | 2.8e5 |
|  | GPU | MNN | Q4 | 2,389 | 3,040 (2,524 GPU) | 274.9 ± 5.1 | 1.5e4 | 21.7 ± 0.2 | 2.3e5 |
|  | CPU | llama.cpp | Q8_0 | 4,611 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q8 | 4,491 | 5,355 | 67.6 ± 0.1 | 6.9e4 | 14.2 ± 0.0 | 3.3e5 |
|  | GPU | llama.cpp | Q8_0 | 4,611 | 6,406 (5,565 GPU) | 240.1 ± 0.3 | 2.4e4 | 12.6 ± 0.0 | 4.1e5 |
|  | GPU | MNN | Q8 | 4,491 | 5,586 (4,626 GPU) | 203.8 ± 1.7 | 2.3e4 | 13.4 ± 0.0 | 4.2e5 |

**Rerun after a slow-governor first run** (prefill far below the phone's other measurements, decode normal; the rerun replaces it): `qwen3-0.6b__Q8_0__llama.cpp__cpu` (rerun; first run 60.0 / 66.96 tokens/s (slow governor)), `qwen3.5-4b__Q4_0__llama.cpp__cpu__head` (rerun; first run 25.3 / 15.05 tokens/s (slow governor)).

### 3.3 OnePlus 8 Pro

Snapdragon 865 (SM8250), Adreno 650, 12 GB (11.2 GiB usable), Android 13. 104 configurations: 79 measured (74 with energy, 79 with peak memory), 17 cannot run, 8 not run.

| Model | Backend | Framework | Quant. | Storage MB | Peak memory MB | Prefill t/s | Prefill µJ/token | Decode t/s | Decode µJ/token |
|---|---|---|---|--:|--:|--:|--:|--:|--:|
| SmolLM2-135M | CPU | llama.cpp | Q4_0 | 92 | 220 | 919.2 ± 0.4 | 6.2e3 | 150.3 ± 1.8 | 4.4e4 |
|  | CPU | MNN | Q4 | 87 | 152 | 835.6 ± 1.0 | 5.7e3 | 137.6 ± 1.4 | 4.2e4 |
|  | GPU | llama.cpp | Q4_0 | 92 | 681 (0 GPU) | 104.8 ± 0.2 | 1.6e4 | 13.3 ± 0.0 | 1.2e5 |
|  | GPU | MNN | Q4 | 87 | 186 (0 GPU) | 743.4 ± 2.6 | 3.0e3 | 9.9 ± 0.1 | 9.2e4 |
|  | CPU | llama.cpp | Q8_0 | 145 | 326 | 917.4 ± 0.6 | 6.7e3 | 115.8 ± 0.6 | 6.0e4 |
|  | CPU | MNN | Q8 | 154 | 217 | 867.1 ± 2.7 | 6.0e3 | 95.0 ± 0.8 | 6.3e4 |
|  | GPU | llama.cpp | Q8_0 | 145 | 734 (0 GPU) | 11.4 ± 0.0 | 1.6e5 | 9.3 ± 0.0 | 1.4e5 |
|  | GPU | MNN | Q8 | 154 | 253 (0 GPU) | 743.1 ± 4.7 | 3.1e3 | 9.8 ± 0.1 | 1.0e5 |
|  | CPU | llama.cpp | F16 | 271 | 310 | 447.7 ± 2.2 | 1.8e4 | 75.5 ± 0.4 | 9.4e4 |
|  | CPU | MNN | F16 | 272 | 458 | 721.8 ± 0.9 | 7.8e3 | 66.0 ± 0.2 | 9.9e4 |
|  | GPU | llama.cpp | F16 | 271 | 886 (0 GPU) | 5.4 ± 0.0 | 3.3e5 | 9.8 ± 0.0 | 1.7e5 |
|  | GPU | MNN | F16 | 272 | crashed | FAIL | FAIL | FAIL | FAIL |
| Gemma-3-270M | CPU | llama.cpp | Q4_0 | 241 | 534 | 1014.7 ± 0.6 | 5.8e3 | 84.3 ± 0.5 | 8.1e4 |
|  | CPU | MNN | Q4 | 176 | 261 | 897.9 ± 1.1 | 5.7e3 | 65.3 ± 0.6 | 9.1e4 |
|  | GPU | llama.cpp | Q4_0 | 241 | 1,429 (0 GPU) | 77.8 ± 0.1 | 2.0e4 | 7.1 ± 0.1 | 1.8e5 |
|  | GPU | MNN | Q4 | 176 | 295 (0 GPU) | 745.7 ± 6.0 | 3.1e3 | 8.3 ± 0.0 | 1.2e5 |
|  | CPU | llama.cpp | Q8_0 | 292 | 634 | 1013.0 ± 1.3 | 6.5e3 | 70.4 ± 0.7 | 1.0e5 |
|  | CPU | MNN | Q8 | 310 | 395 | 926.1 ± 3.2 | 6.1e3 | 46.2 ± 0.4 | 1.3e5 |
|  | GPU | llama.cpp | Q8_0 | 292 | 1,478 (0 GPU) | 11.0 ± 0.0 | 1.6e5 | 6.1 ± 0.0 | 2.2e5 |
|  | GPU | MNN | Q8 | 310 | 430 (0 GPU) | 736.2 ± 3.8 | 3.1e3 | 8.3 ± 0.2 | 1.4e5 |
|  | CPU | llama.cpp | F16 | 543 | 602 | 483.6 ± 0.4 | 1.8e4 | 42.8 ± 0.2 | 1.6e5 |
|  | CPU | MNN | F16 | 544 | 1,435 | 755.2 ± 15.7 | 8.1e3 | 31.1 ± 0.6 | 2.0e5 |
|  | GPU | llama.cpp | F16 | 543 | 1,886 (0 GPU) | 6.2 ± 0.0 | 2.9e5 | 6.3 ± 0.1 | 2.7e5 |
|  | GPU | MNN | F16 | 544 | crashed | FAIL | FAIL | FAIL | FAIL |
| SmolLM2-360M | CPU | llama.cpp | Q4_0 | 229 | 509 | 378.9 ± 0.3 | 1.5e4 | 71.7 ± 0.5 | 9.5e4 |
|  | CPU | MNN | Q4 | 229 | 312 | 326.2 ± 0.6 | 1.5e4 | 67.6 ± 0.3 | 9.4e4 |
|  | GPU | llama.cpp | Q4_0 | 229 | 901 (0 GPU) | 58.0 ± 0.2 | 3.0e4 | 9.0 ± 0.0 | 1.7e5 |
|  | GPU | MNN | Q4 | 229 | 333 (0 GPU) | 298.6 ± 0.3 | 8.4e3 | 9.3 ± 0.1 | 1.3e5 |
|  | CPU | llama.cpp | Q8_0 | 386 | 825 | 377.0 ± 0.1 | 1.8e4 | 51.2 ± 0.1 | 1.3e5 |
|  | CPU | MNN | Q8 | 410 | 492 | 342.1 ± 2.3 | 1.6e4 | 44.6 ± 0.3 | 1.4e5 |
|  | GPU | llama.cpp | Q8_0 | 386 | 1,216 (0 GPU) | 3.8 ± 0.0 | 4.9e5 | 6.2 ± 0.0 | 2.4e5 |
|  | GPU | MNN | Q8 | 410 | 514 (0 GPU) | 298.4 ± 0.4 | 8.8e3 | 9.2 ± 0.1 | 1.6e5 |
|  | CPU | llama.cpp | F16 | 726 | 779 | 161.4 ± 0.1 | 5.1e4 | 30.3 ± 0.1 | 2.3e5 |
|  | CPU | MNN | F16 | 727 | 803 | 280.3 ± 1.0 | 2.2e4 | 25.9 ± 0.2 | 2.4e5 |
|  | GPU | llama.cpp | F16 | 726 | 1,389 (0 GPU) | 2.0 ± 0.0 | – | 6.3 ± 0.0 | – |
|  | GPU | MNN | F16 | 727 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3-0.6B | CPU | llama.cpp | Q4_0 | 469 | 833 | 260.8 ± 0.5 | 2.2e4 | 48.2 ± 1.0 | 1.6e5 |
|  | CPU | MNN | Q4 | 378 | 537 | 227.4 ± 1.9 | 2.2e4 | 42.8 ± 0.5 | 1.4e5 |
|  | GPU | llama.cpp | Q4_0 | 469 | 1,315 (0 GPU) | 35.1 ± 0.0 | 5.1e4 | 3.5 ± 0.0 | 3.5e5 |
|  | GPU | MNN | Q4 | 378 | 546 (0 GPU) | 213.9 ± 0.3 | 1.2e4 | 9.2 ± 0.0 | 1.6e5 |
|  | CPU | llama.cpp | Q8_0 | 805 | 1,440 | 258.8 ± 0.5 | 2.5e4 | 30.3 ± 1.1 | 2.1e5 |
|  | CPU | MNN | Q8 | 676 | 962 | 238.7 ± 0.7 | 2.4e4 | 27.8 ± 0.4 | 2.3e5 |
|  | GPU | llama.cpp | Q8_0 | 805 | 1,908 (0 GPU) | 2.8 ± 0.0 | – | 4.3 ± 0.0 | – |
|  | GPU | MNN | Q8 | 676 | 844 (1 GPU) | 213.1 ± 0.2 | 1.3e4 | 8.9 ± 0.0 | 2.2e5 |
|  | CPU | llama.cpp | F16 | 1,509 | 1,626 | 99.5 ± 5.2 | 7.3e4 | 18.3 ± 0.1 | 3.8e5 |
|  | CPU | MNN | F16 | 1,198 | 1,811 | 168.3 ± 39.3 | 3.3e4 | 16.5 ± 0.4 | 3.8e5 |
|  | GPU | llama.cpp | F16 | 1,509 | 2,344 (0 GPU) | 1.4 ± 0.0 | – | 4.5 ± 0.0 | – |
|  | GPU | MNN | F16 | 1,198 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-0.8B | CPU | llama.cpp | Q4_0 | 513 | 1,114 | 206.6 ± 0.4 | 2.9e4 | 34.8 ± 0.4 | 2.1e5 |
|  | CPU | MNN | Q4 | 440 | 550 | 166.6 ± 16.1 | 2.8e4 | 45.9 ± 0.3 | 1.4e5 |
|  | GPU | llama.cpp | Q4_0 | 513 | not run (no OpenCL build) | not run | not run | not run | not run |
|  | GPU | MNN | Q4 | 440 | 583 (0 GPU) | 207.2 ± 0.5 | 9.8e3 | 24.1 ± 0.0 | 1.2e5 |
|  | CPU | llama.cpp | Q8_0 | 834 | 1,735 | 204.3 ± 0.6 | 3.4e4 | 23.1 ± 0.3 | 2.9e5 |
|  | CPU | MNN | Q8 | 816 | 926 | 182.2 ± 2.1 | 3.0e4 | 26.0 ± 0.6 | 2.4e5 |
|  | GPU | llama.cpp | Q8_0 | 834 | not run (no OpenCL build) | not run | not run | not run | not run |
|  | GPU | MNN | Q8 | 816 | 959 (0 GPU) | 181.7 ± 0.4 | 1.4e4 | 17.9 ± 0.0 | 2.0e5 |
|  | CPU | llama.cpp | F16 | 1,558 | 1,642 | 80.2 ± 5.9 | 8.5e4 | 14.2 ± 0.1 | 4.9e5 |
|  | CPU | MNN | F16 | 1,521 | 2,906 | 152.7 ± 0.3 | 3.8e4 | 14.4 ± 0.1 | 4.4e5 |
|  | GPU | llama.cpp | F16 | 1,558 | not run (no OpenCL build) | not run | not run | not run | not run |
|  | GPU | MNN | F16 | 1,521 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3-1.7B | CPU | llama.cpp | Q4_0 | 1,229 | 2,140 | 99.3 ± 12.0 | 5.5e4 | 21.1 ± 0.7 | 3.6e5 |
|  | CPU | MNN | Q4 | 1,081 | 1,270 | 81.8 ± 0.0 | 6.9e4 | 19.1 ± 0.5 | 3.5e5 |
|  | GPU | llama.cpp | Q4_0 | 1,229 | 2,753 (0 GPU) | 28.9 ± 0.1 | 7.1e4 | 2.6 ± 0.0 | 5.8e5 |
|  | GPU | MNN | Q4 | 1,081 | 1,211 (0 GPU) | 77.3 ± 0.0 | 3.7e4 | 9.1 ± 0.0 | 3.2e5 |
|  | CPU | llama.cpp | Q8_0 | 2,165 | 3,678 | 96.8 ± 1.7 | 7.5e4 | 12.0 ± 0.2 | 5.6e5 |
|  | CPU | MNN | Q8 | 1,942 | 2,416 | 83.2 ± 5.5 | 7.5e4 | 10.9 ± 0.0 | 5.9e5 |
|  | GPU | llama.cpp | Q8_0 | 2,165 | 4,474 (0 GPU) | 0.9 ± 0.0 | – | 3.2 ± 0.0 | – |
|  | GPU | MNN | Q8 | 1,942 | 2,072 (0 GPU) | 73.8 ± 2.8 | 4.5e4 | 8.4 ± 0.1 | 5.0e5 |
|  | CPU | llama.cpp | F16 | 4,070 | 4,196 | 27.4 ± 0.5 | 2.3e5 | 6.9 ± 0.1 | 9.9e5 |
|  | CPU | MNN | F16 | 3,447 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | F16 | 4,070 | 4,908 (0 GPU) | 0.5 ± 0.0 | – | 3.1 ± 0.0 | – |
|  | GPU | MNN | F16 | 3,447 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-2B | CPU | llama.cpp | Q4_0 | 1,237 | 2,523 | 98.8 ± 2.2 | 5.8e4 | 17.4 ± 0.5 | 4.1e5 |
|  | CPU | MNN | Q4 | 1,076 | 1,218 | 77.9 ± 0.2 | 6.1e4 | 20.7 ± 0.3 | 3.2e5 |
|  | GPU | llama.cpp | Q4_0 | 1,237 | not run (no OpenCL build) | not run | not run | not run | not run |
|  | GPU | MNN | Q4 | 1,076 | 1,259 (0 GPU) | 101.9 ± 0.1 | 2.2e4 | 14.2 ± 0.0 | 2.7e5 |
|  | CPU | llama.cpp | Q8_0 | 2,077 | 4,141 | 94.0 ± 1.0 | 7.7e4 | 10.6 ± 0.0 | 6.3e5 |
|  | CPU | MNN | Q8 | 2,016 | 2,974 | 81.6 ± 0.2 | 6.6e4 | 11.2 ± 0.2 | 5.7e5 |
|  | GPU | llama.cpp | Q8_0 | 2,077 | not run (no OpenCL build) | not run | not run | not run | not run |
|  | GPU | MNN | Q8 | 2,016 | 2,221 (31 GPU) | 83.7 ± 0.3 | 3.2e4 | 9.3 ± 0.0 | 4.6e5 |
|  | CPU | llama.cpp | F16 | 3,897 | 3,908 | 28.2 ± 0.3 | 2.3e5 | 6.1 ± 0.1 | 1.1e6 |
|  | CPU | MNN | F16 | 3,780 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | F16 | 3,897 | not run (no OpenCL build) | not run | not run | not run | not run |
|  | GPU | MNN | F16 | 3,780 | crashed | FAIL | FAIL | FAIL | FAIL |
| Qwen3-4B | CPU | llama.cpp | Q4_0 | 2,370 | 4,560 | 41.1 ± 0.6 | 1.4e5 | 8.9 ± 0.1 | 7.5e5 |
|  | CPU | MNN | Q4 | 2,521 | 2,755 | 32.1 ± 1.0 | 1.8e5 | 8.7 ± 0.0 | 7.9e5 |
|  | GPU | llama.cpp | Q4_0 | 2,370 | 5,216 (0 GPU) | 10.9 ± 0.0 | 2.0e5 | 1.7 ± 0.0 | 1.2e6 |
|  | GPU | MNN | Q4 | 2,521 | 2,580 (289 GPU) | 31.1 ± 0.0 | 1.0e5 | 6.3 ± 0.0 | 6.9e5 |
|  | CPU | llama.cpp | Q8_0 | 4,280 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q8 | 4,532 | 5,065 | 31.0 ± 1.1 | 1.9e5 | 4.8 ± 0.0 | 1.3e6 |
|  | GPU | llama.cpp | Q8_0 | 4,280 | load failed | FAIL | FAIL | FAIL | FAIL |
|  | GPU | MNN | Q8 | 4,532 | 4,179 (0 GPU) | 26.4 ± 0.1 | 1.0e5 | 0.7 ± 0.0 | 2.1e6 |
|  | CPU | llama.cpp | F16 | 8,051 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | F16 | 8,052 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | F16 | 8,051 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | MNN | F16 | 8,052 | does not fit | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-4B | CPU | llama.cpp | Q4_0 | 2,612 | 5,264 | 39.8 ± 0.1 | 1.5e5 | 8.0 ± 0.2 | 8.5e5 |
|  | CPU | MNN | Q4 | 2,389 | 2,875 | 29.6 ± 1.2 | 1.5e5 | 9.2 ± 0.0 | 7.2e5 |
|  | GPU | llama.cpp | Q4_0 | 2,612 | not run (no OpenCL build) | not run | not run | not run | not run |
|  | GPU | MNN | Q4 | 2,389 | 2,655 (0 GPU) | 39.5 ± 0.8 | 6.1e4 | 7.1 ± 0.0 | 6.3e5 |
|  | CPU | llama.cpp | Q8_0 | 4,611 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q8 | 4,491 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | GPU | llama.cpp | Q8_0 | 4,611 | not run (no OpenCL build) | not run | not run | not run | not run |
|  | GPU | MNN | Q8 | 4,491 | 5,968 (0 GPU) | 32.7 ± 0.6 | 8.4e4 | 4.0 ± 0.0 | 1.1e6 |

`–` energy (5 rows): `smollm2-360m__F16__llama.cpp__gpu` (no energy reading), `qwen3-0.6b__Q8_0__llama.cpp__gpu` (no energy reading), `qwen3-0.6b__F16__llama.cpp__gpu` (no energy reading), `qwen3-1.7b__Q8_0__llama.cpp__gpu` (no energy reading), `qwen3-1.7b__F16__llama.cpp__gpu` (no energy reading).

**Energy not measurable (5 llama.cpp GPU rows):** `qwen3-0.6b__F16__llama.cpp__gpu`, `qwen3-0.6b__Q8_0__llama.cpp__gpu`, `qwen3-1.7b__F16__llama.cpp__gpu`, `qwen3-1.7b__Q8_0__llama.cpp__gpu`, `smollm2-360m__F16__llama.cpp__gpu`. Their throughput above comes from the first (screen-on) runs. The energy runs abort inside the Adreno 650 OpenCL driver (`cl_a6x_cmdbuf_mgr_submit_ibs`, exit 134) minutes into the first prefill. These rows prefill at 0.5–3 tokens/s, so one GPU call runs for minutes; a driver hang watchdog is the likely cause (not confirmed). The same kind of row kernel-panicked the phone three times with the display panel off.

**Throughput differs by more than 10% from the first (screen-on, energy-less) OnePlus 8 Pro runs** in 34 rows (rerun ÷ first run, prefill / decode): `smollm2-135m__Q8_0__llama.cpp__cpu` (1.89× / 1.47×, rerun 10-20 03:31), `smollm2-135m__Q8__mnn__cpu` (1.90× / 1.84×, rerun 10-20 04:31), `smollm2-135m__F16__llama.cpp__cpu` (1.63× / 1.80×, rerun 10-20 06:33), `smollm2-135m__F16__mnn__cpu` (2.53× / 1.88×, rerun 10-20 08:16), `gemma3-270m__Q8_0__llama.cpp__cpu` (1.95× / 1.52×, rerun 10-20 08:21), `gemma3-270m__Q8__mnn__cpu` (2.35× / 1.69×, rerun 10-20 09:04), `gemma3-270m__F16__llama.cpp__cpu` (1.62× / 1.26×, rerun 10-23 14:02), `gemma3-270m__F16__mnn__cpu` (2.32× / 1.68×, rerun 10-21 06:13), `gemma3-270m__F16__llama.cpp__gpu` (1.11× / 0.94×, rerun 10-21 05:32), `smollm2-360m__Q8_0__llama.cpp__cpu` (1.76× / 1.58×, rerun 10-21 03:30), `smollm2-360m__Q8__mnn__cpu` (2.24× / 1.80×, rerun 10-21 04:44), `smollm2-360m__F16__llama.cpp__cpu` (1.52× / 1.68×, rerun 10-22 14:37), `smollm2-360m__F16__mnn__cpu` (2.39× / 1.78×, rerun 10-22 03:18), `qwen3-0.6b__Q8_0__llama.cpp__cpu` (1.75× / 1.55×, rerun 10-22 03:52), `qwen3-0.6b__Q8__mnn__cpu` (2.32× / 1.79×, rerun 10-21 06:26), `qwen3-0.6b__F16__llama.cpp__cpu` (1.32× / 1.24×, rerun 10-22 16:28), `qwen3-0.6b__F16__mnn__cpu` (2.54× / 1.87×, rerun 10-22 04:21), `qwen3.5-0.8b__Q4_0__llama.cpp__cpu__head` (2.07× / 1.38×, rerun 10-22 18:20), `qwen3.5-0.8b__Q4__mnn__cpu__head` (1.57× / 2.27×, rerun 10-22 17:02), `qwen3.5-0.8b__Q8_0__llama.cpp__cpu__head` (1.80× / 1.49×, rerun 10-22 18:23), `qwen3.5-0.8b__Q8__mnn__cpu__head` (1.72× / 1.71×, rerun 10-22 17:09), `qwen3.5-0.8b__F16__llama.cpp__cpu__head` (1.32× / 1.11×, rerun 10-26 06:14), `qwen3.5-0.8b__F16__mnn__cpu__head` (1.91× / 1.60×, rerun 10-22 17:26), `qwen3-1.7b__Q8_0__llama.cpp__cpu` (1.91× / 1.31×, rerun 10-22 05:40), `qwen3-1.7b__Q8__mnn__cpu` (2.02× / 2.07×, rerun 10-22 05:16), `qwen3-1.7b__F16__llama.cpp__cpu` (1.45× / 1.10×, rerun 10-22 16:39), `qwen3.5-2b__Q4_0__llama.cpp__cpu__head` (1.57× / 1.44×, rerun 10-26 05:30), `qwen3.5-2b__Q4__mnn__cpu__head` (1.43× / 2.28×, rerun 10-22 17:18), `qwen3.5-2b__Q8_0__llama.cpp__cpu__head` (1.66× / 1.31×, rerun 10-26 05:47), `qwen3.5-2b__Q8__mnn__cpu__head` (1.45× / 1.79×, rerun 10-22 17:34), `qwen3.5-2b__F16__llama.cpp__cpu__head` (1.15× / 1.07×, rerun 10-26 05:12), `qwen3-4b-instruct-2507__Q8__mnn__cpu` (2.46× / 2.27×, rerun 10-26 04:11), `qwen3.5-4b__Q4_0__llama.cpp__cpu__head` (1.66× / 1.50×, rerun 10-26 04:33), `qwen3.5-4b__Q4__mnn__cpu__head` (1.34× / 2.49×, rerun 10-22 17:47). Q4 rows reproduce within 5%; most differences are 8-bit and 16-bit CPU rows, 1.5–2.5× faster in the rerun. Cause (tested 2026-10-20): the moment the OnePlus 8 Pro falls asleep it caps policy4 at 1.48 of 2.42 GHz, and the same benchmark (SmolLM2-135M Q8_0 CPU prefill) runs at 124–228 tokens/s asleep against 907–921 awake. OxygenOS sleeps after 30 min whatever the screen timeout says, and the harness checked the caps only at the start and end of each run, so runs that fell asleep midway were slowed without a flag. The faster rerun values are the uncapped ones (they also match the S23, where Q8_0 CPU is about as fast as Q4_0); the first runs' 8/16-bit CPU values were partly capped. The harness now keeps the phone awake (checked every 2 s) and records the lowest cap during every run; rows measured before that fix with only a once-a-minute wake (2026-10-20 12:39–19:00) are being rerun.

Prefill slower than decode (8 llama.cpp GPU rows): `smollm2-135m__F16__llama.cpp__gpu`, `gemma3-270m__F16__llama.cpp__gpu`, `smollm2-360m__Q8_0__llama.cpp__gpu`, `smollm2-360m__F16__llama.cpp__gpu`, `qwen3-0.6b__Q8_0__llama.cpp__gpu`, `qwen3-0.6b__F16__llama.cpp__gpu`, `qwen3-1.7b__Q8_0__llama.cpp__gpu`, `qwen3-1.7b__F16__llama.cpp__gpu`. A slow OpenCL prefill path of that llama.cpp build for these weight types on this GPU (section 5).

### 3.4 Redmi 9A

Helio G25 (MT6762), 8x Cortex-A53, no OpenCL, 4 GB (3.7 GiB usable), 32-bit Android 11. 52 configurations: 34 measured (34 with energy, 34 with peak memory), 18 cannot run, 0 not run.

| Model | Backend | Framework | Quant. | Storage MB | Peak memory MB | Prefill t/s | Prefill µJ/token | Decode t/s | Decode µJ/token |
|---|---|---|---|--:|--:|--:|--:|--:|--:|
| SmolLM2-135M | CPU | llama.cpp | Q4_0 | 92 | 124 | 11.6 ± 0.0 | 7.4e4 | 5.8 ± 0.0 | 1.4e5 |
|  | CPU | MNN | Q4 | 87 | 158 | 36.5 ± 0.2 | 2.4e4 | 16.3 ± 0.7 | 6.5e4 |
|  | CPU | llama.cpp | Q8_0 | 145 | 177 | 11.2 ± 0.0 | 7.6e4 | 5.6 ± 0.0 | 1.7e5 |
|  | CPU | MNN | Q8 | 154 | 225 | 32.5 ± 0.2 | 2.9e4 | 13.2 ± 0.0 | 7.9e4 |
|  | CPU | llama.cpp | F16 | 271 | 304 | 1.5 ± 0.0 | 5.0e5 | 1.1 ± 0.0 | 7.3e5 |
|  | CPU | MNN | F16 | 272 | 637 | 43.0 ± 0.2 | 2.2e4 | 8.2 ± 0.0 | 1.5e5 |
| Gemma-3-270M | CPU | llama.cpp | Q4_0 | 241 | 288 | 11.7 ± 0.0 | 7.7e4 | 4.7 ± 0.0 | 2.1e5 |
|  | CPU | MNN | Q4 | 176 | 326 | 39.9 ± 0.1 | 2.5e4 | 7.7 ± 0.2 | 1.4e5 |
|  | CPU | llama.cpp | Q8_0 | 292 | 338 | 11.3 ± 0.0 | 7.7e4 | 4.6 ± 0.0 | 2.4e5 |
|  | CPU | MNN | Q8 | 310 | 745 | 35.2 ± 0.1 | 2.8e4 | 6.7 ± 0.0 | 1.6e5 |
|  | CPU | llama.cpp | F16 | 543 | 590 | 1.6 ± 0.0 | 4.5e5 | 0.6 ± 0.0 | 1.3e6 |
|  | CPU | MNN | F16 | 544 | 1,681 | 44.9 ± 0.4 | 2.4e4 | 4.2 ± 0.0 | 3.0e5 |
| SmolLM2-360M | CPU | llama.cpp | Q4_0 | 229 | 277 | 5.4 ± 0.0 | 1.5e5 | 3.0 ± 0.0 | 3.0e5 |
|  | CPU | MNN | Q4 | 229 | 355 | 14.8 ± 0.0 | 6.1e4 | 8.6 ± 0.0 | 1.2e5 |
|  | CPU | llama.cpp | Q8_0 | 386 | 434 | 5.1 ± 0.0 | 1.8e5 | 2.8 ± 0.0 | 3.5e5 |
|  | CPU | MNN | Q8 | 410 | 509 | 12.7 ± 0.0 | 7.5e4 | 7.5 ± 0.0 | 1.5e5 |
|  | CPU | llama.cpp | F16 | 726 | 773 | 0.5 ± 0.0 | 1.4e6 | 0.4 ± 0.0 | 1.9e6 |
|  | CPU | MNN | F16 | 727 | 1,614 | 15.9 ± 0.0 | 6.7e4 | 4.1 ± 0.0 | 3.5e5 |
| Qwen3-0.6B | CPU | llama.cpp | Q4_0 | 469 | 572 | 3.3 ± 0.0 | 2.5e5 | 1.7 ± 0.0 | 5.2e5 |
|  | CPU | MNN | Q4 | 378 | 673 | 11.4 ± 0.1 | 8.2e4 | 5.8 ± 0.1 | 1.9e5 |
|  | CPU | llama.cpp | Q8_0 | 805 | 907 | 3.2 ± 0.0 | 2.8e5 | 1.7 ± 0.0 | 6.1e5 |
|  | CPU | MNN | Q8 | 676 | 1,023 | 9.6 ± 0.0 | 9.9e4 | 5.3 ± 0.0 | 2.4e5 |
|  | CPU | llama.cpp | F16 | 1,509 | 1,612 | 0.4 ± 0.0 | 2.0e6 | 0.3 ± 0.0 | 3.1e6 |
|  | CPU | MNN | F16 | 1,198 | does not fit | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-0.8B | CPU | llama.cpp | Q4_0 | 513 | 599 | 4.6 ± 0.0 | 2.5e5 | 2.2 ± 0.0 | 5.7e5 |
|  | CPU | MNN | Q4 | 440 | 715 | 9.1 ± 0.0 | 1.2e5 | 5.8 ± 0.0 | 2.4e5 |
|  | CPU | llama.cpp | Q8_0 | 834 | 910 | 4.3 ± 0.0 | 3.2e5 | 2.2 ± 0.0 | 6.6e5 |
|  | CPU | MNN | Q8 | 816 | 1,241 | 7.8 ± 0.0 | 1.4e5 | 4.7 ± 0.0 | 3.1e5 |
|  | CPU | llama.cpp | F16 | 1,558 | 1,615 | 0.3 ± 0.0 | 2.8e6 | 0.2 ± 0.0 | 4.7e6 |
|  | CPU | MNN | F16 | 1,521 | does not fit | FAIL | FAIL | FAIL | FAIL |
| Qwen3-1.7B | CPU | llama.cpp | Q4_0 | 1,229 | 1,340 | 1.8 ± 0.0 | 5.2e5 | 1.1 ± 0.0 | 9.4e5 |
|  | CPU | MNN | Q4 | 1,081 | 1,543 | 4.8 ± 0.0 | 2.2e5 | 3.4 ± 0.2 | 4.1e5 |
|  | CPU | llama.cpp | Q8_0 | 2,165 | load failed | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q8 | 1,942 | 2,357 | 3.8 ± 0.0 | 3.2e5 | 2.5 ± 0.1 | 6.5e5 |
|  | CPU | llama.cpp | F16 | 4,070 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | F16 | 3,447 | does not fit | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-2B | CPU | llama.cpp | Q4_0 | 1,237 | 1,307 | 2.2 ± 0.0 | 5.6e5 | 1.2 ± 0.0 | 1.1e6 |
|  | CPU | MNN | Q4 | 1,076 | 1,663 | 4.6 ± 0.0 | 2.5e5 | 3.2 ± 0.1 | 4.6e5 |
|  | CPU | llama.cpp | Q8_0 | 2,077 | 2,116 | 2.0 ± 0.0 | 6.2e5 | 1.2 ± 0.0 | 1.2e6 |
|  | CPU | MNN | Q8 | 2,016 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | llama.cpp | F16 | 3,897 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | F16 | 3,780 | does not fit | FAIL | FAIL | FAIL | FAIL |
| Qwen3-4B | CPU | llama.cpp | Q4_0 | 2,370 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q4 | 2,521 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | llama.cpp | Q8_0 | 4,280 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q8 | 4,532 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | llama.cpp | F16 | 8,051 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | F16 | 8,052 | does not fit | FAIL | FAIL | FAIL | FAIL |
| Qwen3.5-4B | CPU | llama.cpp | Q4_0 | 2,612 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q4 | 2,389 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | llama.cpp | Q8_0 | 4,611 | does not fit | FAIL | FAIL | FAIL | FAIL |
|  | CPU | MNN | Q8 | 4,491 | does not fit | FAIL | FAIL | FAIL | FAIL |

## 4. Energy report

![Prefill energy, all phones](figures/all_prefill_energy.png)

![Decode energy, all phones](figures/all_decode_energy.png)

*Energy per token, one column per phone, one row per precision (log scale, mJ/token). A gap means that configuration could not run or has no valid energy.*

![Energy vs speed](figures/all_energy_vs_speed.png)

*Energy per token against speed per phone. Energy per token = power ÷ speed, so points on a grey line draw the same net power; lower right is better.*

### Net power while running (W above idle, median and range)

| Backend | Framework | S23 prefill | S23 decode | OP15 prefill | OP15 decode | OP8P prefill | OP8P decode | R9A prefill | R9A decode |
|---|---|--:|--:|--:|--:|--:|--:|--:|--:|
| CPU | llama.cpp | 6.3 (4.6–7.1) | 8.2 (6.2–9.5) | 2.3 (1.3–2.8) | 4.1 (2.9–5.4) | 6.4 (5.5–8.6) | 6.8 (6.4–7.6) | 0.9 (0.7–1.4) | 1.0 (0.8–1.5) |
| CPU | MNN | 6.3 (5.7–7.5) | 8.9 (8.0–10.1) | 2.3 (1.2–4.6) | 3.3 (2.0–4.6) | 5.5 (4.4–6.3) | 6.3 (5.8–6.8) | 1.0 (0.9–1.2) | 1.3 (1.0–1.6) |
| GPU | llama.cpp | 3.5 (3.0–4.4) | 4.7 (2.8–6.9) | 3.3 (2.1–6.0) | 4.4 (2.6–5.4) | 1.8 (1.6–2.2) | 1.5 (1.2–1.9) | – | – |
| GPU | MNN | 3.9 (3.1–6.1) | 2.1 (0.9–8.6) | 4.0 (3.0–6.0) | 3.2 (1.2–5.7) | 2.5 (2.0–3.3) | 2.4 (0.9–4.5) | – | – |

- **Galaxy S23, cheapest 4-bit configuration per model:** SmolLM2-135M: prefill MNN GPU (1.8e3), decode MNN CPU (4.0e4); Gemma-3-270M: prefill MNN GPU (1.9e3), decode llama.cpp CPU (7.0e4); SmolLM2-360M: prefill MNN GPU (5.2e3), decode MNN CPU (7.9e4); Qwen3-0.6B: prefill MNN GPU (8.0e3), decode MNN CPU (1.3e5); Qwen3.5-0.8B: prefill MNN GPU (7.5e3), decode MNN GPU (1.1e5); Qwen3-1.7B: prefill MNN GPU (2.5e4), decode MNN CPU (3.0e5); Qwen3.5-2B: prefill MNN GPU (1.5e4), decode MNN GPU (2.3e5); Qwen3-4B: prefill MNN GPU (6.9e4), decode MNN CPU (6.0e5); Qwen3.5-4B: prefill MNN GPU (4.0e4), decode MNN GPU (5.6e5) (µJ/token).
- **OnePlus 15, cheapest 4-bit configuration per model:** SmolLM2-135M: prefill MNN GPU (9.0e2), decode MNN CPU (9.9e3); Gemma-3-270M: prefill MNN GPU (9.0e2), decode llama.cpp CPU (1.6e4); SmolLM2-360M: prefill llama.cpp GPU (2.5e3), decode MNN CPU (2.3e4); Qwen3-0.6B: prefill llama.cpp GPU (2.3e3), decode MNN CPU (3.7e4); Qwen3.5-0.8B: prefill MNN GPU (3.0e3), decode MNN CPU (3.6e4); Qwen3-1.7B: prefill llama.cpp GPU (8.2e3), decode MNN CPU (8.6e4); Qwen3.5-2B: prefill MNN GPU (5.3e3), decode MNN CPU (9.6e4); Qwen3-4B: prefill llama.cpp GPU (1.9e4), decode MNN CPU (2.0e5); Qwen3.5-4B: prefill MNN GPU (1.5e4), decode MNN CPU (1.8e5) (µJ/token).
- **OnePlus 8 Pro, cheapest 4-bit configuration per model:** SmolLM2-135M: prefill MNN GPU (3.0e3), decode MNN CPU (4.2e4); Gemma-3-270M: prefill MNN GPU (3.1e3), decode llama.cpp CPU (8.1e4); SmolLM2-360M: prefill MNN GPU (8.4e3), decode MNN CPU (9.4e4); Qwen3-0.6B: prefill MNN GPU (1.2e4), decode MNN CPU (1.4e5); Qwen3.5-0.8B: prefill MNN GPU (9.8e3), decode MNN GPU (1.2e5); Qwen3-1.7B: prefill MNN GPU (3.7e4), decode MNN GPU (3.2e5); Qwen3.5-2B: prefill MNN GPU (2.2e4), decode MNN GPU (2.7e5); Qwen3-4B: prefill MNN GPU (1.0e5), decode MNN GPU (6.9e5); Qwen3.5-4B: prefill MNN GPU (6.1e4), decode MNN GPU (6.3e5) (µJ/token).
- **Redmi 9A, cheapest 4-bit configuration per model:** SmolLM2-135M: prefill MNN CPU (2.4e4), decode MNN CPU (6.5e4); Gemma-3-270M: prefill MNN CPU (2.5e4), decode MNN CPU (1.4e5); SmolLM2-360M: prefill MNN CPU (6.1e4), decode MNN CPU (1.2e5); Qwen3-0.6B: prefill MNN CPU (8.2e4), decode MNN CPU (1.9e5); Qwen3.5-0.8B: prefill MNN CPU (1.2e5), decode MNN CPU (2.4e5); Qwen3-1.7B: prefill MNN CPU (2.2e5), decode MNN CPU (4.1e5); Qwen3.5-2B: prefill MNN CPU (2.5e5), decode MNN CPU (4.6e5) (µJ/token).

- **Energy per token is set by speed far more than by power.** Net power stays within roughly 0.5–10 W on every phone while speed spans three orders of magnitude, so the slow configurations are the expensive ones, and the slow phones pay several times more per token for the same model.
- **Decode costs several times more per token than prefill** on every phone: prefill processes the 256 tokens in batched passes, decode makes one pass per token.


## 5. How far to trust these numbers

- **Repeatable.** The recorded repetitions of a configuration vary by 0.1–5% on every phone (MNN CPU prefill on the OnePlus phones up to ~30%: its first repetition runs before the big cores ramp up). On the S23 a standalone `llama-bench` run outside the harness gave the same Llama-3.2-1B prefill (179.9 vs 179.6 tokens/s).
- **Physically consistent.** Decode reads every weight once per token, so each engine reaches a roughly constant effective memory bandwidth across models on each phone (section 3 figure).
- **Energy is reproducible but absolute values are approximate.** On the S23 the same configuration measured hours apart, screen on and screen off, differed by −7% to +2%. Absolute values carry roughly ±10–20% from each phone's battery gauge, and gauges differ between phones (sampled current on the S23 and OnePlus phones, a slowly updated average current on the Redmi 9A), so compare configurations within a phone more confidently than across phones.
- **OnePlus 15 slow-governor cells.** A OnePlus 15 benchmark that started 20 s or more after the last screen change ran with its CPU clocks at the floor; the harness now sends a wake/sleep key pair right before each run. A few cells may still be affected (Qwen3-0.6B llama.cpp Q8_0 CPU, Qwen3.5-4B llama.cpp Q4_0 CPU: prefill far below their neighbours).
- **GPU decode varies between sessions by 10–20%** (S23 tests ruled out the screen, the energy trace and the battery level). Compare GPU decode rows with that margin in mind.

**Known slow paths (real measurements of these builds, not errors):**

- **llama.cpp GPU (OpenCL) on older Adreno GPUs.** On the S23's Adreno 740 llama.cpp GPU prefill is no faster than the CPU; on the OnePlus 8 Pro's Adreno 650 it is slower than the CPU in every cell (0.5–115 tokens/s). The OpenCL backend's kernels target newer Adreno GPUs; on the OnePlus 15 it is the fastest prefill.
- **llama.cpp GPU with Q8_0 or F16 weights** at `eadc418` has prefill slower than decode on the S23 and the OnePlus 8 Pro (flagged per phone in section 3).
- **llama.cpp "Q4_0" files are not all 4-bit.** `llama-quantize` keeps the output matrix at Q6_K by default (28% of Qwen3-0.6B's Q4_0 file), and the pinned OpenCL Q6_K kernel is unoptimized, so on the GPU several small models decode faster at Q8_0 than at Q4_0.
- **MNN GPU decode on the paper's MNN version** costs about 3–4 ms per layer per token whatever the model size (about 9 tokens/s for the small models on the S23 and OnePlus 8 Pro). The newer MNN used for Qwen3.5 does not.
- **MNN GPU with 16-bit weights** crashes (segfault) on every phone with a GPU backend, in both MNN versions.
- **llama.cpp CPU with F16 weights** is slow because the Android build leaves half-precision vector arithmetic off (llama.cpp's own recipe); on the Redmi 9A's 32-bit build it is 7–10× slower than Q4_0.

## 6. Differences from the paper's protocol

1. **Energy is whole-phone, net of idle, not SoC-only.** The paper reads Qualcomm powercap counters, which need root; none of our phones is rooted or exposes Power Stats rails. Energy comes from each phone's battery gauge, sampled every 100 ms: Perfetto's battery current × voltage on the S23 and OnePlus 15; the battery sysfs files (average current) on the Redmi 9A; Android's battery service (BatteryManager current, read by `tools/PbBattery.java`) on the OnePlus 8 Pro, whose battery files are hidden from the shell and whose Perfetto (v25) cannot record the battery voltage. Idle power is measured for 12 s before every run and subtracted; that removes the constant draw (including the display's, where it is on) but not the extra memory and regulator power the workload causes, so values read higher than SoC-only energy.
2. **Wireless adb, phone on battery.** adb runs over Wi-Fi with the other radios off. Screen: the S23 and Redmi 9A run with the screen off and a partial wake lock held by the adb shell (`tools/PbWake.java`) so the phone does not suspend mid-run. The OnePlus 8 Pro caps its CPU (policy4 at 1.48 of 2.42 GHz) whenever the screen is asleep, so it runs awake with only the display panel switched off (`tools/PbPanel.java`, as scrcpy does) or, in its first sessions, with the screen on at minimum brightness. The OnePlus 15 gets a wake/sleep key pair before each run (section 5).
3. **Cool-down gate.** Every run starts with the CPU frequency caps at their maximum and the battery at or below 28 °C as in the paper (the S23 also passes once its battery has stopped cooling). The OnePlus 8 Pro and Redmi 9A energy reruns (2026-10-20) used 32 °C: in that room both phones rested at 29–31 °C, so a 28 °C gate would have timed out before every run. Each run's start temperature is recorded in its result file.
4. **llama.cpp CPU uses 4 threads.** `llama-bench`'s default of one thread per core includes the little cores and ran 3–9× slower on the S23. MNN's default is 4.
5. **Phones.** Only the OnePlus 15 is in the paper. The S23 is compared with the Xiaomi 14 (one chip generation newer); the OnePlus 8 Pro and Redmi 9A have no comparable paper phone. Phones with less RAM cannot load the larger 8-bit and 16-bit models (the harness refuses a run whose weights exceed 55% of RAM, or that swaps more than 512 MB).
6. **Builds per phone.** llama.cpp is built with `-march=armv8.7a` (llama.cpp's Android recipe) for the S23 and OnePlus 15, `armv8.2-a+dotprod+fp16` for the OnePlus 8 Pro (no i8mm), and 32-bit `armv7-a` NEON for the Redmi 9A (armeabi-v7a only). MNN models use `llmexport` defaults (quant block 64, no HQQ) from MNN 3.4.0; llama.cpp 4-bit is Q4_0, because the pinned OpenCL backend has no Q4_K kernel.
7. **Two builds.** Qwen3.5 is newer than the paper's llama.cpp `eadc418` and MNN `510ac8f`, so it runs on llama.cpp `9c2e0e4` and MNN `a8ec497` (text decoder only).

## 7. Configurations that could not run

**Galaxy S23** (22):

- `smollm2-135m__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=42206768908894 end=42230527855031 prefill_us=1289693 decode_us=22389043 prompt=256 gen=256 | Segmentation fault
- `gemma3-270m__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=42280608263345 end=42316305082655 prefill_us=4331505 decode_us=30830973 prompt=256 gen=256 | Segmentation fault
- `smollm2-360m__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=42365311895657 end=42397955279551 prefill_us=1445309 decode_us=31106910 prompt=256 gen=256 | Segmentation fault
- `qwen3-0.6b__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=42449690453333 end=42485749877746 prefill_us=2957561 decode_us=32759035 prompt=256 gen=256 | Segmentation fault
- `qwen3.5-0.8b__F16__mnn__gpu`: crash/error (exit 139): [llm_bench] -kv true is deprecated: use `-pg <pp>,<tg>` instead (`-p A -n B -kv true` == `-pg A,B`). | PB_MARK mnn mode=kv rep=0 begin=49763886126889 end=49786925196567 prefill_us=5344286 decode_us=16934063 prompt=256 gen=256 | Segmenta
- `qwen3-1.7b__F16__mnn__cpu`: does not fit in RAM: 6.42 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3-1.7b__F16__mnn__gpu`: does not fit in RAM: 888 MB of the benchmark process was swapped out (stopped at > 512 MB)
- `qwen3.5-2b__F16__mnn__cpu`: does not fit in RAM: 3877 MB of the benchmark process was swapped out (stopped at > 512 MB)
- `qwen3.5-2b__F16__llama.cpp__gpu`: crash/error (exit 1): llama_model_load: error loading model: unable to allocate OpenCL buffer | llama_model_load_from_file_impl: failed to load model | llama_bench: error: failed to load model '/data/local/tmp/paper_bench/models/qwen3.5-2b-f16-head.gguf'
- `qwen3.5-2b__F16__mnn__gpu`: does not fit in RAM: 7.04 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 75%); not run
- `qwen3-4b-instruct-2507__Q8_0__llama.cpp__cpu`: does not fit in RAM: 3.99 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__Q8__mnn__cpu`: does not fit in RAM: 4.22 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__Q8_0__llama.cpp__gpu`: does not fit in RAM: 3.99 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__Q8__mnn__gpu`: does not fit in RAM: 4.22 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__llama.cpp__cpu`: does not fit in RAM: 7.50 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__mnn__cpu`: does not fit in RAM: 15.00 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__llama.cpp__gpu`: does not fit in RAM: 7.50 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__mnn__gpu`: does not fit in RAM: 7.50 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3.5-4b__Q8_0__llama.cpp__cpu`: does not fit in RAM: 600 MB of the benchmark process was swapped out (stopped at > 512 MB)
- `qwen3.5-4b__Q8__mnn__cpu`: does not fit in RAM: 4.18 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run
- `qwen3.5-4b__Q8_0__llama.cpp__gpu`: does not fit in RAM: 723 MB of the benchmark process was swapped out (stopped at > 512 MB)
- `qwen3.5-4b__Q8__mnn__gpu`: does not fit in RAM: 4.18 GiB of weights in memory on a phone with 6.91 GiB RAM (limit 55%); not run

**OnePlus 15** (11):

- `smollm2-135m__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=2194330501256364 end=2194338508519590 prefill_us=982364 decode_us=6988371 prompt=256 gen=256 | Segmentation fault
- `gemma3-270m__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=2196989017045923 end=2197010643929248 prefill_us=5524423 decode_us=15807287 prompt=256 gen=256 | Segmentation fault
- `smollm2-360m__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=2198356956630974 end=2198368032137688 prefill_us=1125896 decode_us=9905001 prompt=256 gen=256 | Segmentation fault
- `qwen3-0.6b__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=2199327089575031 end=2199342397916171 prefill_us=3448664 decode_us=11668369 prompt=256 gen=256 | Segmentation fault
- `qwen3.5-0.8b__F16__mnn__gpu`: crash/error (exit 139): [llm_bench] -kv true is deprecated: use `-pg <pp>,<tg>` instead (`-p A -n B -kv true` == `-pg A,B`). | PB_MARK mnn mode=kv rep=0 begin=2354100756337034 end=2354117723592340 prefill_us=5966658 decode_us=10844879 prompt=256 gen=256 | Segm
- `qwen3-1.7b__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=2201644850475188 end=2201692903819128 prefill_us=20955444 decode_us=26665593 prompt=256 gen=256 | Segmentation fault
- `qwen3.5-2b__F16__mnn__gpu`: crash/error (exit 139): [llm_bench] -kv true is deprecated: use `-pg <pp>,<tg>` instead (`-p A -n B -kv true` == `-pg A,B`). | PB_MARK mnn mode=kv rep=0 begin=2359472474962537 end=2359533027613764 prefill_us=29609856 decode_us=30622202 prompt=256 gen=256 | Seg
- `qwen3-4b-instruct-2507__Q8__mnn__cpu`: does not fit in RAM: 2038 MB of the benchmark process was swapped out (stopped at > 512 MB)
- `qwen3-4b-instruct-2507__F16__mnn__cpu`: does not fit in RAM: 15.00 GiB of weights in memory on a phone with 14.76 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=2204299420187821 end=2204372118522689 prefill_us=25961864 decode_us=46452531 prompt=256 gen=256 | Segmentation fault
- `qwen3.5-4b__Q8_0__llama.cpp__cpu`: does not fit in RAM: 700 MB of the benchmark process was swapped out (stopped at > 512 MB)

**OnePlus 8 Pro** (25):

- `smollm2-135m__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=3449313005708 end=3476954763197 prefill_us=2642614 decode_us=24918440 prompt=256 gen=256 | Segmentation fault
- `gemma3-270m__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=3525182569949 end=3568380956495 prefill_us=10870924 decode_us=31729478 prompt=256 gen=256 | Segmentation fault
- `smollm2-360m__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=3614717857988 end=3646245739747 prefill_us=3698300 decode_us=27755604 prompt=256 gen=256 | Segmentation fault
- `qwen3-0.6b__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=3702722899309 end=3743497399606 prefill_us=9996049 decode_us=30423294 prompt=256 gen=256 | Segmentation fault
- `qwen3.5-0.8b__Q4_0__llama.cpp__gpu`: not run: head llama.cpp OpenCL build needs OpenCL symbols this phone's driver lacks
- `qwen3.5-0.8b__Q8_0__llama.cpp__gpu`: not run: head llama.cpp OpenCL build needs OpenCL symbols this phone's driver lacks
- `qwen3.5-0.8b__F16__llama.cpp__gpu`: not run: head llama.cpp OpenCL build needs OpenCL symbols this phone's driver lacks
- `qwen3.5-0.8b__F16__mnn__gpu`: crash/error (exit 139): [llm_bench] -kv true is deprecated: use `-pg <pp>,<tg>` instead (`-p A -n B -kv true` == `-pg A,B`). | PB_MARK mnn mode=kv rep=0 begin=10151625959866 end=10188652934070 prefill_us=11456158 decode_us=24840737 prompt=256 gen=256 | Segment
- `qwen3-1.7b__F16__mnn__cpu`: does not fit in RAM: 6.42 GiB of weights in memory on a phone with 11.24 GiB RAM (limit 55%); not run
- `qwen3-1.7b__F16__mnn__gpu`: crash/error (exit 139): PB_MARK mnn mode=kv rep=0 begin=4396668076752 end=4478064218700 prefill_us=29483435 decode_us=51528531 prompt=256 gen=256 | Segmentation fault
- `qwen3.5-2b__Q4_0__llama.cpp__gpu`: not run: head llama.cpp OpenCL build needs OpenCL symbols this phone's driver lacks
- `qwen3.5-2b__Q8_0__llama.cpp__gpu`: not run: head llama.cpp OpenCL build needs OpenCL symbols this phone's driver lacks
- `qwen3.5-2b__F16__mnn__cpu`: does not fit in RAM: 7.04 GiB of weights in memory on a phone with 11.24 GiB RAM (limit 55%); not run
- `qwen3.5-2b__F16__llama.cpp__gpu`: not run: head llama.cpp OpenCL build needs OpenCL symbols this phone's driver lacks
- `qwen3.5-2b__F16__mnn__gpu`: crash/error (exit 139): [llm_bench] -kv true is deprecated: use `-pg <pp>,<tg>` instead (`-p A -n B -kv true` == `-pg A,B`). | PB_MARK mnn mode=kv rep=0 begin=12065349525594 end=12147317689938 prefill_us=32657448 decode_us=48599592 prompt=256 gen=256 | Segment
- `qwen3-4b-instruct-2507__Q8_0__llama.cpp__cpu`: does not fit in RAM: 625 MB of the benchmark process was swapped out (stopped at > 512 MB)
- `qwen3-4b-instruct-2507__Q8_0__llama.cpp__gpu`: crash/error (exit 1): ggml_opencl: loading OpenCL kernels........................................................................... | ggml_opencl: default device: 'QUALCOMM Adreno(TM) (OpenCL 2.0 Adreno(TM) 650)' | main: error: failed to load model '/data/loc
- `qwen3-4b-instruct-2507__F16__llama.cpp__cpu`: does not fit in RAM: 7.50 GiB of weights in memory on a phone with 11.24 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__mnn__cpu`: does not fit in RAM: 15.00 GiB of weights in memory on a phone with 11.24 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__llama.cpp__gpu`: does not fit in RAM: 7.50 GiB of weights in memory on a phone with 11.24 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__mnn__gpu`: does not fit in RAM: 7.50 GiB of weights in memory on a phone with 11.24 GiB RAM (limit 55%); not run
- `qwen3.5-4b__Q4_0__llama.cpp__gpu`: not run: head llama.cpp OpenCL build needs OpenCL symbols this phone's driver lacks
- `qwen3.5-4b__Q8_0__llama.cpp__cpu`: does not fit in RAM: 556 MB of the benchmark process was swapped out (stopped at > 512 MB)
- `qwen3.5-4b__Q8__mnn__cpu`: does not fit in RAM: 4831 MB of the benchmark process was swapped out (stopped at > 512 MB)
- `qwen3.5-4b__Q8_0__llama.cpp__gpu`: not run: head llama.cpp OpenCL build needs OpenCL symbols this phone's driver lacks

**Redmi 9A** (18):

- `qwen3-0.6b__F16__mnn__cpu`: does not fit in RAM: 2.23 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3.5-0.8b__F16__mnn__cpu`: does not fit in RAM: 2.83 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3-1.7b__Q8_0__llama.cpp__cpu`: crash/error (exit 1): main: error: failed to load model '/data/local/tmp/paper_bench/models/qwen3-1.7b-q8_0.gguf'
- `qwen3-1.7b__F16__llama.cpp__cpu`: does not fit in RAM: 3.79 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3-1.7b__F16__mnn__cpu`: does not fit in RAM: 6.42 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3.5-2b__Q8__mnn__cpu`: does not fit in RAM: 1959 MB of the benchmark process was swapped out (stopped at > 512 MB)
- `qwen3.5-2b__F16__llama.cpp__cpu`: does not fit in RAM: 3.63 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3.5-2b__F16__mnn__cpu`: does not fit in RAM: 7.04 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__Q4_0__llama.cpp__cpu`: does not fit in RAM: 2.21 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__Q4__mnn__cpu`: does not fit in RAM: 2.35 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__Q8_0__llama.cpp__cpu`: does not fit in RAM: 3.99 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__Q8__mnn__cpu`: does not fit in RAM: 4.22 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__llama.cpp__cpu`: does not fit in RAM: 7.50 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3-4b-instruct-2507__F16__mnn__cpu`: does not fit in RAM: 15.00 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3.5-4b__Q4_0__llama.cpp__cpu`: does not fit in RAM: 2.43 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3.5-4b__Q4__mnn__cpu`: does not fit in RAM: 2.22 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3.5-4b__Q8_0__llama.cpp__cpu`: does not fit in RAM: 4.29 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run
- `qwen3.5-4b__Q8__mnn__cpu`: does not fit in RAM: 4.18 GiB of weights in memory on a phone with 3.72 GiB RAM (limit 55%); not run

