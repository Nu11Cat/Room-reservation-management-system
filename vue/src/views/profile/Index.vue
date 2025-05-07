<template>
  <div class="profile-container">
    <div class="page-loading-animation"></div>
    <div class="page-header">
      <h1>个人中心</h1>
      <div class="header-subtitle">管理您的个人信息和账户设置</div>
    </div>
    
    <div class="profile-content">
      <!-- 左侧基本信息 -->
      <el-card class="info-card" shadow="hover">
        <div class="card-decoration"></div>
        <template #header>
          <div class="card-header">
            <span><i class="el-icon-user"></i> 基本信息</span>
          </div>
        </template>
        <div class="info-sections">
          <!-- 基本身份信息 -->
          <div class="info-section">
            <h3 class="section-title"><i class="el-icon-user-solid"></i> 身份信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
              <el-descriptions-item label="用户ID">{{ userInfo.id }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ getRoleName(userInfo.roleId) }}</el-descriptions-item>
              <el-descriptions-item label="身份">{{ userInfo.identity || '学生' }}</el-descriptions-item>
              <el-descriptions-item label="状态" :span="2">
                <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
                  {{ userInfo.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 个人资料 -->
          <div class="info-section">
            <h3 class="section-title"><i class="el-icon-notebook-1"></i> 个人资料</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="真实姓名">
                <div class="edit-field">
                  <span>{{ userInfo.realName || '未设置' }}</span>
                  <el-input v-if="editingField === 'realName'" v-model="userForm.realName" placeholder="请输入真实姓名" />
                  <el-button 
                    v-if="editingField !== 'realName'" 
                    type="primary" 
                    size="small" 
                    @click="startEditing('realName')"
                    class="edit-button"
                  >
                    <i class="el-icon-edit"></i> 修改
                  </el-button>
                  <div v-else class="action-buttons">
                    <el-button type="success" size="small" @click="saveField('realName')">保存</el-button>
                    <el-button size="small" @click="cancelEditing">取消</el-button>
                  </div>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                <div class="edit-field">
                  <span>{{ userInfo.email || '未设置' }}</span>
                  <el-input v-if="editingField === 'email'" v-model="userForm.email" placeholder="请输入邮箱" />
                  <el-button 
                    v-if="editingField !== 'email'" 
                    type="primary" 
                    size="small" 
                    @click="startEditing('email')"
                    class="edit-button"
                  >
                    <i class="el-icon-edit"></i> 修改
                  </el-button>
                  <div v-else class="action-buttons">
                    <el-button type="success" size="small" @click="saveField('email')">保存</el-button>
                    <el-button size="small" @click="cancelEditing">取消</el-button>
                  </div>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="电话">
                <div class="edit-field">
                  <span>{{ userInfo.phone || '未设置' }}</span>
                  <el-input v-if="editingField === 'phone'" v-model="userForm.phone" placeholder="请输入电话" />
                  <el-button 
                    v-if="editingField !== 'phone'" 
                    type="primary" 
                    size="small" 
                    @click="startEditing('phone')"
                    class="edit-button"
                  >
                    <i class="el-icon-edit"></i> 修改
                  </el-button>
                  <div v-else class="action-buttons">
                    <el-button type="success" size="small" @click="saveField('phone')">保存</el-button>
                    <el-button size="small" @click="cancelEditing">取消</el-button>
                  </div>
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 信誉积分 -->
          <div class="info-section credit-section">
            <h3 class="section-title"><i class="el-icon-medal"></i> 信誉积分</h3>
            <div class="credit-score-card">
              <div class="credit-score-value">
                <el-tag size="large" :type="getCreditScoreType(userInfo.creditScore)">
                  {{ userInfo.creditScore || 100 }}
                </el-tag>
                <div class="credit-score-label">{{ getCreditScoreLabel(userInfo.creditScore) }}</div>
              </div>
              <div class="credit-score-bar">
                <div class="credit-score-progress" :style="{width: `${(userInfo.creditScore || 100)}%`, backgroundColor: getCreditScoreColor(userInfo.creditScore)}"></div>
              </div>
            </div>
          </div>
          
          <!-- 时间信息 -->
          <div class="info-section">
            <h3 class="section-title"><i class="el-icon-time"></i> 时间信息</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="创建时间">{{ formatDateTime(userInfo.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatDateTime(userInfo.updateTime) }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
      
      <!-- 右侧功能区域 -->
      <div class="right-section">
        <!-- 修改密码部分 -->
        <el-card class="password-card" shadow="hover">
          <div class="card-decoration"></div>
          <template #header>
            <div class="card-header">
              <span><i class="el-icon-lock"></i> 修改密码</span>
            </div>
          </template>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <div class="form-buttons">
                <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading" class="primary-button">修改密码</el-button>
                <el-button @click="resetPasswordForm" class="reset-button">重置</el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 个人设置部分 -->
        <el-card class="settings-card" shadow="hover">
          <div class="card-decoration"></div>
          <template #header>
            <div class="card-header">
              <span><i class="el-icon-setting"></i> 个人设置</span>
            </div>
          </template>
          <el-form :model="settingsForm" ref="settingsFormRef" label-width="120px">
            <el-form-item label="通知设置">
              <el-switch
                v-model="settingsForm.emailNotification"
                active-text="接收邮件通知"
                inactive-text="不接收邮件通知"
              />
            </el-form-item>
            <el-form-item label="界面主题">
              <el-radio-group v-model="settingsForm.theme">
                <el-radio label="light">浅色</el-radio>
                <el-radio label="dark">深色</el-radio>
                <el-radio label="system">跟随系统</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <div class="form-buttons">
                <el-button type="primary" @click="handleSaveSettings" :loading="settingsLoading" class="primary-button">保存设置</el-button>
                <el-button @click="resetSettings" class="reset-button">重置</el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { updateUserInfo, updatePassword, getUserInfo } from '@/api/user';

export default {
  name: 'UserProfile',
  setup() {
    const userStore = useUserStore();
    
    // 用户信息
    const userInfo = computed(() => userStore.userInfo || {});
    
    // 编辑状态相关
    const editingField = ref(''); // 当前正在编辑的字段
    const submitLoading = ref(false);
    
    // 用户表单数据
    const userForm = reactive({
      username: '',
      email: '',
      phone: '',
      realName: ''
    });
    
    // 修改密码相关
    const passwordLoading = ref(false);
    const passwordFormRef = ref(null);
    
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'));
            } else {
              callback();
            }
          }, 
          trigger: 'blur' 
        }
      ]
    };
    
    // 个人设置相关
    const settingsLoading = ref(false);
    const settingsFormRef = ref(null);
    
    const settingsForm = reactive({
      emailNotification: true,
      theme: 'light'
    });
    
    // 初始化用户表单数据
    const initUserForm = () => {
      userForm.username = userInfo.value.username || '';
      userForm.email = userInfo.value.email || '';
      userForm.phone = userInfo.value.phone || '';
      userForm.realName = userInfo.value.realName || '';
    };
    
    // 获取角色名称
    const getRoleName = (roleId) => {
      const roleMap = {
        1: '管理员',
        2: '普通用户',
        3: '审批人'
      };
      return roleMap[roleId] || '未知角色';
    };
    
    // 获取信誉积分标签类型
    const getCreditScoreType = (score) => {
      if (!score && score !== 0) return 'success';
      if (score >= 90) return 'success';
      if (score >= 70) return 'warning';
      return 'danger';
    };
    
    // 获取信誉积分颜色
    // eslint-disable-next-line no-unused-vars
    const getCreditScoreColor = (score) => {
      if (!score && score !== 0) return '#67c23a';
      if (score >= 90) return '#67c23a';
      if (score >= 70) return '#e6a23c';
      return '#f56c6c';
    };
    
    // 获取信誉积分标签文本
    const getCreditScoreLabel = (score) => {
      if (!score && score !== 0) return '信誉优秀';
      if (score >= 90) return '信誉优秀';
      if (score >= 70) return '信誉良好';
      return '信誉不佳';
    };
    
    // 格式化日期时间
    const formatDateTime = (datetime) => {
      if (!datetime) return '未知';
      return datetime.replace('T', ' ').substring(0, 16);
    };
    
    // 加载用户设置
    const loadUserSettings = () => {
      try {
        // 从localStorage加载设置
        const savedSettings = localStorage.getItem('userSettings');
        if (savedSettings) {
          const settings = JSON.parse(savedSettings);
          settingsForm.emailNotification = settings.emailNotification !== undefined ? 
            settings.emailNotification : true;
          settingsForm.theme = settings.theme || 'light';
          
          // 应用主题设置
          applyTheme(settingsForm.theme);
        }
      } catch (error) {
        console.error('获取用户设置失败:', error);
      }
    };
    
    // 应用主题
    const applyTheme = (theme) => {
      let finalTheme = theme;
      
      if (theme === 'system') {
        // 检查系统主题
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        finalTheme = prefersDark ? 'dark' : 'light';
      }
      
      // 应用主题样式
      document.documentElement.setAttribute('data-theme', finalTheme);
    };
    
    // 重置设置
    const resetSettings = () => {
      settingsForm.emailNotification = true;
      settingsForm.theme = 'light';
      applyTheme('light');
    };
    
    // 刷新用户信息
    const refreshUserInfo = async () => {
      try {
        console.log('开始获取用户信息...');
        const res = await getUserInfo();
        console.log('获取用户信息响应:', res);
        
        if (res.code === 1) {
          // 更新用户信息
          userStore.setUserInfo(res.data);
          // 更新表单数据
          initUserForm();
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
      }
    };
    
    // 开始编辑字段
    const startEditing = (field) => {
      editingField.value = field;
      // 初始化对应字段的值
      userForm[field] = userInfo.value[field] || '';
    };
    
    // 取消编辑
    const cancelEditing = () => {
      editingField.value = '';
    };
    
    // 保存字段
    const saveField = async (field) => {
      submitLoading.value = true;
      try {
        // 构建更新数据
        const updateData = {
          id: userInfo.value.id,
          username: userInfo.value.username
        };
        
        // 添加正在编辑的字段
        updateData[field] = userForm[field];
        
        console.log('保存字段数据:', updateData);
        const res = await updateUserInfo(updateData);
        console.log('保存字段响应:', res);
        
        if (res.code === 1) {
          ElMessage.success(`${field === 'email' ? '邮箱' : '电话'}修改成功`);
          
          // 更新用户信息
          userStore.setUserInfo({
            ...userInfo.value,
            [field]: userForm[field]
          });
          
          // 退出编辑模式
          editingField.value = '';
          
          // 刷新用户信息
          refreshUserInfo();
        } else {
          ElMessage.error(res.msg || `${field === 'email' ? '邮箱' : '电话'}修改失败`);
        }
      } catch (error) {
        console.error('保存字段失败:', error);
        ElMessage.error(`${field === 'email' ? '邮箱' : '电话'}修改失败`);
      } finally {
        submitLoading.value = false;
      }
    };
    
    // 修改密码
    const handleChangePassword = () => {
      passwordFormRef.value.validate(async (valid) => {
        if (valid) {
          passwordLoading.value = true;
          try {
            // 打印调试信息
            console.log('开始修改密码...');
            const passwordData = {
              id: userInfo.value.id,
              username: userInfo.value.username,
              oldPassword: passwordForm.oldPassword,
              password: passwordForm.newPassword
            };
            console.log('发送的密码数据:', passwordData);
            
            // 添加更多必要的字段
            const res = await updatePassword(passwordData);
            console.log('修改密码响应:', res);
            
            if (res.code === 1) {
              ElMessage.success('密码修改成功');
              resetPasswordForm();
            } else {
              ElMessage.error(res.msg || '密码修改失败');
            }
          } catch (error) {
            console.error('修改密码失败:', error);
            ElMessage.error('密码修改失败');
          } finally {
            passwordLoading.value = false;
          }
        }
      });
    };
    
    // 重置密码表单
    const resetPasswordForm = () => {
      passwordForm.oldPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
      if (passwordFormRef.value) {
        passwordFormRef.value.resetFields();
      }
    };
    
    // 保存设置
    const handleSaveSettings = async () => {
      settingsLoading.value = true;
      try {
        // 保存设置到localStorage
        localStorage.setItem('userSettings', JSON.stringify({
          emailNotification: settingsForm.emailNotification,
          theme: settingsForm.theme
        }));
        
        // 应用主题设置
        applyTheme(settingsForm.theme);
        
        ElMessage.success('设置保存成功');
      } catch (error) {
        console.error('保存设置失败:', error);
        ElMessage.error('设置保存失败');
      } finally {
        settingsLoading.value = false;
      }
    };
    
    onMounted(() => {
      loadUserSettings();
      refreshUserInfo();
      initUserForm();
    });
    
    return {
      userInfo,
      editingField,
      submitLoading,
      userForm,
      passwordLoading,
      passwordForm,
      passwordRules,
      passwordFormRef,
      settingsLoading,
      settingsForm,
      settingsFormRef,
      
      getRoleName,
      formatDateTime,
      getCreditScoreType,
      getCreditScoreColor,
      getCreditScoreLabel,
      startEditing,
      cancelEditing,
      saveField,
      handleChangePassword,
      resetPasswordForm,
      handleSaveSettings,
      resetSettings,
      refreshUserInfo
    };
  }
};
</script>

