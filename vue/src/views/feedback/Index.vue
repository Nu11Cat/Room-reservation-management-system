<template>
  <div class="feedback-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>用户评价</h2>
          <el-button type="primary" @click="openFeedbackDialog">提交评价</el-button>
        </div>
      </template>
      
      <!-- 评价列表 -->
      <el-table :data="feedbackList" v-loading="loading" style="width: 100%">
        <el-table-column prop="roomName" label="会议室" width="180" />
        <el-table-column prop="bookingTime" label="使用时间" width="180" />
        <el-table-column prop="rating" label="评分">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" />
        <el-table-column prop="createTime" label="评价时间" width="180" />
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

    <!-- 评价对话框 -->
    <el-dialog v-model="dialogVisible" title="提交评价" width="500px">
      <el-form :model="feedbackForm" :rules="rules" ref="feedbackFormRef" label-width="100px">
        <el-form-item label="会议室预订" prop="bookingId">
          <el-select v-model="feedbackForm.bookingId" placeholder="请选择会议室预订记录">
            <el-option
              v-for="booking in bookingOptions"
              :key="booking.id"
              :label="`${booking.roomName} (${booking.bookingTime})`"
              :value="booking.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="feedbackForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容" prop="comment">
          <el-input
            v-model="feedbackForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入您的评价内容"
          />
        </el-form-item>
        <el-form-item label="不文明行为" prop="misconductTypes" v-if="feedbackForm.rating < 3">
          <el-select
            v-model="feedbackForm.misconductTypes"
            multiple
            placeholder="请选择不文明行为类型"
            style="width: 100%"
          >
            <el-option
              v-for="item in misconductTypeOptions"
              :key="item.id"
              :label="item.typeName"
              :value="item.id"
            >
              <span>{{ item.typeName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                影响: {{ item.defaultScoreImpact }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFeedback" :loading="submitLoading">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getAllReviewTypes } from '@/api/review';

// 数据列表相关
const loading = ref(false);
const feedbackList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 对话框相关
const dialogVisible = ref(false);
const feedbackFormRef = ref(null);
const submitLoading = ref(false);
const bookingOptions = ref([]);

// 不文明行为类型列表
const misconductTypeOptions = ref([]);

// 获取所有不文明行为类型
const loadMisconductTypes = async () => {
  try {
    const res = await getAllReviewTypes();
    misconductTypeOptions.value = res.data || [];
  } catch (error) {
    console.error('获取不文明行为类型失败:', error);
    ElMessage.error('获取不文明行为类型失败');
  }
};

// 表单数据
const feedbackForm = reactive({
  bookingId: '',
  rating: 0,
  comment: '',
  misconductTypes: [] // 不文明行为类型ID数组
});

// 表单验证规则
const rules = {
  bookingId: [{ required: true, message: '请选择会议室预订记录', trigger: 'change' }],
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  comment: [{ required: true, message: '请输入评价内容', trigger: 'blur' }],
  misconductTypes: [{ required: true, message: '请选择不文明行为类型', trigger: 'change' }]
};

// 获取评价列表
const getFeedbackList = async () => {
  loading.value = true;
  try {
    // TODO: 调用获取评价列表接口
    // const { data } = await getFeedbackList({
    //   page: currentPage.value,
    //   pageSize: pageSize.value
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

// 获取可评价的预订记录
const getBookingOptions = async () => {
  try {
    // TODO: 调用获取可评价预订记录接口
    // const { data } = await getAvailableBookings();
    // bookingOptions.value = data;
  } catch (error) {
    console.error('获取预订记录失败:', error);
    ElMessage.error('获取预订记录失败');
  }
};

// 打开评价对话框
const openFeedbackDialog = async () => {
  await getBookingOptions();
  await loadMisconductTypes();
  dialogVisible.value = true;
};

// 监听评分变化，当评分大于等于3时清空不文明行为类型
watch(() => feedbackForm.rating, (newRating) => {
  if (newRating >= 3) {
    feedbackForm.misconductTypes = [];
  }
});

// 提交评价
const submitFeedback = async () => {
  if (!feedbackFormRef.value) return;
  
  await feedbackFormRef.value.validate(async (valid) => {
    if (valid) {
      // 检查差评是否选择了不文明行为类型
      if (feedbackForm.rating < 3 && (!feedbackForm.misconductTypes || feedbackForm.misconductTypes.length === 0)) {
        ElMessage.warning('差评必须选择至少一种不文明行为类型');
        return;
      }
      
      submitLoading.value = true;
      try {
        // TODO: 调用提交评价接口
        // await submitFeedback({
        //   ...feedbackForm,
        //   misconductTypes: feedbackForm.rating < 3 ? feedbackForm.misconductTypes : []
        // });
        ElMessage.success('评价提交成功');
        dialogVisible.value = false;
        getFeedbackList(); // 刷新列表
      } catch (error) {
        console.error('提交评价失败:', error);
        ElMessage.error('提交评价失败');
      } finally {
        submitLoading.value = false;
      }
    }
  });
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
  name: 'UserFeedback'
};
</script>

<style scoped>
.feedback-container {
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