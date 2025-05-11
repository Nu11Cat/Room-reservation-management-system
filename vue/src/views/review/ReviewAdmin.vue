<template>
  <div class="review-admin">
    <!-- 统计信息卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待处理评价</span>
            </div>
          </template>
          <div class="statistics-value">{{ statistics.pendingCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>已处理评价</span>
            </div>
          </template>
          <div class="statistics-value">{{ statistics.processedCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>总评价数</span>
            </div>
          </template>
          <div class="statistics-value">{{ statistics.totalCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评价列表tabs -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="待处理评价" name="pending">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form :inline="true" :model="searchForm" class="demo-form-inline">
            <el-form-item label="评价类型">
              <el-select v-model="searchForm.reviewType" placeholder="请选择">
                <el-option label="全部" :value="null" />
                <el-option label="好评" :value="1" />
                <el-option label="差评" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchReviews">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 待处理评价表格 -->
        <el-table
          v-loading="loading"
          :data="pendingReviews"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="reviewType" label="评价类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.reviewType === 1 ? 'success' : 'danger'">
                {{ row.reviewType === 1 ? '好评' : '差评' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
          <el-table-column prop="misconductTypes" label="不文明行为类型" width="200">
            <template #default="{ row }">
              <div v-if="row.misconductTypes && row.misconductTypes.length > 0">
                <el-tag 
                  v-for="typeId in row.misconductTypes" 
                  :key="typeId"
                  size="small"
                  type="danger"
                  style="margin-right: 5px; margin-bottom: 5px;"
                >
                  {{ getMisconductTypeName(typeId) }}
                </el-tag>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleProcess(row)"
              >处理</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 批量操作栏 -->
        <div class="batch-operation" v-if="selectedReviews.length > 0">
          <el-button type="primary" @click="handleBatchProcess">批量处理</el-button>
        </div>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="已处理评价" name="processed">
        <!-- 已处理评价表格 -->
        <el-table
          v-loading="loading"
          :data="processedReviews"
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="reviewType" label="评价类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.reviewType === 1 ? 'success' : 'danger'">
                {{ row.reviewType === 1 ? '好评' : '差评' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
          <el-table-column prop="misconductTypes" label="不文明行为类型" width="200">
            <template #default="{ row }">
              <div v-if="row.misconductTypes && row.misconductTypes.length > 0">
                <el-tag 
                  v-for="typeId in row.misconductTypes" 
                  :key="typeId"
                  size="small"
                  type="danger"
                  style="margin-right: 5px; margin-bottom: 5px;"
                >
                  {{ getMisconductTypeName(typeId) }}
                </el-tag>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="processTime" label="处理时间" width="180" />
          <el-table-column prop="processResult" label="处理结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.processResult === 1 ? 'success' : 'info'">
                {{ row.processResult === 1 ? '通过' : '不通过' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                type="warning"
                size="small"
                @click="handleUndo(row)"
              >撤销处理</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 处理评价对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理评价"
      width="500px"
    >
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="processForm.processResult">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="0">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="processForm.processNote"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcess">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getReviewList,
  getProcessedReviewList,
  processReview,
  batchProcessReview,
  undoProcessReview,
  getReviewStatistics,
  getAllReviewTypes
} from '@/api/review'

// 统计数据
const statistics = ref({
  pendingCount: 0,
  processedCount: 0,
  totalCount: 0
})

// 不文明行为类型列表
const misconductTypes = ref([])

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getReviewStatistics()
    statistics.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载不文明行为类型
const loadMisconductTypes = async () => {
  try {
    const res = await getAllReviewTypes()
    misconductTypes.value = res.data || []
  } catch (error) {
    console.error('加载不文明行为类型失败:', error)
  }
}

// 获取不文明行为类型名称
const getMisconductTypeName = (typeId) => {
  // 确保typeId是数字类型
  const numericTypeId = typeof typeId === 'string' ? parseInt(typeId, 10) : typeId
  const type = misconductTypes.value.find(item => item.id === numericTypeId)
  return type ? type.typeName : '未知类型'
}

// 表格数据
const loading = ref(false)
const activeTab = ref('pending')
const pendingReviews = ref([])
const processedReviews = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = ref({
  reviewType: null,
  dateRange: []
})

// 处理选中的评价
const selectedReviews = ref([])
const handleSelectionChange = (val) => {
  selectedReviews.value = val
}

// 处理表单
const processDialogVisible = ref(false)
const processForm = ref({
  processResult: 1,
  processNote: '',
  reviewIds: []
})

// 加载评价列表
const loadReviews = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      reviewType: searchForm.value.reviewType,
      startDate: searchForm.value.dateRange[0],
      endDate: searchForm.value.dateRange[1]
    }

    const res = activeTab.value === 'pending'
      ? await getReviewList(params)
      : await getProcessedReviewList(params)

    if (activeTab.value === 'pending') {
      pendingReviews.value = res.data.rows
    } else {
      processedReviews.value = res.data.rows
    }
    total.value = res.data.total
  } catch (error) {
    console.error('加载评价列表失败:', error)
    ElMessage.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索和重置
const searchReviews = () => {
  currentPage.value = 1
  loadReviews()
}

const resetSearch = () => {
  searchForm.value = {
    reviewType: null,
    dateRange: []
  }
  searchReviews()
}

// 处理评价
const handleProcess = (row) => {
  processForm.value = {
    processResult: 1,
    processNote: '',
    reviewIds: [row.id]
  }
  processDialogVisible.value = true
}

// 批量处理评价
const handleBatchProcess = () => {
  if (selectedReviews.value.length === 0) {
    ElMessage.warning('请选择要处理的评价')
    return
  }
  processForm.value = {
    processResult: 1,
    processNote: '',
    reviewIds: selectedReviews.value.map(item => item.id)
  }
  processDialogVisible.value = true
}

// 提交处理
const submitProcess = async () => {
  try {
    if (processForm.value.reviewIds.length === 1) {
      await processReview(processForm.value.reviewIds[0], {
        processResult: processForm.value.processResult,
        processNote: processForm.value.processNote
      })
    } else {
      await batchProcessReview({
        reviewIds: processForm.value.reviewIds,
        processResult: processForm.value.processResult,
        processNote: processForm.value.processNote
      })
    }
    ElMessage.success('处理成功')
    processDialogVisible.value = false
    loadReviews()
    loadStatistics()
  } catch (error) {
    console.error('处理评价失败:', error)
    ElMessage.error('处理评价失败')
  }
}

// 撤销处理
const handleUndo = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤销处理吗？')
    await undoProcessReview(row.id)
    ElMessage.success('撤销成功')
    loadReviews()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销处理失败:', error)
      ElMessage.error('撤销处理失败')
    }
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadReviews()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadReviews()
}

// 标签页切换
const handleTabClick = () => {
  currentPage.value = 1
  loadReviews()
}

// 初始化
onMounted(() => {
  loadStatistics()
  loadMisconductTypes()
  loadReviews()
})
</script>

<style scoped>
.review-admin {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}

.search-bar {
  margin-bottom: 20px;
}

.batch-operation {
  margin: 10px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.mb-4 {
  margin-bottom: 24px;
}

.el-tag {
  margin-right: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>