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
    
    <el-card class="calendar-card">
      <room-calendar-view
        :rooms="rooms"
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
import { Refresh, Bell } from '@element-plus/icons-vue';
import RoomCalendarView from './RoomCalendarView.vue';
import { useUserStore } from '@/stores/user';

export default {
  name: 'RoomBookingPage',
  components: {
    RoomCalendarView,
    Refresh,
    Bell
  },
  setup() {
    const userStore = useUserStore();
    const userId = computed(() => userStore.userInfo?.userId || 0);
    const userRole = computed(() => userStore.userInfo?.roleId || 2);
    
    // 数据相关
    const rooms = ref([]);
    const bookings = ref([]);
    const notifications = ref([]);
    
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
              status: 0, // 待审批
              attendees: 5,
              createTime: '2023-08-06 09:15:00'
            },
            {
              id: 4,
              roomId: 4,
              roomName: '董事会议室',
              userId: 1,
              username: '管理员',
              title: '季度董事会',
              description: '讨论第三季度业绩和下一季度计划',
              bookingDate: '2023-08-10',
              timeSlot: '13-15',
              status: 1, // 已通过
              attendees: 12,
              createTime: '2023-08-04 14:20:00'
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
    
    // 加载通知
    const loadNotifications = async () => {
      loading.value.notifications = true;
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          notifications.value = [
            {
              id: 1,
              title: '预订通知',
              content: '您的会议室预订已提交，等待审批',
              type: 1, // 预订通知
              status: 0, // 未读
              createTime: '2023-08-06 14:30:00'
            },
            {
              id: 2,
              title: '审批通知',
              content: '您的会议室预订已审批通过',
              type: 2, // 审批通知
              status: 0, // 未读
              createTime: '2023-08-07 09:45:00'
            }
          ];
          loading.value.notifications = false;
        }, 500);
      } catch (error) {
        console.error('获取通知失败:', error);
        ElMessage.error('获取通知失败');
        loading.value.notifications = false;
      }
    };
    
    // 提交预订
    const handleBookRoom = async (bookingData) => {
      try {
        // 这里将来替换为API调用
        console.log('提交预订:', bookingData);
        await new Promise(resolve => setTimeout(resolve, 500));
        
        ElMessage.success('预订提交成功，等待审批');
        // 重新加载当前日期的预订
        loadBookings({ date: bookingData.bookingDate });
      } catch (error) {
        ElMessage.error('预订提交失败');
      }
    };
    
    // 取消预订
    const handleCancelBooking = async (bookingId) => {
      try {
        // 这里将来替换为API调用
        console.log('取消预订:', bookingId);
        await new Promise(resolve => setTimeout(resolve, 500));
        
        ElMessage.success('预订已取消');
        // 重新加载当前数据
        refreshData();
      } catch (error) {
        ElMessage.error('取消预订失败');
      }
    };
    
    // 获取特定日期的预订
    const handleFetchBookings = (data) => {
      loadBookings(data.date);
    };
    
    // 标记通知为已读
    const markAsRead = async (notification) => {
      try {
        // 这里将来替换为API调用
        await new Promise(resolve => setTimeout(resolve, 300));
        
        // 从列表中移除
        notifications.value = notifications.value.filter(item => item.id !== notification.id);
        
        ElMessage.success('已标记为已读');
      } catch (error) {
        ElMessage.error('操作失败');
      }
    };
    
    // 获取通知类型标签
    const getNotificationTypeTag = (type) => {
      const typeMap = {
        0: '',      // 系统通知
        1: 'info',  // 预订通知
        2: 'success' // 审批通知
      };
      return typeMap[type] || '';
    };
    
    // 获取通知类型文本
    const getNotificationTypeText = (type) => {
      const typeMap = {
        0: '系统通知',
        1: '预订通知',
        2: '审批通知'
      };
      return typeMap[type] || '未知类型';
    };
    
    // 刷新所有数据
    const refreshData = () => {
      loadRooms();
      loadBookings(new Date());
      loadNotifications();
    };
    
    onMounted(() => {
      refreshData();
      
      // 定时检查新通知，实际项目中可以使用WebSocket
      setInterval(() => {
        loadNotifications();
      }, 30000); // 每30秒检查一次
    });
    
    return {
      rooms,
      bookings,
      notifications,
      loading,
      drawer,
      unreadCount,
      userId,
      userRole,
      handleBookRoom,
      handleCancelBooking,
      handleFetchBookings,
      markAsRead,
      getNotificationTypeTag,
      getNotificationTypeText,
      refreshData
    };
  }
};
</script>

<style scoped>
.room-booking-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.calendar-card {
  margin-bottom: 20px;
  height: calc(100vh - 180px);
}

.notification-badge {
  margin-left: 10px;
}

.notification-list {
  padding: 16px;
}

.notification-item {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 500;
  font-size: 16px;
}

.notification-content {
  color: #606266;
  margin-bottom: 12px;
  line-height: 1.5;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}
</style> 