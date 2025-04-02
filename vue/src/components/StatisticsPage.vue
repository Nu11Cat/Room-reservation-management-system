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
          <el-button type="primary" @click="loadAllStatistics">
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
    
    <!-- 统计汇总卡片 -->
    <div v-else class="statistics-content">
      <div class="summary-cards">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="summary-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><Calendar /></el-icon>
                  <span>总预订数</span>
                </div>
              </template>
              <div class="summary-value">{{ summaryData.totalBookings || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="summary-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>总会议室数</span>
                </div>
              </template>
              <div class="summary-value">{{ summaryData.totalRooms || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="summary-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><User /></el-icon>
                  <span>总用户数</span>
                </div>
              </template>
              <div class="summary-value">{{ summaryData.totalUsers || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 会议室使用率统计 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>会议室预订频率统计</span>
          </div>
        </template>
        <el-table :data="roomUsageData" style="width: 100%" border stripe>
          <el-table-column prop="roomName" label="会议室名称" />
          <el-table-column prop="location" label="位置" />
          <el-table-column prop="capacity" label="容量" />
          <el-table-column prop="bookingCount" label="预约次数" />
          <el-table-column prop="usageRate" label="预订频率">
            <template #default="scope">
              <el-progress 
                :percentage="scope.row.usageRate" 
                :format="(p) => p + '%'" 
                :color="getProgressColor(scope.row.usageRate)" 
              />
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
        <el-row>
          <el-col :span="12">
            <div v-if="bookingStatusData.length === 0" class="empty-chart">
              <el-empty description="暂无状态统计数据" />
            </div>
            <div v-else class="chart-container" ref="bookingStatusChart"></div>
          </el-col>
          <el-col :span="12">
            <el-table :data="bookingStatusData" style="width: 100%" border stripe>
              <el-table-column prop="statusName" label="状态" />
              <el-table-column prop="count" label="数量" />
              <el-table-column prop="percentage" label="占比">
                <template #default="scope">
                  {{ scope.row.percentage }}%
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 用户预订排名 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>用户预订排名</span>
          </div>
        </template>
        <el-table :data="userRankingData" style="width: 100%" border stripe>
          <el-table-column type="index" label="排名" width="80" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="realName" label="真实姓名" />
          <el-table-column prop="bookingCount" label="预订次数" />
          <el-table-column label="占比">
            <template #default="scope">
              <el-progress 
                :percentage="calculatePercentage(scope.row.bookingCount)" 
                :format="(p) => p.toFixed(1) + '%'" 
                :color="getProgressColor(calculatePercentage(scope.row.bookingCount))" 
              />
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
        <el-row>
          <el-col :span="12">
            <div v-if="meetingDurationData.length === 0" class="empty-chart">
              <el-empty description="暂无时长统计数据" />
            </div>
            <div v-else class="chart-container" ref="meetingDurationChart"></div>
          </el-col>
          <el-col :span="12">
            <el-table :data="meetingDurationData" style="width: 100%" border stripe>
              <el-table-column prop="durationRange" label="时长范围" />
              <el-table-column prop="count" label="数量" />
              <el-table-column prop="percentage" label="占比">
                <template #default="scope">
                  {{ scope.row.percentage }}%
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, onUnmounted } from 'vue';
import { Refresh, Calendar, OfficeBuilding, User } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
// 按需导入ECharts
import * as echarts from 'echarts/core';
import { PieChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 注册必需的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  PieChart,
  CanvasRenderer
]);

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
    
    // 图表引用
    const bookingStatusChart = ref(null);
    const meetingDurationChart = ref(null);
    
    // 图表实例
    let statusChart = null;
    let durationChart = null;
    
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
    
    // 初始化状态分布图表
    const initStatusChart = () => {
      if (!bookingStatusChart.value || bookingStatusData.value.length === 0) return;
      
      if (statusChart) {
        statusChart.dispose();
      }
      
      // 确保DOM元素渲染完成并有高度
      nextTick(() => {
        // 强制等待DOM渲染完成
        setTimeout(() => {
          if (!bookingStatusChart.value) return;
          
          try {
            statusChart = echarts.init(bookingStatusChart.value);
            
            const statusData = bookingStatusData.value.map(item => ({
              name: item.statusName,
              value: item.count
            }));
            
            const option = {
              tooltip: {
                trigger: 'item',
                formatter: '{a} <br/>{b}: {c} ({d}%)'
              },
              legend: {
                orient: 'vertical',
                left: 10,
                data: statusData.map(item => item.name)
              },
              series: [
                {
                  name: '预订状态',
                  type: 'pie',
                  radius: ['50%', '70%'],
                  avoidLabelOverlap: false,
                  label: {
                    show: false,
                    position: 'center'
                  },
                  emphasis: {
                    label: {
                      show: true,
                      fontSize: '16',
                      fontWeight: 'bold'
                    }
                  },
                  labelLine: {
                    show: false
                  },
                  data: statusData
                }
              ]
            };
            
            statusChart.setOption(option);
            
            // 强制重新计算大小
            statusChart.resize();
          } catch (error) {
            console.error('初始化状态图表出错:', error);
          }
        }, 100);
      });
    };
    
    // 初始化会议时长图表
    const initDurationChart = () => {
      if (!meetingDurationChart.value || meetingDurationData.value.length === 0) return;
      
      if (durationChart) {
        durationChart.dispose();
      }
      
      // 确保DOM元素渲染完成并有高度
      nextTick(() => {
        // 强制等待DOM渲染完成
        setTimeout(() => {
          if (!meetingDurationChart.value) return;
          
          try {
            durationChart = echarts.init(meetingDurationChart.value);
            
            const durationData = meetingDurationData.value.map(item => ({
              name: item.durationRange,
              value: item.count
            }));
            
            const option = {
              tooltip: {
                trigger: 'item',
                formatter: '{a} <br/>{b}: {c} ({d}%)'
              },
              legend: {
                orient: 'vertical',
                left: 10,
                data: durationData.map(item => item.name)
              },
              series: [
                {
                  name: '会议时长',
                  type: 'pie',
                  radius: ['50%', '70%'],
                  avoidLabelOverlap: false,
                  label: {
                    show: false,
                    position: 'center'
                  },
                  emphasis: {
                    label: {
                      show: true,
                      fontSize: '16',
                      fontWeight: 'bold'
                    }
                  },
                  labelLine: {
                    show: false
                  },
                  data: durationData
                }
              ]
            };
            
            durationChart.setOption(option);
            
            // 强制重新计算大小
            durationChart.resize();
          } catch (error) {
            console.error('初始化时长图表出错:', error);
          }
        }, 100);
      });
    };
    
    // 清理图表实例
    const disposeCharts = () => {
      if (statusChart) {
        statusChart.dispose();
        statusChart = null;
      }
      if (durationChart) {
        durationChart.dispose();
        durationChart = null;
      }
      
      // 移除resize监听
      window.removeEventListener('resize', handleResize);
    };
    
    // 处理窗口大小变化
    const handleResize = () => {
      if (statusChart) statusChart.resize();
      if (durationChart) durationChart.resize();
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
          
          // 初始化图表
          await nextTick();
          initStatusChart();
          initDurationChart();
          
          // 添加resize监听
          window.addEventListener('resize', handleResize);
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
    
    // 添加组件卸载时的清理函数
    onUnmounted(() => {
      disposeCharts();
    });
    
    return {
      loading,
      dateRange,
      roomUsageData,
      bookingStatusData,
      userRankingData,
      meetingDurationData,
      summaryData,
      bookingStatusChart,
      meetingDurationChart,
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

.summary-cards {
  margin-bottom: 20px;
}

.summary-card {
  height: 120px;
  transition: all 0.3s;
}

.summary-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.summary-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
  margin-top: 10px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 350px !important;
  width: 100% !important;
  min-height: 350px;
  position: relative !important;
  display: block !important;
}

.empty-chart {
  height: 350px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style> 