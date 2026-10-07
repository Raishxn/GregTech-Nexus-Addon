"""Port the GTNH custom material texture sets used by the Eye of Harmony / Forge of Gods materials.

Usage: python3 tools/port_godforge_icon_sets.py <GT5-Unofficial checkout or Modernity pack> [GTOCore checkout]

Source: GT5-Unofficial a3e1e112 (LGPL-3.0), assets/gregtech/textures/{items,blocks}/materialicons/CUSTOM/<set>
and blocks/fluids. GTCEu looks up icon set textures under gtceu:textures/{item,block}/material_sets/<set>/, so the
files are written there with GTCEu's names; fluid stills go to gtna:textures/block/fluids/fluid.<name>.png.
"""

from pathlib import Path
import shutil
import sys
import json

ROOT = Path(__file__).resolve().parents[1]
GTCEU = ROOT / "src/main/resources/assets/gtceu/textures"
GTNA_FLUIDS = ROOT / "src/main/resources/assets/gtna/textures/block/fluids"
SETS = {  # GTNH folder -> GTCEu icon set name
    "spacetime": "spacetime",
    "universium": "universium",
    "WhiteDwarfMatter": "white_dwarf_matter",
    "MagnetohydrodynamicallyConstrainedStarMatter": "mhdcsm",
    "magmatter": "magmatter",
    "GravitonShard": "graviton_shard",
}
MODERNITY_SETS = {**SETS, "infinity": "infinity", "eternity": "eternity"}
ITEMS = {
    "dust": "dust", "dustSmall": "dust_small", "dustTiny": "dust_tiny", "ingot": "ingot", "ingotHot": "ingot_hot",
    "nugget": "nugget", "plate": "plate", "plateDouble": "plate_double", "plateDense": "plate_dense", "foil": "foil",
    "gearGt": "gear", "gearGtSmall": "gear_small", "stick": "rod", "stickLong": "rod_long", "bolt": "bolt",
    "screw": "screw", "ring": "ring", "round": "round", "rotor": "rotor", "spring": "spring",
    "springSmall": "spring_small", "wireFine": "wire_fine", "gem": "gem", "turbineBlade": "turbine_blade",
}
BLOCKS = {"frameGt": "frame_gt", "block1": "block"}
FLUIDS = {  # GTNH fluid texture -> GTNA fluid registry name
    "fluid.molten.spacetime": "space_time", "fluid.molten.universium": "universium",
    "fluid.molten.whitedwarfmatter": "white_dwarf_matter",
    "fluid.molten.magnetohydrodynamicallyconstrainedstarmatter": "magnetohydrodynamically_constrained_star_matter",
    "fluid.molten.magmatter": "magmatter", "fluid.dimensionallytranscendentresidue":
        "dimensionally_transcendent_residue", "fluid.quarkgluonplasma": "quark_gluon_plasma",
    "fluid.rawstarmatter": "raw_star_matter",
}


def copy(src: Path, dst: Path):
    if not src.exists():
        return 0
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(src, dst)
    meta = src.with_name(src.name + ".mcmeta")
    if meta.exists():
        shutil.copyfile(meta, dst.with_name(dst.name + ".mcmeta"))
    return 1


def main():
    source = Path(sys.argv[1])
    modernity = (source / "assets/gregtech/textures").is_dir()
    base = source / ("assets/gregtech/textures" if modernity else
                     "src/main/resources/assets/gregtech/textures")
    count = 0
    for folder, name in (MODERNITY_SETS if modernity else SETS).items():
        for gtnh, gtceu in ITEMS.items():
            src = base / "items/materialicons/CUSTOM" / folder / f"{gtnh}.png"
            count += copy(src, GTCEU / "item/material_sets" / name / f"{gtceu}.png")
            count += copy(src.with_name(f"{gtnh}_OVERLAY.png"),
                          GTCEU / "item/material_sets" / name / f"{gtceu}_overlay.png")
        for gtnh, gtceu in BLOCKS.items():
            count += copy(base / "blocks/materialicons/CUSTOM" / folder / f"{gtnh}.png",
                          GTCEU / "block/material_sets" / name / f"{gtceu}.png")
    if not modernity:
        for gtnh, name in FLUIDS.items():
            count += copy(base / "blocks/fluids" / f"{gtnh}.png", GTNA_FLUIDS / f"fluid.{name}.png")
    if len(sys.argv) > 2:
        gto = Path(sys.argv[2]) / "src/main/resources/assets/gtocore/textures"
        for source_texture in (gto / "item/material_sets/black_dwarf_mtter").glob("*.png"):
            count += copy(source_texture,
                          GTCEU / "item/material_sets/black_dwarf_matter" / source_texture.name)
        count += copy(gto / "block/black_dwarf_mtter_block.png",
                      GTCEU / "block/material_sets/black_dwarf_matter/block.png")
    # GTCEu draws <type>_secondary and <type>_overlay on top of the base layer and falls back to the parent
    # set when they are missing; GTNH textures are complete, so the extra layers are left transparent.
    from PIL import Image
    empty = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    names = list((MODERNITY_SETS if modernity else SETS).values())
    if len(sys.argv) > 2:
        names.append("black_dwarf_matter")
    for name in names:
        folder = GTCEU / "item/material_sets" / name
        for texture in list(folder.glob("*.png")):
            if texture.stem.endswith(("_overlay", "_secondary")):
                continue
            for suffix in ("_secondary", "_overlay"):
                layer = folder / f"{texture.stem}{suffix}.png"
                if not layer.exists():
                    empty.save(layer)
        models = ROOT / "src/main/resources/assets/gtceu/models/item/material_sets" / name
        models.mkdir(parents=True, exist_ok=True)
        for texture in folder.glob("*.png"):
            stem = texture.stem
            if stem.endswith(("_overlay", "_secondary")):
                continue
            model = {"parent": "item/generated", "textures": {
                "layer0": f"gtceu:item/material_sets/{name}/{stem}",
                "layer1": f"gtceu:item/material_sets/{name}/{stem}_secondary",
                "layer2": f"gtceu:item/material_sets/{name}/{stem}_overlay"}}
            (models / f"{stem}.json").write_text(json.dumps(model, indent=2) + "\n")
        block_folder = GTCEU / "block/material_sets" / name
        block_models = ROOT / "src/main/resources/assets/gtceu/models/block/material_sets" / name
        block_models.mkdir(parents=True, exist_ok=True)
        for texture in block_folder.glob("*.png"):
            if texture.stem.endswith(("_overlay", "_secondary")):
                continue
            stem = texture.stem
            secondary = block_folder / f"{stem}_secondary.png"
            if not secondary.exists():
                empty.save(secondary)
            icon = f"gtceu:block/material_sets/{name}/{stem}"
            model = {"parent": "block/block", "loader": "forge:composite",
                     "textures": {"particle": icon}, "children": {
                         "base": {"parent": "gtceu:block/cube/tinted/all_0", "render_type":
                                  "cutout" if stem == "frame_gt" else "solid",
                                  "textures": {"all": icon}},
                         "secondary": {"parent": "gtceu:block/cube/tinted/all", "render_type":
                                       "translucent", "textures": {"all": icon + "_secondary"}}},
                     "item_render_order": ["base", "secondary"]}
            (block_models / f"{stem}.json").write_text(json.dumps(model, indent=2) + "\n")
    print(f"copied {count} textures")


if __name__ == "__main__":
    main()
