<template>
  <div class="booking-container">
    <h1>{{ isAdmin ? '预订管理' : '我的预订' }}</h1>
    <div class="toolbar">
      <el-input v-model="searchInput" placeholder="搜索预订" style="width: 300px" clearable />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-select v-model="statusFilter" placeholder="预订状态" clearable style="width: 150px">
        <el-option label="全部" value="" />
        <el-option label="待审批" value="0" />
        <el-option label="已批准" value="1" />
        <el-option label="已拒绝" value="2" />
        <el-option label="已取消" value="3" />
      </el-select>
    </div>
    <el-table :data="bookingList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roomName" label="会议室" />
      <el-table-column prop="title" label="会议主题" />
      <el-table-column label="预订时间">
        <template #default="scope">
          {{ formatDate(scope.row.date) }} {{ scope.row.startTime }}-{{ scope.row.endTime }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column v-if="isAdmin" prop="username" label="预订人" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleView(scope.row)">详情</el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="handleCancel(scope.row)" 
            v-if="scope.row.status === 0 || scope.row.status === 1"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 预订详情对话框 -->
    <el-dialog title="预订详情" v-model="detailVisible" width="700px">
      <div v-if="selectedBooking">
        <el-descriptions border>
          <el-descriptions-item label="会议室">{{ selectedBooking.roomName }}</el-descriptions-item>
          <el-descriptions-item label="会议主题">{{ selectedBooking.title }}</el-descriptions-item>
          <el-descriptions-item label="预订人" v-if="isAdmin">{{ selectedBooking.username }}</el-descriptions-item>
          <el-descriptions-item label="预订日期">{{ formatDate(selectedBooking.date) }}</el-descriptions-item>
          <el-descriptions-item label="时间段">
            {{ selectedBooking.startTime }} - {{ selectedBooking.endTime }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedBooking.status)">
              {{ getStatusText(selectedBooking.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedBooking.createTime }}</el-descriptions-item>
          <el-descriptions-item label="会议描述" :span="2">{{ selectedBooking.description }}</el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2" v-if="selectedBooking.comment">
            {{ selectedBooking.comment }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user';

export default {
  name: 'BookingManagementPage',
  setup() {
    const userStore = useUserStore();
    const loading = ref(false);
    const searchInput = ref('');
    const statusFilter = ref('');
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    const bookingList = ref([]);
    
    // 预订详情相关
    const detailVisible = ref(false);
    const selectedBooking = ref(null);
    
    // 是否是管理员
    const isAdmin = computed(() => {
      return userStore.userInfo?.roleId === 1;
    });
    
    // 加载预订列表
    const loadBookingList = async () => {
      loading.value = true;
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          total.value = 0;
          bookingList.value = [];
          loading.value = false;
        }, 500);
      } catch (error) {
        console.error('获取预订列表失败:', error);
        ElMessage.error('获取预订列表失败');
        loading.value = false;
      }
    };
    
    onMounted(() => {
      loadBookingList();
    });
    
    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    };
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '待审批',
        1: '已批准',
        2: '已拒绝',
        3: '已取消'
      };
      return statusMap[status] || '未知';
    };
    
    // 获取状态标签类型
    const getStatusType = (status) => {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'danger',
        3: 'info'
      };
      return typeMap[status] || 'info';
    };
    
    // 搜索
    const handleSearch = () => {
      currentPage.value = 1;
      loadBookingList();
    };
    
    // 查看预订详情
    const handleView = (row) => {
      selectedBooking.value = row;
      detailVisible.value = true;
    };
    
    // 取消预订
    const handleCancel = (row) => {
      ElMessageBox.confirm(
        `确定要取消会议室 ${row.roomName} 的预订吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          // 这里将来替换为API调用
          await new Promise(resolve => setTimeout(resolve, 500));
          ElMessage.success('取消预订成功');
          loadBookingList();
        } catch (error) {
          ElMessage.error('取消预订失败');
        }
      }).catch(() => {});
    };
    
    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size;
      loadBookingList();
    };
    
    const handleCurrentChange = (page) => {
      currentPage.value = page;
      loadBookingList();
    };
    
    return {
      isAdmin,
      loading,
      searchInput,
      statusFilter,
      bookingList,
      currentPage,
      pageSize,
      total,
      detailVisible,
      selectedBooking,
      formatDate,
      getStatusText,
      getStatusType,
      handleSearch,
      handleView,
      handleCancel,
      handleSizeChange,
      handleCurrentChange
    };
  }
};
</script>

<style scoped>
.booking-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style> 