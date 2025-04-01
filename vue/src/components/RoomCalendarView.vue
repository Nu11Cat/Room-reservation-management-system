<template>
  <div class="room-calendar-container">
    <!-- 日历头部控制区 -->
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
        <h2 class="current-date">{{ formattedCurrentDate }}</h2>
      </div>
      <div class="header-controls">
        <el-select v-model="timeRange" placeholder="时间范围" style="width: 120px">
          <el-option label="上午" value="morning" />
          <el-option label="下午" value="afternoon" />
          <el-option label="全天" value="fullday" />
        </el-select>
        <el-select 
          v-model="capacityFilter" 
          placeholder="容量" 
          clearable
          style="width: 140px"
        >
          <el-option label="5人以下" :value="5" />
          <el-option label="5-10人" :value="10" />
          <el-option label="10-20人" :value="20" />
          <el-option label="20人以上" :value="30" />
        </el-select>
        <el-select 
          v-model="facilityFilter" 
          placeholder="设施" 
          multiple 
          collapse-tags
          style="width: 200px"
        >
          <el-option label="投影仪" value="projector" />
          <el-option label="视频会议" value="videoConference" />
          <el-option label="白板" value="whiteboard" />
          <el-option label="音响设备" value="audio" />
        </el-select>
      </div>
    </div>
    
    <!-- 时间轴 -->
    <div class="time-axis">
      <div class="time-axis-header">会议室 / 时间</div>
      <div class="time-slots">
        <div v-for="timeSlot in displayTimeSlots" :key="timeSlot.value" class="time-slot">
          {{ timeSlot.label }}
        </div>
      </div>
    </div>
    
    <!-- 会议室时间表 -->
    <div class="rooms-timetable">
      <div v-if="filteredRooms.length === 0" class="no-rooms">
        没有符合条件的会议室
      </div>
      <div v-else v-for="room in filteredRooms" :key="room.id" class="room-row">
        <div class="room-info">
          <div class="room-name">{{ room.name }}</div>
          <div class="room-capacity">容量: {{ room.capacity }}人</div>
          <div class="room-facilities">
            <el-tag v-if="room.facilities?.includes('projector')" size="small">投影仪</el-tag>
            <el-tag v-if="room.facilities?.includes('videoConference')" size="small" type="success">视频会议</el-tag>
            <el-tag v-if="room.facilities?.includes('whiteboard')" size="small" type="warning">白板</el-tag>
            <el-tag v-if="room.facilities?.includes('audio')" size="small" type="info">音响</el-tag>
          </div>
        </div>
        <div class="room-timeline">
          <div 
            v-for="timeSlot in displayTimeSlots" 
            :key="timeSlot.value" 
            class="time-block"
            :class="{ 'time-block-available': isTimeSlotAvailable(room.id, timeSlot.value) }"
            @click="handleTimeSlotClick(room, timeSlot)"
          >
            <div 
              v-for="booking in getBookingsForRoomAndTimeSlot(room.id, timeSlot.value)" 
              :key="booking.id"
              class="booking-block"
              :class="getBookingStatusClass(booking)"
              @click.stop="openBookingDetails(booking)"
            >
              {{ booking.title }}
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 预订对话框 -->
    <el-dialog 
      :title="dialogMode === 'create' ? '预订会议室' : '预订详情'" 
      v-model="dialogVisible" 
      width="500px"
    >
      <!-- 查看预订详情 -->
      <div v-if="dialogMode === 'view'" class="booking-details">
        <h3>{{ selectedBooking.title }}</h3>
        <p><strong>会议室:</strong> {{ selectedBooking.roomName }}</p>
        <p><strong>日期:</strong> {{ formatDate(selectedBooking.bookingDate) }}</p>
        <p><strong>时间:</strong> {{ formatTimeRange(selectedBooking.timeSlot) }}</p>
        <p><strong>预订人:</strong> {{ selectedBooking.username }}</p>
        <p><strong>状态:</strong> {{ getStatusText(selectedBooking.status) }}</p>
        <p v-if="selectedBooking.description"><strong>说明:</strong> {{ selectedBooking.description }}</p>
        
        <div class="booking-actions" v-if="canCancelBooking(selectedBooking)">
          <el-button type="danger" @click="cancelBooking">取消预订</el-button>
        </div>
      </div>
      
      <!-- 创建新预订 -->
      <el-form v-else :model="bookingForm" :rules="bookingRules" ref="bookingFormRef" label-width="100px">
        <el-form-item label="会议室" prop="roomId">
          <el-input :value="selectedRoom.name" disabled />
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker 
            v-model="bookingForm.date" 
            type="date" 
            placeholder="选择日期"
            format="YYYY/MM/DD"
            :disabled="!!selectedDate"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlot">
          <el-select 
            v-model="bookingForm.timeSlot" 
            placeholder="选择时间段"
            :disabled="!!selectedTimeSlot"
          >
            <el-option 
              v-for="slot in availableTimeSlots" 
              :key="slot.value" 
              :label="slot.label" 
              :value="slot.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="会议主题" prop="title">
          <el-input v-model="bookingForm.title" placeholder="请输入会议主题" />
        </el-form-item>
        <el-form-item label="会议说明" prop="description">
          <el-input 
            v-model="bookingForm.description" 
            type="textarea" 
            rows="3" 
            placeholder="请输入会议说明（可选）"
          />
        </el-form-item>
        <el-form-item label="参会人数" prop="attendees">
          <el-input-number 
            v-model="bookingForm.attendees" 
            :min="1" 
            :max="selectedRoom.capacity || 50"
          />
          <span class="capacity-hint"> 
            (会议室容量: {{ selectedRoom.capacity || '未知' }}人)
          </span>
        </el-form-item>
        <el-form-item label="需要设备" prop="requiredFacilities">
          <el-checkbox-group v-model="bookingForm.requiredFacilities">
            <el-checkbox label="projector">投影仪</el-checkbox>
            <el-checkbox label="videoConference">视频会议</el-checkbox>
            <el-checkbox label="whiteboard">白板</el-checkbox>
            <el-checkbox label="audio">音响设备</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      
      <!-- 对话框底部按钮 -->
      <template #footer>
        <div v-if="dialogMode === 'create'">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBooking" :loading="submitLoading">提交预订</el-button>
        </div>
        <div v-else>
          <el-button @click="dialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 日期选择对话框 -->
    <el-dialog title="选择日期" v-model="datePickerVisible" width="300px">
      <el-calendar v-model="currentDate" @pick="handleDatePick" />
      <template #footer>
        <el-button @click="datePickerVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';

