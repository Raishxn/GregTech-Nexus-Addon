"""Merge the Forge of Gods PT-BR translations (tools/godforge_lang_pt_br_*.py) into pt_br.json.

Keys left out of the translation tables (upgrade short codes, contributor names, colour codes) fall back to the
English text extracted from GTNH by tools/extract_godforge_lang.py.
"""

from pathlib import Path
import importlib.util
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
LANG = ROOT / "src/main/resources/assets/gtna/lang/pt_br.json"


def load(name):
    spec = importlib.util.spec_from_file_location(name, ROOT / "tools" / f"{name}.py")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module.PT


def main():
    sys.path.insert(0, str(ROOT / "tools"))
    import extract_godforge_lang as extract
    english = {}
    source = Path(sys.argv[1]) / "src/main/resources/assets/tectech/lang/en_US.lang"
    for line in source.read_text(encoding="utf-8").splitlines():
        if "=" not in line or line.startswith("#"):
            continue
        key, value = line.split("=", 1)
        if key.startswith(extract.SKIP):
            continue
        new = extract.rename(key)
        if new:
            english[new] = value.replace("\\n", "\n")
    pt = {}
    for part in ("godforge_lang_pt_br_1", "godforge_lang_pt_br_2", "godforge_lang_pt_br_3"):
        pt.update(load(part))
    text = LANG.read_text(encoding="utf-8")
    data = json.loads(text)
    missing = []
    for key, value in english.items():
        if key in pt:
            data[key] = pt[key]
        else:
            data[key] = value
            missing.append(key)
    for key, value in pt.items():
        data[key] = value
    LANG.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"merged {len(english)} keys; untranslated (kept in English): {len(missing)}")
    for key in missing:
        print("  ", key)


if __name__ == "__main__":
    main()
