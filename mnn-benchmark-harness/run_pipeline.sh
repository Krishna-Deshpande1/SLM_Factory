#!/bin/bash
# run_pipeline.sh — Runs the MNN-first, GGUF-fallback evaluation pipeline
# end to end, as ONE command, while keeping Stage 1 and Stage 2 as two
# genuinely separate top-level processes.
#
# WHY TWO SEPARATE PROCESSES: an earlier single-process design ran GGUF/
# SmolChat as a subprocess NESTED inside a long-running Python process, and
# that nesting had a confirmed, unresolved hang that survived 7 separate fix
# attempts across fundamentally different mechanisms (stdin handling, an MNN
# force-stop, uncaptured output with polling, session isolation, os.system()
# invocation, an ADB server restart) - all failed identically. The one thing
# that reliably worked every time was a fresh, non-nested, top-level process
# invocation. Stage 1 (run_fallback_agent_mnn.py) fully exits - this script
# waits for it, not backgrounds it - before Stage 2 (run_fallback_agent_gguf.py)
# starts, so Stage 2's run_autobench.py subprocess is never nested inside
# anything but this plain shell script.
#
# The SAME arguments are passed through to both stages (each stage's own
# argparse uses parse_known_args(), so it just ignores flags meant for the
# other stage - e.g. Stage 2 ignores --fit-report, Stage 1 ignores
# --gguf-output). Stage 1 writes needs_gguf_fallback.json; Stage 2 reads
# that same file via the same --fallback-file default, so as long as you
# don't override --fallback-file inconsistently between runs, this just
# works with one shared argument list.
#
# Usage:
#   ./run_pipeline.sh --fit-report model_fit_report_q4q8.json --questions eval_questions_phase3.json
#   ./run_pipeline.sh --fit-report ... --questions ... --timeout 300 --no-think

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "======================================================================"
echo "STAGE 1: MNN evaluation (run_fallback_agent_mnn.py)"
echo "======================================================================"
# "|| STAGE1_EXIT=$?" instead of a bare call + separate "STAGE1_EXIT=$?":
# under `set -e`, a bare nonzero exit would kill this script immediately at
# that line, before ever reaching the exit-code check below - this idiom
# captures the code without triggering errexit.
STAGE1_EXIT=0
python3 "$SCRIPT_DIR/run_fallback_agent_mnn.py" "$@" || STAGE1_EXIT=$?

if [ "$STAGE1_EXIT" -ne 0 ]; then
    echo ""
    echo "[run_pipeline.sh] Stage 1 exited with code $STAGE1_EXIT - aborting before Stage 2." >&2
    exit "$STAGE1_EXIT"
fi

echo ""
echo "======================================================================"
echo "STAGE 2: GGUF fallback evaluation (run_fallback_agent_gguf.py)"
echo "======================================================================"
STAGE2_EXIT=0
python3 "$SCRIPT_DIR/run_fallback_agent_gguf.py" "$@" || STAGE2_EXIT=$?

echo ""
echo "======================================================================"
echo "PIPELINE DONE (Stage 1 exit=$STAGE1_EXIT, Stage 2 exit=$STAGE2_EXIT)"
echo "======================================================================"

exit "$STAGE2_EXIT"
