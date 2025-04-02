<template>
  <div class="log-management">
    <el-card class="box-card">
      <template #header>
        <div class="clearfix">
          <span>操作日志管理</span>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable></el-input>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="请选择操作模块" clearable>
            <el-option 
              v-for="module in moduleOptions" 
              :key="module" 
              :label="module" 
              :value="module">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择操作类型" clearable>
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
            value-format="yyyy-MM-dd"
            :picker-options="pickerOptions">
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志列表 -->
      <el-table :data="logList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
        <el-table-column prop="userName" label="用户名" width="120"></el-table-column>
        <el-table-column prop="module" label="操作模块" width="120"></el-table-column>
        <el-table-column prop="operation" label="操作类型" width="100"></el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200"></el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140"></el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180"></el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getOperationLogs, getLogModules, getLogOperations } from '@/api/log'

export default {
  name: 'LogManagement',
  data() {
    return {
      searchForm: {
        userId: '',
        module: '',
        operationType: '',
        timeRange: []
      },
      logList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      moduleOptions: [],
      operationOptions: [],
      pickerOptions: {
        shortcuts: [{
          text: '最近一周',
          onClick(picker) {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            picker.$emit('pick', [start, end])
          }
        }, {
          text: '最近一个月',
          onClick(picker) {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            picker.$emit('pick', [start, end])
          }
        }]
      }
    }
  },
  created() {
    this.fetchOptions()
    this.fetchLogs()
  },
  methods: {
    async fetchOptions() {
      try {
        const modulesRes = await getLogModules()
        if (modulesRes && modulesRes.code === 1 && modulesRes.data) {
          this.moduleOptions = modulesRes.data
        } else {
          this.moduleOptions = ['用户管理', '会议室管理', '预约管理', '审批管理', '系统管理', '通知管理']
          console.warn('获取操作模块失败，使用默认值')
        }
        
        const operationsRes = await getLogOperations()
        if (operationsRes && operationsRes.code === 1 && operationsRes.data) {
          this.operationOptions = operationsRes.data
        } else {
          this.operationOptions = ['新增预约', '修改预约', '删除预约', '审批预约', '登录', '登出']
          console.warn('获取操作类型失败，使用默认值')
        }
      } catch (error) {
        console.error('获取选项数据失败:', error)
        this.moduleOptions = ['用户管理', '会议室管理', '预约管理', '审批管理', '系统管理', '通知管理']
        this.operationOptions = ['新增预约', '修改预约', '删除预约', '审批预约', '登录', '登出']
      }
    },
    async fetchLogs() {
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          userId: this.searchForm.userId,
          module: this.searchForm.module,
          operationType: this.searchForm.operationType,
          startTime: this.searchForm.timeRange ? this.searchForm.timeRange[0] : null,
          endTime: this.searchForm.timeRange ? this.searchForm.timeRange[1] : null
        }
        const res = await getOperationLogs(params)
        
        console.log('API返回数据:', res)
        
        this.logList = []
        this.total = 0
        
        if (res && res.code === 1 && res.data && Array.isArray(res.data.rows)) {
          this.logList = res.data.rows
          this.total = res.data.total
        } else {
          console.error('返回数据格式不符合预期:', res)
        }
      } catch (error) {
        this.$message.error('获取日志列表失败')
        console.error('获取日志列表失败:', error)
        this.logList = []
        this.total = 0
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchLogs()
    },
    resetSearch() {
      this.searchForm = {
        userId: '',
        module: '',
        operationType: '',
        timeRange: []
      }
      this.currentPage = 1
      this.fetchLogs()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchLogs()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchLogs()
    }
  }
}
</script>

<style scoped>
.log-management {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 