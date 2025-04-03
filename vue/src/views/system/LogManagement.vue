<template>
  <div class="log-container">
    <div class="page-header">
      <h1>操作日志管理</h1>
      <div class="header-description">查看和管理系统操作日志记录</div>
    </div>
    
    <el-card class="filter-card">
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户ID">
            <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable>
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="操作模块">
            <el-select v-model="searchForm.module" placeholder="请选择操作模块" clearable style="width: 220px;">
              <el-option 
                v-for="module in moduleOptions" 
                :key="module" 
                :label="module" 
                :value="module">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="操作类型">
            <el-select v-model="searchForm.operationType" placeholder="请选择操作类型" clearable style="width: 220px;">
              <el-option 
                v-for="operation in operationOptions" 
                :key="operation" 
                :label="operation" 
                :value="operation">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="操作时间">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="data-card" :body-style="{ padding: '0px' }">
      <div class="card-header">
        <span class="card-title">日志列表</span>
        <span class="card-subtitle">共 {{ total }} 条记录</span>
      </div>

      <!-- 日志列表 -->
      <el-table 
        :data="logList" 
        border 
        style="width: 100%" 
        v-loading="loading"
        :header-cell-style="{ 
          background: '#f5f7fa', 
          color: '#606266', 
          fontWeight: 'bold' 
        }">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><User /></el-icon>
              <span>用户ID</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" width="120" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><UserFilled /></el-icon>
              <span>用户名</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="120" align="center" />
        <el-table-column prop="module" label="操作模块" width="120" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Files /></el-icon>
              <span>操作模块</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作类型" width="240" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Operation /></el-icon>
              <span>操作类型</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="160" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Document /></el-icon>
              <span>操作描述</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Monitor /></el-icon>
              <span>IP地址</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" align="center">
          <template #header>
            <div class="header-with-icon">
              <el-icon><Calendar /></el-icon>
              <span>操作时间</span>
            </div>
          </template>
          <template #default="scope">
            <span>{{ formatDateTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
        background
      />
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { getOperationLogs, getLogModules, getLogOperations } from '@/api/log'
import { 
  User, 
  UserFilled, 
  Search, 
  Refresh, 
  Calendar, 
  Document, 
  Operation, 
  Files,
  Monitor
} from '@element-plus/icons-vue'

export default {
  name: 'LogManagement',
  components: {
    User,
    UserFilled,
    Search,
    Refresh,
    Calendar,
    Document,
    Operation,
    Files,
    Monitor
  },
  setup() {
    const loading = ref(false)
    const searchForm = reactive({
      userId: '',
      module: '',
      operationType: '',
      timeRange: []
    })
    const logList = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const moduleOptions = ref([])
    const operationOptions = ref([])

    // 格式化日期时间
    const formatDateTime = (datetime) => {
      if (!datetime) return ''
      const date = new Date(datetime)
      const y = date.getFullYear()
      const m = (date.getMonth() + 1).toString().padStart(2, '0')
      const d = date.getDate().toString().padStart(2, '0')
      const h = date.getHours().toString().padStart(2, '0')
      const min = date.getMinutes().toString().padStart(2, '0')
      return `${y}-${m}-${d} ${h}:${min}`
    }

    const fetchOptions = async () => {
      try {
        const modulesRes = await getLogModules()
        if (modulesRes && modulesRes.code === 1 && modulesRes.data) {
          moduleOptions.value = modulesRes.data
        } else {
          moduleOptions.value = ['用户管理', '会议室管理', '预约管理', '审批管理', '系统管理', '通知管理']
          console.warn('获取操作模块失败，使用默认值')
        }
        
        const operationsRes = await getLogOperations()
        if (operationsRes && operationsRes.code === 1 && operationsRes.data) {
          operationOptions.value = operationsRes.data
        } else {
          operationOptions.value = ['新增预约', '修改预约', '删除预约', '审批预约', '登录', '登出']
          console.warn('获取操作类型失败，使用默认值')
        }
      } catch (error) {
        console.error('获取选项数据失败:', error)
        moduleOptions.value = ['用户管理', '会议室管理', '预约管理', '审批管理', '系统管理', '通知管理']
        operationOptions.value = ['新增预约', '修改预约', '删除预约', '审批预约', '登录', '登出']
      }
    }

    const fetchLogs = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value,
          pageSize: pageSize.value,
          userId: searchForm.userId,
          module: searchForm.module,
          operation: searchForm.operationType,
          startTime: searchForm.timeRange ? searchForm.timeRange[0] : null,
          endTime: searchForm.timeRange ? searchForm.timeRange[1] : null
        }
        const res = await getOperationLogs(params)
        
        console.log('API返回数据:', res)
        
        logList.value = []
        total.value = 0
        
        if (res && res.code === 1 && res.data && Array.isArray(res.data.rows)) {
          logList.value = res.data.rows
          total.value = res.data.total
        } else {
          console.error('返回数据格式不符合预期:', res)
        }
      } catch (error) {
        console.error('获取日志列表失败:', error)
        logList.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      currentPage.value = 1
      fetchLogs()
    }

    const resetSearch = () => {
      searchForm.userId = ''
      searchForm.module = ''
      searchForm.operationType = ''
      searchForm.timeRange = []
      currentPage.value = 1
      fetchLogs()
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchLogs()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchLogs()
    }

    onMounted(() => {
      fetchOptions()
      fetchLogs()
    })

    return {
      loading,
      searchForm,
      logList,
      currentPage,
      pageSize,
      total,
      moduleOptions,
      operationOptions,
      formatDateTime,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.log-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
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
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.data-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border-radius: 8px;
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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin: 20px;
  display: flex;
  justify-content: flex-end;
}

.search-form {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.header-with-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
</style> 