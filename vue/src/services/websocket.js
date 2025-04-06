import { useUserStore } from '@/stores/user';
import config from '@/config';
import { ElMessage } from 'element-plus';
import router from '@/router';

class WebSocketService {
  constructor() {
    this.ws = null;
    this.isConnected = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000; // 3秒
    this.listeners = {};
    this.pingInterval = null;
    this.waitForConnection = null;
    this.reconnectTimeout = null;
    this.heartbeatInterval = null;
    this.events = {};
  }

  /**
   * 初始化WebSocket连接
   */
  connect() {
    const userStore = useUserStore();
    const token = userStore.token;
    
    if (!token) {
      console.error('无法连接WebSocket: 未找到token');
      return;
    }
    
    if (this.ws) {
      this.close();
    }
    
    try {
      const wsUrl = `${config.baseURL.replace('http', 'ws')}/websocket/notification/${token}`;
      console.log('尝试连接WebSocket: ', wsUrl);
      
      this.ws = new WebSocket(wsUrl);
      
      this.ws.onopen = () => {
        console.log('WebSocket连接已建立');
        this.isConnected = true;
        this.reconnectAttempts = 0;
        
        // 设置心跳检测
        this.startHeartbeat();
        
        // 如果有等待连接的Promise，解决它
        if (this.waitForConnection) {
          this.waitForConnection.resolve();
          this.waitForConnection = null;
        }
      };
      
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          console.log('收到WebSocket消息: ', message);
          
          if (message.type === 'notification') {
            // 触发通知事件
            this.trigger('notification', message.data);
            
            // 显示通知
            ElMessage({
              type: 'info',
              message: message.data.title,
              duration: 5000,
              showClose: true
            });
          } else if (message.type === 'user_banned') {
            // 处理用户被封禁的消息
            const userStore = useUserStore();
            
            // 显示封禁通知
            ElMessage({
              type: 'error',
              message: message.data.reason || '您的账号已被管理员禁用',
              duration: 0,
              showClose: true
            });
            
            // 登出用户
            userStore.logout();
            
            // 跳转到登录页
            router.push('/login');
            
            // 关闭WebSocket连接
            this.close();
          }
        } catch (error) {
          console.error('解析WebSocket消息失败: ', error);
        }
      };
      
      this.ws.onclose = (event) => {
        this.isConnected = false;
        this.cleanupConnection();
        
        if (!event.wasClean) {
          console.warn('WebSocket连接意外关闭，尝试重新连接...');
          this.reconnect();
        } else {
          console.log('WebSocket连接已关闭: ', event.code, event.reason);
        }
      };
      
      this.ws.onerror = (error) => {
        console.error('WebSocket错误: ', error);
        this.isConnected = false;
      };
      
    } catch (error) {
      console.error('创建WebSocket连接失败: ', error);
    }
  }
  
  /**
   * 关闭WebSocket连接
   */
  close() {
    if (this.ws) {
      this.cleanupConnection();
      
      try {
        this.ws.close();
      } catch (error) {
        console.error('关闭WebSocket连接失败: ', error);
      }
      
      this.ws = null;
      this.isConnected = false;
    }
  }
  
  /**
   * 重新连接WebSocket
   */
  reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error(`WebSocket重连失败，已达到最大尝试次数(${this.maxReconnectAttempts})`);
      return;
    }
    
    this.reconnectAttempts++;
    
    setTimeout(() => {
      console.log(`WebSocket重连尝试 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
      this.connect();
    }, this.reconnectInterval);
  }
  
  /**
   * 清理连接相关的资源
   */
  cleanupConnection() {
    // 清除心跳检测
    if (this.pingInterval) {
      clearInterval(this.pingInterval);
      this.pingInterval = null;
    }
  }
  
  /**
   * 启动心跳检测，保持连接活跃
   */
  startHeartbeat() {
    this.pingInterval = setInterval(() => {
      if (this.isConnected && this.ws && this.ws.readyState === WebSocket.OPEN) {
        // 发送心跳包
        this.ws.send(JSON.stringify({ type: 'ping' }));
      } else {
        clearInterval(this.pingInterval);
      }
    }, 30000); // 30秒一次心跳
  }
  
  /**
   * 等待WebSocket连接建立
   * @returns {Promise} 表示连接状态的Promise
   */
  async waitForConnected() {
    if (this.isConnected) {
      return Promise.resolve();
    }
    
    return new Promise((resolve, reject) => {
      this.waitForConnection = { resolve, reject };
      
      // 设置超时
      setTimeout(() => {
        if (this.waitForConnection) {
          this.waitForConnection.reject(new Error('WebSocket连接超时'));
          this.waitForConnection = null;
        }
      }, 10000); // 10秒超时
    });
  }
  
  /**
   * 注册事件监听器
   * @param {string} event 事件名称
   * @param {Function} callback 回调函数
   */
  on(event, callback) {
    if (!this.events[event]) {
      this.events[event] = [];
    }
    this.events[event].push(callback);
    return this;
  }
  
  /**
   * 取消事件监听
   * @param {string} event 事件名称
   * @param {Function} callback 回调函数
   */
  off(event, callback) {
    if (!this.events[event]) return this;
    
    if (callback) {
      this.events[event] = this.events[event].filter(cb => cb !== callback);
    } else {
      delete this.events[event];
    }
    
    return this;
  }
  
  /**
   * 触发事件
   * @param {string} event 事件名称
   * @param {any} data 事件数据
   */
  trigger(event, data) {
    if (!this.events[event]) return;
    
    this.events[event].forEach(callback => {
      try {
        callback(data);
      } catch (error) {
        console.error(`执行${event}事件回调时出错:`, error);
      }
    });
  }
}

// 创建单例
const websocketService = new WebSocketService();

export default websocketService; 