<style scoped>
:root {
  --primary-color: #409eff;
  --primary-light: #ecf5ff;
  --primary-dark: #337ecc;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --border-color: #dcdfe6;
  --border-light: #e4e7ed;
  --bg-color: #f5f7fa;
  --card-shadow: 0 8px 20px rgba(0, 0, 0, 0.07);
  --card-shadow-hover: 0 12px 24px rgba(0, 0, 0, 0.12);
  --transition-normal: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.profile-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: var(--bg-color);
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 32px;
  border-bottom: 2px solid var(--primary-light);
  padding-bottom: 20px;
  position: relative;
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 80px;
  height: 2px;
  background-color: var(--primary-color);
}

.page-header h1 {
  font-size: 28px;
  color: var(--text-primary);
  margin: 0;
  font-weight: 700;
  letter-spacing: 0.5px;
  position: relative;
  display: inline-block;
}

.header-subtitle {
  color: var(--text-secondary);
  font-size: 16px;
  margin-top: 8px;
  font-weight: 400;
}

.profile-content {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  width: 100%;
}

.info-card {
  flex: 3;
  min-width: 500px;
  transition: var(--transition-normal);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--card-shadow);
  background-color: #fff;
  border: 1px solid var(--border-light);
  position: relative;
}

.info-sections {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
  padding: 16px;
}

