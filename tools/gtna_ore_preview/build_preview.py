#!/usr/bin/env python3
"""Build the GTNA ore-processing structure preview (self-contained HTML + gallery PNGs).

The script reads the *real* multiblock patterns from
``src/main/java/com/raishxn/gtna/common/data/GTNAMachines.java`` (Integrated and Advanced
Integrated Ore Processor) plus the KubeJS auxiliary-module example, composes the block tiles
from the GTCEu / GTNA textures, and injects everything into the viewer template.

The auxiliary modules drawn on top of the two structures are a *design proposal*; they are not
registered in the mod. Nothing in this folder touches the mod code.

Usage::

    python3 tools/gtna_ore_preview/build_preview.py --check           # parse + report only
    python3 tools/gtna_ore_preview/build_preview.py --html            # write the HTML
    python3 tools/gtna_ore_preview/build_preview.py --html --gallery  # + headless screenshots
"""

from __future__ import annotations

import argparse
import base64
import io
import json
import re
import shutil
import subprocess
import sys
from dataclasses import dataclass, field
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
JAVA_SRC = ROOT / "src/main/java/com/raishxn/gtna/common/data/GTNAMachines.java"
GTNA_TEX_DIR = ROOT / "src/main/resources/assets/gtna/textures/block"
TEMPLATE = ROOT / "tools/gtna_ore_preview/viewer_template.html"
OUT_DIR = ROOT / "docs/visualization"
GALLERY_DIR = OUT_DIR / "gallery"
LOGO = ROOT / "docs/assets/logo.png"

GTCEU_CANDIDATES = [Path("/home/raishxn/MineProjects/GTCEu-7.5.3")]

# --------------------------------------------------------------------------------------
# Texture tables (verified against the source checkouts)
# --------------------------------------------------------------------------------------

GTCEU_TEX = {
    "CASING_HSSE_STURDY": "casings/solid/machine_casing_sturdy_hsse.png",
    "CASING_STAINLESS_CLEAN": "casings/solid/machine_casing_clean_stainless_steel.png",
    "CASING_LAMINATED_GLASS": "casings/transparent/laminated_glass.png",
    "CASING_TUNGSTENSTEEL_GEARBOX": "casings/gearbox/machine_casing_gearbox_tungstensteel.png",
    "CASING_TUNGSTENSTEEL_PIPE": "casings/pipe/machine_casing_pipe_tungstensteel.png",
    "CASING_GRATE": "casings/pipe/machine_casing_grate.png",
    "CASING_TUNGSTENSTEEL_ROBUST": "casings/solid/machine_casing_robust_tungstensteel.png",
}

FRAME_TEX = "material_sets/dull/frame_gt.png"
FRAME_COLORS = {"BlueSteel": 0x779AC6, "HSSS": 0xA482BF}

# how each 16x16 tile is composed
TILE_SPECS = {
    "hsse": {"base": ("gtceu", "CASING_HSSE_STURDY"), "label": "HSSE Sturdy Casing"},
    "stainless": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"), "label": "Clean Stainless Steel Casing"},
    "glass": {"base": ("gtceu", "CASING_LAMINATED_GLASS"), "label": "Laminated Glass"},
    "frame_blue": {"base": ("frame", "BlueSteel"), "label": "Blue Steel Frame"},
    "frame_hsss": {"base": ("frame", "HSSS"), "label": "HSSS Frame"},
    "gearbox": {"base": ("gtceu", "CASING_TUNGSTENSTEEL_GEARBOX"), "label": "Tungstensteel Gearbox Casing"},
    "pipe": {"base": ("gtceu", "CASING_TUNGSTENSTEEL_PIPE"), "label": "Tungstensteel Pipe Casing"},
    "grate": {"base": ("gtceu", "CASING_GRATE"), "label": "Grate Casing"},
    "robust": {"base": ("gtceu", "CASING_TUNGSTENSTEEL_ROBUST"), "label": "Robust Tungstensteel Casing"},
    "restraint": {"base": ("gtna", "restraint_device.png"), "label": "Restraint Device"},
    "borosilicate": {"base": ("gtna", "casings/borosilicate_glass.png"), "label": "Borosilicate Glass"},
    "controller": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"), "overlay": "controller",
                   "label": "Controller (Integrated)"},
    "controller_adv": {"base": ("gtceu", "CASING_TUNGSTENSTEEL_ROBUST"), "overlay": "controller",
                       "label": "Controller (Advanced)"},
    "muffler": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"),
                "overlay": ("gtceu", "overlay/machine/overlay_muffler.png"), "label": "Muffler Hatch (ZPM)"},
    "bus_in": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"),
               "overlay": ("gtceu", "overlay/machine/overlay_item_hatch_input.png"), "label": "Item Import Bus"},
    "bus_out": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"),
                "overlay": ("gtceu", "overlay/machine/overlay_item_hatch_output.png"), "label": "Item Export Bus"},
    "hatch_in": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"),
                 "overlay": ("gtceu", "overlay/machine/overlay_fluid_hatch_input.png"), "label": "Fluid Input Hatch"},
    "hatch_out": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"),
                  "overlay": ("gtceu", "overlay/machine/overlay_fluid_hatch_output.png"), "label": "Fluid Output Hatch"},
    "energy": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"),
               "overlay": ("gtceu", "overlay/machine/overlay_energy_1a_in.png"), "label": "Energy Hatch"},
    "energy_adv": {"base": ("gtceu", "CASING_TUNGSTENSTEEL_ROBUST"),
                   "overlay": ("gtceu", "overlay/machine/overlay_energy_1a_in.png"), "label": "Energy Hatch"},
    "laser": {"base": ("gtceu", "CASING_TUNGSTENSTEEL_ROBUST"),
              "overlay": ("gtceu", "overlay/machine/overlay_laser_source.png"), "label": "Laser Source Hatch"},
    "parallel": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"),
                 "overlay": ("gtceu", "overlay/machine/overlay_hatch.png"), "label": "Parallel Hatch"},
    "thread": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"), "overlay": "thread", "label": "Thread Hatch"},
    "maintenance": {"base": ("gtceu", "CASING_STAINLESS_CLEAN"), "overlay": "maintenance",
                    "label": "Maintenance Hatch"},
}

