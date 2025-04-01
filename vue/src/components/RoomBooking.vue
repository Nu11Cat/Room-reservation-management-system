<template>
  <div class="booking-container">
    <div class="booking-header">
      <h1>会议室预订</h1>
      <div class="header-actions">
        <el-date-picker 
          v-model="selectedDate" 
          type="date" 
          placeholder="选择日期"
          :disabled-date="disabledDate"
          @change="handleDateChange"
          format="YYYY年MM月DD日"
          value-format="YYYY-MM-DD"
          style="width: 240px"
        />
        <span class="date-display">{{ formatSelectedDate(selectedDate) }}</span>
        <el-button size="small" @click="goBack">返回列表</el-button>
      </div>
    </div>
    
    <div class="time-legend">
      <div class="legend-item">
        <div class="color-block available"></div>
        <span>可预约</span>
      </div>
      <div class="legend-item">
        <div class="color-block booked"></div>
        <span>已预约</span>
      </div>
      <div class="legend-item">
        <div class="color-block pending"></div>
        <span>待审批</span>
      </div>
    </div>
    
    <div class="booking-table-wrapper" v-loading="loading">
      <table class="booking-table">
        <thead>
          <tr>
            <th class="room-info-header">会议室信息</th>
            <th v-for="hour in timeRange" :key="hour" class="hour-column" :colspan="4">
              {{ hour }}:00
            </th>
          </tr>
          <tr class="time-row">
            <th></th>
            <template v-for="hour in timeRange" :key="`min-${hour}`">
              <th v-for="minute in ['00', '15', '30', '45']" :key="`${hour}:${minute}`" class="minute-column">
                {{ minute }}
              </th>
            </template>
          </tr>
        </thead>
        <tbody>
          <tr v-for="room in rooms" :key="room.id" :class="{'room-disabled': room.status !== 1}">
            <td class="room-info">
              <div class="room-name">{{ room.name }}</div>
              <div class="room-location">{{ room.location }}</div>
              <div class="room-capacity">容量: {{ room.capacity }}人</div>
              <el-tag size="small" :type="room.status === 1 ? 'success' : 'danger'">
                {{ room.status === 1 ? '可用' : '不可用' }}
              </el-tag>
              <el-button 
                type="primary" 
                size="small" 
                @click="handleBook(room)"
                :disabled="room.status !== 1"
                class="book-button">
                预订
              </el-button>
            </td>
            <template v-for="hour in timeRange" :key="`slot-${hour}-${room.id}`">
              <td 
                v-for="minute in ['00', '15', '30', '45']" 
                :key="`${hour}:${minute}-${room.id}`" 
                :class="[
                  'time-slot',
                  getTimeSlotStatus(`${hour}:${minute}`, room.id)
                ]"
              ></td>
            </template>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 预订对话框 -->
    <el-dialog
      v-model="bookingDialogVisible"
      title="预订会议室"
      width="500px"
    >
      <el-form 
        :model="bookingForm" 
        :rules="bookingRules" 
        ref="bookingFormRef" 
        label-width="80px"
      >
        <el-form-item label="会议室" prop="roomName">
          <span>{{ selectedRoom?.name }}</span>
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <span>{{ formatSelectedDate(selectedDate) }}</span>
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlot">
          <div class="time-range-select">
            <el-select v-model="bookingForm.startTime" placeholder="开始时间" style="width: 120px">
              <el-option
                v-for="timeSlot in timeSlots"
                :key="timeSlot"
                :label="timeSlot"
                :value="timeSlot"
                :disabled="isTimeDisabled(timeSlot, bookingForm.endTime, 'start')"
              />
            </el-select>
            <span class="time-separator">至</span>
            <el-select v-model="bookingForm.endTime" placeholder="结束时间" style="width: 120px">
              <el-option
                v-for="timeSlot in timeSlots"
                :key="timeSlot"
                :label="timeSlot"
                :value="timeSlot"
                :disabled="isTimeDisabled(timeSlot, bookingForm.startTime, 'end')"
              />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="会议主题" prop="title">
          <el-input v-model="bookingForm.title" placeholder="请输入会议主题" />
        </el-form-item>
        <el-form-item label="参会人数" prop="attendees">
          <el-input-number v-model="bookingForm.attendees" :min="1" :max="selectedRoom?.capacity || 999" />
        </el-form-item>
        <el-form-item label="会议描述" prop="description">
          <el-input 
            v-model="bookingForm.description" 
            type="textarea" 
            rows="3"
            placeholder="请输入会议描述（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bookingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBooking" :loading="loading">提交预订</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getRoomList } from '@/api/room';
