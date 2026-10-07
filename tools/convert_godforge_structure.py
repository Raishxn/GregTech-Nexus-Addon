"""Convert the GTNH Forge of Gods StructureLib strings to GTNA .mbs patterns.

Usage: python3 tools/convert_godforge_structure.py <GT5-Unofficial checkout>

Source: GT5-Unofficial a3e1e11241a814c9fa0dd0973d5699548428f689 (LGPL-3.0),
tectech/thing/metaTileEntity/multi/godforge/structure/*.java.

StructureLib shapes are [depth][row from top][column], with the controller at
(63, 14, 1) for the main piece, (55, 11, -67) for ring 2 and (47, 13, -76) for
ring 3. GTCEu's FactoryBlockPattern.start() reads aisles toward the front and
rows bottom-up, so slices and rows are reversed. Rings 2 and 3 are written into
the full main frame so all three patterns share the controller position.
"""

from pathlib import Path
import re
import struct
import sys

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/pattern"
CODES = {"C": 0, "B": 1, "A": 2, "J": 3, "G": 4, "F": 5, "E": 6, "D": 7, "K": 8, "L": 9,
         "I": 10, "H": 11, " ": 19, "~": 61}


def parse(path):
    src = path.read_text()
    shapes = {}
    for match in re.finditer(r"public static final String\[\]\[\] (\w+) = \{(.*?)\} \};", src, re.S):
        slices = re.split(r"\}\s*,\s*\{", match.group(2) + "}")
        shapes[match.group(1)] = [re.findall(r'"([^"]*)"', s) for s in slices]
    return shapes


def place(frame, shape, ctrl, main_ctrl):
    cx, cy, cz = ctrl
    mx, my, mz = main_ctrl
    for k, rows in enumerate(shape):
        z = k - cz + mz
        for r, row in enumerate(rows):
            y = r - cy + my
            for c, ch in enumerate(row):
                if ch == " ":
                    continue
                x = c - cx + mx
                if not (0 <= z < len(frame) and 0 <= y < len(frame[0]) and 0 <= x < len(frame[0][0])):
                    raise ValueError(f"block outside the main frame: {(x, y, z)}")
                frame[z][y][x] = ch


def air(shape):
    """GTNH replaceLetters(shape, "L")."""
    return [["".join(" " if ch == " " else "L" for ch in row) for row in rows] for rows in shape]


def write(name, frame):
    data = [[list(reversed(["".join(row) for row in slice_]))] for slice_ in reversed(frame)]
    path = OUT / f"{name}.mbs"
    with path.open("wb") as out:
        out.write(struct.pack(">i", len(data)))
        for (rows,) in data:
            out.write(struct.pack(">i", len(rows)))
            for row in rows:
                out.write(struct.pack(">i", len(row)))
                out.write(bytes(CODES[ch] for ch in row))
    print(f"wrote {path.relative_to(ROOT)} ({path.stat().st_size} bytes)")


def main():
    base = Path(sys.argv[1]) / "src/main/java/tectech/thing/metaTileEntity/multi/godforge/structure"
    shapes = parse(base / "ForgeOfGodsStructureString.java")
    shapes.update(parse(base / "ForgeOfGodsRingsStructureString.java"))
    main_shape = shapes["BEAM_SHAFT"] + shapes["FIRST_RING"]
    depth, height, width = len(main_shape), len(main_shape[0]), len(main_shape[0][0])
    main_ctrl = (63, 14, 1)
    if main_shape[1][14][63] != "~":
        raise ValueError("controller marker not at (63, 14, 1)")

    def empty():
        return [[[" "] * width for _ in range(height)] for _ in range(depth)]

    main_frame = empty()
    place(main_frame, main_shape, main_ctrl, main_ctrl)
    write("god_forge", main_frame)

    # While the star is rendered the ring blocks are taken into the controller (GTNH *_RING_AIR pieces).
    active_frame = empty()
    place(active_frame, shapes["BEAM_SHAFT"], main_ctrl, main_ctrl)
    place(active_frame, air(shapes["FIRST_RING"]), (63, 14, -59), main_ctrl)
    write("god_forge_active", active_frame)
    ring_frame = empty()
    place(ring_frame, shapes["FIRST_RING"], (63, 14, -59), main_ctrl)
    ring_frame[main_ctrl[2]][main_ctrl[1]][main_ctrl[0]] = "~"
    write("god_forge_ring_1", ring_frame)
    for name, shape, ctrl in (("god_forge_ring_2", shapes["SECOND_RING"], (55, 11, -67)),
                              ("god_forge_ring_3", shapes["THIRD_RING"], (47, 13, -76))):
        frame = empty()
        place(frame, shape, ctrl, main_ctrl)
        frame[main_ctrl[2]][main_ctrl[1]][main_ctrl[0]] = "~"
        write(name, frame)
        air_frame = empty()
        place(air_frame, air(shape), ctrl, main_ctrl)
        air_frame[main_ctrl[2]][main_ctrl[1]][main_ctrl[0]] = "~"
        write(name + "_air", air_frame)


if __name__ == "__main__":
    main()
