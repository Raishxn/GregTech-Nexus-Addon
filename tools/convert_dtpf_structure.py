"""Convert the GTNH Dimensionally Transcendent Plasma Forge StructureLib shape to a GTNA .mbs pattern.

Usage: python3 tools/convert_dtpf_structure.py <GT5-Unofficial checkout>

Source: GT5-Unofficial master (2026-10-07), gregtech/common/tileentities/machines/multi/MTEPlasmaForge.java
(LGPL-3.0). The shape is [depth][row from top][column] with the controller at (16, 21, 16); GTCEu's
FactoryBlockPattern.start() reads aisles toward the front and rows bottom-up, so slices and rows are reversed.
Letters: N Dimensionally Transcendent Casing -> A, b Dimensional Injection Casing (hatches) -> B,
s Dimensional Bridge Casing -> D, C heating coil -> C, controller -> ~.
"""

from pathlib import Path
import re
import struct
import sys

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/pattern/dtpf_gtnh.mbs"
LETTERS = {"N": "A", "b": "B", "s": "D", "C": "C", " ": " ", "~": "~"}
CODES = {"C": 0, "B": 1, "A": 2, "D": 7, " ": 19, "~": 61}
CONTROLLER = (16, 21, 16)


def main():
    src = (Path(sys.argv[1]) / "src/main/java/gregtech/common/tileentities/machines/multi/MTEPlasmaForge.java")
    text = src.read_text()
    body = re.search(r"structure_string = new String\[\]\[\] \{(.*?)\} \};", text, re.S).group(1)
    slices = [re.findall(r'"([^"]*)"', s) for s in re.split(r"\}\s*,\s*\{", body + "}")]
    frame = [[[LETTERS[ch] for ch in row] for row in rows] for rows in slices]
    x, y, z = CONTROLLER
    if frame[z][y][x] != "~":
        raise ValueError(f"controller cell is {frame[z][y][x]!r}, expected '~'")
    data = [list(reversed(["".join(row) for row in rows])) for rows in reversed(frame)]
    with OUT.open("wb") as out:
        out.write(struct.pack(">i", len(data)))
        for rows in data:
            out.write(struct.pack(">i", len(rows)))
            for row in rows:
                out.write(struct.pack(">i", len(row)))
                out.write(bytes(CODES[ch] for ch in row))
    counts = {k: sum(row.count(k) for rows in data for row in rows) for k in "ABCD~"}
    print(f"wrote {OUT.relative_to(ROOT)}: {len(data)} aisles x {len(data[0])} rows x {len(data[0][0])}, {counts}")


if __name__ == "__main__":
    main()
