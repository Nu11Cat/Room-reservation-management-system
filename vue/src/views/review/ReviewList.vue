<template>
  <div class="review-list-container">
    <!-- 搜索和筛选区域 -->
    <el-card class="filter-section">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="评价类型">
          <el-select v-model="queryParams.reviewType" placeholder="请选择评价类型" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option v-for="type in reviewTypes" :key="type.value" :label="type.label" :value="type.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评价列表 -->
    <el-card class="review-list">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="我发出的评价" name="outgoing">
          <el-table :data="reviewList" style="width: 100%">
            <el-table-column prop="reviewedUserName" label="被评价人" width="120"></el-table-column>
            <el-table-column prop="reviewScore" label="评分" width="100">
              <template #default="scope">
                <el-rate v-model="scope.row.reviewScore" disabled show-score></el-rate>
              </template>
            </el-table-column>
            <el-table-column prop="reviewContent" label="评价内容"></el-table-column>
            <el-table-column prop="reviewType" label="评价类型" width="120">
              <template #default="scope">
                <el-tag :type="getReviewTypeTag(scope.row.reviewType)">{{ getReviewTypeName(scope.row.reviewType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="评价时间" width="180"></el-table-column>
            <el-table-column prop="isProcessed" label="处理状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.isProcessed === 1 ? 'success' : 'warning'">
                  {{ scope.row.isProcessed === 1 ? '已处理' : '未处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button
                  v-if="scope.row.isProcessed === 0"
                  link
                  type="primary"
                  @click="handleEdit(scope.row)"
                >修改</el-button>
                <el-button
                  v-if="scope.row.isProcessed === 0"
                  link
                  type="danger"
                  @click="handleDelete(scope.row)"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="我收到的评价" name="incoming">
          <el-table :data="reviewList" style="width: 100%">
            <el-table-column prop="reviewerName" label="评价人" width="120"></el-table-column>
            <el-table-column prop="reviewScore" label="评分" width="100">
              <template #default="scope">
                <el-rate v-model="scope.row.reviewScore" disabled show-score></el-rate>
              </template>
            </el-table-column>
            <el-table-column prop="reviewContent" label="评价内容"></el-table-column>
            <el-table-column prop="reviewType" label="评价类型" width="120">
              <template #default="scope">
                <el-tag :type="getReviewTypeTag(scope.row.reviewType)">{{ getReviewTypeName(scope.row.reviewType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="评价时间" width="180"></el-table-column>
            <el-table-column prop="isProcessed" label="处理状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.isProcessed === 1 ? 'success' : 'warning'">
                  {{ scope.row.isProcessed === 1 ? '已处理' : '未处理' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 修改评价对话框 -->
    <el-dialog v-model="editDialogVisible" title="修改评价" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="评分">
          <el-rate v-model="editForm.reviewScore" show-score></el-rate>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="editForm.reviewContent"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ReviewList'
}
</script>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOutgoingReviews, getIncomingReviews, updateReview, deleteReview } from '@/api/review'

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  reviewType: '',
  startDate: '',
  endDate: ''
})

// 评价类型选项
const reviewTypes = [
  { value: 1, label: '使用体验' },
  { value: 2, label: '服务态度' },
  { value: 3, label: '设备状况' },
  { value: 4, label: '其他' }
]

// 日期范围
const dateRange = ref([])

// 评价列表数据
const reviewList = ref([])
const total = ref(0)
const activeTab = ref('outgoing')

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = reactive({
  id: null,
  reviewScore: 5,
  reviewContent: ''
})

// 获取评价列表
const getReviewList = async () => {
  try {
    const api = activeTab.value === 'outgoing' ? getOutgoingReviews : getIncomingReviews
    const { data } = await api(queryParams)
    reviewList.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('获取评价列表失败:', error)
    ElMessage.error('获取评价列表失败')
  }
}

// 处理查询
const handleQuery = () => {
  queryParams.page = 1
  getReviewList()
}

// 重置查询
const resetQuery = () => {
  queryParams.reviewType = ''
  dateRange.value = []
  queryParams.startDate = ''
  queryParams.endDate = ''
  handleQuery()
}

// 处理日期变化
const handleDateChange = (val) => {
  if (val) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
}

// 处理标签页切换
const handleTabChange = () => {
  queryParams.page = 1
  getReviewList()
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getReviewList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.page = val
  getReviewList()
}

// 获取评价类型名称
const getReviewTypeName = (type) => {
  const found = reviewTypes.find(item => item.value === type)
  return found ? found.label : '未知'
}

// 获取评价类型标签样式
const getReviewTypeTag = (type) => {
  const tagTypes = {
    1: '',
    2: 'success',
    3: 'warning',
    4: 'info'
  }
  return tagTypes[type] || ''
}

// 处理修改评价
const handleEdit = (row) => {
  editForm.id = row.id
  editForm.reviewScore = row.reviewScore
  editForm.reviewContent = row.reviewContent
  editDialogVisible.value = true
}

// 提交修改
const submitEdit = async () => {
  try {
    await updateReview(editForm.id, {
      reviewScore: editForm.reviewScore,
      reviewContent: editForm.reviewContent
    })
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    getReviewList()
  } catch (error) {
    console.error('修改评价失败:', error)
    ElMessage.error('修改评价失败')
  }
}

// 处理删除评价
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除这条评价吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReview(row.id)
      ElMessage.success('删除成功')
      getReviewList()
    } catch (error) {
      console.error('删除评价失败:', error)
      ElMessage.error('删除评价失败')
    }
  }).catch(() => {})
}

// 页面加载时获取数据
onMounted(() => {
  getReviewList()
})
</script>

<style scoped>
.review-list-container {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.review-list {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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