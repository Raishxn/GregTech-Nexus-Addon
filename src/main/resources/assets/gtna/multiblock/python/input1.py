import re

def parse_input_file(input_file):
    with open(input_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    block_map = {}
    special_tile_letter = None
    offset_line = None

    in_blocks = False
    in_tiles = False
    in_special_tiles = False
    in_offsets = False
    in_structure = False

    structure_start_idx = -1
    structure_end_idx = -1

    for idx, line in enumerate(lines):
        line_strip = line.strip()

        if line_strip.startswith("Blocks:"):
            in_blocks = True
            in_tiles = False
            in_special_tiles = False
            continue
        if line_strip.startswith("Tiles:"):
            in_blocks = False
            in_tiles = True
            in_special_tiles = False
            continue
        if line_strip.startswith("Special Tiles:"):
            in_blocks = False
            in_tiles = False
            in_special_tiles = True
            continue
        if line_strip.startswith("Offsets:"):
            in_blocks = False
            in_tiles = False
            in_special_tiles = False
            in_offsets = True
            continue
        if line_strip.startswith("Normal Scan:"):
            in_offsets = False
            continue
        if line_strip.startswith("new String[][]"):
            in_structure = True
            structure_start_idx = idx
            continue
        if in_structure and line_strip == "}}":
            structure_end_idx = idx
            in_structure = False
            continue

        if in_blocks or in_tiles or in_special_tiles:
            m = re.match(r"([A-Za-z])\s*->\s*(.*);", line_strip)
            if m:
                letter = m.group(1)
                desc = m.group(2)
                block_map[letter] = desc
                if in_special_tiles and 'ofSpecialTileAdder(gregtech.api.metatileentity.BaseMetaTileEntity' in desc:
                    special_tile_letter = letter

        if in_offsets:
            if re.match(r"^\d+ \d+ \d+$", line_strip):
                offset_line = line_strip

    if structure_start_idx >= 0 and structure_end_idx > structure_start_idx:
        structure_content = lines[structure_start_idx+1:structure_end_idx+1]
    else:
        structure_content = []

    # Count the number of rows in the first {...} section (excluding the rows within curly braces)
    start_brace_idx = -1
    end_brace_idx = -1
    for i, line in enumerate(structure_content):
        if '{' in line:
            start_brace_idx = i
            for j in range(i + 1, len(structure_content)):
                if '}' in structure_content[j]:
                    end_brace_idx = j
                    break
            break

    if start_brace_idx >= 0 and end_brace_idx > start_brace_idx:
        inner_lines_count = end_brace_idx - start_brace_idx - 1
    else:
        inner_lines_count = 0

    return block_map, special_tile_letter, offset_line, structure_content, inner_lines_count


def replace_special_letter(structure_content, special_letter):
    if not special_letter:
        return structure_content
    new_content = []
    for line in structure_content:
        replaced_line = ''
        for ch in line:
            if ch == special_letter or ch == special_letter.lower():
                replaced_line += '~'
            else:
                replaced_line += ch
        new_content.append(replaced_line)
    return new_content


def write_mapping_file(block_map, special_letter, offset_line, inner_lines_count, output_path):
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write("Structure block mapping table:\n\n")
        f.write("Letter correspondence:\n")
        for letter, desc in block_map.items():
            if letter == special_letter:
                f.write(f"{letter} -> Controller position\n")
            else:
                f.write(f"{letter} -> {desc}\n")
        f.write("\n")
        f.write(f"Offset value Offsets: {offset_line}\n")
        f.write(f"Structural height: {inner_lines_count}\n")


def write_structure_file(structure_content, special_letter, output_path):
    replaced = replace_special_letter(structure_content, special_letter)
    clean_lines = []
    for line in replaced:
        if line.strip() != "":
            # Replace all of the lines '",'
            line = re.sub(r'",', '"', line)
        clean_lines.append(line)
    with open(output_path, 'w', encoding='utf-8') as f:
        for line in clean_lines:
            f.write(line)


if __name__ == '__main__':
    input_file = 'input.txt'
    mapping_output = 'Structureblockmappingtable.txt'
    structure_output = 'output1.txt'

    block_map, special_tile_letter, offset_line, structure_content, inner_lines_count = parse_input_file(input_file)
    write_mapping_file(block_map, special_tile_letter, offset_line, inner_lines_count, mapping_output)
    write_structure_file(structure_content, special_tile_letter, structure_output)

    print("Processing complete! The structure block mapping table and structure content have been output.")