dayjs.locale('zh-cn');

export default {
  name: 'RoomCalendarView',
  components: {
    ArrowLeft,
    ArrowRight
  },
  props: {
    rooms: {
      type: Array,
      default: () => []
    },
    bookings: {
      type: Array,
      default: () => []
    },
    userId: {
      type: Number,
      default: 0
    },
    userRole: {
      type: Number,
      default: 2 // 默认普通用户
    }
  },
  emits: ['book-room', 'cancel-booking', 'fetch-bookings'],
  setup(props, { emit }) {
    // 日期和过滤相关
    const currentDate = ref(new Date());
    const datePickerVisible = ref(false);
    const timeRange = ref('fullday');
    const capacityFilter = ref('');
    const facilityFilter = ref([]);
    
    // 预订对话框相关
    const dialogVisible = ref(false);
    const dialogMode = ref('create');
    const selectedRoom = ref({});
    const selectedBooking = ref({});
    const selectedDate = ref(null);
    const selectedTimeSlot = ref(null);
    const submitLoading = ref(false);
    
    // 预订表单
    const bookingFormRef = ref(null);
    const bookingForm = reactive({
      roomId: '',
      date: null,
      timeSlot: '',
      title: '',
      description: '',
      attendees: 1,
      requiredFacilities: []
    });
    
    // 表单验证规则
    const bookingRules = {
      date: [{ required: true, message: '请选择日期', trigger: 'change' }],
      timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
      title: [{ required: true, message: '请输入会议主题', trigger: 'blur' }],
      attendees: [{ required: true, message: '请输入参会人数', trigger: 'change' }]
    };
    
    // 根据过滤条件显示会议室
    const filteredRooms = computed(() => {
      let result = [...props.rooms];
      
      // 按容量过滤
      if (capacityFilter.value) {
        if (capacityFilter.value === 5) {
          result = result.filter(room => room.capacity <= 5);
        } else if (capacityFilter.value === 10) {
          result = result.filter(room => room.capacity > 5 && room.capacity <= 10);
        } else if (capacityFilter.value === 20) {
          result = result.filter(room => room.capacity > 10 && room.capacity <= 20);
        } else if (capacityFilter.value === 30) {
          result = result.filter(room => room.capacity > 20);
        }
      }
      
      // 按设施过滤
      if (facilityFilter.value && facilityFilter.value.length > 0) {
        result = result.filter(room => {
          if (!room.facilities) return false;
          return facilityFilter.value.every(facility => room.facilities.includes(facility));
        });
      }
      
      return result;
    });
    
    // 计算显示的小时范围
    const displayHours = computed(() => {
      if (timeRange.value === 'morning') {
        return [8, 9, 10, 11, 12];
      } else if (timeRange.value === 'afternoon') {
        return [13, 14, 15, 16, 17, 18];
      } else {
        return [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18];
      }
    });
    
    // 计算显示的15分钟时间段
    const displayTimeSlots = computed(() => {
      const slots = [];
      
      displayHours.value.forEach(hour => {
        // 每小时分为4个15分钟的时间段
        [0, 15, 30, 45].forEach(minute => {
          slots.push({
            label: `${hour}:${minute.toString().padStart(2, '0')}`,
            value: `${hour}:${minute}`
          });
        });
      });
      
      return slots;
    });
    
    // 格式化当前日期
    const formattedCurrentDate = computed(() => {
      return dayjs(currentDate.value).format('YYYY年MM月DD日');
    });
    
    // 获取可用的时间段选项
    const availableTimeSlots = computed(() => {
      const slots = [];
      
      // 如果已选择了时间段，则只返回对应的时间段
      if (selectedTimeSlot.value !== null) {
        const timeValue = selectedTimeSlot.value.value;
        const [hour, minute] = timeValue.split(':').map(Number);
        
        // 计算结束时间（15分钟后）
        let endHour = hour;
        let endMinute = minute + 15;
        
        if (endMinute >= 60) {
          endHour += 1;
          endMinute -= 60;
        }
        
        slots.push({
          label: `${hour}:${minute.toString().padStart(2, '0')}-${endHour}:${endMinute.toString().padStart(2, '0')}`,
          value: `${hour}:${minute}-${endHour}:${endMinute}`
        });
        
        return slots;
      }
      
      // 否则返回所有可用时间段
      displayTimeSlots.value.forEach(timeSlot => {
        if (isTimeSlotAvailable(selectedRoom.value?.id, timeSlot.value)) {
          const [hour, minute] = timeSlot.value.split(':').map(Number);
          
          // 计算结束时间（15分钟后）
          let endHour = hour;
          let endMinute = minute + 15;
          
          if (endMinute >= 60) {
            endHour += 1;
            endMinute -= 60;
          }
          
          slots.push({
            label: `${hour}:${minute.toString().padStart(2, '0')}-${endHour}:${endMinute.toString().padStart(2, '0')}`,
            value: `${hour}:${minute}-${endHour}:${endMinute}`
          });
        }
      });
      
      return slots;
    });
    
    // 格式化工具函数
    const formatDate = (date) => {
      return dayjs(date).format('YYYY年MM月DD日');
    };
    
    const formatTimeRange = (timeSlot) => {
      if (!timeSlot) return '';
      const [start, end] = timeSlot.split('-');
      return start.replace(':', '时') + '分-' + end.replace(':', '时') + '分';
    };
    
    // 获取预订状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '待审批',
        1: '已通过',
        2: '已拒绝',
        3: '已取消'
      };
      return statusMap[status] || '未知状态';
    };
    
    // 获取预订状态样式
    const getBookingStatusClass = (booking) => {
      const statusMap = {
        0: 'status-pending',
        1: 'status-approved',
        2: 'status-rejected',
        3: 'status-canceled'
      };
      return statusMap[booking.status] || '';
    };
    
    // 检查时间段是否可用
    const isTimeSlotAvailable = (roomId, timeSlotValue) => {
      if (!roomId) return true;
      
      const dateStr = dayjs(currentDate.value).format('YYYY-MM-DD');
      const [slotHour, slotMinute] = timeSlotValue.split(':').map(Number);
      
      const conflictingBookings = props.bookings.filter(booking => {
        if (booking.roomId !== roomId) return false;
        if (dayjs(booking.bookingDate).format('YYYY-MM-DD') !== dateStr) return false;
        
        // 解析预订的时间段
        const [startTime, endTime] = booking.timeSlot.split('-');
        const [startHour, startMinute] = startTime.split(':').map(Number);
        const [endHour, endMinute] = endTime.split(':').map(Number);
        
        // 将时间转换为分钟数，方便比较
        const slotTimeInMinutes = slotHour * 60 + slotMinute;
        const startTimeInMinutes = startHour * 60 + startMinute;
        const endTimeInMinutes = endHour * 60 + endMinute;
        
        // 检查是否有重叠
        return slotTimeInMinutes >= startTimeInMinutes && slotTimeInMinutes < endTimeInMinutes;
      });
      
      return conflictingBookings.length === 0;
    };
    
    // 获取指定会议室和时间段的预订
    const getBookingsForRoomAndTimeSlot = (roomId, timeSlotValue) => {
      const dateStr = dayjs(currentDate.value).format('YYYY-MM-DD');
      const [slotHour, slotMinute] = timeSlotValue.split(':').map(Number);
      
      return props.bookings.filter(booking => {
        if (booking.roomId !== roomId) return false;
        if (dayjs(booking.bookingDate).format('YYYY-MM-DD') !== dateStr) return false;
        
        // 解析预订的时间段
        const [startTime, endTime] = booking.timeSlot.split('-');
        const [startHour, startMinute] = startTime.split(':').map(Number);
        const [endHour, endMinute] = endTime.split(':').map(Number);
        
        // 将时间转换为分钟数，方便比较
        const slotTimeInMinutes = slotHour * 60 + slotMinute;
        const startTimeInMinutes = startHour * 60 + startMinute;
        const endTimeInMinutes = endHour * 60 + endMinute;
        
        // 检查是否有重叠
        return slotTimeInMinutes >= startTimeInMinutes && slotTimeInMinutes < endTimeInMinutes;
      });
    };
    
    // 处理时间段点击
    const handleTimeSlotClick = (room, timeSlot) => {
      if (!isTimeSlotAvailable(room.id, timeSlot.value)) {
        return; // 已被预订，不可点击
      }
      
      // 设置选中的会议室和时间段
      selectedRoom.value = room;
      selectedDate.value = currentDate.value;
      selectedTimeSlot.value = timeSlot;
      
      // 初始化预订表单
      bookingForm.roomId = room.id;
      bookingForm.date = new Date(currentDate.value);
      
      const [hour, minute] = timeSlot.value.split(':').map(Number);
      let endHour = hour;
      let endMinute = minute + 15;
      
      if (endMinute >= 60) {
        endHour += 1;
        endMinute -= 60;
      }
      
      bookingForm.timeSlot = `${hour}:${minute}-${endHour}:${endMinute}`;
      bookingForm.attendees = 1;
      bookingForm.requiredFacilities = [];
      
      // 打开创建对话框
      dialogMode.value = 'create';
      dialogVisible.value = true;
    };
    
    // 打开预订详情
    const openBookingDetails = (booking) => {
      selectedBooking.value = booking;
      dialogMode.value = 'view';
      dialogVisible.value = true;
    };
    
    // 提交预订
    const submitBooking = () => {
      bookingFormRef.value.validate(async (valid) => {
        if (valid) {
          submitLoading.value = true;
          
          try {
            const bookingData = {
              roomId: bookingForm.roomId,
              bookingDate: dayjs(bookingForm.date).format('YYYY-MM-DD'),
              timeSlot: bookingForm.timeSlot,
              title: bookingForm.title,
              description: bookingForm.description,
              attendees: bookingForm.attendees,
              requiredFacilities: bookingForm.requiredFacilities,
              userId: props.userId
            };
            
            emit('book-room', bookingData);
            
            dialogVisible.value = false;
            ElMessage.success('预订提交成功，等待审批');
            
            // 重置临时状态
            selectedDate.value = null;
            selectedTimeSlot.value = null;
            
          } catch (error) {
            ElMessage.error('预订提交失败');
          } finally {
            submitLoading.value = false;
          }
        }
      });
    };
    
    // 检查是否可以取消预订
    const canCancelBooking = (booking) => {
      if (!booking) return false;
      
      // 只有待审批或已通过的预订可以取消
      if (booking.status !== 0 && booking.status !== 1) {
        return false;
      }
      
      // 只有预订者或管理员可以取消
      return props.userRole === 1 || booking.userId === props.userId;
    };
    
    // 取消预订
    const cancelBooking = () => {
      ElMessageBox.confirm('确定要取消此预订吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        emit('cancel-booking', selectedBooking.value.id);
        dialogVisible.value = false;
        ElMessage.success('预订已取消');
      }).catch(() => {});
    };
    
    // 日期导航
    const today = () => {
      currentDate.value = new Date();
      fetchBookings();
    };
    
    const changeDate = (days) => {
      currentDate.value = dayjs(currentDate.value).add(days, 'day').toDate();
      fetchBookings();
    };
    
    // 处理从日历控件选择日期
    const handleDatePick = (date) => {
      currentDate.value = date;
      datePickerVisible.value = false;
      fetchBookings();
    };
    
    // 获取预订数据
    const fetchBookings = () => {
      const dateStr = dayjs(currentDate.value).format('YYYY-MM-DD');
      emit('fetch-bookings', { date: dateStr });
    };
    
    onMounted(() => {
      fetchBookings();
    });
    
    return {
      currentDate,
      formattedCurrentDate,
      datePickerVisible,
      timeRange,
      capacityFilter,
      facilityFilter,
      dialogVisible,
      dialogMode,
      selectedRoom,
      selectedBooking,
      selectedDate,
      selectedTimeSlot,
      bookingForm,
      bookingRules,
      bookingFormRef,
      submitLoading,
      filteredRooms,
      displayHours,
      displayTimeSlots,
      availableTimeSlots,
      formatDate,
      formatTimeRange,
      getStatusText,
      getBookingStatusClass,
      isTimeSlotAvailable,
      getBookingsForRoomAndTimeSlot,
      handleTimeSlotClick,
      openBookingDetails,
      submitBooking,
      canCancelBooking,
      cancelBooking,
      today,
      changeDate,
      handleDatePick
    };
  }
};
</script>

