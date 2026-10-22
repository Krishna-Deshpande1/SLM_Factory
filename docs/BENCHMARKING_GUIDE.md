# Benchmarking a phone the same way as the Galaxy S23

How to measure the SLM_Factory model pool on an Android phone with exactly the protocol, scripts and flags used for
the Galaxy S23 results in [RESULTS.md](RESULTS.md). The method follows Table 5 of arXiv 2607.05475: 256-token
prefill and 256-token decode, throughput in tokens/s and energy in µJ/token, llama.cpp and MNN, CPU and OpenCL GPU.
All scripts are in `Benchmark-Harness/paper_table5/`; run every command from that folder. The commands are written
for Windows PowerShell; they are the same on macOS with `/` paths.

In the commands, `S` is the phone's adb serial, e.g. `192.168.86.75:5555`.

## What gets measured

- **Pool:** the 9 models in SLM_Factory's `config/android_pool.py`, exported as `pools/slm_factory_android_pool.py`:
  SmolLM2-135M, SmolLM2-360M, Gemma-3-270M, Qwen3-0.6B, Qwen3-1.7B, Qwen3-4B-Instruct-2507, Qwen3.5-0.8B,
  Qwen3.5-2B, Qwen3.5-4B.
- **Configurations per model:** 3 quantizations × 2 frameworks × 2 backends = 12, so 108 in total.
  - llama.cpp GGUF: `Q4_0`, `Q8_0`, `F16`
  - MNN: 4, 8 and 16-bit (`Q4`, `Q8`, `F16`)
  - Backends: CPU and GPU (OpenCL)
- **Two builds:** the paper's versions (llama.cpp `eadc418`, MNN `510ac8f`, called `pinned`) for the first 6 models.
  Qwen3.5 is newer than those versions, so it runs on current upstream (`--ref head`: llama.cpp `9c2e0e4`,
  MNN `a8ec497`, text decoder only).
- **Per configuration:** 1 warm-up and 3 recorded repetitions (mean ± std); extra repetitions are added until each
  phase has at least 30 s of timed work for the energy reading.
- **Energy:** Perfetto battery-current trace (100 ms) × voltage, minus idle power measured for 12 s right before
  every invocation. It covers the whole phone, not just the SoC, and is only valid while the phone is unplugged.

## One-time setup on the computer

1. **Android Studio**, then in SDK Manager > SDK Tools install Platform-Tools, **NDK** (we used 27.2.12479018),
   **CMake** (3.22.1) and Build-Tools. Also install a **JDK** (`javac` on PATH); it builds the small wake-lock
   helper.
2. **Python 3.12.** Python 3.13 builds numpy from source and the result crashes on Windows.
   ```
   uv venv .venv_pb --python 3.12
   uv pip install --python .venv_pb -r requirements.txt
   uv venv .venv_pb_head --python 3.12
   uv pip install --python .venv_pb_head -r requirements-head.txt
   ```
   - `.venv_pb` runs everything and exports MNN models for the pinned build (MNN 3.4.0).
   - `.venv_pb_head` exports Qwen3.5 to MNN (MNN 3.6.1).
   - Without uv, create a conda Python 3.12 and make each venv from it.
