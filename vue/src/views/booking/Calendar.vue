<template>
  <div class="calendar-container">
    <div class="page-header">
      <h1>我的预约日历</h1>
      <div class="calendar-controls">
        <el-button-group>
          <el-button @click="changeWeek(-1)">
            <el-icon><ArrowLeft /></el-icon> 上一周
          </el-button>
          <el-button @click="changeWeek(0)">本周</el-button>
          <el-button @click="changeWeek(1)">
            下一周 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </el-button-group>
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          @change="handleDateChange"
          format="YYYY年MM月DD日"
          value-format="YYYY-MM-DD"
        />
        <el-button type="primary" @click="goToBooking">新增预约</el-button>
      </div>
    </div>
    
    <el-card class="calendar-card" v-loading="loading">
      <div class="week-info">{{ weekRangeText }}</div>
      
      <!-- 课程表式日历视图 -->
      <div class="timetable-wrapper">
        <div class="timetable">
          <!-- 时间标题行 -->
          <div class="timetable-header">
            <div class="time-header-cell"></div>
            <div 
              v-for="time in timeSlots" 
              :key="time" 
              class="time-header-cell"
            >
              {{ time }}
            </div>
          </div>
          
          <!-- 日期行 -->
          <div 
            v-for="day in weekDays" 
            :key="day.date" 
            class="timetable-row"
            :class="{'current-day': isCurrentDay(day.date)}"
          >
            <!-- 日期单元格 -->
            <div class="date-cell">
              <div class="date-number">{{ day.dayOfMonth }}</div>
              <div class="date-day">{{ day.dayOfWeek }}</div>
            </div>
            
            <!-- 时间单元格 -->
            <div class="time-cells-container">
              <!-- 背景网格 -->
              <div 
                v-for="time in timeSlots" 
                :key="`grid-${day.date}-${time}`" 
                class="time-grid-cell"
              ></div>
              
              <!-- 预约块 -->
              <div 
                v-for="booking in getBookingsForDay(day.date)" 
                :key="booking.id" 
                class="booking-block"
                :class="getBookingStatusClass(booking)"
                :style="getBookingStyle(booking)"
                @click="handleBookingClick(booking)"
              >
                <div class="booking-title">{{ booking.title }}</div>
                <div class="booking-time">
                  {{ formatTime(booking.startTime) }} - {{ formatTime(booking.endTime) }}
                </div>
                <div class="booking-room">{{ getRoomName(booking.roomId) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 底部状态图例 -->
      <div class="status-legend">
        <div class="legend-item">
          <div class="color-block approved"></div>
          <span>已批准</span>
        </div>
        <div class="legend-item">
          <div class="color-block pending"></div>
          <span>待审批</span>
        </div>
        <div class="legend-item">
          <div class="color-block rejected"></div>
          <span>已拒绝</span>
        </div>
        <div class="legend-item">
          <div class="color-block cancelled"></div>
          <span>已取消</span>
        </div>
      </div>
    </el-card>
    
    <!-- 预约详情对话框 -->
    <el-dialog v-model="detailVisible" title="预约详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="会议室">{{ getRoomName(selectedBooking?.roomId) }}</el-descriptions-item>
        <el-descriptions-item label="会议主题">{{ selectedBooking?.title }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(selectedBooking?.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(selectedBooking?.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="参会人数">{{ selectedBooking?.attendees }}</el-descriptions-item>
        <el-descriptions-item label="会议描述">{{ selectedBooking?.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(selectedBooking?.status)">
            {{ getStatusText(selectedBooking?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(selectedBooking?.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(selectedBooking?.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button 
            type="danger" 
            @click="handleCancel(selectedBooking)"
            v-if="selectedBooking && (selectedBooking.status === 0 || selectedBooking.status === 1)"
          >取消预约</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import { getBookingList, cancelBooking, getBookingDetail } from '@/api/booking';
import { getRoomList } from '@/api/room';
import { useUserStore } from '@/stores/user';

export default {
  name: 'BookingCalendar',
  components: {
    ArrowLeft,
    ArrowRight
  },
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const loading = ref(false);
    const bookings = ref([]);
    const roomOptions = ref([]);
    const selectedDate = ref(formatDate(new Date()));
    const detailVisible = ref(false);
    const selectedBooking = ref(null);
    
    // 生成时间段（8:00 - 22:00，每小时一个时间段）
    const timeSlots = computed(() => {
      const slots = [];
      for (let hour = 8; hour <= 22; hour++) {
        slots.push(`${hour.toString().padStart(2, '0')}:00`);
      }
      return slots;
    });
    
    // 计算当前周的日期范围
    const weekDays = computed(() => {
      if (!selectedDate.value) return [];
      
      const days = [];
      const currentDate = new Date(selectedDate.value);
      const dayOfWeek = currentDate.getDay(); // 0-6, 0表示周日
      const diff = dayOfWeek === 0 ? 6 : dayOfWeek - 1; // 调整为周一为一周的第一天
      
      // 计算本周的第一天（周一）
      const firstDay = new Date(currentDate);
      firstDay.setDate(currentDate.getDate() - diff);
      
      // 生成本周7天的数据
      for (let i = 0; i < 7; i++) {
        const date = new Date(firstDay);
        date.setDate(firstDay.getDate() + i);
        
        days.push({
          date: formatDate(date),
          dayOfMonth: date.getDate(),
          dayOfWeek: getDayOfWeekText(date.getDay())
        });
      }
      
      return days;
    });
    
    // 显示周日期范围的文本
    const weekRangeText = computed(() => {
      if (weekDays.value.length === 0) return '';
      
      const firstDay = weekDays.value[0].date;
      const lastDay = weekDays.value[6].date;
      
      return `${firstDay} 至 ${lastDay}`;
    });
    
    // 格式化日期
    function formatDate(date) {
      if (!date) return '';
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      
      return `${year}-${month}-${day}`;
    }
    
    // 获取星期几的文本
    function getDayOfWeekText(day) {
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
      return days[day];
    }
    
    // 检查是否为当天
    function isCurrentDay(dateStr) {
      if (!dateStr) return false;
      const today = formatDate(new Date());
      return dateStr === today;
    }
    
    // 格式化时间（提取小时:分钟）
    function formatTime(datetime) {
      if (!datetime) return '';
      return datetime.substring(11, 16);
    }
    
    // 格式化日期时间
    function formatDateTime(datetime) {
      if (!datetime) return '';
      return datetime.replace('T', ' ').substring(0, 16);
    }
    
    // 获取指定日期的预约
    function getBookingsForDay(date) {
      return bookings.value.filter(booking => {
        const bookingDate = booking.startTime.substring(0, 10);
        return bookingDate === date;
      });
    }
    
    // 获取预约的样式（位置和宽度）
    function getBookingStyle(booking) {
      // 计算开始和结束的小时
      const startTime = booking.startTime.substring(11, 16);
      const endTime = booking.endTime.substring(11, 16);
      
      const [startHour, startMinute] = startTime.split(':').map(Number);
      const [endHour, endMinute] = endTime.split(':').map(Number);
      
      // 计算开始时间相对于8:00的偏移百分比
      const startOffset = (startHour - 8) + (startMinute / 60);
      
      // 计算持续时间（小时）
      const duration = (endHour - startHour) + ((endMinute - startMinute) / 60);
      
      // 每小时的宽度为100 / 15 = 6.67%
      const startPercent = startOffset * (100 / 15);
      const widthPercent = duration * (100 / 15);
      
      return {
        left: `${startPercent}%`,
        width: `${widthPercent}%`
      };
    }
    
    // 获取会议室名称
    function getRoomName(roomId) {
      if (!roomId) return '未知会议室';
      const room = roomOptions.value.find(r => r.id === roomId);
      return room ? room.name : '未知会议室';
    }
    
    // 获取预约状态的CSS类
    function getBookingStatusClass(booking) {
      switch (booking.status) {
        case 0: return 'status-pending';
        case 1: return 'status-approved';
        case 2: return 'status-rejected';
        case 3: return 'status-cancelled';
        default: return '';
      }
    }
    
    // 获取状态文本
    function getStatusText(status) {
      switch (status) {
        case 0: return '待审批';
        case 1: return '已批准';
        case 2: return '已拒绝';
        case 3: return '已取消';
        default: return '未知状态';
      }
    }
    
    // 获取状态类型
    function getStatusType(status) {
      switch (status) {
        case 0: return 'warning';
        case 1: return 'success';
        case 2: return 'danger';
        case 3: return 'info';
        default: return '';
      }
    }
    
    // 切换周
    function changeWeek(direction) {
      if (!selectedDate.value) return;
      
      const date = new Date(selectedDate.value);
      
      if (direction === 0) {
        // 本周
        selectedDate.value = formatDate(new Date());
      } else {
        // 上一周或下一周
        date.setDate(date.getDate() + (direction * 7));
        selectedDate.value = formatDate(date);
      }
      
      loadBookings();
    }
    
    // 处理日期变更
    function handleDateChange() {
      loadBookings();
    }
    
    // 处理预约点击
    async function handleBookingClick(booking) {
      try {
        loading.value = true;
        const res = await getBookingDetail(booking.id);
        
        if (res.code === 1) {
          selectedBooking.value = res.data;
          detailVisible.value = true;
        } else {
          ElMessage.error(res.msg || '获取预约详情失败');
        }
      } catch (error) {
        console.error('获取预约详情失败:', error);
        ElMessage.error('获取预约详情失败');
      } finally {
        loading.value = false;
      }
    }
    
    // 处理取消预约
    function handleCancel(booking) {
      ElMessageBox.confirm('确定要取消该预约吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          loading.value = true;
          const res = await cancelBooking(booking.id);
          
          if (res.code === 1) {
            ElMessage.success('取消预约成功');
            
            // 关闭详情对话框
            detailVisible.value = false;
            
            // 重新加载数据
            loadBookings();
          } else {
            ElMessage.error(res.msg || '取消预约失败');
          }
        } catch (error) {
          console.error('取消预约失败:', error);
          ElMessage.error('取消预约失败');
        } finally {
          loading.value = false;
        }
      }).catch(() => {});
    }
    
    // 跳转到预约页面
    function goToBooking() {
      router.push('/booking');
    }
    
    // 加载会议室数据
    async function loadRooms() {
      try {
        const res = await getRoomList({
          page: 1,
          size: 1000
        });
        
        if (res.code === 1 && res.data && Array.isArray(res.data.rows)) {
          roomOptions.value = res.data.rows;
        }
      } catch (error) {
        console.error('获取会议室列表失败:', error);
      }
    }
    
    // 加载当前用户的预约数据
    async function loadBookings() {
      if (!weekDays.value.length || !userStore.userInfo?.id) return;
      
      try {
        loading.value = true;
        
        const params = {
          page: 1,
          pageSize: 1000, // 获取足够多的数据
          begin: weekDays.value[0].date,
          end: weekDays.value[6].date,
          userId: userStore.userInfo.id // 只获取当前用户的预约
        };
        
        const res = await getBookingList(params);
        
        if (res.code === 1 && res.data && Array.isArray(res.data.rows)) {
          bookings.value = res.data.rows;
        } else {
          ElMessage.error(res.msg || '获取预约列表失败');
          bookings.value = [];
        }
      } catch (error) {
        console.error('获取预约列表失败:', error);
        ElMessage.error('获取预约列表失败');
        bookings.value = [];
      } finally {
        loading.value = false;
      }
    }
    
    onMounted(() => {
      loadRooms();
      loadBookings();
    });
    
    return {
      loading,
      selectedDate,
      timeSlots,
      weekDays,
      weekRangeText,
      detailVisible,
      selectedBooking,
      
      isCurrentDay,
      formatTime,
      formatDateTime,
      getBookingsForDay,
      getBookingStyle,
      getRoomName,
      getBookingStatusClass,
      getStatusText,
      getStatusType,
      
      changeWeek,
      handleDateChange,
      handleBookingClick,
      handleCancel,
      goToBooking
    };
  }
};
</script>

<style scoped>
.calendar-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.calendar-controls {
  display: flex;
  gap: 15px;
  align-items: center;
}

.calendar-card {
  margin-bottom: 20px;
}

.week-info {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  text-align: center;
}

.timetable-wrapper {
  overflow-x: auto;
}

.timetable {
  min-width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.timetable-header {
  display: flex;
  background-color: #f5f7fa;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
}

.time-header-cell {
  flex: 1;
  padding: 10px;
  text-align: center;
  min-width: 60px;
  border-right: 1px solid #ebeef5;
}

.time-header-cell:first-child {
  flex: 0 0 100px;
}

.timetable-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  position: relative;
  min-height: 80px;
}

.timetable-row:last-child {
  border-bottom: none;
}

.timetable-row.current-day {
  background-color: rgba(64, 158, 255, 0.1);
}

.date-cell {
  flex: 0 0 100px;
  padding: 10px;
  text-align: center;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.date-number {
  font-size: 18px;
  font-weight: bold;
}

.date-day {
  font-size: 14px;
  color: #909399;
}

.time-cells-container {
  flex: 1;
  position: relative;
  display: flex;
}

.time-grid-cell {
  flex: 1;
  border-right: 1px solid #ebeef5;
}

.time-grid-cell:last-child {
  border-right: none;
}

.booking-block {
  position: absolute;
  top: 5px;
  height: calc(100% - 10px);
  border-radius: 4px;
  padding: 5px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.booking-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.2);
}

.booking-title {
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.booking-time {
  font-size: 12px;
  margin-top: 3px;
}

.booking-room {
  font-size: 12px;
  color: #606266;
  margin-top: 3px;
}

.status-approved, .status-pending, .status-rejected, .status-cancelled {
  color: #fff;
}

.status-approved {
  background-color: #67c23a;
}

.status-pending {
  background-color: #e6a23c;
}

.status-rejected {
  background-color: #f56c6c;
}

.status-cancelled {
  background-color: #909399;
}

.status-legend {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.color-block {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.color-block.approved {
  background-color: #67c23a;
}

.color-block.pending {
  background-color: #e6a23c;
}

.color-block.rejected {
  background-color: #f56c6c;
}

.color-block.cancelled {
  background-color: #909399;
}
</style> 