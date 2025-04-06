<template>
  <div class="register-container" :style="backgroundStyle">
    <div class="register-box">
      <div class="register-header">
        <h2>{{ systemName }}</h2>
        <p>创建您的账号，享受完整功能</p>
      </div>
      <el-form :model="form" :rules="rules" ref="registerForm">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名" 
            class="custom-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="realName">
          <el-input 
            v-model="form.realName" 
            placeholder="请输入真实姓名" 
            class="custom-input"
          >
            <template #prefix>
              <el-icon><Avatar /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号码" 
            class="custom-input"
          >
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input 
            v-model="form.email" 
            placeholder="请输入电子邮箱" 
            class="custom-input"
          >
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            class="custom-input"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请确认密码" 
            show-password
            class="custom-input"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-btn" @click="handleRegister">创建账号</el-button>
        </el-form-item>
        <div class="login-link">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { User, Lock, Avatar, Phone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { ref, onMounted, computed } from 'vue'
import { useConfigStore } from '@/stores/config'
import { register } from '@/api/user'

export default {
  name: 'UserRegister',
  components: {
    User,
    Lock,
    Avatar,
    Phone,
    Message
  },
  setup() {
    const configStore = useConfigStore()
    const systemName = ref('会议室预约管理系统')
    const registerBgImage = ref('');
    
    // 计算背景样式
    const backgroundStyle = computed(() => {
      if (registerBgImage.value) {
        return {
          backgroundImage: `url(${registerBgImage.value})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center'
        };
      }
      return {
        background: 'linear-gradient(120deg, #a18cd1 0%, #8e78ff 100%)'
      };
    });
    
    onMounted(async () => {
      if (!configStore.initialized) {
        try {
          await configStore.getConfig()
          
          if (configStore.config && configStore.config.systemName) {
            systemName.value = configStore.config.systemName
          }
          
          // 尝试获取背景图片设置
          if (configStore.config && configStore.config.registerBgImage) {
            registerBgImage.value = configStore.config.registerBgImage;
          } else {
            // 尝试从localStorage获取用户自定义背景图片
            const customBg = localStorage.getItem('registerBgImage');
            if (customBg) {
              registerBgImage.value = customBg;
            }
          }
        } catch (error) {
          console.error('获取系统配置失败:', error)
        }
      } else {
        if (configStore.config && configStore.config.systemName) {
          systemName.value = configStore.config.systemName
        }
        
        // 尝试获取背景图片设置
        if (configStore.config && configStore.config.registerBgImage) {
          registerBgImage.value = configStore.config.registerBgImage;
        } else {
          // 尝试从localStorage获取用户自定义背景图片
          const customBg = localStorage.getItem('registerBgImage');
          if (customBg) {
            registerBgImage.value = customBg;
          }
        }
      }
    })
    
    return { 
      systemName,
      backgroundStyle
    }
  },
  data() {
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.form.password) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };
    
    const validatePhone = (rule, value, callback) => {
      if (value && !/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的手机号码'));
      } else {
        callback();
      }
    };
    
    const validateEmail = (rule, value, callback) => {
      if (value && !/^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(value)) {
        callback(new Error('请输入正确的电子邮箱'));
      } else {
        callback();
      }
    };
    
    return {
      form: {
        username: '',
        realName: '',
        phone: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { validator: validatePhone, trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入电子邮箱', trigger: 'blur' },
          { validator: validateEmail, trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validatePass2, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          try {
            // 创建要提交的用户数据对象（不包含确认密码字段）
            const userData = {
              username: this.form.username,
              password: this.form.password,
              realName: this.form.realName,
              phone: this.form.phone,
              email: this.form.email,
              roleId: 2 // 默认为普通用户角色
            };
            
            const res = await register(userData);
            
            if (res && res.code === 1) {
              ElMessage.success('注册成功，请登录');
              this.$router.push('/login');
            } else {
              ElMessage.error(res.msg || '注册失败，请重试');
            }
          } catch (error) {
            console.error('注册失败:', error);
            ElMessage.error('注册请求失败: ' + (error.message || '网络错误'));
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100%;
  background: linear-gradient(120deg, #a18cd1 0%, #8e78ff 100%);
  position: relative;
}

.register-container::before {
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

.register-container[style*="background-image"]::before {
  display: block;
}

.register-box {
  width: 450px;
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

.register-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.2);
}

.register-header {
  margin-bottom: 30px;
  text-align: center;
}

.register-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.register-header p {
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

.register-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border-radius: 4px;
  margin-top: 10px;
  transition: all 0.2s;
}

.register-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #8e78ff;
  font-weight: 500;
  margin-left: 5px;
  text-decoration: none;
  transition: color 0.2s;
}

.login-link a:hover {
  text-decoration: underline;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式调整 */
@media (max-width: 500px) {
  .register-box {
    width: 90%;
    padding: 30px;
  }
}
</style> 