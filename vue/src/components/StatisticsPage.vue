<template>
  <div class="statistics-container">
    <h1>会议室统计分析</h1>
    
    <!-- 日期范围选择器 -->
    <div class="filter-section">
      <el-form :inline="true" class="date-form">
        <el-form-item label="统计时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStatistics">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 统计数据展示 -->
    <div v-else class="statistics-content">
      <!-- 会议室使用率统计 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>会议室使用率统计</span>
          </div>
        </template>
        <el-table :data="roomUsageData" style="width: 100%" border stripe>
          <el-table-column prop="roomName" label="会议室名称" />
          <el-table-column prop="location" label="位置" />
          <el-table-column prop="capacity" label="容量" />
          <el-table-column prop="bookingCount" label="预约次数" />
          <el-table-column prop="usageRate" label="使用率">
            <template #default="scope">
              <el-progress :percentage="scope.row.usageRate" :format="(p) => p + '%'" />
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { Refresh } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import {
  getRoomUsageStatistics
} from '@/api/statistics';

export default {
  name: 'StatisticsPage',
  components: {
    Refresh
  },
  setup() {
    const loading = ref(false);
    const dateRange = ref([]);
    const roomUsageData = ref([]);
    
    // 初始化日期范围（最近30天）
    const initDateRange = () => {
      const end = new Date();
      const start = new Date();
      start.setDate(start.getDate() - 30);
      
      dateRange.value = [
        formatDate(start),
        formatDate(end)
      ];
    };
    
    // 格式化日期
    const formatDate = (date) => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    };
    
    // 处理日期变更
    const handleDateChange = () => {
      loadStatistics();
    };
    
    // 加载会议室使用率统计数据
    const loadStatistics = async () => {
      if (!dateRange.value || dateRange.value.length !== 2) {
        ElMessage.warning('请选择日期范围');
        return;
      }
      
      try {
        loading.value = true;
        
        const params = {
          startDate: dateRange.value[0],
          endDate: dateRange.value[1]
        };
        
        // 获取会议室使用率统计数据
        const res = await getRoomUsageStatistics(params);
        
        if (res.code === 1) {
          roomUsageData.value = res.data || [];
        } else {
          ElMessage.error(res.msg || '获取统计数据失败');
        }
      } catch (error) {
        console.error('获取统计数据失败:', error);
        ElMessage.error('获取统计数据失败');
      } finally {
        loading.value = false;
      }
    };
    
    onMounted(() => {
      initDateRange();
      loadStatistics();
    });
    
    return {
      loading,
      dateRange,
      roomUsageData,
      handleDateChange,
      loadStatistics
    };
  }
};
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.statistics-content {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-card {
  margin-bottom: 20px;
}
</style> 