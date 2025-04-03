<template>
  <div class="room-booking-page">
    <div class="page-header">
      <h1>会议室预订</h1>
      <div class="header-actions">
        <el-button type="success" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
          <el-button type="primary" @click="drawer.visible = true">
            <el-icon><Bell /></el-icon>
            通知
          </el-button>
        </el-badge>
      </div>
    </div>
    
    <!-- 添加筛选区域 -->
    <div class="filter-container">
      <el-card class="filter-card">
        <div class="filter-form">
          <el-input 
            v-model="filters.roomName" 
            placeholder="会议室名称" 
            clearable 
            @input="handleFilter"
            style="width: 180px; margin-right: 10px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-input 
            v-model="filters.location" 
            placeholder="会议室位置" 
            clearable 
            @input="handleFilter"
            style="width: 180px; margin-right: 10px;"
          >
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
          <el-select 
            v-model="filters.capacity" 
            placeholder="容纳人数" 
            clearable 
            @change="handleFilter"
            style="width: 150px; margin-right: 10px;"
          >
            <el-option label="5人以下" :value="5" />
            <el-option label="5-10人" :value="10" />
            <el-option label="10-20人" :value="20" />
            <el-option label="20人以上" :value="30" />
          </el-select>
          <el-select 
            v-model="filters.facilities" 
            placeholder="设备要求" 
            multiple 
            collapse-tags
            clearable 
            @change="handleFilter"
            style="width: 200px; margin-right: 10px;"
          >
            <el-option label="投影仪" value="projector" />
            <el-option label="视频会议" value="videoConference" />
            <el-option label="白板" value="whiteboard" />
            <el-option label="音响设备" value="audio" />
          </el-select>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </el-card>
    </div>
    
    <el-card class="calendar-card">
      <room-calendar-view
        :rooms="filteredRooms"
        :bookings="bookings"
        :user-id="userId"
        :user-role="userRole"
        @book-room="handleBookRoom"
        @cancel-booking="handleCancelBooking"
        @fetch-bookings="handleFetchBookings"
      />
    </el-card>
    
    <!-- 通知抽屉 -->
    <el-drawer
      v-model="drawer.visible"
      :title="drawer.title"
      direction="rtl"
      size="400px"
    >
      <div class="notification-list">
        <h3>未读通知 <el-tag type="danger">{{ unreadCount }}</el-tag></h3>
        <el-empty description="暂无未读通知" v-if="notifications.length === 0 && !loading.notifications" />
        <el-skeleton :rows="3" animated v-else-if="loading.notifications" />
        <div v-else>
          <div 
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
          >
            <div class="notification-title">
              <el-tag :type="getNotificationTypeTag(notification.type)" size="small">
                {{ getNotificationTypeText(notification.type) }}
              </el-tag>
              <span>{{ notification.title }}</span>
            </div>
            <div class="notification-content">{{ notification.content }}</div>
            <div class="notification-footer">
              <span class="notification-time">{{ notification.createTime }}</span>
              <el-button size="small" text type="primary" @click="markAsRead(notification)">
                标为已读
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh, Bell, Search, Location } from '@element-plus/icons-vue';
import RoomCalendarView from './RoomCalendarView.vue';
import { useUserStore } from '@/stores/user';

