<template>
  <div class="feedback-management-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>评价管理</h2>
          <div class="header-operations">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索会议室/评价内容"
              style="width: 200px"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" :icon="Refresh" @click="refreshList">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 评价列表 -->
      <el-table :data="feedbackList" v-loading="loading" style="width: 100%">
        <el-table-column prop="roomName" label="会议室" width="150" />
        <el-table-column prop="userName" label="评价用户" width="120" />
        <el-table-column prop="bookingTime" label="使用时间" width="180" />
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="评价时间" width="180" />
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="评价详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="会议室">{{ selectedFeedback?.roomName }}</el-descriptions-item>
        <el-descriptions-item label="评价用户">{{ selectedFeedback?.userName }}</el-descriptions-item>
        <el-descriptions-item label="使用时间">{{ selectedFeedback?.bookingTime }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="selectedFeedback.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="评价内容">{{ selectedFeedback?.comment }}</el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ selectedFeedback?.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh } from '@element-plus/icons-vue';

// 数据列表相关
const loading = ref(false);
const feedbackList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 搜索表单
const searchForm = reactive({
  keyword: ''
});

// 详情对话框相关
const detailVisible = ref(false);
const selectedFeedback = ref({});

// 获取评价列表
const getFeedbackList = async () => {
  loading.value = true;
  try {
    // TODO: 调用获取评价列表接口
    // const { data } = await getFeedbackList({
    //   page: currentPage.value,
    //   pageSize: pageSize.value,
    //   keyword: searchForm.keyword
    // });
    // feedbackList.value = data.list;
    // total.value = data.total;
  } catch (error) {
    console.error('获取评价列表失败:', error);
    ElMessage.error('获取评价列表失败');
  } finally {
    loading.value = false;
  }
};

// 查看详情
const viewDetail = (row) => {
  selectedFeedback.value = { ...row };
  detailVisible.value = true;
};

// 删除评价
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该评价吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // TODO: 调用删除评价接口
      // await deleteFeedback(row.id);
      ElMessage.success('删除成功');
      getFeedbackList();
    } catch (error) {
      console.error('删除评价失败:', error);
      ElMessage.error('删除评价失败');
    }
  }).catch(() => {});
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  getFeedbackList();
};

// 刷新列表
const refreshList = () => {
  searchForm.keyword = '';
  currentPage.value = 1;
  getFeedbackList();
};

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val;
  getFeedbackList();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  getFeedbackList();
};

// 初始化
onMounted(() => {
  getFeedbackList();
});
</script>

<script>
export default {
  name: 'FeedbackManagement'
};
</script>

<style scoped>
.feedback-management-container {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.box-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>