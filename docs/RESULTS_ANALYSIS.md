# Are the Galaxy S23 Table 5 numbers right?

Sections 1-3 analyse the first session (Llama-3.2-1B and Qwen2.5-1.5B, Q4_0 / MNN 4-bit, screen on, run
2026-10-07 morning); section 5 compares it with the screen-off rerun that [RESULTS.md](RESULTS.md) now reports. Paper = arXiv 2607.05475; its closest phone to the S23 is the Xiaomi 14 (Snapdragon 8 Gen 3, one
generation newer).

## 1. Noise, or real?

**Real, and systematic, not noise.**

- **Repeatable.** Within a configuration the three recorded repetitions vary by 0.1–4% (e.g. Llama-1B llama.cpp CPU
  prefill 179.6 ± 2.2 t/s). A standalone `llama-bench` run outside the harness the same morning gave 179.9 ± 8.7.
- **Physically consistent across models.** If the numbers were noise, two different models would not land on the same
  hardware limit. They do:

  | Framework / backend | Decode: t/s × model size (effective GB/s), 1B / 1.5B | Prefill: t/s × 2 × params (TFLOP/s), 1B / 1.5B |
  |---|--:|--:|
  | llama.cpp CPU | 25.1 / 25.5 | 0.44 / 0.41 |
  | MNN CPU | 34.5 / 34.4 | 0.47 / 0.43 |
  | llama.cpp GPU | 14.4 / 15.0 | 0.43 / 0.39 |
  | MNN GPU | 13.9 / 11.4 | 0.77 / 0.71 |

  Decode reads every weight once per token, so it is memory-bandwidth bound: each engine reaches its own constant
  effective bandwidth. Prefill is a batched matrix multiply, compute bound: each engine reaches a constant FLOP rate.
