# Does measuring help? Claude without measurements vs the harness

Generated 2026-10-28T01:44 by `Benchmark-Harness/paper_table5/baseline.py` from `baseline/claude_response.txt` and the measured filter in [SCENARIOS.md](SCENARIOS.md).

A Claude chat was given what the SLM Factory knows *without* this harness (phone specs, model cards, runtime and backend; prompt: `Benchmark-Harness/paper_table5/baseline/baseline_prompt.md`) and asked to filter the pool and pick a configuration for the same 60 scenarios. Its answers are scored against the measured values. With measurements the pick meets every limit by construction.

## Headline

| | Claude without measurements | With the harness |
|---|--:|--:|
| Pick actually meets all four limits on the phone | 37 of 60 (62%) | 60 of 60 |
| Pick is the same configuration as the measured best | 26 of 60 (43%) | 60 of 60 |
| Pick has the same model and precision as the measured best | 35 of 60 (58%) | 60 of 60 |
| Pick is more capable than anything that actually fits (over-promised) | 3 of 60 | 0 |
| Pick fails with the right model and precision, on the wrong runtime or backend | 7 of 60 | 0 |
| Pick cannot run on the phone at all | 4 of 60 | 0 |
| Kept pool: share of predicted-kept configurations that really meet the limits (median precision) | 81% | 100% |
| Kept pool: share of really-passing configurations it kept (median recall) | 95% | 100% |
| Kept pool overlap (median Jaccard) | 0.73 | 1.00 |

## By tier

| Tier | Scenarios | Pick works | Same pick | Median precision | Median recall | Median Jaccard |
|---|--:|--:|--:|--:|--:|--:|
| Easy | 20 | 15 | 11 | 86% | 96% | 0.83 |
| Medium | 20 | 11 | 5 | 78% | 94% | 0.73 |
| Hard | 20 | 11 | 10 | 52% | 93% | 0.45 |

## By phone

| Phone | Scenarios | Pick works | Same pick | Median Jaccard |
|---|--:|--:|--:|--:|
| Galaxy S23 | 11 | 5 | 5 | 0.64 |
| OnePlus 15 | 31 | 23 | 16 | 0.81 |
| OnePlus 8 Pro | 11 | 6 | 3 | 0.58 |
| Redmi 9A | 7 | 3 | 2 | 0.44 |

## Which limits it gets wrong

For configurations that both the measurements and Claude say run: how often its call on each limit matches the measured one.

| Limit | Agreement | Misses it predicted but did not happen | Misses that happened but it did not predict |
|---|--:|--:|--:|
| Peak memory | 93% | 48 | 299 |
| Prefill | 86% | 324 | 313 |
| Decode | 93% | 205 | 121 |
| Energy | 88% | 347 | 203 |

**Cannot run:** of 984 scenario-configurations that really cannot run, it flagged 498; it also flagged 223 that do run.

## Every scenario

