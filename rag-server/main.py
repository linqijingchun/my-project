# -*- coding:utf-8 -*-
from fastapi import FastAPI, UploadFile, File, Form, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
import time
import json
import os
import requests

# 导入现有的功能模块
from Document_upload import Document_Upload
from Document_Q_And_A import Document_Q_And_A

# 创建FastAPI应用实例
app = FastAPI(
    title="文档问答系统API",
    description="支持本地文件上传到讯飞星火文档服务\n" +
    "- **文档问答**: 基于已上传的文档内容进行智能问答\n" +
    "\n" +
    "### 使用说明\n" +
    "1. 首先通过`/api/upload-document`接口上传文档获取fileId\n" +
    "2. 然后使用获取的fileId通过`/api/qa-document`接口进行问答",
    version="1.0.0"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 在生产环境中应该设置具体的域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 应用配置 - 建议从环境变量或配置文件读取
APP_ID = "11f901d1"  # 正确的ID（从Document_upload.py测试中获取）
API_SECRET = "ZDljZDQwZjYxMWQ5MGVlYjM3MzkwZGU5"  # 正确的密钥（从Document_upload.py测试中获取）
UPLOAD_URL = "https://chatdoc.xfyun.cn/openapi/v1/file/upload"
CHAT_URL = "wss://chatdoc.xfyun.cn/openapi/chat"

# 请求模型
class QARequest(BaseModel):
    file_id: str
    question: str

@app.get("/")
async def root():
    return {"message": "文档问答系统API服务运行中"}

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

@app.post("/api/upload-document", summary="上传文档", description="上传本地文件或URL文件到文档服务")
async def upload_document(
    file: UploadFile = File(None),
    fileUrl: str = Form(None),
    fileName: str = Form(None),
    need_summary: bool = Form(False),
    step_by_step: bool = Form(False),
    callback_url: str = Form(None),
    parseType: str = Form("AUTO")
):
    """
    上传文档到讯飞星火文档服务
    
    - **file**: 要上传的本地文件
    - **fileUrl**: 要上传的文件URL
    - **fileName**: 文件名称（当使用fileUrl时必需）
    - **need_summary**: 是否需要摘要，默认False
    - **step_by_step**: 是否分步处理，默认False
    - **callback_url**: 回调URL，可选
    - **parseType**: 解析类型，默认AUTO
    """
    try:
        # 生成当前时间戳
        cur_time = str(int(time.time()))
        
        # 创建上传实例
        document_upload = Document_Upload(APP_ID, API_SECRET, cur_time)
        headers = document_upload.get_header()
        
        # 发送请求
        if file:
            # 本地上传
            # 使用与Document_upload.py完全相同的方式构建请求
            body = {
                "url": "",
                "fileName": file.filename,
                "fileType": "wiki",
                "needSummary": False,
                "stepByStep": False,
                "callbackUrl": "your_callbackUrl",
            }
            
            # 保存上传的文件到临时路径
            import tempfile
            with tempfile.NamedTemporaryFile(delete=False, suffix='.' + file.filename.split('.')[-1]) as temp_file:
                file_content = await file.read()
                temp_file.write(file_content)
                temp_file_path = temp_file.name
            
            # 使用临时文件路径打开文件，与test_main.py保持一致
            files = {'file': open(temp_file_path, 'rb')}  # 使用临时文件路径
            
            # 打印临时文件路径
            print('临时文件路径:', temp_file_path)
            
            # 打印请求信息
            print('\n=== 发送本地上传请求到讯飞API ===')
            print('APP_ID:', APP_ID)
            print('API_SECRET:', API_SECRET)
            print('请求头:', headers)
            print('请求体:', body)
            print('文件:', file.filename)
            
            # 发送请求 - 与Document_upload.py保持一致
            try:
                response = requests.post(UPLOAD_URL, files=files, data=body, headers=headers)
            finally:
                # 关闭文件
                if 'file' in files:
                    files['file'].close()
                # 删除临时文件
                import os
                if os.path.exists(temp_file_path):
                    os.unlink(temp_file_path)
        elif fileUrl and fileName:
            # URL上传
            # 使用与Document_upload.py完全相同的方式构建请求
            body = {
                "file": "",
                "url": fileUrl,
                "fileName": fileName,
                "fileType": "wiki",
                "callbackUrl": "your_callbackUrl"
            }
            
            # 使用MultipartEncoder构建请求体，与Document_upload.py保持一致
            from requests_toolbelt.multipart.encoder import MultipartEncoder
            import random
            form = MultipartEncoder(
                fields=body,
                boundary='------------------' + str(random.randint(1e28, 1e29 - 1))
            )
            
            # 更新请求头，添加Content-Type
            headers['Content-Type'] = form.content_type
            
            # 打印请求信息
            print('\n=== 发送URL上传请求到讯飞API ===')
            print('APP_ID:', APP_ID)
            print('API_SECRET:', API_SECRET)
            print('请求头:', headers)
            print('请求体:', body)
            
            # 发送请求 - 与Document_upload.py保持一致
            response = requests.post(UPLOAD_URL, data=form, headers=headers)
        else:
            raise HTTPException(status_code=400, detail="请提供文件或文件URL和文件名")
        
        # 打印响应信息
        print('\n=== 讯飞API响应 ===')
        print('状态码:', response.status_code)
        print('响应头:', dict(response.headers))
        print('响应内容:', response.text)
        
        response.raise_for_status()  # 检查请求是否成功
        
        return response.json()
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"文档上传失败: {str(e)}")

