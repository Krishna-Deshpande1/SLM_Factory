# Are the Galaxy S23 Table 5 numbers right?

Analysis of the first session in [RESULTS.md](RESULTS.md) (Llama-3.2-1B and Qwen2.5-1.5B, Q4_0 / MNN 4-bit, run
2026-10-07). Paper = arXiv 2607.05475; its closest phone to the S23 is the Xiaomi 14 (Snapdragon 8 Gen 3, one
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
| **Airplane mode** | Works over Wireless debugging when the phone keeps Wi-Fi on in airplane mode (Android remembers this once Wi-Fi is turned back on in airplane mode: `wifi_apm_state = 1`, now set on the S23). The harness now uses airplane mode in that case and keeps the radio-by-radio fallback otherwise. |
| **Screen off** | A screen-off phone with no USB connection suspends: in a 60 s test with the screen off it stalled 15 times, up to 6.25 s. A partial wake lock held by the adb shell user (`tools/PbWake.java`, run with `app_process`, no app install) keeps the CPU running with the screen off: same test, no stall over 1.07 s. The harness now runs screen-off by default over Wi-Fi with this wake lock. |
| **28 °C cool-down** | With the screen on, the phone rested at 31.6–32.2 °C (median 31.8 °C) for six hours where it is now. Turning the screen off should lower that; earlier screen-off probe runs sat at 24.9–27 °C. If the screen-off resting temperature is still above 28 °C, a fan blowing across the back of the phone or a cooler room is needed; a ~4 °C difference changes leakage power by a few percent at most, so it matters much less for these short runs than cooling down fully between runs (which the gate already enforces). |
| **SoC-only energy** | Not reachable without root (no powercap, no Power Stats rails on this phone). |
| **Phone model** | The S23 is not in the paper; compared with the Xiaomi 14. |
