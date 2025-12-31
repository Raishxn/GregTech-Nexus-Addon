import re

def replace_once(text, replacements):
    """
    替换文本中的特定字符为给定字符，保证每个字符只替换一次。

    :param text: 要处理的文本
    :param replacements: 一个字典，键是要替换的字符，值是替换成的字符
    :return: 替换后的文本
    """
    # 创建一个正则表达式匹配模式
    pattern = re.compile('|'.join(map(re.escape, replacements.keys())))
    
    # 用一个局部函数来执行替换，避免再次替换已经替换的字符
    def replacer(match):
        word = match.group(0)
        # 使用 get() 获取替换字符，不删除字典中的键
        return replacements.get(word, word)
    
    # 使用正则替换所有匹配的字符
    return pattern.sub(replacer, text)

def process_file(input_file, output_file, replacements):
    """
    处理文件中的文本，替换特定字符，并将结果保存到新的文件。

    :param input_file: 输入文件路径
    :param output_file: 输出文件路径
    :param replacements: 一个字典，键是要替换的字符，值是替换成的字符
    """
    # 读取输入文件的内容
    with open(input_file, 'r', encoding='utf-8') as f:
        text = f.read()

    # 替换文本
    result = replace_once(text, replacements)

    # 将替换后的文本写入输出文件
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(result)

# 示例
input_file = 'input.txt'  # 输入文件路径
output_file = 'output.txt'  # 输出文件路径
replacements = {
    'A': 'A',
    'B': 'B',
    'C': 'C',
    'D': 'T',
    'E': 'D',
    'F': 'E',
    'G': 'F',
    'H': 'G',
    'I': 'P',
    'J': 'H',
    'K': 'O',
    'L': 'I',
    'M': 'J',
    'N': 'K',
    'O': 'M',
    'P': 'Y',
    'Q': 'Z'
}

process_file(input_file, output_file, replacements)
