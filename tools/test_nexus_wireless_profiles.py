#!/usr/bin/env python3
"""Run the Nexus wireless GameTests under all three balance policies.

The live development config is restored after every run, including failures.
"""

import json
import re
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / "run/config/gtna/balance/nexus_flux_matrix.json"
EXAMPLE = ROOT / "docs/config-examples/gtia/nexus_flux_matrix.json"
LOG_DIR = ROOT / "build/test-results/nexus-wireless-profiles"


def run_profile(name: str, contents: dict) -> bool:
    CONFIG.write_text(json.dumps(contents, indent=2) + "\n", encoding="utf-8")
    log = LOG_DIR / f"{name}.log"
    with log.open("w", encoding="utf-8") as output:
        result = subprocess.run(
            ["./gradlew", "runGameTestServer", "--offline"],
            cwd=ROOT,
            stdout=output,
            stderr=subprocess.STDOUT,
            check=False,
        )
    text = log.read_text(encoding="utf-8")
    count = re.search(r"All (\d+) required tests passed", text)
    policy = f"Nexus Flux Matrix wireless loss policy: {contents['lossApplication']}"
    passed = result.returncode == 0 and count is not None and int(count.group(1)) >= 103 and policy in text
    print(f"{name}: {'PASS' if passed else 'FAIL'} ({log})", flush=True)
    return passed


def main() -> int:
    if not CONFIG.exists():
        print(f"Missing development config: {CONFIG}", file=sys.stderr)
        return 2
    original = CONFIG.read_bytes()
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    try:
        legacy = json.loads(original)
        legacy["lossApplication"] = "LEGACY"
        legacy["generatorArrayAppliesSeparateLoss"] = True
        matrix = json.loads(EXAMPLE.read_text(encoding="utf-8"))
        no_loss = json.loads(original)
        no_loss["lossApplication"] = "NO_LOSS"
        no_loss["generatorArrayAppliesSeparateLoss"] = False
        results = [
            run_profile("legacy", legacy),
            run_profile("matrix_input_once", matrix),
            run_profile("no_loss", no_loss),
        ]
        return 0 if all(results) else 1
    finally:
        CONFIG.write_bytes(original)


if __name__ == "__main__":
    raise SystemExit(main())
