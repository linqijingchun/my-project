/**
 * 星火知识库API鉴权服务
 * 严格按照ApiAuthUtil.java实现的认证逻辑
 */
// 导入crypto-js库
import CryptoJS from 'crypto-js';

class AuthService {
  constructor(appId, secret) {
    this.appId = appId;
    this.secret = secret;
    this.pythonScriptPath = '/d:/WorkSpace/03 VueWorkSpace/hellorag/Document_upload.py';
    // 与Java代码完全一致的MD5字符表
    this.MD5_TABLE = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'];
    // 调试日志
    console.log('===== AuthService 初始化 =====');
    console.log('appId:', this.appId);
    console.log('secret:', this.secret ? '已设置' : '未设置');
    console.log('Python脚本路径:', this.pythonScriptPath);
    console.log('============================');
  }

  // 由于浏览器环境限制，不再使用Python脚本，直接使用JavaScript实现鉴权
  getAuthInfo() {
    console.log('===== 开始生成鉴权信息 =====');
    return new Promise((resolve, reject) => {
      try {
        // 生成秒级时间戳，与Java保持一致
        const timestamp = Math.floor(Date.now() / 1000).toString();
        console.log('生成的timestamp:', timestamp);
        
        const signature = this.getSignatureByBackup(timestamp);
        
        const authInfo = {
          appId: this.appId,
          timestamp: timestamp,
          signature
        };
        
        console.log('生成的鉴权信息:', authInfo);
        console.log('===== 鉴权信息生成完成 =====');
        resolve(authInfo);
      } catch (error) {
        console.error('生成鉴权信息失败:', error);
        reject(error);
      }
    });
  }
  
  // 备份的签名生成方法（仅在Python脚本失败时使用）
  getSignatureByBackup(timestamp) {
    console.log('使用备份方案生成签名');
    const combined = this.appId + timestamp;
    const md5Result = this.md5(combined);
    return this.hmacSHA1Encrypt(md5Result, this.secret);
  }

    // 注意：原始的getSignature方法已被getAuthInfo取代，不再直接提供此方法

  /**
   * MD5加密函数 - 使用crypto-js库实现
   * @param {string} content - 要加密的内容
   * @returns {string} - 返回MD5加密后的十六进制字符串
   */
  md5(content) {
    try {
      console.log('===== 开始MD5加密 =====');
      console.log('加密内容:', content);
      
      // 使用crypto-js库计算MD5
      const md5Result = CryptoJS.MD5(content).toString();
      
      console.log('MD5加密结果:', md5Result);
      console.log('===== MD5加密完成 =====');
      
      return md5Result;
    } catch (error) {
      console.error('MD5加密失败:', error);
      throw error;
    }
  }

  // MD5辅助函数
  _ff(a, b, c, d, x, s, t) {
    const temp = this._add32(this._add32(this._add32(a, ((b & c) | (~b & d))), x), t);
    return (temp << s) | (temp >>> (32 - s));
  }

  _gg(a, b, c, d, x, s, t) {
    const temp = this._add32(this._add32(this._add32(a, ((b & d) | (c & ~d))), x), t);
    return (temp << s) | (temp >>> (32 - s));
  }

  _hh(a, b, c, d, x, s, t) {
    const temp = this._add32(this._add32(this._add32(a, (b ^ c ^ d)), x), t);
    return (temp << s) | (temp >>> (32 - s));
  }

  _ii(a, b, c, d, x, s, t) {
    const temp = this._add32(this._add32(this._add32(a, (c ^ (b | ~d))), x), t);
    return (temp << s) | (temp >>> (32 - s));
  }

  _add32(x, y) {
    const lsw = (x & 0xffff) + (y & 0xffff);
    const msw = (x >>> 16) + (y >>> 16) + (lsw >>> 16);
    return ((msw << 16) | (lsw & 0xffff)) >>> 0;
  }

  /**
   * HmacSHA1加密实现 - 使用crypto-js库实现
   * @param {string} encryptText - 要加密的文本（对应Java中的auth）
   * @param {string} encryptKey - 加密密钥（对应Java中的secret）
   * @returns {string} Base64编码的HMAC-SHA1结果
   */
  hmacSHA1Encrypt(encryptText, encryptKey) {
    try {
      console.log('===== 开始HMAC-SHA1加密 =====');
      console.log('encryptText:', encryptText);
      console.log('encryptKey:', encryptKey);
      
      // 使用crypto-js库计算HMAC-SHA1
      const hmacResult = CryptoJS.HmacSHA1(encryptText, encryptKey);
      const base64Result = CryptoJS.enc.Base64.stringify(hmacResult);
      
      console.log('Base64编码结果:', base64Result);
      console.log('===== HMAC-SHA1加密完成 =====');
      
      return base64Result;
    } catch (error) {
      console.error('HmacSHA1加密失败:', error);
      throw error;
    }
  }



  /**
   * 生成WebSocket鉴权URL
   * @param {string} baseUrl - WebSocket基础URL
   * @returns {string} 带鉴权参数的WebSocket URL
   */
  async generateWebSocketUrl(baseUrl) {
    try {
      const { timestamp, signature } = await this.getAuthInfo();
      // 按照Main.java中的URL构建方式
      return `${baseUrl}?appId=${this.appId}&timestamp=${timestamp}&signature=${signature}`;
    } catch (error) {
      console.error('生成WebSocket URL失败:', error);
      throw error;
    }
  }

  /**
   * 生成HTTP请求头
   * @returns {Object} HTTP请求头对象
   */
  async generateHttpHeaders() {
    try {
      // 直接按照Main.java中的请求头格式
      const { timestamp, signature } = await this.getAuthInfo();
      
      return {
        'appId': this.appId,
        'timestamp': timestamp,
        'signature': signature
      };
    } catch (error) {
      console.error('生成HTTP请求头失败:', error);
      throw error;
    }
  }
}

export default AuthService;