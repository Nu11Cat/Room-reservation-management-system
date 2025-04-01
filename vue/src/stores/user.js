import { defineStore } from 'pinia';
import { usePermissionStore } from './permission';

export const useUserStore = defineStore('user', {
  state: () => {
    // 从localStorage读取用户信息，如果存在则解析为对象
    let userInfo = null;
    try {
      const savedUserInfo = localStorage.getItem('userInfo');
      if (savedUserInfo) {
        userInfo = JSON.parse(savedUserInfo);
      }
    } catch (e) {
      console.error('解析用户信息失败', e);
    }
    
    return {
      token: localStorage.getItem('token') || '',
      userInfo: userInfo,
      unreadCount: 0 // 未读通知数量
    };
  },

  actions: {
    // 设置token
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
    },

    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo;
      // 将用户信息存储到localStorage
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      
      // 设置角色权限
      const permissionStore = usePermissionStore();
      permissionStore.setRoleId(userInfo.roleId);
    },

    // 清除用户信息
    clearUserInfo() {
      this.token = '';
      this.userInfo = null;
      this.unreadCount = 0;
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      
      // 清除权限信息
      const permissionStore = usePermissionStore();
      permissionStore.clearPermissions();
    },
    
    // 登出
    logout() {
      this.clearUserInfo();
    },
    
    // 设置未读通知数量
    setUnreadCount(count) {
      this.unreadCount = count;
    },
    
    // 增加未读通知数量
    incrementUnreadCount() {
      this.unreadCount++;
    },
    
    // 减少未读通知数量
    decrementUnreadCount() {
      if (this.unreadCount > 0) {
        this.unreadCount--;
      }
    }
  }
}); 