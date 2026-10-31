# Does the harness measure accurately? OnePlus 15 against the paper

The OnePlus 15 is one of the paper's four phones (arXiv 2607.05475, Table 5), so its runs of the paper's own models
are a like-for-like check of the benchmark harness. Values: the paper's four models at 4-bit (llama.cpp Q4_0, MNN
4-bit), 256-token prefill and 256-token decode, ours divided by the paper's OnePlus 15 column. Source data:
`docs/RESULTS.md` section 1 (`results/oneplus15_supplement.json`).

**Short answer: mostly yes.** Where the phone's CPU frequency policy is not involved, the harness reproduces the
paper closely. The CPU paths come out about 30% slow, and the evidence points to the OnePlus 15's power management,
not to the measurement.

## Ours ÷ paper, OnePlus 15

| Engine / backend | Prefill | Decode | Verdict |
|---|---|---|---|
| llama.cpp GPU | 1.01–1.18× (median ~1.13) | 0.62–0.93× | Prefill matches; decode a bit low |
| MNN GPU | 0.95–1.09× (median ~1.03) | 1.09–2.27× | Prefill matches; decode faster than the paper (see below) |
| MNN CPU | 1.00× on 3 of 4 models (0.60× on Qwen2.5-1.5B) | 0.84–0.87× | Close |
| llama.cpp CPU | 0.54–0.75× (median ~0.67) | 0.68–0.77× | Consistently ~30% low |
| Decode energy | — | median ~0.95×, 10 of 16 cells within ±10% | Matches |
| Prefill energy | median 0.73× | — | Lower than the paper; unexplained |

## What this says about the harness

- **GPU prefill is the cleanest test, and it matches within about 10%.** It is pure GPU compute, unaffected by CPU
  frequency policy. Getting it right means the builds, model files, prompt and token counts, timing and the
  256/256 workload all reproduce the paper's setup.
- **Decode energy matches within about 5% at the median.** That is a good sign for the battery-based energy
  method, even though ours is whole-phone energy net of idle and the paper's is chip-only.
- **The llama.cpp CPU shortfall is the phone, not the harness.** Earlier runs of the same models on the same phone
  (`oneplus15_pinned_20261009_primed`) gave 370 / 283 / 135 tokens/s prefill for Llama-3.2-1B / Qwen2.5-1.5B /
  Llama-3.2-3B, 0.94–1.29× the paper. Those runs used an all-core warm-up burst before each benchmark; the newer
  runs did not, and got 217 / 163 / 78. So the CPU can reach the paper's speed, but whether it does depends on the
  governor state when the benchmark starts, which the harness does not fully control on this phone. This is the
  OnePlus 15's known slow-governor behaviour: a phone that looks idle keeps its CPU clocks low.
- **MNN GPU decode beating the paper is probably the paper's number.** The paper's OnePlus 15 value (12.3 tokens/s
  for Qwen2.5-1.5B) is far below its own Xiaomi 15 (45.8) on a similar chip. Ours (28.0) sits in between, so this is
  not counted against the harness.
- **Prefill energy is the one open question.** Whole-phone energy should read *higher* than chip-only energy, not
  27% lower. Short measurement windows are ruled out: they were 26–30 s, the same as on the Galaxy S23. One possible
  cause is how the OnePlus 15's battery gauge reports short bursts of current; this is not verified. Treat the
  OnePlus 15's absolute prefill energy as approximate. Comparisons between configurations on the same phone are
  unaffected.

**Conclusion:** the measurement itself is accurate. CPU-bound numbers on the OnePlus 15 carry about ±30%
uncertainty from its power management, so report the paper comparison per engine and backend, not as one overall
pass rate.

## Reruns still needed

### OnePlus 15: done (2026-10-27)

Two pool rows showed the slow-governor signature, a CPU prefill far below what the phone does elsewhere with a
normal decode. Both were rerun, and the reruns confirm it: prefill went up 2.4–5×, decode stayed within 5%.

| Configuration | Prefill (tokens/s) | Decode (tokens/s) | Prefill energy (mJ/token) | Decode energy (mJ/token) |
|---|--:|--:|--:|--:|
| Qwen3-0.6B Q8_0 llama.cpp CPU | 60.0 → **315.4** | 67.0 → 67.8 | 23.6 → **7.0** | 54.6 → 55.9 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 25.3 → **59.8** | 15.1 → 15.9 | 52.4 → **39.0** | 294.1 → 295.5 |

The reruns replace the first runs in `docs/RESULTS.md` (stored in `results/oneplus15_supplement.json`, `reruns`).
They also show the effect on energy: the throttled prefill cost 1.3–3.4× more energy per token, while decode energy
was unaffected.

Still optional: rerun the four llama.cpp CPU paper-model rows with the warm-up burst (`--prime-seconds`) to settle
which governor state is representative for the paper comparison above.

### Galaxy S23 (optional, ~1 h)

The S23 data is sound. Two small gaps:

- **One missing cell:** Qwen3.5-0.8B Q4_0 llama.cpp CPU has no prefill energy. Its energy run's prefill was 17×
  slower than every other measurement of that configuration, so it was excluded (kept under
  `results/slm_pool_s23_head/anomalies/`). About 15 minutes.
- **Three rows with a raised idle baseline** (0.8–0.9 W against the usual 0.24 W): Qwen3-1.7B Q8_0 llama.cpp GPU,
  Qwen3-1.7B Q8 MNN CPU and Qwen3.5-2B Q4_0 llama.cpp GPU. Something else drew power during their idle measurement,
  so their net energy is likely 10–15% too low. Worth rerunning only to make every cell clean.
