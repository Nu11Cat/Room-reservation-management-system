<template>
  <div class="system-config-container">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <h2>系统配置管理</h2>
          <div class="operation-buttons">
            <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
            <el-button @click="resetConfig">重置</el-button>
            <el-button type="warning" @click="refreshConfig">刷新缓存</el-button>
          </div>
        </div>
      </template>
      
      <el-form :model="configForm" label-width="180px" class="config-form" v-loading="loading">
        <el-divider content-position="left">预约规则配置 (BOOKING_RULE)</el-divider>
        
        <el-form-item label="预约开始时间">
          <el-time-select
            v-model="configForm.bookingStartTime"
            :max="configForm.bookingEndTime"
            placeholder="选择开始时间"
            start="06:00"
            step="00:30"
            end="22:00"
          />
        </el-form-item>
        
        <el-form-item label="预约结束时间">
          <el-time-select
            v-model="configForm.bookingEndTime"
            :min="configForm.bookingStartTime"
            placeholder="选择结束时间"
            start="06:00"
            step="00:30"
            end="22:00"
          />
          <div class="rule-desc">系统使用固定的15分钟时间间隔</div>
        </el-form-item>
        
        <!-- 预约时间间隔已固定为15分钟，不再提供配置选项 -->
        
        <!-- 最小预约间隔已固定为15分钟，不再提供配置选项 -->
        
        <el-form-item label="最大预约时长">
          <el-radio-group v-model="configForm.bookingMaxDuration">
            <el-radio :label="60">1小时</el-radio>
            <el-radio :label="120">2小时</el-radio>
            <el-radio :label="180">3小时</el-radio>
            <el-radio :label="240">4小时</el-radio>
            <el-radio :label="360">6小时</el-radio>
            <el-radio :label="480">8小时</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="最大提前预约天数">
          <el-radio-group v-model="configForm.bookingMaxAdvanceDays">
            <el-radio :label="7">7天</el-radio>
            <el-radio :label="14">14天</el-radio>
            <el-radio :label="30">30天</el-radio>
            <el-radio :label="60">60天</el-radio>
            <el-radio :label="90">90天</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="预约取消时限">
          <el-radio-group v-model="configForm.bookingCancelLimit">
            <el-radio :label="0">不限制</el-radio>
            <el-radio :label="30">30分钟</el-radio>
            <el-radio :label="60">1小时</el-radio>
            <el-radio :label="120">2小时</el-radio>
            <el-radio :label="1440">24小时</el-radio>
          </el-radio-group>
          <div class="rule-desc">开始前多久可以取消预约，0表示不限制</div>
        </el-form-item>
        
        <el-divider content-position="left">审批规则配置 (APPROVAL_RULE)</el-divider>
        
        <el-form-item label="是否需要审批">
          <el-switch v-model="configForm.approvalRequired" />
          <div class="rule-desc">
            开启后，用户提交的会议室预订将需要管理员审批才能生效；关闭后，预订自动通过
          </div>
        </el-form-item>
        
        <el-divider content-position="left">系统参数配置 (SYSTEM_PARAM)</el-divider>
        
        <el-form-item label="系统名称">
          <el-input v-model="configForm.systemName" placeholder="系统名称" />
        </el-form-item>
        
        <el-form-item label="管理员联系方式">
          <el-input v-model="configForm.adminContact" placeholder="管理员联系方式" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { reactive, ref, onMounted, toRefs, onUnmounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useConfigStore } from '@/stores/config';
import { updateConfig } from '@/api/system';

