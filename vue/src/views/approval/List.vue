<template>
  <div class="approval-container">
    <div class="page-header">
      <div class="header-content">
        <h1>审批管理</h1>
        <div class="header-description">管理会议室预订审批流程</div>
      </div>
    </div>
    
    <el-card class="tabs-card">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="待审批" name="pending">
          <div class="toolbar">
            <el-input 
              v-model="searchInput" 
              placeholder="搜索会议主题/会议室/预订人" 
              style="width: 300px" 
              clearable 
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
          
          <div class="table-wrapper">
            <div class="card-header">
              <span class="card-title">待审批列表</span>
              <span class="card-subtitle">共 {{ pendingTotal }} 条记录</span>
            </div>
            
            <el-table 
              :data="pendingList" 
              border 
              style="width: 100%" 
              v-loading="loading"
              :max-height="tableHeight"
              :header-cell-style="{ 
                background: '#f5f7fa', 
                color: '#606266', 
                fontWeight: 'bold',
                textAlign: 'center'
              }"
            >
              <el-table-column prop="id" label="审批ID" width="160" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><Ticket /></el-icon>
                    <span>审批ID</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="roomName" label="会议室" min-width="120" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><House /></el-icon>
                    <span>会议室</span>
                  </div>
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
              <el-table-column label="预订时间" min-width="300" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><Timer /></el-icon>
                    <span>预订时间</span>
                  </div>
                </template>
                <template #default="scope">
                  <div class="time-display-horizontal">
                    {{ formatDateTime(scope.row.startTime) }} - {{ formatTime(scope.row.endTime) }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" align="center">
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
              <el-table-column prop="applicantName" label="预订人" width="120" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><User /></el-icon>
                    <span>预订人</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280" align="center" fixed="right">
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
                      详情
                    </el-button>
                    <el-button size="small" type="success" @click="handleApprove(scope.row)">
                      <el-icon><Check /></el-icon>
                      批准
                    </el-button>
                    <el-button size="small" type="danger" @click="handleReject(scope.row)">
                      <el-icon><Close /></el-icon>
                      拒绝
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div class="pagination-wrapper">
            <el-pagination
              v-if="pendingTotal > 0"
              class="pagination"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pendingTotal"
              :page-size="pageSize"
              :current-page="currentPage"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              background
            />
            <el-empty v-else description="暂无待审批记录" />
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="已审批" name="approved">
          <div class="toolbar">
            <el-input 
              v-model="searchInput" 
              placeholder="搜索会议主题/会议室/预订人" 
              style="width: 300px" 
              clearable 
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-select v-model="statusFilter" placeholder="审批结果" clearable style="width: 150px">
              <template #prefix>
                <el-icon><Filter /></el-icon>
              </template>
              <el-option label="全部" value="" />
              <el-option label="已批准" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              :shortcuts="dateShortcuts"
              style="width: 300px"
            >
              <template #prefix>
                <el-icon><Calendar /></el-icon>
              </template>
            </el-date-picker>
          </div>
          
          <div class="table-wrapper">
            <div class="card-header">
              <span class="card-title">已审批列表</span>
              <span class="card-subtitle">共 {{ approvedTotal }} 条记录</span>
            </div>
            
            <el-table 
              :data="approvedList" 
              border 
              style="width: 100%" 
              v-loading="loading"
              :max-height="tableHeight"
              :header-cell-style="{ 
                background: '#f5f7fa', 
                color: '#606266', 
                fontWeight: 'bold',
                textAlign: 'center'
              }"
            >
              <el-table-column prop="id" label="审批ID" width="160" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><Ticket /></el-icon>
                    <span>审批ID</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="roomName" label="会议室" min-width="120" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><House /></el-icon>
                    <span>会议室</span>
                  </div>
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
              <el-table-column label="预订时间" min-width="300" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><Timer /></el-icon>
                    <span>预订时间</span>
                  </div>
                </template>
                <template #default="scope">
                  <div class="time-display-horizontal">
                    {{ formatDateTime(scope.row.startTime) }} - {{ formatTime(scope.row.endTime) }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="审批结果" width="120" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><InfoFilled /></el-icon>
                    <span>审批结果</span>
                  </div>
                </template>
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" effect="light">
                    {{ scope.row.status === 1 ? '已批准' : '已拒绝' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="updateTime" label="审批时间" width="180" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><Stopwatch /></el-icon>
                    <span>审批时间</span>
                  </div>
                </template>
                <template #default="scope">
                  {{ formatDateTime(scope.row.updateTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="applicantName" label="预订人" width="120" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><User /></el-icon>
                    <span>预订人</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center" fixed="right">
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
                      详情
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div class="pagination-wrapper">
            <el-pagination
              v-if="approvedTotal > 0"
              class="pagination"
              layout="total, sizes, prev, pager, next, jumper"
              :total="approvedTotal"
              :page-size="pageSize"
              :current-page="currentPage"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              background
            />
            <el-empty v-else description="暂无审批记录" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 预订详情对话框 -->
    <el-dialog 
      title="预订详情" 
      v-model="detailVisible" 
      width="700px"
      :close-on-click-modal="false"
      :append-to-body="true"
      destroy-on-close
    >
      <div v-if="selectedBooking">
        <el-descriptions border>
          <el-descriptions-item label="会议室">{{ selectedBooking.roomName }}</el-descriptions-item>
          <el-descriptions-item label="会议主题">{{ selectedBooking.title }}</el-descriptions-item>
          <el-descriptions-item label="预订人">{{ selectedBooking.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="预订日期">{{ formatDate(selectedBooking.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="时间段">
            {{ formatTime(selectedBooking.startTime) }} - {{ formatTime(selectedBooking.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedBooking.status)">
              {{ getStatusText(selectedBooking.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(selectedBooking.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ selectedBooking.description || '无' }}</el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2" v-if="selectedBooking.comment">
            {{ selectedBooking.comment }}
          </el-descriptions-item>
          <el-descriptions-item label="审批时间" v-if="selectedBooking.status !== 0">
            {{ formatDateTime(selectedBooking.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog 
      :title="approvalAction === 'approve' ? '批准预订' : '拒绝预订'" 
      v-model="approvalVisible" 
      width="500px"
      :close-on-click-modal="false"
      :append-to-body="true"
      destroy-on-close
    >
      <el-form :model="approvalForm" ref="approvalFormRef">
        <el-form-item label="审批意见" prop="comment" label-width="100px">
          <el-input 
            type="textarea" 
            v-model="approvalForm.comment" 
            rows="3" 
            placeholder="请输入审批意见（可选）" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approvalVisible = false">取消</el-button>
          <el-button 
            :type="approvalAction === 'approve' ? 'success' : 'danger'" 
            @click="submitApproval" 
            :loading="submitLoading"
          >
            确定{{ approvalAction === 'approve' ? '批准' : '拒绝' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { getPendingApprovals, approveBooking, getApprovedList } from '@/api/approval';
import { 
  Search, 
  View, 
  Check, 
  Close, 
  Calendar, 
  House, 
  Document, 
  Timer, 
  InfoFilled, 
  Setting,
  User,
  Filter,
  Ticket,
  Stopwatch
} from '@element-plus/icons-vue';

export default {
  name: 'ApprovalList',
  components: {
    Search, 
    View, 
    Check, 
    Close, 
    Calendar, 
    House, 
    Document, 
    Timer, 
    InfoFilled, 
    Setting,
    User,
    Filter,
    Ticket,
    Stopwatch
  },
  setup() {
    const loading = ref(false);
    const searchInput = ref('');
    const statusFilter = ref('');
    const activeTab = ref('pending');
    const currentPage = ref(1);
    const pageSize = ref(10);
    const pendingTotal = ref(0);
    const approvedTotal = ref(0);
    const pendingList = ref([]);
    const approvedList = ref([]);
    const dateRange = ref([]);
    const tableHeight = ref('450px');
    
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
      status: 1, // 默认通过
      comment: ''
    });
    
    // 日期快捷选项
    const dateShortcuts = [
      {
        text: '最近一周',
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
          return [start, end];
        },
      },
      {
        text: '最近一个月',
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
          return [start, end];
        },
      },
      {
        text: '最近三个月',
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
          return [start, end];
        },
      },
    ];

    // 调整表格高度
    const adjustTableHeight = () => {
      nextTick(() => {
        // 根据窗口高度动态设置表格高度，减去页面其他元素的高度
        const windowHeight = window.innerHeight;
        const headerHeight = 60; // 页面头部高度
        const tabsHeaderHeight = 40; // 标签页头部高度
        const toolbarHeight = 60; // 工具栏高度
        const paginationHeight = 50; // 分页组件高度
        const padding = 80; // 其他内外边距总和
        
        // 计算可用高度
        const availableHeight = windowHeight - headerHeight - tabsHeaderHeight - toolbarHeight - paginationHeight - padding;
        
        // 确保最低高度但降低最小值
        tableHeight.value = `${Math.max(300, availableHeight)}px`;  // 从450px降低到300px
      });
    };
    
    // 加载待审批列表
    const loadPendingList = async () => {
      loading.value = true;
      try {
        const params = {
          page: currentPage.value,
          pageSize: pageSize.value
        };
        
        if (searchInput.value) {
          params.search = searchInput.value;
        }
        
        const res = await getPendingApprovals(params);
        
        if (res.code === 1) {
          pendingList.value = res.data.rows || [];
          pendingTotal.value = res.data.total || 0;
        } else {
          ElMessage.error(res.msg || '获取待审批列表失败');
        }
      } catch (error) {
        console.error('获取待审批列表失败:', error);
        ElMessage.error('获取待审批列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 加载已审批列表
    const loadApprovedList = async () => {
      loading.value = true;
      try {
        const params = {
          page: currentPage.value,
          pageSize: pageSize.value
        };
        
        if (searchInput.value) {
          params.search = searchInput.value;
        }
        
        // 添加状态过滤
        if (statusFilter.value) {
          params.status = statusFilter.value;
        }
        
        // 添加日期范围过滤
        if (dateRange.value && dateRange.value.length === 2) {
          params.startDate = dateRange.value[0];
          params.endDate = dateRange.value[1];
        }
        
        const res = await getApprovedList(params);
        
        if (res.code === 1) {
          approvedList.value = res.data.rows || [];
          approvedTotal.value = res.data.total || 0;
        } else {
          ElMessage.error(res.msg || '获取已审批列表失败');
        }
      } catch (error) {
        console.error('获取已审批列表失败:', error);
        ElMessage.error('获取已审批列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 窗口大小变化监听
    let resizeHandler = null;
    
    onMounted(() => {
      loadPendingList();
      adjustTableHeight();
      
      // 添加窗口大小变化监听
      resizeHandler = () => adjustTableHeight();
      window.addEventListener('resize', resizeHandler);
    });
    
    onUnmounted(() => {
      // 移除窗口大小变化监听
      if (resizeHandler) {
        window.removeEventListener('resize', resizeHandler);
      }
    });
    
    // 处理标签页切换
    const handleTabChange = (tab) => {
      currentPage.value = 1;
      if (tab.props.name === 'pending') {
        loadPendingList();
      } else {
        loadApprovedList();
      }
    };
    
    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    };
    
    // 格式化时间
    const formatTime = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };
    
    // 格式化日期时间
    const formatDateTime = (dateStr) => {
      if (!dateStr) return '';
      return `${formatDate(dateStr)} ${formatTime(dateStr)}`;
    };
    
    // 获取状态类型
    const getStatusType = (status) => {
      switch (status) {
        case 0: return 'warning';
        case 1: return 'success';
        case 2: return 'danger';
        default: return 'info';
      }
    };
    
    // 获取状态文本
    const getStatusText = (status) => {
      switch (status) {
        case 0: return '待审批';
        case 1: return '已批准';
        case 2: return '已拒绝';
        default: return '未知状态';
      }
    };
    
    // 搜索
    const handleSearch = () => {
      currentPage.value = 1;
      if (activeTab.value === 'pending') {
        loadPendingList();
      } else {
        loadApprovedList();
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
      approvalForm.status = 1; // 批准
      approvalForm.comment = '';
      approvalVisible.value = true;
    };
    
    // 拒绝预订
    const handleReject = (row) => {
      approvalAction.value = 'reject';
      approvalForm.id = row.id;
      approvalForm.status = 2; // 拒绝
      approvalForm.comment = '';
      approvalVisible.value = true;
    };
    
    // 提交审批
    const submitApproval = async () => {
      submitLoading.value = true;
      try {
        const data = {
          status: approvalForm.status,
          comment: approvalForm.comment
        };
        
        const res = await approveBooking(approvalForm.id, data);
        
        if (res.code === 1) {
          const actionText = approvalAction.value === 'approve' ? '批准' : '拒绝';
          ElMessage.success(`${actionText}预订成功`);
          approvalVisible.value = false;
          
          // 重新加载列表
          loadPendingList();
        } else {
          ElMessage.error(res.msg || '审批失败');
        }
      } catch (error) {
        console.error('审批失败:', error);
        const actionText = approvalAction.value === 'approve' ? '批准' : '拒绝';
        ElMessage.error(`${actionText}预订失败`);
      } finally {
        submitLoading.value = false;
      }
    };
    
    // 分页处理
    const handleSizeChange = (val) => {
      pageSize.value = val;
      if (activeTab.value === 'pending') {
        loadPendingList();
      } else {
        loadApprovedList();
      }
    };
    
    const handleCurrentChange = (val) => {
      currentPage.value = val;
      if (activeTab.value === 'pending') {
        loadPendingList();
      } else {
        loadApprovedList();
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
      approvedTotal,
      pendingList,
      approvedList,
      dateRange,
      dateShortcuts,
      tableHeight,
      
      detailVisible,
      selectedBooking,
      
      approvalVisible,
      approvalAction,
      submitLoading,
      approvalForm,
      approvalFormRef,
      
      handleTabChange,
      formatDate,
      formatTime,
      formatDateTime,
      getStatusType,
      getStatusText,
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

.tabs-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  padding: 0;
  overflow: hidden;
}

.toolbar {
  margin: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.table-wrapper {
  width: 100%;
  overflow: hidden;
  margin-bottom: 0;  /* 减少表格底部边距 */
}

.el-table {
  margin-bottom: 0;  /* 确保表格本身没有底部边距 */
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
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

.pagination-wrapper {
  margin-top: 5px;  /* 进一步减少到5px */
  padding: 0 20px;  /* 与表格对齐 */
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 5px 0;  /* 减少上下内边距 */
}

/* 当表格数据少于5条时，增加最小高度确保页面布局美观 */
.el-table:deep(.el-table__body-wrapper) {
  min-height: 100px;
}

/* 设置表格最大高度的媒体查询以适应不同屏幕 */
@media (max-height: 900px) {
  .el-table {
    max-height: 400px !important;
  }
}

@media (max-height: 700px) {
  .el-table {
    max-height: 300px !important;
  }
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .action-buttons {
    flex-direction: column;
    width: 100%;
  }
  
  .action-buttons .el-button {
    width: 100%;
    margin-bottom: 5px;
  }
}
</style> 