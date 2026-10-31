# Requirement-filter test: 60 realistic scenarios

Generated 2026-10-27T15:52 by `Benchmark-Harness/paper_table5/scenarios_md.py` from the measured values in [RESULTS.md](RESULTS.md). Every configuration's verdict, with values, is in `Benchmark-Harness/paper_table5/results/scenarios.csv`.

## The test

A scenario is a use case on a target phone. The use case sets a limit on each of the four measured fields; the algorithm then works through the phone's pool (9 models × Q4/Q8/F16 × llama.cpp/MNN × CPU/GPU):

1. **Drop configurations that cannot run on the phone** (too big for its RAM, fails to load, crashes, no GPU build).
2. **Drop every configuration whose measured value misses any limit:** peak memory above the budget, prefill or decode below the minimum, energy per request above the budget. A configuration with no energy reading cannot be shown to meet an energy limit, so it is dropped too (only on the OnePlus 8 Pro, 5 llama.cpp GPU rows).
3. **Pick the most capable survivor:** the largest model (parameters), then the highest precision (F16 > 8-bit > 4-bit); between runtimes and backends for that model and precision, the fastest decode, then the lowest energy.

**Difficulty** uses the REQUIREMENTS.md cut-offs on the share of the phone's *whole* pool that survives, counting the configurations that cannot run on it as filtered out: easy ≥ 60%, medium 30-60%, hard < 30%. The pool is 104 configurations on the flagships and 52 on the CPU-only Redmi 9A. Because configurations that cannot run count against the share, a phone that runs few of them cannot have easy scenarios: the Galaxy S23 runs 82 of 104 and the OnePlus 8 Pro 79, so an easy scenario there would have to keep 77-80% of what runs.

## How the limits are set

The limits come from the use case, not from the measurements; the tier is whatever the measurements then give.

- **Prefill** = prompt tokens ÷ the longest acceptable wait for the first token.
- **Decode**: silent reading is about 5-7 tokens/s, speech about 3.5 tokens/s; chat that feels fluent needs 10-20 tokens/s; background work only needs to finish.
- **Peak memory**: what the app can hold without the system killing it. A keyboard stays resident all day (≤ 300 MB); background services and features inside another app get 0.5-1 GB; a foreground assistant 1.5-3 GB so it works on mainstream phones; an app that is the only thing running (a dedicated offline assistant, an overnight job) a share of the target phone's RAM.
- **Energy per request** = a daily battery budget ÷ requests per day, using each phone's battery (Galaxy S23 3,900 mAh ≈ 15.0 Wh, OnePlus 15 7,300 mAh ≈ 28.1 Wh, OnePlus 8 Pro 4,510 mAh ≈ 17.4 Wh, Redmi 9A 5,000 mAh ≈ 19.3 Wh). A configuration's request energy is prompt tokens × its prefill mJ/token + answer tokens × its decode mJ/token.

| Use case | What it does | Request (prompt / answer tokens) | First token within | Decode ≥ | Peak memory ≤ | Energy budget | Why these limits |
|---|---|--:|--:|--:|--:|---|---|
| Keyboard next-phrase | suggest the next words while typing | 64 / 8 | 0.3 s | 20 tokens/s | 300 MB | 1,000 requests ≤ 3% of battery a day | an input method stays resident all day, so it must be small; suggestions are useless after ~0.3 s |
| Smart replies | three reply chips under a received message | 128 / 24 | 0.5 s | 15 tokens/s | 512 MB | 200 requests ≤ 2% of battery a day | runs inside a messaging app; chips must appear before the user starts typing |
| Live message translation | translate each chat message as it arrives | 64 / 64 | 0.5 s | 10 tokens/s | 1,024 MB | 200 requests ≤ 3% of battery a day | part of a messaging app; the translation should keep up with the conversation |
| Game NPC dialogue | a character answers the player | 200 / 40 | 1 s | 12 tokens/s | 768 MB | 100 requests ≤ 2% of battery a day | the game itself holds most of the memory; a reply within a second keeps the scene moving |
| Notification triage | classify incoming notifications in the background | 128 / 8 | 5 s | 2 tokens/s | 512 MB | 300 requests ≤ 1% of battery a day | background services get killed above a few hundred MB; nobody waits on the result |
| Screen-reader descriptions | describe a screen's content aloud | 400 / 60 | 2 s | 5 tokens/s | 1,024 MB | 100 requests ≤ 3% of battery a day | output is spoken (~3.5 tokens/s), so decode only needs to stay ahead of the voice |
| Voice assistant | spoken answer to a spoken question | 256 / 64 | 1 s | 5 tokens/s | 1,536 MB | 50 requests ≤ 2% of battery a day | a pause over ~1 s feels broken in conversation; decode only needs to beat speech |
| Language tutor | conversational practice with corrections | 300 / 100 | 2 s | 6 tokens/s | 1,536 MB | 40 requests ≤ 3% of battery a day | read along at a learner's pace, slightly faster than reading speed |
| Journaling prompts | a reflective question after a journal entry | 300 / 150 | 5 s | 5 tokens/s | 1,536 MB | 5 requests ≤ 1% of battery a day | a few uses a day; the user is not in a hurry |
| Interactive chat | a general chat assistant | 256 / 256 | 2 s | 10 tokens/s | 2,048 MB | 50 requests ≤ 3% of battery a day | first token within 2 s and text faster than reading speed (~5-7 tokens/s) with margin |
| Email summary on open | summarize an email when it is opened | 1,000 / 100 | 4 s | 8 tokens/s | 2,048 MB | 30 requests ≤ 2% of battery a day | the summary should be ready by the time the user has glanced at the email |
| Recipe / how-to helper | step-by-step instructions | 150 / 300 | 5 s | 5 tokens/s | 2,048 MB | 10 requests ≤ 1% of battery a day | read while cooking; reading speed is enough |
| Bedtime story | a story read aloud by text-to-speech | 100 / 500 | 3 s | 4 tokens/s | 2,048 MB | 3 requests ≤ 1% of battery a day | spoken output; must not drain the battery overnight on the nightstand |
| Notes Q&A (RAG) | answer from retrieved personal notes | 1,500 / 200 | 5 s | 10 tokens/s | 2,560 MB | 20 requests ≤ 2% of battery a day | a long retrieved context must be read within ~5 s |
| Fluent premium chat | a chat assistant that streams fast | 512 / 256 | 1.5 s | 20 tokens/s | 3,072 MB | 50 requests ≤ 3% of battery a day | the flagship-app experience: fast first token and fast streaming |
| Coding helper | explain or fix a code snippet | 1,000 / 150 | 3 s | 15 tokens/s | 3,072 MB | 30 requests ≤ 3% of battery a day | code context is long and answers are scanned quickly |
| Private chat app | a dedicated on-device chat app the user opens | 256 / 256 | 2 s | 10 tokens/s | 40% of RAM | 50 requests ≤ 3% of battery a day | the user opened the AI app itself, so it can hold a large share of RAM; same speed as chat |
| Offline translator app | a dedicated translation app for travel | 100 / 100 | 3 s | 5 tokens/s | 40% of RAM | 50 requests ≤ 3% of battery a day | the app in use; the translation is read, so reading speed is enough |
| Study tutor app | explain homework questions | 300 / 100 | 5 s | 6 tokens/s | 40% of RAM | 40 requests ≤ 3% of battery a day | a dedicated app; students wait a few seconds for an explanation |
| Document Q&A app | ask questions about an opened PDF | 1,000 / 200 | 10 s | 8 tokens/s | 40% of RAM | 20 requests ≤ 3% of battery a day | a dedicated app; reading a long document takes a few seconds anyway |
| Essay feedback app | comments on a pasted essay | 500 / 300 | 10 s | 5 tokens/s | 40% of RAM | 20 requests ≤ 3% of battery a day | a dedicated app; feedback is read at reading speed |
| Trip planner app | plan a day from a list of places | 400 / 400 | 5 s | 5 tokens/s | 40% of RAM | 10 requests ≤ 2% of battery a day | a dedicated app, used a few times a day while travelling |
| Story-writing app | co-write fiction with the user | 300 / 600 | 5 s | 6 tokens/s | 40% of RAM | 20 requests ≤ 3% of battery a day | a dedicated app; long answers read at a relaxed pace |
| Email drafting app | draft an email from bullet points | 300 / 200 | 3 s | 8 tokens/s | 40% of RAM | 20 requests ≤ 2% of battery a day | a dedicated app; the draft is skimmed as it streams |
| Offline medical reference | answer questions from a clinical reference | 300 / 300 | 5 s | 5 tokens/s | 40% of RAM | 10 requests ≤ 2% of battery a day | a dedicated app in clinics with poor connectivity; correctness over speed |
| Language practice app | spoken-style conversation practice | 300 / 150 | 3 s | 6 tokens/s | 40% of RAM | 30 requests ≤ 3% of battery a day | a dedicated app; replies a little faster than a learner reads |
| Field-guide app | explain a plant or bird from a description, offline | 200 / 250 | 5 s | 5 tokens/s | 40% of RAM | 10 requests ≤ 2% of battery a day | a dedicated app used outdoors without signal |
| Offline Q&A (travel) | an assistant with no network | 200 / 200 | 10 s | 4 tokens/s | 40% of RAM | 20 requests ≤ 3% of battery a day | the only app in use; the user accepts a wait but needs to read the answer as it comes |
| Meeting-notes cleanup | turn a transcript into notes after a meeting | 1,500 / 300 | 30 s | 3 tokens/s | 40% of RAM | 3 requests ≤ 2% of battery a day | the user starts it and comes back; it must finish within a few minutes |
| Document translation | translate a page while the user does something else | 500 / 500 | 30 s | 2 tokens/s | 40% of RAM | 10 requests ≤ 3% of battery a day | batch work; finishing in a few minutes is fine |
| Voice-memo summary | title and summarize a recorded voice memo | 800 / 80 | 20 s | 2 tokens/s | 40% of RAM | 10 requests ≤ 1% of battery a day | starts after the recording ends; the user checks back later |
| Morning digest | a scheduled summary of overnight messages | 2,000 / 300 | 120 s | 1 tokens/s | 40% of RAM | 1 request ≤ 1% of battery a day | runs before the user wakes up; only the battery cost matters |
| Weekly journal summary | summarize a week of entries | 3,000 / 400 | 120 s | 1 tokens/s | 50% of RAM | 1 request ≤ 1% of battery a day | rare, scheduled, nothing else running |
| Statement categorization | categorize a month of bank transactions | 2,500 / 500 | 120 s | 1 tokens/s | 40% of RAM | 1 request ≤ 1% of battery a day | a scheduled monthly job; it only has to finish and not cost much battery |
| Overnight photo captioning | caption the photo library while charging | 200 / 40 | 30 s | 1 tokens/s | 50% of RAM | 300 requests ≤ 20% of battery a day | on the charger, but heat and charging time still cap the energy |
| Overnight inbox sorting | label every email while charging | 300 / 10 | 10 s | 1 tokens/s | 50% of RAM | 500 requests ≤ 20% of battery a day | on the charger; many short requests |
| Field data-entry helper | fill forms from notes on a low-cost phone | 128 / 64 | 10 s | 2 tokens/s | 1,024 MB | 30 requests ≤ 3% of battery a day | aimed at budget phones; a slow but steady answer is acceptable |
| SMS auto-reply | draft a reply to a text message | 128 / 32 | 5 s | 3 tokens/s | 768 MB | 50 requests ≤ 2% of battery a day | a short reply; the user can wait a few seconds |
| Async FAQ answers | answer a question, delivered like a text message | 128 / 64 | 30 s | 1 tokens/s | 1,024 MB | 20 requests ≤ 3% of battery a day | the user is not watching; the answer arrives within a minute or two |
| Health-worker checklist | guide an offline checklist on a budget phone | 256 / 128 | 20 s | 2 tokens/s | 1,536 MB | 20 requests ≤ 3% of battery a day | offline, low-end hardware; correctness matters more than speed |

**Caveats.** Values come from the benchmark's 256-token prompt and 256-token answer. For longer prompts (RAG, coding, summaries) real prefill is somewhat slower and the KV cache adds memory, so those scenarios are slightly optimistic. Energy is whole-phone energy net of idle, so it excludes the screen.

## Which scenarios

All 40 use cases were run on all four phones (160 combinations). Tiers that came out:

| Phone | Easy | Medium | Hard | Hard with no survivor |
|---|--:|--:|--:|--:|
| Galaxy S23 | 0 | 24 | 16 | 0 |
| OnePlus 15 | 20 | 13 | 7 | 0 |
| OnePlus 8 Pro | 0 | 24 | 16 | 0 |
| Redmi 9A | 0 | 2 | 38 | 24 |

From these, 20 per tier were chosen, spreading them across phones and use cases, and only combinations where at least one configuration survives (a scenario with no survivor has no answer to check). **All 20 easy scenarios are on the OnePlus 15.** It is the only phone where a realistic use case keeps 60% of the whole pool: it runs 93 of 104 configurations and has 16 GB of RAM. The most any use case keeps on the other phones: Galaxy S23 60 of 104, OnePlus 8 Pro 62 of 104, Redmi 9A 18 of 52.

## Results

| ID | Phone | Use case | Limits (memory / prefill / decode / energy) | Kept | Pick (model, precision, runtime, backend) | Pick's values (memory, prefill / decode, energy) |
|---|---|---|---|--:|---|---|
| [E01](#e01) | OnePlus 15 | Private chat app | ≤ 6,000 MB / ≥ 128 / ≥ 10 tokens/s / ≤ 60.7 J | 67 of 104 (64%) | **Qwen3.5-2B Q8** MNN CPU | 2,206 MB, 169 / 31.4 tokens/s, 41 J |
| [E02](#e02) | OnePlus 15 | Offline translator app | ≤ 6,000 MB / ≥ 33.3 / ≥ 5 tokens/s / ≤ 60.7 J | 83 of 104 (80%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 39.7 J |
| [E03](#e03) | OnePlus 15 | Study tutor app | ≤ 6,000 MB / ≥ 60 / ≥ 6 tokens/s / ≤ 75.9 J | 80 of 104 (77%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 53.4 J |
| [E04](#e04) | OnePlus 15 | Document Q&A app | ≤ 6,000 MB / ≥ 100 / ≥ 8 tokens/s / ≤ 152 J | 74 of 104 (71%) | **Qwen3.5-4B Q8** MNN GPU | 5,586 MB, 204 / 13.4 tokens/s, 108 J |
| [E05](#e05) | OnePlus 15 | Essay feedback app | ≤ 6,000 MB / ≥ 50 / ≥ 5 tokens/s / ≤ 152 J | 82 of 104 (79%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 133 J |
| [E06](#e06) | OnePlus 15 | Trip planner app | ≤ 6,000 MB / ≥ 80 / ≥ 5 tokens/s / ≤ 202 J | 75 of 104 (72%) | **Qwen3.5-4B Q8** MNN GPU | 5,586 MB, 204 / 13.4 tokens/s, 179 J |
| [E07](#e07) | OnePlus 15 | Story-writing app | ≤ 6,000 MB / ≥ 60 / ≥ 6 tokens/s / ≤ 152 J | 73 of 104 (70%) | **Qwen3.5-4B Q4** MNN GPU | 3,040 MB, 275 / 21.7 tokens/s, 143 J |
| [E08](#e08) | OnePlus 15 | Email drafting app | ≤ 6,000 MB / ≥ 100 / ≥ 8 tokens/s / ≤ 101 J | 74 of 104 (71%) | **Qwen3.5-4B Q8** MNN GPU | 5,586 MB, 204 / 13.4 tokens/s, 91.6 J |
| [E09](#e09) | OnePlus 15 | Offline medical reference | ≤ 6,000 MB / ≥ 60 / ≥ 5 tokens/s / ≤ 202 J | 80 of 104 (77%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 119 J |
| [E10](#e10) | OnePlus 15 | Language practice app | ≤ 6,000 MB / ≥ 100 / ≥ 6 tokens/s / ≤ 101 J | 74 of 104 (71%) | **Qwen3.5-4B Q8** MNN GPU | 5,586 MB, 204 / 13.4 tokens/s, 70.4 J |
| [E11](#e11) | OnePlus 15 | Field-guide app | ≤ 6,000 MB / ≥ 40 / ≥ 5 tokens/s / ≤ 202 J | 83 of 104 (80%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 95.8 J |
| [E12](#e12) | OnePlus 15 | Offline Q&A (travel) | ≤ 6,000 MB / ≥ 20 / ≥ 4 tokens/s / ≤ 152 J | 84 of 104 (81%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 79.4 J |
| [E13](#e13) | OnePlus 15 | Meeting-notes cleanup | ≤ 6,000 MB / ≥ 50 / ≥ 3 tokens/s / ≤ 674 J | 82 of 104 (79%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 201 J |
| [E14](#e14) | OnePlus 15 | Document translation | ≤ 6,000 MB / ≥ 16.7 / ≥ 2 tokens/s / ≤ 303 J | 84 of 104 (81%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 198 J |
| [E15](#e15) | OnePlus 15 | Voice-memo summary | ≤ 6,000 MB / ≥ 40 / ≥ 2 tokens/s / ≤ 101 J | 83 of 104 (80%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 81.2 J |
| [E16](#e16) | OnePlus 15 | Morning digest | ≤ 6,000 MB / ≥ 16.7 / ≥ 1 tokens/s / ≤ 1,012 J | 84 of 104 (81%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 236 J |
| [E17](#e17) | OnePlus 15 | Weekly journal summary | ≤ 7,600 MB / ≥ 25 / ≥ 1 tokens/s / ≤ 1,012 J | 85 of 104 (82%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 337 J |
| [E18](#e18) | OnePlus 15 | Statement categorization | ≤ 6,000 MB / ≥ 20.8 / ≥ 1 tokens/s / ≤ 1,012 J | 84 of 104 (81%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 336 J |
| [E19](#e19) | OnePlus 15 | Overnight photo captioning | ≤ 7,600 MB / ≥ 6.67 / ≥ 1 tokens/s / ≤ 67.4 J | 87 of 104 (84%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 26.9 J |
| [E20](#e20) | OnePlus 15 | Overnight inbox sorting | ≤ 7,600 MB / ≥ 30 / ≥ 1 tokens/s / ≤ 40.5 J | 85 of 104 (82%) | **Qwen3.5-4B Q8** MNN CPU | 5,355 MB, 67.6 / 14.2 tokens/s, 23.9 J |
| [M01](#m01) | Galaxy S23 | Language tutor | ≤ 1,536 MB / ≥ 150 / ≥ 6 tokens/s / ≤ 40.5 J | 37 of 104 (36%) | **Qwen3.5-2B Q4** MNN GPU | 1,458 MB, 252 / 28 tokens/s, 27.3 J |
| [M02](#m02) | OnePlus 15 | Voice assistant | ≤ 1,536 MB / ≥ 256 / ≥ 5 tokens/s / ≤ 40.5 J | 41 of 104 (39%) | **Qwen3.5-2B Q4** MNN GPU | 1,470 MB, 669 / 43.2 tokens/s, 7.8 J |
| [M03](#m03) | OnePlus 8 Pro | Journaling prompts | ≤ 1,536 MB / ≥ 60 / ≥ 5 tokens/s / ≤ 125 J | 41 of 104 (39%) | **Qwen3.5-2B Q4** MNN CPU | 1,218 MB, 77.9 / 20.7 tokens/s, 67.1 J |
| [M04](#m04) | Redmi 9A | Async FAQ answers | ≤ 1,024 MB / ≥ 4.27 / ≥ 1 tokens/s / ≤ 104 J | 18 of 52 (35%) | **Qwen3.5-0.8B Q8_0** llama.cpp CPU | 910 MB, 4.31 / 2.2 tokens/s, 82.5 J |
| [M05](#m05) | Galaxy S23 | Bedtime story | ≤ 2,048 MB / ≥ 33.3 / ≥ 4 tokens/s / ≤ 180 J | 45 of 104 (43%) | **Qwen3.5-2B Q4** MNN CPU | 1,236 MB, 134 / 34.1 tokens/s, 152 J |
| [M06](#m06) | OnePlus 15 | Interactive chat | ≤ 2,048 MB / ≥ 128 / ≥ 10 tokens/s / ≤ 60.7 J | 48 of 104 (46%) | **Qwen3.5-2B Q4** MNN GPU | 1,470 MB, 669 / 43.2 tokens/s, 27.1 J |
| [M07](#m07) | OnePlus 8 Pro | Field data-entry helper | ≤ 1,024 MB / ≥ 12.8 / ≥ 2 tokens/s / ≤ 62.6 J | 34 of 104 (33%) | **Qwen3.5-0.8B Q8** MNN CPU | 926 MB, 182 / 26 tokens/s, 18.9 J |
| [M08](#m08) | Redmi 9A | Overnight photo captioning | ≤ 1,900 MB / ≥ 6.67 / ≥ 1 tokens/s / ≤ 46.3 J | 17 of 52 (33%) | **Qwen3.5-0.8B Q8** MNN CPU | 1,241 MB, 7.75 / 4.65 tokens/s, 41 J |
| [M09](#m09) | Galaxy S23 | Health-worker checklist | ≤ 1,536 MB / ≥ 12.8 / ≥ 2 tokens/s / ≤ 81 J | 45 of 104 (43%) | **Qwen3.5-2B Q4** MNN CPU | 1,236 MB, 134 / 34.1 tokens/s, 49.8 J |
| [M10](#m10) | OnePlus 15 | Email summary on open | ≤ 2,048 MB / ≥ 250 / ≥ 8 tokens/s / ≤ 67.4 J | 47 of 104 (45%) | **Qwen3.5-2B Q4** MNN GPU | 1,470 MB, 669 / 43.2 tokens/s, 15.4 J |
| [M11](#m11) | OnePlus 8 Pro | Language tutor | ≤ 1,536 MB / ≥ 150 / ≥ 6 tokens/s / ≤ 47 J | 35 of 104 (34%) | **Qwen3.5-0.8B Q8** MNN CPU | 926 MB, 182 / 26 tokens/s, 32.5 J |
| [M12](#m12) | OnePlus 15 | Recipe / how-to helper | ≤ 2,048 MB / ≥ 30 / ≥ 5 tokens/s / ≤ 101 J | 52 of 104 (50%) | **Qwen3.5-2B Q4** MNN GPU | 1,470 MB, 669 / 43.2 tokens/s, 31 J |
| [M13](#m13) | Galaxy S23 | Journaling prompts | ≤ 1,536 MB / ≥ 60 / ≥ 5 tokens/s / ≤ 108 J | 42 of 104 (40%) | **Qwen3.5-2B Q4** MNN CPU | 1,236 MB, 134 / 34.1 tokens/s, 58.4 J |
| [M14](#m14) | OnePlus 8 Pro | Bedtime story | ≤ 2,048 MB / ≥ 33.3 / ≥ 4 tokens/s / ≤ 209 J | 45 of 104 (43%) | **Qwen3.5-2B Q4** MNN CPU | 1,218 MB, 77.9 / 20.7 tokens/s, 168 J |
| [M15](#m15) | OnePlus 15 | Notes Q&A (RAG) | ≤ 2,560 MB / ≥ 300 / ≥ 10 tokens/s / ≤ 101 J | 49 of 104 (47%) | **Qwen3.5-2B Q4** MNN GPU | 1,470 MB, 669 / 43.2 tokens/s, 28.1 J |
| [M16](#m16) | Galaxy S23 | Offline translator app | ≤ 2,800 MB / ≥ 33.3 / ≥ 5 tokens/s / ≤ 32.4 J | 41 of 104 (39%) | **Qwen3.5-2B Q4** MNN GPU | 1,458 MB, 252 / 28 tokens/s, 24.2 J |
| [M17](#m17) | OnePlus 8 Pro | Study tutor app | ≤ 4,600 MB / ≥ 60 / ≥ 6 tokens/s / ≤ 47 J | 40 of 104 (38%) | **Qwen3.5-2B Q4** MNN GPU | 1,259 MB, 102 / 14.2 tokens/s, 33.6 J |
| [M18](#m18) | OnePlus 15 | Fluent premium chat | ≤ 3,072 MB / ≥ 341 / ≥ 20 tokens/s / ≤ 60.7 J | 45 of 104 (43%) | **Qwen3.5-2B Q8** MNN GPU | 2,810 MB, 458 / 26 tokens/s, 51.4 J |
| [M19](#m19) | Galaxy S23 | Document Q&A app | ≤ 2,800 MB / ≥ 100 / ≥ 8 tokens/s / ≤ 81 J | 40 of 104 (38%) | **Qwen3.5-2B Q4** MNN GPU | 1,458 MB, 252 / 28 tokens/s, 60.7 J |
| [M20](#m20) | OnePlus 8 Pro | Essay feedback app | ≤ 4,600 MB / ≥ 50 / ≥ 5 tokens/s / ≤ 94 J | 39 of 104 (38%) | **Qwen3.5-2B Q4** MNN GPU | 1,259 MB, 102 / 14.2 tokens/s, 92.1 J |
| [H01](#h01) | Galaxy S23 | Keyboard next-phrase | ≤ 300 MB / ≥ 213 / ≥ 20 tokens/s / ≤ 1.62 J | 4 of 104 (4%) | **Gemma3-270M Q4** MNN CPU | 273 MB, 1,511 / 108 tokens/s, 0.942 J |
| [H02](#h02) | OnePlus 15 | Smart replies | ≤ 512 MB / ≥ 256 / ≥ 15 tokens/s / ≤ 10.1 J | 14 of 104 (13%) | **SmolLM2-360M Q8** MNN CPU | 509 MB, 524 / 99.6 tokens/s, 1.23 J |
| [H03](#h03) | OnePlus 8 Pro | Live message translation | ≤ 1,024 MB / ≥ 128 / ≥ 10 tokens/s / ≤ 9.4 J | 13 of 104 (12%) | **Qwen3.5-0.8B Q4** MNN GPU | 583 MB, 207 / 24.1 tokens/s, 8.23 J |
| [H04](#h04) | Redmi 9A | SMS auto-reply | ≤ 768 MB / ≥ 25.6 / ≥ 3 tokens/s / ≤ 27.8 J | 5 of 52 (10%) | **Gemma3-270M Q8** MNN CPU | 745 MB, 35.2 / 6.71 tokens/s, 8.65 J |
| [H05](#h05) | Galaxy S23 | Game NPC dialogue | ≤ 768 MB / ≥ 200 / ≥ 12 tokens/s / ≤ 10.8 J | 16 of 104 (15%) | **Qwen3.5-0.8B Q4** MNN CPU | 691 MB, 273 / 73.6 tokens/s, 9.4 J |
| [H06](#h06) | OnePlus 15 | Notification triage | ≤ 512 MB / ≥ 25.6 / ≥ 2 tokens/s / ≤ 3.37 J | 14 of 104 (13%) | **SmolLM2-360M Q8** MNN CPU | 509 MB, 524 / 99.6 tokens/s, 0.746 J |
| [H07](#h07) | OnePlus 8 Pro | Screen-reader descriptions | ≤ 1,024 MB / ≥ 200 / ≥ 5 tokens/s / ≤ 18.8 J | 26 of 104 (25%) | **Qwen3.5-0.8B Q4** MNN GPU | 583 MB, 207 / 24.1 tokens/s, 11.1 J |
| [H08](#h08) | Redmi 9A | Recipe / how-to helper | ≤ 2,048 MB / ≥ 30 / ≥ 5 tokens/s / ≤ 69.5 J | 5 of 52 (10%) | **Gemma3-270M Q8** MNN CPU | 745 MB, 35.2 / 6.71 tokens/s, 51.8 J |
| [H09](#h09) | Galaxy S23 | Coding helper | ≤ 3,072 MB / ≥ 333 / ≥ 15 tokens/s / ≤ 54 J | 21 of 104 (20%) | **Qwen3.5-0.8B Q8** MNN GPU | 1,083 MB, 379 / 35.1 tokens/s, 37.5 J |
| [H10](#h10) | OnePlus 15 | Keyboard next-phrase | ≤ 300 MB / ≥ 213 / ≥ 20 tokens/s / ≤ 3.03 J | 5 of 104 (5%) | **Gemma3-270M Q4** MNN CPU | 285 MB, 1,278 / 116 tokens/s, 0.246 J |
| [H11](#h11) | OnePlus 8 Pro | Smart replies | ≤ 512 MB / ≥ 256 / ≥ 15 tokens/s / ≤ 6.26 J | 11 of 104 (11%) | **SmolLM2-360M Q8** MNN CPU | 492 MB, 342 / 44.6 tokens/s, 5.49 J |
| [H12](#h12) | Redmi 9A | Field-guide app | ≤ 1,500 MB / ≥ 40 / ≥ 5 tokens/s / ≤ 139 J | 1 of 52 (2%) | **SmolLM2-135M F16** MNN CPU | 637 MB, 43 / 8.17 tokens/s, 42.3 J |
| [H13](#h13) | Galaxy S23 | Live message translation | ≤ 1,024 MB / ≥ 128 / ≥ 10 tokens/s / ≤ 8.1 J | 15 of 104 (14%) | **Qwen3.5-0.8B Q4** MNN GPU | 712 MB, 454 / 46.1 tokens/s, 7.46 J |
| [H14](#h14) | OnePlus 15 | Game NPC dialogue | ≤ 768 MB / ≥ 200 / ≥ 12 tokens/s / ≤ 20.2 J | 24 of 104 (23%) | **Qwen3.5-0.8B Q4** MNN CPU | 568 MB, 337 / 80 tokens/s, 2.41 J |
| [H15](#h15) | OnePlus 8 Pro | Notification triage | ≤ 512 MB / ≥ 25.6 / ≥ 2 tokens/s / ≤ 2.09 J | 11 of 104 (11%) | **Gemma3-270M Q8** MNN CPU | 395 MB, 926 / 46.2 tokens/s, 1.82 J |
| [H16](#h16) | Redmi 9A | Offline Q&A (travel) | ≤ 1,500 MB / ≥ 20 / ≥ 4 tokens/s / ≤ 104 J | 5 of 52 (10%) | **Gemma3-270M Q8** MNN CPU | 745 MB, 35.2 / 6.71 tokens/s, 37.3 J |
| [H17](#h17) | Galaxy S23 | Screen-reader descriptions | ≤ 1,024 MB / ≥ 200 / ≥ 5 tokens/s / ≤ 16.2 J | 25 of 104 (24%) | **Qwen3.5-0.8B Q4** MNN GPU | 712 MB, 454 / 46.1 tokens/s, 9.54 J |
| [H18](#h18) | OnePlus 15 | SMS auto-reply | ≤ 768 MB / ≥ 25.6 / ≥ 3 tokens/s / ≤ 40.5 J | 24 of 104 (23%) | **Qwen3.5-0.8B Q4** MNN CPU | 568 MB, 337 / 80 tokens/s, 1.77 J |
| [H19](#h19) | OnePlus 8 Pro | Voice assistant | ≤ 1,536 MB / ≥ 256 / ≥ 5 tokens/s / ≤ 25.1 J | 25 of 104 (24%) | **Qwen3-0.6B Q8_0** llama.cpp CPU | 1,440 MB, 259 / 30.3 tokens/s, 19.9 J |
| [H20](#h20) | Redmi 9A | Document translation | ≤ 1,500 MB / ≥ 16.7 / ≥ 2 tokens/s / ≤ 208 J | 5 of 52 (10%) | **Gemma3-270M Q8** MNN CPU | 745 MB, 35.2 / 6.71 tokens/s, 93.3 J |

### What the picks show

- **Runtime and backend of the pick:** MNN CPU 37, MNN GPU 21, llama.cpp CPU 2 of 60.
- **Easy picks:** Qwen3.5-4B ×19, Qwen3.5-2B ×1
- **Medium picks:** Qwen3.5-2B ×16, Qwen3.5-0.8B ×4
- **Hard picks:** Qwen3.5-0.8B ×8, Gemma3-270M ×7, SmolLM2-360M ×3, SmolLM2-135M ×1, Qwen3-0.6B ×1
- **Why configurations were dropped** (a configuration can miss several limits): memory 1,622, prefill 1,232, energy 1,197, decode 418, no energy reading 66.

- **Same use case, different phone:** 20 use cases appear on more than one phone; the pick differs between phones for 13 of them (Offline translator app: OnePlus 15 Qwen3.5-4B Q8 MNN CPU, Galaxy S23 Qwen3.5-2B Q4 MNN GPU; Study tutor app: OnePlus 15 Qwen3.5-4B Q8 MNN CPU, OnePlus 8 Pro Qwen3.5-2B Q4 MNN GPU; Document Q&A app: OnePlus 15 Qwen3.5-4B Q8 MNN GPU, Galaxy S23 Qwen3.5-2B Q4 MNN GPU; Essay feedback app: OnePlus 15 Qwen3.5-4B Q8 MNN CPU, OnePlus 8 Pro Qwen3.5-2B Q4 MNN GPU).

## Scenarios

### E01

**Easy · OnePlus 15 · Private chat app**: a dedicated on-device chat app the user opens; 256-token prompt, 256-token answer, first token within 2 s, 50 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 128 tokens/s · decode ≥ 10 tokens/s · energy ≤ 60.7 J per request.

**Pick: Qwen3.5-2B Q8, MNN, CPU** (2,206 MB, 169 / 31.4 tokens/s, 41 J). Kept 67 of 104 configurations (64%); 26 filtered out; 11 cannot run on this phone.

![E01](figures/scenarios/E01.png)

<details><summary>Kept pool (67), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q8 MNN CPU **(pick)** | 2,206 | 169 | 31.4 | 41 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 43.5 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 50.6 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 49.2 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 27.1 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 36.2 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 28.4 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 38.2 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 40.4 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 43.4 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 52.7 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 51.5 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 34.5 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 40.1 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 34.9 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 35.1 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 29.4 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 14.6 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 19.3 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 23.6 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 20.2 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 10.4 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 11.2 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 16.2 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 17.2 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 28.9 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 27 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 16.1 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 14.8 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 19.9 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 24 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 10.8 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 13.7 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 18.5 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 16.9 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 19.3 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 16.8 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 8.72 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 9.62 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 14.3 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 16.9 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 7.2 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 6.68 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 11.8 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 14.5 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 13.6 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 11.5 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 14.1 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 5.78 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 7.37 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 10.1 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 13.4 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 4.48 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 5.38 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 9.43 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 11.6 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 5 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 9.08 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 8.35 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 3.65 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 4.56 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 9.13 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 9.84 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 3.4 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 2.84 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 8.47 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 9.32 |

</details>

<details><summary>Filtered out (26) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 128 tokens/s (17):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3-1.7B Q4 MNN CPU (103), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3.5-2B F16 MNN CPU (113), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6)
- **Decode under 10 tokens/s (2):** Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42)
- **Energy over 60.7 J per request (21):** Qwen3-1.7B F16 llama.cpp CPU (119 J), Qwen3-1.7B F16 MNN CPU (73.2 J), Qwen3-1.7B F16 llama.cpp GPU (76.6 J), Qwen3.5-2B F16 llama.cpp CPU (117 J), Qwen3.5-2B F16 MNN CPU (77.1 J), Qwen3.5-2B F16 llama.cpp GPU (79.4 J), Qwen3-4B Q4_0 llama.cpp CPU (80 J), Qwen3-4B Q4 MNN CPU (67.2 J), Qwen3-4B Q4_0 llama.cpp GPU (84 J), Qwen3-4B Q4 MNN GPU (79.7 J), Qwen3-4B Q8_0 llama.cpp CPU (97.3 J), Qwen3-4B Q8_0 llama.cpp GPU (121 J), Qwen3-4B Q8 MNN GPU (124 J), Qwen3-4B F16 llama.cpp CPU (280 J), Qwen3-4B F16 llama.cpp GPU (181 J), Qwen3.5-4B Q4_0 llama.cpp CPU (85.6 J), Qwen3.5-4B Q4_0 llama.cpp GPU (78.3 J), Qwen3.5-4B Q4 MNN GPU (63 J), Qwen3.5-4B Q8 MNN CPU (102 J), Qwen3.5-4B Q8_0 llama.cpp GPU (111 J), Qwen3.5-4B Q8 MNN GPU (114 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E02

**Easy · OnePlus 15 · Offline translator app**: a dedicated translation app for travel; 100-token prompt, 100-token answer, first token within 3 s, 50 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 33.3 tokens/s · decode ≥ 5 tokens/s · energy ≤ 60.7 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 39.7 J). Kept 83 of 104 configurations (80%); 10 filtered out; 11 cannot run on this phone.

![E02](figures/scenarios/E02.png)

<details><summary>Kept pool (83), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 39.7 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 44.6 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 24.6 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 22.7 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 33.5 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 30.6 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 48.3 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 31.1 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 26.3 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 32.8 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 31.2 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 16 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 17 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 19.8 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 19.2 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 10.6 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 14.1 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 11.1 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 14.9 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 28.6 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 15.8 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 16.9 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 20.6 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 20.1 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 10.5 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 13.5 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 15.6 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 13.6 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 13.7 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 11.5 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 18.9 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 5.72 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 7.53 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 9.23 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 7.88 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 4.07 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 4.36 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 6.31 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 6.71 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 11.3 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 16.2 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 10.5 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 6.29 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 5.79 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 7.77 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 9.36 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 4.23 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 5.35 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 7.21 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 6.61 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 7.53 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 6.55 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 9.66 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 3.41 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 3.76 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 5.6 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 6.61 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 2.81 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 2.61 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 4.62 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 5.65 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 5.31 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 4.5 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 5.5 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 2.26 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 2.88 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 3.96 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 5.24 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 1.75 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 2.1 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 3.69 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 4.53 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 1.95 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 3.55 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 3.26 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 1.43 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 1.78 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 3.57 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 3.84 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 1.33 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 1.11 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 3.31 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 3.64 |

</details>

<details><summary>Filtered out (10) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 33.3 tokens/s (3):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 60.7 J per request (2):** Qwen3-4B F16 llama.cpp CPU (109 J), Qwen3-4B F16 llama.cpp GPU (70.8 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E03

**Easy · OnePlus 15 · Study tutor app**: explain homework questions; 300-token prompt, 100-token answer, first token within 5 s, 40 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 60 tokens/s · decode ≥ 6 tokens/s · energy ≤ 75.9 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 53.4 J). Kept 80 of 104 configurations (77%); 13 filtered out; 11 cannot run on this phone.

![E03](figures/scenarios/E03.png)

<details><summary>Kept pool (80), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 53.4 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 49.3 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 27.6 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 31.3 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 34.9 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 55 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 37.4 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 36.5 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 19.5 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 20 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 21.5 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 20.9 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 11.7 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 15.9 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 14 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 17.6 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 35.9 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 19.1 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 21.4 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 23.9 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 22.3 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 14.4 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 16.1 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 17.3 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 15.6 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 14.5 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 13.4 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 27.1 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 6.85 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 8.95 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 9.82 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 8.78 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 5.05 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 4.96 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 7.01 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 8.06 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 12.2 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 23.9 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 12.6 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 7.69 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 7.03 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 8.7 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 10.1 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 5.36 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 6.6 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 7.67 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 7.23 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 8.12 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 7.99 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 14.7 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 4.2 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 4.61 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 6.22 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 7.09 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 3.6 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 3.29 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 5.11 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 6.16 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 6.93 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 4.98 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 5.8 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 2.65 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 3.22 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 4.28 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 5.41 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 2.11 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 2.38 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 3.95 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 4.71 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 2.35 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 5.3 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 3.46 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 1.7 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 2.1 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 3.85 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 4.02 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 1.59 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 1.35 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 3.53 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 3.82 |

</details>

<details><summary>Filtered out (13) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 60 tokens/s (7):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8)
- **Energy over 75.9 J per request (2):** Qwen3-4B F16 llama.cpp CPU (170 J), Qwen3-4B F16 llama.cpp GPU (79.3 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E04

**Easy · OnePlus 15 · Document Q&A app**: ask questions about an opened PDF; 1,000-token prompt, 200-token answer, first token within 10 s, 20 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 100 tokens/s · decode ≥ 8 tokens/s · energy ≤ 152 J per request.

**Pick: Qwen3.5-4B Q8, MNN, GPU** (5,586 MB, 204 / 13.4 tokens/s, 108 J). Kept 74 of 104 configurations (71%); 19 filtered out; 11 cannot run on this phone.

![E04](figures/scenarios/E04.png)

<details><summary>Kept pool (74), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN GPU **(pick)** | 5,586 | 204 | 13.4 | 108 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 61.1 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 78.3 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 123 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 87.5 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 80.6 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 46.1 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 46.1 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 46.3 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 45.4 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 25.4 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 35.3 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 33.9 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 40.6 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 44.9 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 51.6 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 54.5 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 49.1 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 36.6 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 37.6 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 37.8 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 35 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 30.4 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 30.8 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 16 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 20.8 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 20.8 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 19.4 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 12.1 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 11.1 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 15.4 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 18.8 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 26.2 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 29.2 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 18.2 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 16.6 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 19.3 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 21.5 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 13 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 15.7 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 16.2 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 15.7 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 17.4 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 18.8 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 9.97 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 10.9 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 13.7 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 15.2 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 8.79 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 7.95 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 11.2 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 13.3 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 17.1 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 10.9 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 12.2 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 6.06 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 7.12 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 9.17 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 11.2 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 4.93 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 5.32 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 8.42 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 9.78 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 5.48 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 14.1 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 7.33 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 3.95 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 4.84 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 8.25 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 8.41 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 3.7 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 3.18 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 7.5 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 8 |

</details>

<details><summary>Filtered out (19) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 100 tokens/s (15):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6)
- **Decode under 8 tokens/s (2):** Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42)
- **Energy over 152 J per request (4):** Qwen3-1.7B F16 llama.cpp CPU (196 J), Qwen3.5-2B F16 llama.cpp CPU (177 J), Qwen3-4B F16 llama.cpp CPU (461 J), Qwen3-4B F16 llama.cpp GPU (176 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E05

**Easy · OnePlus 15 · Essay feedback app**: comments on a pasted essay; 500-token prompt, 300-token answer, first token within 10 s, 20 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 50 tokens/s · decode ≥ 5 tokens/s · energy ≤ 152 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 133 J). Kept 82 of 104 configurations (79%); 11 filtered out; 11 cannot run on this phone.

![E05](figures/scenarios/E05.png)

<details><summary>Kept pool (82), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 133 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 139 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 76.8 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 76.8 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 108 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 96.1 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 152 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 99.7 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 102 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 102 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 51.6 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 54 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 61 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 59.4 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 32.8 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 44.2 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 36.2 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 47.5 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 93.1 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 50.7 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 55.3 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 65.1 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 62.5 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 35.4 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 43.1 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 48.6 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 42.8 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 41.9 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 36.4 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 65 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 18.3 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 24 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 28.3 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 24.5 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 13.2 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 13.7 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 19.6 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 21.5 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 34.8 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 56.2 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 33.6 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 20.3 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 18.6 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 24.2 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 28.8 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 13.8 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 17.3 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 22.1 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 20.4 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 23.2 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 21.1 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 34 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 11 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 12.1 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 17.4 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 20.3 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 9.23 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 8.51 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 14.3 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 17.5 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 17.5 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 14 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 16.8 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 7.16 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 8.98 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 12.2 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 15.9 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 5.61 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 6.58 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 11.3 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 13.8 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 6.25 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 12.4 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 9.99 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 4.56 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 5.66 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 11 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 11.7 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 4.25 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 3.57 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 10.2 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 11.1 |

</details>

<details><summary>Filtered out (11) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 50 tokens/s (4):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 152 J per request (4):** Qwen3-1.7B F16 llama.cpp CPU (165 J), Qwen3.5-2B F16 llama.cpp CPU (159 J), Qwen3-4B F16 llama.cpp CPU (388 J), Qwen3-4B F16 llama.cpp GPU (221 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E06

**Easy · OnePlus 15 · Trip planner app**: plan a day from a list of places; 400-token prompt, 400-token answer, first token within 5 s, 10 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 80 tokens/s · decode ≥ 5 tokens/s · energy ≤ 202 J per request.

**Pick: Qwen3.5-4B Q8, MNN, GPU** (5,586 MB, 204 / 13.4 tokens/s, 179 J). Kept 75 of 104 configurations (72%); 18 filtered out; 11 cannot run on this phone.

![E06](figures/scenarios/E06.png)

<details><summary>Kept pool (75), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN GPU **(pick)** | 5,586 | 204 | 13.4 | 179 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 98.4 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 122 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 193 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 124 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 131 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 64 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 67.9 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 79.1 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 76.9 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 42.4 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 56.5 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 44.3 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 59.7 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 63.1 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 67.8 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 82.4 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 80.4 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 42 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 53.9 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 62.6 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 54.5 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 54.9 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 45.9 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 22.9 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 30.1 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 36.9 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 31.5 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 16.3 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 17.4 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 25.3 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 26.8 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 45.2 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 42.1 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 25.2 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 23.2 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 31.1 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 37.5 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 16.9 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 21.4 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 28.8 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 26.4 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 30.1 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 26.2 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 38.6 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 13.6 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 15 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 22.4 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 26.4 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 11.2 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 10.4 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 18.5 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 22.6 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 21.2 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 18 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 22 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 9.03 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 11.5 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 15.9 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 21 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 7.01 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 8.4 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 14.7 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 18.1 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 7.81 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 14.2 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 13.1 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 5.71 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 7.12 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 14.3 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 15.4 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 5.32 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 4.44 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 13.2 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 14.6 |

</details>

<details><summary>Filtered out (18) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 80 tokens/s (13):** Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6)
- **Energy over 202 J per request (2):** Qwen3-4B F16 llama.cpp CPU (437 J), Qwen3-4B F16 llama.cpp GPU (283 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E07

**Easy · OnePlus 15 · Story-writing app**: co-write fiction with the user; 300-token prompt, 600-token answer, first token within 5 s, 20 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 60 tokens/s · decode ≥ 6 tokens/s · energy ≤ 152 J per request.

**Pick: Qwen3.5-4B Q4, MNN, GPU** (3,040 MB, 275 / 21.7 tokens/s, 143 J). Kept 73 of 104 configurations (70%); 20 filtered out; 11 cannot run on this phone.

![E07](figures/scenarios/E07.png)

<details><summary>Kept pool (73), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q4 MNN GPU **(pick)** | 3,040 | 275 | 21.7 | 143 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 124 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 90.8 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 97.4 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 116 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 113 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 62 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 82.2 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 62.1 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 85.5 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 89.6 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 95 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 119 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 117 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 57.2 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 76.8 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 91.4 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 78.9 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 81.3 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 65.8 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 101 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 32.6 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 43 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 54.5 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 45.9 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 22.9 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 25.3 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 36.8 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 38.2 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 66.4 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 85.4 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 60.2 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 35.6 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 32.9 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 45.2 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 55.1 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 23.7 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 30.2 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 42.6 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 38.7 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 44.3 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 37.2 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 50.4 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 19.3 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 21.3 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 32.7 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 38.9 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 15.7 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 14.6 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 27 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 33.1 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 29.4 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 26.3 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 32.5 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 13 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 16.8 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 23.3 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 31.2 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 9.98 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 12.2 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 21.7 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 26.9 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 11.1 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 18.7 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 19.3 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 8.15 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 10.2 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 21 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 22.8 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 7.59 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 6.3 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 19.5 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 21.6 |

</details>

<details><summary>Filtered out (20) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 60 tokens/s (7):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8)
- **Energy over 152 J per request (19):** Qwen3-1.7B F16 llama.cpp CPU (239 J), Qwen3-1.7B F16 MNN CPU (160 J), Qwen3-1.7B F16 llama.cpp GPU (175 J), Qwen3.5-2B F16 llama.cpp CPU (243 J), Qwen3.5-2B F16 MNN CPU (174 J), Qwen3.5-2B F16 llama.cpp GPU (184 J), Qwen3-4B Q4_0 llama.cpp CPU (175 J), Qwen3-4B Q4_0 llama.cpp GPU (191 J), Qwen3-4B Q4 MNN GPU (177 J), Qwen3-4B Q8_0 llama.cpp CPU (214 J), Qwen3-4B Q8_0 llama.cpp GPU (271 J), Qwen3-4B Q8 MNN GPU (280 J), Qwen3-4B F16 llama.cpp CPU (565 J), Qwen3-4B F16 llama.cpp GPU (412 J), Qwen3.5-4B Q4_0 llama.cpp CPU (189 J), Qwen3.5-4B Q4_0 llama.cpp GPU (177 J), Qwen3.5-4B Q8 MNN CPU (217 J), Qwen3.5-4B Q8_0 llama.cpp GPU (253 J), Qwen3.5-4B Q8 MNN GPU (261 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E08

**Easy · OnePlus 15 · Email drafting app**: draft an email from bullet points; 300-token prompt, 200-token answer, first token within 3 s, 20 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 100 tokens/s · decode ≥ 8 tokens/s · energy ≤ 101 J per request.

**Pick: Qwen3.5-4B Q8, MNN, GPU** (5,586 MB, 204 / 13.4 tokens/s, 91.6 J). Kept 74 of 104 configurations (71%); 19 filtered out; 11 cannot run on this phone.

![E08](figures/scenarios/E08.png)

<details><summary>Kept pool (74), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN GPU **(pick)** | 5,586 | 204 | 13.4 | 91.6 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 50.7 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 63.3 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 99.9 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 65.4 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 67.5 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 33.8 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 35.5 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 40.4 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 39.3 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 21.7 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 29.2 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 23.6 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 31.2 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 33.2 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 36.1 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 42.9 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 41.3 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 23 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 28.3 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 32.1 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 28.2 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 27.8 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 23.9 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 12 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 15.8 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 18.8 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 16.2 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 8.63 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 9.02 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 13 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 14.1 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 23 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 22.1 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 13.3 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 12.2 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 16 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 19.1 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 9.02 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 11.3 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 14.6 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 13.5 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 15.4 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 13.8 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 7.21 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 7.94 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 11.5 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 13.5 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 6.02 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 5.56 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 9.48 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 11.5 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 11.4 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 9.25 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 11.1 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 4.71 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 5.93 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 8.08 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 10.6 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 3.68 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 4.34 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 7.5 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 9.15 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 4.1 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 7.97 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 6.63 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 2.99 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 3.72 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 7.27 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 7.78 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 2.79 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 2.34 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 6.73 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 7.37 |

</details>

<details><summary>Filtered out (19) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 100 tokens/s (15):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6)
- **Decode under 8 tokens/s (2):** Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42)
- **Energy over 101 J per request (4):** Qwen3-1.7B F16 llama.cpp CPU (105 J), Qwen3.5-2B F16 llama.cpp CPU (102 J), Qwen3-4B F16 llama.cpp CPU (249 J), Qwen3-4B F16 llama.cpp GPU (146 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E09

**Easy · OnePlus 15 · Offline medical reference**: answer questions from a clinical reference; 300-token prompt, 300-token answer, first token within 5 s, 10 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 60 tokens/s · decode ≥ 5 tokens/s · energy ≤ 202 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 119 J). Kept 80 of 104 configurations (77%); 13 filtered out; 11 cannot run on this phone.

![E09](figures/scenarios/E09.png)

<details><summary>Kept pool (80), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 119 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 134 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 73.8 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 68.2 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 91.8 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 145 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 93.4 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 98.4 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 48 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 50.9 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 59.3 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 57.6 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 31.8 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 42.4 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 33.2 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 44.8 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 85.7 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 47.3 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 50.8 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 61.8 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 60.3 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 31.5 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 40.4 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 46.9 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 40.9 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 41.2 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 34.4 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 56.8 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 17.1 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 22.6 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 27.7 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 23.6 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 12.2 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 13.1 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 18.9 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 20.1 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 33.9 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 48.5 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 31.6 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 18.9 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 17.4 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 23.3 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 28.1 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 12.7 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 16 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 21.6 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 19.8 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 22.6 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 19.7 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 29 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 10.2 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 11.3 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 16.8 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 19.8 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 8.43 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 7.83 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 13.8 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 16.9 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 15.9 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 13.5 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 16.5 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 6.77 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 8.64 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 11.9 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 15.7 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 5.26 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 6.3 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 11.1 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 13.6 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 5.86 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 10.6 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 9.79 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 4.28 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 5.34 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 10.7 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 11.5 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 3.99 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 3.33 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 9.93 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 10.9 |

</details>

<details><summary>Filtered out (13) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 60 tokens/s (7):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8)
- **Energy over 202 J per request (2):** Qwen3-4B F16 llama.cpp CPU (328 J), Qwen3-4B F16 llama.cpp GPU (212 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E10

**Easy · OnePlus 15 · Language practice app**: spoken-style conversation practice; 300-token prompt, 150-token answer, first token within 3 s, 30 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 100 tokens/s · decode ≥ 6 tokens/s · energy ≤ 101 J per request.

**Pick: Qwen3.5-4B Q8, MNN, GPU** (5,586 MB, 204 / 13.4 tokens/s, 70.4 J). Kept 74 of 104 configurations (71%); 19 filtered out; 11 cannot run on this phone.

![E10](figures/scenarios/E10.png)

<details><summary>Kept pool (74), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN GPU **(pick)** | 5,586 | 204 | 13.4 | 70.4 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 39.1 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 49.1 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 77.5 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 51.4 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 52 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 26.7 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 27.7 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 30.9 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 30.1 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 16.7 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 22.5 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 18.8 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 24.4 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 26.2 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 28.7 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 33.4 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 31.8 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 18.7 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 22.2 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 24.7 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 21.9 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 21.1 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 18.7 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 9.43 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 12.4 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 14.3 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 12.5 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 6.84 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 6.99 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 9.99 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 11.1 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 17.6 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 17.3 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 10.5 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 9.62 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 12.4 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 14.6 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 7.19 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 8.96 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 11.2 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 10.4 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 11.7 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 10.9 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 5.7 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 6.28 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 8.87 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 10.3 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 4.81 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 4.43 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 7.3 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 8.85 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 9.18 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 7.11 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 8.47 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 3.68 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 4.57 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 6.18 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 7.99 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 2.89 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 3.36 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 5.72 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 6.93 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 3.23 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 6.63 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 5.05 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 2.35 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 2.91 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 5.56 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 5.9 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 2.19 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 1.84 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 5.13 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 5.59 |

</details>

<details><summary>Filtered out (19) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 100 tokens/s (15):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6)
- **Energy over 101 J per request (2):** Qwen3-4B F16 llama.cpp CPU (209 J), Qwen3-4B F16 llama.cpp GPU (113 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E11

**Easy · OnePlus 15 · Field-guide app**: explain a plant or bird from a description, offline; 200-token prompt, 250-token answer, first token within 5 s, 10 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 40 tokens/s · decode ≥ 5 tokens/s · energy ≤ 202 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 95.8 J). Kept 83 of 104 configurations (80%); 10 filtered out; 11 cannot run on this phone.

![E11](figures/scenarios/E11.png)

<details><summary>Kept pool (83), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 95.8 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 110 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 60.8 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 54.7 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 81.7 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 75.4 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 119 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 76.2 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 62.7 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 81.1 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 76.1 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 39.1 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 41.7 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 49 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 47.6 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 26.2 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 34.9 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 27 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 36.6 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 69.6 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 38.6 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 41.3 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 50.7 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 49.7 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 25.3 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 33 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 38.7 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 33.6 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 34.1 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 28.2 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 45.3 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 14 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 18.5 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 22.9 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 19.5 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 9.93 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 10.7 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 15.6 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 16.4 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 28 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 38.5 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 25.8 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 15.4 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 14.2 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 19.2 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 23.2 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 10.3 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 13 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 17.9 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 16.4 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 18.7 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 16 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 22.9 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 8.32 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 9.18 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 13.8 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 16.4 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 6.83 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 6.35 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 11.4 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 14 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 12.9 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 11.1 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 13.7 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 5.55 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 7.11 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 9.83 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 13.1 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 4.29 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 5.18 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 9.15 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 11.3 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 4.78 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 8.43 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 8.11 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 3.5 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 4.37 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 8.84 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 9.56 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 3.26 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 2.71 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 8.22 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 9.05 |

</details>

<details><summary>Filtered out (10) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 40 tokens/s (3):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 202 J per request (1):** Qwen3-4B F16 llama.cpp CPU (258 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E12

**Easy · OnePlus 15 · Offline Q&A (travel)**: an assistant with no network; 200-token prompt, 200-token answer, first token within 10 s, 20 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 20 tokens/s · decode ≥ 4 tokens/s · energy ≤ 152 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 79.4 J). Kept 84 of 104 configurations (81%); 9 filtered out; 11 cannot run on this phone.

![E12](figures/scenarios/E12.png)

<details><summary>Kept pool (84), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 79.4 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 89.3 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 49.2 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 45.5 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 66.9 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 61.2 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 96.6 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 62.2 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 52.5 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 65.6 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 62.5 |
| Qwen3.5-2B F16 llama.cpp CPU | 3,907 | 23.5 | 14.7 | 91.7 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 32 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 34 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 39.5 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 38.4 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 21.2 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 28.3 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 22.2 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 29.8 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 57.2 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 31.5 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 33.9 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 41.2 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 40.2 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 21 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 26.9 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 31.3 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 27.3 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 27.5 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 22.9 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 37.8 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 11.4 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 15.1 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 18.5 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 15.8 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 8.14 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 8.72 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 12.6 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 13.4 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 22.6 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 32.3 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 21.1 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 12.6 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 11.6 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 15.5 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 18.7 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 8.46 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 10.7 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 14.4 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 13.2 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 15.1 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 13.1 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 19.3 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 6.82 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 7.51 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 11.2 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 13.2 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 5.62 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 5.22 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 9.23 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 11.3 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 10.6 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 9.01 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 11 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 4.52 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 5.76 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 7.93 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 10.5 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 3.5 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 4.2 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 7.37 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 9.06 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 3.91 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 7.1 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 6.53 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 2.85 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 3.56 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 7.13 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 7.69 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 2.66 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 2.22 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 6.62 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 7.28 |

</details>

<details><summary>Filtered out (9) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 20 tokens/s (2):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 152 J per request (1):** Qwen3-4B F16 llama.cpp CPU (219 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E13

**Easy · OnePlus 15 · Meeting-notes cleanup**: turn a transcript into notes after a meeting; 1,500-token prompt, 300-token answer, first token within 30 s, 3 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 50 tokens/s · decode ≥ 3 tokens/s · energy ≤ 674 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 201 J). Kept 82 of 104 configurations (79%); 11 filtered out; 11 cannot run on this phone.

![E13](figures/scenarios/E13.png)

<details><summary>Kept pool (82), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 201 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 162 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 91.6 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 119 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 147 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 117 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 185 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 131 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 121 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 143 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 69.2 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 69.1 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 69.4 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 68 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 38.1 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 53 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 50.8 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 61 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 130 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 67.4 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 77.4 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 81.8 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 73.7 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 54.8 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 56.4 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 56.8 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 52.5 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 45.5 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 46.2 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 106 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 24 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 31.1 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 31.3 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 29 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 18.1 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 16.7 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 23.1 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 28.3 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 39.3 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 94.9 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 43.7 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 27.3 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 24.8 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 28.9 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 32.3 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 19.5 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 23.6 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 24.4 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 23.6 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 26.1 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 28.3 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 59 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 15 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 16.4 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 20.5 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 22.7 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 13.2 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 11.9 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 16.8 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 20 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 25.7 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 16.4 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 18.3 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 9.1 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 10.7 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 13.8 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 16.7 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 7.39 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 7.98 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 12.6 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 14.7 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 8.22 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 21.1 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 11 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 5.93 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 7.26 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 12.4 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 12.6 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 5.55 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 4.77 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 11.2 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 12 |

</details>

<details><summary>Filtered out (11) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 50 tokens/s (4):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 674 J per request (1):** Qwen3-4B F16 llama.cpp CPU (692 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E14

**Easy · OnePlus 15 · Document translation**: translate a page while the user does something else; 500-token prompt, 500-token answer, first token within 30 s, 10 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 16.7 tokens/s · decode ≥ 2 tokens/s · energy ≤ 303 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 198 J). Kept 84 of 104 configurations (81%); 9 filtered out; 11 cannot run on this phone.

![E14](figures/scenarios/E14.png)

<details><summary>Kept pool (84), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 198 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 223 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 123 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 114 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 167 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 153 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 241 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 156 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 131 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 164 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 156 |
| Qwen3.5-2B F16 llama.cpp CPU | 3,907 | 23.5 | 14.7 | 229 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 80.1 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 84.9 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 98.8 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 96.1 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 53 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 70.7 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 55.4 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 74.6 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 143 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 78.9 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 84.7 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 103 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 101 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 52.5 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 67.3 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 78.2 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 68.1 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 68.6 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 57.3 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 94.6 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 28.6 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 37.6 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 46.1 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 39.4 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 20.3 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 21.8 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 31.6 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 33.5 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 56.5 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 80.8 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 52.7 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 31.4 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 28.9 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 38.8 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 46.8 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 21.1 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 26.7 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 36 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 33 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 37.7 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 32.8 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 48.3 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 17 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 18.8 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 28 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 33 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 14.1 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 13 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 23.1 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 28.2 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 26.5 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 22.5 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 27.5 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 11.3 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 14.4 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 19.8 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 26.2 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 8.76 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 10.5 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 18.4 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 22.6 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 9.77 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 17.7 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 16.3 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 7.14 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 8.91 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 17.8 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 19.2 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 6.65 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 5.55 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 16.6 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 18.2 |

</details>

<details><summary>Filtered out (9) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 16.7 tokens/s (2):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 303 J per request (2):** Qwen3-4B F16 llama.cpp CPU (546 J), Qwen3-4B F16 llama.cpp GPU (354 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E15

**Easy · OnePlus 15 · Voice-memo summary**: title and summarize a recorded voice memo; 800-token prompt, 80-token answer, first token within 20 s, 10 requests ≤ 1% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 40 tokens/s · decode ≥ 2 tokens/s · energy ≤ 101 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 81.2 J). Kept 83 of 104 configurations (80%); 10 filtered out; 11 cannot run on this phone.

![E15](figures/scenarios/E15.png)

<details><summary>Kept pool (83), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 81.2 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 52.4 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 30.4 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 48.9 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 54.8 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 39.8 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 62.8 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 47.6 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 63.9 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 39.7 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 54.7 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 25.5 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 24.5 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 21.9 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 21.6 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 12.3 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 17.7 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 19.4 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 21.7 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 49.3 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 24.7 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 29.5 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 28.5 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 24.1 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 22.4 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 20.4 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 18.4 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 17.8 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 13.6 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 16.3 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 44.7 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 8.67 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 11.2 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 9.53 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 9.55 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 6.79 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 5.63 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 7.55 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 10.2 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 12.3 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 40.8 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 15.7 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 10.1 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 9.12 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 9.58 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 10 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 7.47 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 8.79 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 7.41 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 7.54 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 8.15 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 10.4 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 25.8 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 5.57 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 6.08 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 6.72 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 7.03 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 5.1 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 4.55 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 5.48 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 6.36 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 10.1 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 5.31 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 5.48 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 3.2 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 3.53 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 4.29 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 4.8 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 2.68 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 2.69 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 3.89 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 4.27 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 2.98 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 9.13 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 3.34 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 2.13 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 2.58 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 3.86 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 3.73 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 2 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 1.75 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 3.44 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 3.56 |

</details>

<details><summary>Filtered out (10) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 40 tokens/s (3):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 101 J per request (3):** Qwen3-1.7B F16 llama.cpp CPU (130 J), Qwen3.5-2B F16 llama.cpp CPU (113 J), Qwen3-4B F16 llama.cpp CPU (306 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E16

**Easy · OnePlus 15 · Morning digest**: a scheduled summary of overnight messages; 2,000-token prompt, 300-token answer, first token within 120 s, 1 request ≤ 1% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 16.7 tokens/s · decode ≥ 1 tokens/s · energy ≤ 1,012 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 236 J). Kept 84 of 104 configurations (81%); 9 filtered out; 11 cannot run on this phone.

![E16](figures/scenarios/E16.png)

<details><summary>Kept pool (84), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 236 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 173 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 99.1 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 141 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 167 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 128 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 202 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 147 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 180 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 130 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 164 |
| Qwen3.5-2B F16 llama.cpp CPU | 3,907 | 23.5 | 14.7 | 318 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 78.1 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 76.6 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 73.7 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 72.4 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 40.8 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 57.4 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 58.2 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 67.7 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 148 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 75.7 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 88.5 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 90.1 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 79.2 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 64.5 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 63.1 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 60.9 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 57.3 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 47.3 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 51.1 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 127 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 26.8 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 34.7 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 32.8 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 31.3 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 20.6 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 18.1 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 24.9 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 31.6 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 41.6 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 114 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 48.8 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 30.8 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 28 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 31.3 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 34 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 22.3 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 26.7 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 25.5 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 25.1 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 27.6 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 31.8 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 71.5 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 16.9 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 18.5 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 22.1 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 23.9 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 15.2 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 13.6 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 18.1 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 21.3 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 29.8 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 17.5 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 19 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 10.1 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 11.5 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 14.5 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 17.2 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 8.28 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 8.68 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 13.3 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 15.1 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 9.21 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 25.5 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 11.5 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 6.61 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 8.06 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 13.1 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 13.1 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 6.2 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 5.37 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 11.8 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 12.4 |

</details>

<details><summary>Filtered out (9) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 16.7 tokens/s (2):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E17

**Easy · OnePlus 15 · Weekly journal summary**: summarize a week of entries; 3,000-token prompt, 400-token answer, first token within 120 s, 1 request ≤ 1% of battery a day.

Limits: peak memory ≤ 7,600 MB · prefill ≥ 25 tokens/s · decode ≥ 1 tokens/s · energy ≤ 1,012 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 337 J). Kept 85 of 104 configurations (82%); 8 filtered out; 11 cannot run on this phone.

![E17](figures/scenarios/E17.png)

<details><summary>Kept pool (85), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 337 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 239 |
| Qwen3.5-4B Q8_0 llama.cpp GPU | 6,406 | 240 | 12.6 | 234 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 137 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 202 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 235 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 178 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 280 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 206 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 260 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 180 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 232 |
| Qwen3.5-2B F16 MNN CPU | 6,475 | 113 | 14.1 | 177 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 110 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 107 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 101 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 99.4 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 56.1 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 79.5 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 82.5 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 94.8 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 210 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 107 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 125 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 126 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 109 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 92.5 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 88.6 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 83.9 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 79.6 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 64.3 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 71.5 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 183 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 37.7 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 48.6 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 44.7 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 43.2 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 29.1 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 25.2 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 34.3 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 44.4 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 56.9 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 165 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 68.4 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 43.4 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 39.3 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 43.2 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 46.5 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 31.7 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 37.7 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 34.8 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 34.6 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 37.8 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 44.8 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 104 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 23.9 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 26.1 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 30.5 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 32.7 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 21.5 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 19.3 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 24.9 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 29.2 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 42.4 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 24.2 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 25.9 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 14.1 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 15.9 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 19.9 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 23.2 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 11.6 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 12 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 18.1 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 20.5 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 12.9 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 36.9 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 15.7 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 9.28 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 11.3 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 17.9 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 17.7 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 8.7 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 7.56 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 16.1 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 16.9 |

</details>

<details><summary>Filtered out (8) and cannot run (11)</summary>

- **Peak memory over 7,600 MB (6):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB)
- **Prefill under 25 tokens/s (3):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 1,012 J per request (1):** Qwen3-4B F16 llama.cpp CPU (1,226 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E18

**Easy · OnePlus 15 · Statement categorization**: categorize a month of bank transactions; 2,500-token prompt, 500-token answer, first token within 120 s, 1 request ≤ 1% of battery a day.

Limits: peak memory ≤ 6,000 MB · prefill ≥ 20.8 tokens/s · decode ≥ 1 tokens/s · energy ≤ 1,012 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 336 J). Kept 84 of 104 configurations (81%); 9 filtered out; 11 cannot run on this phone.

![E18](figures/scenarios/E18.png)

<details><summary>Kept pool (84), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 336 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 270 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 153 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 199 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 245 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 196 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 308 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 219 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 251 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 201 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 239 |
| Qwen3.5-2B F16 llama.cpp CPU | 3,907 | 23.5 | 14.7 | 442 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 115 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 115 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 116 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 113 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 63.6 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 88.3 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 84.7 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 102 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 216 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 112 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 129 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 136 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 123 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 91.4 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 94 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 94.6 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 87.4 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 75.9 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 77 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 177 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 40 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 51.9 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 52.1 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 48.4 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 30.2 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 27.8 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 38.5 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 47.1 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 65.5 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 158 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 72.9 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 45.5 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 41.4 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 48.2 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 53.8 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 32.5 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 39.3 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 40.6 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 39.3 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 43.6 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 47.1 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 98.4 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 24.9 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 27.3 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 34.2 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 37.9 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 22 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 19.9 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 28 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 33.3 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 42.8 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 27.3 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 30.5 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 15.2 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 17.8 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 22.9 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 27.9 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 12.3 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 13.3 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 21 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 24.4 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 13.7 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 35.2 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 18.3 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 9.88 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 12.1 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 20.6 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 21 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 9.25 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 7.95 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 18.8 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 20 |

</details>

<details><summary>Filtered out (9) and cannot run (11)</summary>

- **Peak memory over 6,000 MB (8):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB)
- **Prefill under 20.8 tokens/s (2):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 1,012 J per request (1):** Qwen3-4B F16 llama.cpp CPU (1,153 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E19

**Easy · OnePlus 15 · Overnight photo captioning**: caption the photo library while charging; 200-token prompt, 40-token answer, first token within 30 s, 300 requests ≤ 20% of battery a day.

Limits: peak memory ≤ 7,600 MB · prefill ≥ 6.67 tokens/s · decode ≥ 1 tokens/s · energy ≤ 67.4 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 26.9 J). Kept 87 of 104 configurations (84%); 6 filtered out; 11 cannot run on this phone.

![E19](figures/scenarios/E19.png)

<details><summary>Kept pool (87), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 26.9 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 21.6 |
| Qwen3.5-4B Q8_0 llama.cpp GPU | 6,406 | 240 | 12.6 | 21.1 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 12.2 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 15.9 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 19.6 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 15.7 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 24.7 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 17.5 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 20 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 16.1 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 19.1 |
| Qwen3.5-2B F16 llama.cpp CPU | 3,907 | 23.5 | 14.7 | 35.4 |
| Qwen3.5-2B F16 MNN CPU | 6,475 | 113 | 14.1 | 15.5 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 9.23 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 9.21 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 9.26 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 9.07 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 5.08 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 7.07 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 6.78 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 8.13 |
| Qwen3-1.7B F16 llama.cpp CPU | 4,115 | 15.9 | 15.9 | 39.1 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 17.3 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 8.98 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 10.3 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 10.9 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 9.82 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 7.31 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 7.52 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 7.57 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 6.99 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 6.07 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 6.16 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 14.1 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 3.2 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 4.15 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 4.17 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 3.87 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 2.41 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 2.22 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 3.08 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 3.77 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 5.24 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 12.7 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 5.83 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 3.64 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 3.31 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 3.86 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 4.3 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 2.6 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 3.14 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 3.25 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 3.14 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 3.48 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 3.77 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 7.87 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 1.99 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 2.19 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 2.74 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 3.03 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 1.76 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 1.59 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 2.24 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 2.67 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 3.43 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 2.18 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 2.44 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 1.21 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 1.42 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 1.83 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 2.23 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 0.985 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 1.06 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 1.68 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 1.96 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 1.1 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 2.82 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 1.47 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 0.79 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 0.969 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 1.65 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 1.68 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 0.74 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 0.636 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 1.5 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 1.6 |

</details>

<details><summary>Filtered out (6) and cannot run (11)</summary>

- **Peak memory over 7,600 MB (6):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB)
- **Energy over 67.4 J per request (1):** Qwen3-4B F16 llama.cpp CPU (92.3 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### E20

**Easy · OnePlus 15 · Overnight inbox sorting**: label every email while charging; 300-token prompt, 10-token answer, first token within 10 s, 500 requests ≤ 20% of battery a day.

Limits: peak memory ≤ 7,600 MB · prefill ≥ 30 tokens/s · decode ≥ 1 tokens/s · energy ≤ 40.5 J per request.

**Pick: Qwen3.5-4B Q8, MNN, CPU** (5,355 MB, 67.6 / 14.2 tokens/s, 23.9 J). Kept 85 of 104 configurations (82%); 8 filtered out; 11 cannot run on this phone.

![E20](figures/scenarios/E20.png)

<details><summary>Kept pool (85), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-4B Q8 MNN CPU **(pick)** | 5,355 | 67.6 | 14.2 | 23.9 |
| Qwen3.5-4B Q8 MNN GPU | 5,586 | 204 | 13.4 | 11.2 |
| Qwen3.5-4B Q8_0 llama.cpp GPU | 6,406 | 240 | 12.6 | 11.2 |
| Qwen3.5-4B Q4 MNN GPU | 3,040 | 275 | 21.7 | 6.77 |
| Qwen3.5-4B Q4 MNN CPU | 2,609 | 62.5 | 16.9 | 14.7 |
| Qwen3.5-4B Q4_0 llama.cpp CPU | 5,263 | 59.8 | 15.9 | 14.7 |
| Qwen3.5-4B Q4_0 llama.cpp GPU | 4,633 | 281 | 14.5 | 9.24 |
| Qwen3-4B Q8 MNN GPU | 4,975 | 180 | 12.6 | 14.5 |
| Qwen3-4B Q4 MNN GPU | 2,964 | 191 | 19.7 | 12.3 |
| Qwen3-4B Q4 MNN CPU | 2,772 | 41 | 16.8 | 19.9 |
| Qwen3-4B Q4_0 llama.cpp GPU | 5,647 | 315 | 16.7 | 8.7 |
| Qwen3-4B Q4_0 llama.cpp CPU | 4,561 | 56.4 | 16.3 | 15.1 |
| Qwen3.5-2B F16 MNN CPU | 6,475 | 113 | 14.1 | 9.32 |
| Qwen3.5-2B Q8 MNN CPU | 2,206 | 169 | 31.4 | 6.72 |
| Qwen3.5-2B Q8_0 llama.cpp CPU | 4,142 | 159 | 29.3 | 6.08 |
| Qwen3.5-2B Q8_0 llama.cpp GPU | 4,157 | 551 | 27.6 | 4.43 |
| Qwen3.5-2B Q8 MNN GPU | 2,810 | 458 | 26 | 4.43 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 2.59 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 3.97 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 5.36 |
| Qwen3.5-2B Q4_0 llama.cpp CPU | 2,523 | 150 | 32.7 | 5.41 |
| Qwen3-1.7B F16 MNN CPU | 5,220 | 79.4 | 13.6 | 13.5 |
| Qwen3-1.7B Q8_0 llama.cpp CPU | 3,520 | 150 | 30.8 | 6.43 |
| Qwen3-1.7B Q8 MNN CPU | 2,716 | 136 | 29.6 | 8.12 |
| Qwen3-1.7B Q8_0 llama.cpp GPU | 5,169 | 164 | 28.1 | 6.89 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 5.24 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 6.69 |
| Qwen3-1.7B Q4_0 llama.cpp CPU | 2,140 | 144 | 34.9 | 5.22 |
| Qwen3-1.7B Q4_0 llama.cpp GPU | 3,113 | 729 | 31.4 | 3.94 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 4.16 |
| Qwen3.5-0.8B F16 llama.cpp GPU | 4,685 | 847 | 35 | 2.42 |
| Qwen3.5-0.8B F16 MNN CPU | 3,068 | 250 | 34.5 | 4 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 13.8 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 2.22 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 2.82 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 1.79 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 2.09 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 1.83 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 1.3 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 1.64 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 2.64 |
| Qwen3-0.6B F16 llama.cpp GPU | 4,464 | 627 | 41.2 | 2.44 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 12.8 |
| Qwen3-0.6B F16 MNN CPU | 2,081 | 184 | 37.3 | 3.99 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 2.66 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 2.39 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 2.13 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 1.95 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 2.07 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 2.35 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 1.38 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 1.57 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 1.61 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 2.73 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 8.23 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 1.48 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 1.61 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 1.46 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 1.36 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 1.43 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 1.25 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 1.18 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 1.31 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 2.89 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 1.14 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 0.984 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 0.787 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 0.782 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 0.848 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 0.769 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 0.69 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 0.616 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 0.749 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 0.714 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 0.767 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 2.89 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 0.62 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 0.541 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 0.642 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 0.762 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 0.646 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 0.51 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 0.459 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 0.65 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 0.625 |

</details>

<details><summary>Filtered out (8) and cannot run (11)</summary>

- **Peak memory over 7,600 MB (6):** Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB)
- **Prefill under 30 tokens/s (3):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 40.5 J per request (2):** Qwen3-1.7B F16 llama.cpp CPU (42 J), Qwen3-4B F16 llama.cpp CPU (98.9 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### M01

**Medium · Galaxy S23 · Language tutor**: conversational practice with corrections; 300-token prompt, 100-token answer, first token within 2 s, 40 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,536 MB · prefill ≥ 150 tokens/s · decode ≥ 6 tokens/s · energy ≤ 40.5 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,458 MB, 252 / 28 tokens/s, 27.3 J). Kept 37 of 104 configurations (36%); 45 filtered out; 22 cannot run on this phone.

![M01](figures/scenarios/M01.png)

<details><summary>Kept pool (37), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,458 | 252 | 28 | 27.3 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 219 | 8.52 | 37.6 |
| Qwen3.5-0.8B Q8 MNN CPU | 1,345 | 274 | 47 | 26.5 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,083 | 379 | 35.1 | 21.2 |
| Qwen3.5-0.8B Q4 MNN CPU | 691 | 273 | 73.6 | 19 |
| Qwen3.5-0.8B Q4 MNN GPU | 712 | 454 | 46.1 | 13.2 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,310 | 157 | 13.4 | 31.6 |
| Qwen3-0.6B Q8 MNN CPU | 856 | 368 | 49.8 | 23.5 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,390 | 216 | 47.1 | 27.5 |
| Qwen3-0.6B Q8 MNN GPU | 1,005 | 540 | 8.45 | 22.8 |
| Qwen3-0.6B Q4 MNN CPU | 548 | 371 | 72.7 | 17.6 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 832 | 232 | 49.8 | 24.6 |
| Qwen3-0.6B Q4 MNN GPU | 696 | 559 | 9.74 | 19.1 |
| SmolLM2-360M F16 MNN CPU | 826 | 405 | 40.8 | 25 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 14.8 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 348 | 75.2 | 17.8 |
| SmolLM2-360M Q8 MNN GPU | 614 | 735 | 8.56 | 18.6 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 11.4 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 14.1 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 961 | 219 | 49.5 | 16.1 |
| SmolLM2-360M Q4 MNN GPU | 433 | 763 | 9.09 | 16 |
| Gemma3-270M F16 MNN CPU | 1,487 | 1,082 | 56.2 | 17.3 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 10.8 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 12.2 |
| Gemma3-270M Q8 MNN GPU | 515 | 1,589 | 8.89 | 14.9 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 9.48 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 9.49 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,498 | 361 | 33.3 | 12.3 |
| Gemma3-270M Q4 MNN GPU | 379 | 1,622 | 8.98 | 12.4 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 9.51 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 6.6 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 7.66 |
| SmolLM2-135M Q8 MNN GPU | 329 | 1,753 | 9.09 | 12.3 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 5.4 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 6.61 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 9.66 |
| SmolLM2-135M Q4 MNN GPU | 259 | 1,725 | 9.13 | 10.9 |

</details>

<details><summary>Filtered out (45) and cannot run (22)</summary>

- **Peak memory over 1,536 MB (35):** Gemma3-270M Q8_0 llama.cpp GPU (1,546 MB), Gemma3-270M F16 llama.cpp GPU (2,486 MB), SmolLM2-360M F16 llama.cpp GPU (2,179 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,970 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,721 MB), Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,734 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,164 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8 MNN CPU (2,397 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B Q8 MNN GPU (2,309 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,522 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,247 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,685 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4 MNN CPU (2,782 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 150 tokens/s (37):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp CPU (120), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp CPU (127), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp CPU (42.1), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q4_0 llama.cpp GPU (133), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3-1.7B Q4_0 llama.cpp CPU (119), Qwen3-1.7B Q4 MNN CPU (138), Qwen3-1.7B Q4_0 llama.cpp GPU (107), Qwen3-1.7B Q8_0 llama.cpp CPU (106), Qwen3-1.7B Q8 MNN CPU (132), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B Q4_0 llama.cpp CPU (132), Qwen3.5-2B Q4 MNN CPU (134), Qwen3.5-2B Q4_0 llama.cpp GPU (109), Qwen3.5-2B Q8_0 llama.cpp CPU (112), Qwen3.5-2B Q8 MNN CPU (128), Qwen3.5-2B Q8_0 llama.cpp GPU (113), Qwen3.5-2B F16 llama.cpp CPU (9.4), Qwen3-4B Q4_0 llama.cpp CPU (44.5), Qwen3-4B Q4 MNN CPU (53.5), Qwen3-4B Q4_0 llama.cpp GPU (40.4), Qwen3-4B Q4 MNN GPU (88.4), Qwen3.5-4B Q4_0 llama.cpp CPU (50.8), Qwen3.5-4B Q4 MNN CPU (49.9), Qwen3.5-4B Q4_0 llama.cpp GPU (47.5), Qwen3.5-4B Q4 MNN GPU (115)
- **Decode under 6 tokens/s (2):** Qwen3-1.7B F16 llama.cpp CPU (5.73), Qwen3.5-2B F16 llama.cpp CPU (5.25)
- **Energy over 40.5 J per request (39):** SmolLM2-135M Q8_0 llama.cpp GPU (47.1 J), SmolLM2-135M F16 llama.cpp GPU (56 J), Gemma3-270M Q8_0 llama.cpp GPU (46.5 J), Gemma3-270M F16 llama.cpp GPU (55.6 J), SmolLM2-360M Q8_0 llama.cpp GPU (127 J), SmolLM2-360M F16 llama.cpp CPU (67.7 J), SmolLM2-360M F16 llama.cpp GPU (151 J), Qwen3-0.6B Q8_0 llama.cpp GPU (182 J), Qwen3-0.6B F16 llama.cpp CPU (95.3 J), Qwen3-0.6B F16 MNN CPU (40.7 J), Qwen3-0.6B F16 llama.cpp GPU (225 J), Qwen3.5-0.8B F16 llama.cpp CPU (108 J), Qwen3.5-0.8B F16 MNN CPU (46.6 J), Qwen3.5-0.8B F16 llama.cpp GPU (47.8 J), Qwen3-1.7B Q4_0 llama.cpp CPU (48.4 J), Qwen3-1.7B Q4 MNN CPU (42.7 J), Qwen3-1.7B Q4_0 llama.cpp GPU (46.4 J), Qwen3-1.7B Q8_0 llama.cpp CPU (60.5 J), Qwen3-1.7B Q8 MNN CPU (53.7 J), Qwen3-1.7B Q8_0 llama.cpp GPU (475 J), Qwen3-1.7B Q8 MNN GPU (51.8 J), Qwen3-1.7B F16 llama.cpp CPU (272 J), Qwen3-1.7B F16 llama.cpp GPU (714 J), Qwen3.5-2B Q4_0 llama.cpp CPU (51.7 J), Qwen3.5-2B Q4 MNN CPU (43.7 J), Qwen3.5-2B Q4_0 llama.cpp GPU (44 J), Qwen3.5-2B Q8_0 llama.cpp CPU (63.1 J), Qwen3.5-2B Q8 MNN CPU (56.5 J), Qwen3.5-2B Q8_0 llama.cpp GPU (58.4 J), Qwen3.5-2B Q8 MNN GPU (45.8 J), Qwen3.5-2B F16 llama.cpp CPU (265 J), Qwen3-4B Q4_0 llama.cpp CPU (107 J), Qwen3-4B Q4 MNN CPU (93.6 J), Qwen3-4B Q4_0 llama.cpp GPU (97.9 J), Qwen3-4B Q4 MNN GPU (82.9 J), Qwen3.5-4B Q4_0 llama.cpp CPU (113 J), Qwen3.5-4B Q4 MNN CPU (92.2 J), Qwen3.5-4B Q4_0 llama.cpp GPU (99.7 J), Qwen3.5-4B Q4 MNN GPU (68.1 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### M02

**Medium · OnePlus 15 · Voice assistant**: spoken answer to a spoken question; 256-token prompt, 64-token answer, first token within 1 s, 50 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 1,536 MB · prefill ≥ 256 tokens/s · decode ≥ 5 tokens/s · energy ≤ 40.5 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,470 MB, 669 / 43.2 tokens/s, 7.8 J). Kept 41 of 104 configurations (39%); 52 filtered out; 11 cannot run on this phone.

![M02](figures/scenarios/M02.png)

<details><summary>Kept pool (41), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,470 | 669 | 43.2 | 7.8 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 10.6 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 4.75 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 5.91 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 3.55 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 3.36 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 5.59 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 5.37 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 4.9 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 6.66 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 3.8 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 4.62 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 4.83 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 5.57 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 2.94 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 3.22 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 4.18 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 4.69 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 2.56 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 2.33 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 3.43 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 4.1 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 4.96 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 3.34 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 1.82 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 2.17 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 3.52 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 1.46 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 1.61 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 3.07 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 1.63 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 3.95 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 2.28 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 1.18 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 1.45 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 2.55 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 2.63 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 1.1 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 0.941 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 2.33 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 2.5 |

</details>

<details><summary>Filtered out (52) and cannot run (11)</summary>

- **Peak memory over 1,536 MB (49):** Gemma3-270M Q4_0 llama.cpp GPU (1,538 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,591 MB), Gemma3-270M F16 llama.cpp GPU (2,534 MB), SmolLM2-360M F16 llama.cpp GPU (2,225 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,552 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (2,017 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (2,081 MB), Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,883 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,273 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B Q8 MNN GPU (2,479 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8 MNN CPU (2,206 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 256 tokens/s (32):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3-0.6B F16 MNN CPU (184), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3.5-0.8B F16 MNN CPU (250), Qwen3-1.7B Q4_0 llama.cpp CPU (144), Qwen3-1.7B Q4 MNN CPU (103), Qwen3-1.7B Q8_0 llama.cpp CPU (150), Qwen3-1.7B Q8 MNN CPU (136), Qwen3-1.7B Q8_0 llama.cpp GPU (164), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3-1.7B F16 llama.cpp GPU (225), Qwen3.5-2B Q4_0 llama.cpp CPU (150), Qwen3.5-2B Q4 MNN CPU (158), Qwen3.5-2B Q8_0 llama.cpp CPU (159), Qwen3.5-2B Q8 MNN CPU (169), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3.5-2B F16 MNN CPU (113), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q4 MNN GPU (191), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B Q8 MNN GPU (180), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6), Qwen3.5-4B Q8_0 llama.cpp GPU (240), Qwen3.5-4B Q8 MNN GPU (204)
- **Energy over 40.5 J per request (4):** Qwen3-1.7B F16 llama.cpp CPU (54.4 J), Qwen3.5-2B F16 llama.cpp CPU (49.8 J), Qwen3-4B F16 llama.cpp CPU (128 J), Qwen3-4B F16 llama.cpp GPU (53.5 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### M03

**Medium · OnePlus 8 Pro · Journaling prompts**: a reflective question after a journal entry; 300-token prompt, 150-token answer, first token within 5 s, 5 requests ≤ 1% of battery a day.

Limits: peak memory ≤ 1,536 MB · prefill ≥ 60 tokens/s · decode ≥ 5 tokens/s · energy ≤ 125 J per request.

**Pick: Qwen3.5-2B Q4, MNN, CPU** (1,218 MB, 77.9 / 20.7 tokens/s, 67.1 J). Kept 41 of 104 configurations (39%); 38 filtered out; 25 cannot run on this phone.

![M03](figures/scenarios/M03.png)

<details><summary>Kept pool (41), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN CPU **(pick)** | 1,218 | 77.9 | 20.7 | 67.1 |
| Qwen3.5-2B Q4 MNN GPU | 1,259 | 102 | 14.2 | 47.1 |
| Qwen3-1.7B Q4 MNN CPU | 1,270 | 81.8 | 19.1 | 73.8 |
| Qwen3-1.7B Q4 MNN GPU | 1,211 | 77.3 | 9.06 | 59.6 |
| Qwen3.5-0.8B Q8 MNN CPU | 926 | 182 | 26 | 44.2 |
| Qwen3.5-0.8B Q8 MNN GPU | 959 | 182 | 17.9 | 33.9 |
| Qwen3.5-0.8B Q4 MNN CPU | 550 | 167 | 45.9 | 28.9 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,114 | 207 | 34.8 | 39.4 |
| Qwen3.5-0.8B Q4 MNN GPU | 583 | 207 | 24.1 | 20.8 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,440 | 259 | 30.3 | 39.3 |
| Qwen3-0.6B Q8 MNN CPU | 962 | 239 | 27.8 | 41.8 |
| Qwen3-0.6B Q8 MNN GPU | 844 | 213 | 8.89 | 37.6 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 261 | 48.2 | 30 |
| Qwen3-0.6B Q4 MNN CPU | 537 | 227 | 42.8 | 28.4 |
| Qwen3-0.6B Q4 MNN GPU | 546 | 214 | 9.15 | 28 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 161 | 30.3 | 49.1 |
| SmolLM2-360M F16 MNN CPU | 803 | 280 | 25.9 | 42.5 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 825 | 377 | 51.2 | 25.2 |
| SmolLM2-360M Q8 MNN CPU | 492 | 342 | 44.6 | 26.3 |
| SmolLM2-360M Q8 MNN GPU | 514 | 298 | 9.25 | 27.3 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 18.7 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 18.6 |
| SmolLM2-360M Q4 MNN GPU | 333 | 299 | 9.3 | 22.6 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 484 | 42.8 | 29.7 |
| Gemma3-270M F16 MNN CPU | 1,435 | 755 | 31.1 | 32.1 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,013 | 70.4 | 17.1 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 21.3 |
| Gemma3-270M Q8 MNN GPU | 430 | 736 | 8.35 | 21.8 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 1,015 | 84.3 | 13.9 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 15.4 |
| Gemma3-270M Q4 MNN GPU | 295 | 746 | 8.27 | 19.1 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,429 | 77.8 | 7.09 | 33.7 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 19.6 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 17.2 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 11 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 11.2 |
| SmolLM2-135M Q8 MNN GPU | 253 | 743 | 9.84 | 16 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 8.45 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 8.06 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 681 | 105 | 13.3 | 22.4 |
| SmolLM2-135M Q4 MNN GPU | 186 | 743 | 9.88 | 14.7 |

</details>

<details><summary>Filtered out (38) and cannot run (25)</summary>

- **Peak memory over 1,536 MB (31):** Gemma3-270M F16 llama.cpp GPU (1,886 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,908 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,811 MB), Qwen3-0.6B F16 llama.cpp GPU (2,344 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,642 MB), Qwen3.5-0.8B F16 MNN CPU (2,906 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (2,753 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,678 MB), Qwen3-1.7B Q8 MNN CPU (2,416 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,474 MB), Qwen3-1.7B Q8 MNN GPU (2,072 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,974 MB), Qwen3.5-2B Q8 MNN GPU (2,221 MB), Qwen3.5-2B F16 llama.cpp CPU (3,908 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,560 MB), Qwen3-4B Q4 MNN CPU (2,755 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q4 MNN GPU (2,580 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3-4B Q8 MNN GPU (4,179 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q4 MNN CPU (2,875 MB), Qwen3.5-4B Q4 MNN GPU (2,655 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 60 tokens/s (25):** SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q4_0 llama.cpp GPU (58), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q4_0 llama.cpp GPU (35.1), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3-1.7B Q4_0 llama.cpp GPU (28.9), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B F16 llama.cpp CPU (27.4), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3.5-2B F16 llama.cpp CPU (28.2), Qwen3-4B Q4_0 llama.cpp CPU (41.1), Qwen3-4B Q4 MNN CPU (32.1), Qwen3-4B Q4_0 llama.cpp GPU (10.9), Qwen3-4B Q4 MNN GPU (31.1), Qwen3-4B Q8 MNN CPU (31), Qwen3-4B Q8 MNN GPU (26.4), Qwen3.5-4B Q4_0 llama.cpp CPU (39.8), Qwen3.5-4B Q4 MNN CPU (29.6), Qwen3.5-4B Q4 MNN GPU (39.5), Qwen3.5-4B Q8 MNN GPU (32.7)
- **Decode under 5 tokens/s (10):** Qwen3-0.6B Q4_0 llama.cpp GPU (3.49), Qwen3-0.6B Q8_0 llama.cpp GPU (4.35), Qwen3-0.6B F16 llama.cpp GPU (4.5), Qwen3-1.7B Q4_0 llama.cpp GPU (2.58), Qwen3-1.7B Q8_0 llama.cpp GPU (3.24), Qwen3-1.7B F16 llama.cpp GPU (3.09), Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q8 MNN CPU (4.85), Qwen3-4B Q8 MNN GPU (0.71), Qwen3.5-4B Q8 MNN GPU (3.96)
- **Energy over 125 J per request (13):** Gemma3-270M F16 llama.cpp GPU (127 J), SmolLM2-360M Q8_0 llama.cpp GPU (182 J), Qwen3-1.7B F16 llama.cpp CPU (217 J), Qwen3.5-2B F16 llama.cpp CPU (233 J), Qwen3-4B Q4_0 llama.cpp CPU (156 J), Qwen3-4B Q4 MNN CPU (172 J), Qwen3-4B Q4_0 llama.cpp GPU (237 J), Qwen3-4B Q4 MNN GPU (133 J), Qwen3-4B Q8 MNN CPU (259 J), Qwen3-4B Q8 MNN GPU (350 J), Qwen3.5-4B Q4_0 llama.cpp CPU (172 J), Qwen3.5-4B Q4 MNN CPU (152 J), Qwen3.5-4B Q8 MNN GPU (186 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### M04

**Medium · Redmi 9A · Async FAQ answers**: answer a question, delivered like a text message; 128-token prompt, 64-token answer, first token within 30 s, 20 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,024 MB · prefill ≥ 4.27 tokens/s · decode ≥ 1 tokens/s · energy ≤ 104 J per request.

**Pick: Qwen3.5-0.8B Q8_0, llama.cpp, CPU** (910 MB, 4.31 / 2.2 tokens/s, 82.5 J). Kept 18 of 52 configurations (35%); 16 filtered out; 18 cannot run on this phone.

![M04](figures/scenarios/M04.png)

<details><summary>Kept pool (18), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q8_0 llama.cpp CPU **(pick)** | 910 | 4.31 | 2.2 | 82.5 |
| Qwen3.5-0.8B Q4 MNN CPU | 715 | 9.1 | 5.82 | 30.5 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 599 | 4.59 | 2.17 | 68.9 |
| Qwen3-0.6B Q8 MNN CPU | 1,023 | 9.59 | 5.35 | 27.9 |
| Qwen3-0.6B Q4 MNN CPU | 673 | 11.4 | 5.83 | 22.7 |
| SmolLM2-360M Q8 MNN CPU | 509 | 12.7 | 7.55 | 19.3 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 434 | 5.14 | 2.82 | 45.5 |
| SmolLM2-360M Q4 MNN CPU | 355 | 14.8 | 8.58 | 15.8 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 277 | 5.39 | 2.95 | 38.6 |
| Gemma3-270M Q8 MNN CPU | 745 | 35.2 | 6.71 | 13.7 |
| Gemma3-270M Q8_0 llama.cpp CPU | 338 | 11.3 | 4.56 | 25.3 |
| Gemma3-270M Q4 MNN CPU | 326 | 39.9 | 7.72 | 12.4 |
| Gemma3-270M Q4_0 llama.cpp CPU | 288 | 11.7 | 4.65 | 23.4 |
| SmolLM2-135M F16 MNN CPU | 637 | 43 | 8.17 | 12.5 |
| SmolLM2-135M Q8 MNN CPU | 225 | 32.5 | 13.2 | 8.74 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 177 | 11.2 | 5.6 | 20.7 |
| SmolLM2-135M Q4 MNN CPU | 158 | 36.5 | 16.3 | 7.21 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 124 | 11.6 | 5.76 | 18.5 |

</details>

<details><summary>Filtered out (16) and cannot run (18)</summary>

- **Peak memory over 1,024 MB (11):** Gemma3-270M F16 MNN CPU (1,681 MB), SmolLM2-360M F16 MNN CPU (1,614 MB), Qwen3-0.6B F16 llama.cpp CPU (1,612 MB), Qwen3.5-0.8B Q8 MNN CPU (1,241 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,615 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (1,340 MB), Qwen3-1.7B Q4 MNN CPU (1,543 MB), Qwen3-1.7B Q8 MNN CPU (2,357 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (1,307 MB), Qwen3.5-2B Q4 MNN CPU (1,663 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (2,116 MB)
- **Prefill under 4.27 tokens/s (11):** SmolLM2-135M F16 llama.cpp CPU (1.54), Gemma3-270M F16 llama.cpp CPU (1.64), SmolLM2-360M F16 llama.cpp CPU (0.54), Qwen3-0.6B Q4_0 llama.cpp CPU (3.31), Qwen3-0.6B Q8_0 llama.cpp CPU (3.18), Qwen3-0.6B F16 llama.cpp CPU (0.38), Qwen3.5-0.8B F16 llama.cpp CPU (0.33), Qwen3-1.7B Q4_0 llama.cpp CPU (1.78), Qwen3-1.7B Q8 MNN CPU (3.76), Qwen3.5-2B Q4_0 llama.cpp CPU (2.2), Qwen3.5-2B Q8_0 llama.cpp CPU (2.03)
- **Decode under 1 tokens/s (4):** Gemma3-270M F16 llama.cpp CPU (0.59), SmolLM2-360M F16 llama.cpp CPU (0.43), Qwen3-0.6B F16 llama.cpp CPU (0.26), Qwen3.5-0.8B F16 llama.cpp CPU (0.21)
- **Energy over 104 J per request (8):** SmolLM2-135M F16 llama.cpp CPU (111 J), Gemma3-270M F16 llama.cpp CPU (144 J), SmolLM2-360M F16 llama.cpp CPU (299 J), Qwen3-0.6B F16 llama.cpp CPU (448 J), Qwen3.5-0.8B F16 llama.cpp CPU (659 J), Qwen3-1.7B Q4_0 llama.cpp CPU (127 J), Qwen3.5-2B Q4_0 llama.cpp CPU (141 J), Qwen3.5-2B Q8_0 llama.cpp CPU (158 J)
- **Cannot run on this phone (18):** Qwen3-0.6B F16 MNN CPU (does not fit in RAM), Qwen3.5-0.8B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B Q8_0 llama.cpp CPU (crash/error (exit 1)), Qwen3-1.7B F16 llama.cpp CPU (does not fit in RAM), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B Q8 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp CPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q4 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q4 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM)

</details>

### M05

**Medium · Galaxy S23 · Bedtime story**: a story read aloud by text-to-speech; 100-token prompt, 500-token answer, first token within 3 s, 3 requests ≤ 1% of battery a day.

Limits: peak memory ≤ 2,048 MB · prefill ≥ 33.3 tokens/s · decode ≥ 4 tokens/s · energy ≤ 180 J per request.

**Pick: Qwen3.5-2B Q4, MNN, CPU** (1,236 MB, 134 / 34.1 tokens/s, 152 J). Kept 45 of 104 configurations (43%); 37 filtered out; 22 cannot run on this phone.

![M05](figures/scenarios/M05.png)

<details><summary>Kept pool (45), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN CPU **(pick)** | 1,236 | 134 | 34.1 | 152 |
| Qwen3.5-2B Q4 MNN GPU | 1,458 | 252 | 28 | 115 |
| Qwen3-1.7B Q4 MNN CPU | 1,290 | 138 | 31.3 | 152 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 219 | 8.52 | 154 |
| Qwen3.5-0.8B Q8 MNN CPU | 1,345 | 274 | 47 | 101 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,734 | 260 | 41.2 | 114 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,083 | 379 | 35.1 | 91.8 |
| Qwen3.5-0.8B Q4 MNN CPU | 691 | 273 | 73.6 | 63.9 |
| Qwen3.5-0.8B Q4 MNN GPU | 712 | 454 | 46.1 | 55.3 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,310 | 157 | 13.4 | 127 |
| Qwen3-0.6B F16 MNN CPU | 1,721 | 240 | 26.5 | 170 |
| Qwen3-0.6B Q8 MNN CPU | 856 | 368 | 49.8 | 92.5 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,390 | 216 | 47.1 | 97 |
| Qwen3-0.6B Q8 MNN GPU | 1,005 | 540 | 8.45 | 102 |
| Qwen3-0.6B Q4 MNN CPU | 548 | 371 | 72.7 | 64.8 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 832 | 232 | 49.8 | 84.6 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,506 | 133 | 14.9 | 96.7 |
| Qwen3-0.6B Q4 MNN GPU | 696 | 559 | 9.74 | 84.5 |
| SmolLM2-360M F16 MNN CPU | 826 | 405 | 40.8 | 104 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 42.1 | 25.9 | 156 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 57.4 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 348 | 75.2 | 61.6 |
| SmolLM2-360M Q8 MNN GPU | 614 | 735 | 8.56 | 85.4 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 40.6 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 46 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 961 | 219 | 49.5 | 56.3 |
| SmolLM2-360M Q4 MNN GPU | 433 | 763 | 9.09 | 72.5 |
| Gemma3-270M F16 MNN CPU | 1,487 | 1,082 | 56.2 | 78.2 |
| Gemma3-270M F16 llama.cpp CPU | 601 | 127 | 38.4 | 102 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 41.3 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 54.3 |
| Gemma3-270M Q8 MNN GPU | 515 | 1,589 | 8.89 | 71.5 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 35.8 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 41 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,498 | 361 | 33.3 | 47.5 |
| Gemma3-270M Q4 MNN GPU | 379 | 1,622 | 8.98 | 59.1 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 38.8 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 120 | 68.5 | 62 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 26.1 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 26.9 |
| SmolLM2-135M Q8 MNN GPU | 329 | 1,753 | 9.09 | 59.1 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 20.4 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 22 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 36.5 |
| SmolLM2-135M Q4 MNN GPU | 259 | 1,725 | 9.13 | 51.9 |

</details>

<details><summary>Filtered out (37) and cannot run (22)</summary>

- **Peak memory over 2,048 MB (29):** Gemma3-270M F16 llama.cpp GPU (2,486 MB), SmolLM2-360M F16 llama.cpp GPU (2,179 MB), Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,164 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8 MNN CPU (2,397 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B Q8 MNN GPU (2,309 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,522 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,247 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,685 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4 MNN CPU (2,782 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 33.3 tokens/s (14):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B F16 llama.cpp CPU (9.4)
- **Energy over 180 J per request (25):** Qwen3-0.6B F16 llama.cpp CPU (230 J), Qwen3-0.6B F16 llama.cpp GPU (199 J), Qwen3.5-0.8B F16 llama.cpp CPU (282 J), Qwen3.5-0.8B F16 MNN CPU (194 J), Qwen3.5-0.8B F16 llama.cpp GPU (201 J), Qwen3-1.7B Q8_0 llama.cpp CPU (215 J), Qwen3-1.7B Q8 MNN CPU (203 J), Qwen3-1.7B Q8_0 llama.cpp GPU (331 J), Qwen3-1.7B Q8 MNN GPU (220 J), Qwen3-1.7B F16 llama.cpp CPU (596 J), Qwen3-1.7B F16 llama.cpp GPU (516 J), Qwen3.5-2B Q4_0 llama.cpp CPU (190 J), Qwen3.5-2B Q8_0 llama.cpp CPU (230 J), Qwen3.5-2B Q8 MNN CPU (213 J), Qwen3.5-2B Q8_0 llama.cpp GPU (243 J), Qwen3.5-2B Q8 MNN GPU (197 J), Qwen3.5-2B F16 llama.cpp CPU (635 J), Qwen3-4B Q4_0 llama.cpp CPU (352 J), Qwen3-4B Q4 MNN CPU (312 J), Qwen3-4B Q4_0 llama.cpp GPU (337 J), Qwen3-4B Q4 MNN GPU (318 J), Qwen3.5-4B Q4_0 llama.cpp CPU (392 J), Qwen3.5-4B Q4 MNN CPU (301 J), Qwen3.5-4B Q4_0 llama.cpp GPU (379 J), Qwen3.5-4B Q4 MNN GPU (284 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### M06

**Medium · OnePlus 15 · Interactive chat**: a general chat assistant; 256-token prompt, 256-token answer, first token within 2 s, 50 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 2,048 MB · prefill ≥ 128 tokens/s · decode ≥ 10 tokens/s · energy ≤ 60.7 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,470 MB, 669 / 43.2 tokens/s, 27.1 J). Kept 48 of 104 configurations (46%); 45 filtered out; 11 cannot run on this phone.

![M06](figures/scenarios/M06.png)

<details><summary>Kept pool (48), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,470 | 669 | 43.2 | 27.1 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 28.4 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 34.9 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 14.6 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 19.3 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 20.2 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 10.4 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 11.2 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 16.2 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 17.2 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 16.1 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 14.8 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 19.9 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 24 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 10.8 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 13.7 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 18.5 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 16.9 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 16.8 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 8.72 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 9.62 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 14.3 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 16.9 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 7.2 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 6.68 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 11.8 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 14.5 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 13.6 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 11.5 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 5.78 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 7.37 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 10.1 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 13.4 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 4.48 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 5.38 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 9.43 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 11.6 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 5 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 9.08 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 8.35 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 3.65 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 4.56 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 9.13 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 9.84 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 3.4 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 2.84 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 8.47 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 9.32 |

</details>

<details><summary>Filtered out (45) and cannot run (11)</summary>

- **Peak memory over 2,048 MB (41):** Gemma3-270M F16 llama.cpp GPU (2,534 MB), SmolLM2-360M F16 llama.cpp GPU (2,225 MB), Qwen3-0.6B F16 MNN CPU (2,081 MB), Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,273 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B Q8 MNN GPU (2,479 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8 MNN CPU (2,206 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 128 tokens/s (17):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3-1.7B Q4 MNN CPU (103), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3.5-2B F16 MNN CPU (113), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6)
- **Decode under 10 tokens/s (2):** Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42)
- **Energy over 60.7 J per request (21):** Qwen3-1.7B F16 llama.cpp CPU (119 J), Qwen3-1.7B F16 MNN CPU (73.2 J), Qwen3-1.7B F16 llama.cpp GPU (76.6 J), Qwen3.5-2B F16 llama.cpp CPU (117 J), Qwen3.5-2B F16 MNN CPU (77.1 J), Qwen3.5-2B F16 llama.cpp GPU (79.4 J), Qwen3-4B Q4_0 llama.cpp CPU (80 J), Qwen3-4B Q4 MNN CPU (67.2 J), Qwen3-4B Q4_0 llama.cpp GPU (84 J), Qwen3-4B Q4 MNN GPU (79.7 J), Qwen3-4B Q8_0 llama.cpp CPU (97.3 J), Qwen3-4B Q8_0 llama.cpp GPU (121 J), Qwen3-4B Q8 MNN GPU (124 J), Qwen3-4B F16 llama.cpp CPU (280 J), Qwen3-4B F16 llama.cpp GPU (181 J), Qwen3.5-4B Q4_0 llama.cpp CPU (85.6 J), Qwen3.5-4B Q4_0 llama.cpp GPU (78.3 J), Qwen3.5-4B Q4 MNN GPU (63 J), Qwen3.5-4B Q8 MNN CPU (102 J), Qwen3.5-4B Q8_0 llama.cpp GPU (111 J), Qwen3.5-4B Q8 MNN GPU (114 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### M07

**Medium · OnePlus 8 Pro · Field data-entry helper**: fill forms from notes on a low-cost phone; 128-token prompt, 64-token answer, first token within 10 s, 30 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,024 MB · prefill ≥ 12.8 tokens/s · decode ≥ 2 tokens/s · energy ≤ 62.6 J per request.

**Pick: Qwen3.5-0.8B Q8, MNN, CPU** (926 MB, 182 / 26 tokens/s, 18.9 J). Kept 34 of 104 configurations (33%); 45 filtered out; 25 cannot run on this phone.

![M07](figures/scenarios/M07.png)

<details><summary>Kept pool (34), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q8 MNN CPU **(pick)** | 926 | 182 | 26 | 18.9 |
| Qwen3.5-0.8B Q8 MNN GPU | 959 | 182 | 17.9 | 14.5 |
| Qwen3.5-0.8B Q4 MNN CPU | 550 | 167 | 45.9 | 12.3 |
| Qwen3.5-0.8B Q4 MNN GPU | 583 | 207 | 24.1 | 8.86 |
| Qwen3-0.6B Q8 MNN CPU | 962 | 239 | 27.8 | 17.8 |
| Qwen3-0.6B Q8 MNN GPU | 844 | 213 | 8.89 | 16.1 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 261 | 48.2 | 12.8 |
| Qwen3-0.6B Q4 MNN CPU | 537 | 227 | 42.8 | 12.1 |
| Qwen3-0.6B Q4 MNN GPU | 546 | 214 | 9.15 | 11.9 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 161 | 30.3 | 21 |
| SmolLM2-360M F16 MNN CPU | 803 | 280 | 25.9 | 18.1 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 825 | 377 | 51.2 | 10.7 |
| SmolLM2-360M Q8 MNN CPU | 492 | 342 | 44.6 | 11.2 |
| SmolLM2-360M Q8 MNN GPU | 514 | 298 | 9.25 | 11.6 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 7.98 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 7.95 |
| SmolLM2-360M Q4 MNN GPU | 333 | 299 | 9.3 | 9.64 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 901 | 58 | 9.02 | 14.5 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 484 | 42.8 | 12.7 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,013 | 70.4 | 7.31 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 9.08 |
| Gemma3-270M Q8 MNN GPU | 430 | 736 | 8.35 | 9.3 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 1,015 | 84.3 | 5.94 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 6.57 |
| Gemma3-270M Q4 MNN GPU | 295 | 746 | 8.27 | 8.13 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 8.37 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 7.35 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 4.69 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 4.79 |
| SmolLM2-135M Q8 MNN GPU | 253 | 743 | 9.84 | 6.81 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 3.6 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 3.44 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 681 | 105 | 13.3 | 9.54 |
| SmolLM2-135M Q4 MNN GPU | 186 | 743 | 9.88 | 6.28 |

</details>

<details><summary>Filtered out (45) and cannot run (25)</summary>

- **Peak memory over 1,024 MB (43):** Gemma3-270M Q4_0 llama.cpp GPU (1,429 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,478 MB), Gemma3-270M F16 MNN CPU (1,435 MB), Gemma3-270M F16 llama.cpp GPU (1,886 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,216 MB), SmolLM2-360M F16 llama.cpp GPU (1,389 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,315 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,440 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,908 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,811 MB), Qwen3-0.6B F16 llama.cpp GPU (2,344 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,114 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,642 MB), Qwen3.5-0.8B F16 MNN CPU (2,906 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,270 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (2,753 MB), Qwen3-1.7B Q4 MNN GPU (1,211 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,678 MB), Qwen3-1.7B Q8 MNN CPU (2,416 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,474 MB), Qwen3-1.7B Q8 MNN GPU (2,072 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,218 MB), Qwen3.5-2B Q4 MNN GPU (1,259 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,974 MB), Qwen3.5-2B Q8 MNN GPU (2,221 MB), Qwen3.5-2B F16 llama.cpp CPU (3,908 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,560 MB), Qwen3-4B Q4 MNN CPU (2,755 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q4 MNN GPU (2,580 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3-4B Q8 MNN GPU (4,179 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q4 MNN CPU (2,875 MB), Qwen3.5-4B Q4 MNN GPU (2,655 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 12.8 tokens/s (11):** SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3-4B Q4_0 llama.cpp GPU (10.9)
- **Decode under 2 tokens/s (2):** Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q8 MNN GPU (0.71)
- **Energy over 62.6 J per request (11):** SmolLM2-360M Q8_0 llama.cpp GPU (77.8 J), Qwen3-1.7B F16 llama.cpp CPU (92.6 J), Qwen3.5-2B F16 llama.cpp CPU (99.5 J), Qwen3-4B Q4_0 llama.cpp CPU (66.4 J), Qwen3-4B Q4 MNN CPU (73.3 J), Qwen3-4B Q4_0 llama.cpp GPU (101 J), Qwen3-4B Q8 MNN CPU (111 J), Qwen3-4B Q8 MNN GPU (149 J), Qwen3.5-4B Q4_0 llama.cpp CPU (73.2 J), Qwen3.5-4B Q4 MNN CPU (65 J), Qwen3.5-4B Q8 MNN GPU (79.4 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### M08

**Medium · Redmi 9A · Overnight photo captioning**: caption the photo library while charging; 200-token prompt, 40-token answer, first token within 30 s, 300 requests ≤ 20% of battery a day.

Limits: peak memory ≤ 1,900 MB · prefill ≥ 6.67 tokens/s · decode ≥ 1 tokens/s · energy ≤ 46.3 J per request.

**Pick: Qwen3.5-0.8B Q8, MNN, CPU** (1,241 MB, 7.75 / 4.65 tokens/s, 41 J). Kept 17 of 52 configurations (33%); 17 filtered out; 18 cannot run on this phone.

![M08](figures/scenarios/M08.png)

<details><summary>Kept pool (17), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q8 MNN CPU **(pick)** | 1,241 | 7.75 | 4.65 | 41 |
| Qwen3.5-0.8B Q4 MNN CPU | 715 | 9.1 | 5.82 | 33.5 |
| Qwen3-0.6B Q8 MNN CPU | 1,023 | 9.59 | 5.35 | 29.3 |
| Qwen3-0.6B Q4 MNN CPU | 673 | 11.4 | 5.83 | 24 |
| SmolLM2-360M F16 MNN CPU | 1,614 | 15.9 | 4.1 | 27.5 |
| SmolLM2-360M Q8 MNN CPU | 509 | 12.7 | 7.55 | 21 |
| SmolLM2-360M Q4 MNN CPU | 355 | 14.8 | 8.58 | 17.2 |
| Gemma3-270M F16 MNN CPU | 1,681 | 44.9 | 4.2 | 16.9 |
| Gemma3-270M Q8 MNN CPU | 745 | 35.2 | 6.71 | 11.9 |
| Gemma3-270M Q8_0 llama.cpp CPU | 338 | 11.3 | 4.56 | 25 |
| Gemma3-270M Q4 MNN CPU | 326 | 39.9 | 7.72 | 10.7 |
| Gemma3-270M Q4_0 llama.cpp CPU | 288 | 11.7 | 4.65 | 23.9 |
| SmolLM2-135M F16 MNN CPU | 637 | 43 | 8.17 | 10.5 |
| SmolLM2-135M Q8 MNN CPU | 225 | 32.5 | 13.2 | 8.93 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 177 | 11.2 | 5.6 | 22.1 |
| SmolLM2-135M Q4 MNN CPU | 158 | 36.5 | 16.3 | 7.36 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 124 | 11.6 | 5.76 | 20.5 |

</details>

<details><summary>Filtered out (17) and cannot run (18)</summary>

- **Peak memory over 1,900 MB (2):** Qwen3-1.7B Q8 MNN CPU (2,357 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (2,116 MB)
- **Prefill under 6.67 tokens/s (17):** SmolLM2-135M F16 llama.cpp CPU (1.54), Gemma3-270M F16 llama.cpp CPU (1.64), SmolLM2-360M Q4_0 llama.cpp CPU (5.39), SmolLM2-360M Q8_0 llama.cpp CPU (5.14), SmolLM2-360M F16 llama.cpp CPU (0.54), Qwen3-0.6B Q4_0 llama.cpp CPU (3.31), Qwen3-0.6B Q8_0 llama.cpp CPU (3.18), Qwen3-0.6B F16 llama.cpp CPU (0.38), Qwen3.5-0.8B Q4_0 llama.cpp CPU (4.59), Qwen3.5-0.8B Q8_0 llama.cpp CPU (4.31), Qwen3.5-0.8B F16 llama.cpp CPU (0.33), Qwen3-1.7B Q4_0 llama.cpp CPU (1.78), Qwen3-1.7B Q4 MNN CPU (4.75), Qwen3-1.7B Q8 MNN CPU (3.76), Qwen3.5-2B Q4_0 llama.cpp CPU (2.2), Qwen3.5-2B Q4 MNN CPU (4.58), Qwen3.5-2B Q8_0 llama.cpp CPU (2.03)
- **Decode under 1 tokens/s (4):** Gemma3-270M F16 llama.cpp CPU (0.59), SmolLM2-360M F16 llama.cpp CPU (0.43), Qwen3-0.6B F16 llama.cpp CPU (0.26), Qwen3.5-0.8B F16 llama.cpp CPU (0.21)
- **Energy over 46.3 J per request (16):** SmolLM2-135M F16 llama.cpp CPU (129 J), Gemma3-270M F16 llama.cpp CPU (144 J), SmolLM2-360M Q8_0 llama.cpp CPU (50 J), SmolLM2-360M F16 llama.cpp CPU (355 J), Qwen3-0.6B Q4_0 llama.cpp CPU (70 J), Qwen3-0.6B Q8_0 llama.cpp CPU (80.9 J), Qwen3-0.6B F16 llama.cpp CPU (516 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (73.5 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (89.6 J), Qwen3.5-0.8B F16 llama.cpp CPU (746 J), Qwen3-1.7B Q4_0 llama.cpp CPU (143 J), Qwen3-1.7B Q4 MNN CPU (59.4 J), Qwen3-1.7B Q8 MNN CPU (90.6 J), Qwen3.5-2B Q4_0 llama.cpp CPU (155 J), Qwen3.5-2B Q4 MNN CPU (69.5 J), Qwen3.5-2B Q8_0 llama.cpp CPU (172 J)
- **Cannot run on this phone (18):** Qwen3-0.6B F16 MNN CPU (does not fit in RAM), Qwen3.5-0.8B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B Q8_0 llama.cpp CPU (crash/error (exit 1)), Qwen3-1.7B F16 llama.cpp CPU (does not fit in RAM), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B Q8 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp CPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q4 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q4 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM)

</details>

### M09

**Medium · Galaxy S23 · Health-worker checklist**: guide an offline checklist on a budget phone; 256-token prompt, 128-token answer, first token within 20 s, 20 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,536 MB · prefill ≥ 12.8 tokens/s · decode ≥ 2 tokens/s · energy ≤ 81 J per request.

**Pick: Qwen3.5-2B Q4, MNN, CPU** (1,236 MB, 134 / 34.1 tokens/s, 49.8 J). Kept 45 of 104 configurations (43%); 37 filtered out; 22 cannot run on this phone.

![M09](figures/scenarios/M09.png)

<details><summary>Kept pool (45), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN CPU **(pick)** | 1,236 | 134 | 34.1 | 49.8 |
| Qwen3.5-2B Q4 MNN GPU | 1,458 | 252 | 28 | 32.9 |
| Qwen3-1.7B Q4 MNN CPU | 1,290 | 138 | 31.3 | 49.1 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 219 | 8.52 | 45 |
| Qwen3.5-0.8B Q8 MNN CPU | 1,345 | 274 | 47 | 31 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,083 | 379 | 35.1 | 25.9 |
| Qwen3.5-0.8B Q4 MNN CPU | 691 | 273 | 73.6 | 21.5 |
| Qwen3.5-0.8B Q4 MNN GPU | 712 | 454 | 46.1 | 15.9 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,310 | 157 | 13.4 | 37.6 |
| Qwen3-0.6B Q8 MNN CPU | 856 | 368 | 49.8 | 27.8 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,390 | 216 | 47.1 | 31.5 |
| Qwen3-0.6B Q8 MNN GPU | 1,005 | 540 | 8.45 | 28.1 |
| Qwen3-0.6B Q4 MNN CPU | 548 | 371 | 72.7 | 20.4 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 832 | 232 | 49.8 | 28 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,506 | 133 | 14.9 | 31 |
| Qwen3-0.6B Q4 MNN GPU | 696 | 559 | 9.74 | 23.5 |
| SmolLM2-360M F16 MNN CPU | 826 | 405 | 40.8 | 30 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 42.1 | 25.9 | 70 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 17.5 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 348 | 75.2 | 20.2 |
| SmolLM2-360M Q8 MNN GPU | 614 | 735 | 8.56 | 23.1 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 13.1 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 15.8 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 961 | 219 | 49.5 | 18.4 |
| SmolLM2-360M Q4 MNN GPU | 433 | 763 | 9.09 | 19.8 |
| Gemma3-270M F16 MNN CPU | 1,487 | 1,082 | 56.2 | 21.4 |
| Gemma3-270M F16 llama.cpp CPU | 601 | 127 | 38.4 | 36.6 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 12.7 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 15 |
| Gemma3-270M Q8 MNN GPU | 515 | 1,589 | 8.89 | 18.8 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 11.1 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 11.6 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,498 | 361 | 33.3 | 14.5 |
| Gemma3-270M Q4 MNN GPU | 379 | 1,622 | 8.98 | 15.6 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 11.4 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 120 | 68.5 | 26.8 |
| SmolLM2-135M F16 llama.cpp GPU | 1,221 | 23.3 | 64.7 | 52.4 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 7.82 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 8.76 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 821 | 25.8 | 67.3 | 43.9 |
| SmolLM2-135M Q8 MNN GPU | 329 | 1,753 | 9.09 | 15.6 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 6.31 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 7.45 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 11.3 |
| SmolLM2-135M Q4 MNN GPU | 259 | 1,725 | 9.13 | 13.7 |

</details>

<details><summary>Filtered out (37) and cannot run (22)</summary>

- **Peak memory over 1,536 MB (35):** Gemma3-270M Q8_0 llama.cpp GPU (1,546 MB), Gemma3-270M F16 llama.cpp GPU (2,486 MB), SmolLM2-360M F16 llama.cpp GPU (2,179 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,970 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,721 MB), Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,734 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,164 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8 MNN CPU (2,397 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B Q8 MNN GPU (2,309 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,522 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,247 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,685 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4 MNN CPU (2,782 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 12.8 tokens/s (8):** SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B F16 llama.cpp CPU (9.4)
- **Energy over 81 J per request (18):** SmolLM2-360M Q8_0 llama.cpp GPU (115 J), SmolLM2-360M F16 llama.cpp GPU (137 J), Qwen3-0.6B Q8_0 llama.cpp GPU (163 J), Qwen3-0.6B F16 llama.cpp CPU (99.5 J), Qwen3-0.6B F16 llama.cpp GPU (204 J), Qwen3.5-0.8B F16 llama.cpp CPU (115 J), Qwen3-1.7B Q8_0 llama.cpp GPU (421 J), Qwen3-1.7B F16 llama.cpp CPU (279 J), Qwen3-1.7B F16 llama.cpp GPU (634 J), Qwen3.5-2B F16 llama.cpp CPU (276 J), Qwen3-4B Q4_0 llama.cpp CPU (121 J), Qwen3-4B Q4 MNN CPU (106 J), Qwen3-4B Q4_0 llama.cpp GPU (111 J), Qwen3-4B Q4 MNN GPU (97.2 J), Qwen3.5-4B Q4_0 llama.cpp CPU (129 J), Qwen3.5-4B Q4 MNN CPU (103 J), Qwen3.5-4B Q4_0 llama.cpp GPU (117 J), Qwen3.5-4B Q4 MNN GPU (82 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### M10

**Medium · OnePlus 15 · Email summary on open**: summarize an email when it is opened; 1,000-token prompt, 100-token answer, first token within 4 s, 30 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 2,048 MB · prefill ≥ 250 tokens/s · decode ≥ 8 tokens/s · energy ≤ 67.4 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,470 MB, 669 / 43.2 tokens/s, 15.4 J). Kept 47 of 104 configurations (45%); 46 filtered out; 11 cannot run on this phone.

![M10](figures/scenarios/M10.png)

<details><summary>Kept pool (47), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,470 | 669 | 43.2 | 15.4 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 22.3 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 10.8 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 13.9 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 11.9 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 8.49 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 7.04 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 9.44 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 12.8 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 12.6 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 11.4 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 12 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 12.5 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 9.34 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 11 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 9.26 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 9.42 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 13 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 6.96 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 7.6 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 8.4 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 8.78 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 6.37 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 5.69 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 6.85 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 7.95 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 12.6 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 6.64 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 4 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 4.41 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 5.37 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 6 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 3.35 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 3.36 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 4.86 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 5.34 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 3.73 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 11.4 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 4.17 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 2.66 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 3.22 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 4.82 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 4.66 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 2.5 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 2.19 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 4.3 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 4.45 |

</details>

<details><summary>Filtered out (46) and cannot run (11)</summary>

- **Peak memory over 2,048 MB (41):** Gemma3-270M F16 llama.cpp GPU (2,534 MB), SmolLM2-360M F16 llama.cpp GPU (2,225 MB), Qwen3-0.6B F16 MNN CPU (2,081 MB), Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,273 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B Q8 MNN GPU (2,479 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8 MNN CPU (2,206 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 250 tokens/s (32):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3-0.6B F16 MNN CPU (184), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3.5-0.8B F16 MNN CPU (250), Qwen3-1.7B Q4_0 llama.cpp CPU (144), Qwen3-1.7B Q4 MNN CPU (103), Qwen3-1.7B Q8_0 llama.cpp CPU (150), Qwen3-1.7B Q8 MNN CPU (136), Qwen3-1.7B Q8_0 llama.cpp GPU (164), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3-1.7B F16 llama.cpp GPU (225), Qwen3.5-2B Q4_0 llama.cpp CPU (150), Qwen3.5-2B Q4 MNN CPU (158), Qwen3.5-2B Q8_0 llama.cpp CPU (159), Qwen3.5-2B Q8 MNN CPU (169), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3.5-2B F16 MNN CPU (113), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q4 MNN GPU (191), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B Q8 MNN GPU (180), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6), Qwen3.5-4B Q8_0 llama.cpp GPU (240), Qwen3.5-4B Q8 MNN GPU (204)
- **Decode under 8 tokens/s (2):** Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42)
- **Energy over 67.4 J per request (11):** Qwen3-1.7B F16 llama.cpp CPU (162 J), Qwen3.5-2B F16 llama.cpp CPU (142 J), Qwen3-4B Q4_0 llama.cpp CPU (68.3 J), Qwen3-4B Q4 MNN CPU (79.9 J), Qwen3-4B Q8_0 llama.cpp CPU (81.4 J), Qwen3-4B Q8_0 llama.cpp GPU (87.9 J), Qwen3-4B Q8 MNN GPU (78.5 J), Qwen3-4B F16 llama.cpp CPU (382 J), Qwen3-4B F16 llama.cpp GPU (109 J), Qwen3.5-4B Q4_0 llama.cpp CPU (68.5 J), Qwen3.5-4B Q8 MNN CPU (101 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### M11

**Medium · OnePlus 8 Pro · Language tutor**: conversational practice with corrections; 300-token prompt, 100-token answer, first token within 2 s, 40 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,536 MB · prefill ≥ 150 tokens/s · decode ≥ 6 tokens/s · energy ≤ 47 J per request.

**Pick: Qwen3.5-0.8B Q8, MNN, CPU** (926 MB, 182 / 26 tokens/s, 32.5 J). Kept 35 of 104 configurations (34%); 44 filtered out; 25 cannot run on this phone.

![M11](figures/scenarios/M11.png)

<details><summary>Kept pool (35), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q8 MNN CPU **(pick)** | 926 | 182 | 26 | 32.5 |
| Qwen3.5-0.8B Q8 MNN GPU | 959 | 182 | 17.9 | 24 |
| Qwen3.5-0.8B Q4 MNN CPU | 550 | 167 | 45.9 | 22 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,114 | 207 | 34.8 | 29.1 |
| Qwen3.5-0.8B Q4 MNN GPU | 583 | 207 | 24.1 | 14.8 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,440 | 259 | 30.3 | 28.6 |
| Qwen3-0.6B Q8 MNN CPU | 962 | 239 | 27.8 | 30.3 |
| Qwen3-0.6B Q8 MNN GPU | 844 | 213 | 8.89 | 26.4 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 261 | 48.2 | 22.2 |
| Qwen3-0.6B Q4 MNN CPU | 537 | 227 | 42.8 | 21.2 |
| Qwen3-0.6B Q4 MNN GPU | 546 | 214 | 9.15 | 19.8 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 161 | 30.3 | 37.8 |
| SmolLM2-360M F16 MNN CPU | 803 | 280 | 25.9 | 30.5 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 825 | 377 | 51.2 | 18.6 |
| SmolLM2-360M Q8 MNN CPU | 492 | 342 | 44.6 | 19.1 |
| SmolLM2-360M Q8 MNN GPU | 514 | 298 | 9.25 | 19.1 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 14 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 14 |
| SmolLM2-360M Q4 MNN GPU | 333 | 299 | 9.3 | 15.9 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 484 | 42.8 | 21.6 |
| Gemma3-270M F16 MNN CPU | 1,435 | 755 | 31.1 | 22.2 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,013 | 70.4 | 12.1 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 14.8 |
| Gemma3-270M Q8 MNN GPU | 430 | 736 | 8.35 | 14.8 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 1,015 | 84.3 | 9.87 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 10.8 |
| Gemma3-270M Q4 MNN GPU | 295 | 746 | 8.27 | 13 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 14.9 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 12.3 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 8.01 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 8.08 |
| SmolLM2-135M Q8 MNN GPU | 253 | 743 | 9.84 | 11 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 6.25 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 5.94 |
| SmolLM2-135M Q4 MNN GPU | 186 | 743 | 9.88 | 10.1 |

</details>

<details><summary>Filtered out (44) and cannot run (25)</summary>

- **Peak memory over 1,536 MB (31):** Gemma3-270M F16 llama.cpp GPU (1,886 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,908 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,811 MB), Qwen3-0.6B F16 llama.cpp GPU (2,344 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,642 MB), Qwen3.5-0.8B F16 MNN CPU (2,906 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (2,753 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,678 MB), Qwen3-1.7B Q8 MNN CPU (2,416 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,474 MB), Qwen3-1.7B Q8 MNN GPU (2,072 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,974 MB), Qwen3.5-2B Q8 MNN GPU (2,221 MB), Qwen3.5-2B F16 llama.cpp CPU (3,908 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,560 MB), Qwen3-4B Q4 MNN CPU (2,755 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q4 MNN GPU (2,580 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3-4B Q8 MNN GPU (4,179 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q4 MNN CPU (2,875 MB), Qwen3.5-4B Q4 MNN GPU (2,655 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 150 tokens/s (41):** SmolLM2-135M Q4_0 llama.cpp GPU (105), SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q4_0 llama.cpp GPU (77.8), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q4_0 llama.cpp GPU (58), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q4_0 llama.cpp GPU (35.1), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B F16 llama.cpp CPU (99.5), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3.5-0.8B F16 llama.cpp CPU (80.2), Qwen3-1.7B Q4_0 llama.cpp CPU (99.3), Qwen3-1.7B Q4 MNN CPU (81.8), Qwen3-1.7B Q4_0 llama.cpp GPU (28.9), Qwen3-1.7B Q4 MNN GPU (77.3), Qwen3-1.7B Q8_0 llama.cpp CPU (96.8), Qwen3-1.7B Q8 MNN CPU (83.2), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B Q8 MNN GPU (73.8), Qwen3-1.7B F16 llama.cpp CPU (27.4), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3.5-2B Q4_0 llama.cpp CPU (98.8), Qwen3.5-2B Q4 MNN CPU (77.9), Qwen3.5-2B Q4 MNN GPU (102), Qwen3.5-2B Q8_0 llama.cpp CPU (94), Qwen3.5-2B Q8 MNN CPU (81.6), Qwen3.5-2B Q8 MNN GPU (83.7), Qwen3.5-2B F16 llama.cpp CPU (28.2), Qwen3-4B Q4_0 llama.cpp CPU (41.1), Qwen3-4B Q4 MNN CPU (32.1), Qwen3-4B Q4_0 llama.cpp GPU (10.9), Qwen3-4B Q4 MNN GPU (31.1), Qwen3-4B Q8 MNN CPU (31), Qwen3-4B Q8 MNN GPU (26.4), Qwen3.5-4B Q4_0 llama.cpp CPU (39.8), Qwen3.5-4B Q4 MNN CPU (29.6), Qwen3.5-4B Q4 MNN GPU (39.5), Qwen3.5-4B Q8 MNN GPU (32.7)
- **Decode under 6 tokens/s (10):** Qwen3-0.6B Q4_0 llama.cpp GPU (3.49), Qwen3-0.6B Q8_0 llama.cpp GPU (4.35), Qwen3-0.6B F16 llama.cpp GPU (4.5), Qwen3-1.7B Q4_0 llama.cpp GPU (2.58), Qwen3-1.7B Q8_0 llama.cpp GPU (3.24), Qwen3-1.7B F16 llama.cpp GPU (3.09), Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q8 MNN CPU (4.85), Qwen3-4B Q8 MNN GPU (0.71), Qwen3.5-4B Q8 MNN GPU (3.96)
- **Energy over 47 J per request (33):** SmolLM2-135M Q8_0 llama.cpp GPU (62.9 J), SmolLM2-135M F16 llama.cpp GPU (115 J), Gemma3-270M Q8_0 llama.cpp GPU (70.3 J), Gemma3-270M F16 llama.cpp GPU (114 J), SmolLM2-360M Q8_0 llama.cpp GPU (170 J), Qwen3-0.6B Q4_0 llama.cpp GPU (50.6 J), Qwen3-0.6B F16 llama.cpp CPU (60 J), Qwen3-0.6B F16 MNN CPU (48.2 J), Qwen3.5-0.8B F16 llama.cpp CPU (74.2 J), Qwen3.5-0.8B F16 MNN CPU (55.8 J), Qwen3-1.7B Q4_0 llama.cpp CPU (52.1 J), Qwen3-1.7B Q4 MNN CPU (56.1 J), Qwen3-1.7B Q4_0 llama.cpp GPU (79.1 J), Qwen3-1.7B Q8_0 llama.cpp CPU (78.9 J), Qwen3-1.7B Q8 MNN CPU (81.3 J), Qwen3-1.7B Q8 MNN GPU (63.1 J), Qwen3-1.7B F16 llama.cpp CPU (168 J), Qwen3.5-2B Q4_0 llama.cpp CPU (58.1 J), Qwen3.5-2B Q4 MNN CPU (50.9 J), Qwen3.5-2B Q8_0 llama.cpp CPU (86.1 J), Qwen3.5-2B Q8 MNN CPU (76.5 J), Qwen3.5-2B Q8 MNN GPU (56 J), Qwen3.5-2B F16 llama.cpp CPU (178 J), Qwen3-4B Q4_0 llama.cpp CPU (118 J), Qwen3-4B Q4 MNN CPU (132 J), Qwen3-4B Q4_0 llama.cpp GPU (178 J), Qwen3-4B Q4 MNN GPU (99 J), Qwen3-4B Q8 MNN CPU (192 J), Qwen3-4B Q8 MNN GPU (244 J), Qwen3.5-4B Q4_0 llama.cpp CPU (129 J), Qwen3.5-4B Q4 MNN CPU (117 J), Qwen3.5-4B Q4 MNN GPU (81.4 J), Qwen3.5-4B Q8 MNN GPU (132 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### M12

**Medium · OnePlus 15 · Recipe / how-to helper**: step-by-step instructions; 150-token prompt, 300-token answer, first token within 5 s, 10 requests ≤ 1% of battery a day.

Limits: peak memory ≤ 2,048 MB · prefill ≥ 30 tokens/s · decode ≥ 5 tokens/s · energy ≤ 101 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,470 MB, 669 / 43.2 tokens/s, 31 J). Kept 52 of 104 configurations (50%); 41 filtered out; 11 cannot run on this phone.

![M12](figures/scenarios/M12.png)

<details><summary>Kept pool (52), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,470 | 669 | 43.2 | 31 |
| Qwen3.5-2B Q4 MNN CPU | 1,512 | 158 | 37.2 | 31 |
| Qwen3-1.7B Q4 MNN CPU | 1,286 | 103 | 36.8 | 28.6 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 39.4 |
| Qwen3.5-0.8B F16 llama.cpp CPU | 1,641 | 61 | 32.2 | 50.6 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 16.3 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 21.5 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 23 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 11.5 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 12.6 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 18.4 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 19.1 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 67.2 | 38.4 | 42.7 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 17.8 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 16.4 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 22.6 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 27.6 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 11.8 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 15.1 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 21.3 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 19.3 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 18.6 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 95.5 | 50.3 | 25.2 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 9.63 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 10.6 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 16.3 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 19.5 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 7.84 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 7.32 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 13.5 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 16.6 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 14.7 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 13.2 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 6.48 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 8.38 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 11.7 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 15.6 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 4.99 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 6.09 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 10.9 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 13.5 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 5.56 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 9.33 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 9.64 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 4.08 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 5.1 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 10.5 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 11.4 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 3.79 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 3.15 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 9.77 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 10.8 |

</details>

<details><summary>Filtered out (41) and cannot run (11)</summary>

- **Peak memory over 2,048 MB (41):** Gemma3-270M F16 llama.cpp GPU (2,534 MB), SmolLM2-360M F16 llama.cpp GPU (2,225 MB), Qwen3-0.6B F16 MNN CPU (2,081 MB), Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,273 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B Q8 MNN GPU (2,479 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8 MNN CPU (2,206 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 30 tokens/s (3):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 101 J per request (10):** Qwen3-1.7B F16 llama.cpp CPU (120 J), Qwen3.5-2B F16 llama.cpp CPU (122 J), Qwen3-4B Q8_0 llama.cpp CPU (107 J), Qwen3-4B Q8_0 llama.cpp GPU (135 J), Qwen3-4B Q8 MNN GPU (140 J), Qwen3-4B F16 llama.cpp CPU (282 J), Qwen3-4B F16 llama.cpp GPU (206 J), Qwen3.5-4B Q8 MNN CPU (109 J), Qwen3.5-4B Q8_0 llama.cpp GPU (126 J), Qwen3.5-4B Q8 MNN GPU (130 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### M13

**Medium · Galaxy S23 · Journaling prompts**: a reflective question after a journal entry; 300-token prompt, 150-token answer, first token within 5 s, 5 requests ≤ 1% of battery a day.

Limits: peak memory ≤ 1,536 MB · prefill ≥ 60 tokens/s · decode ≥ 5 tokens/s · energy ≤ 108 J per request.

**Pick: Qwen3.5-2B Q4, MNN, CPU** (1,236 MB, 134 / 34.1 tokens/s, 58.4 J). Kept 42 of 104 configurations (40%); 40 filtered out; 22 cannot run on this phone.

![M13](figures/scenarios/M13.png)

<details><summary>Kept pool (42), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN CPU **(pick)** | 1,236 | 134 | 34.1 | 58.4 |
| Qwen3.5-2B Q4 MNN GPU | 1,458 | 252 | 28 | 38.6 |
| Qwen3-1.7B Q4 MNN CPU | 1,290 | 138 | 31.3 | 57.5 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 219 | 8.52 | 52.8 |
| Qwen3.5-0.8B Q8 MNN CPU | 1,345 | 274 | 47 | 36.4 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,083 | 379 | 35.1 | 30.3 |
| Qwen3.5-0.8B Q4 MNN CPU | 691 | 273 | 73.6 | 25.2 |
| Qwen3.5-0.8B Q4 MNN GPU | 712 | 454 | 46.1 | 18.6 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,310 | 157 | 13.4 | 44.1 |
| Qwen3-0.6B Q8 MNN CPU | 856 | 368 | 49.8 | 32.5 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,390 | 216 | 47.1 | 36.9 |
| Qwen3-0.6B Q8 MNN GPU | 1,005 | 540 | 8.45 | 32.9 |
| Qwen3-0.6B Q4 MNN CPU | 548 | 371 | 72.7 | 23.9 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 832 | 232 | 49.8 | 32.8 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,506 | 133 | 14.9 | 36.4 |
| Qwen3-0.6B Q4 MNN GPU | 696 | 559 | 9.74 | 27.5 |
| SmolLM2-360M F16 MNN CPU | 826 | 405 | 40.8 | 35.2 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 20.5 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 348 | 75.2 | 23.7 |
| SmolLM2-360M Q8 MNN GPU | 614 | 735 | 8.56 | 27 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 15.3 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 18.6 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 961 | 219 | 49.5 | 21.5 |
| SmolLM2-360M Q4 MNN GPU | 433 | 763 | 9.09 | 23.2 |
| Gemma3-270M F16 MNN CPU | 1,487 | 1,082 | 56.2 | 25.1 |
| Gemma3-270M F16 llama.cpp CPU | 601 | 127 | 38.4 | 42.9 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 14.8 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 17.6 |
| Gemma3-270M Q8 MNN GPU | 515 | 1,589 | 8.89 | 22 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 13 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 13.5 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,498 | 361 | 33.3 | 17 |
| Gemma3-270M Q4 MNN GPU | 379 | 1,622 | 8.98 | 18.3 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 13.3 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 120 | 68.5 | 31.4 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 9.16 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 10.3 |
| SmolLM2-135M Q8 MNN GPU | 329 | 1,753 | 9.09 | 18.2 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 7.4 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 8.74 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 13.2 |
| SmolLM2-135M Q4 MNN GPU | 259 | 1,725 | 9.13 | 16.1 |

</details>

<details><summary>Filtered out (40) and cannot run (22)</summary>

- **Peak memory over 1,536 MB (35):** Gemma3-270M Q8_0 llama.cpp GPU (1,546 MB), Gemma3-270M F16 llama.cpp GPU (2,486 MB), SmolLM2-360M F16 llama.cpp GPU (2,179 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,970 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,721 MB), Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,734 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,164 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8 MNN CPU (2,397 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B Q8 MNN GPU (2,309 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,522 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,247 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,685 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4 MNN CPU (2,782 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 60 tokens/s (21):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp CPU (42.1), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B F16 llama.cpp CPU (9.4), Qwen3-4B Q4_0 llama.cpp CPU (44.5), Qwen3-4B Q4 MNN CPU (53.5), Qwen3-4B Q4_0 llama.cpp GPU (40.4), Qwen3.5-4B Q4_0 llama.cpp CPU (50.8), Qwen3.5-4B Q4 MNN CPU (49.9), Qwen3.5-4B Q4_0 llama.cpp GPU (47.5)
- **Energy over 108 J per request (17):** SmolLM2-360M Q8_0 llama.cpp GPU (135 J), SmolLM2-360M F16 llama.cpp GPU (160 J), Qwen3-0.6B Q8_0 llama.cpp GPU (191 J), Qwen3-0.6B F16 llama.cpp CPU (117 J), Qwen3-0.6B F16 llama.cpp GPU (239 J), Qwen3.5-0.8B F16 llama.cpp CPU (134 J), Qwen3-1.7B Q8_0 llama.cpp GPU (494 J), Qwen3-1.7B F16 llama.cpp CPU (326 J), Qwen3-1.7B F16 llama.cpp GPU (744 J), Qwen3.5-2B F16 llama.cpp CPU (323 J), Qwen3-4B Q4_0 llama.cpp CPU (141 J), Qwen3-4B Q4 MNN CPU (124 J), Qwen3-4B Q4_0 llama.cpp GPU (130 J), Qwen3-4B Q4 MNN GPU (114 J), Qwen3.5-4B Q4_0 llama.cpp CPU (151 J), Qwen3.5-4B Q4 MNN CPU (121 J), Qwen3.5-4B Q4_0 llama.cpp GPU (137 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### M14

**Medium · OnePlus 8 Pro · Bedtime story**: a story read aloud by text-to-speech; 100-token prompt, 500-token answer, first token within 3 s, 3 requests ≤ 1% of battery a day.

Limits: peak memory ≤ 2,048 MB · prefill ≥ 33.3 tokens/s · decode ≥ 4 tokens/s · energy ≤ 209 J per request.

**Pick: Qwen3.5-2B Q4, MNN, CPU** (1,218 MB, 77.9 / 20.7 tokens/s, 168 J). Kept 45 of 104 configurations (43%); 34 filtered out; 25 cannot run on this phone.

![M14](figures/scenarios/M14.png)

<details><summary>Kept pool (45), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN CPU **(pick)** | 1,218 | 77.9 | 20.7 | 168 |
| Qwen3.5-2B Q4 MNN GPU | 1,259 | 102 | 14.2 | 137 |
| Qwen3-1.7B Q4 MNN CPU | 1,270 | 81.8 | 19.1 | 184 |
| Qwen3-1.7B Q4 MNN GPU | 1,211 | 77.3 | 9.06 | 166 |
| Qwen3.5-0.8B Q8 MNN CPU | 926 | 182 | 26 | 121 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 204 | 23.1 | 149 |
| Qwen3.5-0.8B Q8 MNN GPU | 959 | 182 | 17.9 | 101 |
| Qwen3.5-0.8B Q4 MNN CPU | 550 | 167 | 45.9 | 71 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,114 | 207 | 34.8 | 106 |
| Qwen3.5-0.8B Q4 MNN GPU | 583 | 207 | 24.1 | 60.4 |
| Qwen3-0.6B F16 llama.cpp CPU | 1,626 | 99.5 | 18.3 | 197 |
| Qwen3-0.6B F16 MNN CPU | 1,811 | 168 | 16.5 | 195 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,440 | 259 | 30.3 | 109 |
| Qwen3-0.6B Q8 MNN CPU | 962 | 239 | 27.8 | 118 |
| Qwen3-0.6B Q8 MNN GPU | 844 | 213 | 8.89 | 114 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 261 | 48.2 | 80.6 |
| Qwen3-0.6B Q4 MNN CPU | 537 | 227 | 42.8 | 74.5 |
| Qwen3-0.6B Q4 MNN GPU | 546 | 214 | 9.15 | 82.6 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 161 | 30.3 | 118 |
| SmolLM2-360M F16 MNN CPU | 803 | 280 | 25.9 | 122 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 825 | 377 | 51.2 | 68 |
| SmolLM2-360M Q8 MNN CPU | 492 | 342 | 44.6 | 73 |
| SmolLM2-360M Q8 MNN GPU | 514 | 298 | 9.25 | 83 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 48.8 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 48.3 |
| SmolLM2-360M Q4 MNN GPU | 333 | 299 | 9.3 | 67.8 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 901 | 58 | 9.02 | 86.1 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 484 | 42.8 | 83.1 |
| Gemma3-270M F16 MNN CPU | 1,435 | 755 | 31.1 | 99.9 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,013 | 70.4 | 51.2 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 65.4 |
| Gemma3-270M Q8 MNN GPU | 430 | 736 | 8.35 | 69.9 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 1,015 | 84.3 | 41.1 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 46.2 |
| Gemma3-270M Q4 MNN GPU | 295 | 746 | 8.27 | 60.7 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,429 | 77.8 | 7.09 | 94.2 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 48.8 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 50.4 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 30.6 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 32 |
| SmolLM2-135M Q8 MNN GPU | 253 | 743 | 9.84 | 50.4 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 22.6 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 21.7 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 681 | 105 | 13.3 | 59.8 |
| SmolLM2-135M Q4 MNN GPU | 186 | 743 | 9.88 | 46.4 |

</details>

<details><summary>Filtered out (34) and cannot run (25)</summary>

- **Peak memory over 2,048 MB (25):** Qwen3-0.6B F16 llama.cpp GPU (2,344 MB), Qwen3.5-0.8B F16 MNN CPU (2,906 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (2,753 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,678 MB), Qwen3-1.7B Q8 MNN CPU (2,416 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,474 MB), Qwen3-1.7B Q8 MNN GPU (2,072 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,974 MB), Qwen3.5-2B Q8 MNN GPU (2,221 MB), Qwen3.5-2B F16 llama.cpp CPU (3,908 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,560 MB), Qwen3-4B Q4 MNN CPU (2,755 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q4 MNN GPU (2,580 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3-4B Q8 MNN GPU (4,179 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q4 MNN CPU (2,875 MB), Qwen3.5-4B Q4 MNN GPU (2,655 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 33.3 tokens/s (20):** SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3-1.7B Q4_0 llama.cpp GPU (28.9), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B F16 llama.cpp CPU (27.4), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3.5-2B F16 llama.cpp CPU (28.2), Qwen3-4B Q4 MNN CPU (32.1), Qwen3-4B Q4_0 llama.cpp GPU (10.9), Qwen3-4B Q4 MNN GPU (31.1), Qwen3-4B Q8 MNN CPU (31), Qwen3-4B Q8 MNN GPU (26.4), Qwen3.5-4B Q4 MNN CPU (29.6), Qwen3.5-4B Q8 MNN GPU (32.7)
- **Decode under 4 tokens/s (7):** Qwen3-0.6B Q4_0 llama.cpp GPU (3.49), Qwen3-1.7B Q4_0 llama.cpp GPU (2.58), Qwen3-1.7B Q8_0 llama.cpp GPU (3.24), Qwen3-1.7B F16 llama.cpp GPU (3.09), Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q8 MNN GPU (0.71), Qwen3.5-4B Q8 MNN GPU (3.96)
- **Energy over 209 J per request (22):** Qwen3.5-0.8B F16 llama.cpp CPU (252 J), Qwen3.5-0.8B F16 MNN CPU (226 J), Qwen3-1.7B Q4_0 llama.cpp GPU (297 J), Qwen3-1.7B Q8_0 llama.cpp CPU (289 J), Qwen3-1.7B Q8 MNN CPU (301 J), Qwen3-1.7B Q8 MNN GPU (253 J), Qwen3-1.7B F16 llama.cpp CPU (516 J), Qwen3.5-2B Q4_0 llama.cpp CPU (210 J), Qwen3.5-2B Q8_0 llama.cpp CPU (322 J), Qwen3.5-2B Q8 MNN CPU (290 J), Qwen3.5-2B Q8 MNN GPU (234 J), Qwen3.5-2B F16 llama.cpp CPU (574 J), Qwen3-4B Q4_0 llama.cpp CPU (391 J), Qwen3-4B Q4 MNN CPU (412 J), Qwen3-4B Q4_0 llama.cpp GPU (607 J), Qwen3-4B Q4 MNN GPU (355 J), Qwen3-4B Q8 MNN CPU (692 J), Qwen3-4B Q8 MNN GPU (1,072 J), Qwen3.5-4B Q4_0 llama.cpp CPU (439 J), Qwen3.5-4B Q4 MNN CPU (374 J), Qwen3.5-4B Q4 MNN GPU (322 J), Qwen3.5-4B Q8 MNN GPU (544 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### M15

**Medium · OnePlus 15 · Notes Q&A (RAG)**: answer from retrieved personal notes; 1,500-token prompt, 200-token answer, first token within 5 s, 20 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 2,560 MB · prefill ≥ 300 tokens/s · decode ≥ 10 tokens/s · energy ≤ 101 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,470 MB, 669 / 43.2 tokens/s, 28.1 J). Kept 49 of 104 configurations (47%); 44 filtered out; 11 cannot run on this phone.

![M15](figures/scenarios/M15.png)

<details><summary>Kept pool (49), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,470 | 669 | 43.2 | 28.1 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 54.7 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 39.8 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 18.8 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 316 | 59.6 | 24.3 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 22.3 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 21.6 |
| Qwen3.5-0.8B Q4 MNN CPU | 568 | 337 | 80 | 14.5 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 12.6 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 17.1 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,115 | 307 | 60.1 | 22.2 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,291 | 315 | 67.8 | 21.7 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 19.7 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 21.6 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 23.3 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 15.8 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 309 | 72 | 18.8 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 17.4 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 17.3 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 18.9 |
| SmolLM2-360M F16 MNN CPU | 1,035 | 326 | 58.4 | 22.4 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 11.9 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 13.1 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 15.2 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 16.4 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 10.8 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 9.66 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 12.5 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 14.6 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 12.1 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 12.9 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 7.03 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 7.97 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 9.95 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 11.6 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 5.81 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 6.02 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 9.07 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 10.2 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 6.47 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 7.84 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 4.64 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 5.64 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 8.95 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 8.86 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 4.35 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 3.78 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 8.05 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 8.45 |

</details>

<details><summary>Filtered out (44) and cannot run (11)</summary>

- **Peak memory over 2,560 MB (33):** Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 300 tokens/s (36):** SmolLM2-135M F16 llama.cpp CPU (260), Gemma3-270M F16 llama.cpp CPU (282), SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3-0.6B F16 MNN CPU (184), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3.5-0.8B F16 MNN CPU (250), Qwen3-1.7B Q4_0 llama.cpp CPU (144), Qwen3-1.7B Q4 MNN CPU (103), Qwen3-1.7B Q8_0 llama.cpp CPU (150), Qwen3-1.7B Q8 MNN CPU (136), Qwen3-1.7B Q8_0 llama.cpp GPU (164), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3-1.7B F16 llama.cpp GPU (225), Qwen3.5-2B Q4_0 llama.cpp CPU (150), Qwen3.5-2B Q4 MNN CPU (158), Qwen3.5-2B Q8_0 llama.cpp CPU (159), Qwen3.5-2B Q8 MNN CPU (169), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3.5-2B F16 MNN CPU (113), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q4 MNN GPU (191), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B Q8 MNN GPU (180), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q4_0 llama.cpp GPU (281), Qwen3.5-4B Q4 MNN GPU (275), Qwen3.5-4B Q8 MNN CPU (67.6), Qwen3.5-4B Q8_0 llama.cpp GPU (240), Qwen3.5-4B Q8 MNN GPU (204)
- **Decode under 10 tokens/s (2):** Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42)
- **Energy over 101 J per request (15):** Qwen3-1.7B F16 llama.cpp CPU (260 J), Qwen3-1.7B F16 MNN CPU (105 J), Qwen3.5-2B F16 llama.cpp CPU (230 J), Qwen3-4B Q4_0 llama.cpp CPU (116 J), Qwen3-4B Q4 MNN CPU (130 J), Qwen3-4B Q4 MNN GPU (103 J), Qwen3-4B Q8_0 llama.cpp CPU (139 J), Qwen3-4B Q8_0 llama.cpp GPU (153 J), Qwen3-4B Q8 MNN GPU (140 J), Qwen3-4B F16 llama.cpp CPU (613 J), Qwen3-4B F16 llama.cpp GPU (197 J), Qwen3.5-4B Q4_0 llama.cpp CPU (118 J), Qwen3.5-4B Q8 MNN CPU (169 J), Qwen3.5-4B Q8_0 llama.cpp GPU (117 J), Qwen3.5-4B Q8 MNN GPU (119 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### M16

**Medium · Galaxy S23 · Offline translator app**: a dedicated translation app for travel; 100-token prompt, 100-token answer, first token within 3 s, 50 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 2,800 MB · prefill ≥ 33.3 tokens/s · decode ≥ 5 tokens/s · energy ≤ 32.4 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,458 MB, 252 / 28 tokens/s, 24.2 J). Kept 41 of 104 configurations (39%); 41 filtered out; 22 cannot run on this phone.

![M16](figures/scenarios/M16.png)

<details><summary>Kept pool (41), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,458 | 252 | 28 | 24.2 |
| Qwen3.5-0.8B Q8 MNN CPU | 1,345 | 274 | 47 | 22 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,734 | 260 | 41.2 | 25 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,083 | 379 | 35.1 | 19.2 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,164 | 163 | 15.2 | 30.5 |
| Qwen3.5-0.8B Q4 MNN CPU | 691 | 273 | 73.6 | 14.6 |
| Qwen3.5-0.8B Q4 MNN GPU | 712 | 454 | 46.1 | 11.7 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,310 | 157 | 13.4 | 27.2 |
| Qwen3-0.6B Q8 MNN CPU | 856 | 368 | 49.8 | 19.9 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,390 | 216 | 47.1 | 21.7 |
| Qwen3-0.6B Q8 MNN GPU | 1,005 | 540 | 8.45 | 21.1 |
| Qwen3-0.6B Q4 MNN CPU | 548 | 371 | 72.7 | 14.3 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 832 | 232 | 49.8 | 19.1 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,506 | 133 | 14.9 | 21.5 |
| Qwen3-0.6B Q4 MNN GPU | 696 | 559 | 9.74 | 17.5 |
| SmolLM2-360M F16 MNN CPU | 826 | 405 | 40.8 | 22 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 12.4 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 348 | 75.2 | 13.9 |
| SmolLM2-360M Q8 MNN GPU | 614 | 735 | 8.56 | 17.5 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 9.05 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 10.6 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 961 | 219 | 49.5 | 12.6 |
| SmolLM2-360M Q4 MNN GPU | 433 | 763 | 9.09 | 14.9 |
| Gemma3-270M F16 MNN CPU | 1,487 | 1,082 | 56.2 | 16.1 |
| Gemma3-270M F16 llama.cpp CPU | 601 | 127 | 38.4 | 24.1 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 8.99 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 11.2 |
| Gemma3-270M Q8 MNN GPU | 515 | 1,589 | 8.89 | 14.5 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 7.82 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 8.58 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,498 | 361 | 33.3 | 10.3 |
| Gemma3-270M Q4 MNN GPU | 379 | 1,622 | 8.98 | 12 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 8.26 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 120 | 68.5 | 16.2 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 5.61 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 6.03 |
| SmolLM2-135M Q8 MNN GPU | 329 | 1,753 | 9.09 | 12 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 4.46 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 5.03 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 7.98 |
| SmolLM2-135M Q4 MNN GPU | 259 | 1,725 | 9.13 | 10.5 |

</details>

<details><summary>Filtered out (41) and cannot run (22)</summary>

- **Peak memory over 2,800 MB (18):** Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 33.3 tokens/s (14):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B F16 llama.cpp CPU (9.4)
- **Energy over 32.4 J per request (36):** SmolLM2-360M Q8_0 llama.cpp GPU (52.2 J), SmolLM2-360M F16 llama.cpp CPU (41.6 J), SmolLM2-360M F16 llama.cpp GPU (62.6 J), Qwen3-0.6B Q8_0 llama.cpp GPU (73.1 J), Qwen3-0.6B F16 llama.cpp CPU (60.1 J), Qwen3-0.6B F16 MNN CPU (36 J), Qwen3-0.6B F16 llama.cpp GPU (92.8 J), Qwen3.5-0.8B F16 llama.cpp CPU (71.1 J), Qwen3.5-0.8B F16 MNN CPU (41.1 J), Qwen3.5-0.8B F16 llama.cpp GPU (42.3 J), Qwen3-1.7B Q4_0 llama.cpp CPU (37.9 J), Qwen3-1.7B Q4 MNN CPU (34 J), Qwen3-1.7B Q4_0 llama.cpp GPU (38.6 J), Qwen3-1.7B Q4 MNN GPU (32.7 J), Qwen3-1.7B Q8_0 llama.cpp CPU (48 J), Qwen3-1.7B Q8 MNN CPU (44.3 J), Qwen3-1.7B Q8_0 llama.cpp GPU (183 J), Qwen3-1.7B Q8 MNN GPU (46.3 J), Qwen3-1.7B F16 llama.cpp CPU (163 J), Qwen3-1.7B F16 llama.cpp GPU (278 J), Qwen3.5-2B Q4_0 llama.cpp CPU (41.9 J), Qwen3.5-2B Q4 MNN CPU (34.2 J), Qwen3.5-2B Q4_0 llama.cpp GPU (38.3 J), Qwen3.5-2B Q8_0 llama.cpp CPU (50.9 J), Qwen3.5-2B Q8 MNN CPU (46.6 J), Qwen3.5-2B Q8_0 llama.cpp GPU (51.4 J), Qwen3.5-2B Q8 MNN GPU (41.2 J), Qwen3.5-2B F16 llama.cpp CPU (166 J), Qwen3-4B Q4_0 llama.cpp CPU (80.9 J), Qwen3-4B Q4 MNN CPU (71.3 J), Qwen3-4B Q4_0 llama.cpp GPU (76.1 J), Qwen3-4B Q4 MNN GPU (69.1 J), Qwen3.5-4B Q4_0 llama.cpp CPU (88.2 J), Qwen3.5-4B Q4 MNN CPU (69.3 J), Qwen3.5-4B Q4_0 llama.cpp GPU (82.6 J), Qwen3.5-4B Q4 MNN GPU (60.1 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### M17

**Medium · OnePlus 8 Pro · Study tutor app**: explain homework questions; 300-token prompt, 100-token answer, first token within 5 s, 40 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 4,600 MB · prefill ≥ 60 tokens/s · decode ≥ 6 tokens/s · energy ≤ 47 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,259 MB, 102 / 14.2 tokens/s, 33.6 J). Kept 40 of 104 configurations (38%); 39 filtered out; 25 cannot run on this phone.

![M17](figures/scenarios/M17.png)

<details><summary>Kept pool (40), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,259 | 102 | 14.2 | 33.6 |
| Qwen3-1.7B Q4 MNN GPU | 1,211 | 77.3 | 9.06 | 43.4 |
| Qwen3.5-0.8B Q8 MNN CPU | 926 | 182 | 26 | 32.5 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,735 | 204 | 23.1 | 39.2 |
| Qwen3.5-0.8B Q8 MNN GPU | 959 | 182 | 17.9 | 24 |
| Qwen3.5-0.8B Q4 MNN CPU | 550 | 167 | 45.9 | 22 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,114 | 207 | 34.8 | 29.1 |
| Qwen3.5-0.8B Q4 MNN GPU | 583 | 207 | 24.1 | 14.8 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,440 | 259 | 30.3 | 28.6 |
| Qwen3-0.6B Q8 MNN CPU | 962 | 239 | 27.8 | 30.3 |
| Qwen3-0.6B Q8 MNN GPU | 844 | 213 | 8.89 | 26.4 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 261 | 48.2 | 22.2 |
| Qwen3-0.6B Q4 MNN CPU | 537 | 227 | 42.8 | 21.2 |
| Qwen3-0.6B Q4 MNN GPU | 546 | 214 | 9.15 | 19.8 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 161 | 30.3 | 37.8 |
| SmolLM2-360M F16 MNN CPU | 803 | 280 | 25.9 | 30.5 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 825 | 377 | 51.2 | 18.6 |
| SmolLM2-360M Q8 MNN CPU | 492 | 342 | 44.6 | 19.1 |
| SmolLM2-360M Q8 MNN GPU | 514 | 298 | 9.25 | 19.1 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 14 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 14 |
| SmolLM2-360M Q4 MNN GPU | 333 | 299 | 9.3 | 15.9 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 484 | 42.8 | 21.6 |
| Gemma3-270M F16 MNN CPU | 1,435 | 755 | 31.1 | 22.2 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,013 | 70.4 | 12.1 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 14.8 |
| Gemma3-270M Q8 MNN GPU | 430 | 736 | 8.35 | 14.8 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 1,015 | 84.3 | 9.87 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 10.8 |
| Gemma3-270M Q4 MNN GPU | 295 | 746 | 8.27 | 13 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,429 | 77.8 | 7.09 | 24.5 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 14.9 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 12.3 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 8.01 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 8.08 |
| SmolLM2-135M Q8 MNN GPU | 253 | 743 | 9.84 | 11 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 6.25 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 5.94 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 681 | 105 | 13.3 | 16.5 |
| SmolLM2-135M Q4 MNN GPU | 186 | 743 | 9.88 | 10.1 |

</details>

<details><summary>Filtered out (39) and cannot run (25)</summary>

- **Peak memory over 4,600 MB (5):** Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 60 tokens/s (25):** SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q4_0 llama.cpp GPU (58), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q4_0 llama.cpp GPU (35.1), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3-1.7B Q4_0 llama.cpp GPU (28.9), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B F16 llama.cpp CPU (27.4), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3.5-2B F16 llama.cpp CPU (28.2), Qwen3-4B Q4_0 llama.cpp CPU (41.1), Qwen3-4B Q4 MNN CPU (32.1), Qwen3-4B Q4_0 llama.cpp GPU (10.9), Qwen3-4B Q4 MNN GPU (31.1), Qwen3-4B Q8 MNN CPU (31), Qwen3-4B Q8 MNN GPU (26.4), Qwen3.5-4B Q4_0 llama.cpp CPU (39.8), Qwen3.5-4B Q4 MNN CPU (29.6), Qwen3.5-4B Q4 MNN GPU (39.5), Qwen3.5-4B Q8 MNN GPU (32.7)
- **Decode under 6 tokens/s (10):** Qwen3-0.6B Q4_0 llama.cpp GPU (3.49), Qwen3-0.6B Q8_0 llama.cpp GPU (4.35), Qwen3-0.6B F16 llama.cpp GPU (4.5), Qwen3-1.7B Q4_0 llama.cpp GPU (2.58), Qwen3-1.7B Q8_0 llama.cpp GPU (3.24), Qwen3-1.7B F16 llama.cpp GPU (3.09), Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q8 MNN CPU (4.85), Qwen3-4B Q8 MNN GPU (0.71), Qwen3.5-4B Q8 MNN GPU (3.96)
- **Energy over 47 J per request (33):** SmolLM2-135M Q8_0 llama.cpp GPU (62.9 J), SmolLM2-135M F16 llama.cpp GPU (115 J), Gemma3-270M Q8_0 llama.cpp GPU (70.3 J), Gemma3-270M F16 llama.cpp GPU (114 J), SmolLM2-360M Q8_0 llama.cpp GPU (170 J), Qwen3-0.6B Q4_0 llama.cpp GPU (50.6 J), Qwen3-0.6B F16 llama.cpp CPU (60 J), Qwen3-0.6B F16 MNN CPU (48.2 J), Qwen3.5-0.8B F16 llama.cpp CPU (74.2 J), Qwen3.5-0.8B F16 MNN CPU (55.8 J), Qwen3-1.7B Q4_0 llama.cpp CPU (52.1 J), Qwen3-1.7B Q4 MNN CPU (56.1 J), Qwen3-1.7B Q4_0 llama.cpp GPU (79.1 J), Qwen3-1.7B Q8_0 llama.cpp CPU (78.9 J), Qwen3-1.7B Q8 MNN CPU (81.3 J), Qwen3-1.7B Q8 MNN GPU (63.1 J), Qwen3-1.7B F16 llama.cpp CPU (168 J), Qwen3.5-2B Q4_0 llama.cpp CPU (58.1 J), Qwen3.5-2B Q4 MNN CPU (50.9 J), Qwen3.5-2B Q8_0 llama.cpp CPU (86.1 J), Qwen3.5-2B Q8 MNN CPU (76.5 J), Qwen3.5-2B Q8 MNN GPU (56 J), Qwen3.5-2B F16 llama.cpp CPU (178 J), Qwen3-4B Q4_0 llama.cpp CPU (118 J), Qwen3-4B Q4 MNN CPU (132 J), Qwen3-4B Q4_0 llama.cpp GPU (178 J), Qwen3-4B Q4 MNN GPU (99 J), Qwen3-4B Q8 MNN CPU (192 J), Qwen3-4B Q8 MNN GPU (244 J), Qwen3.5-4B Q4_0 llama.cpp CPU (129 J), Qwen3.5-4B Q4 MNN CPU (117 J), Qwen3.5-4B Q4 MNN GPU (81.4 J), Qwen3.5-4B Q8 MNN GPU (132 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### M18

**Medium · OnePlus 15 · Fluent premium chat**: a chat assistant that streams fast; 512-token prompt, 256-token answer, first token within 1.5 s, 50 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 3,072 MB · prefill ≥ 341 tokens/s · decode ≥ 20 tokens/s · energy ≤ 60.7 J per request.

**Pick: Qwen3.5-2B Q8, MNN, GPU** (2,810 MB, 458 / 26 tokens/s, 51.4 J). Kept 45 of 104 configurations (43%); 48 filtered out; 11 cannot run on this phone.

![M18](figures/scenarios/M18.png)

<details><summary>Kept pool (45), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q8 MNN GPU **(pick)** | 2,810 | 458 | 26 | 51.4 |
| Qwen3.5-2B Q4 MNN GPU | 1,470 | 669 | 43.2 | 28.5 |
| Qwen3.5-2B Q4_0 llama.cpp GPU | 2,980 | 641 | 38.1 | 38.4 |
| Qwen3-1.7B Q8 MNN GPU | 2,479 | 443 | 22.8 | 54.3 |
| Qwen3-1.7B Q4 MNN GPU | 1,443 | 486 | 25.6 | 37.4 |
| Qwen3.5-0.8B Q8 MNN CPU | 940 | 356 | 69.2 | 16.1 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,273 | 1,041 | 53 | 24.4 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,108 | 982 | 52.6 | 21.3 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 11.9 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,883 | 1,120 | 61.4 | 17.1 |
| Qwen3-0.6B Q8 MNN CPU | 1,029 | 424 | 66.4 | 16.4 |
| Qwen3-0.6B Q8_0 llama.cpp GPU | 2,017 | 472 | 57.6 | 21.1 |
| Qwen3-0.6B Q8 MNN GPU | 1,002 | 1,178 | 25.6 | 24.9 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 12.3 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,552 | 1,680 | 61.7 | 19 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 17.7 |
| SmolLM2-360M F16 llama.cpp GPU | 2,225 | 901 | 59 | 20 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 9.73 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 472 | 98.4 | 10.7 |
| SmolLM2-360M Q8_0 llama.cpp GPU | 1,326 | 679 | 79.6 | 15.1 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 17.5 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 8.21 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 7.56 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 1,009 | 1,830 | 85.6 | 12.5 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 15.1 |
| Gemma3-270M F16 MNN CPU | 1,528 | 861 | 67.9 | 12.1 |
| Gemma3-270M F16 llama.cpp GPU | 2,534 | 1,888 | 55.8 | 14.5 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 6.28 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 7.81 |
| Gemma3-270M Q8_0 llama.cpp GPU | 1,591 | 1,551 | 84.6 | 10.5 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 13.6 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 4.94 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 5.73 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,538 | 2,875 | 73.2 | 9.77 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 11.8 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 5.5 |
| SmolLM2-135M F16 llama.cpp GPU | 1,265 | 2,430 | 97.5 | 8.61 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 4 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 4.97 |
| SmolLM2-135M Q8_0 llama.cpp GPU | 846 | 1,896 | 99.3 | 9.49 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 10.1 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 3.74 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 3.15 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 791 | 3,634 | 93.2 | 8.76 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 9.55 |

</details>

<details><summary>Filtered out (48) and cannot run (11)</summary>

- **Peak memory over 3,072 MB (25):** Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 341 tokens/s (43):** SmolLM2-135M F16 llama.cpp CPU (260), Gemma3-270M F16 llama.cpp CPU (282), SmolLM2-360M F16 llama.cpp CPU (95.5), SmolLM2-360M F16 MNN CPU (326), Qwen3-0.6B Q4_0 llama.cpp CPU (309), Qwen3-0.6B Q8_0 llama.cpp CPU (315), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3-0.6B F16 MNN CPU (184), Qwen3.5-0.8B Q4_0 llama.cpp CPU (307), Qwen3.5-0.8B Q4 MNN CPU (337), Qwen3.5-0.8B Q8_0 llama.cpp CPU (316), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3.5-0.8B F16 MNN CPU (250), Qwen3-1.7B Q4_0 llama.cpp CPU (144), Qwen3-1.7B Q4 MNN CPU (103), Qwen3-1.7B Q8_0 llama.cpp CPU (150), Qwen3-1.7B Q8 MNN CPU (136), Qwen3-1.7B Q8_0 llama.cpp GPU (164), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3-1.7B F16 llama.cpp GPU (225), Qwen3.5-2B Q4_0 llama.cpp CPU (150), Qwen3.5-2B Q4 MNN CPU (158), Qwen3.5-2B Q8_0 llama.cpp CPU (159), Qwen3.5-2B Q8 MNN CPU (169), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3.5-2B F16 MNN CPU (113), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q4_0 llama.cpp GPU (315), Qwen3-4B Q4 MNN GPU (191), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B Q8 MNN GPU (180), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q4_0 llama.cpp GPU (281), Qwen3.5-4B Q4 MNN GPU (275), Qwen3.5-4B Q8 MNN CPU (67.6), Qwen3.5-4B Q8_0 llama.cpp GPU (240), Qwen3.5-4B Q8 MNN GPU (204)
- **Decode under 20 tokens/s (21):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (13.6), Qwen3-1.7B F16 llama.cpp GPU (17.1), Qwen3.5-2B F16 llama.cpp CPU (14.7), Qwen3.5-2B F16 MNN CPU (14.1), Qwen3.5-2B F16 llama.cpp GPU (16.4), Qwen3-4B Q4_0 llama.cpp CPU (16.3), Qwen3-4B Q4 MNN CPU (16.8), Qwen3-4B Q4_0 llama.cpp GPU (16.7), Qwen3-4B Q4 MNN GPU (19.7), Qwen3-4B Q8_0 llama.cpp CPU (13.8), Qwen3-4B Q8_0 llama.cpp GPU (12.7), Qwen3-4B Q8 MNN GPU (12.6), Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42), Qwen3.5-4B Q4_0 llama.cpp CPU (15.9), Qwen3.5-4B Q4 MNN CPU (16.9), Qwen3.5-4B Q4_0 llama.cpp GPU (14.5), Qwen3.5-4B Q8 MNN CPU (14.2), Qwen3.5-4B Q8_0 llama.cpp GPU (12.6), Qwen3.5-4B Q8 MNN GPU (13.4)
- **Energy over 60.7 J per request (22):** Qwen3-1.7B F16 llama.cpp CPU (152 J), Qwen3-1.7B F16 MNN CPU (82.6 J), Qwen3-1.7B F16 llama.cpp GPU (80.3 J), Qwen3.5-2B F16 llama.cpp CPU (145 J), Qwen3.5-2B F16 MNN CPU (82.7 J), Qwen3.5-2B F16 llama.cpp GPU (81.4 J), Qwen3-4B Q4_0 llama.cpp CPU (90.5 J), Qwen3-4B Q4 MNN CPU (82.5 J), Qwen3-4B Q4_0 llama.cpp GPU (88.8 J), Qwen3-4B Q4 MNN GPU (87.7 J), Qwen3-4B Q8_0 llama.cpp CPU (110 J), Qwen3-4B Q8_0 llama.cpp GPU (133 J), Qwen3-4B Q8 MNN GPU (132 J), Qwen3-4B F16 llama.cpp CPU (357 J), Qwen3-4B F16 llama.cpp GPU (192 J), Qwen3.5-4B Q4_0 llama.cpp CPU (95.6 J), Qwen3.5-4B Q4 MNN CPU (69.2 J), Qwen3.5-4B Q4_0 llama.cpp GPU (83.8 J), Qwen3.5-4B Q4 MNN GPU (66.8 J), Qwen3.5-4B Q8 MNN CPU (119 J), Qwen3.5-4B Q8_0 llama.cpp GPU (117 J), Qwen3.5-4B Q8 MNN GPU (120 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### M19

**Medium · Galaxy S23 · Document Q&A app**: ask questions about an opened PDF; 1,000-token prompt, 200-token answer, first token within 10 s, 20 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 2,800 MB · prefill ≥ 100 tokens/s · decode ≥ 8 tokens/s · energy ≤ 81 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,458 MB, 252 / 28 tokens/s, 60.7 J). Kept 40 of 104 configurations (38%); 42 filtered out; 22 cannot run on this phone.

![M19](figures/scenarios/M19.png)

<details><summary>Kept pool (40), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,458 | 252 | 28 | 60.7 |
| Qwen3.5-0.8B Q8 MNN CPU | 1,345 | 274 | 47 | 62.2 |
| Qwen3.5-0.8B Q8_0 llama.cpp CPU | 1,734 | 260 | 41.2 | 71 |
| Qwen3.5-0.8B Q8 MNN GPU | 1,083 | 379 | 35.1 | 46.6 |
| Qwen3.5-0.8B Q8_0 llama.cpp GPU | 2,164 | 163 | 15.2 | 78.4 |
| Qwen3.5-0.8B Q4 MNN CPU | 691 | 273 | 73.6 | 47 |
| Qwen3.5-0.8B Q4 MNN GPU | 712 | 454 | 46.1 | 29.3 |
| Qwen3.5-0.8B Q4_0 llama.cpp GPU | 1,310 | 157 | 13.4 | 72.1 |
| Qwen3-0.6B Q8 MNN CPU | 856 | 368 | 49.8 | 54 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,390 | 216 | 47.1 | 66.6 |
| Qwen3-0.6B Q8 MNN GPU | 1,005 | 540 | 8.45 | 48.9 |
| Qwen3-0.6B Q4 MNN CPU | 548 | 371 | 72.7 | 41.9 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 832 | 232 | 49.8 | 60.2 |
| Qwen3-0.6B Q4_0 llama.cpp GPU | 1,506 | 133 | 14.9 | 64.8 |
| Qwen3-0.6B Q4 MNN GPU | 696 | 559 | 9.74 | 41.5 |
| SmolLM2-360M F16 MNN CPU | 826 | 405 | 40.8 | 56.1 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 34.5 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 348 | 75.2 | 43.3 |
| SmolLM2-360M Q8 MNN GPU | 614 | 735 | 8.56 | 39.2 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 27.5 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 35.3 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 961 | 219 | 49.5 | 39 |
| SmolLM2-360M Q4 MNN GPU | 433 | 763 | 9.09 | 34 |
| Gemma3-270M F16 MNN CPU | 1,487 | 1,082 | 56.2 | 37 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 25.2 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 26.3 |
| Gemma3-270M Q8 MNN GPU | 515 | 1,589 | 8.89 | 30.5 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 22.3 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 20.8 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,498 | 361 | 33.3 | 28.6 |
| Gemma3-270M Q4 MNN GPU | 379 | 1,622 | 8.98 | 25.5 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 21.5 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 120 | 68.5 | 70.3 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 15.2 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 18.6 |
| SmolLM2-135M Q8 MNN GPU | 329 | 1,753 | 9.09 | 25.4 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 12.7 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 16.4 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 22.7 |
| SmolLM2-135M Q4 MNN GPU | 259 | 1,725 | 9.13 | 22.5 |

</details>

<details><summary>Filtered out (42) and cannot run (22)</summary>

- **Peak memory over 2,800 MB (18):** Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 100 tokens/s (22):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp CPU (42.1), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B F16 llama.cpp CPU (9.4), Qwen3-4B Q4_0 llama.cpp CPU (44.5), Qwen3-4B Q4 MNN CPU (53.5), Qwen3-4B Q4_0 llama.cpp GPU (40.4), Qwen3-4B Q4 MNN GPU (88.4), Qwen3.5-4B Q4_0 llama.cpp CPU (50.8), Qwen3.5-4B Q4 MNN CPU (49.9), Qwen3.5-4B Q4_0 llama.cpp GPU (47.5)
- **Decode under 8 tokens/s (5):** Qwen3-1.7B Q8 MNN GPU (7.99), Qwen3-1.7B F16 llama.cpp CPU (5.73), Qwen3.5-2B F16 llama.cpp CPU (5.25), Qwen3-4B Q4 MNN GPU (6.71), Qwen3.5-4B Q4_0 llama.cpp GPU (6.45)
- **Energy over 81 J per request (41):** SmolLM2-135M Q8_0 llama.cpp GPU (145 J), SmolLM2-135M F16 llama.cpp GPU (173 J), Gemma3-270M Q8_0 llama.cpp GPU (141 J), Gemma3-270M F16 llama.cpp CPU (84.1 J), Gemma3-270M F16 llama.cpp GPU (167 J), SmolLM2-360M Q8_0 llama.cpp GPU (405 J), SmolLM2-360M F16 llama.cpp CPU (188 J), SmolLM2-360M F16 llama.cpp GPU (479 J), Qwen3-0.6B Q8_0 llama.cpp GPU (580 J), Qwen3-0.6B F16 llama.cpp CPU (261 J), Qwen3-0.6B F16 MNN CPU (91 J), Qwen3-0.6B F16 llama.cpp GPU (715 J), Qwen3.5-0.8B F16 llama.cpp CPU (290 J), Qwen3.5-0.8B F16 MNN CPU (104 J), Qwen3.5-0.8B F16 llama.cpp GPU (106 J), Qwen3-1.7B Q4_0 llama.cpp CPU (118 J), Qwen3-1.7B Q4 MNN CPU (103 J), Qwen3-1.7B Q4_0 llama.cpp GPU (108 J), Qwen3-1.7B Q4 MNN GPU (85.1 J), Qwen3-1.7B Q8_0 llama.cpp CPU (146 J), Qwen3-1.7B Q8 MNN CPU (126 J), Qwen3-1.7B Q8_0 llama.cpp GPU (1,535 J), Qwen3-1.7B Q8 MNN GPU (115 J), Qwen3-1.7B F16 llama.cpp CPU (764 J), Qwen3-1.7B F16 llama.cpp GPU (2,300 J), Qwen3.5-2B Q4_0 llama.cpp CPU (123 J), Qwen3.5-2B Q4 MNN CPU (106 J), Qwen3.5-2B Q4_0 llama.cpp GPU (99.5 J), Qwen3.5-2B Q8_0 llama.cpp CPU (151 J), Qwen3.5-2B Q8 MNN CPU (133 J), Qwen3.5-2B Q8_0 llama.cpp GPU (131 J), Qwen3.5-2B Q8 MNN GPU (101 J), Qwen3.5-2B F16 llama.cpp CPU (726 J), Qwen3-4B Q4_0 llama.cpp CPU (268 J), Qwen3-4B Q4 MNN CPU (232 J), Qwen3-4B Q4_0 llama.cpp GPU (239 J), Qwen3-4B Q4 MNN GPU (193 J), Qwen3.5-4B Q4_0 llama.cpp CPU (275 J), Qwen3.5-4B Q4 MNN CPU (230 J), Qwen3.5-4B Q4_0 llama.cpp GPU (234 J), Qwen3.5-4B Q4 MNN GPU (152 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### M20

**Medium · OnePlus 8 Pro · Essay feedback app**: comments on a pasted essay; 500-token prompt, 300-token answer, first token within 10 s, 20 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 4,600 MB · prefill ≥ 50 tokens/s · decode ≥ 5 tokens/s · energy ≤ 94 J per request.

**Pick: Qwen3.5-2B Q4, MNN, GPU** (1,259 MB, 102 / 14.2 tokens/s, 92.1 J). Kept 39 of 104 configurations (38%); 40 filtered out; 25 cannot run on this phone.

![M20](figures/scenarios/M20.png)

<details><summary>Kept pool (39), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-2B Q4 MNN GPU **(pick)** | 1,259 | 102 | 14.2 | 92.1 |
| Qwen3.5-0.8B Q8 MNN CPU | 926 | 182 | 26 | 85.5 |
| Qwen3.5-0.8B Q8 MNN GPU | 959 | 182 | 17.9 | 66.5 |
| Qwen3.5-0.8B Q4 MNN CPU | 550 | 167 | 45.9 | 54.9 |
| Qwen3.5-0.8B Q4_0 llama.cpp CPU | 1,114 | 207 | 34.8 | 76 |
| Qwen3.5-0.8B Q4 MNN GPU | 583 | 207 | 24.1 | 40.5 |
| Qwen3-0.6B Q8_0 llama.cpp CPU | 1,440 | 259 | 30.3 | 76 |
| Qwen3-0.6B Q8 MNN CPU | 962 | 239 | 27.8 | 81.3 |
| Qwen3-0.6B Q8 MNN GPU | 844 | 213 | 8.89 | 73.9 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 261 | 48.2 | 57.9 |
| Qwen3-0.6B Q4 MNN CPU | 537 | 227 | 42.8 | 54.5 |
| Qwen3-0.6B Q4 MNN GPU | 546 | 214 | 9.15 | 54.8 |
| SmolLM2-360M F16 llama.cpp CPU | 779 | 161 | 30.3 | 93.2 |
| SmolLM2-360M F16 MNN CPU | 803 | 280 | 25.9 | 82.8 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 825 | 377 | 51.2 | 48.6 |
| SmolLM2-360M Q8 MNN CPU | 492 | 342 | 44.6 | 50.9 |
| SmolLM2-360M Q8 MNN GPU | 514 | 298 | 9.25 | 53.7 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 35.9 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 35.7 |
| SmolLM2-360M Q4 MNN GPU | 333 | 299 | 9.3 | 44.3 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 901 | 58 | 9.02 | 64.8 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 484 | 42.8 | 57.6 |
| Gemma3-270M F16 MNN CPU | 1,435 | 755 | 31.1 | 63.5 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,013 | 70.4 | 33.6 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 41.9 |
| Gemma3-270M Q8 MNN GPU | 430 | 736 | 8.35 | 43.3 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 1,015 | 84.3 | 27.3 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 30.2 |
| Gemma3-270M Q4 MNN GPU | 295 | 746 | 8.27 | 37.8 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,429 | 77.8 | 7.09 | 65.4 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 37.4 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 33.7 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 21.3 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 21.8 |
| SmolLM2-135M Q8 MNN GPU | 253 | 743 | 9.84 | 31.6 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 16.3 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 15.5 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 681 | 105 | 13.3 | 43.1 |
| SmolLM2-135M Q4 MNN GPU | 186 | 743 | 9.88 | 29.1 |

</details>

<details><summary>Filtered out (40) and cannot run (25)</summary>

- **Peak memory over 4,600 MB (5):** Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 50 tokens/s (24):** SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q4_0 llama.cpp GPU (35.1), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3-1.7B Q4_0 llama.cpp GPU (28.9), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B F16 llama.cpp CPU (27.4), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3.5-2B F16 llama.cpp CPU (28.2), Qwen3-4B Q4_0 llama.cpp CPU (41.1), Qwen3-4B Q4 MNN CPU (32.1), Qwen3-4B Q4_0 llama.cpp GPU (10.9), Qwen3-4B Q4 MNN GPU (31.1), Qwen3-4B Q8 MNN CPU (31), Qwen3-4B Q8 MNN GPU (26.4), Qwen3.5-4B Q4_0 llama.cpp CPU (39.8), Qwen3.5-4B Q4 MNN CPU (29.6), Qwen3.5-4B Q4 MNN GPU (39.5), Qwen3.5-4B Q8 MNN GPU (32.7)
- **Decode under 5 tokens/s (10):** Qwen3-0.6B Q4_0 llama.cpp GPU (3.49), Qwen3-0.6B Q8_0 llama.cpp GPU (4.35), Qwen3-0.6B F16 llama.cpp GPU (4.5), Qwen3-1.7B Q4_0 llama.cpp GPU (2.58), Qwen3-1.7B Q8_0 llama.cpp GPU (3.24), Qwen3-1.7B F16 llama.cpp GPU (3.09), Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q8 MNN CPU (4.85), Qwen3-4B Q8 MNN GPU (0.71), Qwen3.5-4B Q8 MNN GPU (3.96)
- **Energy over 94 J per request (35):** SmolLM2-135M Q8_0 llama.cpp GPU (124 J), SmolLM2-135M F16 llama.cpp GPU (214 J), Gemma3-270M Q8_0 llama.cpp GPU (146 J), Gemma3-270M F16 llama.cpp GPU (226 J), SmolLM2-360M Q8_0 llama.cpp GPU (316 J), Qwen3-0.6B Q4_0 llama.cpp GPU (131 J), Qwen3-0.6B F16 llama.cpp CPU (151 J), Qwen3-0.6B F16 MNN CPU (131 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (104 J), Qwen3.5-0.8B F16 llama.cpp CPU (188 J), Qwen3.5-0.8B F16 MNN CPU (152 J), Qwen3-1.7B Q4_0 llama.cpp CPU (134 J), Qwen3-1.7B Q4 MNN CPU (141 J), Qwen3-1.7B Q4_0 llama.cpp GPU (209 J), Qwen3-1.7B Q4 MNN GPU (116 J), Qwen3-1.7B Q8_0 llama.cpp CPU (207 J), Qwen3-1.7B Q8 MNN CPU (214 J), Qwen3-1.7B Q8 MNN GPU (171 J), Qwen3-1.7B F16 llama.cpp CPU (411 J), Qwen3.5-2B Q4_0 llama.cpp CPU (151 J), Qwen3.5-2B Q4 MNN CPU (128 J), Qwen3.5-2B Q8_0 llama.cpp CPU (227 J), Qwen3.5-2B Q8 MNN CPU (203 J), Qwen3.5-2B Q8 MNN GPU (155 J), Qwen3.5-2B F16 llama.cpp CPU (444 J), Qwen3-4B Q4_0 llama.cpp CPU (297 J), Qwen3-4B Q4 MNN CPU (326 J), Qwen3-4B Q4_0 llama.cpp GPU (453 J), Qwen3-4B Q4 MNN GPU (257 J), Qwen3-4B Q8 MNN CPU (499 J), Qwen3-4B Q8 MNN GPU (690 J), Qwen3.5-4B Q4_0 llama.cpp CPU (328 J), Qwen3.5-4B Q4 MNN CPU (290 J), Qwen3.5-4B Q4 MNN GPU (220 J), Qwen3.5-4B Q8 MNN GPU (364 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### H01

**Hard · Galaxy S23 · Keyboard next-phrase**: suggest the next words while typing; 64-token prompt, 8-token answer, first token within 0.3 s, 1,000 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 300 MB · prefill ≥ 213 tokens/s · decode ≥ 20 tokens/s · energy ≤ 1.62 J per request.

**Pick: Gemma3-270M Q4, MNN, CPU** (273 MB, 1,511 / 108 tokens/s, 0.942 J). Kept 4 of 104 configurations (4%); 78 filtered out; 22 cannot run on this phone.

![H01](figures/scenarios/H01.png)

<details><summary>Kept pool (4), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Gemma3-270M Q4 MNN CPU **(pick)** | 273 | 1,511 | 108 | 0.942 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 0.727 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 0.621 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 0.845 |

</details>

<details><summary>Filtered out (78) and cannot run (22)</summary>

- **Peak memory over 300 MB (77):** SmolLM2-135M Q4_0 llama.cpp GPU (747 MB), SmolLM2-135M Q8_0 llama.cpp CPU (326 MB), SmolLM2-135M Q8_0 llama.cpp GPU (821 MB), SmolLM2-135M Q8 MNN GPU (329 MB), SmolLM2-135M F16 llama.cpp CPU (309 MB), SmolLM2-135M F16 MNN CPU (340 MB), SmolLM2-135M F16 llama.cpp GPU (1,221 MB), Gemma3-270M Q4_0 llama.cpp CPU (534 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,498 MB), Gemma3-270M Q4 MNN GPU (379 MB), Gemma3-270M Q8_0 llama.cpp CPU (634 MB), Gemma3-270M Q8 MNN CPU (567 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,546 MB), Gemma3-270M Q8 MNN GPU (515 MB), Gemma3-270M F16 llama.cpp CPU (601 MB), Gemma3-270M F16 MNN CPU (1,487 MB), Gemma3-270M F16 llama.cpp GPU (2,486 MB), SmolLM2-360M Q4_0 llama.cpp CPU (508 MB), SmolLM2-360M Q4 MNN CPU (323 MB), SmolLM2-360M Q4_0 llama.cpp GPU (961 MB), SmolLM2-360M Q4 MNN GPU (433 MB), SmolLM2-360M Q8_0 llama.cpp CPU (824 MB), SmolLM2-360M Q8 MNN CPU (499 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,324 MB), SmolLM2-360M Q8 MNN GPU (614 MB), SmolLM2-360M F16 llama.cpp CPU (779 MB), SmolLM2-360M F16 MNN CPU (826 MB), SmolLM2-360M F16 llama.cpp GPU (2,179 MB), Qwen3-0.6B Q4_0 llama.cpp CPU (832 MB), Qwen3-0.6B Q4 MNN CPU (548 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,506 MB), Qwen3-0.6B Q4 MNN GPU (696 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,390 MB), Qwen3-0.6B Q8 MNN CPU (856 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,970 MB), Qwen3-0.6B Q8 MNN GPU (1,005 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,721 MB), Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,114 MB), Qwen3.5-0.8B Q4 MNN CPU (691 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,310 MB), Qwen3.5-0.8B Q4 MNN GPU (712 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,734 MB), Qwen3.5-0.8B Q8 MNN CPU (1,345 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,164 MB), Qwen3.5-0.8B Q8 MNN GPU (1,083 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,290 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q4 MNN GPU (1,443 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8 MNN CPU (2,397 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B Q8 MNN GPU (2,309 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,522 MB), Qwen3.5-2B Q4 MNN CPU (1,236 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,247 MB), Qwen3.5-2B Q4 MNN GPU (1,458 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,685 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4 MNN CPU (2,782 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 213 tokens/s (42):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp CPU (120), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp CPU (127), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp CPU (42.1), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q4_0 llama.cpp GPU (133), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B Q4_0 llama.cpp GPU (157), Qwen3.5-0.8B Q8_0 llama.cpp GPU (163), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3.5-0.8B F16 llama.cpp GPU (159), Qwen3-1.7B Q4_0 llama.cpp CPU (119), Qwen3-1.7B Q4 MNN CPU (138), Qwen3-1.7B Q4_0 llama.cpp GPU (107), Qwen3-1.7B Q8_0 llama.cpp CPU (106), Qwen3-1.7B Q8 MNN CPU (132), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B Q8 MNN GPU (211), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B Q4_0 llama.cpp CPU (132), Qwen3.5-2B Q4 MNN CPU (134), Qwen3.5-2B Q4_0 llama.cpp GPU (109), Qwen3.5-2B Q8_0 llama.cpp CPU (112), Qwen3.5-2B Q8 MNN CPU (128), Qwen3.5-2B Q8_0 llama.cpp GPU (113), Qwen3.5-2B Q8 MNN GPU (203), Qwen3.5-2B F16 llama.cpp CPU (9.4), Qwen3-4B Q4_0 llama.cpp CPU (44.5), Qwen3-4B Q4 MNN CPU (53.5), Qwen3-4B Q4_0 llama.cpp GPU (40.4), Qwen3-4B Q4 MNN GPU (88.4), Qwen3.5-4B Q4_0 llama.cpp CPU (50.8), Qwen3.5-4B Q4 MNN CPU (49.9), Qwen3.5-4B Q4_0 llama.cpp GPU (47.5), Qwen3.5-4B Q4 MNN GPU (115)
- **Decode under 20 tokens/s (33):** SmolLM2-135M Q4 MNN GPU (9.13), SmolLM2-135M Q8 MNN GPU (9.09), Gemma3-270M Q4 MNN GPU (8.98), Gemma3-270M Q8 MNN GPU (8.89), SmolLM2-360M Q4 MNN GPU (9.09), SmolLM2-360M Q8 MNN GPU (8.56), Qwen3-0.6B Q4_0 llama.cpp GPU (14.9), Qwen3-0.6B Q4 MNN GPU (9.74), Qwen3-0.6B Q8 MNN GPU (8.45), Qwen3-0.6B F16 llama.cpp CPU (16.4), Qwen3.5-0.8B Q4_0 llama.cpp GPU (13.4), Qwen3.5-0.8B Q8_0 llama.cpp GPU (15.2), Qwen3.5-0.8B F16 llama.cpp CPU (13.7), Qwen3.5-0.8B F16 llama.cpp GPU (11.2), Qwen3-1.7B Q4_0 llama.cpp GPU (12.2), Qwen3-1.7B Q4 MNN GPU (8.52), Qwen3-1.7B Q8_0 llama.cpp GPU (16.3), Qwen3-1.7B Q8 MNN GPU (7.99), Qwen3-1.7B F16 llama.cpp CPU (5.73), Qwen3-1.7B F16 llama.cpp GPU (11.2), Qwen3.5-2B Q4_0 llama.cpp GPU (11.3), Qwen3.5-2B Q8_0 llama.cpp CPU (19.4), Qwen3.5-2B Q8_0 llama.cpp GPU (11.3), Qwen3.5-2B Q8 MNN GPU (19), Qwen3.5-2B F16 llama.cpp CPU (5.25), Qwen3-4B Q4_0 llama.cpp CPU (10.8), Qwen3-4B Q4 MNN CPU (14.1), Qwen3-4B Q4_0 llama.cpp GPU (8.69), Qwen3-4B Q4 MNN GPU (6.71), Qwen3.5-4B Q4_0 llama.cpp CPU (10), Qwen3.5-4B Q4 MNN CPU (14.6), Qwen3.5-4B Q4_0 llama.cpp GPU (6.45), Qwen3.5-4B Q4 MNN GPU (15.3)
- **Energy over 1.62 J per request (63):** SmolLM2-135M Q8_0 llama.cpp GPU (8.88 J), SmolLM2-135M F16 llama.cpp CPU (3.95 J), SmolLM2-135M F16 llama.cpp GPU (10.5 J), Gemma3-270M Q8_0 llama.cpp GPU (8.51 J), Gemma3-270M F16 llama.cpp CPU (4.45 J), Gemma3-270M F16 MNN CPU (1.62 J), Gemma3-270M F16 llama.cpp GPU (10 J), SmolLM2-360M Q4_0 llama.cpp CPU (1.83 J), SmolLM2-360M Q4_0 llama.cpp GPU (1.97 J), SmolLM2-360M Q8_0 llama.cpp CPU (2.2 J), SmolLM2-360M Q8 MNN CPU (1.67 J), SmolLM2-360M Q8_0 llama.cpp GPU (25.2 J), SmolLM2-360M Q8 MNN GPU (1.7 J), SmolLM2-360M F16 llama.cpp CPU (10.6 J), SmolLM2-360M F16 MNN CPU (2.61 J), SmolLM2-360M F16 llama.cpp GPU (29.8 J), Qwen3-0.6B Q4_0 llama.cpp CPU (3.07 J), Qwen3-0.6B Q4 MNN CPU (2.07 J), Qwen3-0.6B Q4_0 llama.cpp GPU (3.24 J), Qwen3-0.6B Q4 MNN GPU (1.85 J), Qwen3-0.6B Q8_0 llama.cpp CPU (3.36 J), Qwen3-0.6B Q8 MNN CPU (2.59 J), Qwen3-0.6B Q8_0 llama.cpp GPU (36.2 J), Qwen3-0.6B Q8 MNN GPU (2.16 J), Qwen3-0.6B F16 llama.cpp CPU (14.7 J), Qwen3-0.6B F16 MNN CPU (4.21 J), Qwen3-0.6B F16 llama.cpp GPU (44.5 J), Qwen3.5-0.8B Q4 MNN CPU (2.42 J), Qwen3.5-0.8B Q4_0 llama.cpp GPU (3.42 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (3.47 J), Qwen3.5-0.8B Q8 MNN CPU (3.04 J), Qwen3.5-0.8B Q8_0 llama.cpp GPU (3.66 J), Qwen3.5-0.8B Q8 MNN GPU (2.11 J), Qwen3.5-0.8B F16 llama.cpp CPU (16 J), Qwen3.5-0.8B F16 MNN CPU (4.84 J), Qwen3.5-0.8B F16 llama.cpp GPU (4.91 J), Qwen3-1.7B Q4_0 llama.cpp CPU (5.97 J), Qwen3-1.7B Q4 MNN CPU (5.16 J), Qwen3-1.7B Q4_0 llama.cpp GPU (5.27 J), Qwen3-1.7B Q4 MNN GPU (3.99 J), Qwen3-1.7B Q8_0 llama.cpp CPU (7.35 J), Qwen3-1.7B Q8 MNN CPU (6.17 J), Qwen3-1.7B Q8_0 llama.cpp GPU (96.5 J), Qwen3-1.7B Q8 MNN GPU (5.25 J), Qwen3-1.7B F16 llama.cpp CPU (43.7 J), Qwen3-1.7B F16 llama.cpp GPU (144 J), Qwen3.5-2B Q4_0 llama.cpp CPU (6.11 J), Qwen3.5-2B Q4 MNN CPU (5.38 J), Qwen3.5-2B Q4_0 llama.cpp GPU (4.67 J), Qwen3.5-2B Q4 MNN GPU (2.8 J), Qwen3.5-2B Q8_0 llama.cpp CPU (7.49 J), Qwen3.5-2B Q8 MNN CPU (6.51 J), Qwen3.5-2B Q8_0 llama.cpp GPU (6.07 J), Qwen3.5-2B Q8 MNN GPU (4.57 J), Qwen3.5-2B F16 llama.cpp CPU (40.9 J), Qwen3-4B Q4_0 llama.cpp CPU (13.9 J), Qwen3-4B Q4 MNN CPU (12 J), Qwen3-4B Q4_0 llama.cpp GPU (12.2 J), Qwen3-4B Q4 MNN GPU (9.4 J), Qwen3.5-4B Q4_0 llama.cpp CPU (14 J), Qwen3.5-4B Q4 MNN CPU (12 J), Qwen3.5-4B Q4_0 llama.cpp GPU (11.4 J), Qwen3.5-4B Q4 MNN GPU (7.06 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### H02

**Hard · OnePlus 15 · Smart replies**: three reply chips under a received message; 128-token prompt, 24-token answer, first token within 0.5 s, 200 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 512 MB · prefill ≥ 256 tokens/s · decode ≥ 15 tokens/s · energy ≤ 10.1 J per request.

**Pick: SmolLM2-360M Q8, MNN, CPU** (509 MB, 524 / 99.6 tokens/s, 1.23 J). Kept 14 of 104 configurations (13%); 79 filtered out; 11 cannot run on this phone.

![H02](figures/scenarios/H02.png)

<details><summary>Kept pool (14), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| SmolLM2-360M Q8 MNN CPU **(pick)** | 509 | 524 | 99.6 | 1.23 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 1.09 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 0.982 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 1.62 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 0.65 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 1.18 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 0.674 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 1.76 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 0.485 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 0.594 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 1.02 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 0.454 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 0.391 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 0.967 |

</details>

<details><summary>Filtered out (79) and cannot run (11)</summary>

- **Peak memory over 512 MB (79):** SmolLM2-135M Q4_0 llama.cpp GPU (791 MB), SmolLM2-135M Q8_0 llama.cpp GPU (846 MB), SmolLM2-135M F16 llama.cpp GPU (1,265 MB), Gemma3-270M Q4_0 llama.cpp CPU (535 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,538 MB), Gemma3-270M Q8_0 llama.cpp CPU (634 MB), Gemma3-270M Q8 MNN CPU (694 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,591 MB), Gemma3-270M Q8 MNN GPU (524 MB), Gemma3-270M F16 llama.cpp CPU (602 MB), Gemma3-270M F16 MNN CPU (1,528 MB), Gemma3-270M F16 llama.cpp GPU (2,534 MB), SmolLM2-360M Q4_0 llama.cpp GPU (1,009 MB), SmolLM2-360M Q8_0 llama.cpp CPU (824 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,326 MB), SmolLM2-360M Q8 MNN GPU (624 MB), SmolLM2-360M F16 llama.cpp CPU (779 MB), SmolLM2-360M F16 MNN CPU (1,035 MB), SmolLM2-360M F16 llama.cpp GPU (2,225 MB), Qwen3-0.6B Q4_0 llama.cpp CPU (833 MB), Qwen3-0.6B Q4 MNN CPU (589 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,552 MB), Qwen3-0.6B Q4 MNN GPU (707 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,291 MB), Qwen3-0.6B Q8 MNN CPU (1,029 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (2,017 MB), Qwen3-0.6B Q8 MNN GPU (1,002 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (2,081 MB), Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,115 MB), Qwen3.5-0.8B Q4 MNN CPU (568 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,883 MB), Qwen3.5-0.8B Q4 MNN GPU (733 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B Q8 MNN CPU (940 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,273 MB), Qwen3.5-0.8B Q8 MNN GPU (1,108 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,286 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q4 MNN GPU (1,443 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B Q8 MNN GPU (2,479 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,512 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q4 MNN GPU (1,470 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8 MNN CPU (2,206 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 256 tokens/s (32):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3-0.6B F16 MNN CPU (184), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3.5-0.8B F16 MNN CPU (250), Qwen3-1.7B Q4_0 llama.cpp CPU (144), Qwen3-1.7B Q4 MNN CPU (103), Qwen3-1.7B Q8_0 llama.cpp CPU (150), Qwen3-1.7B Q8 MNN CPU (136), Qwen3-1.7B Q8_0 llama.cpp GPU (164), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3-1.7B F16 llama.cpp GPU (225), Qwen3.5-2B Q4_0 llama.cpp CPU (150), Qwen3.5-2B Q4 MNN CPU (158), Qwen3.5-2B Q8_0 llama.cpp CPU (159), Qwen3.5-2B Q8 MNN CPU (169), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3.5-2B F16 MNN CPU (113), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q4 MNN GPU (191), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B Q8 MNN GPU (180), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6), Qwen3.5-4B Q8_0 llama.cpp GPU (240), Qwen3.5-4B Q8 MNN GPU (204)
- **Decode under 15 tokens/s (12):** Qwen3-1.7B F16 MNN CPU (13.6), Qwen3.5-2B F16 llama.cpp CPU (14.7), Qwen3.5-2B F16 MNN CPU (14.1), Qwen3-4B Q8_0 llama.cpp CPU (13.8), Qwen3-4B Q8_0 llama.cpp GPU (12.7), Qwen3-4B Q8 MNN GPU (12.6), Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42), Qwen3.5-4B Q4_0 llama.cpp GPU (14.5), Qwen3.5-4B Q8 MNN CPU (14.2), Qwen3.5-4B Q8_0 llama.cpp GPU (12.6), Qwen3.5-4B Q8 MNN GPU (13.4)
- **Energy over 10.1 J per request (15):** Qwen3-1.7B F16 llama.cpp CPU (24.5 J), Qwen3-1.7B F16 MNN CPU (10.7 J), Qwen3.5-2B F16 llama.cpp CPU (22.1 J), Qwen3-4B Q4_0 llama.cpp CPU (11.8 J), Qwen3-4B Q4 MNN CPU (12.5 J), Qwen3-4B Q4 MNN GPU (10.7 J), Qwen3-4B Q8_0 llama.cpp CPU (14.1 J), Qwen3-4B Q8_0 llama.cpp GPU (16.1 J), Qwen3-4B Q8 MNN GPU (15.1 J), Qwen3-4B F16 llama.cpp CPU (57.8 J), Qwen3-4B F16 llama.cpp GPU (21.4 J), Qwen3.5-4B Q4_0 llama.cpp CPU (12.1 J), Qwen3.5-4B Q8 MNN CPU (16.7 J), Qwen3.5-4B Q8_0 llama.cpp GPU (12.8 J), Qwen3.5-4B Q8 MNN GPU (13.1 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### H03

**Hard · OnePlus 8 Pro · Live message translation**: translate each chat message as it arrives; 64-token prompt, 64-token answer, first token within 0.5 s, 200 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,024 MB · prefill ≥ 128 tokens/s · decode ≥ 10 tokens/s · energy ≤ 9.4 J per request.

**Pick: Qwen3.5-0.8B Q4, MNN, GPU** (583 MB, 207 / 24.1 tokens/s, 8.23 J). Kept 13 of 104 configurations (12%); 66 filtered out; 25 cannot run on this phone.

![H03](figures/scenarios/H03.png)

<details><summary>Kept pool (13), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q4 MNN GPU **(pick)** | 583 | 207 | 24.1 | 8.23 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 7.02 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 6.97 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,013 | 70.4 | 6.89 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 8.69 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 1,015 | 84.3 | 5.57 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 6.2 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 7.19 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 6.85 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 4.26 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 4.4 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 3.21 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 3.07 |

</details>

<details><summary>Filtered out (66) and cannot run (25)</summary>

- **Peak memory over 1,024 MB (43):** Gemma3-270M Q4_0 llama.cpp GPU (1,429 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,478 MB), Gemma3-270M F16 MNN CPU (1,435 MB), Gemma3-270M F16 llama.cpp GPU (1,886 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,216 MB), SmolLM2-360M F16 llama.cpp GPU (1,389 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,315 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,440 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,908 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,811 MB), Qwen3-0.6B F16 llama.cpp GPU (2,344 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,114 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,642 MB), Qwen3.5-0.8B F16 MNN CPU (2,906 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,270 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (2,753 MB), Qwen3-1.7B Q4 MNN GPU (1,211 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,678 MB), Qwen3-1.7B Q8 MNN CPU (2,416 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,474 MB), Qwen3-1.7B Q8 MNN GPU (2,072 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,218 MB), Qwen3.5-2B Q4 MNN GPU (1,259 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,974 MB), Qwen3.5-2B Q8 MNN GPU (2,221 MB), Qwen3.5-2B F16 llama.cpp CPU (3,908 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,560 MB), Qwen3-4B Q4 MNN CPU (2,755 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q4 MNN GPU (2,580 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3-4B Q8 MNN GPU (4,179 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q4 MNN CPU (2,875 MB), Qwen3.5-4B Q4 MNN GPU (2,655 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 128 tokens/s (41):** SmolLM2-135M Q4_0 llama.cpp GPU (105), SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q4_0 llama.cpp GPU (77.8), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q4_0 llama.cpp GPU (58), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q4_0 llama.cpp GPU (35.1), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B F16 llama.cpp CPU (99.5), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3.5-0.8B F16 llama.cpp CPU (80.2), Qwen3-1.7B Q4_0 llama.cpp CPU (99.3), Qwen3-1.7B Q4 MNN CPU (81.8), Qwen3-1.7B Q4_0 llama.cpp GPU (28.9), Qwen3-1.7B Q4 MNN GPU (77.3), Qwen3-1.7B Q8_0 llama.cpp CPU (96.8), Qwen3-1.7B Q8 MNN CPU (83.2), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B Q8 MNN GPU (73.8), Qwen3-1.7B F16 llama.cpp CPU (27.4), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3.5-2B Q4_0 llama.cpp CPU (98.8), Qwen3.5-2B Q4 MNN CPU (77.9), Qwen3.5-2B Q4 MNN GPU (102), Qwen3.5-2B Q8_0 llama.cpp CPU (94), Qwen3.5-2B Q8 MNN CPU (81.6), Qwen3.5-2B Q8 MNN GPU (83.7), Qwen3.5-2B F16 llama.cpp CPU (28.2), Qwen3-4B Q4_0 llama.cpp CPU (41.1), Qwen3-4B Q4 MNN CPU (32.1), Qwen3-4B Q4_0 llama.cpp GPU (10.9), Qwen3-4B Q4 MNN GPU (31.1), Qwen3-4B Q8 MNN CPU (31), Qwen3-4B Q8 MNN GPU (26.4), Qwen3.5-4B Q4_0 llama.cpp CPU (39.8), Qwen3.5-4B Q4 MNN CPU (29.6), Qwen3.5-4B Q4 MNN GPU (39.5), Qwen3.5-4B Q8 MNN GPU (32.7)
- **Decode under 10 tokens/s (37):** SmolLM2-135M Q4 MNN GPU (9.88), SmolLM2-135M Q8_0 llama.cpp GPU (9.26), SmolLM2-135M Q8 MNN GPU (9.84), SmolLM2-135M F16 llama.cpp GPU (9.81), Gemma3-270M Q4_0 llama.cpp GPU (7.09), Gemma3-270M Q4 MNN GPU (8.27), Gemma3-270M Q8_0 llama.cpp GPU (6.11), Gemma3-270M Q8 MNN GPU (8.35), Gemma3-270M F16 llama.cpp GPU (6.35), SmolLM2-360M Q4_0 llama.cpp GPU (9.02), SmolLM2-360M Q4 MNN GPU (9.3), SmolLM2-360M Q8_0 llama.cpp GPU (6.17), SmolLM2-360M Q8 MNN GPU (9.25), SmolLM2-360M F16 llama.cpp GPU (6.34), Qwen3-0.6B Q4_0 llama.cpp GPU (3.49), Qwen3-0.6B Q4 MNN GPU (9.15), Qwen3-0.6B Q8_0 llama.cpp GPU (4.35), Qwen3-0.6B Q8 MNN GPU (8.89), Qwen3-0.6B F16 llama.cpp GPU (4.5), Qwen3-1.7B Q4_0 llama.cpp GPU (2.58), Qwen3-1.7B Q4 MNN GPU (9.06), Qwen3-1.7B Q8_0 llama.cpp GPU (3.24), Qwen3-1.7B Q8 MNN GPU (8.38), Qwen3-1.7B F16 llama.cpp CPU (6.88), Qwen3-1.7B F16 llama.cpp GPU (3.09), Qwen3.5-2B Q8 MNN GPU (9.33), Qwen3.5-2B F16 llama.cpp CPU (6.07), Qwen3-4B Q4_0 llama.cpp CPU (8.93), Qwen3-4B Q4 MNN CPU (8.66), Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q4 MNN GPU (6.33), Qwen3-4B Q8 MNN CPU (4.85), Qwen3-4B Q8 MNN GPU (0.71), Qwen3.5-4B Q4_0 llama.cpp CPU (8.01), Qwen3.5-4B Q4 MNN CPU (9.25), Qwen3.5-4B Q4 MNN GPU (7.07), Qwen3.5-4B Q8 MNN GPU (3.96)
- **Energy over 9.4 J per request (55):** SmolLM2-135M Q8_0 llama.cpp GPU (19.6 J), SmolLM2-135M F16 llama.cpp GPU (31.6 J), Gemma3-270M Q4_0 llama.cpp GPU (13.1 J), Gemma3-270M Q8_0 llama.cpp GPU (24.4 J), Gemma3-270M F16 llama.cpp CPU (11.5 J), Gemma3-270M F16 MNN CPU (13.2 J), Gemma3-270M F16 llama.cpp GPU (35.8 J), SmolLM2-360M Q4_0 llama.cpp GPU (12.5 J), SmolLM2-360M Q8_0 llama.cpp CPU (9.61 J), SmolLM2-360M Q8 MNN CPU (10.2 J), SmolLM2-360M Q8_0 llama.cpp GPU (46.6 J), SmolLM2-360M Q8 MNN GPU (11.1 J), SmolLM2-360M F16 llama.cpp CPU (17.7 J), SmolLM2-360M F16 MNN CPU (16.7 J), Qwen3-0.6B Q4_0 llama.cpp CPU (11.4 J), Qwen3-0.6B Q4 MNN CPU (10.7 J), Qwen3-0.6B Q4_0 llama.cpp GPU (25.8 J), Qwen3-0.6B Q4 MNN GPU (11.2 J), Qwen3-0.6B Q8_0 llama.cpp CPU (15.2 J), Qwen3-0.6B Q8 MNN CPU (16.3 J), Qwen3-0.6B Q8 MNN GPU (15.2 J), Qwen3-0.6B F16 llama.cpp CPU (29 J), Qwen3-0.6B F16 MNN CPU (26.6 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (15 J), Qwen3.5-0.8B Q4 MNN CPU (10.5 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (20.8 J), Qwen3.5-0.8B Q8 MNN CPU (17 J), Qwen3.5-0.8B Q8 MNN GPU (13.6 J), Qwen3.5-0.8B F16 llama.cpp CPU (36.6 J), Qwen3.5-0.8B F16 MNN CPU (30.9 J), Qwen3-1.7B Q4_0 llama.cpp CPU (26.3 J), Qwen3-1.7B Q4 MNN CPU (27.1 J), Qwen3-1.7B Q4_0 llama.cpp GPU (41.6 J), Qwen3-1.7B Q4 MNN GPU (23.1 J), Qwen3-1.7B Q8_0 llama.cpp CPU (40.8 J), Qwen3-1.7B Q8 MNN CPU (42.4 J), Qwen3-1.7B Q8 MNN GPU (34.6 J), Qwen3-1.7B F16 llama.cpp CPU (77.9 J), Qwen3.5-2B Q4_0 llama.cpp CPU (29.8 J), Qwen3.5-2B Q4 MNN CPU (24.7 J), Qwen3.5-2B Q4 MNN GPU (18.7 J), Qwen3.5-2B Q8_0 llama.cpp CPU (45.2 J), Qwen3.5-2B Q8 MNN CPU (40.5 J), Qwen3.5-2B Q8 MNN GPU (31.7 J), Qwen3.5-2B F16 llama.cpp CPU (85 J), Qwen3-4B Q4_0 llama.cpp CPU (57.3 J), Qwen3-4B Q4 MNN CPU (61.8 J), Qwen3-4B Q4_0 llama.cpp GPU (88 J), Qwen3-4B Q4 MNN GPU (50.6 J), Qwen3-4B Q8 MNN CPU (98.4 J), Qwen3-4B Q8 MNN GPU (143 J), Qwen3.5-4B Q4_0 llama.cpp CPU (63.8 J), Qwen3.5-4B Q4 MNN CPU (55.5 J), Qwen3.5-4B Q4 MNN GPU (44.3 J), Qwen3.5-4B Q8 MNN GPU (74 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### H04

**Hard · Redmi 9A · SMS auto-reply**: draft a reply to a text message; 128-token prompt, 32-token answer, first token within 5 s, 50 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 768 MB · prefill ≥ 25.6 tokens/s · decode ≥ 3 tokens/s · energy ≤ 27.8 J per request.

**Pick: Gemma3-270M Q8, MNN, CPU** (745 MB, 35.2 / 6.71 tokens/s, 8.65 J). Kept 5 of 52 configurations (10%); 29 filtered out; 18 cannot run on this phone.

![H04](figures/scenarios/H04.png)

<details><summary>Kept pool (5), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Gemma3-270M Q8 MNN CPU **(pick)** | 745 | 35.2 | 6.71 | 8.65 |
| Gemma3-270M Q4 MNN CPU | 326 | 39.9 | 7.72 | 7.77 |
| SmolLM2-135M F16 MNN CPU | 637 | 43 | 8.17 | 7.69 |
| SmolLM2-135M Q8 MNN CPU | 225 | 32.5 | 13.2 | 6.22 |
| SmolLM2-135M Q4 MNN CPU | 158 | 36.5 | 16.3 | 5.13 |

</details>

<details><summary>Filtered out (29) and cannot run (18)</summary>

- **Peak memory over 768 MB (15):** Gemma3-270M F16 MNN CPU (1,681 MB), SmolLM2-360M F16 llama.cpp CPU (773 MB), SmolLM2-360M F16 MNN CPU (1,614 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (907 MB), Qwen3-0.6B Q8 MNN CPU (1,023 MB), Qwen3-0.6B F16 llama.cpp CPU (1,612 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (910 MB), Qwen3.5-0.8B Q8 MNN CPU (1,241 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,615 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (1,340 MB), Qwen3-1.7B Q4 MNN CPU (1,543 MB), Qwen3-1.7B Q8 MNN CPU (2,357 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (1,307 MB), Qwen3.5-2B Q4 MNN CPU (1,663 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (2,116 MB)
- **Prefill under 25.6 tokens/s (28):** SmolLM2-135M Q4_0 llama.cpp CPU (11.6), SmolLM2-135M Q8_0 llama.cpp CPU (11.2), SmolLM2-135M F16 llama.cpp CPU (1.54), Gemma3-270M Q4_0 llama.cpp CPU (11.7), Gemma3-270M Q8_0 llama.cpp CPU (11.3), Gemma3-270M F16 llama.cpp CPU (1.64), SmolLM2-360M Q4_0 llama.cpp CPU (5.39), SmolLM2-360M Q4 MNN CPU (14.8), SmolLM2-360M Q8_0 llama.cpp CPU (5.14), SmolLM2-360M Q8 MNN CPU (12.7), SmolLM2-360M F16 llama.cpp CPU (0.54), SmolLM2-360M F16 MNN CPU (15.9), Qwen3-0.6B Q4_0 llama.cpp CPU (3.31), Qwen3-0.6B Q4 MNN CPU (11.4), Qwen3-0.6B Q8_0 llama.cpp CPU (3.18), Qwen3-0.6B Q8 MNN CPU (9.59), Qwen3-0.6B F16 llama.cpp CPU (0.38), Qwen3.5-0.8B Q4_0 llama.cpp CPU (4.59), Qwen3.5-0.8B Q4 MNN CPU (9.1), Qwen3.5-0.8B Q8_0 llama.cpp CPU (4.31), Qwen3.5-0.8B Q8 MNN CPU (7.75), Qwen3.5-0.8B F16 llama.cpp CPU (0.33), Qwen3-1.7B Q4_0 llama.cpp CPU (1.78), Qwen3-1.7B Q4 MNN CPU (4.75), Qwen3-1.7B Q8 MNN CPU (3.76), Qwen3.5-2B Q4_0 llama.cpp CPU (2.2), Qwen3.5-2B Q4 MNN CPU (4.58), Qwen3.5-2B Q8_0 llama.cpp CPU (2.03)
- **Decode under 3 tokens/s (15):** SmolLM2-135M F16 llama.cpp CPU (1.09), Gemma3-270M F16 llama.cpp CPU (0.59), SmolLM2-360M Q4_0 llama.cpp CPU (2.95), SmolLM2-360M Q8_0 llama.cpp CPU (2.82), SmolLM2-360M F16 llama.cpp CPU (0.43), Qwen3-0.6B Q4_0 llama.cpp CPU (1.68), Qwen3-0.6B Q8_0 llama.cpp CPU (1.67), Qwen3-0.6B F16 llama.cpp CPU (0.26), Qwen3.5-0.8B Q4_0 llama.cpp CPU (2.17), Qwen3.5-0.8B Q8_0 llama.cpp CPU (2.2), Qwen3.5-0.8B F16 llama.cpp CPU (0.21), Qwen3-1.7B Q4_0 llama.cpp CPU (1.07), Qwen3-1.7B Q8 MNN CPU (2.49), Qwen3.5-2B Q4_0 llama.cpp CPU (1.24), Qwen3.5-2B Q8_0 llama.cpp CPU (1.2)
- **Energy over 27.8 J per request (18):** SmolLM2-135M F16 llama.cpp CPU (87.6 J), Gemma3-270M F16 llama.cpp CPU (101 J), SmolLM2-360M Q4_0 llama.cpp CPU (29.1 J), SmolLM2-360M Q8_0 llama.cpp CPU (34.2 J), SmolLM2-360M F16 llama.cpp CPU (239 J), Qwen3-0.6B Q4_0 llama.cpp CPU (48.1 J), Qwen3-0.6B Q8_0 llama.cpp CPU (55.7 J), Qwen3-0.6B F16 llama.cpp CPU (350 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (50.7 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (61.5 J), Qwen3.5-0.8B Q8 MNN CPU (28.2 J), Qwen3.5-0.8B F16 llama.cpp CPU (508 J), Qwen3-1.7B Q4_0 llama.cpp CPU (97.3 J), Qwen3-1.7B Q4 MNN CPU (40.6 J), Qwen3-1.7B Q8 MNN CPU (62.1 J), Qwen3.5-2B Q4_0 llama.cpp CPU (106 J), Qwen3.5-2B Q4 MNN CPU (47.4 J), Qwen3.5-2B Q8_0 llama.cpp CPU (118 J)
- **Cannot run on this phone (18):** Qwen3-0.6B F16 MNN CPU (does not fit in RAM), Qwen3.5-0.8B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B Q8_0 llama.cpp CPU (crash/error (exit 1)), Qwen3-1.7B F16 llama.cpp CPU (does not fit in RAM), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B Q8 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp CPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q4 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q4 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM)

</details>

### H05

**Hard · Galaxy S23 · Game NPC dialogue**: a character answers the player; 200-token prompt, 40-token answer, first token within 1 s, 100 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 768 MB · prefill ≥ 200 tokens/s · decode ≥ 12 tokens/s · energy ≤ 10.8 J per request.

**Pick: Qwen3.5-0.8B Q4, MNN, CPU** (691 MB, 273 / 73.6 tokens/s, 9.4 J). Kept 16 of 104 configurations (15%); 66 filtered out; 22 cannot run on this phone.

![H05](figures/scenarios/H05.png)

<details><summary>Kept pool (16), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q4 MNN CPU **(pick)** | 691 | 273 | 73.6 | 9.4 |
| Qwen3.5-0.8B Q4 MNN GPU | 712 | 454 | 46.1 | 5.86 |
| Qwen3-0.6B Q4 MNN CPU | 548 | 371 | 72.7 | 8.38 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 6.9 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 5.49 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 7.06 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 5.04 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 5.26 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 4.45 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 4.16 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 4.3 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 3.04 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 3.72 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 2.54 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 3.28 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 4.53 |

</details>

<details><summary>Filtered out (66) and cannot run (22)</summary>

- **Peak memory over 768 MB (57):** SmolLM2-135M Q8_0 llama.cpp GPU (821 MB), SmolLM2-135M F16 llama.cpp GPU (1,221 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,498 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,546 MB), Gemma3-270M F16 MNN CPU (1,487 MB), Gemma3-270M F16 llama.cpp GPU (2,486 MB), SmolLM2-360M Q4_0 llama.cpp GPU (961 MB), SmolLM2-360M Q8_0 llama.cpp CPU (824 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,324 MB), SmolLM2-360M F16 llama.cpp CPU (779 MB), SmolLM2-360M F16 MNN CPU (826 MB), SmolLM2-360M F16 llama.cpp GPU (2,179 MB), Qwen3-0.6B Q4_0 llama.cpp CPU (832 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,506 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,390 MB), Qwen3-0.6B Q8 MNN CPU (856 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,970 MB), Qwen3-0.6B Q8 MNN GPU (1,005 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,721 MB), Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,114 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,310 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,734 MB), Qwen3.5-0.8B Q8 MNN CPU (1,345 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,164 MB), Qwen3.5-0.8B Q8 MNN GPU (1,083 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,290 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q4 MNN GPU (1,443 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8 MNN CPU (2,397 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B Q8 MNN GPU (2,309 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,522 MB), Qwen3.5-2B Q4 MNN CPU (1,236 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,247 MB), Qwen3.5-2B Q4 MNN GPU (1,458 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,685 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4 MNN CPU (2,782 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 200 tokens/s (40):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp CPU (120), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp CPU (127), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp CPU (42.1), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q4_0 llama.cpp GPU (133), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B Q4_0 llama.cpp GPU (157), Qwen3.5-0.8B Q8_0 llama.cpp GPU (163), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3.5-0.8B F16 llama.cpp GPU (159), Qwen3-1.7B Q4_0 llama.cpp CPU (119), Qwen3-1.7B Q4 MNN CPU (138), Qwen3-1.7B Q4_0 llama.cpp GPU (107), Qwen3-1.7B Q8_0 llama.cpp CPU (106), Qwen3-1.7B Q8 MNN CPU (132), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B Q4_0 llama.cpp CPU (132), Qwen3.5-2B Q4 MNN CPU (134), Qwen3.5-2B Q4_0 llama.cpp GPU (109), Qwen3.5-2B Q8_0 llama.cpp CPU (112), Qwen3.5-2B Q8 MNN CPU (128), Qwen3.5-2B Q8_0 llama.cpp GPU (113), Qwen3.5-2B F16 llama.cpp CPU (9.4), Qwen3-4B Q4_0 llama.cpp CPU (44.5), Qwen3-4B Q4 MNN CPU (53.5), Qwen3-4B Q4_0 llama.cpp GPU (40.4), Qwen3-4B Q4 MNN GPU (88.4), Qwen3.5-4B Q4_0 llama.cpp CPU (50.8), Qwen3.5-4B Q4 MNN CPU (49.9), Qwen3.5-4B Q4_0 llama.cpp GPU (47.5), Qwen3.5-4B Q4 MNN GPU (115)
- **Decode under 12 tokens/s (21):** SmolLM2-135M Q4 MNN GPU (9.13), SmolLM2-135M Q8 MNN GPU (9.09), Gemma3-270M Q4 MNN GPU (8.98), Gemma3-270M Q8 MNN GPU (8.89), SmolLM2-360M Q4 MNN GPU (9.09), SmolLM2-360M Q8 MNN GPU (8.56), Qwen3-0.6B Q4 MNN GPU (9.74), Qwen3-0.6B Q8 MNN GPU (8.45), Qwen3.5-0.8B F16 llama.cpp GPU (11.2), Qwen3-1.7B Q4 MNN GPU (8.52), Qwen3-1.7B Q8 MNN GPU (7.99), Qwen3-1.7B F16 llama.cpp CPU (5.73), Qwen3-1.7B F16 llama.cpp GPU (11.2), Qwen3.5-2B Q4_0 llama.cpp GPU (11.3), Qwen3.5-2B Q8_0 llama.cpp GPU (11.3), Qwen3.5-2B F16 llama.cpp CPU (5.25), Qwen3-4B Q4_0 llama.cpp CPU (10.8), Qwen3-4B Q4_0 llama.cpp GPU (8.69), Qwen3-4B Q4 MNN GPU (6.71), Qwen3.5-4B Q4_0 llama.cpp CPU (10), Qwen3.5-4B Q4_0 llama.cpp GPU (6.45)
- **Energy over 10.8 J per request (52):** SmolLM2-135M Q8_0 llama.cpp GPU (29.1 J), SmolLM2-135M F16 llama.cpp CPU (14.1 J), SmolLM2-135M F16 llama.cpp GPU (34.5 J), Gemma3-270M Q8_0 llama.cpp GPU (28.2 J), Gemma3-270M F16 llama.cpp CPU (16.8 J), Gemma3-270M F16 llama.cpp GPU (33.4 J), SmolLM2-360M Q8_0 llama.cpp GPU (81.1 J), SmolLM2-360M F16 llama.cpp CPU (37.5 J), SmolLM2-360M F16 MNN CPU (11.2 J), SmolLM2-360M F16 llama.cpp GPU (95.8 J), Qwen3-0.6B Q4_0 llama.cpp CPU (12 J), Qwen3-0.6B Q4_0 llama.cpp GPU (13 J), Qwen3-0.6B Q8_0 llama.cpp CPU (13.3 J), Qwen3-0.6B Q8 MNN CPU (10.8 J), Qwen3-0.6B Q8_0 llama.cpp GPU (116 J), Qwen3-0.6B F16 llama.cpp CPU (52.2 J), Qwen3-0.6B F16 MNN CPU (18.2 J), Qwen3-0.6B F16 llama.cpp GPU (143 J), Qwen3.5-0.8B Q4_0 llama.cpp GPU (14.4 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (14.2 J), Qwen3.5-0.8B Q8 MNN CPU (12.4 J), Qwen3.5-0.8B Q8_0 llama.cpp GPU (15.7 J), Qwen3.5-0.8B F16 llama.cpp CPU (58 J), Qwen3.5-0.8B F16 MNN CPU (20.9 J), Qwen3.5-0.8B F16 llama.cpp GPU (21.3 J), Qwen3-1.7B Q4_0 llama.cpp CPU (23.6 J), Qwen3-1.7B Q4 MNN CPU (20.6 J), Qwen3-1.7B Q4_0 llama.cpp GPU (21.7 J), Qwen3-1.7B Q4 MNN GPU (17 J), Qwen3-1.7B Q8_0 llama.cpp CPU (29.2 J), Qwen3-1.7B Q8 MNN CPU (25.2 J), Qwen3-1.7B Q8_0 llama.cpp GPU (307 J), Qwen3-1.7B Q8 MNN GPU (22.9 J), Qwen3-1.7B F16 llama.cpp CPU (153 J), Qwen3-1.7B F16 llama.cpp GPU (460 J), Qwen3.5-2B Q4_0 llama.cpp CPU (24.6 J), Qwen3.5-2B Q4 MNN CPU (21.2 J), Qwen3.5-2B Q4_0 llama.cpp GPU (19.9 J), Qwen3.5-2B Q4 MNN GPU (12.1 J), Qwen3.5-2B Q8_0 llama.cpp CPU (30.1 J), Qwen3.5-2B Q8 MNN CPU (26.6 J), Qwen3.5-2B Q8_0 llama.cpp GPU (26.2 J), Qwen3.5-2B Q8 MNN GPU (20.1 J), Qwen3.5-2B F16 llama.cpp CPU (145 J), Qwen3-4B Q4_0 llama.cpp CPU (53.6 J), Qwen3-4B Q4 MNN CPU (46.4 J), Qwen3-4B Q4_0 llama.cpp GPU (47.8 J), Qwen3-4B Q4 MNN GPU (38.7 J), Qwen3.5-4B Q4_0 llama.cpp CPU (55 J), Qwen3.5-4B Q4 MNN CPU (46.1 J), Qwen3.5-4B Q4_0 llama.cpp GPU (46.7 J), Qwen3.5-4B Q4 MNN GPU (30.5 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### H06

**Hard · OnePlus 15 · Notification triage**: classify incoming notifications in the background; 128-token prompt, 8-token answer, first token within 5 s, 300 requests ≤ 1% of battery a day.

Limits: peak memory ≤ 512 MB · prefill ≥ 25.6 tokens/s · decode ≥ 2 tokens/s · energy ≤ 3.37 J per request.

**Pick: SmolLM2-360M Q8, MNN, CPU** (509 MB, 524 / 99.6 tokens/s, 0.746 J). Kept 14 of 104 configurations (13%); 79 filtered out; 11 cannot run on this phone.

![H06](figures/scenarios/H06.png)

<details><summary>Kept pool (14), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| SmolLM2-360M Q8 MNN CPU **(pick)** | 509 | 524 | 99.6 | 0.746 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 0.7 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 0.619 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 0.759 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 0.336 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 0.47 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 0.393 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 1.33 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 0.279 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 0.335 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 0.416 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 0.262 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 0.233 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 0.399 |

</details>

<details><summary>Filtered out (79) and cannot run (11)</summary>

- **Peak memory over 512 MB (79):** SmolLM2-135M Q4_0 llama.cpp GPU (791 MB), SmolLM2-135M Q8_0 llama.cpp GPU (846 MB), SmolLM2-135M F16 llama.cpp GPU (1,265 MB), Gemma3-270M Q4_0 llama.cpp CPU (535 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,538 MB), Gemma3-270M Q8_0 llama.cpp CPU (634 MB), Gemma3-270M Q8 MNN CPU (694 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,591 MB), Gemma3-270M Q8 MNN GPU (524 MB), Gemma3-270M F16 llama.cpp CPU (602 MB), Gemma3-270M F16 MNN CPU (1,528 MB), Gemma3-270M F16 llama.cpp GPU (2,534 MB), SmolLM2-360M Q4_0 llama.cpp GPU (1,009 MB), SmolLM2-360M Q8_0 llama.cpp CPU (824 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,326 MB), SmolLM2-360M Q8 MNN GPU (624 MB), SmolLM2-360M F16 llama.cpp CPU (779 MB), SmolLM2-360M F16 MNN CPU (1,035 MB), SmolLM2-360M F16 llama.cpp GPU (2,225 MB), Qwen3-0.6B Q4_0 llama.cpp CPU (833 MB), Qwen3-0.6B Q4 MNN CPU (589 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,552 MB), Qwen3-0.6B Q4 MNN GPU (707 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,291 MB), Qwen3-0.6B Q8 MNN CPU (1,029 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (2,017 MB), Qwen3-0.6B Q8 MNN GPU (1,002 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (2,081 MB), Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,115 MB), Qwen3.5-0.8B Q4 MNN CPU (568 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,883 MB), Qwen3.5-0.8B Q4 MNN GPU (733 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B Q8 MNN CPU (940 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,273 MB), Qwen3.5-0.8B Q8 MNN GPU (1,108 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,286 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q4 MNN GPU (1,443 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B Q8 MNN GPU (2,479 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,512 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q4 MNN GPU (1,470 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8 MNN CPU (2,206 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 25.6 tokens/s (3):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 3.37 J per request (28):** SmolLM2-360M F16 llama.cpp CPU (3.78 J), Qwen3-0.6B F16 llama.cpp CPU (5.94 J), Qwen3.5-0.8B F16 llama.cpp CPU (6.45 J), Qwen3-1.7B Q8 MNN CPU (4.01 J), Qwen3-1.7B Q8_0 llama.cpp GPU (3.65 J), Qwen3-1.7B F16 llama.cpp CPU (19.2 J), Qwen3-1.7B F16 MNN CPU (6.7 J), Qwen3-1.7B F16 llama.cpp GPU (4.14 J), Qwen3.5-2B Q8 MNN CPU (3.4 J), Qwen3.5-2B F16 llama.cpp CPU (16.4 J), Qwen3.5-2B F16 MNN CPU (5.02 J), Qwen3.5-2B F16 llama.cpp GPU (3.4 J), Qwen3-4B Q4_0 llama.cpp CPU (7.44 J), Qwen3-4B Q4 MNN CPU (9.25 J), Qwen3-4B Q4_0 llama.cpp GPU (4.87 J), Qwen3-4B Q4 MNN GPU (6.27 J), Qwen3-4B Q8_0 llama.cpp CPU (8.82 J), Qwen3-4B Q8_0 llama.cpp GPU (9.2 J), Qwen3-4B Q8 MNN GPU (7.88 J), Qwen3-4B F16 llama.cpp CPU (45.2 J), Qwen3-4B F16 llama.cpp GPU (10.8 J), Qwen3.5-4B Q4_0 llama.cpp CPU (7.36 J), Qwen3.5-4B Q4 MNN CPU (6.95 J), Qwen3.5-4B Q4_0 llama.cpp GPU (5.01 J), Qwen3.5-4B Q4 MNN GPU (3.75 J), Qwen3.5-4B Q8 MNN CPU (11.4 J), Qwen3.5-4B Q8_0 llama.cpp GPU (6.29 J), Qwen3.5-4B Q8 MNN GPU (6.35 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### H07

**Hard · OnePlus 8 Pro · Screen-reader descriptions**: describe a screen's content aloud; 400-token prompt, 60-token answer, first token within 2 s, 100 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,024 MB · prefill ≥ 200 tokens/s · decode ≥ 5 tokens/s · energy ≤ 18.8 J per request.

**Pick: Qwen3.5-0.8B Q4, MNN, GPU** (583 MB, 207 / 24.1 tokens/s, 11.1 J). Kept 26 of 104 configurations (25%); 53 filtered out; 25 cannot run on this phone.

![H07](figures/scenarios/H07.png)

<details><summary>Kept pool (26), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q4 MNN GPU **(pick)** | 583 | 207 | 24.1 | 11.1 |
| Qwen3-0.6B Q8 MNN GPU | 844 | 213 | 8.89 | 18.8 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 261 | 48.2 | 18.1 |
| Qwen3-0.6B Q4 MNN CPU | 537 | 227 | 42.8 | 17.6 |
| Qwen3-0.6B Q4 MNN GPU | 546 | 214 | 9.15 | 14.5 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 825 | 377 | 51.2 | 15 |
| SmolLM2-360M Q8 MNN CPU | 492 | 342 | 44.6 | 15 |
| SmolLM2-360M Q8 MNN GPU | 514 | 298 | 9.25 | 13.4 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 11.7 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 11.8 |
| SmolLM2-360M Q4 MNN GPU | 333 | 299 | 9.3 | 11.4 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 484 | 42.8 | 16.8 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,013 | 70.4 | 8.67 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 10.2 |
| Gemma3-270M Q8 MNN GPU | 430 | 736 | 8.35 | 9.59 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 1,015 | 84.3 | 7.21 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 7.75 |
| Gemma3-270M Q4 MNN GPU | 295 | 746 | 8.27 | 8.5 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 13 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 9.07 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 6.29 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 6.16 |
| SmolLM2-135M Q8 MNN GPU | 253 | 743 | 9.84 | 7.26 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 5.11 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 4.82 |
| SmolLM2-135M Q4 MNN GPU | 186 | 743 | 9.88 | 6.72 |

</details>

<details><summary>Filtered out (53) and cannot run (25)</summary>

- **Peak memory over 1,024 MB (43):** Gemma3-270M Q4_0 llama.cpp GPU (1,429 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,478 MB), Gemma3-270M F16 MNN CPU (1,435 MB), Gemma3-270M F16 llama.cpp GPU (1,886 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,216 MB), SmolLM2-360M F16 llama.cpp GPU (1,389 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,315 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,440 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,908 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,811 MB), Qwen3-0.6B F16 llama.cpp GPU (2,344 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,114 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,642 MB), Qwen3.5-0.8B F16 MNN CPU (2,906 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,270 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (2,753 MB), Qwen3-1.7B Q4 MNN GPU (1,211 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,678 MB), Qwen3-1.7B Q8 MNN CPU (2,416 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,474 MB), Qwen3-1.7B Q8 MNN GPU (2,072 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,218 MB), Qwen3.5-2B Q4 MNN GPU (1,259 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,974 MB), Qwen3.5-2B Q8 MNN GPU (2,221 MB), Qwen3.5-2B F16 llama.cpp CPU (3,908 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,560 MB), Qwen3-4B Q4 MNN CPU (2,755 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q4 MNN GPU (2,580 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3-4B Q8 MNN GPU (4,179 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q4 MNN CPU (2,875 MB), Qwen3.5-4B Q4 MNN GPU (2,655 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 200 tokens/s (47):** SmolLM2-135M Q4_0 llama.cpp GPU (105), SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q4_0 llama.cpp GPU (77.8), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q4_0 llama.cpp GPU (58), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp CPU (161), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q4_0 llama.cpp GPU (35.1), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B F16 llama.cpp CPU (99.5), Qwen3-0.6B F16 MNN CPU (168), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3.5-0.8B Q4 MNN CPU (167), Qwen3.5-0.8B Q8 MNN CPU (182), Qwen3.5-0.8B Q8 MNN GPU (182), Qwen3.5-0.8B F16 llama.cpp CPU (80.2), Qwen3.5-0.8B F16 MNN CPU (153), Qwen3-1.7B Q4_0 llama.cpp CPU (99.3), Qwen3-1.7B Q4 MNN CPU (81.8), Qwen3-1.7B Q4_0 llama.cpp GPU (28.9), Qwen3-1.7B Q4 MNN GPU (77.3), Qwen3-1.7B Q8_0 llama.cpp CPU (96.8), Qwen3-1.7B Q8 MNN CPU (83.2), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B Q8 MNN GPU (73.8), Qwen3-1.7B F16 llama.cpp CPU (27.4), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3.5-2B Q4_0 llama.cpp CPU (98.8), Qwen3.5-2B Q4 MNN CPU (77.9), Qwen3.5-2B Q4 MNN GPU (102), Qwen3.5-2B Q8_0 llama.cpp CPU (94), Qwen3.5-2B Q8 MNN CPU (81.6), Qwen3.5-2B Q8 MNN GPU (83.7), Qwen3.5-2B F16 llama.cpp CPU (28.2), Qwen3-4B Q4_0 llama.cpp CPU (41.1), Qwen3-4B Q4 MNN CPU (32.1), Qwen3-4B Q4_0 llama.cpp GPU (10.9), Qwen3-4B Q4 MNN GPU (31.1), Qwen3-4B Q8 MNN CPU (31), Qwen3-4B Q8 MNN GPU (26.4), Qwen3.5-4B Q4_0 llama.cpp CPU (39.8), Qwen3.5-4B Q4 MNN CPU (29.6), Qwen3.5-4B Q4 MNN GPU (39.5), Qwen3.5-4B Q8 MNN GPU (32.7)
- **Decode under 5 tokens/s (10):** Qwen3-0.6B Q4_0 llama.cpp GPU (3.49), Qwen3-0.6B Q8_0 llama.cpp GPU (4.35), Qwen3-0.6B F16 llama.cpp GPU (4.5), Qwen3-1.7B Q4_0 llama.cpp GPU (2.58), Qwen3-1.7B Q8_0 llama.cpp GPU (3.24), Qwen3-1.7B F16 llama.cpp GPU (3.09), Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q8 MNN CPU (4.85), Qwen3-4B Q8 MNN GPU (0.71), Qwen3.5-4B Q8 MNN GPU (3.96)
- **Energy over 18.8 J per request (45):** SmolLM2-135M Q8_0 llama.cpp GPU (73.3 J), SmolLM2-135M F16 llama.cpp GPU (141 J), Gemma3-270M Q4_0 llama.cpp GPU (19.2 J), Gemma3-270M Q8_0 llama.cpp GPU (77.6 J), Gemma3-270M F16 llama.cpp GPU (132 J), SmolLM2-360M Q4_0 llama.cpp GPU (21.9 J), SmolLM2-360M Q8_0 llama.cpp GPU (209 J), SmolLM2-360M F16 llama.cpp CPU (33.8 J), SmolLM2-360M F16 MNN CPU (23.2 J), Qwen3-0.6B Q4_0 llama.cpp GPU (41.6 J), Qwen3-0.6B Q8_0 llama.cpp CPU (22.6 J), Qwen3-0.6B Q8 MNN CPU (23.4 J), Qwen3-0.6B F16 llama.cpp CPU (52.2 J), Qwen3-0.6B F16 MNN CPU (36.2 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (23.8 J), Qwen3.5-0.8B Q4 MNN CPU (19.4 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (30.9 J), Qwen3.5-0.8B Q8 MNN CPU (26 J), Qwen3.5-0.8B F16 llama.cpp CPU (63.3 J), Qwen3.5-0.8B F16 MNN CPU (41.7 J), Qwen3-1.7B Q4_0 llama.cpp CPU (43.4 J), Qwen3-1.7B Q4 MNN CPU (48.8 J), Qwen3-1.7B Q4_0 llama.cpp GPU (63 J), Qwen3-1.7B Q4 MNN GPU (34.1 J), Qwen3-1.7B Q8_0 llama.cpp CPU (63.9 J), Qwen3-1.7B Q8 MNN CPU (65.3 J), Qwen3-1.7B Q8 MNN GPU (47.7 J), Qwen3-1.7B F16 llama.cpp CPU (151 J), Qwen3.5-2B Q4_0 llama.cpp CPU (47.6 J), Qwen3.5-2B Q4 MNN CPU (44 J), Qwen3.5-2B Q4 MNN GPU (25 J), Qwen3.5-2B Q8_0 llama.cpp CPU (68.7 J), Qwen3.5-2B Q8 MNN CPU (60.4 J), Qwen3.5-2B Q8 MNN GPU (40.7 J), Qwen3.5-2B F16 llama.cpp CPU (157 J), Qwen3-4B Q4_0 llama.cpp CPU (102 J), Qwen3-4B Q4 MNN CPU (119 J), Qwen3-4B Q4_0 llama.cpp GPU (151 J), Qwen3-4B Q4 MNN GPU (81.4 J), Qwen3-4B Q8 MNN CPU (157 J), Qwen3-4B Q8 MNN GPU (169 J), Qwen3.5-4B Q4_0 llama.cpp CPU (110 J), Qwen3.5-4B Q4 MNN CPU (103 J), Qwen3.5-4B Q4 MNN GPU (62.2 J), Qwen3.5-4B Q8 MNN GPU (98 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### H08

**Hard · Redmi 9A · Recipe / how-to helper**: step-by-step instructions; 150-token prompt, 300-token answer, first token within 5 s, 10 requests ≤ 1% of battery a day.

Limits: peak memory ≤ 2,048 MB · prefill ≥ 30 tokens/s · decode ≥ 5 tokens/s · energy ≤ 69.5 J per request.

**Pick: Gemma3-270M Q8, MNN, CPU** (745 MB, 35.2 / 6.71 tokens/s, 51.8 J). Kept 5 of 52 configurations (10%); 29 filtered out; 18 cannot run on this phone.

![H08](figures/scenarios/H08.png)

<details><summary>Kept pool (5), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Gemma3-270M Q8 MNN CPU **(pick)** | 745 | 35.2 | 6.71 | 51.8 |
| Gemma3-270M Q4 MNN CPU | 326 | 39.9 | 7.72 | 46.8 |
| SmolLM2-135M F16 MNN CPU | 637 | 43 | 8.17 | 48.8 |
| SmolLM2-135M Q8 MNN CPU | 225 | 32.5 | 13.2 | 28 |
| SmolLM2-135M Q4 MNN CPU | 158 | 36.5 | 16.3 | 23.1 |

</details>

<details><summary>Filtered out (29) and cannot run (18)</summary>

- **Peak memory over 2,048 MB (2):** Qwen3-1.7B Q8 MNN CPU (2,357 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (2,116 MB)
- **Prefill under 30 tokens/s (28):** SmolLM2-135M Q4_0 llama.cpp CPU (11.6), SmolLM2-135M Q8_0 llama.cpp CPU (11.2), SmolLM2-135M F16 llama.cpp CPU (1.54), Gemma3-270M Q4_0 llama.cpp CPU (11.7), Gemma3-270M Q8_0 llama.cpp CPU (11.3), Gemma3-270M F16 llama.cpp CPU (1.64), SmolLM2-360M Q4_0 llama.cpp CPU (5.39), SmolLM2-360M Q4 MNN CPU (14.8), SmolLM2-360M Q8_0 llama.cpp CPU (5.14), SmolLM2-360M Q8 MNN CPU (12.7), SmolLM2-360M F16 llama.cpp CPU (0.54), SmolLM2-360M F16 MNN CPU (15.9), Qwen3-0.6B Q4_0 llama.cpp CPU (3.31), Qwen3-0.6B Q4 MNN CPU (11.4), Qwen3-0.6B Q8_0 llama.cpp CPU (3.18), Qwen3-0.6B Q8 MNN CPU (9.59), Qwen3-0.6B F16 llama.cpp CPU (0.38), Qwen3.5-0.8B Q4_0 llama.cpp CPU (4.59), Qwen3.5-0.8B Q4 MNN CPU (9.1), Qwen3.5-0.8B Q8_0 llama.cpp CPU (4.31), Qwen3.5-0.8B Q8 MNN CPU (7.75), Qwen3.5-0.8B F16 llama.cpp CPU (0.33), Qwen3-1.7B Q4_0 llama.cpp CPU (1.78), Qwen3-1.7B Q4 MNN CPU (4.75), Qwen3-1.7B Q8 MNN CPU (3.76), Qwen3.5-2B Q4_0 llama.cpp CPU (2.2), Qwen3.5-2B Q4 MNN CPU (4.58), Qwen3.5-2B Q8_0 llama.cpp CPU (2.03)
- **Decode under 5 tokens/s (22):** SmolLM2-135M F16 llama.cpp CPU (1.09), Gemma3-270M Q4_0 llama.cpp CPU (4.65), Gemma3-270M Q8_0 llama.cpp CPU (4.56), Gemma3-270M F16 llama.cpp CPU (0.59), Gemma3-270M F16 MNN CPU (4.2), SmolLM2-360M Q4_0 llama.cpp CPU (2.95), SmolLM2-360M Q8_0 llama.cpp CPU (2.82), SmolLM2-360M F16 llama.cpp CPU (0.43), SmolLM2-360M F16 MNN CPU (4.1), Qwen3-0.6B Q4_0 llama.cpp CPU (1.68), Qwen3-0.6B Q8_0 llama.cpp CPU (1.67), Qwen3-0.6B F16 llama.cpp CPU (0.26), Qwen3.5-0.8B Q4_0 llama.cpp CPU (2.17), Qwen3.5-0.8B Q8_0 llama.cpp CPU (2.2), Qwen3.5-0.8B Q8 MNN CPU (4.65), Qwen3.5-0.8B F16 llama.cpp CPU (0.21), Qwen3-1.7B Q4_0 llama.cpp CPU (1.07), Qwen3-1.7B Q4 MNN CPU (3.35), Qwen3-1.7B Q8 MNN CPU (2.49), Qwen3.5-2B Q4_0 llama.cpp CPU (1.24), Qwen3.5-2B Q4 MNN CPU (3.25), Qwen3.5-2B Q8_0 llama.cpp CPU (1.2)
- **Energy over 69.5 J per request (25):** SmolLM2-135M F16 llama.cpp CPU (295 J), Gemma3-270M Q4_0 llama.cpp CPU (74.9 J), Gemma3-270M Q8_0 llama.cpp CPU (84.1 J), Gemma3-270M F16 llama.cpp CPU (472 J), Gemma3-270M F16 MNN CPU (93.9 J), SmolLM2-360M Q4_0 llama.cpp CPU (113 J), SmolLM2-360M Q8_0 llama.cpp CPU (133 J), SmolLM2-360M F16 llama.cpp CPU (770 J), SmolLM2-360M F16 MNN CPU (116 J), Qwen3-0.6B Q4_0 llama.cpp CPU (194 J), Qwen3-0.6B Q4 MNN CPU (69.9 J), Qwen3-0.6B Q8_0 llama.cpp CPU (225 J), Qwen3-0.6B Q8 MNN CPU (86.4 J), Qwen3-0.6B F16 llama.cpp CPU (1,212 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (209 J), Qwen3.5-0.8B Q4 MNN CPU (89 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (244 J), Qwen3.5-0.8B Q8 MNN CPU (113 J), Qwen3.5-0.8B F16 llama.cpp CPU (1,833 J), Qwen3-1.7B Q4_0 llama.cpp CPU (362 J), Qwen3-1.7B Q4 MNN CPU (154 J), Qwen3-1.7B Q8 MNN CPU (243 J), Qwen3.5-2B Q4_0 llama.cpp CPU (409 J), Qwen3.5-2B Q4 MNN CPU (177 J), Qwen3.5-2B Q8_0 llama.cpp CPU (462 J)
- **Cannot run on this phone (18):** Qwen3-0.6B F16 MNN CPU (does not fit in RAM), Qwen3.5-0.8B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B Q8_0 llama.cpp CPU (crash/error (exit 1)), Qwen3-1.7B F16 llama.cpp CPU (does not fit in RAM), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B Q8 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp CPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q4 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q4 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM)

</details>

### H09

**Hard · Galaxy S23 · Coding helper**: explain or fix a code snippet; 1,000-token prompt, 150-token answer, first token within 3 s, 30 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 3,072 MB · prefill ≥ 333 tokens/s · decode ≥ 15 tokens/s · energy ≤ 54 J per request.

**Pick: Qwen3.5-0.8B Q8, MNN, GPU** (1,083 MB, 379 / 35.1 tokens/s, 37.5 J). Kept 21 of 104 configurations (20%); 61 filtered out; 22 cannot run on this phone.

![H09](figures/scenarios/H09.png)

<details><summary>Kept pool (21), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q8 MNN GPU **(pick)** | 1,083 | 379 | 35.1 | 37.5 |
| Qwen3.5-0.8B Q4 MNN GPU | 712 | 454 | 46.1 | 23.9 |
| Qwen3-0.6B Q8 MNN CPU | 856 | 368 | 49.8 | 45 |
| Qwen3-0.6B Q4 MNN CPU | 548 | 371 | 72.7 | 35.6 |
| SmolLM2-360M F16 MNN CPU | 826 | 405 | 40.8 | 45.9 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 28.9 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 348 | 75.2 | 37.3 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 23.5 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 30.9 |
| Gemma3-270M F16 MNN CPU | 1,487 | 1,082 | 56.2 | 29.3 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 21.2 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 20.9 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 18.8 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 16.7 |
| Gemma3-270M Q4_0 llama.cpp GPU | 1,498 | 361 | 33.3 | 24 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 17.7 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 12.6 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 16 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 10.7 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 14.3 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 19.1 |

</details>

<details><summary>Filtered out (61) and cannot run (22)</summary>

- **Peak memory over 3,072 MB (15):** Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB)
- **Prefill under 333 tokens/s (53):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp CPU (120), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp CPU (127), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q4_0 llama.cpp GPU (219), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp CPU (42.1), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q4_0 llama.cpp CPU (232), Qwen3-0.6B Q4_0 llama.cpp GPU (133), Qwen3-0.6B Q8_0 llama.cpp CPU (216), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 MNN CPU (240), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B Q4_0 llama.cpp CPU (291), Qwen3.5-0.8B Q4 MNN CPU (273), Qwen3.5-0.8B Q4_0 llama.cpp GPU (157), Qwen3.5-0.8B Q8_0 llama.cpp CPU (260), Qwen3.5-0.8B Q8 MNN CPU (274), Qwen3.5-0.8B Q8_0 llama.cpp GPU (163), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3.5-0.8B F16 MNN CPU (217), Qwen3.5-0.8B F16 llama.cpp GPU (159), Qwen3-1.7B Q4_0 llama.cpp CPU (119), Qwen3-1.7B Q4 MNN CPU (138), Qwen3-1.7B Q4_0 llama.cpp GPU (107), Qwen3-1.7B Q4 MNN GPU (219), Qwen3-1.7B Q8_0 llama.cpp CPU (106), Qwen3-1.7B Q8 MNN CPU (132), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B Q8 MNN GPU (211), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B Q4_0 llama.cpp CPU (132), Qwen3.5-2B Q4 MNN CPU (134), Qwen3.5-2B Q4_0 llama.cpp GPU (109), Qwen3.5-2B Q4 MNN GPU (252), Qwen3.5-2B Q8_0 llama.cpp CPU (112), Qwen3.5-2B Q8 MNN CPU (128), Qwen3.5-2B Q8_0 llama.cpp GPU (113), Qwen3.5-2B Q8 MNN GPU (203), Qwen3.5-2B F16 llama.cpp CPU (9.4), Qwen3-4B Q4_0 llama.cpp CPU (44.5), Qwen3-4B Q4 MNN CPU (53.5), Qwen3-4B Q4_0 llama.cpp GPU (40.4), Qwen3-4B Q4 MNN GPU (88.4), Qwen3.5-4B Q4_0 llama.cpp CPU (50.8), Qwen3.5-4B Q4 MNN CPU (49.9), Qwen3.5-4B Q4_0 llama.cpp GPU (47.5), Qwen3.5-4B Q4 MNN GPU (115)
- **Decode under 15 tokens/s (27):** SmolLM2-135M Q4 MNN GPU (9.13), SmolLM2-135M Q8 MNN GPU (9.09), Gemma3-270M Q4 MNN GPU (8.98), Gemma3-270M Q8 MNN GPU (8.89), SmolLM2-360M Q4 MNN GPU (9.09), SmolLM2-360M Q8 MNN GPU (8.56), Qwen3-0.6B Q4_0 llama.cpp GPU (14.9), Qwen3-0.6B Q4 MNN GPU (9.74), Qwen3-0.6B Q8 MNN GPU (8.45), Qwen3.5-0.8B Q4_0 llama.cpp GPU (13.4), Qwen3.5-0.8B F16 llama.cpp CPU (13.7), Qwen3.5-0.8B F16 llama.cpp GPU (11.2), Qwen3-1.7B Q4_0 llama.cpp GPU (12.2), Qwen3-1.7B Q4 MNN GPU (8.52), Qwen3-1.7B Q8 MNN GPU (7.99), Qwen3-1.7B F16 llama.cpp CPU (5.73), Qwen3-1.7B F16 llama.cpp GPU (11.2), Qwen3.5-2B Q4_0 llama.cpp GPU (11.3), Qwen3.5-2B Q8_0 llama.cpp GPU (11.3), Qwen3.5-2B F16 llama.cpp CPU (5.25), Qwen3-4B Q4_0 llama.cpp CPU (10.8), Qwen3-4B Q4 MNN CPU (14.1), Qwen3-4B Q4_0 llama.cpp GPU (8.69), Qwen3-4B Q4 MNN GPU (6.71), Qwen3.5-4B Q4_0 llama.cpp CPU (10), Qwen3.5-4B Q4 MNN CPU (14.6), Qwen3.5-4B Q4_0 llama.cpp GPU (6.45)
- **Energy over 54 J per request (47):** SmolLM2-135M Q8_0 llama.cpp GPU (141 J), SmolLM2-135M F16 llama.cpp CPU (64.6 J), SmolLM2-135M F16 llama.cpp GPU (167 J), Gemma3-270M Q8_0 llama.cpp GPU (136 J), Gemma3-270M F16 llama.cpp CPU (74.3 J), Gemma3-270M F16 llama.cpp GPU (160 J), SmolLM2-360M Q8_0 llama.cpp GPU (398 J), SmolLM2-360M F16 llama.cpp CPU (173 J), SmolLM2-360M F16 llama.cpp GPU (470 J), Qwen3-0.6B Q4_0 llama.cpp GPU (55.4 J), Qwen3-0.6B Q8_0 llama.cpp CPU (57.2 J), Qwen3-0.6B Q8_0 llama.cpp GPU (570 J), Qwen3-0.6B F16 llama.cpp CPU (240 J), Qwen3-0.6B F16 MNN CPU (74.2 J), Qwen3-0.6B F16 llama.cpp GPU (702 J), Qwen3.5-0.8B Q4_0 llama.cpp GPU (59.6 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (59.8 J), Qwen3.5-0.8B Q8_0 llama.cpp GPU (64.2 J), Qwen3.5-0.8B F16 llama.cpp CPU (264 J), Qwen3.5-0.8B F16 MNN CPU (85.2 J), Qwen3.5-0.8B F16 llama.cpp GPU (86.6 J), Qwen3-1.7B Q4_0 llama.cpp CPU (101 J), Qwen3-1.7B Q4 MNN CPU (88.1 J), Qwen3-1.7B Q4_0 llama.cpp GPU (91.1 J), Qwen3-1.7B Q4 MNN GPU (70 J), Qwen3-1.7B Q8_0 llama.cpp CPU (125 J), Qwen3-1.7B Q8 MNN CPU (106 J), Qwen3-1.7B Q8_0 llama.cpp GPU (1,516 J), Qwen3-1.7B Q8 MNN GPU (92.9 J), Qwen3-1.7B F16 llama.cpp CPU (710 J), Qwen3-1.7B F16 llama.cpp GPU (2,270 J), Qwen3.5-2B Q4_0 llama.cpp CPU (105 J), Qwen3.5-2B Q4 MNN CPU (91.4 J), Qwen3.5-2B Q4_0 llama.cpp GPU (81.8 J), Qwen3.5-2B Q8_0 llama.cpp CPU (128 J), Qwen3.5-2B Q8 MNN CPU (112 J), Qwen3.5-2B Q8_0 llama.cpp GPU (107 J), Qwen3.5-2B Q8 MNN GPU (81.1 J), Qwen3.5-2B F16 llama.cpp CPU (668 J), Qwen3-4B Q4_0 llama.cpp CPU (234 J), Qwen3-4B Q4 MNN CPU (202 J), Qwen3-4B Q4_0 llama.cpp GPU (207 J), Qwen3-4B Q4 MNN GPU (162 J), Qwen3.5-4B Q4_0 llama.cpp CPU (237 J), Qwen3.5-4B Q4 MNN CPU (201 J), Qwen3.5-4B Q4_0 llama.cpp GPU (197 J), Qwen3.5-4B Q4 MNN GPU (124 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### H10

**Hard · OnePlus 15 · Keyboard next-phrase**: suggest the next words while typing; 64-token prompt, 8-token answer, first token within 0.3 s, 1,000 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 300 MB · prefill ≥ 213 tokens/s · decode ≥ 20 tokens/s · energy ≤ 3.03 J per request.

**Pick: Gemma3-270M Q4, MNN, CPU** (285 MB, 1,278 / 116 tokens/s, 0.246 J). Kept 5 of 104 configurations (5%); 88 filtered out; 11 cannot run on this phone.

![H10](figures/scenarios/H10.png)

<details><summary>Kept pool (5), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Gemma3-270M Q4 MNN CPU **(pick)** | 285 | 1,278 | 116 | 0.246 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 0.232 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 0.179 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 0.156 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 0.342 |

</details>

<details><summary>Filtered out (88) and cannot run (11)</summary>

- **Peak memory over 300 MB (88):** SmolLM2-135M Q4_0 llama.cpp GPU (791 MB), SmolLM2-135M Q8_0 llama.cpp CPU (326 MB), SmolLM2-135M Q8_0 llama.cpp GPU (846 MB), SmolLM2-135M Q8 MNN GPU (342 MB), SmolLM2-135M F16 llama.cpp CPU (309 MB), SmolLM2-135M F16 MNN CPU (352 MB), SmolLM2-135M F16 llama.cpp GPU (1,265 MB), Gemma3-270M Q4_0 llama.cpp CPU (535 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,538 MB), Gemma3-270M Q4 MNN GPU (391 MB), Gemma3-270M Q8_0 llama.cpp CPU (634 MB), Gemma3-270M Q8 MNN CPU (694 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,591 MB), Gemma3-270M Q8 MNN GPU (524 MB), Gemma3-270M F16 llama.cpp CPU (602 MB), Gemma3-270M F16 MNN CPU (1,528 MB), Gemma3-270M F16 llama.cpp GPU (2,534 MB), SmolLM2-360M Q4_0 llama.cpp CPU (509 MB), SmolLM2-360M Q4 MNN CPU (340 MB), SmolLM2-360M Q4_0 llama.cpp GPU (1,009 MB), SmolLM2-360M Q4 MNN GPU (444 MB), SmolLM2-360M Q8_0 llama.cpp CPU (824 MB), SmolLM2-360M Q8 MNN CPU (509 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,326 MB), SmolLM2-360M Q8 MNN GPU (624 MB), SmolLM2-360M F16 llama.cpp CPU (779 MB), SmolLM2-360M F16 MNN CPU (1,035 MB), SmolLM2-360M F16 llama.cpp GPU (2,225 MB), Qwen3-0.6B Q4_0 llama.cpp CPU (833 MB), Qwen3-0.6B Q4 MNN CPU (589 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,552 MB), Qwen3-0.6B Q4 MNN GPU (707 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,291 MB), Qwen3-0.6B Q8 MNN CPU (1,029 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (2,017 MB), Qwen3-0.6B Q8 MNN GPU (1,002 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (2,081 MB), Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,115 MB), Qwen3.5-0.8B Q4 MNN CPU (568 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,883 MB), Qwen3.5-0.8B Q4 MNN GPU (733 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B Q8 MNN CPU (940 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,273 MB), Qwen3.5-0.8B Q8 MNN GPU (1,108 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,286 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q4 MNN GPU (1,443 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B Q8 MNN GPU (2,479 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,512 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q4 MNN GPU (1,470 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8 MNN CPU (2,206 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 213 tokens/s (29):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3-0.6B F16 MNN CPU (184), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3-1.7B Q4_0 llama.cpp CPU (144), Qwen3-1.7B Q4 MNN CPU (103), Qwen3-1.7B Q8_0 llama.cpp CPU (150), Qwen3-1.7B Q8 MNN CPU (136), Qwen3-1.7B Q8_0 llama.cpp GPU (164), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3.5-2B Q4_0 llama.cpp CPU (150), Qwen3.5-2B Q4 MNN CPU (158), Qwen3.5-2B Q8_0 llama.cpp CPU (159), Qwen3.5-2B Q8 MNN CPU (169), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3.5-2B F16 MNN CPU (113), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q4 MNN GPU (191), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B Q8 MNN GPU (180), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6), Qwen3.5-4B Q8 MNN GPU (204)
- **Decode under 20 tokens/s (21):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (13.6), Qwen3-1.7B F16 llama.cpp GPU (17.1), Qwen3.5-2B F16 llama.cpp CPU (14.7), Qwen3.5-2B F16 MNN CPU (14.1), Qwen3.5-2B F16 llama.cpp GPU (16.4), Qwen3-4B Q4_0 llama.cpp CPU (16.3), Qwen3-4B Q4 MNN CPU (16.8), Qwen3-4B Q4_0 llama.cpp GPU (16.7), Qwen3-4B Q4 MNN GPU (19.7), Qwen3-4B Q8_0 llama.cpp CPU (13.8), Qwen3-4B Q8_0 llama.cpp GPU (12.7), Qwen3-4B Q8 MNN GPU (12.6), Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42), Qwen3.5-4B Q4_0 llama.cpp CPU (15.9), Qwen3.5-4B Q4 MNN CPU (16.9), Qwen3.5-4B Q4_0 llama.cpp GPU (14.5), Qwen3.5-4B Q8 MNN CPU (14.2), Qwen3.5-4B Q8_0 llama.cpp GPU (12.6), Qwen3.5-4B Q8 MNN GPU (13.4)
- **Energy over 3.03 J per request (22):** Qwen3-0.6B F16 llama.cpp CPU (3.46 J), Qwen3.5-0.8B F16 llama.cpp CPU (3.82 J), Qwen3-1.7B F16 llama.cpp CPU (10.9 J), Qwen3-1.7B F16 MNN CPU (4.34 J), Qwen3-1.7B F16 llama.cpp GPU (3.21 J), Qwen3.5-2B F16 llama.cpp CPU (9.63 J), Qwen3.5-2B F16 MNN CPU (3.63 J), Qwen3-4B Q4_0 llama.cpp CPU (4.81 J), Qwen3-4B Q4 MNN CPU (5.44 J), Qwen3-4B Q4_0 llama.cpp GPU (3.67 J), Qwen3-4B Q4 MNN GPU (4.26 J), Qwen3-4B Q8_0 llama.cpp CPU (5.74 J), Qwen3-4B Q8_0 llama.cpp GPU (6.31 J), Qwen3-4B Q8 MNN GPU (5.74 J), Qwen3-4B F16 llama.cpp CPU (25.7 J), Qwen3-4B F16 llama.cpp GPU (8.05 J), Qwen3.5-4B Q4_0 llama.cpp CPU (4.86 J), Qwen3.5-4B Q4 MNN CPU (4.21 J), Qwen3.5-4B Q4_0 llama.cpp GPU (3.64 J), Qwen3.5-4B Q8 MNN CPU (7.02 J), Qwen3.5-4B Q8_0 llama.cpp GPU (4.78 J), Qwen3.5-4B Q8 MNN GPU (4.87 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### H11

**Hard · OnePlus 8 Pro · Smart replies**: three reply chips under a received message; 128-token prompt, 24-token answer, first token within 0.5 s, 200 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 512 MB · prefill ≥ 256 tokens/s · decode ≥ 15 tokens/s · energy ≤ 6.26 J per request.

**Pick: SmolLM2-360M Q8, MNN, CPU** (492 MB, 342 / 44.6 tokens/s, 5.49 J). Kept 11 of 104 configurations (11%); 68 filtered out; 25 cannot run on this phone.

![H11](figures/scenarios/H11.png)

<details><summary>Kept pool (11), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| SmolLM2-360M Q8 MNN CPU **(pick)** | 492 | 342 | 44.6 | 5.49 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 4.19 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 4.21 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 3.89 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 2.92 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 4.61 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 3.38 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 2.3 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 2.27 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 1.85 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 1.75 |

</details>

<details><summary>Filtered out (68) and cannot run (25)</summary>

- **Peak memory over 512 MB (63):** SmolLM2-135M Q4_0 llama.cpp GPU (681 MB), SmolLM2-135M Q8_0 llama.cpp GPU (734 MB), SmolLM2-135M F16 llama.cpp GPU (886 MB), Gemma3-270M Q4_0 llama.cpp CPU (534 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,429 MB), Gemma3-270M Q8_0 llama.cpp CPU (634 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,478 MB), Gemma3-270M F16 llama.cpp CPU (602 MB), Gemma3-270M F16 MNN CPU (1,435 MB), Gemma3-270M F16 llama.cpp GPU (1,886 MB), SmolLM2-360M Q4_0 llama.cpp GPU (901 MB), SmolLM2-360M Q8_0 llama.cpp CPU (825 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,216 MB), SmolLM2-360M Q8 MNN GPU (514 MB), SmolLM2-360M F16 llama.cpp CPU (779 MB), SmolLM2-360M F16 MNN CPU (803 MB), SmolLM2-360M F16 llama.cpp GPU (1,389 MB), Qwen3-0.6B Q4_0 llama.cpp CPU (833 MB), Qwen3-0.6B Q4 MNN CPU (537 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,315 MB), Qwen3-0.6B Q4 MNN GPU (546 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,440 MB), Qwen3-0.6B Q8 MNN CPU (962 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,908 MB), Qwen3-0.6B Q8 MNN GPU (844 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,811 MB), Qwen3-0.6B F16 llama.cpp GPU (2,344 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,114 MB), Qwen3.5-0.8B Q4 MNN CPU (550 MB), Qwen3.5-0.8B Q4 MNN GPU (583 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B Q8 MNN CPU (926 MB), Qwen3.5-0.8B Q8 MNN GPU (959 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,642 MB), Qwen3.5-0.8B F16 MNN CPU (2,906 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,270 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (2,753 MB), Qwen3-1.7B Q4 MNN GPU (1,211 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,678 MB), Qwen3-1.7B Q8 MNN CPU (2,416 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,474 MB), Qwen3-1.7B Q8 MNN GPU (2,072 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,218 MB), Qwen3.5-2B Q4 MNN GPU (1,259 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,974 MB), Qwen3.5-2B Q8 MNN GPU (2,221 MB), Qwen3.5-2B F16 llama.cpp CPU (3,908 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,560 MB), Qwen3-4B Q4 MNN CPU (2,755 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q4 MNN GPU (2,580 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3-4B Q8 MNN GPU (4,179 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q4 MNN CPU (2,875 MB), Qwen3.5-4B Q4 MNN GPU (2,655 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 256 tokens/s (54):** SmolLM2-135M Q4_0 llama.cpp GPU (105), SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q4_0 llama.cpp GPU (77.8), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q4_0 llama.cpp GPU (58), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp CPU (161), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q4 MNN CPU (227), Qwen3-0.6B Q4_0 llama.cpp GPU (35.1), Qwen3-0.6B Q4 MNN GPU (214), Qwen3-0.6B Q8 MNN CPU (239), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B Q8 MNN GPU (213), Qwen3-0.6B F16 llama.cpp CPU (99.5), Qwen3-0.6B F16 MNN CPU (168), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3.5-0.8B Q4_0 llama.cpp CPU (207), Qwen3.5-0.8B Q4 MNN CPU (167), Qwen3.5-0.8B Q4 MNN GPU (207), Qwen3.5-0.8B Q8_0 llama.cpp CPU (204), Qwen3.5-0.8B Q8 MNN CPU (182), Qwen3.5-0.8B Q8 MNN GPU (182), Qwen3.5-0.8B F16 llama.cpp CPU (80.2), Qwen3.5-0.8B F16 MNN CPU (153), Qwen3-1.7B Q4_0 llama.cpp CPU (99.3), Qwen3-1.7B Q4 MNN CPU (81.8), Qwen3-1.7B Q4_0 llama.cpp GPU (28.9), Qwen3-1.7B Q4 MNN GPU (77.3), Qwen3-1.7B Q8_0 llama.cpp CPU (96.8), Qwen3-1.7B Q8 MNN CPU (83.2), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B Q8 MNN GPU (73.8), Qwen3-1.7B F16 llama.cpp CPU (27.4), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3.5-2B Q4_0 llama.cpp CPU (98.8), Qwen3.5-2B Q4 MNN CPU (77.9), Qwen3.5-2B Q4 MNN GPU (102), Qwen3.5-2B Q8_0 llama.cpp CPU (94), Qwen3.5-2B Q8 MNN CPU (81.6), Qwen3.5-2B Q8 MNN GPU (83.7), Qwen3.5-2B F16 llama.cpp CPU (28.2), Qwen3-4B Q4_0 llama.cpp CPU (41.1), Qwen3-4B Q4 MNN CPU (32.1), Qwen3-4B Q4_0 llama.cpp GPU (10.9), Qwen3-4B Q4 MNN GPU (31.1), Qwen3-4B Q8 MNN CPU (31), Qwen3-4B Q8 MNN GPU (26.4), Qwen3.5-4B Q4_0 llama.cpp CPU (39.8), Qwen3.5-4B Q4 MNN CPU (29.6), Qwen3.5-4B Q4 MNN GPU (39.5), Qwen3.5-4B Q8 MNN GPU (32.7)
- **Decode under 15 tokens/s (45):** SmolLM2-135M Q4_0 llama.cpp GPU (13.3), SmolLM2-135M Q4 MNN GPU (9.88), SmolLM2-135M Q8_0 llama.cpp GPU (9.26), SmolLM2-135M Q8 MNN GPU (9.84), SmolLM2-135M F16 llama.cpp GPU (9.81), Gemma3-270M Q4_0 llama.cpp GPU (7.09), Gemma3-270M Q4 MNN GPU (8.27), Gemma3-270M Q8_0 llama.cpp GPU (6.11), Gemma3-270M Q8 MNN GPU (8.35), Gemma3-270M F16 llama.cpp GPU (6.35), SmolLM2-360M Q4_0 llama.cpp GPU (9.02), SmolLM2-360M Q4 MNN GPU (9.3), SmolLM2-360M Q8_0 llama.cpp GPU (6.17), SmolLM2-360M Q8 MNN GPU (9.25), SmolLM2-360M F16 llama.cpp GPU (6.34), Qwen3-0.6B Q4_0 llama.cpp GPU (3.49), Qwen3-0.6B Q4 MNN GPU (9.15), Qwen3-0.6B Q8_0 llama.cpp GPU (4.35), Qwen3-0.6B Q8 MNN GPU (8.89), Qwen3-0.6B F16 llama.cpp GPU (4.5), Qwen3.5-0.8B F16 llama.cpp CPU (14.2), Qwen3.5-0.8B F16 MNN CPU (14.4), Qwen3-1.7B Q4_0 llama.cpp GPU (2.58), Qwen3-1.7B Q4 MNN GPU (9.06), Qwen3-1.7B Q8_0 llama.cpp CPU (12), Qwen3-1.7B Q8 MNN CPU (10.9), Qwen3-1.7B Q8_0 llama.cpp GPU (3.24), Qwen3-1.7B Q8 MNN GPU (8.38), Qwen3-1.7B F16 llama.cpp CPU (6.88), Qwen3-1.7B F16 llama.cpp GPU (3.09), Qwen3.5-2B Q4 MNN GPU (14.2), Qwen3.5-2B Q8_0 llama.cpp CPU (10.6), Qwen3.5-2B Q8 MNN CPU (11.2), Qwen3.5-2B Q8 MNN GPU (9.33), Qwen3.5-2B F16 llama.cpp CPU (6.07), Qwen3-4B Q4_0 llama.cpp CPU (8.93), Qwen3-4B Q4 MNN CPU (8.66), Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q4 MNN GPU (6.33), Qwen3-4B Q8 MNN CPU (4.85), Qwen3-4B Q8 MNN GPU (0.71), Qwen3.5-4B Q4_0 llama.cpp CPU (8.01), Qwen3.5-4B Q4 MNN CPU (9.25), Qwen3.5-4B Q4 MNN GPU (7.07), Qwen3.5-4B Q8 MNN GPU (3.96)
- **Energy over 6.26 J per request (49):** SmolLM2-135M Q8_0 llama.cpp GPU (24.1 J), SmolLM2-135M F16 llama.cpp GPU (45.8 J), Gemma3-270M Q4_0 llama.cpp GPU (7.02 J), Gemma3-270M Q8_0 llama.cpp GPU (25.9 J), Gemma3-270M F16 llama.cpp GPU (43.7 J), SmolLM2-360M Q4_0 llama.cpp GPU (7.81 J), SmolLM2-360M Q8_0 llama.cpp GPU (68.2 J), SmolLM2-360M F16 llama.cpp CPU (11.9 J), SmolLM2-360M F16 MNN CPU (8.57 J), Qwen3-0.6B Q4_0 llama.cpp CPU (6.54 J), Qwen3-0.6B Q4 MNN CPU (6.33 J), Qwen3-0.6B Q4_0 llama.cpp GPU (15 J), Qwen3-0.6B Q8_0 llama.cpp CPU (8.25 J), Qwen3-0.6B Q8 MNN CPU (8.59 J), Qwen3-0.6B Q8 MNN GPU (7.08 J), Qwen3-0.6B F16 llama.cpp CPU (18.5 J), Qwen3-0.6B F16 MNN CPU (13.4 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (8.59 J), Qwen3.5-0.8B Q4 MNN CPU (6.85 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (11.3 J), Qwen3.5-0.8B Q8 MNN CPU (9.45 J), Qwen3.5-0.8B Q8 MNN GPU (6.53 J), Qwen3.5-0.8B F16 llama.cpp CPU (22.6 J), Qwen3.5-0.8B F16 MNN CPU (15.5 J), Qwen3-1.7B Q4_0 llama.cpp CPU (15.6 J), Qwen3-1.7B Q4 MNN CPU (17.3 J), Qwen3-1.7B Q4_0 llama.cpp GPU (22.9 J), Qwen3-1.7B Q4 MNN GPU (12.5 J), Qwen3-1.7B Q8_0 llama.cpp CPU (23.2 J), Qwen3-1.7B Q8 MNN CPU (23.7 J), Qwen3-1.7B Q8 MNN GPU (17.6 J), Qwen3-1.7B F16 llama.cpp CPU (53.2 J), Qwen3.5-2B Q4_0 llama.cpp CPU (17.2 J), Qwen3.5-2B Q4 MNN CPU (15.6 J), Qwen3.5-2B Q4 MNN GPU (9.31 J), Qwen3.5-2B Q8_0 llama.cpp CPU (25 J), Qwen3.5-2B Q8 MNN CPU (22.1 J), Qwen3.5-2B Q8 MNN GPU (15.2 J), Qwen3.5-2B F16 llama.cpp CPU (55.4 J), Qwen3-4B Q4_0 llama.cpp CPU (36.3 J), Qwen3-4B Q4 MNN CPU (41.8 J), Qwen3-4B Q4_0 llama.cpp GPU (54.1 J), Qwen3-4B Q4 MNN GPU (29.4 J), Qwen3-4B Q8 MNN CPU (56.8 J), Qwen3-4B Q8 MNN GPU (64.4 J), Qwen3.5-4B Q4_0 llama.cpp CPU (39.3 J), Qwen3.5-4B Q4 MNN CPU (36.3 J), Qwen3.5-4B Q4 MNN GPU (22.9 J), Qwen3.5-4B Q8 MNN GPU (36.5 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### H12

**Hard · Redmi 9A · Field-guide app**: explain a plant or bird from a description, offline; 200-token prompt, 250-token answer, first token within 5 s, 10 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 1,500 MB · prefill ≥ 40 tokens/s · decode ≥ 5 tokens/s · energy ≤ 139 J per request.

**Pick: SmolLM2-135M F16, MNN, CPU** (637 MB, 43 / 8.17 tokens/s, 42.3 J). Kept 1 of 52 configurations (2%); 33 filtered out; 18 cannot run on this phone.

![H12](figures/scenarios/H12.png)

<details><summary>Kept pool (1), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| SmolLM2-135M F16 MNN CPU **(pick)** | 637 | 43 | 8.17 | 42.3 |

</details>

<details><summary>Filtered out (33) and cannot run (18)</summary>

- **Peak memory over 1,500 MB (8):** Gemma3-270M F16 MNN CPU (1,681 MB), SmolLM2-360M F16 MNN CPU (1,614 MB), Qwen3-0.6B F16 llama.cpp CPU (1,612 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,615 MB), Qwen3-1.7B Q4 MNN CPU (1,543 MB), Qwen3-1.7B Q8 MNN CPU (2,357 MB), Qwen3.5-2B Q4 MNN CPU (1,663 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (2,116 MB)
- **Prefill under 40 tokens/s (32):** SmolLM2-135M Q4_0 llama.cpp CPU (11.6), SmolLM2-135M Q4 MNN CPU (36.5), SmolLM2-135M Q8_0 llama.cpp CPU (11.2), SmolLM2-135M Q8 MNN CPU (32.5), SmolLM2-135M F16 llama.cpp CPU (1.54), Gemma3-270M Q4_0 llama.cpp CPU (11.7), Gemma3-270M Q4 MNN CPU (39.9), Gemma3-270M Q8_0 llama.cpp CPU (11.3), Gemma3-270M Q8 MNN CPU (35.2), Gemma3-270M F16 llama.cpp CPU (1.64), SmolLM2-360M Q4_0 llama.cpp CPU (5.39), SmolLM2-360M Q4 MNN CPU (14.8), SmolLM2-360M Q8_0 llama.cpp CPU (5.14), SmolLM2-360M Q8 MNN CPU (12.7), SmolLM2-360M F16 llama.cpp CPU (0.54), SmolLM2-360M F16 MNN CPU (15.9), Qwen3-0.6B Q4_0 llama.cpp CPU (3.31), Qwen3-0.6B Q4 MNN CPU (11.4), Qwen3-0.6B Q8_0 llama.cpp CPU (3.18), Qwen3-0.6B Q8 MNN CPU (9.59), Qwen3-0.6B F16 llama.cpp CPU (0.38), Qwen3.5-0.8B Q4_0 llama.cpp CPU (4.59), Qwen3.5-0.8B Q4 MNN CPU (9.1), Qwen3.5-0.8B Q8_0 llama.cpp CPU (4.31), Qwen3.5-0.8B Q8 MNN CPU (7.75), Qwen3.5-0.8B F16 llama.cpp CPU (0.33), Qwen3-1.7B Q4_0 llama.cpp CPU (1.78), Qwen3-1.7B Q4 MNN CPU (4.75), Qwen3-1.7B Q8 MNN CPU (3.76), Qwen3.5-2B Q4_0 llama.cpp CPU (2.2), Qwen3.5-2B Q4 MNN CPU (4.58), Qwen3.5-2B Q8_0 llama.cpp CPU (2.03)
- **Decode under 5 tokens/s (22):** SmolLM2-135M F16 llama.cpp CPU (1.09), Gemma3-270M Q4_0 llama.cpp CPU (4.65), Gemma3-270M Q8_0 llama.cpp CPU (4.56), Gemma3-270M F16 llama.cpp CPU (0.59), Gemma3-270M F16 MNN CPU (4.2), SmolLM2-360M Q4_0 llama.cpp CPU (2.95), SmolLM2-360M Q8_0 llama.cpp CPU (2.82), SmolLM2-360M F16 llama.cpp CPU (0.43), SmolLM2-360M F16 MNN CPU (4.1), Qwen3-0.6B Q4_0 llama.cpp CPU (1.68), Qwen3-0.6B Q8_0 llama.cpp CPU (1.67), Qwen3-0.6B F16 llama.cpp CPU (0.26), Qwen3.5-0.8B Q4_0 llama.cpp CPU (2.17), Qwen3.5-0.8B Q8_0 llama.cpp CPU (2.2), Qwen3.5-0.8B Q8 MNN CPU (4.65), Qwen3.5-0.8B F16 llama.cpp CPU (0.21), Qwen3-1.7B Q4_0 llama.cpp CPU (1.07), Qwen3-1.7B Q4 MNN CPU (3.35), Qwen3-1.7B Q8 MNN CPU (2.49), Qwen3.5-2B Q4_0 llama.cpp CPU (1.24), Qwen3.5-2B Q4 MNN CPU (3.25), Qwen3.5-2B Q8_0 llama.cpp CPU (1.2)
- **Energy over 139 J per request (15):** SmolLM2-135M F16 llama.cpp CPU (284 J), Gemma3-270M F16 llama.cpp CPU (427 J), SmolLM2-360M F16 llama.cpp CPU (747 J), Qwen3-0.6B Q4_0 llama.cpp CPU (180 J), Qwen3-0.6B Q8_0 llama.cpp CPU (209 J), Qwen3-0.6B F16 llama.cpp CPU (1,158 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (193 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (227 J), Qwen3.5-0.8B F16 llama.cpp CPU (1,737 J), Qwen3-1.7B Q4_0 llama.cpp CPU (341 J), Qwen3-1.7B Q4 MNN CPU (145 J), Qwen3-1.7B Q8 MNN CPU (226 J), Qwen3.5-2B Q4_0 llama.cpp CPU (383 J), Qwen3.5-2B Q4 MNN CPU (167 J), Qwen3.5-2B Q8_0 llama.cpp CPU (431 J)
- **Cannot run on this phone (18):** Qwen3-0.6B F16 MNN CPU (does not fit in RAM), Qwen3.5-0.8B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B Q8_0 llama.cpp CPU (crash/error (exit 1)), Qwen3-1.7B F16 llama.cpp CPU (does not fit in RAM), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B Q8 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp CPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q4 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q4 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM)

</details>

### H13

**Hard · Galaxy S23 · Live message translation**: translate each chat message as it arrives; 64-token prompt, 64-token answer, first token within 0.5 s, 200 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,024 MB · prefill ≥ 128 tokens/s · decode ≥ 10 tokens/s · energy ≤ 8.1 J per request.

**Pick: Qwen3.5-0.8B Q4, MNN, GPU** (712 MB, 454 / 46.1 tokens/s, 7.46 J). Kept 15 of 104 configurations (14%); 67 filtered out; 22 cannot run on this phone.

![H13](figures/scenarios/H13.png)

<details><summary>Kept pool (15), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q4 MNN GPU **(pick)** | 712 | 454 | 46.1 | 7.46 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 7.96 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 5.79 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 6.79 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 961 | 219 | 49.5 | 8.08 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 5.75 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 7.19 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 5.01 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 5.49 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 5.29 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 3.59 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 3.86 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 2.85 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 3.22 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 5.11 |

</details>

<details><summary>Filtered out (67) and cannot run (22)</summary>

- **Peak memory over 1,024 MB (49):** SmolLM2-135M F16 llama.cpp GPU (1,221 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,498 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,546 MB), Gemma3-270M F16 MNN CPU (1,487 MB), Gemma3-270M F16 llama.cpp GPU (2,486 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,324 MB), SmolLM2-360M F16 llama.cpp GPU (2,179 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,506 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,390 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,970 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,721 MB), Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,114 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,310 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,734 MB), Qwen3.5-0.8B Q8 MNN CPU (1,345 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,164 MB), Qwen3.5-0.8B Q8 MNN GPU (1,083 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,290 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q4 MNN GPU (1,443 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8 MNN CPU (2,397 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B Q8 MNN GPU (2,309 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,522 MB), Qwen3.5-2B Q4 MNN CPU (1,236 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,247 MB), Qwen3.5-2B Q4 MNN GPU (1,458 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,685 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4 MNN CPU (2,782 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 128 tokens/s (31):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp CPU (120), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp CPU (127), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp CPU (42.1), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3-1.7B Q4_0 llama.cpp CPU (119), Qwen3-1.7B Q4_0 llama.cpp GPU (107), Qwen3-1.7B Q8_0 llama.cpp CPU (106), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B Q4_0 llama.cpp GPU (109), Qwen3.5-2B Q8_0 llama.cpp CPU (112), Qwen3.5-2B Q8_0 llama.cpp GPU (113), Qwen3.5-2B F16 llama.cpp CPU (9.4), Qwen3-4B Q4_0 llama.cpp CPU (44.5), Qwen3-4B Q4 MNN CPU (53.5), Qwen3-4B Q4_0 llama.cpp GPU (40.4), Qwen3-4B Q4 MNN GPU (88.4), Qwen3.5-4B Q4_0 llama.cpp CPU (50.8), Qwen3.5-4B Q4 MNN CPU (49.9), Qwen3.5-4B Q4_0 llama.cpp GPU (47.5), Qwen3.5-4B Q4 MNN GPU (115)
- **Decode under 10 tokens/s (15):** SmolLM2-135M Q4 MNN GPU (9.13), SmolLM2-135M Q8 MNN GPU (9.09), Gemma3-270M Q4 MNN GPU (8.98), Gemma3-270M Q8 MNN GPU (8.89), SmolLM2-360M Q4 MNN GPU (9.09), SmolLM2-360M Q8 MNN GPU (8.56), Qwen3-0.6B Q4 MNN GPU (9.74), Qwen3-0.6B Q8 MNN GPU (8.45), Qwen3-1.7B Q4 MNN GPU (8.52), Qwen3-1.7B Q8 MNN GPU (7.99), Qwen3-1.7B F16 llama.cpp CPU (5.73), Qwen3.5-2B F16 llama.cpp CPU (5.25), Qwen3-4B Q4_0 llama.cpp GPU (8.69), Qwen3-4B Q4 MNN GPU (6.71), Qwen3.5-4B Q4_0 llama.cpp GPU (6.45)
- **Energy over 8.1 J per request (62):** SmolLM2-135M Q8_0 llama.cpp GPU (13.8 J), SmolLM2-135M F16 llama.cpp CPU (10.4 J), SmolLM2-135M F16 llama.cpp GPU (16.5 J), Gemma3-270M Q8_0 llama.cpp GPU (14.4 J), Gemma3-270M Q8 MNN GPU (9.26 J), Gemma3-270M F16 llama.cpp CPU (15.4 J), Gemma3-270M F16 MNN CPU (10.3 J), Gemma3-270M F16 llama.cpp GPU (17.7 J), SmolLM2-360M Q4 MNN GPU (9.55 J), SmolLM2-360M Q8_0 llama.cpp CPU (8.88 J), SmolLM2-360M Q8_0 llama.cpp GPU (33.4 J), SmolLM2-360M Q8 MNN GPU (11.2 J), SmolLM2-360M F16 llama.cpp CPU (26.6 J), SmolLM2-360M F16 MNN CPU (14 J), SmolLM2-360M F16 llama.cpp GPU (40 J), Qwen3-0.6B Q4_0 llama.cpp CPU (12.2 J), Qwen3-0.6B Q4 MNN CPU (9.14 J), Qwen3-0.6B Q4_0 llama.cpp GPU (13.8 J), Qwen3-0.6B Q4 MNN GPU (11.2 J), Qwen3-0.6B Q8_0 llama.cpp CPU (13.9 J), Qwen3-0.6B Q8 MNN CPU (12.7 J), Qwen3-0.6B Q8_0 llama.cpp GPU (46.8 J), Qwen3-0.6B Q8 MNN GPU (13.5 J), Qwen3-0.6B F16 llama.cpp CPU (38.5 J), Qwen3-0.6B F16 MNN CPU (23 J), Qwen3-0.6B F16 llama.cpp GPU (59.4 J), Qwen3.5-0.8B Q4 MNN CPU (9.32 J), Qwen3.5-0.8B Q4_0 llama.cpp GPU (17.4 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (16 J), Qwen3.5-0.8B Q8 MNN CPU (14 J), Qwen3.5-0.8B Q8_0 llama.cpp GPU (19.5 J), Qwen3.5-0.8B Q8 MNN GPU (12.3 J), Qwen3.5-0.8B F16 llama.cpp CPU (45.5 J), Qwen3.5-0.8B F16 MNN CPU (26.3 J), Qwen3.5-0.8B F16 llama.cpp GPU (27.1 J), Qwen3-1.7B Q4_0 llama.cpp CPU (24.2 J), Qwen3-1.7B Q4 MNN CPU (21.7 J), Qwen3-1.7B Q4_0 llama.cpp GPU (24.7 J), Qwen3-1.7B Q4 MNN GPU (20.9 J), Qwen3-1.7B Q8_0 llama.cpp CPU (30.7 J), Qwen3-1.7B Q8 MNN CPU (28.3 J), Qwen3-1.7B Q8_0 llama.cpp GPU (117 J), Qwen3-1.7B Q8 MNN GPU (29.6 J), Qwen3-1.7B F16 llama.cpp CPU (104 J), Qwen3-1.7B F16 llama.cpp GPU (178 J), Qwen3.5-2B Q4_0 llama.cpp CPU (26.8 J), Qwen3.5-2B Q4 MNN CPU (21.9 J), Qwen3.5-2B Q4_0 llama.cpp GPU (24.5 J), Qwen3.5-2B Q4 MNN GPU (15.5 J), Qwen3.5-2B Q8_0 llama.cpp CPU (32.6 J), Qwen3.5-2B Q8 MNN CPU (29.8 J), Qwen3.5-2B Q8_0 llama.cpp GPU (32.9 J), Qwen3.5-2B Q8 MNN GPU (26.4 J), Qwen3.5-2B F16 llama.cpp CPU (106 J), Qwen3-4B Q4_0 llama.cpp CPU (51.8 J), Qwen3-4B Q4 MNN CPU (45.6 J), Qwen3-4B Q4_0 llama.cpp GPU (48.7 J), Qwen3-4B Q4 MNN GPU (44.2 J), Qwen3.5-4B Q4_0 llama.cpp CPU (56.4 J), Qwen3.5-4B Q4 MNN CPU (44.3 J), Qwen3.5-4B Q4_0 llama.cpp GPU (52.9 J), Qwen3.5-4B Q4 MNN GPU (38.4 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### H14

**Hard · OnePlus 15 · Game NPC dialogue**: a character answers the player; 200-token prompt, 40-token answer, first token within 1 s, 100 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 768 MB · prefill ≥ 200 tokens/s · decode ≥ 12 tokens/s · energy ≤ 20.2 J per request.

**Pick: Qwen3.5-0.8B Q4, MNN, CPU** (568 MB, 337 / 80 tokens/s, 2.41 J). Kept 24 of 104 configurations (23%); 69 filtered out; 11 cannot run on this phone.

![H14](figures/scenarios/H14.png)

<details><summary>Kept pool (24), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q4 MNN CPU **(pick)** | 568 | 337 | 80 | 2.41 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 2.22 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 2.6 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 3.14 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 1.99 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 3.03 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 1.76 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 1.59 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 2.67 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 3.43 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 1.21 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 1.42 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 2.23 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 0.985 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 1.06 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 1.96 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 1.1 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 2.82 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 0.79 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 0.969 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 1.68 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 0.74 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 0.636 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 1.6 |

</details>

<details><summary>Filtered out (69) and cannot run (11)</summary>

- **Peak memory over 768 MB (69):** SmolLM2-135M Q4_0 llama.cpp GPU (791 MB), SmolLM2-135M Q8_0 llama.cpp GPU (846 MB), SmolLM2-135M F16 llama.cpp GPU (1,265 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,538 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,591 MB), Gemma3-270M F16 MNN CPU (1,528 MB), Gemma3-270M F16 llama.cpp GPU (2,534 MB), SmolLM2-360M Q4_0 llama.cpp GPU (1,009 MB), SmolLM2-360M Q8_0 llama.cpp CPU (824 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,326 MB), SmolLM2-360M F16 llama.cpp CPU (779 MB), SmolLM2-360M F16 MNN CPU (1,035 MB), SmolLM2-360M F16 llama.cpp GPU (2,225 MB), Qwen3-0.6B Q4_0 llama.cpp CPU (833 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,552 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,291 MB), Qwen3-0.6B Q8 MNN CPU (1,029 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (2,017 MB), Qwen3-0.6B Q8 MNN GPU (1,002 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (2,081 MB), Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,115 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,883 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B Q8 MNN CPU (940 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,273 MB), Qwen3.5-0.8B Q8 MNN GPU (1,108 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,286 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q4 MNN GPU (1,443 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B Q8 MNN GPU (2,479 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,512 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q4 MNN GPU (1,470 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8 MNN CPU (2,206 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 200 tokens/s (28):** SmolLM2-360M F16 llama.cpp CPU (95.5), Qwen3-0.6B F16 llama.cpp CPU (67.2), Qwen3-0.6B F16 MNN CPU (184), Qwen3.5-0.8B F16 llama.cpp CPU (61), Qwen3-1.7B Q4_0 llama.cpp CPU (144), Qwen3-1.7B Q4 MNN CPU (103), Qwen3-1.7B Q8_0 llama.cpp CPU (150), Qwen3-1.7B Q8 MNN CPU (136), Qwen3-1.7B Q8_0 llama.cpp GPU (164), Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3-1.7B F16 MNN CPU (79.4), Qwen3.5-2B Q4_0 llama.cpp CPU (150), Qwen3.5-2B Q4 MNN CPU (158), Qwen3.5-2B Q8_0 llama.cpp CPU (159), Qwen3.5-2B Q8 MNN CPU (169), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3.5-2B F16 MNN CPU (113), Qwen3-4B Q4_0 llama.cpp CPU (56.4), Qwen3-4B Q4 MNN CPU (41), Qwen3-4B Q4 MNN GPU (191), Qwen3-4B Q8_0 llama.cpp CPU (58.8), Qwen3-4B Q8_0 llama.cpp GPU (63.7), Qwen3-4B Q8 MNN GPU (180), Qwen3-4B F16 llama.cpp CPU (8.93), Qwen3-4B F16 llama.cpp GPU (88.2), Qwen3.5-4B Q4_0 llama.cpp CPU (59.8), Qwen3.5-4B Q4 MNN CPU (62.5), Qwen3.5-4B Q8 MNN CPU (67.6)
- **Decode under 12 tokens/s (2):** Qwen3-4B F16 llama.cpp CPU (6.87), Qwen3-4B F16 llama.cpp GPU (7.42)
- **Energy over 20.2 J per request (10):** Qwen3-1.7B F16 llama.cpp CPU (39.1 J), Qwen3.5-2B F16 llama.cpp CPU (35.4 J), Qwen3-4B Q8_0 llama.cpp CPU (22.9 J), Qwen3-4B Q8_0 llama.cpp GPU (26.2 J), Qwen3-4B Q8 MNN GPU (24.7 J), Qwen3-4B F16 llama.cpp CPU (92.3 J), Qwen3-4B F16 llama.cpp GPU (35.1 J), Qwen3.5-4B Q8 MNN CPU (26.9 J), Qwen3.5-4B Q8_0 llama.cpp GPU (21.1 J), Qwen3.5-4B Q8 MNN GPU (21.6 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### H15

**Hard · OnePlus 8 Pro · Notification triage**: classify incoming notifications in the background; 128-token prompt, 8-token answer, first token within 5 s, 300 requests ≤ 1% of battery a day.

Limits: peak memory ≤ 512 MB · prefill ≥ 25.6 tokens/s · decode ≥ 2 tokens/s · energy ≤ 2.09 J per request.

**Pick: Gemma3-270M Q8, MNN, CPU** (395 MB, 926 / 46.2 tokens/s, 1.82 J). Kept 11 of 104 configurations (11%); 68 filtered out; 25 cannot run on this phone.

![H15](figures/scenarios/H15.png)

<details><summary>Kept pool (11), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Gemma3-270M Q8 MNN CPU **(pick)** | 395 | 926 | 46.2 | 1.82 |
| Gemma3-270M Q8 MNN GPU | 430 | 736 | 8.35 | 1.51 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 1.46 |
| Gemma3-270M Q4 MNN GPU | 295 | 746 | 8.27 | 1.37 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 1.79 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 1.34 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 1.27 |
| SmolLM2-135M Q8 MNN GPU | 253 | 743 | 9.84 | 1.2 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 1.14 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 1.07 |
| SmolLM2-135M Q4 MNN GPU | 186 | 743 | 9.88 | 1.12 |

</details>

<details><summary>Filtered out (68) and cannot run (25)</summary>

- **Peak memory over 512 MB (63):** SmolLM2-135M Q4_0 llama.cpp GPU (681 MB), SmolLM2-135M Q8_0 llama.cpp GPU (734 MB), SmolLM2-135M F16 llama.cpp GPU (886 MB), Gemma3-270M Q4_0 llama.cpp CPU (534 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,429 MB), Gemma3-270M Q8_0 llama.cpp CPU (634 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,478 MB), Gemma3-270M F16 llama.cpp CPU (602 MB), Gemma3-270M F16 MNN CPU (1,435 MB), Gemma3-270M F16 llama.cpp GPU (1,886 MB), SmolLM2-360M Q4_0 llama.cpp GPU (901 MB), SmolLM2-360M Q8_0 llama.cpp CPU (825 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,216 MB), SmolLM2-360M Q8 MNN GPU (514 MB), SmolLM2-360M F16 llama.cpp CPU (779 MB), SmolLM2-360M F16 MNN CPU (803 MB), SmolLM2-360M F16 llama.cpp GPU (1,389 MB), Qwen3-0.6B Q4_0 llama.cpp CPU (833 MB), Qwen3-0.6B Q4 MNN CPU (537 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,315 MB), Qwen3-0.6B Q4 MNN GPU (546 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,440 MB), Qwen3-0.6B Q8 MNN CPU (962 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,908 MB), Qwen3-0.6B Q8 MNN GPU (844 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,811 MB), Qwen3-0.6B F16 llama.cpp GPU (2,344 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,114 MB), Qwen3.5-0.8B Q4 MNN CPU (550 MB), Qwen3.5-0.8B Q4 MNN GPU (583 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B Q8 MNN CPU (926 MB), Qwen3.5-0.8B Q8 MNN GPU (959 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,642 MB), Qwen3.5-0.8B F16 MNN CPU (2,906 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,270 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (2,753 MB), Qwen3-1.7B Q4 MNN GPU (1,211 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,678 MB), Qwen3-1.7B Q8 MNN CPU (2,416 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,474 MB), Qwen3-1.7B Q8 MNN GPU (2,072 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,218 MB), Qwen3.5-2B Q4 MNN GPU (1,259 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,974 MB), Qwen3.5-2B Q8 MNN GPU (2,221 MB), Qwen3.5-2B F16 llama.cpp CPU (3,908 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,560 MB), Qwen3-4B Q4 MNN CPU (2,755 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q4 MNN GPU (2,580 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3-4B Q8 MNN GPU (4,179 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q4 MNN CPU (2,875 MB), Qwen3.5-4B Q4 MNN GPU (2,655 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 25.6 tokens/s (11):** SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3-4B Q4_0 llama.cpp GPU (10.9)
- **Decode under 2 tokens/s (2):** Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q8 MNN GPU (0.71)
- **Energy over 2.09 J per request (61):** SmolLM2-135M Q4_0 llama.cpp GPU (3.02 J), SmolLM2-135M Q8_0 llama.cpp GPU (21.8 J), SmolLM2-135M F16 llama.cpp CPU (3.11 J), SmolLM2-135M F16 llama.cpp GPU (43.1 J), Gemma3-270M Q4_0 llama.cpp GPU (4.07 J), Gemma3-270M Q8_0 llama.cpp GPU (22.4 J), Gemma3-270M F16 llama.cpp CPU (3.56 J), Gemma3-270M F16 MNN CPU (2.62 J), Gemma3-270M F16 llama.cpp GPU (39.4 J), SmolLM2-360M Q4_0 llama.cpp CPU (2.68 J), SmolLM2-360M Q4 MNN CPU (2.71 J), SmolLM2-360M Q4_0 llama.cpp GPU (5.15 J), SmolLM2-360M Q4 MNN GPU (2.14 J), SmolLM2-360M Q8_0 llama.cpp CPU (3.33 J), SmolLM2-360M Q8 MNN CPU (3.2 J), SmolLM2-360M Q8_0 llama.cpp GPU (64.3 J), SmolLM2-360M Q8 MNN GPU (2.45 J), SmolLM2-360M F16 llama.cpp CPU (8.28 J), SmolLM2-360M F16 MNN CPU (4.75 J), Qwen3-0.6B Q4_0 llama.cpp CPU (4.03 J), Qwen3-0.6B Q4 MNN CPU (4.01 J), Qwen3-0.6B Q4_0 llama.cpp GPU (9.38 J), Qwen3-0.6B Q4 MNN GPU (2.82 J), Qwen3-0.6B Q8_0 llama.cpp CPU (4.85 J), Qwen3-0.6B Q8 MNN CPU (4.89 J), Qwen3-0.6B Q8 MNN GPU (3.49 J), Qwen3-0.6B F16 llama.cpp CPU (12.4 J), Qwen3-0.6B F16 MNN CPU (7.29 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (5.3 J), Qwen3.5-0.8B Q4 MNN CPU (4.67 J), Qwen3.5-0.8B Q4 MNN GPU (2.21 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (6.63 J), Qwen3.5-0.8B Q8 MNN CPU (5.68 J), Qwen3.5-0.8B Q8 MNN GPU (3.35 J), Qwen3.5-0.8B F16 llama.cpp CPU (14.8 J), Qwen3.5-0.8B F16 MNN CPU (8.38 J), Qwen3-1.7B Q4_0 llama.cpp CPU (9.91 J), Qwen3-1.7B Q4 MNN CPU (11.7 J), Qwen3-1.7B Q4_0 llama.cpp GPU (13.7 J), Qwen3-1.7B Q4 MNN GPU (7.27 J), Qwen3-1.7B Q8_0 llama.cpp CPU (14.2 J), Qwen3-1.7B Q8 MNN CPU (14.3 J), Qwen3-1.7B Q8 MNN GPU (9.71 J), Qwen3-1.7B F16 llama.cpp CPU (37.4 J), Qwen3.5-2B Q4_0 llama.cpp CPU (10.7 J), Qwen3.5-2B Q4 MNN CPU (10.5 J), Qwen3.5-2B Q4 MNN GPU (4.99 J), Qwen3.5-2B Q8_0 llama.cpp CPU (14.9 J), Qwen3.5-2B Q8 MNN CPU (13 J), Qwen3.5-2B Q8 MNN GPU (7.85 J), Qwen3.5-2B F16 llama.cpp CPU (37.8 J), Qwen3-4B Q4_0 llama.cpp CPU (24.2 J), Qwen3-4B Q4 MNN CPU (29.2 J), Qwen3-4B Q4_0 llama.cpp GPU (35.3 J), Qwen3-4B Q4 MNN GPU (18.3 J), Qwen3-4B Q8 MNN CPU (35.2 J), Qwen3-4B Q8 MNN GPU (30.4 J), Qwen3.5-4B Q4_0 llama.cpp CPU (25.7 J), Qwen3.5-4B Q4 MNN CPU (24.8 J), Qwen3.5-4B Q4 MNN GPU (12.8 J), Qwen3.5-4B Q8 MNN GPU (19.4 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### H16

**Hard · Redmi 9A · Offline Q&A (travel)**: an assistant with no network; 200-token prompt, 200-token answer, first token within 10 s, 20 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,500 MB · prefill ≥ 20 tokens/s · decode ≥ 4 tokens/s · energy ≤ 104 J per request.

**Pick: Gemma3-270M Q8, MNN, CPU** (745 MB, 35.2 / 6.71 tokens/s, 37.3 J). Kept 5 of 52 configurations (10%); 29 filtered out; 18 cannot run on this phone.

![H16](figures/scenarios/H16.png)

<details><summary>Kept pool (5), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Gemma3-270M Q8 MNN CPU **(pick)** | 745 | 35.2 | 6.71 | 37.3 |
| Gemma3-270M Q4 MNN CPU | 326 | 39.9 | 7.72 | 33.7 |
| SmolLM2-135M F16 MNN CPU | 637 | 43 | 8.17 | 34.8 |
| SmolLM2-135M Q8 MNN CPU | 225 | 32.5 | 13.2 | 21.5 |
| SmolLM2-135M Q4 MNN CPU | 158 | 36.5 | 16.3 | 17.8 |

</details>

<details><summary>Filtered out (29) and cannot run (18)</summary>

- **Peak memory over 1,500 MB (8):** Gemma3-270M F16 MNN CPU (1,681 MB), SmolLM2-360M F16 MNN CPU (1,614 MB), Qwen3-0.6B F16 llama.cpp CPU (1,612 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,615 MB), Qwen3-1.7B Q4 MNN CPU (1,543 MB), Qwen3-1.7B Q8 MNN CPU (2,357 MB), Qwen3.5-2B Q4 MNN CPU (1,663 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (2,116 MB)
- **Prefill under 20 tokens/s (28):** SmolLM2-135M Q4_0 llama.cpp CPU (11.6), SmolLM2-135M Q8_0 llama.cpp CPU (11.2), SmolLM2-135M F16 llama.cpp CPU (1.54), Gemma3-270M Q4_0 llama.cpp CPU (11.7), Gemma3-270M Q8_0 llama.cpp CPU (11.3), Gemma3-270M F16 llama.cpp CPU (1.64), SmolLM2-360M Q4_0 llama.cpp CPU (5.39), SmolLM2-360M Q4 MNN CPU (14.8), SmolLM2-360M Q8_0 llama.cpp CPU (5.14), SmolLM2-360M Q8 MNN CPU (12.7), SmolLM2-360M F16 llama.cpp CPU (0.54), SmolLM2-360M F16 MNN CPU (15.9), Qwen3-0.6B Q4_0 llama.cpp CPU (3.31), Qwen3-0.6B Q4 MNN CPU (11.4), Qwen3-0.6B Q8_0 llama.cpp CPU (3.18), Qwen3-0.6B Q8 MNN CPU (9.59), Qwen3-0.6B F16 llama.cpp CPU (0.38), Qwen3.5-0.8B Q4_0 llama.cpp CPU (4.59), Qwen3.5-0.8B Q4 MNN CPU (9.1), Qwen3.5-0.8B Q8_0 llama.cpp CPU (4.31), Qwen3.5-0.8B Q8 MNN CPU (7.75), Qwen3.5-0.8B F16 llama.cpp CPU (0.33), Qwen3-1.7B Q4_0 llama.cpp CPU (1.78), Qwen3-1.7B Q4 MNN CPU (4.75), Qwen3-1.7B Q8 MNN CPU (3.76), Qwen3.5-2B Q4_0 llama.cpp CPU (2.2), Qwen3.5-2B Q4 MNN CPU (4.58), Qwen3.5-2B Q8_0 llama.cpp CPU (2.03)
- **Decode under 4 tokens/s (17):** SmolLM2-135M F16 llama.cpp CPU (1.09), Gemma3-270M F16 llama.cpp CPU (0.59), SmolLM2-360M Q4_0 llama.cpp CPU (2.95), SmolLM2-360M Q8_0 llama.cpp CPU (2.82), SmolLM2-360M F16 llama.cpp CPU (0.43), Qwen3-0.6B Q4_0 llama.cpp CPU (1.68), Qwen3-0.6B Q8_0 llama.cpp CPU (1.67), Qwen3-0.6B F16 llama.cpp CPU (0.26), Qwen3.5-0.8B Q4_0 llama.cpp CPU (2.17), Qwen3.5-0.8B Q8_0 llama.cpp CPU (2.2), Qwen3.5-0.8B F16 llama.cpp CPU (0.21), Qwen3-1.7B Q4_0 llama.cpp CPU (1.07), Qwen3-1.7B Q4 MNN CPU (3.35), Qwen3-1.7B Q8 MNN CPU (2.49), Qwen3.5-2B Q4_0 llama.cpp CPU (1.24), Qwen3.5-2B Q4 MNN CPU (3.25), Qwen3.5-2B Q8_0 llama.cpp CPU (1.2)
- **Energy over 104 J per request (16):** SmolLM2-135M F16 llama.cpp CPU (247 J), Gemma3-270M F16 llama.cpp CPU (360 J), SmolLM2-360M Q8_0 llama.cpp CPU (106 J), SmolLM2-360M F16 llama.cpp CPU (654 J), Qwen3-0.6B Q4_0 llama.cpp CPU (154 J), Qwen3-0.6B Q8_0 llama.cpp CPU (178 J), Qwen3-0.6B F16 llama.cpp CPU (1,005 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (165 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (194 J), Qwen3.5-0.8B F16 llama.cpp CPU (1,501 J), Qwen3-1.7B Q4_0 llama.cpp CPU (293 J), Qwen3-1.7B Q4 MNN CPU (124 J), Qwen3-1.7B Q8 MNN CPU (194 J), Qwen3.5-2B Q4_0 llama.cpp CPU (328 J), Qwen3.5-2B Q4 MNN CPU (143 J), Qwen3.5-2B Q8_0 llama.cpp CPU (369 J)
- **Cannot run on this phone (18):** Qwen3-0.6B F16 MNN CPU (does not fit in RAM), Qwen3.5-0.8B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B Q8_0 llama.cpp CPU (crash/error (exit 1)), Qwen3-1.7B F16 llama.cpp CPU (does not fit in RAM), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B Q8 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp CPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q4 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q4 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM)

</details>

### H17

**Hard · Galaxy S23 · Screen-reader descriptions**: describe a screen's content aloud; 400-token prompt, 60-token answer, first token within 2 s, 100 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,024 MB · prefill ≥ 200 tokens/s · decode ≥ 5 tokens/s · energy ≤ 16.2 J per request.

**Pick: Qwen3.5-0.8B Q4, MNN, GPU** (712 MB, 454 / 46.1 tokens/s, 9.54 J). Kept 25 of 104 configurations (24%); 57 filtered out; 22 cannot run on this phone.

![H17](figures/scenarios/H17.png)

<details><summary>Kept pool (25), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q4 MNN GPU **(pick)** | 712 | 454 | 46.1 | 9.54 |
| Qwen3-0.6B Q8 MNN GPU | 1,005 | 540 | 8.45 | 15.5 |
| Qwen3-0.6B Q4 MNN CPU | 548 | 371 | 72.7 | 14.2 |
| Qwen3-0.6B Q4 MNN GPU | 696 | 559 | 9.74 | 13.2 |
| SmolLM2-360M Q8 MNN CPU | 499 | 553 | 79.9 | 11.5 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 824 | 348 | 75.2 | 14.9 |
| SmolLM2-360M Q8 MNN GPU | 614 | 735 | 8.56 | 12.3 |
| SmolLM2-360M Q4 MNN CPU | 323 | 554 | 115 | 9.41 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 508 | 366 | 95.2 | 12.4 |
| SmolLM2-360M Q4_0 llama.cpp GPU | 961 | 219 | 49.5 | 13.4 |
| SmolLM2-360M Q4 MNN GPU | 433 | 763 | 9.09 | 10.7 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 790 | 118 | 8.47 |
| Gemma3-270M Q8 MNN CPU | 567 | 1,465 | 82.2 | 8.36 |
| Gemma3-270M Q8 MNN GPU | 515 | 1,589 | 8.89 | 9.36 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 799 | 133 | 7.5 |
| Gemma3-270M Q4 MNN CPU | 273 | 1,511 | 108 | 6.7 |
| Gemma3-270M Q4 MNN GPU | 379 | 1,622 | 8.98 | 7.84 |
| SmolLM2-135M F16 MNN CPU | 340 | 976 | 112 | 7.08 |
| SmolLM2-135M Q8 MNN CPU | 230 | 1,509 | 175 | 5.05 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 786 | 169 | 6.4 |
| SmolLM2-135M Q8 MNN GPU | 329 | 1,753 | 9.09 | 7.81 |
| SmolLM2-135M Q4 MNN CPU | 162 | 1,518 | 224 | 4.28 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 793 | 204 | 5.71 |
| SmolLM2-135M Q4_0 llama.cpp GPU | 747 | 410 | 39.6 | 7.64 |
| SmolLM2-135M Q4 MNN GPU | 259 | 1,725 | 9.13 | 6.93 |

</details>

<details><summary>Filtered out (57) and cannot run (22)</summary>

- **Peak memory over 1,024 MB (49):** SmolLM2-135M F16 llama.cpp GPU (1,221 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,498 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,546 MB), Gemma3-270M F16 MNN CPU (1,487 MB), Gemma3-270M F16 llama.cpp GPU (2,486 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,324 MB), SmolLM2-360M F16 llama.cpp GPU (2,179 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,506 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,390 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,970 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,721 MB), Qwen3-0.6B F16 llama.cpp GPU (3,602 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,114 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,310 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,734 MB), Qwen3.5-0.8B Q8 MNN CPU (1,345 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,164 MB), Qwen3.5-0.8B Q8 MNN GPU (1,083 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,135 MB), Qwen3.5-0.8B F16 llama.cpp GPU (5,376 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,290 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,069 MB), Qwen3-1.7B Q4 MNN GPU (1,443 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,787 MB), Qwen3-1.7B Q8 MNN CPU (2,397 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,429 MB), Qwen3-1.7B Q8 MNN GPU (2,309 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (7,795 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,522 MB), Qwen3.5-2B Q4 MNN CPU (1,236 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,247 MB), Qwen3.5-2B Q4 MNN GPU (1,458 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,685 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (3,650 MB), Qwen3.5-2B Q8 MNN GPU (3,315 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,513 MB), Qwen3-4B Q4 MNN CPU (2,782 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,400 MB), Qwen3-4B Q4 MNN GPU (2,966 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (4,893 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (3,775 MB), Qwen3.5-4B Q4 MNN GPU (3,033 MB)
- **Prefill under 200 tokens/s (40):** SmolLM2-135M Q8_0 llama.cpp GPU (25.8), SmolLM2-135M F16 llama.cpp CPU (120), SmolLM2-135M F16 llama.cpp GPU (23.3), Gemma3-270M Q8_0 llama.cpp GPU (26.8), Gemma3-270M F16 llama.cpp CPU (127), Gemma3-270M F16 llama.cpp GPU (24.4), SmolLM2-360M Q8_0 llama.cpp GPU (8.9), SmolLM2-360M F16 llama.cpp CPU (42.1), SmolLM2-360M F16 llama.cpp GPU (7.69), Qwen3-0.6B Q4_0 llama.cpp GPU (133), Qwen3-0.6B Q8_0 llama.cpp GPU (6.29), Qwen3-0.6B F16 llama.cpp CPU (29.6), Qwen3-0.6B F16 llama.cpp GPU (5.35), Qwen3.5-0.8B Q4_0 llama.cpp GPU (157), Qwen3.5-0.8B Q8_0 llama.cpp GPU (163), Qwen3.5-0.8B F16 llama.cpp CPU (26.4), Qwen3.5-0.8B F16 llama.cpp GPU (159), Qwen3-1.7B Q4_0 llama.cpp CPU (119), Qwen3-1.7B Q4 MNN CPU (138), Qwen3-1.7B Q4_0 llama.cpp GPU (107), Qwen3-1.7B Q8_0 llama.cpp CPU (106), Qwen3-1.7B Q8 MNN CPU (132), Qwen3-1.7B Q8_0 llama.cpp GPU (2.05), Qwen3-1.7B F16 llama.cpp CPU (9.25), Qwen3-1.7B F16 llama.cpp GPU (1.69), Qwen3.5-2B Q4_0 llama.cpp CPU (132), Qwen3.5-2B Q4 MNN CPU (134), Qwen3.5-2B Q4_0 llama.cpp GPU (109), Qwen3.5-2B Q8_0 llama.cpp CPU (112), Qwen3.5-2B Q8 MNN CPU (128), Qwen3.5-2B Q8_0 llama.cpp GPU (113), Qwen3.5-2B F16 llama.cpp CPU (9.4), Qwen3-4B Q4_0 llama.cpp CPU (44.5), Qwen3-4B Q4 MNN CPU (53.5), Qwen3-4B Q4_0 llama.cpp GPU (40.4), Qwen3-4B Q4 MNN GPU (88.4), Qwen3.5-4B Q4_0 llama.cpp CPU (50.8), Qwen3.5-4B Q4 MNN CPU (49.9), Qwen3.5-4B Q4_0 llama.cpp GPU (47.5), Qwen3.5-4B Q4 MNN GPU (115)
- **Energy over 16.2 J per request (53):** SmolLM2-135M Q8_0 llama.cpp GPU (56.4 J), SmolLM2-135M F16 llama.cpp CPU (25.8 J), SmolLM2-135M F16 llama.cpp GPU (66.9 J), Gemma3-270M Q8_0 llama.cpp GPU (54.2 J), Gemma3-270M F16 llama.cpp CPU (29.7 J), Gemma3-270M F16 llama.cpp GPU (64.1 J), SmolLM2-360M Q8_0 llama.cpp GPU (159 J), SmolLM2-360M F16 llama.cpp CPU (69.3 J), SmolLM2-360M F16 MNN CPU (18.3 J), SmolLM2-360M F16 llama.cpp GPU (188 J), Qwen3-0.6B Q4_0 llama.cpp CPU (20.8 J), Qwen3-0.6B Q4_0 llama.cpp GPU (22.2 J), Qwen3-0.6B Q8_0 llama.cpp CPU (22.9 J), Qwen3-0.6B Q8 MNN CPU (18 J), Qwen3-0.6B Q8_0 llama.cpp GPU (228 J), Qwen3-0.6B F16 llama.cpp CPU (95.9 J), Qwen3-0.6B F16 MNN CPU (29.7 J), Qwen3-0.6B F16 llama.cpp GPU (281 J), Qwen3.5-0.8B Q4 MNN CPU (16.3 J), Qwen3.5-0.8B Q4_0 llama.cpp GPU (23.9 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (23.9 J), Qwen3.5-0.8B Q8 MNN CPU (21 J), Qwen3.5-0.8B Q8_0 llama.cpp GPU (25.7 J), Qwen3.5-0.8B F16 llama.cpp CPU (105 J), Qwen3.5-0.8B F16 MNN CPU (34.1 J), Qwen3.5-0.8B F16 llama.cpp GPU (34.6 J), Qwen3-1.7B Q4_0 llama.cpp CPU (40.6 J), Qwen3-1.7B Q4 MNN CPU (35.2 J), Qwen3-1.7B Q4_0 llama.cpp GPU (36.4 J), Qwen3-1.7B Q4 MNN GPU (28 J), Qwen3-1.7B Q8_0 llama.cpp CPU (50.1 J), Qwen3-1.7B Q8 MNN CPU (42.5 J), Qwen3-1.7B Q8_0 llama.cpp GPU (607 J), Qwen3-1.7B Q8 MNN GPU (37.2 J), Qwen3-1.7B F16 llama.cpp CPU (284 J), Qwen3-1.7B F16 llama.cpp GPU (908 J), Qwen3.5-2B Q4_0 llama.cpp CPU (41.9 J), Qwen3.5-2B Q4 MNN CPU (36.6 J), Qwen3.5-2B Q4_0 llama.cpp GPU (32.7 J), Qwen3.5-2B Q4 MNN GPU (19.7 J), Qwen3.5-2B Q8_0 llama.cpp CPU (51.3 J), Qwen3.5-2B Q8 MNN CPU (44.8 J), Qwen3.5-2B Q8_0 llama.cpp GPU (42.7 J), Qwen3.5-2B Q8 MNN GPU (32.4 J), Qwen3.5-2B F16 llama.cpp CPU (267 J), Qwen3-4B Q4_0 llama.cpp CPU (93.7 J), Qwen3-4B Q4 MNN CPU (80.8 J), Qwen3-4B Q4_0 llama.cpp GPU (82.6 J), Qwen3-4B Q4 MNN GPU (65 J), Qwen3.5-4B Q4_0 llama.cpp CPU (94.9 J), Qwen3.5-4B Q4 MNN CPU (80.6 J), Qwen3.5-4B Q4_0 llama.cpp GPU (78.6 J), Qwen3.5-4B Q4 MNN GPU (49.7 J)
- **No energy reading (1):** Qwen3.5-0.8B Q4_0 llama.cpp CPU
- **Cannot run on this phone (22):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (crash/error (exit 1)), Qwen3.5-2B F16 MNN GPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3-4B Q8 MNN GPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (does not fit in RAM), Qwen3.5-4B Q8 MNN GPU (does not fit in RAM)

</details>

### H18

**Hard · OnePlus 15 · SMS auto-reply**: draft a reply to a text message; 128-token prompt, 32-token answer, first token within 5 s, 50 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 768 MB · prefill ≥ 25.6 tokens/s · decode ≥ 3 tokens/s · energy ≤ 40.5 J per request.

**Pick: Qwen3.5-0.8B Q4, MNN, CPU** (568 MB, 337 / 80 tokens/s, 1.77 J). Kept 24 of 104 configurations (23%); 69 filtered out; 11 cannot run on this phone.

![H18](figures/scenarios/H18.png)

<details><summary>Kept pool (24), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3.5-0.8B Q4 MNN CPU **(pick)** | 568 | 337 | 80 | 1.77 |
| Qwen3.5-0.8B Q4 MNN GPU | 733 | 1,292 | 75.9 | 1.68 |
| Qwen3-0.6B Q4 MNN CPU | 589 | 398 | 79 | 1.9 |
| Qwen3-0.6B Q4 MNN GPU | 707 | 1,252 | 32 | 2.41 |
| SmolLM2-360M Q8 MNN CPU | 509 | 524 | 99.6 | 1.47 |
| SmolLM2-360M Q8 MNN GPU | 624 | 1,562 | 37 | 2.35 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 456 | 125 | 1.28 |
| SmolLM2-360M Q4 MNN CPU | 340 | 488 | 115 | 1.16 |
| SmolLM2-360M Q4 MNN GPU | 444 | 1,653 | 39.3 | 2.05 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 282 | 81.8 | 2.48 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,105 | 169 | 0.908 |
| Gemma3-270M Q8 MNN CPU | 694 | 1,329 | 101 | 1.08 |
| Gemma3-270M Q8 MNN GPU | 524 | 3,614 | 23.8 | 1.76 |
| Gemma3-270M Q4_0 llama.cpp CPU | 535 | 1,090 | 186 | 0.731 |
| Gemma3-270M Q4 MNN CPU | 285 | 1,278 | 116 | 0.806 |
| Gemma3-270M Q4 MNN GPU | 391 | 3,889 | 27.4 | 1.54 |
| SmolLM2-135M F16 MNN CPU | 352 | 845 | 127 | 0.814 |
| SmolLM2-135M F16 llama.cpp CPU | 309 | 260 | 119 | 1.97 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 1,045 | 226 | 0.588 |
| SmolLM2-135M Q8 MNN CPU | 238 | 1,087 | 185 | 0.724 |
| SmolLM2-135M Q8 MNN GPU | 342 | 3,626 | 40 | 1.32 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 1,032 | 261 | 0.55 |
| SmolLM2-135M Q4 MNN CPU | 170 | 1,027 | 206 | 0.47 |
| SmolLM2-135M Q4 MNN GPU | 274 | 3,666 | 42.2 | 1.25 |

</details>

<details><summary>Filtered out (69) and cannot run (11)</summary>

- **Peak memory over 768 MB (69):** SmolLM2-135M Q4_0 llama.cpp GPU (791 MB), SmolLM2-135M Q8_0 llama.cpp GPU (846 MB), SmolLM2-135M F16 llama.cpp GPU (1,265 MB), Gemma3-270M Q4_0 llama.cpp GPU (1,538 MB), Gemma3-270M Q8_0 llama.cpp GPU (1,591 MB), Gemma3-270M F16 MNN CPU (1,528 MB), Gemma3-270M F16 llama.cpp GPU (2,534 MB), SmolLM2-360M Q4_0 llama.cpp GPU (1,009 MB), SmolLM2-360M Q8_0 llama.cpp CPU (824 MB), SmolLM2-360M Q8_0 llama.cpp GPU (1,326 MB), SmolLM2-360M F16 llama.cpp CPU (779 MB), SmolLM2-360M F16 MNN CPU (1,035 MB), SmolLM2-360M F16 llama.cpp GPU (2,225 MB), Qwen3-0.6B Q4_0 llama.cpp CPU (833 MB), Qwen3-0.6B Q4_0 llama.cpp GPU (1,552 MB), Qwen3-0.6B Q8_0 llama.cpp CPU (1,291 MB), Qwen3-0.6B Q8 MNN CPU (1,029 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (2,017 MB), Qwen3-0.6B Q8 MNN GPU (1,002 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (2,081 MB), Qwen3-0.6B F16 llama.cpp GPU (4,464 MB), Qwen3.5-0.8B Q4_0 llama.cpp CPU (1,115 MB), Qwen3.5-0.8B Q4_0 llama.cpp GPU (1,883 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B Q8 MNN CPU (940 MB), Qwen3.5-0.8B Q8_0 llama.cpp GPU (2,273 MB), Qwen3.5-0.8B Q8 MNN GPU (1,108 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,641 MB), Qwen3.5-0.8B F16 MNN CPU (3,068 MB), Qwen3.5-0.8B F16 llama.cpp GPU (4,685 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4 MNN CPU (1,286 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (3,113 MB), Qwen3-1.7B Q4 MNN GPU (1,443 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,520 MB), Qwen3-1.7B Q8 MNN CPU (2,716 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (5,169 MB), Qwen3-1.7B Q8 MNN GPU (2,479 MB), Qwen3-1.7B F16 llama.cpp CPU (4,115 MB), Qwen3-1.7B F16 MNN CPU (5,220 MB), Qwen3-1.7B F16 llama.cpp GPU (11,362 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q4 MNN CPU (1,512 MB), Qwen3.5-2B Q4_0 llama.cpp GPU (2,980 MB), Qwen3.5-2B Q4 MNN GPU (1,470 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,142 MB), Qwen3.5-2B Q8 MNN CPU (2,206 MB), Qwen3.5-2B Q8_0 llama.cpp GPU (4,157 MB), Qwen3.5-2B Q8 MNN GPU (2,810 MB), Qwen3.5-2B F16 llama.cpp CPU (3,907 MB), Qwen3.5-2B F16 MNN CPU (6,475 MB), Qwen3.5-2B F16 llama.cpp GPU (10,039 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,561 MB), Qwen3-4B Q4 MNN CPU (2,772 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,647 MB), Qwen3-4B Q4 MNN GPU (2,964 MB), Qwen3-4B Q8_0 llama.cpp CPU (8,715 MB), Qwen3-4B Q8_0 llama.cpp GPU (9,544 MB), Qwen3-4B Q8 MNN GPU (4,975 MB), Qwen3-4B F16 llama.cpp CPU (8,212 MB), Qwen3-4B F16 llama.cpp GPU (18,196 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,263 MB), Qwen3.5-4B Q4 MNN CPU (2,609 MB), Qwen3.5-4B Q4_0 llama.cpp GPU (4,633 MB), Qwen3.5-4B Q4 MNN GPU (3,040 MB), Qwen3.5-4B Q8 MNN CPU (5,355 MB), Qwen3.5-4B Q8_0 llama.cpp GPU (6,406 MB), Qwen3.5-4B Q8 MNN GPU (5,586 MB)
- **Prefill under 25.6 tokens/s (3):** Qwen3-1.7B F16 llama.cpp CPU (15.9), Qwen3.5-2B F16 llama.cpp CPU (23.5), Qwen3-4B F16 llama.cpp CPU (8.93)
- **Energy over 40.5 J per request (1):** Qwen3-4B F16 llama.cpp CPU (64.1 J)
- **Cannot run on this phone (11):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM)

</details>

### H19

**Hard · OnePlus 8 Pro · Voice assistant**: spoken answer to a spoken question; 256-token prompt, 64-token answer, first token within 1 s, 50 requests ≤ 2% of battery a day.

Limits: peak memory ≤ 1,536 MB · prefill ≥ 256 tokens/s · decode ≥ 5 tokens/s · energy ≤ 25.1 J per request.

**Pick: Qwen3-0.6B Q8_0, llama.cpp, CPU** (1,440 MB, 259 / 30.3 tokens/s, 19.9 J). Kept 25 of 104 configurations (24%); 54 filtered out; 25 cannot run on this phone.

![H19](figures/scenarios/H19.png)

<details><summary>Kept pool (25), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Qwen3-0.6B Q8_0 llama.cpp CPU **(pick)** | 1,440 | 259 | 30.3 | 19.9 |
| Qwen3-0.6B Q4_0 llama.cpp CPU | 833 | 261 | 48.2 | 15.6 |
| SmolLM2-360M F16 MNN CPU | 803 | 280 | 25.9 | 21 |
| SmolLM2-360M Q8_0 llama.cpp CPU | 825 | 377 | 51.2 | 13 |
| SmolLM2-360M Q8 MNN CPU | 492 | 342 | 44.6 | 13.3 |
| SmolLM2-360M Q8 MNN GPU | 514 | 298 | 9.25 | 12.8 |
| SmolLM2-360M Q4_0 llama.cpp CPU | 509 | 379 | 71.7 | 9.9 |
| SmolLM2-360M Q4 MNN CPU | 312 | 326 | 67.6 | 9.92 |
| SmolLM2-360M Q4 MNN GPU | 333 | 299 | 9.3 | 10.7 |
| Gemma3-270M F16 llama.cpp CPU | 602 | 484 | 42.8 | 14.9 |
| Gemma3-270M F16 MNN CPU | 1,435 | 755 | 31.1 | 14.7 |
| Gemma3-270M Q8_0 llama.cpp CPU | 634 | 1,013 | 70.4 | 8.14 |
| Gemma3-270M Q8 MNN CPU | 395 | 926 | 46.2 | 9.86 |
| Gemma3-270M Q8 MNN GPU | 430 | 736 | 8.35 | 9.7 |
| Gemma3-270M Q4_0 llama.cpp CPU | 534 | 1,015 | 84.3 | 6.69 |
| Gemma3-270M Q4 MNN CPU | 261 | 898 | 65.3 | 7.3 |
| Gemma3-270M Q4 MNN GPU | 295 | 746 | 8.27 | 8.53 |
| SmolLM2-135M F16 llama.cpp CPU | 310 | 448 | 75.5 | 10.7 |
| SmolLM2-135M F16 MNN CPU | 458 | 722 | 66 | 8.34 |
| SmolLM2-135M Q8_0 llama.cpp CPU | 326 | 917 | 116 | 5.55 |
| SmolLM2-135M Q8 MNN CPU | 217 | 867 | 95 | 5.55 |
| SmolLM2-135M Q8 MNN GPU | 253 | 743 | 9.84 | 7.21 |
| SmolLM2-135M Q4_0 llama.cpp CPU | 220 | 919 | 150 | 4.39 |
| SmolLM2-135M Q4 MNN CPU | 152 | 836 | 138 | 4.17 |
| SmolLM2-135M Q4 MNN GPU | 186 | 743 | 9.88 | 6.66 |

</details>

<details><summary>Filtered out (54) and cannot run (25)</summary>

- **Peak memory over 1,536 MB (31):** Gemma3-270M F16 llama.cpp GPU (1,886 MB), Qwen3-0.6B Q8_0 llama.cpp GPU (1,908 MB), Qwen3-0.6B F16 llama.cpp CPU (1,626 MB), Qwen3-0.6B F16 MNN CPU (1,811 MB), Qwen3-0.6B F16 llama.cpp GPU (2,344 MB), Qwen3.5-0.8B Q8_0 llama.cpp CPU (1,735 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,642 MB), Qwen3.5-0.8B F16 MNN CPU (2,906 MB), Qwen3-1.7B Q4_0 llama.cpp CPU (2,140 MB), Qwen3-1.7B Q4_0 llama.cpp GPU (2,753 MB), Qwen3-1.7B Q8_0 llama.cpp CPU (3,678 MB), Qwen3-1.7B Q8 MNN CPU (2,416 MB), Qwen3-1.7B Q8_0 llama.cpp GPU (4,474 MB), Qwen3-1.7B Q8 MNN GPU (2,072 MB), Qwen3-1.7B F16 llama.cpp CPU (4,196 MB), Qwen3-1.7B F16 llama.cpp GPU (4,908 MB), Qwen3.5-2B Q4_0 llama.cpp CPU (2,523 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (4,141 MB), Qwen3.5-2B Q8 MNN CPU (2,974 MB), Qwen3.5-2B Q8 MNN GPU (2,221 MB), Qwen3.5-2B F16 llama.cpp CPU (3,908 MB), Qwen3-4B Q4_0 llama.cpp CPU (4,560 MB), Qwen3-4B Q4 MNN CPU (2,755 MB), Qwen3-4B Q4_0 llama.cpp GPU (5,216 MB), Qwen3-4B Q4 MNN GPU (2,580 MB), Qwen3-4B Q8 MNN CPU (5,065 MB), Qwen3-4B Q8 MNN GPU (4,179 MB), Qwen3.5-4B Q4_0 llama.cpp CPU (5,264 MB), Qwen3.5-4B Q4 MNN CPU (2,875 MB), Qwen3.5-4B Q4 MNN GPU (2,655 MB), Qwen3.5-4B Q8 MNN GPU (5,968 MB)
- **Prefill under 256 tokens/s (54):** SmolLM2-135M Q4_0 llama.cpp GPU (105), SmolLM2-135M Q8_0 llama.cpp GPU (11.4), SmolLM2-135M F16 llama.cpp GPU (5.44), Gemma3-270M Q4_0 llama.cpp GPU (77.8), Gemma3-270M Q8_0 llama.cpp GPU (11), Gemma3-270M F16 llama.cpp GPU (6.2), SmolLM2-360M Q4_0 llama.cpp GPU (58), SmolLM2-360M Q8_0 llama.cpp GPU (3.76), SmolLM2-360M F16 llama.cpp CPU (161), SmolLM2-360M F16 llama.cpp GPU (2.01), Qwen3-0.6B Q4 MNN CPU (227), Qwen3-0.6B Q4_0 llama.cpp GPU (35.1), Qwen3-0.6B Q4 MNN GPU (214), Qwen3-0.6B Q8 MNN CPU (239), Qwen3-0.6B Q8_0 llama.cpp GPU (2.82), Qwen3-0.6B Q8 MNN GPU (213), Qwen3-0.6B F16 llama.cpp CPU (99.5), Qwen3-0.6B F16 MNN CPU (168), Qwen3-0.6B F16 llama.cpp GPU (1.43), Qwen3.5-0.8B Q4_0 llama.cpp CPU (207), Qwen3.5-0.8B Q4 MNN CPU (167), Qwen3.5-0.8B Q4 MNN GPU (207), Qwen3.5-0.8B Q8_0 llama.cpp CPU (204), Qwen3.5-0.8B Q8 MNN CPU (182), Qwen3.5-0.8B Q8 MNN GPU (182), Qwen3.5-0.8B F16 llama.cpp CPU (80.2), Qwen3.5-0.8B F16 MNN CPU (153), Qwen3-1.7B Q4_0 llama.cpp CPU (99.3), Qwen3-1.7B Q4 MNN CPU (81.8), Qwen3-1.7B Q4_0 llama.cpp GPU (28.9), Qwen3-1.7B Q4 MNN GPU (77.3), Qwen3-1.7B Q8_0 llama.cpp CPU (96.8), Qwen3-1.7B Q8 MNN CPU (83.2), Qwen3-1.7B Q8_0 llama.cpp GPU (0.88), Qwen3-1.7B Q8 MNN GPU (73.8), Qwen3-1.7B F16 llama.cpp CPU (27.4), Qwen3-1.7B F16 llama.cpp GPU (0.46), Qwen3.5-2B Q4_0 llama.cpp CPU (98.8), Qwen3.5-2B Q4 MNN CPU (77.9), Qwen3.5-2B Q4 MNN GPU (102), Qwen3.5-2B Q8_0 llama.cpp CPU (94), Qwen3.5-2B Q8 MNN CPU (81.6), Qwen3.5-2B Q8 MNN GPU (83.7), Qwen3.5-2B F16 llama.cpp CPU (28.2), Qwen3-4B Q4_0 llama.cpp CPU (41.1), Qwen3-4B Q4 MNN CPU (32.1), Qwen3-4B Q4_0 llama.cpp GPU (10.9), Qwen3-4B Q4 MNN GPU (31.1), Qwen3-4B Q8 MNN CPU (31), Qwen3-4B Q8 MNN GPU (26.4), Qwen3.5-4B Q4_0 llama.cpp CPU (39.8), Qwen3.5-4B Q4 MNN CPU (29.6), Qwen3.5-4B Q4 MNN GPU (39.5), Qwen3.5-4B Q8 MNN GPU (32.7)
- **Decode under 5 tokens/s (10):** Qwen3-0.6B Q4_0 llama.cpp GPU (3.49), Qwen3-0.6B Q8_0 llama.cpp GPU (4.35), Qwen3-0.6B F16 llama.cpp GPU (4.5), Qwen3-1.7B Q4_0 llama.cpp GPU (2.58), Qwen3-1.7B Q8_0 llama.cpp GPU (3.24), Qwen3-1.7B F16 llama.cpp GPU (3.09), Qwen3-4B Q4_0 llama.cpp GPU (1.66), Qwen3-4B Q8 MNN CPU (4.85), Qwen3-4B Q8 MNN GPU (0.71), Qwen3.5-4B Q8 MNN GPU (3.96)
- **Energy over 25.1 J per request (36):** SmolLM2-135M Q8_0 llama.cpp GPU (50.6 J), SmolLM2-135M F16 llama.cpp GPU (94.3 J), Gemma3-270M Q8_0 llama.cpp GPU (55.3 J), Gemma3-270M F16 llama.cpp GPU (91.6 J), SmolLM2-360M Q8_0 llama.cpp GPU (140 J), SmolLM2-360M F16 llama.cpp CPU (27.4 J), Qwen3-0.6B Q4_0 llama.cpp GPU (35.7 J), Qwen3-0.6B F16 llama.cpp CPU (43.1 J), Qwen3-0.6B F16 MNN CPU (33 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (27.2 J), Qwen3.5-0.8B F16 llama.cpp CPU (53 J), Qwen3.5-0.8B F16 MNN CPU (38.1 J), Qwen3-1.7B Q4_0 llama.cpp CPU (36.9 J), Qwen3-1.7B Q4 MNN CPU (40.3 J), Qwen3-1.7B Q4_0 llama.cpp GPU (55.1 J), Qwen3-1.7B Q4 MNN GPU (30.1 J), Qwen3-1.7B Q8_0 llama.cpp CPU (55.3 J), Qwen3-1.7B Q8 MNN CPU (56.9 J), Qwen3-1.7B Q8 MNN GPU (43.2 J), Qwen3-1.7B F16 llama.cpp CPU (122 J), Qwen3.5-2B Q4_0 llama.cpp CPU (40.9 J), Qwen3.5-2B Q4 MNN CPU (36.5 J), Qwen3.5-2B Q8_0 llama.cpp CPU (60.1 J), Qwen3.5-2B Q8 MNN CPU (53.2 J), Qwen3.5-2B Q8 MNN GPU (37.9 J), Qwen3.5-2B F16 llama.cpp CPU (128 J), Qwen3-4B Q4_0 llama.cpp CPU (84.6 J), Qwen3-4B Q4 MNN CPU (96.1 J), Qwen3-4B Q4_0 llama.cpp GPU (127 J), Qwen3-4B Q4 MNN GPU (69.7 J), Qwen3-4B Q8 MNN CPU (135 J), Qwen3-4B Q8 MNN GPU (163 J), Qwen3.5-4B Q4_0 llama.cpp CPU (92.1 J), Qwen3.5-4B Q4 MNN CPU (84.1 J), Qwen3.5-4B Q4 MNN GPU (56 J), Qwen3.5-4B Q8 MNN GPU (90.2 J)
- **No energy reading (5):** SmolLM2-360M F16 llama.cpp GPU, Qwen3-0.6B Q8_0 llama.cpp GPU, Qwen3-0.6B F16 llama.cpp GPU, Qwen3-1.7B Q8_0 llama.cpp GPU, Qwen3-1.7B F16 llama.cpp GPU
- **Cannot run on this phone (25):** SmolLM2-135M F16 MNN GPU (crash/error (exit 139)), Gemma3-270M F16 MNN GPU (crash/error (exit 139)), SmolLM2-360M F16 MNN GPU (crash/error (exit 139)), Qwen3-0.6B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-0.8B Q4_0 llama.cpp GPU (not run), Qwen3.5-0.8B Q8_0 llama.cpp GPU (not run), Qwen3.5-0.8B F16 llama.cpp GPU (not run), Qwen3.5-0.8B F16 MNN GPU (crash/error (exit 139)), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B F16 MNN GPU (crash/error (exit 139)), Qwen3.5-2B Q4_0 llama.cpp GPU (not run), Qwen3.5-2B Q8_0 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp GPU (not run), Qwen3.5-2B F16 MNN GPU (crash/error (exit 139)), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp GPU (crash/error (exit 1)), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp GPU (does not fit in RAM), Qwen3-4B F16 MNN GPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp GPU (not run), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp GPU (not run)

</details>

### H20

**Hard · Redmi 9A · Document translation**: translate a page while the user does something else; 500-token prompt, 500-token answer, first token within 30 s, 10 requests ≤ 3% of battery a day.

Limits: peak memory ≤ 1,500 MB · prefill ≥ 16.7 tokens/s · decode ≥ 2 tokens/s · energy ≤ 208 J per request.

**Pick: Gemma3-270M Q8, MNN, CPU** (745 MB, 35.2 / 6.71 tokens/s, 93.3 J). Kept 5 of 52 configurations (10%); 29 filtered out; 18 cannot run on this phone.

![H20](figures/scenarios/H20.png)

<details><summary>Kept pool (5), most capable first</summary>

| Configuration | Peak memory (MB) | Prefill (tokens/s) | Decode (tokens/s) | Energy (J per request) |
|---|--:|--:|--:|--:|
| Gemma3-270M Q8 MNN CPU **(pick)** | 745 | 35.2 | 6.71 | 93.3 |
| Gemma3-270M Q4 MNN CPU | 326 | 39.9 | 7.72 | 84.1 |
| SmolLM2-135M F16 MNN CPU | 637 | 43 | 8.17 | 86.9 |
| SmolLM2-135M Q8 MNN CPU | 225 | 32.5 | 13.2 | 53.8 |
| SmolLM2-135M Q4 MNN CPU | 158 | 36.5 | 16.3 | 44.5 |

</details>

<details><summary>Filtered out (29) and cannot run (18)</summary>

- **Peak memory over 1,500 MB (8):** Gemma3-270M F16 MNN CPU (1,681 MB), SmolLM2-360M F16 MNN CPU (1,614 MB), Qwen3-0.6B F16 llama.cpp CPU (1,612 MB), Qwen3.5-0.8B F16 llama.cpp CPU (1,615 MB), Qwen3-1.7B Q4 MNN CPU (1,543 MB), Qwen3-1.7B Q8 MNN CPU (2,357 MB), Qwen3.5-2B Q4 MNN CPU (1,663 MB), Qwen3.5-2B Q8_0 llama.cpp CPU (2,116 MB)
- **Prefill under 16.7 tokens/s (28):** SmolLM2-135M Q4_0 llama.cpp CPU (11.6), SmolLM2-135M Q8_0 llama.cpp CPU (11.2), SmolLM2-135M F16 llama.cpp CPU (1.54), Gemma3-270M Q4_0 llama.cpp CPU (11.7), Gemma3-270M Q8_0 llama.cpp CPU (11.3), Gemma3-270M F16 llama.cpp CPU (1.64), SmolLM2-360M Q4_0 llama.cpp CPU (5.39), SmolLM2-360M Q4 MNN CPU (14.8), SmolLM2-360M Q8_0 llama.cpp CPU (5.14), SmolLM2-360M Q8 MNN CPU (12.7), SmolLM2-360M F16 llama.cpp CPU (0.54), SmolLM2-360M F16 MNN CPU (15.9), Qwen3-0.6B Q4_0 llama.cpp CPU (3.31), Qwen3-0.6B Q4 MNN CPU (11.4), Qwen3-0.6B Q8_0 llama.cpp CPU (3.18), Qwen3-0.6B Q8 MNN CPU (9.59), Qwen3-0.6B F16 llama.cpp CPU (0.38), Qwen3.5-0.8B Q4_0 llama.cpp CPU (4.59), Qwen3.5-0.8B Q4 MNN CPU (9.1), Qwen3.5-0.8B Q8_0 llama.cpp CPU (4.31), Qwen3.5-0.8B Q8 MNN CPU (7.75), Qwen3.5-0.8B F16 llama.cpp CPU (0.33), Qwen3-1.7B Q4_0 llama.cpp CPU (1.78), Qwen3-1.7B Q4 MNN CPU (4.75), Qwen3-1.7B Q8 MNN CPU (3.76), Qwen3.5-2B Q4_0 llama.cpp CPU (2.2), Qwen3.5-2B Q4 MNN CPU (4.58), Qwen3.5-2B Q8_0 llama.cpp CPU (2.03)
- **Decode under 2 tokens/s (10):** SmolLM2-135M F16 llama.cpp CPU (1.09), Gemma3-270M F16 llama.cpp CPU (0.59), SmolLM2-360M F16 llama.cpp CPU (0.43), Qwen3-0.6B Q4_0 llama.cpp CPU (1.68), Qwen3-0.6B Q8_0 llama.cpp CPU (1.67), Qwen3-0.6B F16 llama.cpp CPU (0.26), Qwen3.5-0.8B F16 llama.cpp CPU (0.21), Qwen3-1.7B Q4_0 llama.cpp CPU (1.07), Qwen3.5-2B Q4_0 llama.cpp CPU (1.24), Qwen3.5-2B Q8_0 llama.cpp CPU (1.2)
- **Energy over 208 J per request (19):** SmolLM2-135M F16 llama.cpp CPU (618 J), Gemma3-270M F16 llama.cpp CPU (900 J), SmolLM2-360M Q4_0 llama.cpp CPU (226 J), SmolLM2-360M Q8_0 llama.cpp CPU (266 J), SmolLM2-360M F16 llama.cpp CPU (1,634 J), SmolLM2-360M F16 MNN CPU (210 J), Qwen3-0.6B Q4_0 llama.cpp CPU (384 J), Qwen3-0.6B Q8_0 llama.cpp CPU (446 J), Qwen3-0.6B F16 llama.cpp CPU (2,513 J), Qwen3.5-0.8B Q4_0 llama.cpp CPU (412 J), Qwen3.5-0.8B Q8_0 llama.cpp CPU (486 J), Qwen3.5-0.8B Q8 MNN CPU (225 J), Qwen3.5-0.8B F16 llama.cpp CPU (3,753 J), Qwen3-1.7B Q4_0 llama.cpp CPU (734 J), Qwen3-1.7B Q4 MNN CPU (311 J), Qwen3-1.7B Q8 MNN CPU (485 J), Qwen3.5-2B Q4_0 llama.cpp CPU (821 J), Qwen3.5-2B Q4 MNN CPU (359 J), Qwen3.5-2B Q8_0 llama.cpp CPU (923 J)
- **Cannot run on this phone (18):** Qwen3-0.6B F16 MNN CPU (does not fit in RAM), Qwen3.5-0.8B F16 MNN CPU (does not fit in RAM), Qwen3-1.7B Q8_0 llama.cpp CPU (crash/error (exit 1)), Qwen3-1.7B F16 llama.cpp CPU (does not fit in RAM), Qwen3-1.7B F16 MNN CPU (does not fit in RAM), Qwen3.5-2B Q8 MNN CPU (does not fit in RAM), Qwen3.5-2B F16 llama.cpp CPU (does not fit in RAM), Qwen3.5-2B F16 MNN CPU (does not fit in RAM), Qwen3-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q4 MNN CPU (does not fit in RAM), Qwen3-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3-4B Q8 MNN CPU (does not fit in RAM), Qwen3-4B F16 llama.cpp CPU (does not fit in RAM), Qwen3-4B F16 MNN CPU (does not fit in RAM), Qwen3.5-4B Q4_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q4 MNN CPU (does not fit in RAM), Qwen3.5-4B Q8_0 llama.cpp CPU (does not fit in RAM), Qwen3.5-4B Q8 MNN CPU (does not fit in RAM)

</details>

