<template>
  <div class="system-config-container">
    <h2 class="page-title">系统配置</h2>
    
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>基础配置</span>
        </div>
      </template>
      
      <el-form :model="configForm" label-width="120px">
        <el-form-item label="系统名称">
          <el-input v-model="configForm.systemName" placeholder="请输入系统名称" />
        </el-form-item>
        
        <el-form-item label="系统Logo">
          <el-upload
            class="logo-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleLogoChange">
            <img v-if="logoUrl" :src="logoUrl" class="logo-preview" />
            <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-hint">推荐尺寸: 200x60 像素</div>
        </el-form-item>
        
        <el-divider />
        
        <el-form-item label="预订审批">
          <el-switch v-model="configForm.requireApproval" />
          <span class="config-hint">开启后，所有会议室预订需要审批</span>
        </el-form-item>
        
        <el-form-item label="预订时间限制">
          <el-select v-model="configForm.bookingTimeLimit" placeholder="预订时间限制">
            <el-option label="不限制" value="none" />
            <el-option label="1天内" value="1day" />
            <el-option label="3天内" value="3days" />
            <el-option label="1周内" value="1week" />
            <el-option label="2周内" value="2weeks" />
            <el-option label="1个月内" value="1month" />
          </el-select>
          <span class="config-hint">限制用户只能预订未来多长时间内的会议室</span>
        </el-form-item>
        
        <el-form-item label="通知设置">
          <el-checkbox-group v-model="configForm.notificationSettings">
            <el-checkbox label="email">邮件通知</el-checkbox>
            <el-checkbox label="sms">短信通知</el-checkbox>
            <el-checkbox label="system">系统内通知</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>高级配置</span>
        </div>
      </template>
      
      <el-form :model="advancedForm" label-width="120px">
        <el-form-item label="系统维护模式">
          <el-switch v-model="advancedForm.maintenanceMode" />
          <span class="config-hint">开启后，除管理员外的用户将无法访问系统</span>
        </el-form-item>
        
        <el-form-item label="数据备份">
          <el-button type="primary">立即备份</el-button>
          <el-button>恢复备份</el-button>
        </el-form-item>
        
        <el-form-item label="缓存清理">
          <el-button type="warning">清理系统缓存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div class="action-buttons">
      <el-button>取消</el-button>
      <el-button type="primary" @click="saveConfig">保存配置</el-button>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

export default {
  name: 'SystemConfigPage',
  components: {
    Plus
  },
  setup() {
    const logoUrl = ref('');
    
    const configForm = reactive({
      systemName: '会议室预订系统',
      requireApproval: true,
      bookingTimeLimit: '2weeks',
      notificationSettings: ['email', 'system']
    });
    
    const advancedForm = reactive({
      maintenanceMode: false
    });
    
    const handleLogoChange = (file) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        logoUrl.value = e.target.result;
      };
      reader.readAsDataURL(file.raw);
    };
    
    const saveConfig = () => {
      ElMessage.success('配置保存成功');
      // 实际项目中这里应该调用API保存配置
    };
    
    return {
      logoUrl,
      configForm,
      advancedForm,
      handleLogoChange,
      saveConfig
    };
  }
};
</script>

<style scoped>
.system-config-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-title {
  margin-bottom: 24px;
  font-weight: 500;
  color: #303133;
}

.config-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-uploader {
  width: 200px;
  height: 60px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-uploader:hover {
  border-color: #409EFF;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.logo-preview {
  max-width: 100%;
  max-height: 100%;
}

.upload-hint, .config-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.config-hint {
  margin-left: 10px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 