export default {
  name: 'SystemConfigPage',
  setup() {
    const configStore = useConfigStore();
    const loading = ref(false);
    const saving = ref(false);
    
    // 表单数据，初始从store中获取
    const configForm = reactive({
      // 预约规则配置
      bookingStartTime: configStore.bookingStartTime,
      bookingEndTime: configStore.bookingEndTime,
      // 时间间隔和最小预约间隔固定为15分钟
      bookingTimeInterval: 15,
      bookingMaxDuration: configStore.bookingMaxDuration,
      bookingMinInterval: 15,
      bookingMaxAdvanceDays: configStore.bookingMaxAdvanceDays,
      bookingCancelLimit: configStore.bookingCancelLimit,
      
      // 审批规则配置
      approvalRequired: configStore.approvalRequired,
      
      // 系统参数配置
      systemName: configStore.systemName,
      adminContact: configStore.adminContact,
    });
    
    // 监听预约时间间隔变化，自动调整最小预约间隔
    watch(() => configForm.bookingTimeInterval, (newValue) => {
      // 如果最小预约间隔大于预约时间间隔，弹出提示
      if (configForm.bookingMinInterval > newValue) {
        ElMessage.info(`已将最小预约间隔从${configForm.bookingMinInterval}分钟调整为${newValue}分钟，以保持与预约时间间隔一致`);
        configForm.bookingMinInterval = newValue;
      }
    });
    
    // 初始化加载配置
    const loadConfig = async () => {
      loading.value = true;
      
      try {
        // 确保配置已初始化
        if (!configStore.initialized) {
          await configStore.initialize();
        }
        
        // 从store中刷新表单数据，但保留固定的时间间隔设置
        const storeData = toRefs(configStore);
        Object.keys(configForm).forEach(key => {
          // 跳过时间间隔和最小预约间隔
          if (key !== 'bookingTimeInterval' && key !== 'bookingMinInterval' && key in storeData) {
            configForm[key] = storeData[key].value;
          }
        });
      } catch (error) {
        console.error('加载配置失败:', error);
        ElMessage.error('加载配置失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 保存配置
    const saveConfig = async () => {
      saving.value = true;
      
      try {
        // 将配置转换为API所需格式
        const configUpdates = [
          { configKey: 'BOOKING_START_TIME', configValue: configForm.bookingStartTime, configType: 'BOOKING_RULE', description: '预约开始时间' },
          { configKey: 'BOOKING_END_TIME', configValue: configForm.bookingEndTime, configType: 'BOOKING_RULE', description: '预约结束时间' },
          { configKey: 'BOOKING_TIME_INTERVAL', configValue: '15', configType: 'BOOKING_RULE', description: '预约时间间隔' },
          { configKey: 'BOOKING_MAX_DURATION', configValue: configForm.bookingMaxDuration.toString(), configType: 'BOOKING_RULE', description: '最大预约时长' },
          { configKey: 'BOOKING_MIN_INTERVAL', configValue: '15', configType: 'BOOKING_RULE', description: '最小预约间隔' },
          { configKey: 'BOOKING_MAX_ADVANCE_DAYS', configValue: configForm.bookingMaxAdvanceDays.toString(), configType: 'BOOKING_RULE', description: '最大提前预约天数' },
          { configKey: 'BOOKING_CANCEL_LIMIT', configValue: configForm.bookingCancelLimit.toString(), configType: 'BOOKING_RULE', description: '预约取消时限' },
          { configKey: 'APPROVAL_REQUIRED', configValue: configForm.approvalRequired.toString(), configType: 'APPROVAL_RULE', description: '是否需要审批' },
          { configKey: 'SYSTEM_NAME', configValue: configForm.systemName, configType: 'SYSTEM_PARAM', description: '系统名称' },
          { configKey: 'ADMIN_CONTACT', configValue: configForm.adminContact, configType: 'SYSTEM_PARAM', description: '管理员联系方式' }
        ];
        
        // 由于逐个更新配置可能导致临时状态不一致，先在本地直接更新store
        for (const config of configUpdates) {
          configStore.updateConfigValue(config.configKey, config.configValue);
        }
        
        console.log('已直接更新本地配置Store');
        
        // 逐个更新配置到服务器
        let hasError = false;
        let successCount = 0;
        
        for (const config of configUpdates) {
          try {
            // 使用单个更新API
            const res = await updateConfig(config);
            if (res.code === 1) {
              console.log(`更新配置成功: ${config.configKey} = ${config.configValue}`);
              successCount++;
            } else {
              console.error(`更新配置 ${config.configKey} 失败:`, res.msg);
              hasError = true;
            }
          } catch (err) {
            console.error(`更新配置 ${config.configKey} 出错:`, err);
            hasError = true;
          }
        }
        
        if (hasError) {
          ElMessage.warning(`部分配置更新失败，已成功更新 ${successCount}/${configUpdates.length} 项，需要重启系统才能生效`);
        } else {
          // 显示重启提示
          ElMessageBox.alert(
            '配置保存成功！为确保配置立即生效，请刷新浏览器页面。', 
            '保存成功', 
            {
              confirmButtonText: '刷新页面',
              type: 'success',
              callback: () => {
                // 刷新页面
                window.location.reload();
              }
            }
          );
        }
        
        // 重新加载表单数据
        await loadConfig();
        
      } catch (error) {
        console.error('保存配置失败:', error);
        ElMessage.error('保存配置失败');
      } finally {
        saving.value = false;
      }
    };
    
    // 重置配置
    const resetConfig = () => {
      ElMessageBox.confirm('确定要重置所有修改吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        loadConfig();
        ElMessage.info('配置已重置');
      }).catch(() => {});
    };
    
    // 刷新缓存
    const refreshConfig = () => {
      ElMessageBox.confirm('确定要刷新系统配置缓存吗？这将从服务器重新加载所有配置。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        loading.value = true;
        try {
          await configStore.clearCache();
          await loadConfig();
          ElMessage.success('系统配置缓存已刷新');
        } catch (error) {
          console.error('刷新配置缓存失败:', error);
          ElMessage.error('刷新配置缓存失败');
        } finally {
          loading.value = false;
        }
      }).catch(() => {
        // 用户取消操作
      });
    };
    
    // 监听全局配置刷新事件
    function setupEventListeners() {
      document.addEventListener('config-refreshed', (event) => {
        console.log('收到配置刷新事件:', event);
        loadConfig();
      });
    }
    
    onMounted(() => {
      loadConfig();
      setupEventListeners();
    });
    
    onUnmounted(() => {
      document.removeEventListener('config-refreshed', loadConfig);
    });
    
    return {
      loading,
      saving,
      configForm,
      saveConfig,
      resetConfig,
      refreshConfig
    };
  }
};
</script>

<style scoped>
.system-config-container {
  padding: 20px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.operation-buttons {
  display: flex;
  gap: 10px;
}

.config-form {
  margin-top: 20px;
}

.el-divider {
  margin: 20px 0;
}

.rule-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 