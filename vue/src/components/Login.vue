<template>
  <div class="login-container">
    <div class="login-box">
      <h2>会议室预订系统</h2>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
        </el-form-item>
        <div class="register-link">
          <router-link to="/register">没有账号？立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { login } from '@/api/user';

export default {
  name: 'LoginPage',
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const loginFormRef = ref(null);
    const loading = ref(false);

    const loginForm = reactive({
      username: '',
      password: ''
    });

    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    };

    const handleLogin = () => {
      loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true;
          try {
            console.log('发送登录请求:', loginForm);
            // 发送登录请求
            const res = await login(loginForm);
            console.log('登录响应:', res);
            
            // 检查响应是否成功 (code=1表示成功)
            if (res && res.code === 1) {
              ElMessage.success('登录成功');
              
              // 存储服务器返回的token
              userStore.setToken(res.data);
              
              // 获取JWT中的用户信息
              try {
                // 解析JWT获取用户信息
                const tokenParts = res.data.split('.');
                if (tokenParts.length === 3) {
                  const payload = JSON.parse(atob(tokenParts[1]));
                  console.log('Token payload:', payload);
                  
                  // 构建用户信息对象
                  const userInfo = {
                    id: payload.userId,
                    username: payload.username,
                    roleId: payload.roleId
                  };
                  
                  // 保存用户信息
                  userStore.setUserInfo(userInfo);
                  
                  // 登录成功，跳转到会议室预订页面
                  router.push('/room/booking');
                  return;
                }
              } catch (error) {
                console.error('解析token失败:', error);
              }
              
              // 如果解析token失败，使用默认值
              userStore.setUserInfo({
                id: 1,
                username: loginForm.username,
                roleId: 2, // 默认为普通用户
                roleName: '普通用户'
              });
              
              // 登录成功，跳转到会议室预订页面
              router.push('/room/booking');
            } else if (res) {
              // 处理错误情况，优先使用msg字段作为错误消息
              ElMessage.error(res.msg || res.message || '登录失败');
            } else {
              // 响应为空
              ElMessage.error('登录失败，服务器返回空数据');
            }
          } catch (error) {
            console.error('登录失败:', error);
            ElMessage.error('登录请求失败: ' + (error.message || '网络错误'));
          } finally {
            loading.value = false;
          }
        }
      });
    };

    return {
      loginForm,
      loginRules,
      loginFormRef,
      loading,
      handleLogin
    };
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-box {
  width: 400px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #409eff;
}

.login-btn {
  width: 100%;
}

.register-link {
  text-align: right;
  margin-top: 10px;
  font-size: 14px;
}
</style> 