@media (max-width: 1200px) {
  .profile-content {
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .info-sections {
    grid-template-columns: 1fr;
  }
  
  .credit-section {
    grid-column: auto;
  }
  
  .profile-content {
    flex-direction: column;
  }
}

.info-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.password-card,
.settings-card {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  transition: var(--transition-normal);
  box-shadow: var(--card-shadow);
  background-color: #fff;
  border: 1px solid var(--border-light);
  position: relative;
  padding: 0;
}

.el-form-item {
  margin-bottom: 18px;
}

.password-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle at top right, rgba(103, 194, 58, 0.2), transparent 70%);
  opacity: 0.6;
  z-index: 0;
}

.settings-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle at top right, rgba(230, 162, 60, 0.2), transparent 70%);
  opacity: 0.6;
  z-index: 0;
}

.password-card:hover::before,
.settings-card:hover::before {
  opacity: 0.8;
}

.password-card:hover,
.settings-card:hover,
.info-card:hover {
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  font-size: 18px;
  padding: 16px 0;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 12px;
  position: relative;
  z-index: 2;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header span i {
  font-size: 20px;
  color: var(--primary-color);
}

.edit-field {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.edit-field span {
  color: var(--text-regular);
  font-size: 15px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.el-descriptions {
  margin: 20px 0;
}

.el-descriptions-item__label {
  font-weight: 600;
  color: var(--text-primary);
}

.form-buttons {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.edit-button {
  transition: var(--transition-normal);
  border-radius: 6px;
}

.edit-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

/* 自定义标签样式 */
.el-tag {
  border-radius: 16px;
  padding: 0 12px;
  height: 28px;
  line-height: 26px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 信誉积分样式 */
.credit-score-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.credit-score-bar {
  width: 100%;
  max-width: 400px;
  height: 12px;
  background-color: #eee;
  border-radius: 6px;
  overflow: hidden;
  margin-top: 16px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.credit-score-progress {
  height: 100%;
  border-radius: 6px;
  transition: width 0.8s cubic-bezier(0.22, 1, 0.36, 1);
}

/* 自定义输入框样式 */
.el-input__inner {
  border-radius: 8px;
}

/* 自定义按钮样式 */
.el-button {
  border-radius: 8px;
  font-weight: 500;
  letter-spacing: 0.5px;
  transition: var(--transition-normal);
}

.el-button--primary:not(.is-disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(64, 158, 255, 0.2);
}

.el-button--success:not(.is-disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(103, 194, 58, 0.2);
}

.primary-button {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  border: none;
  position: relative;
  overflow: hidden;
}

.primary-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: all 0.6s;
}

.primary-button:hover::before {
  left: 100%;
}

.reset-button {
  border: 1px solid var(--border-color);
  background-color: white;
  transition: all 0.3s;
}

.reset-button:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
  background-color: var(--primary-light);
}

/* 加载动画 */
.page-loading-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 4px;
  height: 4px;
  background-color: var(--primary-color);
  animation: loading-bar 1.5s cubic-bezier(0.65, 0.05, 0.36, 1) forwards;
  z-index: 1000;
}

@keyframes loading-bar {
  0% {
    width: 0;
    opacity: 1;
  }
  90% {
    width: 100%;
    opacity: 1;
  }
  100% {
    width: 100%;
    opacity: 0;
  }
}

/* 卡片内容动画 */
.info-card, .password-card, .settings-card {
  animation: fade-in-up 0.6s ease-out forwards;
}

.password-card {
  animation-delay: 0.1s;
}

.settings-card {
  animation-delay: 0.2s;
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .profile-container {
    padding: 20px;
  }
  
  .profile-content {
    flex-direction: column;
    gap: 24px;
  }
  
  .info-card,
  .right-section {
    min-width: 100%;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
}
@media (max-width: 1200px) {
  .profile-content {
    gap: 20px;
  }
  
  .info-card,
  .right-section {
    min-width: 100%;
  }
}

.info-section:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: var(--primary-color);
}

.credit-section {
  grid-column: 1 / -1;
}

.credit-score-card {
  padding: 20px;
  background: linear-gradient(to right, #f8f9fa, #e9ecef);
  border-radius: 12px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 8px;
}

.credit-score-value {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.credit-score-label {
  margin-top: 8px;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-regular);
}

.credit-score-value .el-tag {
  font-size: 24px;
  padding: 8px 16px;
  font-weight: bold;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.card-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color), var(--primary-dark));
  z-index: 1;
}

.info-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle at top right, var(--primary-light), transparent 70%);
  opacity: 0.6;
  z-index: 0;
}

.info-card:hover::before {
  opacity: 0.8;
}

.right-section {
  flex: 2;
  min-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
</style>