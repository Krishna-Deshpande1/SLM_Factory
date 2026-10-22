# Why each phone needs its own measurements: requirement difficulty

Generated 2026-10-16T14:35 by `Benchmark-Harness/paper_table5/requirements_md.py` from the four measured phones ([all_results.md](all_results.md) and [RESULTS.md](RESULTS.md)).

## The question

A user asks the SLM Factory for a model that meets a requirement on *their* phone, for example "a 1.5B+ model that streams at 20 tokens/s". From the model card alone, the Factory knows each configuration's file size, parameter count and bit-width. It does not know, for this phone:

- how fast prefill and decode are;
- how much energy a request costs;
- whether the configuration runs at all (it may not fit in memory, swap, or crash).

**Difficulty** is the chance that a configuration passing the model-card filter also meets the measured part of the requirement on the target phone, i.e. the success rate of a choice made without that phone's measurements:

| Tier | Chance of success without phone-specific measurements |
|---|---|
| Easy | 60–100%: the Factory alone usually gets it right |
| Medium | 30–60%: a coin flip |
| Hard | 0–30%: without measurements the Factory is usually wrong |

A requirement's tier is the average over the three flagship-class phones; each phone's own rate and tier is shown too, because the same requirement changes tier from phone to phone. The budget Redmi 9A is graded separately (last section).

**Phones:** Galaxy S23 (Snapdragon 8 Gen 2, 8 GB); OnePlus 15 (Snapdragon 8 Elite Gen 5, 16 GB); OnePlus 8 Pro (Snapdragon 865, 12 GB); Redmi 9A (Helio G25, 4 GB, CPU only). Same pool (9 models × Q4/Q8/F16 × llama.cpp/MNN × CPU/GPU), same 256-token prompt and 256-token answer. Energy is missing on the OnePlus 8 Pro, so energy requirements are graded on the other phones.

## Requirements by tier

| ID | Requirement | Use case | Model-card part (Factory can check) | Measured part (needs the phone) | Galaxy S23 | OnePlus 15 | OnePlus 8 Pro | Average | Tier |
|---|---|---|---|---|--:|--:|--:|--:|---|
| E1 | Basic assistant | a help bot where a slow but steady answer is fine | any model | decode ≥ 5 tokens/s | 79% (E) | 89% (E) | 65% (E) | 78% | **Easy** |
| E2 | Small on-device helper | smart replies, short rewrites | model file ≤ 1 GB | decode ≥ 10 tokens/s | 79% (E) | 94% (E) | 58% (M) | 77% | **Easy** |
| E3 | Capable model, patient user | offline Q&A where answer quality matters more than speed | ≥ 1.5B parameters | decode ≥ 5 tokens/s | 61% (E) | 86% (E) | 43% (M) | 64% | **Easy** |
| M1 | Interactive chat | a chat app: first token quickly, text faster than reading speed | any model | decode ≥ 10 tokens/s and first token ≤ 2 s after a 256-token prompt | 39% (M) | 72% (E) | 21% (H) | 44% | **Medium** |
| M2 | Mid-size model at reading speed | assistant that needs more than a toy model | ≥ 0.6B parameters | decode ≥ 10 tokens/s | 59% (M) | 85% (E) | 24% (H) | 56% | **Medium** |
| M3 | Higher precision | accuracy-sensitive extraction, no 4-bit quantization | 8-bit or 16-bit weights | decode ≥ 15 tokens/s | 51% (M) | 68% (E) | 24% (H) | 48% | **Medium** |
| M4 | Snappy small model | in-app assistant that must feel instant | model file ≤ 1 GB | decode ≥ 15 tokens/s and first token ≤ 1 s | 48% (M) | 90% (E) | 29% (H) | 56% | **Medium** |
| M5 | Keyboard autocomplete | suggestions while typing | model file ≤ 0.5 GB | first token ≤ 0.5 s and decode ≥ 30 tokens/s | 32% (M) | 71% (E) | 15% (H) | 39% | **Medium** |
| H1 | Capable fluent chat | a 1.5B+ assistant that streams faster than you read | ≥ 1.5B parameters | decode ≥ 20 tokens/s | 18% (H) | 39% (M) | 2% (H) | 20% | **Hard** |
| H2 | Capable interactive chat | the M1 chat app with a 1.5B+ model | ≥ 1.5B parameters | decode ≥ 10 tokens/s and first token ≤ 2 s | 16% (H) | 55% (M) | 0% (H) | 23% | **Hard** |
| H3 | Document summarization | read a long note or email, then summarize | any model | prefill ≥ 300 tokens/s (a 2,000-token document in under 7 s) and decode ≥ 10 tokens/s | 20% (H) | 54% (M) | 12% (H) | 29% | **Hard** |
| H4 | Battery-capped capable model | always-available assistant that must not drain the battery | ≥ 1.5B parameters | ≤ 50 J per request (256-token prompt + 256-token answer, about 0.1% of a phone battery) and decode ≥ 10 tokens/s | 0% (H) | 30% (H) | n/a | 15% | **Hard** |

