"""
A script to scan through all json files, and minify them.
"""
__author__ = '70CentsApple'

import os
import json

def minify_json_file(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, separators=(',', ':'), ensure_ascii=False)
        print(f'Minified: {file_path}')
    except json.JSONDecodeError:
        print(f'Skipped (invalid JSON): {file_path}')
    except Exception as e:
        print(f'Error processing {file_path}: {e}')

def main():
    for root, _, files in os.walk('.'):
        for file in files:
            if file.endswith('.json'):
                file_path = os.path.join(root, file)
                minify_json_file(file_path)

if __name__ == '__main__':
    main()
