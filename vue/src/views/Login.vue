<template>
  <!-- No changes to template section -->
</template>

<script setup>
import { useUserStore } from '../stores/user';
import { usePermissionStore } from '../stores/permission';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { login, getUserInfo } from '@/api/user';
import { getUnreadCount } from '@/api/notification';

const userStore = useUserStore();
const permissionStore = usePermissionStore();
const router = useRouter();
const isLoading = ref(false);

// 获取未读通知数量
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount();
    if (res.code === 1) {
      userStore.setUnreadCount(res.data);
    }
  } catch (error) {
    console.error('获取未读通知数量失败:', error);
  }
};

// 表单提交
const handleSubmit = async () => {
  await formRef.value.validate();
  
  isLoading.value = true;
  try {
    const res = await login(formData);
    if (res.code === 1) {
      userStore.setToken(res.data);
      
      // 获取用户信息
      const userInfo = await getUserInfo();
      if (userInfo.code === 1) {
        userStore.setUserInfo(userInfo.data);
        permissionStore.setRoleId(userInfo.data.roleId);
        
        // 获取未读通知数量
        await fetchUnreadCount();
        
        // 跳转到主页
        router.push('/');
        ElMessage.success('登录成功');
      }
    } else {
      ElMessage.error(res.msg || '登录失败');
    }
  } catch (error) {
    console.error('登录出错', error);
    ElMessage.error('登录失败');
  } finally {
    isLoading.value = false;
  }
};
</script> 