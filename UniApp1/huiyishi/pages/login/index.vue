<template>
  <view class="login-container">
    <view class="login-box">
      <view class="login-header">
        <text class="title">会议室预约系统</text>
        <text class="subtitle">欢迎回来，请登录您的账号</text>
      </view>
      
      <view class="form-item">
        <input 
          v-model="loginForm.username" 
          placeholder="请输入用户名" 
          class="input"
        />
      </view>
      
      <view class="form-item">
        <input 
          v-model="loginForm.password" 
          type="password" 
          placeholder="请输入密码" 
          class="input"
        />
      </view>
      
      <button class="login-btn" @tap="handleLogin" :loading="loading">登录系统</button>
      
      <view class="register-link">
        <text>还没有账号？</text>
        <text class="link" @tap="goToRegister">立即注册</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        uni.showToast({
          title: '请输入用户名和密码',
          icon: 'none'
        })
        return
      }
      
      this.loading = true
      try {
        const res = await uni.request({
          url: 'http://localhost:8080/user/login',
          method: 'POST',
          data: this.loginForm
        })
        
        if (res.data && res.data.code === 1) {
          // 保存token
          uni.setStorageSync('token', res.data.msg)
          
          // 解析token获取用户信息
          const tokenParts = res.data.msg.split('.')
          if (tokenParts.length === 3) {
            const payload = JSON.parse(atob(tokenParts[1]))
            const userInfo = {
              id: payload.userId,
              username: payload.username,
              roleId: payload.roleId
            }
            uni.setStorageSync('userInfo', userInfo)
          }
          
          uni.showToast({
            title: '登录成功',
            icon: 'success'
          })
          
          // 跳转到首页
          setTimeout(() => {
            uni.switchTab({
              url: '/pages/index/index'
            })
          }, 500)
        } else {
          uni.showToast({
            title: res.data.msg || '登录失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.showToast({
          title: '登录失败：' + (error.message || '网络错误'),
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    goToRegister() {
      uni.navigateTo({
        url: '/pages/register/index'
      })
    }
  }
}
</script>

<style>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
}

.login-box {
  width: 80%;
  padding: 40rpx;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 30rpx rgba(0, 0, 0, 0.15);
}

.login-header {
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

.login-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #409EFF;
  color: #fff;
  border-radius: 8rpx;
  font-size: 32rpx;
  margin-top: 20rpx;
}

.register-link {
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