<template>
  <div class="booking-container">
    <div class="page-header">
      <div class="header-left">
        <h1>
          会议室预约
          <el-tooltip
            v-if="configLoaded"
            :content="configStore.approvalRequired ? '预约需要审批：您的申请将提交给管理员审批，审批通过后才能使用会议室' : '预约自动通过：您的申请将自动通过，无需等待审批'"
            placement="top"
            effect="light"
          >
            <el-icon class="info-icon" :class="configStore.approvalRequired ? 'warning-icon' : 'success-icon'">
              <InfoFilled />
            </el-icon>
          </el-tooltip>
        </h1>
        <div class="header-description">请选择日期和时间段预约会议室</div>
      </div>
      <div class="header-right">
        <el-button @click="goBack" type="primary" plain>
          <el-icon><Back /></el-icon>
          返回会议室列表
        </el-button>
      </div>
    </div>

    <!-- 预约流程说明 -->
    <!-- 移除了el-alert元素 -->

    <!-- 配置加载提示 -->
    <el-alert
      v-if="false"
      title="正在加载系统配置..."
      type="info"
      description="请等待系统配置加载完成后再进行预约操作"
      show-icon
      :closable="false"
    />

    <!-- 筛选条件卡片 -->
    <el-card class="filter-card">
      <div class="booking-header">
        <div class="date-selector">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            @change="handleDateChange"
          />
          <span class="date-display">{{ formatSelectedDate(selectedDate) }}</span>
        </div>
      </div>

      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="名称">
          <el-input v-model="filterForm.name" placeholder="会议室名称" clearable />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="filterForm.location" placeholder="会议室位置" clearable />
        </el-form-item>
        <el-form-item label="容量">
          <el-select v-model="filterForm.capacity" placeholder="请选择" clearable style="width: 120px">
            <el-option label="10人以上" :value="10" />
            <el-option label="20人以上" :value="20" />
            <el-option label="30人以上" :value="30" />
            <el-option label="50人以上" :value="50" />
            <el-option label="100人以上" :value="100" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilters" :icon="Search">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="booking-card" v-loading="loading">
      <div class="legend-container">
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
      </div>
      
      <div class="booking-table-wrapper">
        <table class="booking-table">
          <thead>
            <tr>
              <th class="room-info-header">会议室</th>
              <th v-for="hour in timeRange" :key="hour" class="hour-column" :colspan="4">
                {{ hour }}:00 - {{ hour + 1 }}:00
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
                <div class="room-card">
                  <div class="room-name">{{ room.name }}</div>
                  <div class="room-detail">
                    <div class="room-location">
                      <el-icon><Location /></el-icon>
                      {{ room.location }}
                    </div>
                    <div class="room-capacity">
                      <el-icon><UserFilled /></el-icon>
                      容量: {{ room.capacity }}人
                    </div>
                  </div>
                  <div class="room-actions">
                    <el-tag size="small" :type="room.status === 1 ? 'success' : 'danger'" effect="light">
                      {{ room.status === 1 ? '可用' : '不可用' }}
                    </el-tag>
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="handleBook(room)"
                      :disabled="room.status !== 1"
                      class="book-button">
                      <el-icon><Calendar /></el-icon>
                      预订
                    </el-button>
                  </div>
                </div>
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
    </el-card>
    
    <!-- 预订对话框 -->
    <el-dialog
      v-model="bookingDialogVisible"
      title="预订会议室"
      width="500px"
      destroy-on-close
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
/* eslint-disable no-unused-vars */
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getRoomList } from '@/api/room';
import { addBooking, getBookingList } from '@/api/booking';
import { useConfigStore } from '@/stores/config';
import useTimeOptions from '@/hooks/useTimeOptions';
// eslint-disable-next-line no-unused-vars
import { 
  Search, 
  Location, 
  UserFilled, 
  Calendar, 
  // eslint-disable-next-line no-unused-vars
  Back, 
  // eslint-disable-next-line no-unused-vars
  Refresh,
  InfoFilled
} from '@element-plus/icons-vue';

