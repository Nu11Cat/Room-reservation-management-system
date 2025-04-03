<template>
  <div class="statistics-container">
    <div class="page-header">
      <h1>会议室统计分析</h1>
    </div>
    
    <!-- 日期范围选择器 -->
    <el-card class="filter-card">
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
          >
            <template #prefix>
              <el-icon><Calendar /></el-icon>
            </template>
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAllStatistics">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 统计汇总卡片 -->
    <div v-else class="statistics-content">
      <el-row :gutter="20" class="summary-row">
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <el-card class="summary-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Calendar /></el-icon>
                <span>总预订数</span>
              </div>
            </template>
            <div class="card-body">
              <div class="summary-value">{{ summaryData.totalBookings || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <el-card class="summary-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><OfficeBuilding /></el-icon>
                <span>总会议室数</span>
              </div>
            </template>
            <div class="card-body">
              <div class="summary-value">{{ summaryData.totalRooms || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <el-card class="summary-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><User /></el-icon>
                <span>总用户数</span>
              </div>
            </template>
            <div class="card-body">
              <div class="summary-value">{{ summaryData.totalUsers || 0 }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 会议室使用率统计 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>会议室预订频率统计</span>
          </div>
        </template>
        <el-table :data="roomUsageData" style="width: 100%" border stripe :header-cell-style="tableHeaderStyle">
          <el-table-column prop="roomName" label="会议室名称" align="center" />
          <el-table-column prop="location" label="位置" align="center" />
          <el-table-column prop="capacity" label="容量" align="center" />
          <el-table-column prop="bookingCount" label="预约次数" align="center" />
          <el-table-column prop="usageRate" label="预订频率" align="center">
            <template #default="scope">
              <div class="percentage-display">
                <div>{{ scope.row.usageRate }}%</div>
                <el-progress 
                  :percentage="scope.row.usageRate" 
                  :color="getProgressColor(scope.row.usageRate)" 
                  :stroke-width="12"
                  :show-text="false"
                />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      
      <!-- 预订状态统计 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>预订状态统计</span>
          </div>
        </template>
        <div class="full-width-table">
          <el-table 
            :data="bookingStatusData" 
            border 
            stripe 
            style="width: 100%"
            :header-cell-style="tableHeaderStyle"
          >
            <el-table-column prop="statusName" label="状态" align="center" />
            <el-table-column prop="count" label="数量" align="center" />
            <el-table-column prop="percentage" label="占比" align="center">
              <template #default="scope">
                <div class="percentage-display">
                  <div>{{ scope.row.percentage }}%</div>
                  <el-progress 
                    :percentage="scope.row.percentage" 
                    :color="getProgressColor(scope.row.percentage)"
                    :stroke-width="12"
                    :show-text="false"
                  />
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
      
      <!-- 用户预订排名 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>用户预订排名</span>
          </div>
        </template>
        <el-table 
          :data="userRankingData" 
          style="width: 100%" 
          border 
          stripe 
          :header-cell-style="tableHeaderStyle"
        >
          <el-table-column type="index" label="排名" width="80" align="center" />
          <el-table-column prop="username" label="用户名" align="center" />
          <el-table-column prop="realName" label="真实姓名" align="center" />
          <el-table-column prop="bookingCount" label="预订次数" align="center" />
          <el-table-column label="占比" align="center">
            <template #default="scope">
              <div class="percentage-display">
                <div>{{ calculatePercentage(scope.row.bookingCount).toFixed(1) }}%</div>
                <el-progress 
                  :percentage="calculatePercentage(scope.row.bookingCount)" 
                  :color="getProgressColor(calculatePercentage(scope.row.bookingCount))" 
                  :stroke-width="12"
                  :show-text="false"
                />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      
      <!-- 会议时长统计 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>会议时长统计</span>
          </div>
        </template>
        <div class="full-width-table">
          <el-table 
            :data="meetingDurationData" 
            border 
            stripe 
            style="width: 100%"
            :header-cell-style="tableHeaderStyle"
          >
            <el-table-column prop="durationRange" label="时长范围" align="center" />
            <el-table-column prop="count" label="数量" align="center" />
            <el-table-column prop="percentage" label="占比" align="center">
              <template #default="scope">
                <div class="percentage-display">
                  <div>{{ scope.row.percentage }}%</div>
                  <el-progress 
                    :percentage="scope.row.percentage" 
                    :color="getProgressColor(scope.row.percentage)"
                    :stroke-width="12"
                    :show-text="false"
                  />
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue';
import { Refresh, Calendar, OfficeBuilding, User } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

import {
  getAllStatistics
} from '@/api/statistics';

export default {
  name: 'StatisticsPage',
  components: {
    Refresh,
    Calendar,
    OfficeBuilding,
    User
  },
  setup() {
    const loading = ref(false);
    const dateRange = ref([]);
    
    // 统计数据
    const roomUsageData = ref([]);
    const bookingStatusData = ref([]);
    const userRankingData = ref([]);
    const meetingDurationData = ref([]);
    const summaryData = ref({
      totalBookings: 0,
      totalRooms: 0,
      totalUsers: 0
    });
    
    // 表格表头样式
    const tableHeaderStyle = reactive({
      background: '#f5f7fa',
      color: '#606266',
      fontWeight: 'bold',
      textAlign: 'center'
    });
    
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
      loadAllStatistics();
    };
    
    // 获取进度条颜色
    const getProgressColor = (percentage) => {
      if (percentage < 30) return '#909399';
      if (percentage < 70) return '#67C23A';
      return '#F56C6C';
    };
    
    // 计算用户预订占比
    const calculatePercentage = (count) => {
      if (!roomUsageData.value.length) return 0;
      const totalBookings = summaryData.value.totalBookings || 0;
      if (totalBookings === 0) return 0;
      return (count / totalBookings) * 100;
    };
    
    // 加载所有统计数据
    const loadAllStatistics = async () => {
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
        
        // 获取所有统计数据
        const res = await getAllStatistics(params);
        
        if (res.code === 1) {
          const data = res.data || {};
          
          // 更新各项统计数据
          roomUsageData.value = data.roomUsage || [];
          bookingStatusData.value = data.bookingStatus || [];
          userRankingData.value = data.userRanking || [];
          meetingDurationData.value = data.meetingDuration || [];
          
          // 更新汇总数据
          summaryData.value = {
            totalBookings: data.totalBookings || 0,
            totalRooms: data.totalRooms || 0,
            totalUsers: data.totalUsers || 0
          };
          
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
      loadAllStatistics();
    });
    
    return {
      loading,
      dateRange,
      roomUsageData,
      bookingStatusData,
      userRankingData,
      meetingDurationData,
      summaryData,
      tableHeaderStyle,
      handleDateChange,
      loadAllStatistics,
      getProgressColor,
      calculatePercentage
    };
  }
};
</script>

<style scoped>
.statistics-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.statistics-content {
  margin-top: 20px;
}

.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  height: auto;
  transition: all 0.3s;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.summary-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.card-body {
  padding: 20px;
}

.summary-value {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
}

.chart-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chart-wrapper {
  padding: 0;
}

.chart-row {
  margin: 0 !important;
}

.chart-col {
  padding: 10px;
}

.chart-container {
  height: 350px;
  width: 100%;
  display: block !important;
  position: relative;
}

.table-container {
  height: 350px;
  width: 100%;
  overflow: auto;
}

.empty-chart {
  height: 350px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-content {
  padding: 10px;
}

@media (max-width: 768px) {
  .chart-col {
    margin-bottom: 20px;
  }
  
  .summary-card {
    margin-bottom: 20px;
  }
}

/* 新增样式 */
.full-width-table {
  width: 100%;
  padding: 20px;
}

.percentage-display {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.percentage-display .el-progress {
  margin-top: 5px;
}

/* 修改卡片标题样式 */
.card-header span {
  font-weight: bold;
  font-size: 16px;
}
</style> 