- **In line with outside references.** The S23's LPDDR5X peaks at ~64 GB/s, and one processor alone sustains about
  40–45 GB/s on the next chip ([arXiv 2501.14794](https://arxiv.org/html/2501.14794v2)), so 25–35 GB/s effective is
  normal for these engines. ExecuTorch reports ~260 / 50 t/s for Llama-3.2-1B on a OnePlus 12 (8 Gen 3)
  ([ExecuTorch README](https://github.com/pytorch/executorch/blob/main/examples/models/llama/README.md)); the paper
  reports 327 / 76 on its newest phone; 180 / 33 on the 8 Gen 2 fits that progression.
- **Generation gap matches.** Against the Xiaomi 14, the CPU rows are at 0.77–0.80× for decode (memory: 8533 vs 9600
  MT/s LPDDR5X, plus a faster CPU) and 0.85–1.08× for llama.cpp prefill, what one generation predicts.

**Where the numbers look low (not wrong, but not the best this phone can do):**

1. **llama.cpp GPU prefill** (174 / 126 t/s, 0.34–0.39× the Xiaomi 14, and no faster than the CPU). Likely causes:
   the S23's Adreno 740 is not among the GPUs llama.cpp's Adreno kernels were validated on at `eadc418` (Adreno
   750, 830, X1-85: [OPENCL.md @ eadc418](https://raw.githubusercontent.com/ggml-org/llama.cpp/eadc418/docs/backend/OPENCL.md));
   and our default Q4_0 files keep the output / tied-embedding matrix at **Q6_K** (183 MiB of Qwen2.5-1.5B's
   ~890 MiB), which that document calls "supported, but not optimized"; Qualcomm's instructions quantize with
   `--pure`. A `Q4_0-PURE` variant is now available (`--gguf Q4_0-PURE`) to test this.
2. **MNN CPU prefill** (0.53–0.59× the Xiaomi 14) is lower than the generation gap alone explains. MNN runs 4 threads
   by default; which cores they land on differs between the 1+4+3 (8 Gen 2) and 1+5+2 (8 Gen 3) layouts.
3. **MNN GPU decode** (0.33–0.40× of MNN CPU decode) is weaker than in the MNN-LLM paper (~0.5×,
   [arXiv 2506.10443](https://arxiv.org/pdf/2506.10443)).

**One setting that was wrong and is fixed:** `llama-bench` defaults to one thread per core (8 on the S23, including
three Cortex-A510 little cores). Every thread then waits for the slowest: 68 t/s standalone and 20.8 t/s in a session,
against 180 at 4 threads. Using big + mid cores only is the published recommendation
([arXiv 2410.03613](https://arxiv.org/html/2410.03613v2)); all reported llama.cpp CPU rows use `-t 4`.

## 2. Is the GPU faster than the CPU?

| GPU ÷ CPU throughput | Llama-1B S23 | Qwen-1.5B S23 | Xiaomi 14 (paper), Llama / Qwen |
|---|--:|--:|--:|
| llama.cpp prefill | 0.97 | 0.94 | 2.71 / 2.33 |
| llama.cpp decode | 0.57 | 0.59 | 0.54 / 0.92 |
| MNN prefill | 1.64 | 1.66 | 1.15 / 1.05 |
| MNN decode | 0.40 | 0.33 | 0.80 / 0.57 |

- **Prefill: yes for MNN (1.6×), no for llama.cpp (≈ 1.0×).** Prefill is compute bound and the GPU has more
  arithmetic, so it should win; MNN's OpenCL kernels get that, llama.cpp's do not on the Adreno 740 (see 1.1). In the
  paper the GPU wins prefill on every phone.
- **Decode: no, the GPU is slower in every case (0.33–0.59×), and it is in the paper too** (Xiaomi 14: 0.54–0.92×).
  Decode is a matrix-vector product per token: bandwidth bound, and the CPU and GPU share the same LPDDR, so the GPU
  has no bandwidth advantage, while each token costs hundreds of small kernel launches and a host synchronization
  (~0.4 ms each, arXiv 2501.14794). The same result is reported for llama.cpp and MNN elsewhere (arXiv 2506.10443,
  [arXiv 2505.06461](https://arxiv.org/abs/2505.06461)).

## 3. Does the energy match the paper's pattern?

Energy so far exists for Llama-3.2-1B (the Qwen2.5-1.5B rows lost theirs when the trace ran out; they are being
rerun). Paper energy is reported for the Xiaomi 17 and OnePlus 15 only.

| Pattern | S23 (Llama-1B) | Paper, Xiaomi 17 / OnePlus 15 | Match |
|---|---|---|:-:|
| Decode costs more per token than prefill (decode ÷ prefill µJ/token) | 6.3–12.5× | 4.4–22× | yes |
| GPU prefill cheaper than CPU prefill (GPU ÷ CPU) | llama.cpp 0.74, MNN 0.51 | 0.34–0.87 | yes |
| GPU decode no cheaper than CPU decode (GPU ÷ CPU) | llama.cpp 1.17, MNN 0.99 | 0.70–1.49 | yes |
| Absolute µJ/token | 1.6e4–3.4e4 prefill, 2.0e5–2.8e5 decode | 5.4e3–1.6e4, 5.5e4–1.4e5 | 2–4× higher |

**Why the pattern holds:** power during inference is a few watts in both phases, so energy per token ≈ power ÷
tokens/s. Decode runs 4–17× fewer tokens per second than prefill on the S23 (5.5× for llama.cpp CPU), so it costs correspondingly more per token; the GPU prefills faster at
similar power, so its prefill is cheaper; GPU decode is slower at similar or lower power, so it saves nothing.

**Why the S23 reads 2–4× higher:** mostly because it is slower, not because it draws more power. Energy × throughput
gives the implied power: the S23 draws 3.6–8.9 W net, and the paper's values imply 0.9–13.8 W (e.g. llama.cpp CPU
decode: S23 7.7 W, Xiaomi 17 10.7 W). The rest is the measurement: the battery gauge includes DRAM, regulator losses
and other rails the SoC counters do not, and the S23's 4 nm chip is two generations older than the 8 Elite Gen 5. The
paper does not say it subtracts idle power; the S23 values are net of idle, which, if anything, makes them lower than
a gross measurement. Paper oddity to be aware of: its Xiaomi 17 MNN CPU row for Llama-1B (29.8 / 3.2 t/s) implies
0.3 W and is likely a measurement problem on their side.

## 4. Closing the protocol differences

| Difference | Status |
|---|---|
| **Airplane mode** | Done. Android keeps Wi-Fi on in airplane mode once Wi-Fi has been turned back on there (`wifi_apm_state = 1`), and Wireless debugging survives. The harness uses airplane mode in that case (radio-by-radio fallback otherwise). |
| **Screen off** | Done. Without a USB connection a screen-off phone suspends (60 s test: 15 stalls, up to 6.25 s). A partial wake lock held by the adb shell user (`tools/PbWake.java`, run with `app_process`, no app install) keeps the CPU running with the screen off (same test: no stall over 1.07 s). Screen off is now the default. |
| **28 °C cool-down** | Done. With the screen off the phone rests at 24.4 °C battery / 24.0 °C SoC / 26.1 °C skin, so every run starts at ≤ 28 °C by the paper's rule; no fan is needed. (With the screen on it rested at 31.6–32.2 °C.) |
| **SoC-only energy** | Not reachable without root (no powercap, no Power Stats rails on this phone). |
| **Phone model / RAM** | The S23 is not in the paper (compared with the Xiaomi 14), and it has 8 GB of RAM. Qwen2.5-7B Q4_0 does not fit: llama.cpp's CPU backend keeps a repacked copy of the 4.1 GB weights, 1.6–3 GB of the process ended up in swap and decode slowed to a crawl; on the GPU the phone became unresponsive while loading it. The harness now stops a run whose process has > 512 MB swapped out and records "does not fit in RAM". The paper's phones have 12–16 GB. |

## 5. Screen on vs screen off (same phone, same day)

| Configuration | Prefill t/s, on → off | Decode t/s, on → off | Energy/token change (prefill, decode) |
|---|--:|--:|--:|
| Llama-1B llama.cpp CPU | 179.6 → 190.1 (+6%) | 32.6 → 34.6 (+6%) | 0%, +1% |
| Llama-1B MNN CPU | 191.3 → 202.7 (+6%) | 44.4 → 45.6 (+3%) | −2%, +2% |
| Llama-1B llama.cpp GPU | 173.8 → 174.4 (0%) | 18.7 → 16.8 (−10%) | −4%, −6% |
| Llama-1B MNN GPU | 313.2 → 315.3 (+1%) | 17.9 → 14.4 (−20%) | −7%, +1% |
| Qwen-1.5B llama.cpp CPU | 133.7 → 140.7 (+5%) | 27.2 → 28.8 (+6%) | (no screen-on energy) |
| Qwen-1.5B MNN CPU | 138.8 → 147.1 (+6%) | 35.4 → 36.0 (+2%) | |
| Qwen-1.5B llama.cpp GPU | 126.1 → 126.2 (0%) | 16.1 → 13.5 (−16%) | |
| Qwen-1.5B MNN GPU | 230.8 → 232.3 (+1%) | 11.8 → 9.4 (−20%) | |

- **CPU rows are 2–6% faster** with the screen off, starting at ≤ 28 °C instead of ~31.8 °C (cooler silicon, and the
  display no longer shares the power budget).
- **GPU prefill is unchanged, GPU decode is 10–20% slower — but not because of the screen.** A controlled experiment
  (Llama-1B Q4_0, same commands, conditions interleaved, every run started at ≤ 28 °C, GPU clock and per-cluster CPU
  clocks sampled every 0.1 s; `results/gpu_decode_*_experiment/`) found:

  | Condition | llama.cpp GPU decode | MNN GPU decode |
  |---|--:|--:|
  | screen off, no energy trace (n=4) | 18.31 t/s | 17.70 t/s |
  | screen off, energy trace (n=2) | 18.66 | 18.57 |
  | screen off, two traces (n=2) | 18.96 | 20.34 |
  | screen on, no trace (n=2) | 19.16 | 18.11 |
  | screen on, energy trace (n=2) | 19.13 | 18.49 |

  The GPU ran at its maximum clock (711–719 MHz) in every condition, so the screen does not clock it down; the screen
  changes GPU decode by 2–5%, and the energy trace does not slow it (if anything it speeds it up). GPU decode is a
  long chain of small kernels launched by one CPU thread that mostly waits, so the CPU frequency governor keeps the
  clusters at low clocks (mid cores 1.1–1.5 GHz with the screen off and nothing else running vs 1.7–1.9 GHz with the
  screen on); anything that adds CPU load (the screen, a trace) raises the clocks and shortens each launch. That is
  why GPU decode is sensitive to background conditions at all.

  The 10–20% slower **session** is not explained by any of these conditions. Battery level was tested and ruled
  out: at 94–97% charge GPU decode was 18.17 / 18.18 t/s (llama.cpp) and 17.81 / 17.70 t/s (MNN), the same as at
  8–55% (`results/gpu_decode_fullcharge_experiment/`). The most likely remaining cause is a leftover process from a
  preceding aborted run competing for the CPU: the harness's stop command did not actually kill a benchmark it had
  "stopped" (fixed 2026-10-09, see [DEBUG.md](DEBUG.md)); this is untested.
- **Energy per token is reproducible:** the same configuration measured hours apart, screen on vs off, differs by
  −7% to +2%. Subtracting the idle power measured before each run removes the display's draw, and the remaining
  differences follow the throughput changes. This is the best evidence that the battery-gauge method is stable.



  Issues?
  Here's the verdict on the numbers so far.

1. SmolLM2 135M on MNN GPU: decode at 9.2 tokens/s is real, but it's an MNN problem, not a measurement error.

Every repetition gives the same result (27.6–28.2 s for 256 tokens), so it's not noise.
Prefill on the same run is very fast: 0.14 s for 256 tokens.
That works out to about 110 ms per generated token. For a 135M model, that's far too slow to be limited by memory or compute. Something in MNN's GPU decode path for this model costs ~110 ms per token: some operation running slowly or off the GPU, or a per-token overhead.
Qwen3.5 0.8B on the same backend decodes at 50 tokens/s, and llama.cpp's GPU path gets 39.6 tokens/s on this SmolLM2. So it's specific to MNN with SmolLM2.
I'll keep the number and footnote it. SmolLM2 360M and Gemma 270M are coming up soon and will show whether it's this model or MNN's handling of small models generally.
2. "Suspend detected" on the Qwen3.5 MNN GPU runs is a false alarm.

Every Qwen3.5 MNN GPU run reads 1.05–1.08×. The same check reads 1.00–1.015× on every pinned-build MNN GPU run and every CPU run.
So it follows the newer MNN version, not the phone. That version's llm_bench spends some time outside the prefill and decode intervals it reports. Its log also warns that the -kv option is deprecated, so its internals have changed.
A real suspend would also hit the CPU and llama.cpp runs, and it doesn't.
Throughput and energy both use MNN's own timed intervals, so neither is affected. I'll note the warning as a false positive.
3. Unusual, but consistent and explainable:

On llama.cpp GPU, 8-bit decodes as fast as or faster than 4-bit. Qwen3.5 0.8B: Q8_0 14.6 vs Q4_0 12.9 tokens/s. Qwen3.5 2B: 11.3 for both. GPU decode is limited by launching kernels from the CPU, not by memory bandwidth, so weight size barely matters. This matches what the GPU experiments showed.
F16 on the CPU is extremely slow (Qwen3.5 2B: 9.3 tokens/s prefill). That's the CPU build's flags, which follow llama.cpp's Android instructions and leave half-precision vector arithmetic off. These numbers are accurate for this build, but F16 could be several times faster with that flag on. I'll note it rather than rebuild mid-run.
Everything else passes: run-to-run spread under 5%, decode bandwidth in the expected 11–35 GB/s range, plausible implied power, no gate timeouts, no idle-CPU warnings, and no invalid energy.

The queue is still running normally: 5 of 72 pinned-build configurations done, and the phone is holding up.



11:13 AM
The queue is running normally. Two things will alert me: a background wait that ends when the queue finishes, a session stops, or the battery limit is hit; and a watch for failed configurations.