HATCH_OVERLAY_FALLBACKS = {
    "energy": ["overlay/machine/overlay_energy_1a_tinted.png"],
    "laser": ["overlay/machine/overlay_laser_base.png"],
}

# GTCEu block constant -> tile key
BLOCK_TILE = {
    "CASING_HSSE_STURDY": "hsse",
    "CASING_STAINLESS_CLEAN": "stainless",
    "CASING_LAMINATED_GLASS": "glass",
    "CASING_TUNGSTENSTEEL_GEARBOX": "gearbox",
    "CASING_TUNGSTENSTEEL_PIPE": "pipe",
    "CASING_GRATE": "grate",
    "CASING_TUNGSTENSTEEL_ROBUST": "robust",
    "gtna:RESTRAINT_DEVICE": "restraint",
    "gtna:BOROSILICATE_GLASS_BLOCK": "borosilicate",
    "frame:BlueSteel": "frame_blue",
    "frame:HSSS": "frame_hsss",
}

TRANSPARENT_TILES = {"glass", "borosilicate"}


# --------------------------------------------------------------------------------------
# Pattern parsing
# --------------------------------------------------------------------------------------

@dataclass
class ParsedPattern:
    machine_id: str
    aisles: list[list[str]]
    symbols: dict[str, str] = field(default_factory=dict)
    limits: dict[str, int] = field(default_factory=dict)

    @property
    def depth(self) -> int:
        return len(self.aisles)

    @property
    def height(self) -> int:
        return len(self.aisles[0])

    @property
    def width(self) -> int:
        return len(self.aisles[0][0])


def parse_pattern(src: str, machine_id: str) -> ParsedPattern:
    anchor = f'.multiblock("{machine_id}"'
    start = src.index(anchor)
    end = src.index(".build())", start)
    block = src[start:end]

    aisles = [re.findall(r'"([^"]*)"', args) for args in re.findall(r"\.aisle\(([^)]*)\)", block, re.S)]
    assert aisles, f"no aisles parsed for {machine_id}"
    width = len(aisles[0][0])
    for aisle in aisles:
        assert all(len(row) == width for row in aisle), f"ragged rows in {machine_id}"

    symbols: dict[str, str] = {}
    chunks = re.findall(r'\.where\("(.)", (.*?)\)(?=\n\s*\.where\(|\n\s*\.build\(\))', block, re.S)
    limits: dict[str, int] = {}
    for sym, predicate in chunks:
        symbols[sym] = " ".join(predicate.split())
        m = re.search(r"setMinGlobalLimited\((\d+)\)", symbols[sym])
        if m:
            limits[sym] = int(m.group(1))
    return ParsedPattern(machine_id, aisles, symbols, limits)


def symbol_block_key(predicate: str) -> str | None:
    if "controller(" in predicate:
        return "~"
    if "MUFFLER_HATCH" in predicate:
        return "muffler"
    m = re.search(r"GTBlocks\.(\w+)\.get\(\)", predicate)
    if m:
        return m.group(1)
    m = re.search(r"GTNABlocks\.(\w+)\.get\(\)", predicate)
    if m:
        return f"gtna:{m.group(1)}"
    m = re.search(r"ChemicalHelper\.getBlock\(TagPrefix\.frameGt, GTMaterials\.(\w+)\)", predicate)
    if m:
        return f"frame:{m.group(1)}"
    return None


# --------------------------------------------------------------------------------------
# Texture composition
# --------------------------------------------------------------------------------------

def gtceu_tex_dir() -> Path:
    for candidate in GTCEU_CANDIDATES:
        tex = candidate / "src/main/resources/assets/gtceu/textures/block"
        if tex.is_dir():
            return tex
    raise SystemExit("GTCEu checkout not found; update GTCEU_CANDIDATES")


