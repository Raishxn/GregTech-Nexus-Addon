"""Build the Nexus ME Hypercore .mbs from the author's packet.txt export.

The export has no controller or CPU Interface markers. Both replace adjacent
Laminated Glass blocks in the center of the glass cylinder's final face.
"""

from pathlib import Path
import re
import struct


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/structures/nexus_me_hypercore_source.txt"
TARGET = ROOT / "src/main/resources/pattern/nexus_me_hypercore.mbs"
# The GTNAMultiBlockFileReader uses these byte values for its uppercase symbols.
SYMBOLS = {
    "a": 19, "b": 2, "c": 1, "d": 0, "e": 7, "f": 6, "g": 5,
    "h": 4, "i": 11, "j": 10, "k": 3, "l": 8, "m": 9,
    "n": 14, "o": 13, "p": 15, "q": 18,
}
CONTROLLER = (30, 10, 21)  # aisle, row, column
CPU_INTERFACE = (30, 10, 22)


def main() -> None:
    aisles = [re.findall(r'"([^"]*)"', line) for line in SOURCE.read_text().splitlines()
              if ".aisle(" in line]
    if len(aisles) != 44 or any(len(aisle) != 22 for aisle in aisles):
        raise ValueError("Expected 44 aisles of 22 rows")
    if any(len(row) != 44 for aisle in aisles for row in aisle):
        raise ValueError("Expected every row to contain 44 blocks")
    if sum(row.count("m") for aisle in aisles for row in aisle) != 320:
        raise ValueError("Expected 320 crafting core slots")

    for position, marker in ((CONTROLLER, "q"), (CPU_INTERFACE, "p")):
        aisle, row, column = position
        line = aisles[aisle][row]
        if line[column] != "k":
            raise ValueError(f"Expected Laminated Glass at {position}")
        aisles[aisle][row] = line[:column] + marker + line[column + 1:]

    with TARGET.open("wb") as output:
        output.write(struct.pack(">i", len(aisles)))
        for rows in aisles:
            output.write(struct.pack(">i", len(rows)))
            for blocks in rows:
                output.write(struct.pack(">i", len(blocks)))
                output.write(bytes(SYMBOLS[block] for block in blocks))
    print(f"Wrote {TARGET.relative_to(ROOT)} ({TARGET.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
