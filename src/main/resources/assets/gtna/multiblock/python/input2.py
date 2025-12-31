import re

def clean_structure_file(input_file, cleaned_file):
    with open(input_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    cleaned_lines = []
    for line in lines:
        # 删除行首4个空格加引号
        line = re.sub(r'^\s{4}"', '', line)
        # 替换所有的'",'为'"'
        line = line.replace('"', '')
        # 跳过包含'},{'的行
        if '},{' in line:
            continue
        if '}}' in line:
            continue
        # 过滤空行
        cleaned_lines.append(line.rstrip('\n'))

    with open(cleaned_file, 'w', encoding='utf-8') as f:
        for line in cleaned_lines:
            f.write(line + '\n')

    print(f"清理完成，输出文件：{cleaned_file}")

# 调用示例
input_file = 'output1.txt'
cleaned_file = 'cleaned_structure.txt'

clean_structure_file(input_file, cleaned_file)
