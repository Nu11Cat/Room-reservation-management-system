<template>
  <div class="admin-booking-container">
    <div class="page-header">
      <div class="header-content">
        <h1>预约管理</h1>
        <div class="header-description">管理所有用户的会议室预约记录</div>
      </div>
      <el-button type="success" @click="loadBookings">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>
    
    <!-- 搜索条件 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="会议室:">
          <el-select v-model="searchForm.roomId" placeholder="全部会议室" clearable>
            <template #prefix>
              <el-icon><House /></el-icon>
            </template>
            <el-option
              v-for="room in roomOptions"
              :key="room.id"
              :label="room.name"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约人:">
          <el-input v-model="searchForm.username" placeholder="预约人用户名" clearable>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="时间范围:">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
          >
            <template #prefix>
              <el-icon><Calendar /></el-icon>
            </template>
          </el-date-picker>
        </el-form-item>
        <el-form-item class="search-buttons">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Delete /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="data-card">
      <div class="card-header">
        <span class="card-title">预约列表</span>
        <span class="card-subtitle">共 {{ total }} 条记录</span>
      </div>
      
      <div class="table-operations">
        <el-button-group>
          <el-button :type="activeTab === 'all' ? 'primary' : ''" @click="setActiveTab('all')">全部</el-button>
          <el-button :type="activeTab === 'pending' ? 'primary' : ''" @click="setActiveTab('pending')">待审批</el-button>
          <el-button :type="activeTab === 'approved' ? 'primary' : ''" @click="setActiveTab('approved')">已批准</el-button>
          <el-button :type="activeTab === 'rejected' ? 'primary' : ''" @click="setActiveTab('rejected')">已拒绝</el-button>
          <el-button :type="activeTab === 'cancelled' ? 'primary' : ''" @click="setActiveTab('cancelled')">已取消</el-button>
        </el-button-group>
      </div>
      
      <el-table 
        :data="bookingList" 
        style="width: 100%" 
        v-loading="loading" 
        border
        :header-cell-style="{ 
          background: '#f5f7fa', 
          color: '#606266', 
          fontWeight: 'bold' 
        }"
      >
        <el-table-column prop="id" label="预约ID" width="160" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Ticket /></el-icon>
              <span>预约ID</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="会议室" min-width="120" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><House /></el-icon>
              <span>会议室</span>
            </div>
          </template>
          <template #default="scope">
            <el-tooltip :content="getRoomFullInfo(scope.row)" placement="top">
              <span>{{ getRoomName(scope.row.roomId) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="预约人" min-width="100" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><User /></el-icon>
              <span>预约人</span>
            </div>
          </template>
          <template #default="scope">
            {{ scope.row.userName || '未知用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="会议主题" min-width="150" show-overflow-tooltip align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Document /></el-icon>
              <span>会议主题</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" min-width="300" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Timer /></el-icon>
              <span>预约时间</span>
            </div>
          </template>
          <template #default="scope">
            <div class="time-display-horizontal">
              {{ formatDateTime(scope.row.startTime) }} 至 {{ formatDateTime(scope.row.endTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><InfoFilled /></el-icon>
              <span>状态</span>
            </div>
          </template>
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Calendar /></el-icon>
              <span>创建时间</span>
            </div>
          </template>
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Setting /></el-icon>
              <span>操作</span>
            </div>
          </template>
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" type="primary" @click="handleView(scope.row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button 
                size="small" 
                type="success" 
                @click="handleEdit(scope.row)"
                v-if="scope.row.status === 0 || scope.row.status === 1"
              >
                <el-icon><Edit /></el-icon>
                修改
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="handleCancel(scope.row)"
                v-if="scope.row.status === 0 || scope.row.status === 1"
              >
                <el-icon><Delete /></el-icon>
                取消
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :small="false"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
        background
      />
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="预约详情" width="550px" destroy-on-close>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预约ID">{{ selectedBooking?.id }}</el-descriptions-item>
        <el-descriptions-item label="会议室">{{ getRoomName(selectedBooking?.roomId) }}</el-descriptions-item>
        <el-descriptions-item label="预约人">{{ selectedBooking?.userName || '未知用户' }}</el-descriptions-item>
        <el-descriptions-item label="会议主题">{{ selectedBooking?.title }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(selectedBooking?.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(selectedBooking?.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="参会人员">{{ selectedBooking?.attendees || '无' }}</el-descriptions-item>
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
            type="primary" 
            @click="handleEdit(selectedBooking)"
            v-if="selectedBooking && (selectedBooking.status === 0 || selectedBooking.status === 1)"
          >修改预约</el-button>
          <el-button 
            type="danger" 
            @click="handleCancel(selectedBooking)"
            v-if="selectedBooking && (selectedBooking.status === 0 || selectedBooking.status === 1)"
          >取消预约</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 修改预约对话框 -->
    <el-dialog v-model="editVisible" title="修改预约" width="550px" destroy-on-close>
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="会议室" prop="roomId">
          <el-select v-model="editForm.roomId" placeholder="请选择会议室">
            <template #prefix>
              <el-icon><House /></el-icon>
            </template>
            <el-option
              v-for="room in roomOptions"
              :key="room.id"
              :label="room.name"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="会议主题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入会议主题">
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="预约时间" prop="timeRange">
          <el-date-picker
            v-model="editForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          >
            <template #prefix>
              <el-icon><Timer /></el-icon>
            </template>
          </el-date-picker>
        </el-form-item>
        <el-form-item label="参会人员">
          <el-input v-model="editForm.attendees" placeholder="请输入参会人员">
            <template #prefix>
              <el-icon><UserFilled /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="会议描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入会议描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="submitLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, watch, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getBookingList, getBookingDetail, updateBooking, cancelBooking } from '@/api/booking';
import { getRoomList } from '@/api/room';
import { 
  Search, 
  Refresh, 
  Delete, 
  Calendar, 
  House, 
  Document, 
  Timer, 
  InfoFilled, 
  Setting, 
  View, 
  Edit, 
  User,
  UserFilled,
  Ticket
} from '@element-plus/icons-vue';

export default {
  name: 'AdminBookingManagement',
  components: {
    Search, 
    Refresh, 
    Delete, 
    Calendar, 
    House, 
    Document, 
    Timer, 
    InfoFilled, 
    Setting, 
    View, 
    Edit, 
    User,
    UserFilled,
    Ticket
  },
  setup() {
    const loading = ref(false);
    const submitLoading = ref(false);
    const bookingList = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    const detailVisible = ref(false);
    const editVisible = ref(false);
    const selectedBooking = ref(null);
    const roomOptions = ref([]);
    const activeTab = ref('all');
    const editFormRef = ref(null);
    
    const dateRange = ref([]);
    
    const searchForm = reactive({
      roomId: undefined,
      username: '',
      status: undefined,
      begin: '',
      end: ''
    });
    
    const editForm = reactive({
      id: null,
      roomId: null,
      title: '',
      timeRange: [],
      attendees: '',
      description: '',
      startTime: '',
      endTime: ''
    });
    
    const editRules = {
      roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
      title: [{ required: true, message: '请输入会议主题', trigger: 'blur' }],
      timeRange: [{ required: true, message: '请选择时间范围', trigger: 'change' }]
    };
    
    const statusOptions = [
      { value: 0, label: '待审批' },
      { value: 1, label: '已批准' },
      { value: 2, label: '已拒绝' },
      { value: 3, label: '已取消' }
    ];
    
    const dateShortcuts = [
      {
        text: '最近一周',
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
          return [start, end];
        }
      },
      {
        text: '最近一个月',
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
          return [start, end];
        }
      },
      {
        text: '最近三个月',
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
          return [start, end];
        }
      }
    ];
    
    // 监听日期变化
    watch(dateRange, (newVal) => {
      if (newVal && newVal.length === 2) {
        searchForm.begin = newVal[0];
        searchForm.end = newVal[1];
      } else {
        searchForm.begin = '';
        searchForm.end = '';
      }
    });
    
    // 加载会议室数据
    const loadRooms = async () => {
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
    };
    
    // 获取会议室名称
    const getRoomName = (roomId) => {
      if (!roomId) return '未知会议室';
      const room = roomOptions.value.find(r => r.id === roomId);
      return room ? room.name : '未知会议室';
    };
    
    // 获取会议室完整信息
    const getRoomFullInfo = (booking) => {
      if (!booking || !booking.roomId) return '未知会议室';
      const room = roomOptions.value.find(r => r.id === booking.roomId);
      if (!room) return '未知会议室';
      return `${room.name} (位置: ${room.location}, 容量: ${room.capacity}人)`;
    };
    
    // 获取状态文本
    const getStatusText = (status) => {
      switch (status) {
        case 0: return '待审批';
        case 1: return '已批准';
        case 2: return '已拒绝';
        case 3: return '已取消';
        default: return '未知状态';
      }
    };
    
    // 获取状态类型
    const getStatusType = (status) => {
      switch (status) {
        case 0: return 'warning';
        case 1: return 'success';
        case 2: return 'danger';
        case 3: return 'info';
        default: return '';
      }
    };
    
    // 格式化日期时间
    const formatDateTime = (datetime) => {
      if (!datetime) return '';
      return datetime.replace('T', ' ').substring(0, 16);
    };
    
    // 加载预订数据 - 管理员可以看到所有预约
    const loadBookings = async () => {
      try {
        loading.value = true;
        
        // 处理状态过滤
        let status = searchForm.status;
        if (activeTab.value === 'pending') status = 0;
        else if (activeTab.value === 'approved') status = 1;
        else if (activeTab.value === 'rejected') status = 2;
        else if (activeTab.value === 'cancelled') status = 3;
        
        const params = {
          page: currentPage.value,
          pageSize: pageSize.value,
          roomId: searchForm.roomId,
          status: status,
          begin: searchForm.begin,
          end: searchForm.end,
          username: searchForm.username || undefined
          // 管理员可查看所有预约，不传userId参数
        };
        
        const res = await getBookingList(params);
        
        if (res.code === 1) {
          bookingList.value = res.data.rows || [];
          total.value = res.data.total || 0;
        } else {
          ElMessage.error(res.msg || '获取预订列表失败');
        }
      } catch (error) {
        console.error('获取预订列表失败:', error);
        ElMessage.error('获取预订列表失败: ' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };
    
    // 处理查询
    const handleSearch = () => {
      currentPage.value = 1;
      loadBookings();
    };
    
    // 重置查询条件
    const resetSearch = () => {
      searchForm.roomId = undefined;
      searchForm.username = '';
      searchForm.status = undefined;
      dateRange.value = [];
      searchForm.begin = '';
      searchForm.end = '';
      currentPage.value = 1;
      activeTab.value = 'all';
      loadBookings();
    };
    
    // 处理查看详情
    const handleView = async (row) => {
      try {
        loading.value = true;
        const res = await getBookingDetail(row.id);
        
        if (res.code === 1) {
          selectedBooking.value = res.data;
          detailVisible.value = true;
        } else {
          ElMessage.error(res.msg || '获取预订详情失败');
        }
      } catch (error) {
        console.error('获取预订详情失败:', error);
        ElMessage.error('获取预订详情失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 处理编辑
    const handleEdit = (row) => {
      selectedBooking.value = row;
      
      // 设置编辑表单的值
      editForm.id = row.id;
      editForm.roomId = row.roomId;
      editForm.title = row.title;
      editForm.timeRange = [row.startTime, row.endTime];
      editForm.attendees = row.attendees || '';
      editForm.description = row.description || '';
      
      // 关闭详情对话框（如果打开）
      if (detailVisible.value) {
        detailVisible.value = false;
      }
      
      // 打开编辑对话框
      editVisible.value = true;
    };
    
    // 提交编辑
    const submitEdit = () => {
      editFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            submitLoading.value = true;
            
            const updateData = {
              roomId: editForm.roomId,
              title: editForm.title,
              startTime: editForm.timeRange[0],
              endTime: editForm.timeRange[1],
              attendees: editForm.attendees,
              description: editForm.description
            };
            
            const res = await updateBooking(editForm.id, updateData);
            
            if (res.code === 1) {
              ElMessage.success('修改预约成功');
              editVisible.value = false;
              loadBookings();
            } else {
              ElMessage.error(res.msg || '修改预约失败');
            }
          } catch (error) {
            console.error('修改预约失败:', error);
            ElMessage.error('修改预约失败');
          } finally {
            submitLoading.value = false;
          }
        }
      });
    };
    
    // 处理取消预订
    const handleCancel = (row) => {
      ElMessageBox.confirm('确定要取消这个预约吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          loading.value = true;
          const res = await cancelBooking(row.id);
          
          if (res.code === 1) {
            ElMessage.success('取消预约成功');
            
            // 关闭详情对话框（如果打开）
            if (detailVisible.value) {
              detailVisible.value = false;
            }
            
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
    };
    
    // 设置活动标签
    const setActiveTab = (tab) => {
      activeTab.value = tab;
      currentPage.value = 1;
      searchForm.status = undefined; // 重置状态条件，让tab设置接管
      loadBookings();
    };
    
    // 处理页面大小变化
    const handleSizeChange = (val) => {
      pageSize.value = val;
      loadBookings();
    };
    
    // 处理当前页变化
    const handleCurrentChange = (val) => {
      currentPage.value = val;
      loadBookings();
    };
    
    onMounted(() => {
      loadRooms();
      loadBookings();
    });
    
    return {
      loading,
      submitLoading,
      bookingList,
      currentPage,
      pageSize,
      total,
      detailVisible,
      editVisible,
      selectedBooking,
      roomOptions,
      searchForm,
      dateRange,
      statusOptions,
      activeTab,
      dateShortcuts,
      editForm,
      editRules,
      editFormRef,
      getRoomName,
      getRoomFullInfo,
      getStatusText,
      getStatusType,
      formatDateTime,
      loadBookings,
      handleSearch,
      resetSearch,
      handleView,
      handleEdit,
      submitEdit,
      handleCancel,
      setActiveTab,
      handleSizeChange,
      handleCurrentChange
    };
  }
};
</script>

<style scoped>
.admin-booking-container {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  text-align: center;
}

.header-content {
  width: 100%;
  text-align: center;
}

.page-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
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

.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.search-buttons {
  margin-left: auto;
}

.data-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.card-subtitle {
  font-size: 13px;
  color: #909399;
}

.table-operations {
  margin: 15px 20px;
}

.header-with-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.time-display-horizontal {
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 15px;
}

@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-buttons {
    margin-left: 0;
    margin-top: 10px;
  }
  
  .table-operations .el-button-group {
    display: flex;
    flex-wrap: wrap;
  }
}
</style> 