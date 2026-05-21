import os
import re

dir_path = '/Users/leminhdao168/Documents/học tập/code/mobile/bài tập cuối kì/BTCK-APP-FE/app/src/main/java/com/example/btck'

patterns = [
    (r'"Lỗi kết nối: "\s*\+\s*t\.getMessage\(\)', r'"Lỗi mạng"'),
    (r'"Lỗi kết nối khi gửi FCM Token: "\s*\+\s*t\.getMessage\(\)', r'"Lỗi mạng"'),
    (r'"Lỗi gửi FCM token: "\s*\+\s*t\.getMessage\(\)', r'"Lỗi mạng"'),
    (r'"Lỗi tải số thông báo chưa đọc: "\s*\+\s*t\.getMessage\(\)', r'"Lỗi mạng"'),
    (r'callback\.onError\("Lỗi kết nối: " \+ t\.getMessage\(\)\);', r'callback.onError("Lỗi mạng");')
]

for root, dirs, files in os.walk(dir_path):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            new_content = content
            for p, r in patterns:
                new_content = re.sub(p, r, new_content)
                
            if new_content != content:
                with open(filepath, 'w') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
