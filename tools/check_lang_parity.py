#!/usr/bin/env python3
"""Report translation keys that exist in the generated en_us but not in a locale file.

Usage:
    python3 tools/check_lang_parity.py                 # informational, exit 0
    python3 tools/check_lang_parity.py --strict        # exit 1 if any key is missing
    python3 tools/check_lang_parity.py --locale pt_br

The mod falls back to en_us for missing keys, so this is a *parity* report for the
localization backlog (see docs/roadmap/tooltip-standard.md), not a build gate.
"""

import argparse
import json
import sys
from pathlib import Path

PREFIXES = ("gtna.machine.", "gtna.tooltip.", "gtna.multiblock.", "gtna.source.", "block.gtna.", "item.gtna.")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--locale", default="pt_br")
    parser.add_argument("--strict", action="store_true")
    args = parser.parse_args()

    generated = Path("src/generated/resources/assets/gtna/lang/en_us.json")
    manual = Path(f"src/main/resources/assets/gtna/lang/{args.locale}.json")
    if not generated.exists():
        print(f"missing generated lang: {generated} (run ./gradlew runData)", file=sys.stderr)
        return 1
    if not manual.exists():
        print(f"missing locale file: {manual}", file=sys.stderr)
        return 1

    en = json.loads(generated.read_text(encoding="utf-8-sig"))
    loc = json.loads(manual.read_text(encoding="utf-8-sig"))

    missing = [k for k in en if k not in loc]
    total = len(en)
    print(f"locale {args.locale}: {total - len(missing)}/{total} keys present "
          f"({len(missing)} missing, fall back to en_us)")
    for prefix in PREFIXES:
        group = [k for k in missing if k.startswith(prefix)]
        if group:
            print(f"  {prefix:<22} {len(group):>4} missing")
    if missing and args.strict:
        print("first 20 missing:", *missing[:20], sep="\n  ")
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
