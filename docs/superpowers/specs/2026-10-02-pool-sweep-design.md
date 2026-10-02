# Model-pool benchmark sweep (Windows, wireless adb) — design

Status: approved in chat 2026-10-02, pending written-spec review.

## Goal

One unattended command, run on a Windows laptop, that benchmarks a fixed pool of small language models on a
phone connected over wireless adb, across two runtimes (llama.cpp via SmolChat, MNN via MNN Chat), two hardware
backends (CPU, OpenCL GPU) and three precisions (F16, Q8, Q4), and produces a side-by-side comparison of:
memory, cold start, prefill tokens/s, decode tokens/s and energy.

## Scope

**Pool** (9 models x 3 precisions = 27 variants), stored in `Benchmark-Harness/model_pool.json`:

| Family | Hugging Face id |
|---|---|
| Gemma 3 | `google/gemma-3-270m-it` |
| SmolLM2 | `HuggingFaceTB/SmolLM2-135M-Instruct`, `HuggingFaceTB/SmolLM2-360M-Instruct` |
| Qwen3 | `Qwen/Qwen3-0.6B`, `Qwen/Qwen3-1.7B`, `Qwen/Qwen3-4B` |
| Qwen3.5 | `Qwen/Qwen3.5-0.8B`, `Qwen/Qwen3.5-2B`, `Qwen/Qwen3.5-4B` |

Exact Qwen3.5 repo ids are confirmed against Hugging Face during implementation.

**Precisions per runtime** (unchanged from the existing converters; not byte-identical across runtimes):

| Label | llama.cpp (GGUF) | MNN |
|---|---|---|
| F16 | `f16` | `q16` (fp16) |
| Q8 | `Q8_0` | 8-bit, block 64 |
| Q4 | `Q4_K_M` | 4-bit, block 64, lm_head 8-bit |

**Configurations:** 27 variants x 2 runtimes x 2 backends = 108. Every variant is attempted, including ones
that will not fit in memory (F16 4B models are ~8 GB on an 8 GB phone); those are recorded as failures.

**Metrics and protocol:** unchanged from the existing harnesses (`run_autobench.py`, `run_mnn_autobench.py`,
`bench_common.py`): the 10 built-in `DEFAULT_QUESTIONS`; per question gate -> force-stop -> page-cache eviction
-> 3 back-to-back runs (run 1 cold, run 3 reported); energy from a Perfetto battery trace, net of an idle
baseline, aggregated as mJ/token.

**Out of scope:** answer-quality scoring, Vulkan, models other than the pool, changes to either Android app.

## Target device

Samsung Galaxy S23 (SM-S911U, Snapdragon 8 Gen 2 / Adreno 740, 8 GB), reached over wireless adb
(serial like `10.19.204.70:43523`; the port changes whenever wireless debugging restarts). Both installed apps
expose their headless receivers (`io.shubham0204.smollmandroid/.headless.HeadlessBenchmarkReceiver`,
`com.alibaba.mnnllm.android/.benchmark.headless.BenchmarkHeadlessReceiver`). The S23 on Android 16 is already in
`run_autobench.py`'s `KNOWN_AFFECTED_DEVICES`, so GGUF files are piped into the app's internal storage.

## Architecture

```
run_pool_sweep.py  (new, orchestrator)
  ├─ reads model_pool.json + sweep options
  ├─ per model: convert_to_gguf.py / convert_to_mnn.py   (existing, path fixes only)
  ├─ per config: run_autobench.py / run_mnn_autobench.py (existing, small fixes) as subprocesses
  ├─ bench_common.py                                     (existing, Windows + gate fixes)
  └─ compare_engines.py --pivot                          (existing + new side-by-side report)
```

### New: `Benchmark-Harness/run_pool_sweep.py`

Pure-Python replacement for `run_model_sweep.sh` (which stays for macOS). Responsibilities:

1. **Preflight:** resolve `adb`; require `--serial` (or exactly one connected device); check both receivers
   exist; check both SmolChat APK paths exist; record device model, Android SDK level, MemTotal and both apps'
   versions into `pool_results/sweep_info.json`.
2. **Phone setup for the whole sweep** (originals saved and restored on exit, including Ctrl+C):
   screen timeout set to its maximum, brightness to minimum with auto-brightness off, both packages added to the
   battery-optimization allowlist (`dumpsys deviceidle whitelist +<pkg>`), a wake key sent before each config.
3. **Sweep-level heat baseline:** rest 5 minutes, read battery temperature once, and pass
   `--gate-max-temp <baseline + rise>` (default rise 2 C, optional absolute cap) to every configuration, so the
   limit does not drift upward across configurations the way the per-config relative gate does.
4. **Model-first loop:**
   ```
   for model in pool:
       convert -> GGUF {f16, q8_0, q4_k_m} and MNN {q16, q8, q4}        (skip if already present)
       install SmolChat CPU APK;    for quant: run llama.cpp CPU
       install SmolChat OpenCL APK; for quant: run llama.cpp OpenCL
       push MNN folders;            for quant: run MNN CPU, then MNN OpenCL
       delete this model's files from the phone and the laptop (unless --keep-files)
   ```
   GGUF files are pushed by `run_autobench.py` itself (existing behaviour); MNN folders are pushed by the
   orchestrator to `/data/local/tmp/mnn_models` (existing location).