export default {
  name: 'RoomBookingPage',
  components: {
    RoomCalendarView,
    Refresh,
    Bell,
    Search,
    Location
  },
  setup() {
    const userStore = useUserStore();
    const userId = computed(() => userStore.userInfo?.userId || 0);
    const userRole = computed(() => userStore.userInfo?.roleId || 2);
    
    // 数据相关
    const rooms = ref([]);
    const bookings = ref([]);
    const notifications = ref([]);
    
    // 筛选条件
    const filters = ref({
      roomName: '',
      location: '',
      capacity: '',
      facilities: []
    });
    
    // 筛选后的会议室列表
    const filteredRooms = computed(() => {
      let result = [...rooms.value];
      
      // 按名称筛选
      if (filters.value.roomName) {
        result = result.filter(room => 
          room.name.toLowerCase().includes(filters.value.roomName.toLowerCase())
        );
      }
      
      // 按位置筛选
      if (filters.value.location) {
        result = result.filter(room => 
          room.location.toLowerCase().includes(filters.value.location.toLowerCase())
        );
      }
      
      // 按容量筛选
      if (filters.value.capacity) {
        if (filters.value.capacity === 5) {
          result = result.filter(room => room.capacity <= 5);
        } else if (filters.value.capacity === 10) {
          result = result.filter(room => room.capacity > 5 && room.capacity <= 10);
        } else if (filters.value.capacity === 20) {
          result = result.filter(room => room.capacity > 10 && room.capacity <= 20);
        } else if (filters.value.capacity === 30) {
          result = result.filter(room => room.capacity > 20);
        }
      }
      
      // 按设施筛选
      if (filters.value.facilities && filters.value.facilities.length > 0) {
        result = result.filter(room => {
          if (!room.facilities) return false;
          return filters.value.facilities.every(facility => 
            room.facilities.includes(facility)
          );
        });
      }
      
      return result;
    });
    
    // 状态相关
    const loading = ref({
      rooms: false,
      bookings: false,
      notifications: false
    });
    
    // 通知抽屉
    const drawer = ref({
      visible: false,
      title: '通知中心'
    });
    
    // 计算未读通知数量
    const unreadCount = computed(() => notifications.value.length);
    
    // 重置筛选条件
    const resetFilters = () => {
      filters.value = {
        roomName: '',
        location: '',
        capacity: '',
        facilities: []
      };
      handleFilter();
    };
    
    // 处理筛选
    const handleFilter = () => {
      console.log('应用筛选:', filters.value);
      // 筛选逻辑已在computed属性中实现
    };
    
    // 加载会议室列表
    const loadRooms = async () => {
      loading.value.rooms = true;
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          rooms.value = [
            { 
              id: 1, 
              name: '会议室A', 
              capacity: 10, 
              facilities: ['projector', 'whiteboard'],
              location: '1楼'
            },
            { 
              id: 2, 
              name: '会议室B', 
              capacity: 20, 
              facilities: ['projector', 'videoConference', 'audio'],
              location: '2楼'
            },
            { 
              id: 3, 
              name: '小型会议室C', 
              capacity: 5, 
              facilities: ['whiteboard'],
              location: '1楼'
            },
            { 
              id: 4, 
              name: '董事会议室', 
              capacity: 15, 
              facilities: ['projector', 'videoConference', 'audio', 'whiteboard'],
              location: '3楼'
            }
          ];
          loading.value.rooms = false;
        }, 500);
      } catch (error) {
        console.error('获取会议室列表失败:', error);
        ElMessage.error('获取会议室列表失败');
        loading.value.rooms = false;
      }
    };
    
    // 加载预订列表
    const loadBookings = async (date) => {
      loading.value.bookings = true;
      
      try {
        // 这里将来替换为API调用
        console.log('加载日期的预订:', date);
        
        setTimeout(() => {
          // 模拟从API获取的数据
          bookings.value = [
            {
              id: 1,
              roomId: 1,
              roomName: '会议室A',
              userId: 1,
              username: '管理员',
              title: '项目启动会',
              description: '讨论新项目的启动计划和分工',
              bookingDate: '2023-08-10',
              timeSlot: '9-10',
              status: 1, // 已通过
              attendees: 8,
              createTime: '2023-08-05 10:00:00'
            },
            {
              id: 2,
              roomId: 2,
              roomName: '会议室B',
              userId: 2,
              username: '张三',
              title: '部门周会',
              description: '每周例行部门会议',
              bookingDate: '2023-08-10',
              timeSlot: '14-16',
              status: 1, // 已通过
              attendees: 15,
              createTime: '2023-08-05 11:30:00'
            },
            {
              id: 3,
              roomId: 3,
              roomName: '小型会议室C',
              userId: 3,
              username: '李四',
              title: '产品评审',
              description: '评审最新的产品设计方案',
              bookingDate: '2023-08-10',
              timeSlot: '10-11',
              status: 1, // 已通过
              attendees: 4,
              createTime: '2023-08-04 14:00:00'
            }
          ];
          loading.value.bookings = false;
        }, 500);
      } catch (error) {
        console.error('获取预订列表失败:', error);
        ElMessage.error('获取预订列表失败');
        loading.value.bookings = false;
      }
    };
    
    // 加载通知列表
    const loadNotifications = async () => {
      loading.value.notifications = true;
      
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          notifications.value = [
            {
              id: 1,
              title: '预约审批结果',
              content: '您预约的会议室A已通过审批',
              type: 1, // 预约状态变更
              createTime: '2023-08-05 10:30:00'
            },
            {
              id: 2,
              title: '会议即将开始',
              content: '您预约的会议将在15分钟后开始',
              type: 2, // 预约提醒
              createTime: '2023-08-05 09:45:00'
            }
          ];
          loading.value.notifications = false;
        }, 500);
      } catch (error) {
        console.error('获取通知列表失败:', error);
        ElMessage.error('获取通知列表失败');
        loading.value.notifications = false;
      }
    };
    
    // 刷新数据
    const refreshData = () => {
      loadRooms();
      loadBookings();
      loadNotifications();
    };
    
    // 处理预定房间
    const handleBookRoom = (bookingData) => {
      console.log('预订会议室:', bookingData);
      // 这里将来替换为API调用
      setTimeout(() => {
        ElMessage.success('预订成功，等待审批');
        loadBookings();
      }, 500);
    };
    
    // 处理取消预订
    const handleCancelBooking = (bookingId) => {
      console.log('取消预订:', bookingId);
      // 这里将来替换为API调用
      setTimeout(() => {
        ElMessage.success('预订已取消');
        loadBookings();
      }, 500);
    };
    
    // 处理获取预订
    const handleFetchBookings = (date) => {
      loadBookings(date);
    };
    
    // 标记通知为已读
    const markAsRead = (notification) => {
      console.log('标记通知为已读:', notification.id);
      // 这里将来替换为API调用
      setTimeout(() => {
        notifications.value = notifications.value.filter(n => n.id !== notification.id);
        ElMessage.success('已标记为已读');
      }, 300);
    };
    
    // 获取通知类型标签
    const getNotificationTypeTag = (type) => {
      const types = {
        1: 'success',
        2: 'warning',
        3: 'info'
      };
      return types[type] || 'info';
    };
    
    // 获取通知类型文本
    const getNotificationTypeText = (type) => {
      const types = {
        1: '预约状态',
        2: '预约提醒',
        3: '系统公告'
      };
      return types[type] || '其他';
    };
    
    onMounted(() => {
      refreshData();
    });
    
    return {
      drawer,
      notifications,
      unreadCount,
      rooms,
      bookings,
      loading,
      userId,
      userRole,
      handleBookRoom,
      handleCancelBooking,
      handleFetchBookings,
      markAsRead,
      getNotificationTypeTag,
      getNotificationTypeText,
      refreshData,
      filters,
      filteredRooms,
      resetFilters,
      handleFilter
    };
  }
};
</script>

<style scoped>
.room-booking-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.calendar-card {
  margin-bottom: 20px;
}

.notification-badge {
  margin-left: 10px;
}

.notification-list {
  padding: 10px;
}

.notification-item {
  border-bottom: 1px solid #ebeef5;
  padding: 10px 0;
  margin-bottom: 10px;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  font-weight: bold;
}

.notification-content {
  margin: 8px 0;
  color: #606266;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.filter-container {
  margin-bottom: 20px;
}

.filter-card {
  padding: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.filter-form .el-button {
  margin-left: 10px;
}
</style> 