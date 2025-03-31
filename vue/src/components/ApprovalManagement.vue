<template>
  <div class="approval-container">
    <h1>审批管理</h1>
    <div class="tabs-container">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="待审批" name="pending">
          <div class="toolbar">
            <el-input v-model="searchInput" placeholder="搜索预订" style="width: 300px" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
          <el-table :data="pendingList" border style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="roomName" label="会议室" />
            <el-table-column prop="title" label="会议主题" />
            <el-table-column label="预订时间">
              <template #default="scope">
                {{ formatDate(scope.row.date) }} {{ scope.row.startTime }}-{{ scope.row.endTime }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="username" label="预订人" width="120" />
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleView(scope.row)">详情</el-button>
                <el-button size="small" type="success" @click="handleApprove(scope.row)">批准</el-button>
                <el-button size="small" type="danger" @click="handleReject(scope.row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            layout="total, sizes, prev, pager, next, jumper"
            :total="pendingTotal"
            :page-size="pageSize"
            :current-page="currentPage"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </el-tab-pane>
        <el-tab-pane label="审批历史" name="history">
          <div class="toolbar">
            <el-input v-model="searchInput" placeholder="搜索预订" style="width: 300px" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-select v-model="statusFilter" placeholder="审批结果" clearable style="width: 150px">
              <el-option label="全部" value="" />
              <el-option label="已批准" value="1" />
              <el-option label="已拒绝" value="2" />
            </el-select>
          </div>
          <el-table :data="historyList" border style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="roomName" label="会议室" />
            <el-table-column prop="title" label="会议主题" />
            <el-table-column label="预订时间">
              <template #default="scope">
                {{ formatDate(scope.row.date) }} {{ scope.row.startTime }}-{{ scope.row.endTime }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="审批结果" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '已批准' : '已拒绝' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approveTime" label="审批时间" width="180" />
            <el-table-column prop="username" label="预订人" width="120" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleView(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            layout="total, sizes, prev, pager, next, jumper"
            :total="historyTotal"
            :page-size="pageSize"
            :current-page="currentPage"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 预订详情对话框 -->
    <el-dialog title="预订详情" v-model="detailVisible" width="700px">
      <div v-if="selectedBooking">
        <el-descriptions border>
          <el-descriptions-item label="会议室">{{ selectedBooking.roomName }}</el-descriptions-item>
          <el-descriptions-item label="会议主题">{{ selectedBooking.title }}</el-descriptions-item>
          <el-descriptions-item label="预订人">{{ selectedBooking.username }}</el-descriptions-item>
          <el-descriptions-item label="预订日期">{{ formatDate(selectedBooking.date) }}</el-descriptions-item>
          <el-descriptions-item label="时间段">
            {{ selectedBooking.startTime }} - {{ selectedBooking.endTime }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedBooking.status === 0 ? 'warning' : (selectedBooking.status === 1 ? 'success' : 'danger')">
              {{ 
                selectedBooking.status === 0 
                  ? '待审批' 
                  : (selectedBooking.status === 1 
                    ? '已批准' 
                    : '已拒绝') 
              }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedBooking.createTime }}</el-descriptions-item>
          <el-descriptions-item label="会议描述" :span="2">{{ selectedBooking.description }}</el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2" v-if="selectedBooking.comment">
            {{ selectedBooking.comment }}
          </el-descriptions-item>
          <el-descriptions-item label="审批时间" v-if="selectedBooking.approveTime">
            {{ selectedBooking.approveTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog :title="approvalAction === 'approve' ? '批准预订' : '拒绝预订'" v-model="approvalVisible" width="500px">
      <el-form :model="approvalForm" ref="approvalFormRef">
        <el-form-item label="审批意见" prop="comment" label-width="100px">
          <el-input type="textarea" v-model="approvalForm.comment" rows="3" placeholder="请输入审批意见（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitApproval" 
          :loading="submitLoading"
        >
          确定{{ approvalAction === 'approve' ? '批准' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

export default {
  name: 'ApprovalManagementPage',
  setup() {
    const loading = ref(false);
    const searchInput = ref('');
    const statusFilter = ref('');
    const activeTab = ref('pending');
    const currentPage = ref(1);
    const pageSize = ref(10);
    const pendingTotal = ref(0);
    const historyTotal = ref(0);
    const pendingList = ref([]);
    const historyList = ref([]);
    
    // 详情相关
    const detailVisible = ref(false);
    const selectedBooking = ref(null);
    
    // 审批相关
    const approvalVisible = ref(false);
    const approvalAction = ref('approve'); // approve 或 reject
    const submitLoading = ref(false);
    const approvalFormRef = ref(null);
    const approvalForm = reactive({
      id: null,
      comment: ''
    });
    
    // 加载待审批列表
    const loadPendingList = async () => {
      loading.value = true;
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          pendingTotal.value = 0;
          pendingList.value = [];
          loading.value = false;
        }, 500);
      } catch (error) {
        console.error('获取待审批列表失败:', error);
        ElMessage.error('获取待审批列表失败');
        loading.value = false;
      }
    };
    
    // 加载审批历史列表
    const loadHistoryList = async () => {
      loading.value = true;
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          historyTotal.value = 0;
          historyList.value = [];
          loading.value = false;
        }, 500);
      } catch (error) {
        console.error('获取审批历史列表失败:', error);
        ElMessage.error('获取审批历史列表失败');
        loading.value = false;
      }
    };
    
    onMounted(() => {
      loadPendingList();
    });
    
    // 处理标签页切换
    const handleTabChange = (tab) => {
      if (tab.props.name === 'pending') {
        loadPendingList();
      } else {
        loadHistoryList();
      }
    };
    
    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    };
    
    // 搜索
    const handleSearch = () => {
      currentPage.value = 1;
      if (activeTab.value === 'pending') {
        loadPendingList();
      } else {
        loadHistoryList();
      }
    };
    
    // 查看详情
    const handleView = (row) => {
      selectedBooking.value = row;
      detailVisible.value = true;
    };
    
    // 批准预订
    const handleApprove = (row) => {
      approvalAction.value = 'approve';
      approvalForm.id = row.id;
      approvalForm.comment = '';
      approvalVisible.value = true;
    };
    
    // 拒绝预订
    const handleReject = (row) => {
      approvalAction.value = 'reject';
      approvalForm.id = row.id;
      approvalForm.comment = '';
      approvalVisible.value = true;
    };
    
    // 提交审批结果
    const submitApproval = async () => {
      submitLoading.value = true;
      try {
        // 这里将来替换为API调用
        await new Promise(resolve => setTimeout(resolve, 500));
        
        const actionText = approvalAction.value === 'approve' ? '批准' : '拒绝';
        ElMessage.success(`${actionText}预订成功`);
        
        approvalVisible.value = false;
        loadPendingList();
      } catch (error) {
        const actionText = approvalAction.value === 'approve' ? '批准' : '拒绝';
        ElMessage.error(`${actionText}预订失败`);
      } finally {
        submitLoading.value = false;
      }
    };
    
    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size;
      if (activeTab.value === 'pending') {
        loadPendingList();
      } else {
        loadHistoryList();
      }
    };
    
    const handleCurrentChange = (page) => {
      currentPage.value = page;
      if (activeTab.value === 'pending') {
        loadPendingList();
      } else {
        loadHistoryList();
      }
    };
    
    return {
      loading,
      searchInput,
      statusFilter,
      activeTab,
      currentPage,
      pageSize,
      pendingTotal,
      historyTotal,
      pendingList,
      historyList,
      detailVisible,
      selectedBooking,
      approvalVisible,
      approvalAction,
      submitLoading,
      approvalForm,
      approvalFormRef,
      formatDate,
      handleTabChange,
      handleSearch,
      handleView,
      handleApprove,
      handleReject,
      submitApproval,
      handleSizeChange,
      handleCurrentChange
    };
  }
};
</script>

<style scoped>
.approval-container {
  padding: 20px;
}

.tabs-container {
  margin-top: 20px;
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