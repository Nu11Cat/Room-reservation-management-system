<template>
  <div class="notification-container">
    <div class="page-header">
      <h1>通知中心</h1>
    </div>
    
    <el-card class="tabs-card">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <!-- 未读通知标签页 -->
        <el-tab-pane label="未读通知" name="unread">
          <div class="toolbar">
            <el-input v-model="searchInput" placeholder="搜索通知" style="width: 300px" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
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
          >
            <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip />
            <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag :type="getTypeTag(scope.row.type)">
                  {{ getTypeText(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170">
              <template #default="scope">
                {{ formatDateTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="handleView(scope.row)"
                >
                  查看
                </el-button>
                <el-button 
                  size="small" 
                  type="info" 
                  @click="handleMarkRead(scope.row)"
                >
                  标为已读
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              v-if="unreadList.length > 0"
            />
          </div>
        </el-tab-pane>
        
        <!-- 全部通知标签页 -->
        <el-tab-pane label="全部通知" name="all">
          <div class="toolbar">
            <el-input v-model="searchInput" placeholder="搜索通知" style="width: 300px" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-select 
              v-model="typeFilter" 
              placeholder="通知类型" 
              clearable 
              style="width: 120px"
              @change="handleFilterChange"
            >
              <el-option label="全部类型" value="" />
              <el-option label="系统通知" :value="0" />
              <el-option label="预订通知" :value="1" />
              <el-option label="审批通知" :value="2" />
            </el-select>
            <el-select 
              v-model="readFilter" 
              placeholder="已读状态" 
              clearable 
              style="width: 120px"
              @change="handleFilterChange"
            >
              <el-option label="全部状态" value="" />
              <el-option label="已读" :value="1" />
              <el-option label="未读" :value="0" />
            </el-select>
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
          >
            <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip />
            <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag :type="getTypeTag(scope.row.type)">
                  {{ getTypeText(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="is_read" label="状态" width="80">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.is_read == 1 ? 'success' : 'danger'">
                  {{ scope.row.is_read == 1 ? '已读' : '未读' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170">
              <template #default="scope">
                {{ formatDateTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="handleView(scope.row)"
                >
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              v-if="allList.length > 0"
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
          <el-button type="primary" @click="handleMarkDetailRead">标为已读</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getNotificationList, markNotificationRead } from '@/api/notification';

export default {
  name: 'NotificationList',
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
    
    // 监听标签页变化
    watch(activeTab, () => {
      currentPage.value = 1;
      loadNotifications();
    });
    
    // 获取类型对应的标签类型
    const getTypeTag = (type) => {
      switch (Number(type)) {
        case 0: return '';        // 系统通知
        case 1: return 'info';    // 预订通知
        case 2: return 'success'; // 审批通知
        default: return '';
      }
    };
    
    // 获取类型对应的文本
    const getTypeText = (type) => {
      switch (Number(type)) {
        case 0: return '系统通知';
        case 1: return '预订通知';
        case 2: return '审批通知';
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
        
        // 根据不同标签页设置不同参数
        if (activeTab.value === 'unread') {
          params.is_read = 0;
        } else {
          // 全部通知标签页，默认不设置is_read过滤
          if (typeFilter.value !== '') {
            params.type = typeFilter.value;
          }
          // 只在明确选择已读状态时才设置
          if (readFilter.value !== '') {
            params.is_read = readFilter.value;
          }
        }
        
        // 打印请求参数用于调试
        console.log('请求参数:', params);
        
        // 关键词搜索
        if (searchInput.value) {
          params.keyword = searchInput.value;
        }
        
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
      handleCurrentChange
    };
  }
};
</script>

<style scoped>
.notification-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tabs-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.notification-detail {
  padding: 10px;
}

.notification-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  color: #909399;
}

.notification-content {
  line-height: 1.6;
  margin-bottom: 20px;
  white-space: pre-line;
}

.notification-actions {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}
</style> 