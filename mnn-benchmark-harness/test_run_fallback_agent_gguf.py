#!/usr/bin/env python3
"""
test_run_fallback_agent_gguf.py — Verifies run_fallback_agent_gguf.py's
per-question stop-on-first-garbage logic via a mocked subprocess.run(), so
these scenarios can be re-run any time without a real device/model.

Run directly: python3 test_run_fallback_agent_gguf.py
"""

import importlib.util
import json
import subprocess
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent


def _load_module(path: Path, name: str):
    spec = importlib.util.spec_from_file_location(name, str(path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


m2 = _load_module(SCRIPT_DIR / "run_fallback_agent_gguf.py", "_run_fallback_agent_gguf")
quality = m2._load_module(m2.RESPONSE_QUALITY_SCRIPT, "_response_quality")

# Conversion is mocked out for every scenario - these tests are about the
# per-question garbage-checking/stop logic, not GGUF conversion itself.
m2.ensure_gguf_converted = lambda model_id, quant, label: {"ok": True, "local_path": Path("/tmp/fake.gguf")}


def run_with_fake_results(flagged_model, fake_results):
    """Runs process_flagged_model() with subprocess.run() mocked to write
    `fake_results` (a list of run_autobench.py-shaped result dicts) to the
    --output path, then restores the real subprocess.run()."""
    orig_run = subprocess.run

    def fake_run(cmd, timeout=None):
        output_path = cmd[cmd.index("--output") + 1]
        with open(output_path, "w") as f:
            json.dump({"run_info": {"completed": len(fake_results), "total": len(fake_results)},
                       "results": fake_results}, f)
        class R:
            returncode = 0
        return R()

    subprocess.run = fake_run
    try:
        return m2.process_flagged_model(quality, flagged_model, 1, 1, 30, False, None)
    finally:
        subprocess.run = orig_run


def success_result(rid, response, tps=10.0):
    return {"status": "success", "run_id": rid, "response": response, "metrics": {"tps": tps}}


def main():
    # -------------------------------------------------------------------
    # Scenario 1: every flagged question passes -> whole model succeeds,
    # everything recorded.
    # -------------------------------------------------------------------
    flagged = {
        "model_id": "Qwen/Qwen3-0.6B", "quant": "Q4_K_M",
        "questions": [
            {"question_number": 5, "text": "What is 2+2?", "answer": "4"},
            {"question_number": 6, "text": "What is 3+3?", "answer": "6"},
        ],
    }
    r1 = run_with_fake_results(flagged, [
        success_result("g5", "The answer is 4."),
        success_result("g6", "The answer is 6."),
    ])
    assert r1["status"] == "success"
    assert len(r1["gguf_results"]) == 2
    assert [e["question_number"] for e in r1["gguf_results"]] == [5, 6]
    assert all(e["is_correct"] for e in r1["gguf_results"])
    print("Scenario 1 PASS - all questions pass, all recorded, status=success")

    # -------------------------------------------------------------------
    # Scenario 2: the FIRST flagged question (the one that originally
    # failed on MNN) is ALSO garbage -> model failed, nothing recorded,
    # even though a later question in the same batch would have been fine.
    # -------------------------------------------------------------------
    r2 = run_with_fake_results(flagged, [
        success_result("g5", ""),  # empty response -> garbage
        success_result("g6", "The answer is 6."),  # would have been fine
    ])
    assert r2["status"] == "failed"
    assert len(r2["gguf_results"]) == 0
    assert "Q5" in r2["error"]
    print("Scenario 2 PASS - first flagged question also garbage -> failed, nothing recorded")

    # -------------------------------------------------------------------
    # Scenario 3 (NEW): the FIRST flagged question SUCCEEDS, a LATER one is
    # garbage. Must stop at the garbage question - the later one must NOT
    # be recorded, and nothing after it should even be considered - while
    # the genuinely-successful question(s) before it ARE kept and the model
    # is still marked "failed" overall (mirrors Stage 1's own
    # stop-on-first-garbage rule, just starting mid-batch).
    # -------------------------------------------------------------------
    flagged_three = {
        "model_id": "Qwen/Qwen3-1.7B", "quant": "Q8_0",
        "questions": [
            {"question_number": 5, "text": "What is 2+2?", "answer": "4"},
            {"question_number": 6, "text": "What is 3+3?", "answer": "6"},
            {"question_number": 7, "text": "What is 4+4?", "answer": "8"},
        ],
    }
    r3 = run_with_fake_results(flagged_three, [
        success_result("g5", "The answer is 4."),   # Q5: genuinely succeeds
        success_result("g6", ""),                    # Q6: garbage - stop here
        success_result("g7", "The answer is 8."),    # Q7: would have been fine, must NOT be attempted/recorded
    ])
    assert r3["status"] == "failed", f"expected failed, got {r3['status']}"
    assert len(r3["gguf_results"]) == 1, f"expected exactly 1 kept result (Q5), got {len(r3['gguf_results'])}"
    assert r3["gguf_results"][0]["question_number"] == 5
    assert r3["gguf_results"][0]["is_correct"] is True
    recorded_question_numbers = [e["question_number"] for e in r3["gguf_results"]]
    assert 6 not in recorded_question_numbers, "the garbage question itself must not be recorded"
    assert 7 not in recorded_question_numbers, "questions after the garbage one must not be recorded"
    assert "Q6" in r3["error"]
    print("Scenario 3 PASS - Q5 succeeds and is kept, Q6 garbage stops processing, Q7 never recorded, status=failed")

    print("\nALL SCENARIOS PASSED")


if __name__ == "__main__":
    main()