import { addBooking, getBookingList } from '@/api/booking';

export default {
  name: 'RoomBookingPage',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const loading = ref(false);
    const bookingFormRef = ref(null);
    const rooms = ref([]);
    const bookings = ref([]);
    const bookingDialogVisible = ref(false);
    const selectedRoom = ref(null);
    
    // 格式化日期
    const formatDate = (date) => {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    };
    
    const selectedDate = ref(formatDate(new Date()));
    
    // 时间范围（小时）
    const timeRange = computed(() => {
      // 生成8点到21点的数组
      return Array.from({ length: 14 }, (_, i) => i + 8);
    });
    
    // 生成时间段列表
    const timeSlots = computed(() => {
      const slots = [];
      for (let hour of timeRange.value) {
        for (let minute of ['00', '15', '30', '45']) {
          slots.push(`${hour.toString().padStart(2, '0')}:${minute}`);
        }
      }
      return slots;
    });
    
    const bookingForm = reactive({
      title: '',
      attendees: 1,
      description: '',
      startTime: '',
      endTime: ''
    });
    
    const bookingRules = {
      title: [
        { required: true, message: '请输入会议主题', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      attendees: [
        { required: true, message: '请输入参会人数', trigger: 'blur' },
        { type: 'number', min: 1, message: '参会人数必须大于0', trigger: 'blur' }
      ],
      startTime: [
        { required: true, message: '请选择开始时间', trigger: 'change' }
      ],
      endTime: [
        { required: true, message: '请选择结束时间', trigger: 'change' },
        { validator: validateTimeRange, trigger: 'change' }
      ]
    };
    
    // 验证时间范围
    function validateTimeRange(rule, value, callback) {
      if (bookingForm.startTime && value) {
        if (value <= bookingForm.startTime) {
          callback(new Error('结束时间必须晚于开始时间'));
        } else {
          callback();
        }
      } else {
        callback();
      }
    }
    
    // 判断时间是否应该禁用
    function isTimeDisabled(time, compareTime, type) {
      if (!compareTime) return false;
      
      if (type === 'start') {
        // 开始时间不能晚于结束时间
        return time >= compareTime;
      } else {
        // 结束时间不能早于或等于开始时间
        return time <= compareTime;
      }
    }
    
    // 格式化选中日期显示
    const formatSelectedDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
      return `${dateStr.replace(/-/g, '/')} ${days[date.getDay()]}`;
    };
    
    // 获取时间段状态
    const getTimeSlotStatus = (timeSlot, roomId) => {
      // 根据预约记录判断时间段状态
      const booking = bookings.value.find(b => 
        b.roomId === roomId && 
        isTimeInRange(timeSlot, b.startTime, b.endTime)
      );
      
      if (!booking) return 'available';
      
      // 根据预约状态返回不同的样式
      switch (booking.status) {
        case 0: return 'pending';
        case 1: return 'booked';
        case 2: return 'rejected';
        case 3: return 'cancelled';
        default: return 'available';
      }
    };
    
    // 判断时间是否在预约范围内
    const isTimeInRange = (timeSlot, startTime, endTime) => {
      const time = timeSlot.padStart(5, '0');
      return time >= startTime.substring(11, 16) && time < endTime.substring(11, 16);
    };
    
    // 日期变更处理
    const handleDateChange = () => {
      loadRoomList();
      loadBookings();
    };
    
    // 加载会议室列表
    const loadRoomList = async () => {
      try {
        loading.value = true;
        const res = await getRoomList({
          page: 1,
          size: 100
        });
        
        if (res && res.code === 1) {
          rooms.value = res.data.rows || [];
        } else {
          ElMessage.error(res?.msg || '获取会议室列表失败');
        }
      } catch (error) {
        console.error('获取会议室列表失败:', error);
        ElMessage.error('获取会议室列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 加载预订记录
    const loadBookings = async () => {
      try {
        const res = await getBookingList({
          page: 1,
          pageSize: 100,
          begin: selectedDate.value,
          end: addDays(selectedDate.value, 1)  // 使用下一天作为结束日期，确保查询整天
        });
        
        if (res.code === 1 && res.data && Array.isArray(res.data.rows)) {
          bookings.value = res.data.rows;
          console.log('获取到的预订数据:', bookings.value);
        } else {
          console.error('获取预订数据失败:', res);
          bookings.value = [];
        }
      } catch (error) {
        console.error('获取预订记录失败:', error);
        bookings.value = [];
      }
    };
    
    // 日期加减函数
    const addDays = (dateStr, days) => {
      const date = new Date(dateStr);
      date.setDate(date.getDate() + days);
      return formatDate(date);
    };
    
    // 处理预订按钮点击
    const handleBook = (room) => {
      selectedRoom.value = room;
      bookingForm.title = '';
      bookingForm.attendees = 1;
      bookingForm.description = '';
      bookingForm.startTime = '';
      bookingForm.endTime = '';
      bookingDialogVisible.value = true;
    };
    
    // 提交预订
    const submitBooking = async () => {
      if (!selectedRoom.value || !bookingForm.startTime || !bookingForm.endTime) {
        ElMessage.warning('请选择会议室和时间段');
        return;
      }
      
      try {
        await bookingFormRef.value.validate();
        loading.value = true;
        
        const bookingData = {
          roomId: selectedRoom.value.id,
          title: bookingForm.title,
          startTime: `${selectedDate.value} ${bookingForm.startTime}:00`,
          endTime: `${selectedDate.value} ${bookingForm.endTime}:00`,
          attendees: String(bookingForm.attendees),
          description: bookingForm.description || ''
        };
        
        const res = await addBooking(bookingData);
        
        if (res.code === 1) {
          ElMessage.success('预订申请提交成功，等待审批');
          bookingDialogVisible.value = false;
          // 重新加载预订记录
          await loadBookings();
        } else {
          ElMessage.error(res.msg || '预订申请提交失败');
        }
      } catch (error) {
        console.error('预订申请提交失败:', error);
        ElMessage.error('预订申请提交失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 返回会议室列表
    const goBack = () => {
      router.push('/room/list');
    };
    
    onMounted(() => {
      loadRoomList();
      loadBookings();
      if (route.query.date) {
        selectedDate.value = route.query.date;
      }
    });
    
    return {
      loading,
      bookingFormRef,
      selectedDate,
      rooms,
      timeRange,
      timeSlots,
      bookingForm,
      bookingRules,
      bookingDialogVisible,
      selectedRoom,
      disabledDate: (time) => time.getTime() < Date.now() - 8.64e7,
      handleDateChange,
      formatSelectedDate,
      getTimeSlotStatus,
      handleBook,
      submitBooking,
      goBack,
      isTimeDisabled
    };
  }
};
</script>

<style scoped>
.booking-container {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.date-display {
  font-size: 16px;
  font-weight: bold;
}

.time-legend {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
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

.color-block.available {
  background-color: #e1f3d8;
  border: 1px solid #67c23a;
}

.color-block.booked {
  background-color: #fde2e2;
  border: 1px solid #f56c6c;
}

.color-block.pending {
  background-color: #faecd8;
  border: 1px solid #e6a23c;
}

.booking-table-wrapper {
  width: 100%;
  overflow-x: auto;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.booking-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ebeef5;
}

.booking-table th, 
.booking-table td {
  border: 1px solid #ebeef5;
  padding: 4px;
  text-align: center;
}

.booking-table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

.room-info-header {
  width: 200px;
}

.hour-column {
  background-color: #f5f7fa;
}

.minute-column {
  font-size: 12px;
  color: #909399;
}

.time-row th {
  height: 24px;
  line-height: 24px;
}

.room-info {
  text-align: left;
  padding: 10px;
  vertical-align: top;
}

.room-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.room-location, 
.room-capacity {
  color: #606266;
  font-size: 12px;
  margin-bottom: 5px;
}

.book-button {
  margin-top: 8px;
}

.time-slot {
  width: 25px;
  height: 25px;
  cursor: pointer;
}

.time-slot.available {
  background-color: #e1f3d8;
}

.time-slot.booked {
  background-color: #fde2e2;
}

.time-slot.pending {
  background-color: #faecd8;
}

.time-slot.rejected {
  background-color: #e1f3d8;
}

.time-slot.cancelled {
  background-color: #e1f3d8;
  background-image: none;
}

.room-disabled {
  background-color: #f5f7fa;
  opacity: 0.7;
}

.room-disabled .time-slot {
  cursor: not-allowed;
  background-color: #f5f7fa;
}

.time-range-select {
  display: flex;
  align-items: center;
  gap: 10px;
}

.time-separator {
  color: #606266;
}
</style> 