<style scoped>
.room-calendar-container {
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
  cursor: pointer;
}

.header-controls {
  display: flex;
  gap: 10px;
}

.time-axis {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

.time-axis-header {
  width: 220px;
  padding: 10px;
  font-weight: 500;
  border-right: 1px solid #ebeef5;
  text-align: center;
}

.time-slots {
  display: flex;
  flex: 1;
}

.time-slot {
  flex: 1;
  padding: 8px 4px;
  text-align: center;
  border-right: 1px solid #ebeef5;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
}

.rooms-timetable {
  flex: 1;
  overflow-y: auto;
}

.no-rooms {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #909399;
  font-size: 16px;
}

.room-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
}

.room-info {
  width: 220px;
  padding: 15px;
  border-right: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

.room-name {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 15px;
}

.room-capacity {
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.room-facilities {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.room-timeline {
  display: flex;
  flex: 1;
}

.time-block {
  flex: 1;
  position: relative;
  height: 100%;
  min-height: 90px;
  border-right: 1px solid #ebeef5;
  background-color: #f2f2f2;
  cursor: not-allowed;
}

.time-block-available {
  background-color: #fff;
  cursor: pointer;
}

.time-block-available:hover {
  background-color: #ecf5ff;
}

.booking-block {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  background-color: #95a5a6;
  color: #fff;
  cursor: pointer;
  z-index: 1;
}

.status-pending {
  background-color: #e67e22;
}

.status-approved {
  background-color: #27ae60;
}

.status-rejected {
  background-color: #e74c3c;
  opacity: 0.7;
  text-decoration: line-through;
}

.status-canceled {
  background-color: #27ae60;
  opacity: 1;
}

.booking-details {
  padding: 20px 10px;
}

.booking-details h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #303133;
}

.booking-details p {
  margin: 8px 0;
  line-height: 1.5;
}

.booking-actions {
  margin-top: 20px;
  text-align: right;
}

.capacity-hint {
  margin-left: 10px;
  font-size: 13px;
  color: #909399;
}
</style> 