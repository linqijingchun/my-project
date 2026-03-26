# 测试main.py文件中的上传逻辑
import time
import requests
from Document_upload import Document_Upload

# 测试配置
APP_ID = "11f901d1"
API_SECRET = "ZDljZDQwZjYxMWQ5MGVlYjM3MzkwZGU5"
UPLOAD_URL = "https://chatdoc.xfyun.cn/openapi/v1/file/upload"

# 测试上传
def test_upload():
    # 生成当前时间戳
    cur_time = str(int(time.time()))
    
    # 创建上传实例
    document_upload = Document_Upload(APP_ID, API_SECRET, cur_time)
    headers = document_upload.get_header()
    
    # 使用与Document_upload.py完全相同的方式上传文件
    body = {
        "url": "",
        "fileName": "test.txt",
        "fileType": "wiki",
        "needSummary": False,
        "stepByStep": False,
        "callbackUrl": "your_callbackUrl",
    }
    
    # 使用与Document_upload.py完全相同的方式打开文件
    files = {'file': open('f:\\soft\\代码\\test.txt', 'rb')}
    
    # 打印请求信息
    print('\n=== 测试上传请求 ===')
    print('APP_ID:', APP_ID)
    print('API_SECRET:', API_SECRET)
    print('请求头:', headers)
    print('请求体:', body)
    print('文件:', 'test.txt')
    
    # 发送请求
    try:
        response = requests.post(UPLOAD_URL, files=files, data=body, headers=headers)
        print('\n=== 响应信息 ===')
        print('状态码:', response.status_code)
        print('响应内容:', response.text)
    except Exception as e:
        print('\n=== 错误信息 ===')
        print('错误:', str(e))
    finally:
        # 关闭文件
        if 'file' in files:
            files['file'].close()

if __name__ == "__main__":
    test_upload()