5. **Between configurations:** a gated rest (same readiness gate, not a fixed sleep), plus a connection check;
   if the phone is unreachable, wait and retry `adb connect <serial>` for up to 30 minutes before aborting.
6. **Resume:** a configuration whose result file (success or recorded failure) exists is skipped; conversions
   that already exist are reused; `--plan` prints the remaining work and exits.
7. **Report:** after each model and at the end, regenerate the summary and side-by-side report.

Options: `--serial`, `--pool`, `--models` (subset), `--backends cpu,opencl`, `--runtimes llamacpp,mnn`,
`--gate-rise`, `--gate-max-temp`, `--keep-files`, `--plan`, `--results-dir`.

### Changes to existing files

- **`bench_common.py`:** NDK clang path resolved per OS (Windows:
  `%LOCALAPPDATA%\Android\Sdk\ndk\27.2.12479018\toolchains\llvm\prebuilt\windows-x86_64\bin\aarch64-linux-android28-clang.cmd`),
  overridable by `ANDROID_NDK_HOME`; venv interpreter resolved as `Scripts\python.exe` on Windows; the shared
  `Adb` wrapper honours a serial (`-s`).
- **`run_autobench.py`:** `--serial` replaces the USB-only `-d` flag (`-d` kept when no serial is given);
  `adb` fallback paths for Windows; drop the hard-coded `~/SLM_Factory_Krishna_Personal` paths (sibling
  `bench_common.py` only); **fail fast**: if run 1 of question 1 fails with a load error, crash, or timeout,
  retry question 1 once, then stop the configuration and write a failure result instead of retrying every
  question.
- **`run_mnn_autobench.py`:** same `--serial`, Windows `adb`, fail-fast and failure-result changes.
- **`convert_to_gguf.py`:** `llama-quantize` located at `build/bin/Release/llama-quantize.exe` on Windows, or
  `--quantize-bin`. Built from the vendored `SmolChat-Android/llama.cpp` so GGUF versions match the app.
- **`convert_to_mnn.py`:** `MNN_ROOT`, `llmexport.py` interpreter and `MNNConvert` paths configurable by flags /
  environment instead of `~/SLM_Factory_Krishna_Personal/MNN`; Windows executable names.
- **`compare_engines.py`:** show recorded failures as rows (`status = oom/crash/timeout/load_error`) and add a
  `--pivot` report: one row per (model, precision, backend), llama.cpp and MNN values side by side for each
  metric, written as text and CSV.

## Failure handling

| Situation | Behaviour |
|---|---|
| Variant does not fit (load fails, app killed, timeout on run 1 of question 1) | Stop that config after one retry of question 1; write `<config>.json` with `status` and the logcat reason; continue |
| Conversion fails | Record a failure for every config of that variant; continue with the next variant |
| Phone disconnects | Pause, retry `adb connect` for up to 30 min, then resume the same config from scratch |
| Phone externally powered at a config start | Warn loudly in the log; energy for that config is flagged invalid by the existing logic |
| Ctrl+C | Stop the energy trace, restore phone settings, leave completed results in place |

Failure results are written with the same `run_info` labels as successes, so resume skips them; deleting the
file forces a retry.

## Thinking mode

- **llama.cpp (SmolChat):** already off. `LLMInference.cpp` always renders the chat template with
  `enable_thinking = false`; no change needed.
- **MNN Chat:** the harness's `--no-think` appends ` /no_think` to the prompt. The orchestrator passes it for
  Qwen models only (so SmolLM/Gemma prompts are identical across runtimes). Qwen3 honours this switch; whether
  Qwen3.5 does is **unverified**, and the MNN app source is not in this repo. Verification step: one Qwen3.5
  question on MNN, check the response for a `<think>` block. If thinking cannot be disabled there, those
  results are kept but flagged `thinking_on: true` in the report.

## Laptop prerequisites (runner)

Android Studio (SDK platform-tools, NDK 27.2.12479018, CMake 3.22.1), Python 3.10+ with a venv containing
the conversion requirements (`torch`, `transformers`, `huggingface_hub`, `gguf`, `sentencepiece`, `perfetto`),
a Hugging Face token with the Gemma licence accepted, Visual Studio Build Tools (C++) for `llama-quantize` and
`MNNConvert`, a clone of public `alibaba/MNN` at a version compatible with the installed MNN Chat, the two
SmolChat APKs, and ~40 GB free disk (largest single model's downloads plus both conversions).

## Testing

- **Unit tests (no phone):** pool parsing, plan generation and resume skipping, failure-result writing and
  classification, pivot report on the existing `sweep_results/smollm2-135m` files.
- **Dry run:** `--plan` against the real pool.
- **Smoke test on the phone:** SmolLM2-135M only, Q4 only, both runtimes, both backends, with a 1-question set;
  confirm valid energy (phone unplugged), cold-start eviction succeeds, results and pivot report are produced.
- **Fail-fast check:** Qwen3-4B F16 on CPU must produce a failure result within minutes, not hours.

## Estimated run time

The existing log shows ~1.5 h for all 12 configurations of SmolLM2-135M. Larger models load and decode much
more slowly and wait longer at the heat gate; expect well over a day for the full pool. The sweep is resumable,
so it can run across several sessions.

## Open items

Waiting on the user's device outputs: Android SDK level (`KNOWN_AFFECTED_DEVICES` expects 36), MNN Chat
version (determines the MNN conversion version), SmolChat APK install/signature check, and `run-as`
(debuggable) check.