export default {
  name: 'RoomBookingPage',
  components: {
    Location,
    UserFilled,
    Calendar,
    InfoFilled
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const configStore = useConfigStore();
    const loading = ref(false);
    const bookingFormRef = ref(null);
    const rooms = ref([]);
    const allRooms = ref([]); // 保存原始数据用于筛选
    const bookings = ref([]);
    const bookingDialogVisible = ref(false);
    const selectedRoom = ref(null);
    const configLoaded = ref(true); // 添加configLoaded变量定义
    
    // 筛选表单
    const filterForm = reactive({
      name: '',
      location: '',
      capacity: ''
    });
    
    // 应用筛选
    const applyFilters = () => {
      console.log('应用筛选:', filterForm);
      
      let filteredRooms = [...allRooms.value];
      
      // 按名称筛选
      if (filterForm.name) {
        filteredRooms = filteredRooms.filter(room => 
          room.name.toLowerCase().includes(filterForm.name.toLowerCase())
        );
      }
      
      // 按位置筛选
      if (filterForm.location) {
        filteredRooms = filteredRooms.filter(room => 
          room.location.toLowerCase().includes(filterForm.location.toLowerCase())
        );
      }
      
      // 按容量筛选
      if (filterForm.capacity) {
        if (filterForm.capacity === 10) {
          filteredRooms = filteredRooms.filter(room => room.capacity > 10);
        } else if (filterForm.capacity === 20) {
          filteredRooms = filteredRooms.filter(room => room.capacity > 20);
        } else if (filterForm.capacity === 30) {
          filteredRooms = filteredRooms.filter(room => room.capacity > 30);
        } else if (filterForm.capacity === 50) {
          filteredRooms = filteredRooms.filter(room => room.capacity > 50);
        } else if (filterForm.capacity === 100) {
          filteredRooms = filteredRooms.filter(room => room.capacity > 100);
        }
      }
      
      rooms.value = filteredRooms;
    };
    
    // 重置筛选
    const resetFilters = () => {
      filterForm.name = '';
      filterForm.location = '';
      filterForm.capacity = '';
      
      // 重置为原始数据
      rooms.value = [...allRooms.value];
    };
    
    // 格式化日期
    const formatDate = (date) => {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    };
    
    const selectedDate = ref(formatDate(new Date()));
    
    // 获取时间选项工具
    const { 
      timeRange, 
      timeOptions, 
      validateTimeRange, 
      disabledDate: configDisabledDate,
      refreshTimeOptions
    } = useTimeOptions();
    
    // 时间段列表
    const timeSlots = computed(() => {
      return timeOptions.value.map(option => option.value);
    });
    
    const bookingForm = reactive({
      title: '',
      attendees: 1,
      description: '',
      startTime: '',
      endTime: ''
    });
    
    // 验证时间范围
    function validateTimeRangeRule(rule, value, callback) {
      if (bookingForm.startTime && value) {
        const result = validateTimeRange(bookingForm.startTime, value);
        if (!result.valid) {
          callback(new Error(result.message));
        } else {
          callback();
        }
      } else {
        callback();
      }
    }
    
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
        { validator: validateTimeRangeRule, trigger: 'change' }
      ]
    };
    
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
    
    // 日期禁用规则
    const disabledDate = (date) => {
      // 使用配置的日期禁用规则
      return configDisabledDate(date);
    };
    
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
          allRooms.value = res.data.rows || [];
          rooms.value = [...allRooms.value];
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
    
    // 提交预订前的额外验证
    const validateBookingBeforeSubmit = () => {
      if (!selectedRoom.value || !bookingForm.startTime || !bookingForm.endTime) {
        ElMessage.warning('请选择会议室和时间段');
        return false;
      }
      
      const result = validateTimeRange(bookingForm.startTime, bookingForm.endTime);
      if (!result.valid) {
        ElMessage.warning(result.message);
        return false;
      }
      
      return true;
    };
    
    // 提交预订
    const submitBooking = async () => {
      if (!validateBookingBeforeSubmit()) {
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
          // 根据configStore中的审批配置显示不同的成功消息
          if (configStore.approvalRequired) {
            ElMessage.success('预订申请提交成功，等待审批');
          } else {
            ElMessage.success('预订申请提交成功，已自动通过');
          }
          bookingDialogVisible.value = false;
          // 重新加载预订记录
          await loadBookings();
        } else {
          // 显示错误信息，使用更明显的样式
          ElMessage({
            message: res.msg || '预订申请提交失败',
            type: 'error',
            duration: 5000,
            showClose: true,
            offset: 80
          });
        }
      } catch (error) {
        console.error('预订申请提交失败:', error);
        ElMessage.error('系统错误，请稍后重试');
      } finally {
        loading.value = false;
      }
    };
    
    // 返回会议室列表
    const goBack = () => {
      router.push('/room/list');
    };
    
    // 强制刷新配置和时间选项
    onMounted(() => {
      // 确保从服务器获取最新配置
      console.log('[RoomBooking] 组件挂载，强制刷新配置');
      configStore.forceRefreshConfig().then(() => {
        console.log('[RoomBooking] 配置刷新完成，开始刷新时间选项');
        refreshTimeOptions();
        console.log('[RoomBooking] 使用固定15分钟时间间隔');
        
        // 加载会议室和预订信息
        loadRoomList();
        loadBookings();
      });
      
      if (route.query.date) {
        selectedDate.value = route.query.date;
      }
    });
    
    return {
      loading,
      rooms,
      bookings,
      timeRange,
      timeSlots,
      selectedDate,
      bookingDialogVisible,
      selectedRoom,
      bookingForm,
      bookingFormRef,
      bookingRules,
      formatSelectedDate,
      handleDateChange,
      getTimeSlotStatus,
      handleBook,
      submitBooking,
      goBack,
      isTimeDisabled,
      filterForm,
      applyFilters,
      resetFilters,
      disabledDate,
      configStore,
      configLoaded
    };
  }
};
</script>

