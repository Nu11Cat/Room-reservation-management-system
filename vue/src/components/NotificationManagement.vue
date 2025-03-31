<template>
  <div class="notification-container">
    <h1>通知中心</h1>
    <div class="tabs-container">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="未读通知" name="unread">
          <div class="toolbar">
            <el-input v-model="searchInput" placeholder="搜索通知" style="width: 300px" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button type="success" @click="handleAllRead" v-if="unreadList.length > 0">全部标为已读</el-button>
          </div>
          <el-empty description="暂无未读通知" v-if="unreadList.length === 0 && !loading" />
          <el-table :data="unreadList" border style="width: 100%" v-loading="loading" v-else>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="content" label="内容" :show-overflow-tooltip="true" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag :type="getTypeTag(scope.row.type)">
                  {{ getTypeText(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleView(scope.row)">查看</el-button>
                <el-button size="small" type="info" @click="handleMarkRead(scope.row)">标为已读</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            layout="total, sizes, prev, pager, next, jumper"
            :total="unreadTotal"
            :page-size="pageSize"
            :current-page="currentPage"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            v-if="unreadList.length > 0"
          />
        </el-tab-pane>
        <el-tab-pane label="全部通知" name="all">
          <div class="toolbar">
            <el-input v-model="searchInput" placeholder="搜索通知" style="width: 300px" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-select v-model="typeFilter" placeholder="通知类型" clearable style="width: 150px">
              <el-option label="全部" value="" />
              <el-option label="系统通知" value="0" />
              <el-option label="预订通知" value="1" />
              <el-option label="审批通知" value="2" />
            </el-select>
            <el-select v-model="readFilter" placeholder="已读状态" clearable style="width: 150px">
              <el-option label="全部" value="" />
              <el-option label="已读" value="1" />
              <el-option label="未读" value="0" />
            </el-select>
          </div>
          <el-empty description="暂无通知" v-if="allList.length === 0 && !loading" />
          <el-table :data="allList" border style="width: 100%" v-loading="loading" v-else>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="content" label="内容" :show-overflow-tooltip="true" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag :type="getTypeTag(scope.row.type)">
                  {{ getTypeText(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.status === 1 ? 'info' : 'danger'">
                  {{ scope.row.status === 1 ? '已读' : '未读' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleView(scope.row)">查看</el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="handleDelete(scope.row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            layout="total, sizes, prev, pager, next, jumper"
            :total="allTotal"
            :page-size="pageSize"
            :current-page="currentPage"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            v-if="allList.length > 0"
          />
        </el-tab-pane>
        <el-tab-pane label="发送通知" name="send" v-if="canSendNotification">
          <div class="send-form">
            <el-form :model="notificationForm" :rules="notificationRules" ref="notificationFormRef" label-width="100px">
              <el-form-item label="接收者类型" prop="receiverType">
                <el-radio-group v-model="notificationForm.receiverType">
                  <el-radio :label="0">指定用户</el-radio>
                  <el-radio :label="1">指定角色</el-radio>
                  <el-radio :label="2">所有用户</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="接收者" prop="receiver" v-if="notificationForm.receiverType === 0">
                <el-select v-model="notificationForm.receiverId" placeholder="请选择接收者" filterable>
                  <el-option 
                    v-for="user in userList" 
                    :key="user.id" 
                    :label="user.username" 
                    :value="user.id" 
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="接收角色" prop="roleId" v-if="notificationForm.receiverType === 1">
                <el-select v-model="notificationForm.roleId" placeholder="请选择接收角色">
                  <el-option label="管理员" :value="1" />
                  <el-option label="普通用户" :value="2" />
                  <el-option label="审批人" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item label="通知类型" prop="type">
                <el-select v-model="notificationForm.type" placeholder="请选择通知类型">
                  <el-option label="系统通知" :value="0" />
                  <el-option label="预订通知" :value="1" />
                  <el-option label="审批通知" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item label="标题" prop="title">
                <el-input v-model="notificationForm.title" />
              </el-form-item>
              <el-form-item label="内容" prop="content">
                <el-input type="textarea" v-model="notificationForm.content" rows="5" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitNotification" :loading="submitLoading">发送通知</el-button>
                <el-button @click="resetNotificationForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 通知详情对话框 -->
    <el-dialog title="通知详情" v-model="detailVisible" width="600px">
      <div v-if="selectedNotification" class="notification-detail">
        <h3 class="notification-title">{{ selectedNotification.title }}</h3>
        <div class="notification-meta">
          <el-tag :type="getTypeTag(selectedNotification.type)" size="small">
            {{ getTypeText(selectedNotification.type) }}
          </el-tag>
          <span class="notification-time">{{ selectedNotification.createTime }}</span>
        </div>
        <div class="notification-content">
          {{ selectedNotification.content }}
        </div>
        <div class="notification-actions" v-if="selectedNotification.status === 0">
          <el-button type="primary" size="small" @click="handleMarkDetailRead">标为已读</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user';

export default {
  name: 'NotificationManagementPage',
  setup() {
    const userStore = useUserStore();
    const loading = ref(false);
    const searchInput = ref('');
    const typeFilter = ref('');
    const readFilter = ref('');
    const activeTab = ref('unread');
    const currentPage = ref(1);
    const pageSize = ref(10);
    const unreadTotal = ref(0);
    const allTotal = ref(0);
    const unreadList = ref([]);
    const allList = ref([]);
    const userList = ref([]);
    
    // 详情相关
    const detailVisible = ref(false);
    const selectedNotification = ref(null);
    
    // 发送通知相关
    const submitLoading = ref(false);
    const notificationFormRef = ref(null);
    const notificationForm = reactive({
      receiverType: 0, // 0-指定用户, 1-指定角色, 2-所有用户
      receiverId: null,
      roleId: null,
      type: 0, // 0-系统通知, 1-预订通知, 2-审批通知
      title: '',
      content: ''
    });
    
    const notificationRules = {
      receiverId: [
        { required: true, message: '请选择接收者', trigger: 'change', 
          validator: (rule, value, callback) => {
            if (notificationForm.receiverType === 0 && !value) {
              callback(new Error('请选择接收者'));
            } else {
              callback();
            }
          }
        }
      ],
      roleId: [
        { required: true, message: '请选择接收角色', trigger: 'change',
          validator: (rule, value, callback) => {
            if (notificationForm.receiverType === 1 && !value) {
              callback(new Error('请选择接收角色'));
            } else {
              callback();
            }
          }
        }
      ],
      type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
      title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
      content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
    };
    
    // 是否可以发送通知（管理员和审批人）
    const canSendNotification = computed(() => {
      const roleId = userStore.userInfo?.roleId;
      return roleId === 1 || roleId === 3;
    });
    
    // 加载未读通知列表
    const loadUnreadList = async () => {
      loading.value = true;
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          unreadTotal.value = 0;
          unreadList.value = [];
          loading.value = false;
        }, 500);
      } catch (error) {
        console.error('获取未读通知列表失败:', error);
        ElMessage.error('获取未读通知列表失败');
        loading.value = false;
      }
    };
    
    // 加载全部通知列表
    const loadAllList = async () => {
      loading.value = true;
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          allTotal.value = 0;
          allList.value = [];
          loading.value = false;
        }, 500);
      } catch (error) {
        console.error('获取全部通知列表失败:', error);
        ElMessage.error('获取全部通知列表失败');
        loading.value = false;
      }
    };
    
    // 加载用户列表（用于选择接收者）
    const loadUserList = async () => {
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          userList.value = [];
        }, 500);
      } catch (error) {
        console.error('获取用户列表失败:', error);
        ElMessage.error('获取用户列表失败');
      }
    };
    
    onMounted(() => {
      loadUnreadList();
      if (canSendNotification.value) {
        loadUserList();
      }
    });
    
    // 处理标签页切换
    const handleTabChange = (tab) => {
      if (tab.props.name === 'unread') {
        loadUnreadList();
      } else if (tab.props.name === 'all') {
        loadAllList();
      }
    };
    
    // 获取通知类型标签
    const getTypeTag = (type) => {
      const tagMap = {
        0: '',      // 系统通知
        1: 'info',  // 预订通知
        2: 'success' // 审批通知
      };
      return tagMap[type] || '';
    };
    
    // 获取通知类型文本
    const getTypeText = (type) => {
      const textMap = {
        0: '系统通知',
        1: '预订通知',
        2: '审批通知'
      };
      return textMap[type] || '未知类型';
    };
    
    // 搜索
    const handleSearch = () => {
      currentPage.value = 1;
      if (activeTab.value === 'unread') {
        loadUnreadList();
      } else {
        loadAllList();
      }
    };
    
    // 查看通知详情
    const handleView = (row) => {
      selectedNotification.value = row;
      detailVisible.value = true;
      
      // 如果通知是未读状态，自动标记为已读
      if (row.status === 0) {
        // 这里将来替换为API调用
        // markAsRead(row.id);
      }
    };
    
    // 标记为已读（详情页面）
    const handleMarkDetailRead = async () => {
      try {
        // 这里将来替换为API调用
        await new Promise(resolve => setTimeout(resolve, 500));
        
        ElMessage.success('已标记为已读');
        detailVisible.value = false;
        
        // 刷新列表
        if (activeTab.value === 'unread') {
          loadUnreadList();
        } else {
          loadAllList();
        }
      } catch (error) {
        ElMessage.error('操作失败');
      }
    };
    
    // 标记单条通知为已读
    const handleMarkRead = async (row) => {
      try {
        // 这里将来替换为API调用
        await new Promise(resolve => setTimeout(resolve, 500));
        
        ElMessage.success(`已将通知"${row.title}"标记为已读`);
        
        // 刷新列表
        loadUnreadList();
      } catch (error) {
        ElMessage.error('操作失败');
      }
    };
    
    // 标记全部为已读
    const handleAllRead = async () => {
      ElMessageBox.confirm(
        '确定要将所有未读通知标记为已读吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          // 这里将来替换为API调用
          await new Promise(resolve => setTimeout(resolve, 500));
          
          ElMessage.success('已全部标记为已读');
          loadUnreadList();
        } catch (error) {
          ElMessage.error('操作失败');
        }
      }).catch(() => {});
    };
    
    // 删除通知
    const handleDelete = (row) => {
      ElMessageBox.confirm(
        `确定要删除通知"${row.title}"吗？`,
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
          
          ElMessage.success('删除成功');
          
          if (activeTab.value === 'unread') {
            loadUnreadList();
          } else {
            loadAllList();
          }
        } catch (error) {
          ElMessage.error('删除失败');
        }
      }).catch(() => {});
    };
    
    // 提交发送通知
    const submitNotification = () => {
      notificationFormRef.value.validate(async (valid) => {
        if (valid) {
          submitLoading.value = true;
          try {
            // 这里将来替换为API调用
            await new Promise(resolve => setTimeout(resolve, 500));
            
            ElMessage.success('通知发送成功');
            resetNotificationForm();
          } catch (error) {
            ElMessage.error('通知发送失败');
          } finally {
            submitLoading.value = false;
          }
        }
      });
    };
    
    // 重置通知表单
    const resetNotificationForm = () => {
      notificationForm.receiverType = 0;
      notificationForm.receiverId = null;
      notificationForm.roleId = null;
      notificationForm.type = 0;
      notificationForm.title = '';
      notificationForm.content = '';
      if (notificationFormRef.value) {
        notificationFormRef.value.resetFields();
      }
    };
    
    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size;
      if (activeTab.value === 'unread') {
        loadUnreadList();
      } else {
        loadAllList();
      }
    };
    
    const handleCurrentChange = (page) => {
      currentPage.value = page;
      if (activeTab.value === 'unread') {
        loadUnreadList();
      } else {
        loadAllList();
      }
    };
    
    return {
      loading,
      searchInput,
      typeFilter,
      readFilter,
      activeTab,
      currentPage,
      pageSize,
      unreadTotal,
      allTotal,
      unreadList,
      allList,
      userList,
      detailVisible,
      selectedNotification,
      submitLoading,
      notificationForm,
      notificationRules,
      notificationFormRef,
      canSendNotification,
      getTypeTag,
      getTypeText,
      handleTabChange,
      handleSearch,
      handleView,
      handleMarkRead,
      handleMarkDetailRead,
      handleAllRead,
      handleDelete,
      submitNotification,
      resetNotificationForm,
      handleSizeChange,
      handleCurrentChange
    };
  }
};
</script>

<style scoped>
.notification-container {
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

.send-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.notification-detail {
  padding: 10px;
}

.notification-title {
  font-size: 18px;
  margin-bottom: 10px;
}

.notification-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  color: #909399;
  font-size: 14px;
}

.notification-content {
  padding: 10px 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.notification-actions {
  margin-top: 20px;
  text-align: right;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style> 