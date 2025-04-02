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
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['token'] = token;
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
      
      switch (error.response.status) {
        case 401:
          // token过期或未登录
          ElMessage.error('请重新登录');
          router.push('/login');
          break;
        case 403:
          ElMessage.error('没有权限');
          router.push('/403');
          break;
        case 404:
          ElMessage.error('请求的资源不存在');
          break;
        case 500:
          ElMessage.error('服务器错误');
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