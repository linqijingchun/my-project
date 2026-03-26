import requests
import os

# 测试文件上传
def test_upload():
    url = "http://localhost:8000/api/upload-document"
    
    # 准备文件
    file_path = "f:\\soft\\代码\\test.txt"
    
    # 准备FormData
    files = {
        'file': (os.path.basename(file_path), open(file_path, 'rb'))
    }
    
    data = {
        'need_summary': 'false',
        'step_by_step': 'false',
        'parseType': 'AUTO'
    }
    
    try:
        response = requests.post(url, files=files, data=data)
        print(f"Status code: {response.status_code}")
        print(f"Response: {response.text}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        # 关闭文件
        if 'file' in files:
            files['file'][1].close()

if __name__ == "__main__":
    test_upload()
