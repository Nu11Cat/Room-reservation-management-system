<template>
  <div class="login-container" :style="backgroundStyle">
    <div class="login-box">
      <div class="login-header">
        <h2>{{ systemName }}</h2>
        <p>欢迎回来，请登录您的账号</p>
      </div>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            prefix-icon="User"
            class="custom-input" 
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock" 
            show-password
            class="custom-input"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录系统</el-button>
        </el-form-item>
        <div class="register-link">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { useConfigStore } from '@/stores/config';
import { login } from '@/api/user';

export default {
  name: 'LoginPage',
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const configStore = useConfigStore();
    const loginFormRef = ref(null);
    const loading = ref(false);

    // 获取系统名称和背景图片
    const systemName = ref('会议室预约管理系统');
    const loginBgImage = ref('');
    
    // 计算背景样式
    const backgroundStyle = computed(() => {
      if (loginBgImage.value) {
        return {
          backgroundImage: `url(${loginBgImage.value})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center'
        };
      }
      return {
        background: 'linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%)'
      };
    });
    
    // 初始化时尝试获取配置
    onMounted(async () => {
      if (!configStore.initialized) {
        try {
          await configStore.initialize();
          if (configStore.systemName) {
            systemName.value = configStore.systemName;
          }
          // 尝试获取背景图片设置
          if (configStore.loginBgImage) {
            loginBgImage.value = configStore.loginBgImage;
          } else {
            // 尝试从localStorage获取用户自定义背景图片
            const customBg = localStorage.getItem('loginBgImage');
            if (customBg) {
              loginBgImage.value = customBg;
            }
          }
        } catch (error) {
          console.error('获取系统配置失败:', error);
        }
      } else {
        if (configStore.systemName) {
          systemName.value = configStore.systemName;
        }
        // 尝试获取背景图片设置
        if (configStore.loginBgImage) {
          loginBgImage.value = configStore.loginBgImage;
        } else {
          // 尝试从localStorage获取用户自定义背景图片
          const customBg = localStorage.getItem('loginBgImage');
          if (customBg) {
            loginBgImage.value = customBg;
          }
        }
      }
    });

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
              
              // 从data字段获取token
              const token = res.data;
              console.log('从data字段获取token:', token);
              
              // 直接存储到localStorage，避免通过store
              localStorage.setItem('token', token);
              console.log('Token已直接保存到localStorage:', token);
              console.log('localStorage中保存的token:', localStorage.getItem('token'));
              
              // 获取JWT中的用户信息
              try {
                // 解析JWT获取用户信息
                const tokenParts = token.split('.');
                if (tokenParts.length === 3) {
                  const payload = JSON.parse(atob(tokenParts[1]));
                  console.log('Token payload:', payload);
                  
                  // 构建用户信息对象
                  const userInfo = {
                    id: payload.userId,
                    username: payload.username,
                    roleId: payload.roleId
                  };
                  
                  // 直接保存用户信息到localStorage
                  localStorage.setItem('userInfo', JSON.stringify(userInfo));
                  console.log('userInfo已保存到localStorage:', userInfo);
                  
                  // 同步更新store
                  userStore.token = token;
                  userStore.userInfo = userInfo;
                  
                  // 登录成功，跳转到会议室预订页面
                  setTimeout(() => {
                    console.log('登录成功后localStorage token:', localStorage.getItem('token'));
                    router.push('/room/booking');
                  }, 500);
                  
                  return;
                }
              } catch (error) {
                console.error('解析token失败:', error);
              }
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
      handleLogin,
      systemName,
      backgroundStyle
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
  width: 100%;
  background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 1;
  pointer-events: none;
  display: none;
}

.login-container[style*="background-image"]::before {
  display: block;
}

.login-box {
  width: 400px;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 2;
  backdrop-filter: blur(5px);
  transition: all 0.3s ease;
  animation: fadeIn 0.8s;
}

.login-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  margin-bottom: 30px;
  text-align: center;
}

.login-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  color: #888;
  font-size: 14px;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 12px 15px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.login-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border-radius: 4px;
  margin-top: 10px;
  transition: all 0.2s;
}

.login-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #409EFF;
  font-weight: 500;
  margin-left: 5px;
  text-decoration: none;
  transition: color 0.2s;
}

.register-link a:hover {
  text-decoration: underline;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式调整 */
@media (max-width: 500px) {
  .login-box {
    width: 90%;
    padding: 30px;
  }
}
</style>