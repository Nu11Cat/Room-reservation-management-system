<template>
  <div class="profile-container">
    <h1>个人中心</h1>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="primary" size="small" @click="handleEditInfo">修改信息</el-button>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ userInfo.userId }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ getRoleName(userInfo.roleId) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ userInfo.createTime }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
                {{ userInfo.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="修改密码" name="password">
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
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="个人设置" name="settings">
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
              <el-button type="primary" @click="handleSaveSettings" :loading="settingsLoading">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 编辑用户信息对话框 -->
    <el-dialog title="编辑个人信息" v-model="dialogVisible" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUserForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

export default {
  name: 'UserProfilePage',
  setup() {
    const userStore = useUserStore();
    const activeTab = ref('basic');
    
    // 用户信息
    const userInfo = computed(() => userStore.userInfo || {});
    
    // 编辑用户信息相关
    const dialogVisible = ref(false);
    const submitLoading = ref(false);
    const userFormRef = ref(null);
    
    const userForm = reactive({
      username: '',
      email: ''
    });
    
    const userRules = {
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ]
    };
    
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
    
    // 获取角色名称
    const getRoleName = (roleId) => {
      const roleMap = {
        1: '管理员',
        2: '普通用户',
        3: '审批人'
      };
      return roleMap[roleId] || '未知角色';
    };
    
    // 加载用户设置
    const loadUserSettings = async () => {
      try {
        // 这里将来替换为API调用
        // 模拟从API获取设置
        settingsForm.emailNotification = true;
        settingsForm.theme = 'light';
      } catch (error) {
        console.error('获取用户设置失败:', error);
      }
    };
    
    onMounted(() => {
      loadUserSettings();
    });
    
    // 编辑个人信息
    const handleEditInfo = () => {
      userForm.username = userInfo.value.username || '';
      userForm.email = userInfo.value.email || '';
      dialogVisible.value = true;
    };
    
    // 提交用户表单
    const submitUserForm = () => {
      userFormRef.value.validate(async (valid) => {
        if (valid) {
          submitLoading.value = true;
          try {
            // 这里将来替换为API调用
            await new Promise(resolve => setTimeout(resolve, 500));
            ElMessage.success('个人信息修改成功');
            dialogVisible.value = false;
            // 更新存储的用户信息
            // userStore.setUserInfo({ ...userInfo.value, ...userForm });
          } catch (error) {
            ElMessage.error('个人信息修改失败');
          } finally {
            submitLoading.value = false;
          }
        }
      });
    };
    
    // 修改密码
    const handleChangePassword = () => {
      passwordFormRef.value.validate(async (valid) => {
        if (valid) {
          passwordLoading.value = true;
          try {
            // 这里将来替换为API调用
            await new Promise(resolve => setTimeout(resolve, 500));
            ElMessage.success('密码修改成功');
            resetPasswordForm();
          } catch (error) {
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
        // 这里将来替换为API调用
        await new Promise(resolve => setTimeout(resolve, 500));
        ElMessage.success('设置保存成功');
      } catch (error) {
        ElMessage.error('设置保存失败');
      } finally {
        settingsLoading.value = false;
      }
    };
    
    return {
      activeTab,
      userInfo,
      dialogVisible,
      submitLoading,
      userForm,
      userRules,
      userFormRef,
      passwordLoading,
      passwordForm,
      passwordRules,
      passwordFormRef,
      settingsLoading,
      settingsForm,
      settingsFormRef,
      getRoleName,
      handleEditInfo,
      submitUserForm,
      handleChangePassword,
      resetPasswordForm,
      handleSaveSettings
    };
  }
};
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.info-card,
.password-card,
.settings-card {
  margin-top: 20px;
  max-width: 800px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-descriptions {
  margin: 20px 0;
}
</style> 