Each cell: share of model-card-eligible configurations that meet the requirement on that phone, and that phone's tier (E/M/H).

**10 of 12 requirements change tier between phones** (E2, E3, M1, M2, M3, M4, M5, H1, H2, H3). For example, H2 (a 1.5B+ model with interactive latency) is a coin flip on the OnePlus 15, hard on the Galaxy S23 and impossible on the OnePlus 8 Pro, even though all three are Snapdragon flagships.

## What the measurements reveal: the passing configurations differ by phone

For the hard requirements, the configurations that pass on each phone (fastest decode first):

**H1 Capable fluent chat** (≥ 1.5B parameters; decode ≥ 20 tokens/s)

- Galaxy S23: Qwen3.5-2B Q4 MNN CPU, Qwen3-1.7B Q4 MNN CPU, Qwen3.5-2B Q4 MNN GPU, Qwen3-1.7B Q4_0 llama.cpp CPU (+4 more)
- OnePlus 15: Qwen3.5-2B Q4 MNN GPU, Qwen3.5-2B Q4_0 llama.cpp GPU, Qwen3.5-2B Q4 MNN CPU, Qwen3-1.7B Q4 MNN CPU (+13 more)
- OnePlus 8 Pro: Qwen3-1.7B Q4_0 llama.cpp CPU

**H2 Capable interactive chat** (≥ 1.5B parameters; decode ≥ 10 tokens/s and first token ≤ 2 s)

- Galaxy S23: Qwen3.5-2B Q4 MNN CPU, Qwen3-1.7B Q4 MNN CPU, Qwen3.5-2B Q4 MNN GPU, Qwen3.5-2B Q4_0 llama.cpp CPU (+3 more)
- OnePlus 15: Qwen3.5-2B Q4 MNN GPU, Qwen3.5-2B Q4_0 llama.cpp GPU, Qwen3.5-2B Q4 MNN CPU, Qwen3-1.7B Q4_0 llama.cpp CPU (+20 more)
- OnePlus 8 Pro: **none**

**H3 Document summarization** (any model; prefill ≥ 300 tokens/s (a 2,000-token document in under 7 s) and decode ≥ 10 tokens/s)

- Galaxy S23: SmolLM2-135M Q4 MNN CPU, SmolLM2-135M Q4_0 llama.cpp CPU, SmolLM2-135M Q8 MNN CPU, SmolLM2-135M Q8_0 llama.cpp CPU (+17 more)
- OnePlus 15: SmolLM2-135M Q4_0 llama.cpp CPU, SmolLM2-135M Q8_0 llama.cpp CPU, SmolLM2-135M Q4 MNN CPU, Gemma3-270M Q4_0 llama.cpp CPU (+52 more)
- OnePlus 8 Pro: SmolLM2-135M Q4_0 llama.cpp CPU, SmolLM2-135M Q4 MNN CPU, Gemma3-270M Q4_0 llama.cpp CPU, SmolLM2-135M Q8_0 llama.cpp CPU (+8 more)

**H4 Battery-capped capable model** (≥ 1.5B parameters; ≤ 50 J per request (256-token prompt + 256-token answer, about 0.1% of a phone battery) and decode ≥ 10 tokens/s)

- Galaxy S23: **none**
- OnePlus 15: Qwen3.5-2B Q4 MNN GPU, Qwen3.5-2B Q4_0 llama.cpp GPU, Qwen3.5-2B Q4 MNN CPU, Qwen3-1.7B Q4 MNN CPU (+9 more)
- OnePlus 8 Pro: no energy data

## Can another phone's measurements stand in?

If the Factory measured one phone and reused those numbers for another, how often would its choice still work? For each pair of flagship phones: **all passing** = deploy any configuration that met the requirement on the measured phone; **best pick** = deploy the one with the most decode headroom there.

| ID | Requirement | All passing: still meets it on the other phone | Best pick: still meets it |
|---|---|--:|--:|
| E1 | Basic assistant | 91% | 6 of 6 phone pairs |
| E2 | Small on-device helper | 85% | 6 of 6 phone pairs |
| E3 | Capable model, patient user | 82% | 6 of 6 phone pairs |
| M1 | Interactive chat | 67% | 6 of 6 phone pairs |
| M2 | Mid-size model at reading speed | 73% | 6 of 6 phone pairs |
| M3 | Higher precision | 76% | 6 of 6 phone pairs |
| M4 | Snappy small model | 69% | 6 of 6 phone pairs |
| M5 | Keyboard autocomplete | 66% | 6 of 6 phone pairs |
| H1 | Capable fluent chat | 61% | 4 of 6 phone pairs |
| H2 | Capable interactive chat | 28% | 2 of 4 phone pairs |
| H3 | Document summarization | 67% | 6 of 6 phone pairs |
| H4 | Battery-capped capable model | 0% | 0 of 1 phone pairs |

Reusing another flagship's measurements beats choosing blind. Its single fastest configuration usually transfers for easy and medium requirements, but not for hard ones (H1, H2, H4 above). And 24–34% of the configurations that pass a medium requirement on one phone fail it on another (33–100% for hard ones), so a Factory that needs a specific model, engine or bit-width still has to measure. Speed does not scale by a single factor between phones (next section).

