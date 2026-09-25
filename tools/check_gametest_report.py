#!/usr/bin/env python3
"""Require a complete, passing GameTest batch and its per-test JUnit report."""

import argparse
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def check(log_path: Path, report_path: Path) -> str:
    log = log_path.read_text(encoding="utf-8", errors="replace")
    batches = re.findall(r"=+ (\d+) GAME TESTS COMPLETE =+", log)
    passed = re.findall(r"All (\d+) required tests passed", log)
    if len(batches) != 1 or len(passed) != 1:
        raise ValueError("expected one completed, passing GameTest batch in the server log")
    expected = int(batches[0])
    if int(passed[0]) != expected or expected == 0:
        raise ValueError("GameTest pass count does not match the completed batch")

    root = ET.parse(report_path).getroot()
    cases = root.findall(".//testcase")
    if len(cases) != expected:
        raise ValueError(f"JUnit report has {len(cases)} cases, expected {expected}")
    failures = [case.get("name", "unknown") for case in cases
                if case.find("failure") is not None or case.find("error") is not None]
    if failures:
        raise ValueError("GameTest failures in JUnit report: " + ", ".join(failures))
    return f"GameTest QA: {expected} cases passed; JUnit report: {report_path}"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--log", type=Path, default=Path("run/logs/latest.log"))
    parser.add_argument("--report", type=Path,
                        default=Path("build/test-results/gametest/TEST-gtna.xml"))
    args = parser.parse_args()
    try:
        print(check(args.log, args.report))
    except (OSError, ET.ParseError, ValueError) as error:
        print(f"GameTest QA failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
