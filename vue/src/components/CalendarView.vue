<template>
  <div class="calendar-container">
    <div class="calendar-header">
      <div class="header-left">
        <el-button-group>
          <el-button type="primary" @click="today">今天</el-button>
          <el-button plain @click="changeDate(-1)">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-button plain @click="changeDate(1)">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </el-button-group>
        <h2 class="current-date">{{ currentMonthYear }}</h2>
      </div>
      <div class="header-right">
        <el-radio-group v-model="viewType" size="small">
          <el-radio-button label="day">日</el-radio-button>
          <el-radio-button label="week">周</el-radio-button>
          <el-radio-button label="month">月</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <div class="calendar-body">
      <!-- 日视图 -->
      <div v-if="viewType === 'day'" class="day-view">
        <div class="time-column">
          <div v-for="hour in 24" :key="hour-1" class="time-slot">
            {{ formatHour(hour-1) }}
          </div>
        </div>
        <div class="day-column">
          <div class="day-header">{{ formatDate(currentDate) }}</div>
          <div class="day-events">
            <div v-for="hour in 24" :key="hour-1" class="hour-slot" @click="openBookingDialog(currentDate, hour-1)">
              <div 
                v-for="event in getEventsForHour(currentDate, hour-1)" 
                :key="event.id" 
                class="event-item"
                :class="getEventStatusClass(event)"
                @click.stop="openEventDetails(event)"
              >
                {{ event.title }}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 周视图 -->
      <div v-else-if="viewType === 'week'" class="week-view">
        <div class="time-column">
          <div class="day-header"></div>
          <div v-for="hour in 24" :key="hour-1" class="time-slot">
            {{ formatHour(hour-1) }}
          </div>
        </div>
        <div v-for="(day, index) in weekDays" :key="index" class="day-column">
          <div class="day-header">
            <div class="weekday">{{ formatWeekday(day) }}</div>
            <div class="date">{{ formatDay(day) }}</div>
          </div>
          <div class="day-events">
            <div v-for="hour in 24" :key="hour-1" class="hour-slot" @click="openBookingDialog(day, hour-1)">
              <div 
                v-for="event in getEventsForHour(day, hour-1)" 
                :key="event.id" 
                class="event-item"
                :class="getEventStatusClass(event)"
                @click.stop="openEventDetails(event)"
              >
                {{ event.title }}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 月视图 -->
      <div v-else class="month-view">
        <div class="weekday-header">
          <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
        </div>
        <div class="month-grid">
          <div 
            v-for="(day, index) in monthDays" 
            :key="index" 
            class="day-cell"
            :class="{ 'current-month': day.currentMonth, 'today': isToday(day.date) }"
            @click="selectDay(day.date)"
          >
            <div class="date-number">{{ day.day }}</div>
            <div class="day-events-mini">
              <div 
                v-for="event in getEventsForDay(day.date).slice(0, 3)" 
                :key="event.id" 
                class="event-dot"
                :class="getEventStatusClass(event)"
                @click.stop="openEventDetails(event)"
              >
                {{ event.title }}
              </div>
              <div v-if="getEventsForDay(day.date).length > 3" class="more-events">
                +{{ getEventsForDay(day.date).length - 3 }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 预订对话框 -->
    <el-dialog 
      :title="bookingMode === 'create' ? '预订会议室' : '会议详情'" 
      v-model="bookingDialogVisible" 
      width="500px"
    >
      <div v-if="bookingMode === 'view'" class="event-details">
        <h3>{{ selectedEvent.title }}</h3>
        <p><strong>时间:</strong> {{ formatEventTime(selectedEvent) }}</p>
        <p><strong>会议室:</strong> {{ selectedEvent.roomName }}</p>
        <p><strong>预订人:</strong> {{ selectedEvent.username }}</p>
        <p><strong>状态:</strong> {{ getStatusText(selectedEvent.status) }}</p>
        <p v-if="selectedEvent.description"><strong>说明:</strong> {{ selectedEvent.description }}</p>
        <div class="event-actions">
          <el-button 
            v-if="canCancelBooking(selectedEvent)" 
            type="danger" 
            @click="cancelBooking(selectedEvent)"
          >
            取消预订
          </el-button>
        </div>
      </div>
      <el-form v-else :model="bookingForm" :rules="bookingRules" ref="bookingFormRef">
        <el-form-item label="会议室" prop="roomId">
          <el-select v-model="bookingForm.roomId" placeholder="请选择会议室" filterable>
            <el-option 
              v-for="room in availableRooms" 
              :key="room.id" 
              :label="room.name" 
              :value="room.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker 
            v-model="bookingForm.date" 
            type="date" 
            placeholder="选择日期"
            format="YYYY/MM/DD"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlot">
          <el-select v-model="bookingForm.timeSlot" placeholder="请选择时间段">
            <el-option 
              v-for="(slot, index) in timeSlots" 
              :key="index" 
              :label="slot.label" 
              :value="slot.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="会议主题" prop="title">
          <el-input v-model="bookingForm.title" />
        </el-form-item>
        <el-form-item label="会议说明" prop="description">
          <el-input 
            v-model="bookingForm.description" 
            type="textarea" 
            rows="3" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div v-if="bookingMode !== 'view'">
          <el-button @click="bookingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBooking" :loading="submitLoading">提交预订</el-button>
        </div>
      </template>
    </el-dialog>
    
  </div>
</template>

<script>
import { ref, computed, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';

dayjs.locale('zh-cn');

export default {
  name: 'CalendarViewComponent',
  components: {
    ArrowLeft,
    ArrowRight
  },
  props: {
    events: {
      type: Array,
      default: () => []
    },
    rooms: {
      type: Array,
      default: () => []
    },
    userId: {
      type: Number,
      default: 0
    },
    userRole: {
      type: Number,
      default: 2
    }
  },
  emits: ['book-room', 'cancel-booking', 'fetch-events'],
  setup(props, { emit }) {
    const currentDate = ref(new Date());
    const viewType = ref('week');
    const bookingDialogVisible = ref(false);
    const bookingMode = ref('create');
    const selectedEvent = ref(null);
    const bookingFormRef = ref(null);
    const submitLoading = ref(false);
    
    // 预订表单
    const bookingForm = reactive({
      roomId: '',
      date: null,
      timeSlot: '',
      title: '',
      description: ''
    });
    
    const bookingRules = {
      roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
      date: [{ required: true, message: '请选择日期', trigger: 'change' }],
      timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
      title: [{ required: true, message: '请输入会议主题', trigger: 'blur' }]
    };
    
    // 星期几的中文名称
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    
    // 当前月份和年份
    const currentMonthYear = computed(() => {
      const date = dayjs(currentDate.value);
      return date.format('YYYY年 M月');
    });
    
    // 获取当前周的天数
    const weekDays = computed(() => {
      const date = dayjs(currentDate.value);
      const weekStart = date.startOf('week');
      const days = [];
      
      for (let i = 0; i < 7; i++) {
        days.push(weekStart.add(i, 'day').toDate());
      }
      
      return days;
    });
    
    // 获取当前月的天数网格
    const monthDays = computed(() => {
      const date = dayjs(currentDate.value);
      const firstDayOfMonth = date.startOf('month');
      const lastDayOfMonth = date.endOf('month');
      const daysInMonth = lastDayOfMonth.date();
      
      const firstDayOfWeek = firstDayOfMonth.day(); // 0 = Sunday, 1 = Monday, etc.
      const lastDayOfWeek = lastDayOfMonth.day();
      
      const days = [];
      
      // 上个月的最后几天
      const prevMonth = firstDayOfMonth.subtract(1, 'month');
      const daysInPrevMonth = prevMonth.daysInMonth();
      
      for (let i = 0; i < firstDayOfWeek; i++) {
        days.push({
          day: daysInPrevMonth - firstDayOfWeek + i + 1,
          date: prevMonth.date(daysInPrevMonth - firstDayOfWeek + i + 1).toDate(),
          currentMonth: false
        });
      }
      
      // 当前月的天数
      for (let i = 1; i <= daysInMonth; i++) {
        days.push({
          day: i,
          date: date.date(i).toDate(),
          currentMonth: true
        });
      }
      
      // 下个月的前几天
      const nextMonth = lastDayOfMonth.add(1, 'month');
      for (let i = 1; i < 7 - lastDayOfWeek; i++) {
        days.push({
          day: i,
          date: nextMonth.date(i).toDate(),
          currentMonth: false
        });
      }
      
      return days;
    });
    
    // 可选时间段
    const timeSlots = [
      { label: '08:00-09:00', value: '8-9' },
      { label: '09:00-10:00', value: '9-10' },
      { label: '10:00-11:00', value: '10-11' },
      { label: '11:00-12:00', value: '11-12' },
      { label: '13:00-14:00', value: '13-14' },
      { label: '14:00-15:00', value: '14-15' },
      { label: '15:00-16:00', value: '15-16' },
      { label: '16:00-17:00', value: '16-17' },
      { label: '17:00-18:00', value: '17-18' }
    ];
    
    // 可用会议室
    const availableRooms = computed(() => {
      return props.rooms;
    });
    
    // 日期和时间格式化
    const formatDate = (date) => {
      return dayjs(date).format('YYYY年MM月DD日');
    };
    
    const formatDay = (date) => {
      return dayjs(date).format('D');
    };
    
    const formatWeekday = (date) => {
      return weekdays[dayjs(date).day()];
    };
    
    const formatHour = (hour) => {
      return `${hour.toString().padStart(2, '0')}:00`;
    };
    
    const formatEventTime = (event) => {
      if (!event) return '';
      
      const date = dayjs(event.bookingDate).format('YYYY-MM-DD');
      const [start, end] = event.timeSlot.split('-');
      return `${date} ${start}:00-${end}:00`;
    };
    
    // 检查是否是今天
    const isToday = (date) => {
      return dayjs(date).format('YYYY-MM-DD') === dayjs().format('YYYY-MM-DD');
    };
    
    // 日期导航
    const today = () => {
      currentDate.value = new Date();
      fetchEvents();
    };
    
    const changeDate = (direction) => {
      const date = dayjs(currentDate.value);
      
      if (viewType.value === 'day') {
        currentDate.value = date.add(direction, 'day').toDate();
      } else if (viewType.value === 'week') {
        currentDate.value = date.add(direction, 'week').toDate();
      } else {
        currentDate.value = date.add(direction, 'month').toDate();
      }
      
      fetchEvents();
    };
    
    // 选择某一天
    const selectDay = (date) => {
      currentDate.value = date;
      viewType.value = 'day';
      fetchEvents();
    };
    
    // 获取特定日期和小时的事件
    const getEventsForHour = (date, hour) => {
      const dateStr = dayjs(date).format('YYYY-MM-DD');
      
      return props.events.filter(event => {
        if (dayjs(event.bookingDate).format('YYYY-MM-DD') !== dateStr) {
          return false;
        }
        
        const [start, end] = event.timeSlot.split('-').map(Number);
        return hour >= start && hour < end;
      });
    };
    
    // 获取特定日期的所有事件
    const getEventsForDay = (date) => {
      const dateStr = dayjs(date).format('YYYY-MM-DD');
      
      return props.events.filter(event => {
        return dayjs(event.bookingDate).format('YYYY-MM-DD') === dateStr;
      });
    };
    
    // 获取事件状态的CSS类
    const getEventStatusClass = (event) => {
      const statusMap = {
        0: 'status-pending',
        1: 'status-approved',
        2: 'status-rejected',
        3: 'status-canceled'
      };
      
      return statusMap[event.status] || '';
    };
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '待审批',
        1: '已通过',
        2: '已拒绝',
        3: '已取消'
      };
      
      return statusMap[status] || '未知状态';
    };
    
    // 打开预订对话框
    const openBookingDialog = (date, hour) => {
      bookingMode.value = 'create';
      bookingForm.date = date;
      bookingForm.timeSlot = `${hour}-${hour+1}`;
      bookingDialogVisible.value = true;
    };
    
    // 打开事件详情
    const openEventDetails = (event) => {
      selectedEvent.value = event;
      bookingMode.value = 'view';
      bookingDialogVisible.value = true;
    };
    
    // 提交预订
    const submitBooking = () => {
      bookingFormRef.value.validate(async (valid) => {
        if (valid) {
          submitLoading.value = true;
          
          try {
            const booking = {
              roomId: bookingForm.roomId,
              bookingDate: dayjs(bookingForm.date).format('YYYY-MM-DD'),
              timeSlot: bookingForm.timeSlot,
              title: bookingForm.title,
              description: bookingForm.description,
              userId: props.userId || 0
            };
            
            // 触发预订事件，由父组件处理API调用
            emit('book-room', booking);
            
            bookingDialogVisible.value = false;
            ElMessage.success('预订提交成功，等待审批');
            
            // 重置表单
            bookingForm.roomId = '';
            bookingForm.date = null;
            bookingForm.timeSlot = '';
            bookingForm.title = '';
            bookingForm.description = '';
            
            if (bookingFormRef.value) {
              bookingFormRef.value.resetFields();
            }
          } catch (error) {
            ElMessage.error('预订提交失败');
          } finally {
            submitLoading.value = false;
          }
        }
      });
    };
    
    // 检查是否可以取消预订
    const canCancelBooking = (event) => {
      if (!event) return false;
      
      // 只有待审批或已通过的预订可以取消
      if (event.status !== 0 && event.status !== 1) {
        return false;
      }
      
      // 只有预订者或管理员可以取消
      const userRole = props.userRole;
      const userId = props.userId;
      
      return userRole === 1 || event.userId === userId;
    };
    
    // 取消预订
    const cancelBooking = (event) => {
      ElMessageBox.confirm('确定要取消此预订吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 触发取消预订事件，由父组件处理API调用
        emit('cancel-booking', event.id);
        bookingDialogVisible.value = false;
      }).catch(() => {});
    };
    
    // 获取事件数据
    const fetchEvents = () => {
      let startDate, endDate;
      
      if (viewType.value === 'day') {
        startDate = dayjs(currentDate.value).format('YYYY-MM-DD');
        endDate = startDate;
      } else if (viewType.value === 'week') {
        startDate = dayjs(currentDate.value).startOf('week').format('YYYY-MM-DD');
        endDate = dayjs(currentDate.value).endOf('week').format('YYYY-MM-DD');
      } else {
        startDate = dayjs(currentDate.value).startOf('month').format('YYYY-MM-DD');
        endDate = dayjs(currentDate.value).endOf('month').format('YYYY-MM-DD');
      }
      
      // 触发获取事件事件，由父组件处理API调用
      emit('fetch-events', { startDate, endDate });
    };
    
    onMounted(() => {
      fetchEvents();
    });
    
    return {
      currentDate,
      viewType,
      weekdays,
      weekDays,
      monthDays,
      currentMonthYear,
      bookingDialogVisible,
      bookingMode,
      selectedEvent,
      bookingForm,
      bookingRules,
      bookingFormRef,
      submitLoading,
      timeSlots,
      availableRooms,
      today,
      changeDate,
      selectDay,
      formatDate,
      formatDay,
      formatWeekday,
      formatHour,
      formatEventTime,
      isToday,
      getEventsForHour,
      getEventsForDay,
      getEventStatusClass,
      getStatusText,
      openBookingDialog,
      openEventDetails,
      submitBooking,
      canCancelBooking,
      cancelBooking
    };
  }
};
</script>

