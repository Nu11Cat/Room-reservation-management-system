<template>
  <div class="system-config-container">
    <h1>系统配置</h1>
    
    <el-form :model="configForm" label-width="180px" class="config-form">
      <el-divider content-position="left">预约规则配置 (BOOKING_RULE)</el-divider>
      
      <el-form-item label="预约冲突规则配置">
        <el-input 
          v-model="configForm.conflictRule" 
          placeholder="冲突规则配置"
          :disabled="true"
        />
        <div class="rule-desc">是否允许重叠: 否，最小间隔: 30分钟</div>
      </el-form-item>
      
      <el-form-item label="最小预约间隔(分钟)">
        <el-input-number 
          v-model="configForm.minInterval" 
          :min="5" 
          :max="60" 
          :step="5"
          :disabled="true"
        />
      </el-form-item>
      
      <el-form-item label="最大预约时长(分钟)">
        <el-input-number 
          v-model="configForm.maxDuration" 
          :min="60" 
          :max="720" 
          :step="30"
          :disabled="true"
        />
      </el-form-item>
      
      <el-divider content-position="left">审批规则配置 (APPROVAL_RULE)</el-divider>
      
      <el-form-item label="是否需要审批">
        <el-switch 
          v-model="configForm.approvalRequired" 
          :disabled="true"
        />
      </el-form-item>
      
      <el-form-item label="是否自动审批">
        <el-switch 
          v-model="configForm.autoApprove" 
          :disabled="true"
        />
      </el-form-item>
      
      <el-form-item label="审批结果是否邮件通知">
        <el-switch 
          v-model="configForm.notifyEmail" 
          :disabled="true" 
        />
      </el-form-item>
      
      <el-divider content-position="left">系统参数配置 (SYSTEM_PARAM)</el-divider>
      
      <el-form-item label="系统名称">
        <el-input 
          v-model="configForm.systemName" 
          placeholder="系统名称"
          :disabled="true"
        />
      </el-form-item>
      
      <el-form-item label="系统Logo">
        <el-input 
          v-model="configForm.systemLogo" 
          placeholder="系统Logo路径"
          :disabled="true"
        />
        <div class="logo-preview">
          <img src="/static/images/logo.png" alt="系统Logo" height="40" />
        </div>
      </el-form-item>
      
      <el-form-item label="邮件服务器地址">
        <el-input 
          v-model="configForm.emailServer" 
          placeholder="邮件服务器地址"
          :disabled="true"
        />
      </el-form-item>
      
      <el-form-item label="邮件服务器端口">
        <el-input-number 
          v-model="configForm.emailPort" 
          :min="1" 
          :max="65535"
          :disabled="true"
        />
      </el-form-item>
      
      <el-divider content-position="left">通知模板配置 (NOTIFY_TEMPLATE)</el-divider>
      
      <el-form-item label="审批提交通知模板">
        <el-input 
          type="textarea" 
          v-model="configForm.approvalTemplate" 
          :rows="2"
          :disabled="true"
        />
      </el-form-item>
      
      <el-form-item label="审批通过通知模板">
        <el-input 
          type="textarea" 
          v-model="configForm.approvedTemplate" 
          :rows="2"
          :disabled="true"
        />
      </el-form-item>
      
      <el-form-item label="审批拒绝通知模板">
        <el-input 
          type="textarea" 
          v-model="configForm.rejectedTemplate" 
          :rows="2"
          :disabled="true"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" disabled>保存配置</el-button>
        <el-button disabled>重置</el-button>
        <div class="config-hint">当前为展示模式，暂不支持修改配置</div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

export default {
  name: 'SystemConfigPage',
  setup() {
    const loading = ref(false);
    const configForm = reactive({
      // 预约规则配置
      conflictRule: '{"allowOverlap": false, "minInterval": 30}',
      minInterval: 15,
      maxDuration: 480,
      
      // 审批规则配置
      approvalRequired: true,
      autoApprove: false,
      notifyEmail: true,
      
      // 系统参数配置
      systemName: '会议室预约管理系统',
      systemLogo: '/static/images/logo.png',
      emailServer: 'smtp.example.com',
      emailPort: 465,
      
      // 通知模板配置
      approvalTemplate: '您的会议室预约申请已提交，等待审批',
      approvedTemplate: '您的会议室预约申请已通过审批',
      rejectedTemplate: '您的会议室预约申请未通过审批'
    });
    
    // 初始化加载配置
    const loadConfig = () => {
      loading.value = true;
      
      // 使用预设的配置数据（这里是只读展示，无实际API调用）
      setTimeout(() => {
        loading.value = false;
      }, 500);
    };
    
    // 保存配置
    const saveConfig = () => {
      // 展示模式下不实际保存
      ElMessage.info('当前为展示模式，不支持修改配置');
    };
    
    // 重置配置
    const resetConfig = () => {
      // 展示模式下不实际重置
      ElMessage.info('当前为展示模式，不支持修改配置');
    };
    
    onMounted(() => {
      loadConfig();
    });
    
    return {
      loading,
      configForm,
      saveConfig,
      resetConfig
    };
  }
};
</script>

<style scoped>
.system-config-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.config-form {
  margin-top: 20px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-divider {
  margin: 20px 0;
}

.rule-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.logo-preview {
  margin-top: 10px;
}

.config-hint {
  font-size: 12px;
  color: #e6a23c;
  margin-top: 10px;
}
</style> 