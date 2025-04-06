import axios from 'axios'
import { ElMessage } from 'element-plus';
import router from '@/router';
import config from '@/config';
import { useUserStore } from '@/stores/user';

// 创建axios实例
const service = axios.create({
  baseURL: config.baseURL,
  timeout: 5000,
  withCredentials: true // 允许跨域携带 cookie
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 优先从localStorage直接读取token，确保最新值
    const token = localStorage.getItem('token');
    console.log('请求拦截器中获取的token:', token);
    
    if (token) {
      // 确保headers对象存在
      config.headers = config.headers || {};
      // 设置token到请求头
      config.headers['token'] = token;
      console.log(`已添加token到请求头，URL: ${config.url}`);
    } else {
      console.warn(`请求未携带token，URL: ${config.url}`);
    }
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果响应为空，返回错误
    if (!response || !response.data) {
      ElMessage.error('服务器返回了空的响应');
      return Promise.reject(new Error('服务器返回了空的响应'));
    }
    
    // 返回响应数据，让业务代码自行处理状态码
    return response.data;
  },
  error => {
    // 检查错误响应
    if (error.response) {
      // 检查是否为用户封禁错误
      if (error.response.data && error.response.data.msg === 'USER_BANNED') {
        ElMessage.error('您的账号已被禁用');
        // 清除用户信息
        const userStore = useUserStore();
        userStore.logout();
        router.push('/login');
        return Promise.reject(error);
      }
      
      // 对于500错误，不显示任何消息，由业务代码处理
      if (error.response.status === 500) {
        // 静默处理500错误，只向控制台输出日志
        console.error('服务器返回500错误:', error);
        return Promise.reject(error);
      }
      
      // 根据不同状态码处理错误
      switch (error.response.status) {
        case 401:
          console.error('身份验证失败(401): 用户未登录或token已过期');
          
          // 清除本地无效token
          localStorage.removeItem('token');
          
          // 获取并清空用户store
          // eslint-disable-next-line no-case-declarations
          const userStore = useUserStore();
          userStore.clearUserInfo();
          
          // 只有非登录页面才显示提示和重定向
          if (!window.location.pathname.includes('/login')) {
            ElMessage.error('登录已过期，请重新登录');
            router.push('/login');
          }
          break;
        case 403:
          ElMessage.error('没有权限');
          router.push('/403');
          break;
        case 404:
          ElMessage.error('请求的资源不存在');
          break;
        case 405:
          // 方法不允许，静默处理
          console.error('方法不允许(405):', error);
          break;
        default:
          ElMessage.error(error.message || '请求失败');
      }
    } else if (error.request) {
      ElMessage.error('网络请求超时或服务器无响应');
    } else {
      ElMessage.error('网络请求错误: ' + error.message);
    }
    return Promise.reject(error);
  }
);

export default service; 