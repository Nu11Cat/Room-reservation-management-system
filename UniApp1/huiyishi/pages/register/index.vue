<template>
  <view class="register-container">
    <view class="register-box">
      <view class="register-header">
        <text class="title">会议室预约系统</text>
        <text class="subtitle">欢迎注册，请填写以下信息</text>
      </view>
      
      <view class="form-item">
        <input 
          v-model="registerForm.username" 
          placeholder="请输入用户名" 
          class="input"
        />
      </view>
      
      <view class="form-item">
        <input 
          v-model="registerForm.realName" 
          placeholder="请输入真实姓名" 
          class="input"
        />
      </view>
      
      <view class="form-item">
        <input 
          v-model="registerForm.phone" 
          placeholder="请输入手机号" 
          class="input"
        />
      </view>
      
      <view class="form-item">
        <input 
          v-model="registerForm.email" 
          placeholder="请输入邮箱" 
          class="input"
        />
      </view>
      
      <view class="form-item">
        <input 
          v-model="registerForm.password" 
          type="password" 
          placeholder="请输入密码" 
          class="input"
        />
      </view>
      
      <view class="form-item">
        <input 
          v-model="registerForm.confirmPassword" 
          type="password" 
          placeholder="请确认密码" 
          class="input"
        />
      </view>
      
      <button class="register-btn" @tap="handleRegister" :loading="loading">注册账号</button>
      
      <view class="login-link">
        <text>已有账号？</text>
        <text class="link" @tap="goToLogin">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      registerForm: {
        username: '',
        realName: '',
        phone: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      loading: false
    }
  },
  methods: {
    validateForm() {
      if (!this.registerForm.username) {
        uni.showToast({
          title: '请输入用户名',
          icon: 'none'
        })
        return false
      }
      if (!this.registerForm.realName) {
        uni.showToast({
          title: '请输入真实姓名',
          icon: 'none'
        })
        return false
      }
      if (!this.registerForm.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return false
      }
      if (!/^1[3-9]\d{9}$/.test(this.registerForm.phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        })
        return false
      }
      if (!this.registerForm.email) {
        uni.showToast({
          title: '请输入邮箱',
          icon: 'none'
        })
        return false
      }
      if (!/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(this.registerForm.email)) {
        uni.showToast({
          title: '请输入正确的邮箱',
          icon: 'none'
        })
        return false
      }
      if (!this.registerForm.password) {
        uni.showToast({
          title: '请输入密码',
          icon: 'none'
        })
        return false
      }
      if (this.registerForm.password.length < 6) {
        uni.showToast({
          title: '密码长度不能小于6位',
          icon: 'none'
        })
        return false
      }
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        uni.showToast({
          title: '两次输入的密码不一致',
          icon: 'none'
        })
        return false
      }
      return true
    },
    
    async handleRegister() {
      if (!this.validateForm()) return
      
      this.loading = true
      try {
        const res = await uni.request({
          url: 'http://localhost:8080/user/register',
          method: 'POST',
          data: {
            username: this.registerForm.username,
            realName: this.registerForm.realName,
            phone: this.registerForm.phone,
            email: this.registerForm.email,
            password: this.registerForm.password
          }
        })
        
        if (res.data && res.data.code === 1) {
          uni.showToast({
            title: '注册成功',
            icon: 'success'
          })
          
          // 跳转到登录页
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages/login/index'
            })
          }, 500)
        } else {
          uni.showToast({
            title: res.data.msg || '注册失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.showToast({
          title: '注册失败：' + (error.message || '网络错误'),
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    goToLogin() {
      uni.navigateTo({
        url: '/pages/login/index'
      })
    }
  }
}
</script>

<style>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
  padding: 40rpx 0;
}

.register-box {
  width: 80%;
  padding: 40rpx;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 30rpx rgba(0, 0, 0, 0.15);
}

.register-header {
  text-align: center;
  margin-bottom: 40rpx;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.subtitle {
  font-size: 28rpx;
  color: #666;
}

.form-item {
  margin-bottom: 30rpx;
}

.input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.register-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #409EFF;
  color: #fff;
  border-radius: 8rpx;
  font-size: 32rpx;
  margin-top: 20rpx;
}

.login-link {
  text-align: center;
  margin-top: 30rpx;
  font-size: 28rpx;
  color: #666;
}

.link {
  color: #409EFF;
  margin-left: 10rpx;
}
</style> 