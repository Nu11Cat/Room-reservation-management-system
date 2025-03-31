<template>
  <div class="profile-container">
    <div class="page-header">
      <h1>个人中心</h1>
    </div>
    
    <div class="profile-content">
      <!-- 左侧基本信息 -->
      <el-card class="info-card">
        <el-descriptions :column="1" border direction="vertical">
          <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ userInfo.id }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">
            <div class="edit-field">
              <span>{{ userInfo.email || '未设置' }}</span>
              <el-input v-if="editingField === 'email'" v-model="userForm.email" placeholder="请输入邮箱" />
              <el-button 
                v-if="editingField !== 'email'" 
                type="primary" 
                size="small" 
                @click="startEditing('email')"
              >
                修改
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
              >
                修改
              </el-button>
              <div v-else class="action-buttons">
                <el-button type="success" size="small" @click="saveField('phone')">保存</el-button>
                <el-button size="small" @click="cancelEditing">取消</el-button>
              </div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="角色">{{ getRoleName(userInfo.roleId) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(userInfo.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
              {{ userInfo.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- 右侧功能区域 -->
      <div class="right-section">
        <!-- 修改密码部分 -->
        <el-card class="password-card">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
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
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">修改密码</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 个人设置部分 -->
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>个人设置</span>
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
              <el-button type="primary" @click="handleSaveSettings" :loading="settingsLoading">保存设置</el-button>
              <el-button @click="resetSettings">重置</el-button>
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
      phone: ''
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
.profile-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.profile-content {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.info-card {
  flex: 1;
  min-width: 300px;
}

.right-section {
  flex: 1;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.password-card,
.settings-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-field {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.action-buttons {
  display: flex;
  gap: 5px;
}

.el-descriptions {
  margin: 20px 0;
}
</style> 