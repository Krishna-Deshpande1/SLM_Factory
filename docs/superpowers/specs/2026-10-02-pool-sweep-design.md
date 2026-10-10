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
| Qwen3 | `Qwen/Qwen3-0.6B`, `Qwen/Qwen3-1.7B`, `Qwen/Qwen3-4B-Instruct-2507` |
| Qwen3.5 | `Qwen/Qwen3.5-0.8B`, `Qwen/Qwen3.5-2B`, `Qwen/Qwen3.5-4B` |

Identical to SLM_Factory's `config/android_pool.py`. All are post-trained chat models; Qwen3-0.6B/1.7B and
Qwen3.5 are hybrid-thinking (thinking is turned off, see below), Qwen3-4B-Instruct-2507 is non-thinking.

**Precisions per runtime** (not byte-identical across runtimes; the comparison is by bit width):

| Label | llama.cpp (GGUF) | MNN (`llmexport.py` flags) |
|---|---|---|
| F16 | `f16` | `--quant_bit 16 --quant_block 64 --lm_quant_bit 16` |
| Q8 | `Q8_0` | `--quant_bit 8 --quant_block 64 --lm_quant_bit 8` |
| Q4 | `Q4_K_M` | `--quant_bit 4 --quant_block 32 --hqq --lm_quant_bit 8` |

The MNN column is the recipe from `SLM_Factory/training/quantize_mnn.py` (`export_recipe()` and
`MNN_LM_QUANT_BIT`), not this repo's current `convert_to_mnn.py` (which uses block 64 and no HQQ for Q4).
SLM_Factory measured MNN's default 4-bit (min/max, block 64) losing up to 0.108 macro-F1 against `Q4_K_M` on
the same weights, and HQQ + block 32 closing that to within 0.011 for ~10% more file size. 8-bit keeps MNN's
default block. The lm_head is never quantized below the body (`max(8, bits)`).

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
- **`convert_to_mnn.py`:** ported to SLM_Factory's export behaviour (the recipe is copied, not imported, so
  this repo does not depend on SLM_Factory's training stack or on pymnn):
  - the per-precision flags in the table above, with the same `SLM_MNN_QUANT_BLOCK` / `SLM_MNN_HQQ` /
    `SLM_MNN_LM_QUANT_BIT` environment overrides;
  - every path handed to `llmexport.py` made absolute (it runs from its own directory; relative paths
    silently wrote the model into the MNN source tree in SLM_Factory run 40260162);
  - a half-written output directory is deleted before re-exporting;
  - completeness accepts `tokenizer.mtok` or `tokenizer.txt` (sentencepiece models such as Gemma write the
    latter; the current check requires `.mtok` only);
  - after export, `export_args.json` is read back and the export is refused if its `quant_bit`, `quant_block`,
    `hqq` or `lm_quant_bit` differ from what was asked for;
  - the validation sidecar records the recipe and the MNN commit/version, and a cache hit requires them to
    match, so a Q4 built with the old recipe is never reused as the new one;
  - toolchain located by `SLM_MNN_ROOT` / `SLM_MNN_LLMEXPORT` / `SLM_MNN_CONVERT_BIN` / `SLM_MNN_PYTHON` (same
    names as SLM_Factory), defaulting to a git-ignored `MNN/` checkout and `.venv_mnn` at this repo's root; Windows executable names
    (`MNNConvert.exe`, `Scripts\python.exe`).
  - Not ported: SLM_Factory's pymnn load-validation and thread/GPU cross-checks. They need a pymnn build with
    the LLM API on the laptop; here the phone run itself is the load test.
- **`compare_engines.py`:** show recorded failures as rows (`status = oom/crash/timeout/load_error`) and add a
  `--pivot` report: one row per (model, precision, backend), llama.cpp and MNN values side by side for each
  metric, written as text and CSV. Each row also reports how many reported answers are degenerate (no word
  characters once markup is stripped, SLM_Factory's `looks_degenerate()`), because SLM_Factory found MNN can
  decode garbage at some CPU thread counts with finite, plausible logits; speed numbers from a config whose
  answers are garbage are flagged rather than trusted.

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

- Android Studio (SDK platform-tools, NDK 27.2.12479018, CMake 3.22.1) and the two SmolChat APKs.
- Python 3.10+ venv for GGUF conversion and the harness (`torch`, `transformers`, `huggingface_hub`, `gguf`,
  `sentencepiece`, `perfetto`), and a Hugging Face token with the Gemma licence accepted.
- Visual Studio Build Tools (C++) to build `llama-quantize` (vendored llama.cpp) and `MNNConvert`.
- MNN toolchain matching SLM_Factory: `alibaba/MNN` pinned to **3.6.1 @ `47ccf6c6bb5b`** (the commit SLM_Factory
  recorded), `MNNConvert` built with `MNN_BUILD_CONVERTER=ON MNN_BUILD_LLM=ON MNN_LOW_MEMORY=ON
  MNN_SUPPORT_TRANSFORMER_FUSE=ON`, and a separate `.venv_mnn` for `llmexport.py` (CPU `torch`, `transformers`,
  `peft`, `onnx`, `onnxslim`, `onnxruntime`, `sentencepiece`, `numpy<3`, `tqdm`, `yaspin`, `Pillow`,
  `requests`, `datasets`). SLM_Factory's `scripts/setup_mnn_env.sh` is Linux/SLURM-specific; this repo's
  `scripts/setup_mnn.ps1` (Windows) and `scripts/setup_mnn.sh` (macOS/Linux) cover only these two stages (no
  pymnn, no CUDA), with the exporter's packages in `requirements-mnn.txt`.
- ~40 GB free disk (the largest model's download plus both conversions).

SLM_Factory exported all 9 pool models at all three precisions with this toolchain (backend matrix job
40305686), so export of SmolLM2, Gemma 3, Qwen3 and Qwen3.5 is known to work. Whether the phone's installed
MNN Chat loads MNN 3.6.1 exports is checked in the smoke test.

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
