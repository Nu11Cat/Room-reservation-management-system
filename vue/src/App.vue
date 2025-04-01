<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
import { watch } from 'vue';
import { useUserStore } from '@/stores/user';
import websocketService from '@/services/websocket';
import { getUnreadCount } from '@/api/notification';
import { ElNotification } from 'element-plus';

export default {
  name: 'App',
  setup() {
    const userStore = useUserStore();

    // 监听用户登录状态
    watch(() => userStore.token, (newToken) => {
      if (newToken) {
        // 用户已登录，初始化WebSocket连接
        console.log('用户已登录，初始化WebSocket连接');
        websocketService.connect();
      } else {
        // 用户已登出，关闭WebSocket连接
        console.log('用户已登出，关闭WebSocket连接');
        websocketService.close();
      }
    }, { immediate: true });

    // 监听通知事件
    websocketService.on('notification', (notification) => {
      // 更新未读通知数量
      updateUnreadCount();
      
      // 显示系统通知
      ElNotification({
        title: notification.title,
        message: notification.content,
        type: 'info',
        duration: 5000
      });
    });

    // 更新未读通知数量
    const updateUnreadCount = async () => {
      try {
        const res = await getUnreadCount();
        if (res.code === 1) {
          userStore.setUnreadCount(res.data);
        }
      } catch (error) {
        console.error('获取未读通知数量失败:', error);
      }
    };

    // 初始状态检查
    if (userStore.token) {
      websocketService.connect();
    }

    return {};
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  height: 100vh;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
}
</style>
