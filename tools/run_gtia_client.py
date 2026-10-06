#!/usr/bin/env python3
"""Open GTNA + a mapped GTIACore jar with the checked-in GTIA fluid profile."""
import argparse
import datetime
import shutil
import subprocess
import tomllib
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--core-dev-jar", type=Path, required=True)
    args = parser.parse_args()
    core = args.core_dev_jar.resolve()
    with ZipFile(core) as jar:
        metadata = tomllib.loads(jar.read("META-INF/mods.toml").decode())
        dependencies = {d["modId"]: d for d in metadata["dependencies"]["gtia"]}
        if not dependencies["gtna"]["mandatory"]:
            raise ValueError("GTIACore must require GTNA even in development")
        if any(dependencies[name]["mandatory"] for name in ("avaritia", "gtmthings")):
            raise ValueError("Build GTIACore dev jar with -PgtiaDevOptionalDependencies=true")
    backup = ROOT / "run/gtia-profile-backups" / datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    backup.mkdir(parents=True)
    example = ROOT / "docs/config-examples/gtia"
    pairs = [
        (example / "void_fluid_drill.json", ROOT / "run/config/gtna/balance/void_fluid_drill.json"),
        (example / "electric_void_miner.json", ROOT / "run/config/gtna/balance/electric_void_miner.json"),
        (example / "void-fluid-datapack/data/gtia/gtceu/fluid_veins/mars_radon.json",
         ROOT / "run/kubejs/data/gtia/gtceu/fluid_veins/mars_radon.json"),
    ]
    for source, destination in pairs:
        relative = destination.relative_to(ROOT / "run")
        if destination.exists():
            saved = backup / relative
            saved.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(destination, saved)
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, destination)
    print(f"GTIA profile installed. Previous files preserved in {backup}", flush=True)
    # Keep the profile installed after closing so this QA world can be reopened consistently.
    return subprocess.call(["./gradlew", "runClient", "--offline", f"-PgtiaDevJar={core}"], cwd=ROOT)


if __name__ == "__main__":
    raise SystemExit(main())