<style scoped>
.booking-container {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.header-left {
  display: flex;
  flex-direction: column;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  z-index: 1;
}

.header-right {
  position: relative;
  z-index: 2;
}

.page-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
  margin-top: 0;
}

.header-description {
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.booking-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.date-selector {
  display: flex;
  align-items: center;
  gap: 15px;
}

.date-display {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
}

.legend-container {
  margin-bottom: 15px;
  background-color: #f8f8f8;
  border-radius: 4px;
  padding: 10px;
}

.time-legend {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-block {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  transition: all 0.2s;
}

.color-block.available {
  background-color: #e1f3d8;
  border: 1px solid #67c23a;
}

.color-block.booked {
  background-color: #fcb5b5;
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
  background-color: #fff;
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
  color: #606266;
}

.room-info-header {
  width: 200px;
  background-color: #ecf5ff;
  color: #409EFF;
}

.hour-column {
  background-color: #ecf5ff;
}

.minute-column {
  font-size: 12px;
  color: #909399;
}

.time-row th {
  height: 30px;
  line-height: 30px;
}

.room-info {
  text-align: left;
  padding: 10px;
  vertical-align: top;
  background-color: #fafafa;
}

.room-card {
  background-color: #fff;
  border-radius: 4px;
  padding: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.room-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.room-name {
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
  font-size: 14px;
}

.room-detail {
  margin-bottom: 10px;
}

.room-location, 
.room-capacity {
  color: #606266;
  font-size: 12px;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.room-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.book-button {
  display: flex;
  align-items: center;
  gap: 5px;
}

.time-slot {
  width: 24px;
  height: 24px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #fff;
}

.time-slot:hover {
  transform: scale(1.1);
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
  z-index: 2;
  position: relative;
}

.time-slot.available {
  background-color: #e1f3d8;
}

.time-slot.booked {
  background-color: #fcb5b5;
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

.filter-container {
  margin-top: 20px;
}

.filter-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .booking-table-wrapper {
    overflow-x: auto;
  }
  
  .filter-container .el-row .el-col {
    margin-bottom: 10px;
  }
}

.approval-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

.info-icon {
  font-size: 16px;
  margin-left: 5px;
  vertical-align: middle;
  cursor: help;
}

.warning-icon {
  color: #E6A23C;
}

.success-icon {
  color: #67C23A;
}

.time-slots-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.room-column {
  font-weight: bold;
  color: #303133;
  font-size: 14px;
}
</style> 