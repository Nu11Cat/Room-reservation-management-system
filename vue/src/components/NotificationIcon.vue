<template>
  <div class="notification-icon">
    <el-badge :value="unreadCount" :max="99" class="notification-badge">
      <el-button @click="handleClick" circle class="notification-btn">
        <el-icon><Bell /></el-icon>
      </el-button>
    </el-badge>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { Bell } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

export default {
  name: 'NotificationIcon',
  components: {
    Bell
  },
  setup() {
    const userStore = useUserStore();
    const router = useRouter();
    
    const unreadCount = computed(() => userStore.unreadCount);
    
    const handleClick = () => {
      router.push('/notification/list');
    };
    
    return {
      unreadCount,
      handleClick
    };
  }
};
</script>

<style scoped>
.notification-icon {
  display: inline-block;
  margin-right: 10px;
}

.notification-badge {
  margin-top: 10px;
}

.notification-btn {
  background: transparent;
  border: none;
  font-size: 18px;
  color: #606266;
}

.notification-btn:hover {
  color: #409EFF;
}
</style> 