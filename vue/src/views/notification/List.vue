<template>
  <div class="notification-container">
    <div class="page-header">
      <div class="header-content">
        <h1>通知中心</h1>
        <div class="header-description">查看系统消息、预订状态变更及其他通知</div>
      </div>
      <div>
        <el-button type="primary" @click="handlePublish" v-if="hasPermission([1])">
          <el-icon><Plus /></el-icon>发布通知
        </el-button>
      </div>
    </div>
    
    <el-card class="tabs-card">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <!-- 未读通知标签页 -->
        <el-tab-pane label="未读通知" name="unread">
          <div class="toolbar">
            <el-input 
              v-model="searchInput" 
              placeholder="搜索通知" 
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
            <div class="card-header" v-if="unreadList.length > 0">
              <span class="card-title">未读通知列表</span>
              <span class="card-subtitle">共 {{ total }} 条记录</span>
            </div>
            
            <el-empty 
              description="暂无未读通知" 
              v-if="unreadList.length === 0 && !loading" 
            />
            
            <el-table 
              :data="unreadList" 
              border 
              style="width: 100%" 
              v-loading="loading" 
              v-else
              :header-cell-style="{ 
                background: '#f5f7fa', 
                color: '#606266', 
                fontWeight: 'bold',
                textAlign: 'center'
              }"
            >
              <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><Document /></el-icon>
                    <span>标题</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>内容</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="类型" width="120" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><InfoFilled /></el-icon>
                    <span>类型</span>
                  </div>
                </template>
                <template #default="scope">
                  <el-tag :type="getTypeTag(scope.row.type)" effect="light">
                    {{ getTypeText(scope.row.type) }}
                  </el-tag>
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
              <el-table-column label="操作" width="200" align="center" fixed="right">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><Setting /></el-icon>
                    <span>操作</span>
                  </div>
                </template>
                <template #default="scope">
                  <div class="action-buttons">
                    <el-button 
                      size="small" 
                      type="primary" 
                      @click="handleView(scope.row)"
                    >
                      <el-icon><View /></el-icon>
                      查看
                    </el-button>
                    <el-button 
                      size="small" 
                      type="info" 
                      @click="handleMarkRead(scope.row)"
                    >
                      <el-icon><Check /></el-icon>
                      标为已读
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div class="pagination-wrapper" v-if="unreadList.length > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              background
            />
          </div>
        </el-tab-pane>
        
        <!-- 全部通知标签页 -->
        <el-tab-pane label="全部通知" name="all">
          <div class="toolbar">
            <el-input 
              v-model="searchInput" 
              placeholder="搜索通知" 
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
            <el-select 
              v-model="typeFilter" 
              placeholder="通知类型" 
              clearable 
              style="width: 150px"
              @change="handleFilterChange"
            >
              <template #prefix>
                <el-icon><Filter /></el-icon>
              </template>
              <el-option label="全部类型" value="" />
              <el-option label="系统通知" :value="0" />
              <el-option label="预订通知" :value="1" />
              <el-option label="审批通知" :value="2" />
            </el-select>
            <el-select 
              v-model="readFilter" 
              placeholder="已读状态" 
              clearable 
              style="width: 150px"
              @change="handleFilterChange"
            >
              <template #prefix>
                <el-icon><MessageBox /></el-icon>
              </template>
              <el-option label="全部状态" value="" />
              <el-option label="已读" :value="1" />
              <el-option label="未读" :value="0" />
            </el-select>
          </div>
          
          <div class="table-wrapper">
            <div class="card-header" v-if="allList.length > 0">
              <span class="card-title">全部通知列表</span>
              <span class="card-subtitle">共 {{ total }} 条记录</span>
            </div>
            
            <el-empty 
              description="暂无通知" 
              v-if="allList.length === 0 && !loading" 
            />
            
            <el-table 
              :data="allList" 
              border 
              style="width: 100%" 
              v-loading="loading" 
              v-else
              :header-cell-style="{ 
                background: '#f5f7fa', 
                color: '#606266', 
                fontWeight: 'bold',
                textAlign: 'center'
              }"
            >
              <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><Document /></el-icon>
                    <span>标题</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>内容</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="类型" width="120" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><InfoFilled /></el-icon>
                    <span>类型</span>
                  </div>
                </template>
                <template #default="scope">
                  <el-tag :type="getTypeTag(scope.row.type)" effect="light">
                    {{ getTypeText(scope.row.type) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="is_read" label="状态" width="100" align="center">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><MessageBox /></el-icon>
                    <span>状态</span>
                  </div>
                </template>
                <template #default="scope">
                  <el-tag size="small" :type="scope.row.is_read == 1 ? 'success' : 'danger'" effect="light">
                    {{ scope.row.is_read == 1 ? '已读' : '未读' }}
                  </el-tag>
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
              <el-table-column label="操作" width="120" align="center" fixed="right">
                <template #header>
                  <div class="header-with-icon">
                    <el-icon><Setting /></el-icon>
                    <span>操作</span>
                  </div>
                </template>
                <template #default="scope">
                  <div class="action-buttons">
                    <el-button 
                      size="small" 
                      type="primary" 
                      @click="handleView(scope.row)"
                    >
                      <el-icon><View /></el-icon>
                      查看
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div class="pagination-wrapper" v-if="allList.length > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              background
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 通知详情对话框 -->
    <el-dialog 
      v-model="detailVisible" 
      title="通知详情" 
      width="500px"
      destroy-on-close
    >
      <el-skeleton :rows="6" animated v-if="!selectedNotification" />
      
      <div v-else class="notification-detail">
        <h3 class="notification-title">{{ selectedNotification.title }}</h3>
        <div class="notification-meta">
          <el-tag :type="getTypeTag(selectedNotification.type)" size="small">
            {{ getTypeText(selectedNotification.type) }}
          </el-tag>
          <span class="notification-time">{{ formatDateTime(selectedNotification.createTime) }}</span>
        </div>
        <div class="notification-content">
          {{ selectedNotification.content }}
        </div>
        <div class="notification-actions" v-if="selectedNotification.is_read != 1">
          <el-button type="primary" @click="handleMarkDetailRead">
            <el-icon><Check /></el-icon>
            标为已读
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 发布通知对话框 -->
    <el-dialog 
      v-model="publishVisible" 
      title="发布通知" 
      width="600px"
      destroy-on-close
    >
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="100px">
        <el-form-item label="接收者类型" prop="receiverType">
          <el-radio-group v-model="publishForm.receiverType">
            <el-radio :label="0">指定用户</el-radio>
            <el-radio :label="1">指定角色</el-radio>
            <el-radio :label="2">所有用户</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="接收用户" prop="receiverId" v-if="publishForm.receiverType === 0">
          <el-select v-model="publishForm.receiverId" placeholder="请选择接收用户" filterable>
            <el-option 
              v-for="user in userList" 
              :key="user.id" 
              :label="user.username" 
              :value="user.id">
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="接收角色" prop="roleId" v-if="publishForm.receiverType === 1">
          <el-select v-model="publishForm.roleId" placeholder="请选择接收角色">
            <el-option :label="'管理员'" :value="1"></el-option>
            <el-option :label="'普通用户'" :value="2"></el-option>
            <el-option :label="'审批员'" :value="3"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="publishForm.type" placeholder="请选择通知类型">
            <el-option :label="'系统公告'" :value="3"></el-option>
            <el-option :label="'预约状态变更'" :value="1"></el-option>
            <el-option :label="'预约提醒'" :value="2"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>
        
        <el-form-item label="通知内容" prop="content">
          <el-input 
            v-model="publishForm.content" 
            type="textarea" 
            :rows="5" 
            placeholder="请输入通知内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitPublish" :loading="publishLoading">
            确认发布
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getNotificationList, markNotificationRead, sendNotification } from '@/api/notification';
import { getUsers } from '@/api/user';
import { useUserStore } from '@/stores/user';
import { 
  Search, 
  View, 
  Check, 
  Calendar, 
  Document, 
  InfoFilled, 
  Setting,
  Plus,
  Filter,
  MessageBox,
  ChatDotRound
} from '@element-plus/icons-vue';

export default {
  name: 'NotificationList',
  components: {
    Search,
    View,
    Check,
    Calendar,
    Document,
    InfoFilled,
    Setting,
    Plus,
    Filter,
    MessageBox,
    ChatDotRound
  },
  setup() {
    // 基础数据
    const loading = ref(false);
    const activeTab = ref('unread');
    const searchInput = ref('');
    const typeFilter = ref('');
    const readFilter = ref('');
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    const unreadList = ref([]);
    const allList = ref([]);
    
    // 详情相关
    const detailVisible = ref(false);
    const selectedNotification = ref(null);
    
    // 发布通知相关
    const publishVisible = ref(false);
    const publishFormRef = ref(null);
    const submitting = ref(false);
    const userList = ref([]);
    
    const publishForm = ref({
      receiverType: 2,  // 默认为所有用户
      receiverId: null,
      roleId: null,
      type: 3,          // 默认为系统公告
      title: '',
      content: ''
    });
    
    const publishRules = ref({
      title: [
        { required: true, message: '请输入通知标题', trigger: 'blur' },
        { min: 2, max: 50, message: '标题长度在2-50个字符之间', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入通知内容', trigger: 'blur' },
        { min: 5, max: 500, message: '内容长度在5-500个字符之间', trigger: 'blur' }
      ],
      receiverId: [
        { required: true, message: '请选择接收用户', trigger: 'change', 
          validator: (rule, value, callback) => {
            if (publishForm.value.receiverType === 0 && !value) {
              callback(new Error('请选择接收用户'));
            } else {
              callback();
            }
          }
        }
      ],
      roleId: [
        { required: true, message: '请选择接收角色', trigger: 'change',
          validator: (rule, value, callback) => {
            if (publishForm.value.receiverType === 1 && !value) {
              callback(new Error('请选择接收角色'));
            } else {
              callback();
            }
          }
        }
      ],
      type: [
        { required: true, message: '请选择通知类型', trigger: 'change' }
      ]
    });
    
    // 监听标签页变化
    watch(activeTab, () => {
      currentPage.value = 1;
      loadNotifications();
    });
    
    // 获取类型对应的标签类型
    const getTypeTag = (type) => {
      switch (Number(type)) {
        case 1: return 'warning';   // 预约状态变更
        case 2: return 'primary';   // 预约提醒
        case 3: return 'success';   // 系统公告
        default: return 'info';     // 默认
      }
    };
    
    // 获取类型对应的文本
    const getTypeText = (type) => {
      switch (Number(type)) {
        case 1: return '预约状态变更';
        case 2: return '预约提醒';
        case 3: return '系统公告';
        default: return '未知类型';
      }
    };
    
    // 格式化日期时间
    const formatDateTime = (datetime) => {
      if (!datetime) return '';
      return datetime.replace('T', ' ').substring(0, 16);
    };
    
    // 加载通知列表
    const loadNotifications = async () => {
      try {
        loading.value = true;
        
        const params = {
          page: currentPage.value,
          pageSize: pageSize.value
        };
        
        if (typeFilter.value) {
          params.type = typeFilter.value;
        }
        
        if (activeTab.value === 'unread') {
          params.is_read = 0;
          console.log('设置is_read=0筛选未读通知');
        } else if (readFilter.value !== '') {
          // 全部通知标签页，根据过滤条件设置is_read
          params.is_read = readFilter.value;
          console.log(`根据筛选设置is_read=${readFilter.value}`);
        }
        
        if (searchInput.value) {
          params.keyword = searchInput.value;
        }
        
        console.log('请求参数:', params);
        console.log('请求URL: /notification');
        
        const res = await getNotificationList(params);
        console.log('API原始返回:', res);
        
        if (res.code === 1) {
          // 详细打印通知的所有字段，找出正确的读取状态字段
          console.log('API返回通知字段详情:');
          if (res.data && res.data.rows && res.data.rows.length > 0) {
            const firstItem = res.data.rows[0];
            console.log('通知对象所有字段:', Object.keys(firstItem));
            console.log('通知对象完整数据:', firstItem);
          }
          
          // 修复数据，确保每个通知对象都有is_read属性
          const fixNotificationData = (items) => {
            return (items || []).map(item => {
              // 尝试所有可能的属性名
              const readStatus = 
                item.is_read !== undefined ? item.is_read :
                item.isRead !== undefined ? item.isRead : 
                item.read !== undefined ? item.read : 0;
              
              // 返回修复后的对象
              return {
                ...item,
                is_read: readStatus
              };
            });
          };
          
          if (activeTab.value === 'unread') {
            unreadList.value = fixNotificationData(res.data.rows);
          } else {
            allList.value = fixNotificationData(res.data.rows);
          }
          
          total.value = res.data.total || 0;
        } else {
          ElMessage.error(res.msg || '获取通知列表失败');
        }
      } catch (error) {
        console.error('获取通知列表失败:', error);
        ElMessage.error('获取通知列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 处理标签页切换
    const handleTabChange = () => {
      // 切换标签页时重置过滤条件
      searchInput.value = '';
      typeFilter.value = '';
      readFilter.value = '';
      
      // 重新加载通知列表
      loadNotifications();
    };
    
    // 处理搜索
    const handleSearch = () => {
      currentPage.value = 1;
      loadNotifications();
    };
    
    // 处理过滤条件变化
    const handleFilterChange = () => {
      currentPage.value = 1;
      loadNotifications();
    };
    
    // 处理查看通知详情
    const handleView = async (row) => {
      selectedNotification.value = row;
      detailVisible.value = true;
    };
    
    // 处理标记已读
    const handleMarkRead = async (row) => {
      try {
        loading.value = true;
        const res = await markNotificationRead(row.id);
        
        if (res.code === 1) {
          ElMessage.success('已标记为已读');
          // 更新未读通知计数
          const userStore = useUserStore();
          userStore.decrementUnreadCount();
          // 重新加载通知列表
          loadNotifications();
        } else {
          ElMessage.error(res.msg || '标记已读失败');
        }
      } catch (error) {
        console.error('标记已读失败:', error);
        ElMessage.error('标记已读失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 处理标记详情中的通知为已读
    const handleMarkDetailRead = async () => {
      if (!selectedNotification.value) return;
      
      try {
        loading.value = true;
        const res = await markNotificationRead(selectedNotification.value.id);
        
        if (res.code === 1) {
          ElMessage.success('已标记为已读');
          // 更新状态
          selectedNotification.value.is_read = 1;
          // 更新未读通知计数
          const userStore = useUserStore();
          userStore.decrementUnreadCount();
          // 重新加载通知列表
          loadNotifications();
        } else {
          ElMessage.error(res.msg || '标记已读失败');
        }
      } catch (error) {
        console.error('标记已读失败:', error);
        ElMessage.error('标记已读失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 处理分页大小变化
    const handleSizeChange = (val) => {
      pageSize.value = val;
      loadNotifications();
    };
    
    // 处理当前页变化
    const handleCurrentChange = (val) => {
      currentPage.value = val;
      loadNotifications();
    };
    
    // 加载用户列表
    const loadUsers = async () => {
      try {
        const res = await getUsers();
        if (res.code === 1 && res.data) {
          userList.value = res.data;
        } else {
          console.error('获取用户列表失败:', res);
        }
      } catch (error) {
        console.error('获取用户列表出错:', error);
      }
    };
    
    // 处理发布通知点击
    const handlePublish = () => {
      loadUsers();
      publishVisible.value = true;
    };
    
    // 提交发布通知
    const handleSubmitPublish = () => {
      console.log('handleSubmitPublish 方法被调用');
      publishFormRef.value.validate(async (valid) => {
        console.log('表单验证结果:', valid);
        if (valid) {
          submitting.value = true;
          try {
            console.log('即将调用 sendNotification 方法，数据:', publishForm.value);
            const res = await sendNotification(publishForm.value);
            console.log('sendNotification 返回结果:', res);
            if (res.code === 1) {
              ElMessage.success('通知发布成功');
              publishVisible.value = false;
              publishForm.value = {
                receiverType: 2,
                receiverId: null,
                roleId: null,
                type: 3,
                title: '',
                content: ''
              };
              loadNotifications();
            } else {
              ElMessage.error(res.msg || '发布失败，请重试');
            }
          } catch (error) {
            console.error('发布通知出错:', error);
            ElMessage.error('发布失败，请重试');
          } finally {
            submitting.value = false;
          }
        } else {
          ElMessage.warning('请填写完整的通知信息');
        }
      });
    };
    
    // 检查权限
    const userStore = useUserStore();
    const hasPermission = (roles) => {
      if (!userStore.userInfo || !userStore.userInfo.roleId) return false;
      return roles.includes(userStore.userInfo.roleId);
    };
    
    onMounted(() => {
      loadNotifications();
    });
    
    return {
      loading,
      activeTab,
      searchInput,
      typeFilter,
      readFilter,
      currentPage,
      pageSize,
      total,
      unreadList,
      allList,
      detailVisible,
      selectedNotification,
      publishVisible,
      publishForm,
      publishRules,
      publishFormRef,
      submitting,
      userList,
      getTypeTag,
      getTypeText,
      formatDateTime,
      handleTabChange,
      handleSearch,
      handleFilterChange,
      handleView,
      handleMarkRead,
      handleMarkDetailRead,
      handleSizeChange,
      handleCurrentChange,
      handlePublish,
      handleSubmitPublish,
      hasPermission,
      Plus
    };
  }
};
</script>

<style scoped>
.notification-container {
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
}

.header-content {
  flex: 1;
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
  margin-bottom: 0;
}

.el-table {
  margin-bottom: 0;
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

.header-with-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
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
  margin-top: 5px;
  padding: 0 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 通知详情样式 */
.notification-detail {
  padding: 10px;
}

.notification-title {
  margin-bottom: 16px;
  color: #303133;
  text-align: center;
}

.notification-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  color: #909399;
  font-size: 14px;
}

.notification-content {
  background-color: #f8f9fb;
  padding: 15px;
  border-radius: 4px;
  min-height: 100px;
  margin-bottom: 20px;
  white-space: pre-line;
  line-height: 1.6;
}

.notification-actions {
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
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

/* 当表格数据少于5条时，增加最小高度确保页面布局美观 */
.el-table:deep(.el-table__body-wrapper) {
  min-height: 100px;
}
</style>