<style scoped>
.calendar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
}

.header-left {
  display: flex;
  align-items: center;
}

.current-date {
  margin: 0 16px;
  font-size: 20px;
  font-weight: 500;
}

.calendar-body {
  flex: 1;
  overflow: auto;
  position: relative;
}

/* 日视图 */
.day-view {
  display: flex;
  height: 100%;
  min-height: 600px;
}

.time-column {
  width: 60px;
  border-right: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

.time-slot {
  height: 50px;
  padding: 0 8px;
  text-align: right;
  font-size: 12px;
  color: #909399;
  box-sizing: border-box;
  border-bottom: 1px solid #ebeef5;
}

.day-column {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.day-header {
  height: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  font-weight: 500;
  background-color: #f5f7fa;
}

.day-events {
  flex: 1;
  position: relative;
}

.hour-slot {
  height: 50px;
  border-bottom: 1px solid #ebeef5;
  box-sizing: border-box;
  position: relative;
}

.weekday {
  font-weight: bold;
}

.date {
  font-size: 14px;
  color: #606266;
}

/* 周视图 */
.week-view {
  display: flex;
  height: 100%;
  min-height: 600px;
}

.week-view .day-column {
  flex: 1;
  min-width: 120px;
  border-right: 1px solid #ebeef5;
}

.week-view .day-column:last-child {
  border-right: none;
}

/* 月视图 */
.month-view {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.weekday-header {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

.weekday-header .weekday {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-weight: 500;
}

.month-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-auto-rows: minmax(100px, 1fr);
}

.day-cell {
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  padding: 8px;
  position: relative;
  background-color: #fff;
}

.day-cell:nth-child(7n) {
  border-right: none;
}

.day-cell.current-month {
  background-color: #fff;
}

.day-cell:not(.current-month) {
  background-color: #f9f9f9;
  color: #c0c4cc;
}

.day-cell.today {
  background-color: #ecf5ff;
}

.date-number {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.day-events-mini {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.event-dot {
  height: 20px;
  border-radius: 4px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 4px;
  background-color: #95a5a6;
  color: #fff;
}

.more-events {
  font-size: 12px;
  color: #409eff;
  text-align: center;
}

/* 事件项 */
.event-item {
  position: absolute;
  left: 2px;
  right: 2px;
  top: 2px;
  height: calc(100% - 4px);
  padding: 4px 8px;
  background-color: #95a5a6;
  color: #fff;
  border-radius: 4px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  z-index: 1;
}

/* 事件状态 */
.status-pending {
  background-color: #e67e22;
}

.status-approved {
  background-color: #27ae60;
}

.status-rejected {
  background-color: #e74c3c;
}

.status-canceled {
  background-color: #7f8c8d;
}

/* 事件详情 */
.event-details {
  padding: 16px;
}

.event-details h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #303133;
}

.event-actions {
  margin-top: 20px;
  text-align: right;
}
</style> 