import websocket
import _thread as thread
import ssl
from fastapi.responses import StreamingResponse
import asyncio
from io import StringIO

@app.post("/api/qa-document", summary="文档问答", description="基于上传的文档进行问答")
async def qa_document(
    request: QARequest
):
    """
    基于上传的文档进行问答
    
    - **file_id**: 上传文档返回的fileId
    - **question**: 用户的问题
    """
    try:
        # 生成当前时间戳
        cur_time = str(int(time.time()))
        
        # 创建问答实例
        document_qa = Document_Q_And_A(APP_ID, API_SECRET, cur_time, CHAT_URL)
        
        # 准备请求体
        body = {
            "chatExtends": {
                "wikiPromptTpl": "请将以下内容作为已知信息：\n<wikicontent>\n请根据以上内容回答用户的问题。\n问题:<wikiquestion>\n回答:",
                "wikiFilterScore": 0.83,
                "temperature": 0.5
            },
            "fileIds": [request.file_id],
            "messages": [
                {
                    "role": "user",
                    "content": request.question
                }
            ]
        }
        
        # 获取WebSocket URL
        ws_url = document_qa.get_url()
        
        # 使用同步方式调用WebSocket（由于websocket库不支持异步）
        result = await asyncio.to_thread(process_websocket_request, ws_url, body)
        
        return {"answer": result}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"文档问答失败: {str(e)}")

@app.get("/api/file-status", summary="获取文件状态", description="获取文档处理状态")
async def get_file_status(
    fileId: str
):
    """
    获取文档处理状态
    
    - **fileId**: 上传文档返回的fileId
    """
    try:
        # 生成当前时间戳
        cur_time = str(int(time.time()))
        
        # 创建上传实例
        document_upload = Document_Upload(APP_ID, API_SECRET, cur_time)
        headers = document_upload.get_header()
        
        # 发送请求
        import urllib.parse
        encoded_fileId = urllib.parse.quote(fileId)
        url = f"https://chatdoc.xfyun.cn/openapi/v1/file/status?fileId={encoded_fileId}"
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # 检查请求是否成功
        
        return response.json()
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取文件状态失败: {str(e)}")

# 处理WebSocket请求的函数
def process_websocket_request(ws_url, body):
    result_buffer = []
    
    # 定义WebSocket回调函数
    def on_message(ws, message):
        data = json.loads(message)
        code = data['code']
        if code != 0:
            print(f'请求错误: {code}, {data}')
            ws.close()
        else:
            content = data["content"]
            status = data["status"]
            result_buffer.append(content)
            if status == 2:
                ws.close()
    
    def on_error(ws, error):
        print(f"WebSocket错误: {error}")
        result_buffer.append(f"错误: {str(error)}")
    
    def on_close(ws, close_status_code, close_msg):
        print("WebSocket连接关闭")
    
    def on_open(ws):
        def run(*args):
            data = json.dumps(body)
            ws.send(data)
        thread.start_new_thread(run, ())
    
    # 禁用WebSocket库的跟踪功能
    websocket.enableTrace(False)
    
    # 创建WebSocket应用
    ws = websocket.WebSocketApp(
        ws_url,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
        on_open=on_open
    )
    
    # 运行WebSocket直到关闭
    ws.run_forever(sslopt={"cert_reqs": ssl.CERT_NONE})
    
    # 返回结果
    return ''.join(result_buffer)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)