| ID | Tier | Phone | Use case | Claude's pick | Works? | Measured best | Kept (measured / Claude / both) | Jaccard |
|---|---|---|---|---|---|---|--:|--:|
| [E01](SCENARIOS.md#e01) | Easy | OnePlus 15 | Private chat app | Qwen3.5-2B Q4 MNN CPU | yes | Qwen3.5-2B Q8 MNN CPU | 67 / 63 / 56 | 0.76 |
| [E02](SCENARIOS.md#e02) | Easy | OnePlus 15 | Offline translator app | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 83 / 92 / 80 | 0.84 |
| [E03](SCENARIOS.md#e03) | Easy | OnePlus 15 | Study tutor app | Qwen3.5-4B Q4 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 80 / 88 / 75 | 0.81 |
| [E04](SCENARIOS.md#e04) | Easy | OnePlus 15 | Document Q&A app | Qwen3.5-4B Q4 MNN CPU | **no** (P) | Qwen3.5-4B Q8 MNN GPU | 74 / 83 / 70 | 0.80 |
| [E05](SCENARIOS.md#e05) | Easy | OnePlus 15 | Essay feedback app | Qwen3.5-4B Q4 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 82 / 84 / 76 | 0.84 |
| [E06](SCENARIOS.md#e06) | Easy | OnePlus 15 | Trip planner app | Qwen3.5-4B Q4 MNN CPU | **no** (P) | Qwen3.5-4B Q8 MNN GPU | 75 / 84 / 72 | 0.83 |
| [E07](SCENARIOS.md#e07) | Easy | OnePlus 15 | Story-writing app | Qwen3-4B Q4_0 llama.cpp GPU | **no** (E) | Qwen3.5-4B Q4 MNN GPU | 73 / 67 / 61 | 0.77 |
| [E08](SCENARIOS.md#e08) | Easy | OnePlus 15 | Email drafting app | Qwen3.5-4B Q4 MNN CPU | **no** (P) | Qwen3.5-4B Q8 MNN GPU | 74 / 82 / 70 | 0.81 |
| [E09](SCENARIOS.md#e09) | Easy | OnePlus 15 | Offline medical reference | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 80 / 93 / 77 | 0.80 |
| [E10](SCENARIOS.md#e10) | Easy | OnePlus 15 | Language practice app | Qwen3.5-4B Q4 MNN CPU | **no** (P) | Qwen3.5-4B Q8 MNN GPU | 74 / 85 / 70 | 0.79 |
| [E11](SCENARIOS.md#e11) | Easy | OnePlus 15 | Field-guide app | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 83 / 97 / 82 | 0.84 |
| [E12](SCENARIOS.md#e12) | Easy | OnePlus 15 | Offline Q&A (travel) | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 84 / 97 / 83 | 0.85 |
| [E13](SCENARIOS.md#e13) | Easy | OnePlus 15 | Meeting-notes cleanup | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 82 / 97 / 81 | 0.83 |
| [E14](SCENARIOS.md#e14) | Easy | OnePlus 15 | Document translation | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 84 / 92 / 81 | 0.85 |
| [E15](SCENARIOS.md#e15) | Easy | OnePlus 15 | Voice-memo summary | Qwen3.5-4B Q4 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 83 / 87 / 77 | 0.83 |
| [E16](SCENARIOS.md#e16) | Easy | OnePlus 15 | Morning digest | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 84 / 97 / 83 | 0.85 |
| [E17](SCENARIOS.md#e17) | Easy | OnePlus 15 | Weekly journal summary | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 85 / 98 / 83 | 0.83 |
| [E18](SCENARIOS.md#e18) | Easy | OnePlus 15 | Statement categorization | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 84 / 97 / 83 | 0.85 |
| [E19](SCENARIOS.md#e19) | Easy | OnePlus 15 | Overnight photo captioning | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 87 / 98 / 85 | 0.85 |
| [E20](SCENARIOS.md#e20) | Easy | OnePlus 15 | Overnight inbox sorting | Qwen3.5-4B Q8 MNN CPU | yes | Qwen3.5-4B Q8 MNN CPU | 85 / 95 / 82 | 0.84 |
| [H01](SCENARIOS.md#h01) | Hard | Galaxy S23 | Keyboard next-phrase | Gemma3-270M Q4 MNN CPU | yes | Gemma3-270M Q4 MNN CPU | 4 / 7 / 4 | 0.57 |
| [H02](SCENARIOS.md#h02) | Hard | OnePlus 15 | Smart replies | SmolLM2-360M Q8 MNN CPU | yes | SmolLM2-360M Q8 MNN CPU | 14 / 24 / 13 | 0.52 |
| [H03](SCENARIOS.md#h03) | Hard | OnePlus 8 Pro | Live message translation | Qwen3-0.6B Q4 MNN CPU | **no** (E) | Qwen3.5-0.8B Q4 MNN GPU | 13 / 20 / 9 | 0.38 |
| [H04](SCENARIOS.md#h04) | Hard | Redmi 9A | SMS auto-reply | Gemma3-270M Q8 MNN CPU | yes | Gemma3-270M Q8 MNN CPU | 5 / 8 / 4 | 0.44 |
| [H05](SCENARIOS.md#h05) | Hard | Galaxy S23 | Game NPC dialogue | Qwen3.5-0.8B Q4 MNN CPU | yes | Qwen3.5-0.8B Q4 MNN CPU | 16 / 34 / 16 | 0.47 |
| [H06](SCENARIOS.md#h06) | Hard | OnePlus 15 | Notification triage | SmolLM2-360M Q8 MNN CPU | yes | SmolLM2-360M Q8 MNN CPU | 14 / 24 / 13 | 0.52 |
| [H07](SCENARIOS.md#h07) | Hard | OnePlus 8 Pro | Screen-reader descriptions | Gemma3-270M F16 MNN GPU | **no** (cannot run) | Qwen3.5-0.8B Q4 MNN GPU | 26 / 14 / 12 | 0.43 |
| [H08](SCENARIOS.md#h08) | Hard | Redmi 9A | Recipe / how-to helper | Gemma3-270M Q8 MNN CPU | yes | Gemma3-270M Q8 MNN CPU | 5 / 8 / 4 | 0.44 |
| [H09](SCENARIOS.md#h09) | Hard | Galaxy S23 | Coding helper | Qwen3-0.6B F16 MNN GPU | **no** (cannot run) | Qwen3.5-0.8B Q8 MNN GPU | 21 / 37 / 16 | 0.38 |
| [H10](SCENARIOS.md#h10) | Hard | OnePlus 15 | Keyboard next-phrase | Gemma3-270M Q4 MNN CPU | yes | Gemma3-270M Q4 MNN CPU | 5 / 7 / 5 | 0.71 |
| [H11](SCENARIOS.md#h11) | Hard | OnePlus 8 Pro | Smart replies | Gemma3-270M Q8 MNN CPU | yes | SmolLM2-360M Q8 MNN CPU | 11 / 11 / 5 | 0.29 |
| [H12](SCENARIOS.md#h12) | Hard | Redmi 9A | Field-guide app | Gemma3-270M Q8 MNN CPU | **no** (P) | SmolLM2-135M F16 MNN CPU | 1 / 4 / 0 | 0.00 |
| [H13](SCENARIOS.md#h13) | Hard | Galaxy S23 | Live message translation | Qwen3.5-0.8B Q4 MNN CPU | **no** (E) | Qwen3.5-0.8B Q4 MNN GPU | 15 / 40 / 15 | 0.38 |
| [H14](SCENARIOS.md#h14) | Hard | OnePlus 15 | Game NPC dialogue | Qwen3.5-0.8B Q4 MNN CPU | yes | Qwen3.5-0.8B Q4 MNN CPU | 24 / 35 / 24 | 0.69 |
| [H15](SCENARIOS.md#h15) | Hard | OnePlus 8 Pro | Notification triage | Gemma3-270M Q8 MNN CPU | yes | Gemma3-270M Q8 MNN CPU | 11 / 8 / 7 | 0.58 |
| [H16](SCENARIOS.md#h16) | Hard | Redmi 9A | Offline Q&A (travel) | SmolLM2-360M Q8 MNN CPU | **no** (P) | Gemma3-270M Q8 MNN CPU | 5 / 11 / 5 | 0.45 |
| [H17](SCENARIOS.md#h17) | Hard | Galaxy S23 | Screen-reader descriptions | Qwen3.5-0.8B Q4 MNN CPU | **no** (E) | Qwen3.5-0.8B Q4 MNN GPU | 25 / 39 / 25 | 0.64 |
| [H18](SCENARIOS.md#h18) | Hard | OnePlus 15 | SMS auto-reply | Qwen3.5-0.8B Q4 MNN CPU | yes | Qwen3.5-0.8B Q4 MNN CPU | 24 / 35 / 24 | 0.69 |
| [H19](SCENARIOS.md#h19) | Hard | OnePlus 8 Pro | Voice assistant | Gemma3-270M F16 MNN GPU | **no** (cannot run) | Qwen3-0.6B Q8_0 llama.cpp CPU | 25 / 13 / 11 | 0.41 |
| [H20](SCENARIOS.md#h20) | Hard | Redmi 9A | Document translation | SmolLM2-360M Q8 MNN CPU | **no** (P) | Gemma3-270M Q8 MNN CPU | 5 / 12 / 5 | 0.42 |
| [M01](SCENARIOS.md#m01) | Medium | Galaxy S23 | Language tutor | Qwen3-1.7B Q4 MNN CPU | **no** (E P) | Qwen3.5-2B Q4 MNN GPU | 37 / 48 / 34 | 0.67 |
| [M02](SCENARIOS.md#m02) | Medium | OnePlus 15 | Voice assistant | Qwen3.5-2B Q4 MNN CPU | **no** (P) | Qwen3.5-2B Q4 MNN GPU | 41 / 52 / 39 | 0.72 |
| [M03](SCENARIOS.md#m03) | Medium | OnePlus 8 Pro | Journaling prompts | Qwen3-1.7B Q4 MNN CPU | yes | Qwen3.5-2B Q4 MNN CPU | 41 / 37 / 34 | 0.77 |
| [M04](SCENARIOS.md#m04) | Medium | Redmi 9A | Async FAQ answers | Qwen3.5-0.8B Q8 MNN CPU | **no** (M) | Qwen3.5-0.8B Q8_0 llama.cpp CPU | 18 / 24 / 18 | 0.75 |
| [M05](SCENARIOS.md#m05) | Medium | Galaxy S23 | Bedtime story | Qwen3.5-2B Q4 MNN CPU | yes | Qwen3.5-2B Q4 MNN CPU | 45 / 64 / 44 | 0.68 |
| [M06](SCENARIOS.md#m06) | Medium | OnePlus 15 | Interactive chat | Qwen3.5-2B Q4 MNN CPU | yes | Qwen3.5-2B Q4 MNN GPU | 48 / 60 / 48 | 0.80 |
| [M07](SCENARIOS.md#m07) | Medium | OnePlus 8 Pro | Field data-entry helper | Qwen3.5-0.8B Q8 MNN CPU | yes | Qwen3.5-0.8B Q8 MNN CPU | 34 / 35 / 30 | 0.77 |
| [M08](SCENARIOS.md#m08) | Medium | Redmi 9A | Overnight photo captioning | Qwen3-0.6B Q8 MNN CPU | yes | Qwen3.5-0.8B Q8 MNN CPU | 17 / 18 / 14 | 0.67 |
| [M09](SCENARIOS.md#m09) | Medium | Galaxy S23 | Health-worker checklist | Qwen3.5-2B Q4 MNN CPU | yes | Qwen3.5-2B Q4 MNN CPU | 45 / 57 / 44 | 0.76 |
| [M10](SCENARIOS.md#m10) | Medium | OnePlus 15 | Email summary on open | Qwen3.5-2B Q4 MNN CPU | **no** (P) | Qwen3.5-2B Q4 MNN GPU | 47 / 56 / 45 | 0.78 |
| [M11](SCENARIOS.md#m11) | Medium | OnePlus 8 Pro | Language tutor | SmolLM2-360M F16 MNN GPU | **no** (cannot run) | Qwen3.5-0.8B Q8 MNN CPU | 35 / 21 / 18 | 0.47 |
| [M12](SCENARIOS.md#m12) | Medium | OnePlus 15 | Recipe / how-to helper | Qwen3.5-2B Q4 MNN CPU | yes | Qwen3.5-2B Q4 MNN GPU | 52 / 64 / 52 | 0.81 |
| [M13](SCENARIOS.md#m13) | Medium | Galaxy S23 | Journaling prompts | Qwen3.5-2B Q4 MNN CPU | yes | Qwen3.5-2B Q4 MNN CPU | 42 / 56 / 41 | 0.72 |
| [M14](SCENARIOS.md#m14) | Medium | OnePlus 8 Pro | Bedtime story | Qwen3.5-2B Q4 MNN CPU | yes | Qwen3.5-2B Q4 MNN CPU | 45 / 45 / 40 | 0.80 |
| [M15](SCENARIOS.md#m15) | Medium | OnePlus 15 | Notes Q&A (RAG) | Qwen3-1.7B Q8 MNN CPU | **no** (M P) | Qwen3.5-2B Q4 MNN GPU | 49 / 58 / 45 | 0.73 |
| [M16](SCENARIOS.md#m16) | Medium | Galaxy S23 | Offline translator app | Qwen3.5-2B Q4 MNN CPU | **no** (E) | Qwen3.5-2B Q4 MNN GPU | 41 / 64 / 40 | 0.62 |
| [M17](SCENARIOS.md#m17) | Medium | OnePlus 8 Pro | Study tutor app | Qwen3-1.7B Q4 MNN CPU | **no** (E) | Qwen3.5-2B Q4 MNN GPU | 40 / 38 / 33 | 0.73 |
| [M18](SCENARIOS.md#m18) | Medium | OnePlus 15 | Fluent premium chat | Qwen3-1.7B Q8 MNN GPU | yes | Qwen3.5-2B Q8 MNN GPU | 45 / 51 / 39 | 0.68 |
| [M19](SCENARIOS.md#m19) | Medium | Galaxy S23 | Document Q&A app | Qwen3.5-2B Q4 MNN CPU | **no** (E) | Qwen3.5-2B Q4 MNN GPU | 40 / 60 / 39 | 0.64 |
| [M20](SCENARIOS.md#m20) | Medium | OnePlus 8 Pro | Essay feedback app | Qwen3.5-0.8B Q8 MNN CPU | yes | Qwen3.5-2B Q4 MNN GPU | 39 / 35 / 32 | 0.76 |

Works? lists the limits the pick really misses (M memory, P prefill, D decode, E energy). Scenario IDs here are the SCENARIOS.md IDs; the prompt used shuffled IDs S01-S60 (`baseline/scenario_ids.json`).
