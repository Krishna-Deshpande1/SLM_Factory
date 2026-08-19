#!/usr/bin/env python3
"""Pre-flight check: which model variants can realistically fit in the
connected Android phone's current free RAM, before any download/conversion.

Only Q4_K_M/Q8_0 variants are evaluated - bf16/null-quant variants aren't
supported by MNN export at all (see run_model_pool.py's resolve_quant_bit()),
so they're filtered out up front rather than being scored and then never
usable. Each remaining variant's own peak_memory_mb field (a pool-provided,
already-measured/trusted figure) is compared directly against the phone's
current free RAM, with no scaling multiplier or safety margin applied.

Usage:
    python3 check_model_fit.py --pool model_pool_unfiltered.json --output model_fit_report.json
"""

import argparse
import datetime
import json
import re
import subprocess
import sys

MEM_AVAILABLE_LINE_RE = re.compile(r"MemAvailable:\s*(\d+)\s*kB", re.IGNORECASE)


def get_phone_free_ram_mb():
    """Query the connected device's current available RAM via
    `adb shell cat /proc/meminfo`, parsing the "MemAvailable:" line.

    MemAvailable is a more conservative, more realistic estimate of
    genuinely usable memory than dumpsys meminfo's "Free RAM" figure, which
    includes cached memory that may require real reclaim activity to
    actually reclaim.

    Returns the available RAM in MB (float). Raises RuntimeError with a
    clear description if adb isn't available, no device is connected, or
    the "MemAvailable:" line can't be found/parsed.
    """
    try:
        proc = subprocess.run(
            ["adb", "shell", "cat", "/proc/meminfo"],
            capture_output=True,
            text=True,
            timeout=30,
        )
    except FileNotFoundError as exc:
        raise RuntimeError(f"adb not found on PATH: {exc}") from exc
    except subprocess.TimeoutExpired as exc:
        raise RuntimeError("adb shell cat /proc/meminfo timed out after 30s") from exc

    if proc.returncode != 0:
        raise RuntimeError(
            f"adb shell cat /proc/meminfo failed (exit {proc.returncode}): {proc.stderr.strip()}"
        )

    match = MEM_AVAILABLE_LINE_RE.search(proc.stdout)
    if not match:
        raise RuntimeError(
            "Could not find a 'MemAvailable:' line in /proc/meminfo output. "
            "Is a device connected and authorized?"
        )

    return float(match.group(1)) / 1024.0


SUPPORTED_QUANTS = {"Q4_K_M", "Q8_0"}


def load_pool(pool_path):
    with open(pool_path, "r") as f:
        data = json.load(f)
    return data["variants"]


def filter_supported_quants(variants):
    """Keep only Q4_K_M/Q8_0 variants - bf16/null-quant variants are skipped
    entirely (not evaluated, not scored) since MNN export doesn't support
    them at all."""
    return [v for v in variants if v.get("quant") in SUPPORTED_QUANTS]


def evaluate_variant(variant, phone_free_ram_mb):
    peak_memory_mb = variant["peak_memory_mb"]
    fits = peak_memory_mb <= phone_free_ram_mb

    result = dict(variant)
    result["fits"] = fits
    return result


def print_summary_table(evaluated_variants):
    headers = ["MODEL", "QUANT", "PEAK_MEMORY_MB", "FITS"]
    rows = []
    for v in evaluated_variants:
        rows.append([
            str(v.get("model_id", "")),
            str(v.get("quant", "")),
            f"{v.get('peak_memory_mb', 0):.0f}",
            "Y" if v["fits"] else "N",
        ])

    widths = [
        max(len(headers[i]), max((len(r[i]) for r in rows), default=0))
        for i in range(len(headers))
    ]

    def fmt_row(cols):
        return "  ".join(col.ljust(widths[i]) for i, col in enumerate(cols))

    print(fmt_row(headers))
    print("  ".join("-" * w for w in widths))
    for row in rows:
        print(fmt_row(row))


def main():
    parser = argparse.ArgumentParser(
        description="Check which model variants fit in the connected phone's current free RAM."
    )
    parser.add_argument("--pool", required=True, help="Path to the input model pool JSON file.")
    parser.add_argument(
        "--output", default="model_fit_report.json",
        help="Path to write the fit report JSON to (default: model_fit_report.json).",
    )
    args = parser.parse_args()

    try:
        variants = load_pool(args.pool)
    except (OSError, json.JSONDecodeError, KeyError) as exc:
        print(f"[check_model_fit] Failed to load pool file '{args.pool}': {exc}", file=sys.stderr)
        sys.exit(1)

    supported = filter_supported_quants(variants)
    n_skipped = len(variants) - len(supported)

    try:
        phone_free_ram_mb = get_phone_free_ram_mb()
    except RuntimeError as exc:
        print(f"[check_model_fit] Failed to query phone free RAM: {exc}", file=sys.stderr)
        sys.exit(1)

    evaluated = [evaluate_variant(v, phone_free_ram_mb) for v in supported]

    fits = [v for v in evaluated if v["fits"]]
    does_not_fit = [v for v in evaluated if not v["fits"]]

    report = {
        "phone_free_ram_mb": phone_free_ram_mb,
        "timestamp": datetime.datetime.now().isoformat(),
        "fits": fits,
        "does_not_fit": does_not_fit,
    }

    with open(args.output, "w") as f:
        json.dump(report, f, indent=2)

    print(f"Phone free RAM: {phone_free_ram_mb:.0f} MB")
    if n_skipped:
        print(f"Skipped {n_skipped} variant(s) with unsupported quant (not Q4_K_M/Q8_0)")
    print()
    print_summary_table(evaluated)
    print()
    print(f"{len(fits)}/{len(evaluated)} variants fit. Report written to {args.output}")


if __name__ == "__main__":
    main()