3. **Host `llama-quantize`** (optional, but much faster than quantizing on the phone). `build_binaries.py` checks
   out llama.cpp under `%USERPROFILE%\.pb\src`. Build the `llama-quantize` target there once per ref with
   MSYS2 gcc, as described in [the harness README](../Benchmark-Harness/paper_table5/README.md#one-time-setup-per-computer).
   Ours are `%USERPROFILE%\.pb\build\host-quantize\bin\llama-quantize.exe` (pinned) and
   `...\host-quantize-head\bin\llama-quantize.exe` (head).
4. **Disk space:** about 90 GB free. The converted models take 81 GB (45 GB GGUF, 36 GB MNN); the source
   checkouts, builds and Hugging Face downloads need a few GB more while converting.
5. **Laptop power settings:** plug the laptop in and keep the lid open. The harness blocks idle sleep while it
   runs, but closing the lid or restarting still stops a session.

## One-time setup on the phone

1. **Developer options:** USB debugging on, Wireless debugging available.
2. **Samsung: turn RAM Plus off** (Settings > Device care > Memory > RAM Plus), then restart. With it on, large
   models thrashed swap and the phone hard-reset.
3. **Wi-Fi in airplane mode:** turn airplane mode on, turn Wi-Fi back on, then turn airplane mode off. Android
   remembers this, so the harness's airplane mode keeps Wi-Fi and adb.
4. **Free storage:** about 90 GB. Every converted model is pushed to `/data/local/tmp/paper_bench` (81 GB on the
   S23). On a phone with less space, prepare and run one model at a time with `--models`, and delete pushed
   models after they finish.

## Connecting over Wi-Fi

Benchmarks run with the phone **unplugged**: charging current replaces the battery reading that energy comes from.

1. Put the laptop and the phone on the **same Wi-Fi network**. Campus networks with many access points (e.g.
   University of Washington) drop the connection when the phone roams; a home network was the most stable.
2. Plug in USB once and switch adb to a fixed port. The Wireless debugging port changes every time the service
   restarts, while port 5555 stays until the phone reboots:
   ```
   adb tcpip 5555
   adb connect <phone Wi-Fi IP>:5555
   ```
   Then unplug. The Wireless debugging toggle shows off afterwards; that is expected. Without a cable, connect
   once with Wireless debugging (`adb connect IP:PORT`) and run `adb -s IP:PORT tcpip 5555`.
3. Keep the phone awake for long runs:
   ```
   adb -s S shell cmd deviceidle disable
   ```
   Undo it with `cmd deviceidle enable` when all benchmarking is done.

## Build and prepare (once per phone)

```
.venv_pb\Scripts\python build_binaries.py --push --serial S
.venv_pb\Scripts\python build_binaries.py --ref head --push --serial S

$env:SLM_MNN_PYTHON = "$PWD\.venv_pb\Scripts\python.exe"
.venv_pb\Scripts\python prepare_models.py --pool-file pools\slm_factory_android_pool.py --gguf Q4_0 Q8_0 F16 --mnn 4 8 16 --quantize-bin $env:USERPROFILE\.pb\build\host-quantize\bin\llama-quantize.exe --serial S

$env:SLM_MNN_PYTHON = "$PWD\.venv_pb_head\Scripts\python.exe"
.venv_pb\Scripts\python prepare_models.py --ref head --pool-file pools\slm_factory_android_pool.py --gguf Q4_0 Q8_0 F16 --mnn 4 8 16 --quantize-bin $env:USERPROFILE\.pb\build\host-quantize-head\bin\llama-quantize.exe --serial S
```

- The first build takes about 15 minutes per ref. `build_binaries.py` also builds the wake-lock helper
  (`tools/PbWake.java`).
- `prepare_models.py` downloads from Hugging Face and skips models that belong to the other ref ("needs --ref
  head"); that is expected.
- **Energy method, only for a phone model we have not measured** (about 15 minutes, unplugged):
  `python energy_probe.py collect --serial S`. Add a profile for the phone to `devices.json` with the recommended
  `energy_method`, `max_temp_c: 28` and a `proxy_column` (the closest phone in the paper). The S23 profile is the
  example; it uses `perfetto:current`.

## Run

Results go to one folder per build. Both commands are resumable: rerunning skips finished configurations.

```
.venv_pb\Scripts\python -u run_paper_table5.py --serial S --llama-threads 4 --min-battery 5 --smallest-first --ref pinned --pool-file pools\slm_factory_android_pool.py --results-dir results\slm_pool_<phone>_pinned

.venv_pb\Scripts\python -u run_paper_table5.py --serial S --llama-threads 4 --min-battery 5 --smallest-first --ref head --pool-file pools\slm_factory_android_pool.py --results-dir results\slm_pool_<phone>_head
```

| Flag | Why |
|---|---|
| `--llama-threads 4` | llama-bench's default (one thread per core, including little cores) ran 3–9× slower on the S23 |
| `--min-battery 5` | stops the session at 5% before any benchmark run starts; the default is 20% |
| `--smallest-first` | finishes the small models before the large ones |
| `--ref pinned` / `--ref head` | which build to use (see "What gets measured") |
| `--pool-file` | the model list |
| `--results-dir` | where results go; reusing it resumes |

Everything else stays at its default:

- `--trials 3`, `--n-prompt 256`, `--n-gen 256`
- cool-down gate at 28 °C battery temperature before every invocation (`--gate-timeout 1800`)
- 40 s idle baseline at session start, `--pre-idle-seconds 12`, `--min-energy-seconds 30`
- airplane mode, Do Not Disturb, background apps killed, screen off with a partial wake lock

`models\queue_pool.cmd S` in our checkout chains both runs, the S23's extra paper-model runs and the reports, and
logs to `models\queue.log`. It is gitignored, so copy it from Evan if useful.

### While it runs

- **Speed:** each configuration takes 15–40 minutes, mostly cooling to 28 °C between runs. 108 configurations
  took us several days of sessions.
- **Battery:** a full charge lasts about 6–9 hours of benchmarking. When the session stops at 5%, charge to full,
  unplug and rerun the same command.
- **Don't switch git branches** or edit files in `Benchmark-Harness/paper_table5` while it runs. A checkout that
  removes the scripts crashes the session.
- **If the phone drops off Wi-Fi,** run `adb connect S` again. The harness waits up to 30 minutes for it. If the
  phone rebooted, port 5555 is gone: redo the tcpip step.
- **If a session stops early** (phone unreachable, laptop restart), recover its energy before starting the next
  session. Once a new session starts, the old trace is set aside on the phone, and a reboot deletes it:
  ```
  .venv_pb\Scripts\python run_paper_table5.py --serial S --results-dir results\slm_pool_<phone>_pinned --recover-energy
  ```

## Storage and memory (after the benchmarks)

```
.venv_pb\Scripts\python -u memory_usage.py --serial S --ref pinned --pool-file pools\slm_factory_android_pool.py --results-dir results\slm_pool_<phone>_pinned
.venv_pb\Scripts\python -u memory_usage.py --serial S --ref head --pool-file pools\slm_factory_android_pool.py --results-dir results\slm_pool_<phone>_head
```

- **Storage:** the size of the model files as pushed to the phone.
- **Memory:** one run of the same workload (256-token prompt, then 256 generated tokens) per configuration. It
  records the process's peak total memory from `dumpsys meminfo` (PSS, GPU buffers included) and the GPU share
  ("GL mtrack"), plus peak resident memory, swap and the drop in the phone's free memory.
- Use `dumpsys meminfo`, not just resident memory: a GPU run's weights and KV cache live in GPU buffers that
  resident memory doesn't count.
- It takes about 1 minute per small configuration and needs no cool-down. Results go to `<results-dir>/memory/`,
  and `results_md.py` adds them to the pool table.

## Reports

```
.venv_pb\Scripts\python report.py results\slm_pool_<phone>_pinned results\slm_pool_<phone>_head --out results\slm_pool_<phone>_report
.venv_pb\Scripts\python results_md.py
```

- `report.py` writes `TABLE5.md`, `.html` and `.csv` in the paper's layout.
- `results_md.py` writes `docs/RESULTS.md` and `docs/figures/`. Edit its `T5_DIR` / `POOL_DIRS` to point at the
  new phone's folders.

## Expected failures (not problems with your setup)

- **F16 4B models and some 2B F16 / 4B Q8 configurations** don't fit in 8 GB of RAM. The harness records "does
  not fit in RAM" or stops a run that swaps more than 512 MB.
- **MNN GPU with 16-bit weights** segfaults (exit 139) on the pinned MNN for every model we tried.
- **llama.cpp GPU Q8_0 / F16 prefill** at `eadc418` is slower than decode for the small models. RESULTS.md flags
  these rows as not citable.

`python tests\fake_phone_test.py` checks the harness end to end without a phone; run it after changing any code.
