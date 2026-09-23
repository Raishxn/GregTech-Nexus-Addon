"""Convert the author's Nexus ME Hypercore text layout to GTNA's .mbs format.

The text export has no controller or CPU Interface markers. Its center glass on
the final aisle is replaced by Q; the adjacent grating is replaced by P.
"""

from pathlib import Path
import re
import struct


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/structures/nexus_me_hypercore_source.txt"
TARGET = ROOT / "src/main/resources/pattern/nexus_me_hypercore.mbs"
SYMBOLS = {" ": 19, "B": 1, "C": 0, "D": 7, "E": 6, "F": 5, "G": 4,
           "H": 11, "I": 10, "J": 3, "P": 15, "Q": 18}
CONTROLLER = (40, 21, 20)  # aisle, row, column
CPU_INTERFACE = (40, 21, 19)


def main() -> None:
    aisles = [re.findall(r'"([^"]*)"', line) for line in SOURCE.read_text().splitlines()
              if ".aisle(" in line]
    if len(aisles) != 41 or any(len(aisle) != 43 for aisle in aisles):
        raise ValueError("Expected 41 aisles of 43 rows")
    if any(len(row) != 41 for aisle in aisles for row in aisle):
        raise ValueError("Expected every row to contain 41 blocks")
    for position, expected, replacement in ((CONTROLLER, "B", "Q"),
                                            (CPU_INTERFACE, "C", "P")):
        aisle, row, column = position
        if aisles[aisle][row][column] != expected:
            raise ValueError(f"Expected {expected} at {position}")
        line = aisles[aisle][row]
        aisles[aisle][row] = line[:column] + replacement + line[column + 1:]

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