def tint(image: Image.Image, color: int) -> Image.Image:
    r, g, b = (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF
    scale = 255 / max(r, g, b)
    tr, tg, tb = r * scale, g * scale, b * scale
    src, out = image.load(), image.copy()
    dst = out.load()
    for y in range(image.height):
        for x in range(image.width):
            pr, pg, pb, pa = src[x, y]
            dst[x, y] = (min(255, int(pr * tr / 255)), min(255, int(pg * tg / 255)),
                         min(255, int(pb * tb / 255)), pa)
    return out


def _load(path: Path) -> Image.Image:
    image = Image.open(path).convert("RGBA")
    if image.height > 16:
        image = image.crop((0, 0, 16, 16))
    return image


def controller_overlay() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            if 2 <= x <= 13 and 2 <= y <= 13:
                if x in (2, 13) or y in (2, 13):
                    px[x, y] = (255, 186, 66, 255)
                else:
                    px[x, y] = (26, 28, 34, 220)
    glyph = [".####.", "#.##.#", "######", "######", "#.##.#", ".####."]
    for gy, row in enumerate(glyph):
        for gx, ch in enumerate(row):
            if ch == "#":
                px[5 + gx, 5 + gy] = (255, 214, 140, 255)
    return img


def hatch_overlay() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            if 3 <= x <= 12 and 3 <= y <= 12:
                if x in (3, 12) or y in (3, 12):
                    px[x, y] = (198, 226, 255, 235)
                elif 5 <= x <= 10 and 5 <= y <= 10:
                    px[x, y] = (120, 180, 255, 190)
    return img


def compose_tile(spec: dict, tex_dir: Path) -> Image.Image:
    kind, ref = spec["base"]
    if kind == "gtceu":
        base = _load(tex_dir / GTCEU_TEX[ref])
    elif kind == "gtna":
        base = _load(GTNA_TEX_DIR / ref)
    elif kind == "frame":
        base = tint(_load(tex_dir / FRAME_TEX), FRAME_COLORS[ref])
    else:
        raise ValueError(kind)

    overlay = spec.get("overlay")
    over = None
    if overlay == "controller":
        over = controller_overlay()
    elif overlay in ("thread", "maintenance"):
        over = hatch_overlay()
    elif isinstance(overlay, tuple):
        path = tex_dir / overlay[1]
        if not path.exists():
            for fallback in HATCH_OVERLAY_FALLBACKS.get(spec.get("key", ""), []):
                if (tex_dir / fallback).exists():
                    path = tex_dir / fallback
                    break
        over = _load(path)
    if over is not None:
        base = Image.alpha_composite(base, over)
    return base


def data_uri(image: Image.Image) -> str:
    buf = io.BytesIO()
    image.save(buf, format="PNG")
    return "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()


# --------------------------------------------------------------------------------------
# Structures
# --------------------------------------------------------------------------------------

def build_structure(pattern: ParsedPattern, tile_for_symbol: dict[str, str | None]) -> list[dict]:
    """Voxels in controller-relative coordinates.

    GTCEu's ``FactoryBlockPattern.start()`` uses (charDir=LEFT, stringDir=UP, aisleDir=FRONT):
    the char index grows to the controller's left, the row index grows up and the aisle index
    grows toward the controller's front.  Relative to the controller::

        X = -(char - controllerChar)   Y = row - controllerRow   Z = -(aisle - controllerAisle)
    """
    ctrl = None
    for a, aisle in enumerate(pattern.aisles):
        for r, row in enumerate(aisle):
            c = row.find("~")
            if c >= 0:
                ctrl = (a, r, c)
    assert ctrl is not None, f"no controller in {pattern.machine_id}"
    ca, cr, cc = ctrl

    voxels = []
    for a, aisle in enumerate(pattern.aisles):
        for r, row in enumerate(aisle):
            for c, sym in enumerate(row):
                if sym == " ":
                    continue
                tile = tile_for_symbol.get(sym)
                if tile is None:
                    continue
                voxels.append({
                    "p": [cc - c, r - cr, ca - a],
                    "t": tile,
                    "part": bool(pattern.symbols.get(sym, "").find("abilities(") >= 0),
                })
    return voxels


def bbox(voxels: list[dict]) -> dict:
    return {
        "min": [min(v["p"][i] for v in voxels) for i in range(3)],
        "max": [max(v["p"][i] for v in voxels) for i in range(3)],
    }


def row_extents(voxels: list[dict]) -> dict[int, tuple[int, int, int, int]]:
    """y -> (min_x, max_x, min_z, max_z) of the machine itself (modules excluded)."""
    out: dict[int, tuple[int, int, int, int]] = {}
    for y in {v["p"][1] for v in voxels}:
        cells = [v["p"] for v in voxels if v["p"][1] == y]
        out[y] = (min(c[0] for c in cells), max(c[0] for c in cells),
                  min(c[2] for c in cells), max(c[2] for c in cells))
    return out


# --------------------------------------------------------------------------------------
# Proposed auxiliary modules (design proposal -- not implemented in the mod)
# --------------------------------------------------------------------------------------

MODULES: list[dict] = [
    {
        "id": "intake_column",
        "machine": "integrated",
        "name": "Auxiliary Intake Column",
        "accent": "#54d0ff",
        "attach": "West face, beside the controller",
        "size": "1 x 5 x 1",
        "bonus_short": "+1 Import Bus, +1 Fluid Input Hatch",
        "bonus": [
            "Adds one Item Import Bus and one Fluid Input Hatch on the west face, so ores and the "
            "wash fluid can be fed from both sides of the machine.",
            "Doubles the throughput of the feeding line without touching the main structure.",
        ],
        "impl": "Structure only: sub-pattern with IMPORT_ITEMS + IMPORT_FLUIDS, both "
                "setMaxGlobalLimited(2) so the base keeps its own hatches.",
        "status": "Estrutura apenas",
        "blocks": lambda ctx: [
            [[ctx["rows"][y][0] - 1, y, 0], tile]
            for y, tile in ((-2, "stainless"), (-1, "bus_in"), (0, "bus_in"),
                            (1, "hatch_in"), (2, "stainless"))
        ],
    },
    {
        "id": "grinding_heads",
        "machine": "integrated",
        "name": "Grinding Head Array",
        "accent": "#ffb347",
        "attach": "East face of the lower hall",
        "size": "3 x 3 x 3",
        "bonus_short": "-20% duration, +1 Parallel Hatch",
        "bonus": [
            "A 3x3x3 annex of gearbox casings drives three extra grinding heads: recipes run 20% "
            "faster while the module is formed.",
            "The annex also exposes one additional Parallel Hatch, so the factory can double its "
            "parallel count (the base allows a single one).",
        ],
        "impl": "Needs a small hook: recipe modifier duration *0.8 while the module is formed, plus "
                "a second PARALLEL_HATCH slot in the module pattern.",
        "status": "Precisa modifier",
        "blocks": lambda ctx: [
            [[ctx["rows"][y][1] + 1 + layer, y, z],
             "parallel" if (layer, y, z) == (1, -1, 1) else "gearbox"]
            for y in range(-2, 1) for layer in range(3) for z in range(3)
        ],
    },
    {
        "id": "wash_tank",
        "machine": "integrated",
        "name": "Wash Recovery Tank",
        "accent": "#5ce1a6",
        "attach": "Back face (+Z)",
        "size": "5 x 3 x 3",
        "bonus_short": "32 B wash buffer, -25% fluid cost, +1 Fluid Output Hatch",
        "bonus": [
            "Stores 32 buckets of the recipe wash fluid (distilled water, mercury, sodium "
            "persulfate...) and refills the machine from that buffer.",
            "The wash step consumes 25% less fluid and the tank vents one extra Fluid Output "
            "Hatch for the chemical bath circuits.",
        ],
        "impl": "Needs a small hook: an internal tank on the controller plus a fluid-cost multiplier "
                "on the wash input while the module is formed.",
        "status": "Precisa modifier",
        "blocks": lambda ctx: [
            [[3 + x, y, ctx["rows"][y][3] + 1 + layer],
             "hatch_out" if (x, y, layer) == (2, 0, 1) else "stainless"]
            for x in range(5) for y in range(-2, 1) for layer in range(3)
        ],
    },
    {
        "id": "sorting_rack",
        "machine": "integrated",
        "name": "Byproduct Sorting Rack",
        "accent": "#c792ea",
        "attach": "Roof of the east annex",
        "size": "3 x 2 x 3",
        "bonus_short": "+2 Export Buses, +10% chanced byproducts",
        "bonus": [
            "Two extra Item Export Buses collect the 9 byproduct outputs of every circuit, so the "
            "main bus never clogs.",
            "Re-sorts the chanced byproducts (the 1/3 and 1/9 drops): their chance is raised by 10%.",
        ],
        "impl": "Needs a small hook: an output-chance modifier on some-chance outputs. Stack it on "
                "the Grinding Head Array for the full east wing.",
        "status": "Precisa modifier",
        "blocks": lambda ctx: [
            [[ctx["rows"][y][1] + 1 + layer, 1 + y, z],
             "bus_out" if (layer, y, z) in ((0, 0, 1), (2, 1, 1)) else "grate"]
            for layer in range(3) for y in range(2) for z in range(3)
        ],
    },
    {
        "id": "laser_array",
        "machine": "advanced",
        "name": "Laser Array Expansion",
        "accent": "#ff5c7a",
        "attach": "West face, beside the controller",
        "size": "5 x 3 x 5",
        "bonus_short": "+2 Laser Hatches, +25% laser conversion",
        "bonus": [
            "Two more Laser Source Hatches feed the tower, so three separate laser lines can keep "
            "the unlimited parallel running at full speed.",
            "Laser energy converts 25% more efficiently while the module is formed.",
        ],
        "impl": "Hatches are structure-only (INPUT_LASER x2). The conversion bonus needs a recipe "
                "modifier reading the laser energy input.",
        "status": "Estrutura + modifier",
        "blocks": lambda ctx: [
            [[ctx["rows"][y][0] - 1 - layer, y, z],
             "laser" if (layer, y, z) in ((0, 0, 2), (2, -1, 2)) else "hsse"]
            for y in range(-2, 1) for layer in range(5) for z in range(5)
        ],
    },
    {
        "id": "parallel_governor",
        "machine": "advanced",
        "name": "Parallel Governor",
        "accent": "#ffd166",
        "attach": "Front-east corner, beside the controller",
        "size": "3 x 3 x 3",
        "bonus_short": "Parallel cap in the GUI: 512 / 1024 / 2048 / MAX",
        "bonus": [
            "The tower parallel is effectively unlimited; the governor lets the player cap it from "
            "the machine UI instead of editing configs.",
            "Spreads a craft evenly over the available recipe slots, keeping TPS stable in big packs.",
        ],
        "impl": "Needs code: a GUI option plus a getMaxParallel() override capped by the module.",
        "status": "Precisa codigo",
        "blocks": lambda ctx: [
            [[2 + layer, y, z], "thread" if (layer, y, z) == (1, 0, 1) else "robust"]
            for layer in range(3) for y in range(-2, 1) for z in range(3)
        ],
    },
    {
        "id": "reagent_complex",
        "machine": "advanced",
        "name": "Reagent Complex",
        "accent": "#5ce1a6",
        "attach": "Far back (+Z)",
        "size": "5 x 3 x 4",
        "bonus_short": "+15% yield on circuits 5-7, +1 Fluid Hatch",
        "bonus": [
            "Doses the chemical bath circuits (5, 6 and 7) with a richer reagent mix: their "
            "byproduct yield grows 15%.",
            "Adds one Fluid Input Hatch and one Fluid Output Hatch for the reagent loop.",
        ],
        "impl": "Needs a hook: yield modifier for recipes whose circuit is 5-7, plus the two hatch "
                "abilities in the structure.",
        "status": "Estrutura + modifier",
        "blocks": lambda ctx: [
            [[-9 + x, y, ctx["rows"][y][3] + 1 + layer],
             "hatch_in" if (x, y, layer) == (0, 0, 0) else
             ("hatch_out" if (x, y, layer) == (4, 0, 3) else "robust")]
            for x in range(5) for y in range(-2, 1) for layer in range(4)
        ],
    },
    {
        "id": "nexus_uplink",
        "machine": "advanced",
        "name": "Nexus Uplink",
        "accent": "#8ab4ff",
        "attach": "Roof",
        "size": "3 x 2 x 3",
        "bonus_short": "Wireless AE2 / Terminal Nexus link",
        "bonus": [
            "Links the tower to the Terminal Nexus: ore is pulled from the ME network and the "
            "products pushed back, so no import/export buses are needed.",
            "The Nexus can auto-build the tower with 'Module Build = N', modules included.",
        ],
        "impl": "Needs code: reuse the GTNA wireless / nexus link used by the terminal.",
        "status": "Precisa codigo",
        "blocks": lambda ctx: [
            [[-9 + x, ctx["bb"]["max"][1] + 1 + y, 20 + z],
             "parallel" if (x, y, z) == (1, 0, 1) else "restraint"]
            for x in range(3) for y in range(2) for z in range(3)
        ],
    },
]

# proposed hatches/buses drawn on the base pattern (so the render looks like a real build)
PROPOSED_PARTS = {
    "integrated": [
        ("parallel", {"front": 0, "east": 1}),
        ("energy", {"front": 2, "east": 0}),
        ("bus_in", {"front": 1, "east": 0}),
        ("bus_out", {"front": 2, "east": 0}),
        ("hatch_in", {"front": 1, "east": 0}),
        ("maintenance", {"front": 1, "east": 0}),
    ],
    "advanced": [
        ("laser", {"front": 0, "west": 1}),
        ("thread", {"front": 1, "east": 0}),
        ("bus_in", {"front": 2, "east": 0}),
        ("bus_out", {"front": 2, "east": 0}),
        ("hatch_in", {"front": 1, "east": 0}),
    ],
}

CIRCUITS = [
    {"n": 1, "chain": ["Grinding", "Grinding", "Centrifuge"], "fluid": "none", "needs": "always"},
    {"n": 2, "chain": ["Grinding", "Ore Wash", "Thermal Centrifuge", "Grinding"],
     "fluid": "distilled water", "needs": "crushedRefined input"},
    {"n": 3, "chain": ["Grinding", "Ore Wash", "Grinding", "Centrifuge"],
     "fluid": "distilled water", "needs": "always"},
    {"n": 4, "chain": ["Grinding", "Ore Wash", "Sifting", "Centrifuge"],
     "fluid": "distilled water", "needs": "gem input"},
    {"n": 5, "chain": ["Grinding", "Chemical Bath", "Thermal Centrifuge", "Grinding"],
     "fluid": "mercury / sodium persulfate", "needs": "crushedRefined + washedIn"},
    {"n": 6, "chain": ["Grinding", "Chemical Bath", "Grinding", "Centrifuge"],
     "fluid": "mercury / sodium persulfate", "needs": "washedIn"},
    {"n": 7, "chain": ["Grinding", "Chemical Bath", "Sifting", "Centrifuge"],
     "fluid": "mercury / sodium persulfate", "needs": "gem + washedIn"},
]

SNIPPET_PREDICATES = {
    "stainless": "Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())",
    "robust": "Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())",
    "hsse": "Predicates.blocks(GTBlocks.CASING_HSSE_STURDY.get())",
    "gearbox": "Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get())",
    "grate": "Predicates.blocks(GTBlocks.CASING_GRATE.get())",
    "restraint": "Predicates.blocks(GTNABlocks.RESTRAINT_DEVICE.get())",
    "bus_in": "Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())\n    "
              ".or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2))",
    "bus_out": "Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())\n    "
               ".or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1))",
    "hatch_in": "Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())\n    "
                ".or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2))",
    "hatch_out": "Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())\n    "
                 ".or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2))",
    "energy": "Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())\n    "
              ".or(Predicates.abilities(PartAbility.INPUT_ENERGY))",
    "laser": "Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())\n    "
             ".or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(3))",
    "parallel": "Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())\n    "
                ".or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))",
    "thread": "Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())\n    "
              ".or(Predicates.abilities(GTNAPartAbility.THREAD_HATCH).setMaxGlobalLimited(1))",
}
SNIPPET_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"


def module_snippet(module: dict) -> str:
    """Emit the KubeJS sub-pattern that would build this module.

    The controller symbol is placed at (char=maxX, row=-minY, aisle=maxZ) so every index is >= 0.
    Rows are emitted bottom-first, exactly like the mod's patterns.
    """
    blocks = module["_blocks"]
    max_x = max(p[0] for p, _ in blocks)
    min_x = min(p[0] for p, _ in blocks)
    min_y = min(p[1] for p, _ in blocks)
    max_y = max(p[1] for p, _ in blocks)
    min_z = min(p[2] for p, _ in blocks)
    max_z = max(p[2] for p, _ in blocks)

    char_c, row_c, aisle_c = max(max_x, 0), -min(min_y, 0), max(max_z, 0)
    chars = char_c - min(min_x, 0) + 1
    rows = row_c + max(max_y, 0) + 1
    aisles = aisle_c - min(min_z, 0) + 1

    cells: dict[tuple[int, int, int], str] = {(aisle_c, row_c, char_c): "~"}
    for (x, y, z), tile in blocks:
        cells[(aisle_c - z, row_c + y, char_c - x)] = tile

    tiles = sorted({tile for tile in cells.values() if tile != "~"})
    symbol_of = {tile: SNIPPET_SYMBOLS[i] for i, tile in enumerate(tiles)}

    aisle_lines = []
    for aisle in range(aisles):
        row_strings = []
        for row in range(rows):
            row_strings.append("".join(
                "~" if cells.get((aisle, row, char)) == "~"
                else symbol_of.get(cells.get((aisle, row, char), " "), " ")
                for char in range(chars)))
        aisle_lines.append("    .aisle(" + ", ".join(f"'{row}'" for row in row_strings) + ")")

    where_lines = ["    .where('~', Predicates.controller(Predicates.blocks(definition.get())))"]
    for tile in tiles:
        where_lines.append(f"    .where('{symbol_of[tile]}', "
                           + SNIPPET_PREDICATES.get(tile, "/* TODO */").replace("\n    ", "\n        ") + ")")

    machine = module["machine"]
    return "\n".join([
        f"// {module['name']} - {module['attach']} ({module['size']})",
        f"GTNAServerEvents.subPatterns(event => {{",
        f"  event.add('gtna:{'integrated' if machine == 'integrated' else 'advanced_integrated'}_ore_processor',",
        "    definition => FactoryBlockPattern.start()",
        *aisle_lines,
        *where_lines,
        "    .build(), 'gtna.machine.auxiliary_module." + module["id"] + "')",
        "})",
    ])


# --------------------------------------------------------------------------------------
# Assembly
# --------------------------------------------------------------------------------------

def place_proposed_parts(structure: dict, library: list[tuple[str, dict]]) -> None:
    """Paint proposed hatches on exposed, part-capable cells of the base pattern.

    A face only counts when it points away from the machine centre (so the parts land on the
    outer shell, not inside the chimney), and the cells are taken bottom-up so the hatches sit
    next to the controller where a player would build them.
    """
    voxels = structure["voxels"]
    occupied = {tuple(v["p"]): v for v in voxels}
    centre = [(min(v["p"][i] for v in voxels) + max(v["p"][i] for v in voxels)) / 2 for i in range(3)]
    used: set[tuple[int, int, int]] = set()

    def outward_faces(pos):
        faces = []
        for axis, sign, name in ((0, 1, "east"), (0, -1, "west"), (2, -1, "front"), (2, 1, "back")):
            nb = list(pos)
            nb[axis] += sign
            if tuple(nb) in occupied:
                continue
            if sign * (pos[axis] - centre[axis]) > 0:
                faces.append(name)
        return faces

    candidates = []
    for voxel in voxels:
        if not voxel.get("part") or "m" in voxel:
            continue
        if voxel["t"] not in ("stainless", "robust"):
            continue
        for face in outward_faces(voxel["p"]):
            candidates.append((face, tuple(voxel["p"]), voxel))
    candidates.sort(key=lambda c: (c[1][1], c[1][0], c[1][2]))

    for tile, wants in library:
        for axis, count in wants.items():
            picks = [c for c in candidates if c[0] == axis and c[1] not in used]
            for _, pos, voxel in picks[:count]:
                voxel["t"] = tile
                voxel["prop"] = True
                used.add(pos)


def dump_structure(key: str, structure: dict) -> None:
    """ASCII slices (X to the right, Z down) so the geometry and the modules can be eyeballed."""
    voxels = structure["voxels"]
    xs = [v["p"][0] for v in voxels]
    ys = [v["p"][1] for v in voxels]
    zs = [v["p"][2] for v in voxels]
    min_x, min_y, min_z = min(xs), min(ys), min(zs)
    index = {(v["p"][0], v["p"][1], v["p"][2]): v for v in voxels}
    print(f"=== {key}: X[{min_x},{max(xs)}] Y[{min_y},{max(ys)}] Z[{min_z},{max(zs)}]")
    for y in range(max(ys), min_y - 1, -1):
        print(f"-- y = {y}")
        for z in range(min_z, max(zs) + 1):
            row = []
            for x in range(min_x, max(xs) + 1):
                voxel = index.get((x, y, z))
                if voxel is None:
                    row.append(" ")
                elif voxel.get("m"):
                    row.append("#")
                elif voxel.get("prop"):
                    row.append("*")
                elif voxel["t"] == "controller" or voxel["t"] == "controller_adv":
                    row.append("@")
                elif voxel["t"] in TRANSPARENT_TILES:
                    row.append("o")
                else:
                    row.append("x")
            print("   " + "".join(row))


def build_payload(check_only: bool = False) -> dict:
    src = JAVA_SRC.read_text()
    tex_dir = gtceu_tex_dir()

    parsed = {
        "integrated": parse_pattern(src, "integrated_ore_processor"),
        "advanced": parse_pattern(src, "advanced_integrated_ore_processor"),
    }

    structures: dict[str, dict] = {}
    for key, pattern in parsed.items():
        controller_tile = f"controller{'_adv' if key == 'advanced' else ''}"
        tile_for_symbol: dict[str, str | None] = {}
        for sym, predicate in pattern.symbols.items():
            block = symbol_block_key(predicate)
            if block is None:
                tile_for_symbol[sym] = None
            elif block == "~":
                tile_for_symbol[sym] = controller_tile
            elif block == "muffler":
                tile_for_symbol[sym] = "muffler"
            elif block in BLOCK_TILE:
                tile_for_symbol[sym] = BLOCK_TILE[block]
            else:
                raise SystemExit(f"unmapped block {block!r} for symbol {sym!r} in {pattern.machine_id}")
        voxels = build_structure(pattern, tile_for_symbol)
        structures[key] = {
            "id": key,
            "voxels": voxels,
            "dims": [pattern.width, pattern.height, pattern.depth],
            "stainless_min": pattern.limits.get("c"),
            "counts": {},
        }
        counts: dict[str, int] = {}
        for voxel in voxels:
            counts[voxel["t"]] = counts.get(voxel["t"], 0) + 1
        structures[key]["counts"] = counts
        if check_only:
            print(f"[{key}] {pattern.width}x{pattern.height}x{pattern.depth} voxels={len(voxels)} "
                  f"stainless_min={pattern.limits.get('c')}")
            for tile, count in sorted(counts.items(), key=lambda kv: -kv[1]):
                print(f"    {tile:<14} {count}")

    for key, structure in structures.items():
        place_proposed_parts(structure, PROPOSED_PARTS[key])

    base_voxels = {key: list(structure["voxels"]) for key, structure in structures.items()}
    modules = []
    problems: list[str] = []
    for module in MODULES:
        key = module["machine"]
        base = base_voxels[key]
        ctx = {"bb": bbox(base), "rows": row_extents(base)}
        blocks = [(tuple(p), tile) for p, tile in module["blocks"](ctx)]
        occupied = {tuple(v["p"]) for v in base}
        module_pos = {pos for pos, _ in blocks}
        for pos, _ in blocks:
            if pos in occupied:
                problems.append(f"{module['id']}: collision at {pos}")
        for pos, _ in blocks:
            solid = occupied | module_pos
            if not any(tuple(pos[i] + (sign if j == i else 0) for i in range(3)) in solid
                       for j in range(3) for sign in (-1, 1)):
                problems.append(f"{module['id']}: block {pos} does not touch the machine")
        module = dict(module)
        module["_blocks"] = blocks
        module["_ctx"] = ctx
        modules.append(module)

    for module in modules:
        structure = structures[module["machine"]]
        for (x, y, z), tile in module["_blocks"]:
            structure["voxels"].append({"p": [x, y, z], "t": tile, "m": module["id"],
                                        "part": tile not in TRANSPARENT_TILES})
        module["snippet"] = module_snippet(module)

    if problems:
        print("module warnings:")
        for problem in problems:
            print("   ", problem)

    payload = {
        "structures": structures,
        "modules": [
            {k: v for k, v in module.items() if k not in ("_blocks", "_bbox", "blocks")}
            for module in modules
        ],
        "circuits": CIRCUITS,
        "meta": {
            "integrated": {
                "name": "Integrated Ore Processor",
                "machine_id": "gtna:integrated_ore_processor",
                "era": "EV",
                "size": "11 wide x 12 tall x 6 deep",
                "source": "GTLCore port (LGPLv3)",
                "lines": [
                    "Main Function: Complete ore processing in one step.",
                    "Available circuits: 1 grinding-grinding-centrifuge ... 7 grinding-chemical bath-sifting-centrifuge.",
                    "Accepts Parallel Hatch, Thread / Overclock / Accelerate and Maintenance.",
                    "Recipe map: gtna:ore_processing (2 item in / 9 item out / 1 fluid in, 30 EU/t).",
                ],
            },
            "advanced": {
                "name": "Advanced Integrated Ore Processor",
                "machine_id": "gtna:advanced_integrated_ore_processor",
                "era": "Endgame (laser)",
                "size": "15 wide x 12 tall x 32 deep",
                "source": "GTLCore / TST port (LGPLv3)",
                "lines": [
                    "Main Function: Max Parallel: 2147483647",
                    "Only accepts Laser Energy Hatches.",
                    "Processes multiple different recipes at the same time.",
                    "Same gtna:ore_processing recipe map.",
                ],
            },
        },
    }

    if not check_only:
        tiles = {}
        for key, spec in TILE_SPECS.items():
            spec = dict(spec, key=key)
            tiles[key] = data_uri(compose_tile(spec, tex_dir))
        payload["tiles"] = tiles
        payload["transparent"] = sorted(TRANSPARENT_TILES)
        payload["legend"] = [{"tile": key, "label": spec["label"]} for key, spec in TILE_SPECS.items()]
        payload["logo"] = data_uri(_load(LOGO).resize((96, 96), Image.LANCZOS))
    return payload


def write_html(payload: dict) -> Path:
    template = TEMPLATE.read_text()
    html = template.replace("/*__GTNA_DATA__*/", json.dumps(payload, separators=(",", ":")))
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    out = OUT_DIR / "gtna-ore-processing-preview.html"
    out.write_text(html)
    return out


SHOTS = [
    ("01-integrated-overview", "hero", "integrated", "modules=0&yaw=-0.62&pitch=0.44",
     "Integrated Ore Processor", "11 x 12 x 6 - port do GTLCore, circuitos EV, 30 EU/t"),
    ("02-integrated-modules", "hero", "integrated", "modules=1&yaw=-0.62&pitch=0.44",
     "Integrated + modulos (ala leste)", "Grinding Head Array, Byproduct Sorting Rack e o controller na frente"),
    ("03-integrated-modules-west", "hero", "integrated", "modules=1&yaw=2.30&pitch=0.45",
     "Integrated + modulos (ala oeste)", "Auxiliary Intake Column na face oeste e Wash Recovery Tank na traseira"),
    ("04-advanced-overview", "hero", "advanced", "modules=0&yaw=-0.62&pitch=0.42&fit=0.80&oy=0.50",
     "Advanced Integrated Ore Processor", "15 x 12 x 32 - laser, parallel ilimitado, casings GTNA"),
    ("05-advanced-modules", "hero", "advanced", "modules=1&yaw=0.62&pitch=0.42&fit=0.72&oy=0.50",
     "Advanced + modulos endgame", "Laser Array, Nexus Uplink, Reagent Complex e Parallel Governor"),
    ("06-circuits", "circuits", "integrated", "",
     "Circuitos 1-7", "byproducts reais por estagio e fluido de lavagem"),
    ("07-modules-board", "modules", "integrated", "",
     "Modulos propostos", "8 modulos auxiliares com bonus e status de implementacao"),
]


def render_gallery(html_path: Path, only: list[str] | None = None) -> None:
    chromium = shutil.which("chromium") or shutil.which("chromium-browser") or shutil.which("google-chrome")
    if chromium is None:
        raise SystemExit("chromium not found; skipping gallery")
    GALLERY_DIR.mkdir(parents=True, exist_ok=True)
    for slug, shot, machine, flags, title, subtitle in SHOTS:
        if only and slug not in only:
            continue
        out = GALLERY_DIR / f"{slug}.png"
        extra = f"&{flags}" if flags else ""
        url = (f"file://{html_path}?shot={shot}&machine={machine}{extra}"
               f"&title={title.replace(' ', '%20')}&subtitle={subtitle.replace(' ', '%20')}")
        cmd = [chromium, "--headless=new", "--no-sandbox", "--disable-gpu", "--hide-scrollbars",
               "--force-device-scale-factor=1", "--window-size=1920,1080",
               "--virtual-time-budget=9000", f"--screenshot={out}", url]
        print("  ->", slug)
        subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    print(f"gallery written to {GALLERY_DIR}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--dump", action="store_true")
    parser.add_argument("--html", action="store_true")
    parser.add_argument("--gallery", action="store_true")
    parser.add_argument("--only", nargs="*", default=None)
    args = parser.parse_args()

    check_only = args.check and not args.html
    payload = build_payload(check_only=check_only)
    if args.dump:
        for key, structure in payload["structures"].items():
            dump_structure(key, structure)
    if check_only:
        print("payload ok")
        return
    html_path = write_html(payload)
    print(f"html written to {html_path}")
    if args.gallery:
        render_gallery(html_path, args.only)


if __name__ == "__main__":
    sys.exit(main())
