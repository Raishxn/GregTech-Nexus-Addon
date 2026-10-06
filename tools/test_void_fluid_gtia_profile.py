#!/usr/bin/env python3
"""Validate GTIA's fluid profile and deposit with the normal GameTests; restore dev files."""
import argparse
import json
import os
import re
import signal
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / "run/config/gtna/balance/void_fluid_drill.json"
EXAMPLE = ROOT / "docs/config-examples/gtia/void_fluid_drill.json"
DEPOSIT = ROOT / "run/kubejs/data/gtia/gtceu/fluid_veins/mars_radon.json"
DEPOSIT_EXAMPLE = ROOT / "docs/config-examples/gtia/void-fluid-datapack/data/gtia/gtceu/fluid_veins/mars_radon.json"
LOG = ROOT / "build/test-results/void-fluid-gtia/profile.log"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--core-dev-jar", type=Path)
    args = parser.parse_args()
    command = ["./gradlew", "runGameTestServer", "--offline"]
    if args.core_dev_jar:
        command.append(f"-PgtiaDevJar={args.core_dev_jar.resolve()}")
    originals = {path: path.read_bytes() if path.exists() else None for path in (CONFIG, DEPOSIT)}
    LOG.parent.mkdir(parents=True, exist_ok=True)
    try:
        CONFIG.parent.mkdir(parents=True, exist_ok=True)
        DEPOSIT.parent.mkdir(parents=True, exist_ok=True)
        CONFIG.write_bytes(EXAMPLE.read_bytes())
        DEPOSIT.write_bytes(DEPOSIT_EXAMPLE.read_bytes())
        with LOG.open("w", encoding="utf-8") as output:
            process = subprocess.Popen(command, cwd=ROOT,
                                       stdout=output, stderr=subprocess.STDOUT, start_new_session=True)
            try:
                code = process.wait(timeout=240)
            except BaseException:
                os.killpg(process.pid, signal.SIGTERM)
                process.wait(timeout=30)
                raise
        log = LOG.read_text(encoding="utf-8")
        count = re.search(r"All (\d+) required tests passed", log)
        passed = code == 0 and count is not None and int(count.group(1)) >= 127
        if args.core_dev_jar:
            passed = passed and "with {gtia} mods - versions {0.1.0}" in log
        passed = passed and "Void Fluid Drill: 7 programs" in log
        passed = passed and "GTIA Mars native extraction -> recorded data -> remote Radon: PASS" in log
        passed = passed and "FTB effective party discovery, copying and membership gates: PASS" in log
        print(f"GTIA fluid profile: {'PASS' if passed else 'FAIL'} ({LOG})", flush=True)
        return 0 if passed else 1
    finally:
        for path, data in originals.items():
            if data is None:
                path.unlink(missing_ok=True)
            else:
                path.write_bytes(data)


if __name__ == "__main__":
    raise SystemExit(main())