## Why the Factory can't predict this from the model card

1. **Speed doesn't scale by one factor between phones.** The same configuration's prefill is 0.28× to 133× faster on the OnePlus 15 than on the Galaxy S23 (median 2.1×), and decode 0.92× to 4.6×. Against the S23, the OnePlus 8 Pro decodes at 0.14× to 1.1×. The engine, backend and weight type, not the phone's general speed, decide most of the outcome.
2. **"Use the GPU" is phone-specific.** GPU prefill beats CPU prefill in: Galaxy S23 18 of 38; OnePlus 15 42 of 42; OnePlus 8 Pro 11 of 33 configurations. GPU decode is usually *slower* than CPU decode on all three. On the OnePlus 8 Pro, llama.cpp on the GPU is unusable (Qwen3-1.7B 8-bit: 0.88 tokens/s prefill).
3. **The best configuration for the same goal differs by phone:**

   | Goal | Galaxy S23 | OnePlus 15 | OnePlus 8 Pro | Redmi 9A |
   |---|---|---|---|---|
   | Fastest decode, ≥ 1.5B parameters | Qwen3.5-2B Q4 MNN CPU | Qwen3.5-2B Q4 MNN GPU | Qwen3-1.7B Q4_0 llama.cpp CPU | Qwen3-1.7B Q4 MNN CPU |
   | Fastest prefill, Qwen3-0.6B | Qwen3-0.6B Q4 MNN GPU | Qwen3-0.6B Q4_0 llama.cpp GPU | Qwen3-0.6B Q4_0 llama.cpp CPU | Qwen3-0.6B Q4 MNN CPU |
   | Fastest prefill, any model | SmolLM2-135M Q8 MNN GPU | Gemma3-270M Q4 MNN GPU | Gemma3-270M Q4_0 llama.cpp CPU | Gemma3-270M F16 MNN CPU |

4. **Whether a configuration runs at all differs by phone** (it runs on one phone but not the other): Galaxy S23 vs OnePlus 15: 11 of 104; Galaxy S23 vs OnePlus 8 Pro: 9 of 104; OnePlus 15 vs OnePlus 8 Pro: 16 of 104. Memory limits, OpenCL driver support and crashes are device-specific: the head llama.cpp GPU build cannot even load on the OnePlus 8 Pro's driver.
5. **The cost of more bits differs by phone.** Going from 4-bit to 8-bit keeps a median 0.87–0.88× of decode speed on the Galaxy S23 and OnePlus 15 but only 0.66× on the OnePlus 8 Pro, and on the S23's llama.cpp GPU path 8-bit even decodes faster than 4-bit for some small models.

## The budget phone (Redmi 9A)

The flagship requirements are all **hard** on the Redmi 9A: almost nothing reaches 10 tokens/s. A budget phone needs its own requirements, and they show the same pattern:

| ID | Requirement | Model-card part | Measured part | Redmi 9A | Tier |
|---|---|---|---|--:|---|
| B1 | Runs at all | any model | loads and completes the benchmark | 65% | **Easy** |
| B2 | Tiny model, usable | model file ≤ 0.5 GB | decode ≥ 5 tokens/s | 65% | **Easy** |
| B3 | Small model, usable | model file ≤ 1 GB | decode ≥ 5 tokens/s | 46% | **Medium** |
| B4 | Mid-size model runs | ≥ 0.6B parameters | loads and completes the benchmark | 47% | **Medium** |
| B5 | Mid-size model, usable | ≥ 0.6B parameters | decode ≥ 3 tokens/s | 18% | **Hard** |
| B6 | Any model, answer in a minute | any model | first token ≤ 10 s and decode ≥ 5 tokens/s (256 tokens in under a minute) | 10% | **Hard** |

Flagship requirements on the Redmi 9A: E1 23%, E2 8%, E3 0%, M1 0%, M2 0%, M3 0%, M4 0%, M5 0%, H1 0%, H2 0%, H3 0%, H4 0%. On this phone llama.cpp decodes 1.7–3.5× slower than MNN for the same 4-bit model, so the engine choice alone decides whether a small model is usable, and which mid-size configurations even load depends on its 4 GB of RAM.

## Takeaways

- **Easy requirements** (loose speed targets) the Factory can meet without measuring: most configurations pass on a flagship phone.
- **Medium requirements** (interactive latency, higher precision, snappy small models) are a coin flip without measurements, and the odds differ by 3–5× between flagship phones.
- **Hard requirements** (a capable model that is fast, a long-prompt workload, a battery budget) are met by only a few configurations, and which ones depends on the phone's engine, GPU driver and memory. Without measuring that phone, the Factory is usually wrong, and even another flagship's measurements often pick a configuration that fails.
- **One benchmark pass per phone** (the 104-configuration sweep in [BENCHMARKING_GUIDE.md](BENCHMARKING_GUIDE.md)) turns every hard requirement into a lookup.

