<template>
  <div class="special-time-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>特殊时间段管理</h2>
          <el-button type="primary" @click="handleAdd">添加特殊时间段</el-button>
        </div>
      </template>
      
      <!-- 特殊时间段列表 -->
      <el-table
        :data="timePeriodsData"
        style="width: 100%;"
        v-loading="loading"
        border
        :max-height="'calc(100vh - 300px)'"
      >
        <el-table-column prop="weekStartDate" label="周开始日期" width="150" align="center">
          <template #header>
            <div class="header-with-icon-fixed">
              <el-icon><Calendar /></el-icon>
              <span>周开始日期</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="teacherAvailableCount" label="教师可用数量" width="150" align="center">
          <template #header>
            <div class="header-with-icon-fixed">
              <el-icon><UserFilled /></el-icon>
              <span>教师可用数量</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="teacherMinReserveCount" label="教师最小预留数量" width="180" align="center">
          <template #header>
            <div class="header-with-icon-fixed">
              <el-icon><UserFilled /></el-icon>
              <span>教师最小预留数量</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentAvailableCount" label="学生可用数量" width="150" align="center">
          <template #header>
            <div class="header-with-icon-fixed">
              <el-icon><UserFilled /></el-icon>
              <span>学生可用数量</span>
            </div>
          </template>
          <template #default="scope">
            {{ scope.row.studentAvailableCount === -1 ? '不限' : scope.row.studentAvailableCount }}
          </template>
        </el-table-column>
        <el-table-column prop="totalRoomsCount" label="总会议室数量" width="150" align="center">
          <template #header>
            <div class="header-with-icon-fixed">
              <el-icon><House /></el-icon>
              <span>总会议室数量</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="adjustmentDate" label="调整日期" width="200" align="center">
          <template #header>
            <div class="header-with-icon-fixed">
              <el-icon><Timer /></el-icon>
              <span>调整日期</span>
            </div>
          </template>
          <template #default="scope">
            {{ formatDateTime(scope.row.adjustmentDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #header>
            <div class="header-with-icon-fixed">
              <el-icon><Setting /></el-icon>
              <span>操作</span>
            </div>
          </template>
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="handleDelete(scope.row.id)"
              >删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑特殊时间段对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑特殊时间段' : '添加特殊时间段'"
      width="50%"
    >
      <el-form :model="form" label-width="180px" :rules="rules" ref="formRef">
        <el-form-item label="周开始日期" prop="weekStartDate">
          <el-date-picker
            v-model="form.weekStartDate"
            type="date"
            placeholder="选择周开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="教师已预约数量" prop="teacherReservedCount">
          <el-input-number 
            v-model="form.teacherReservedCount" 
            :min="0" 
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="教师可用数量" prop="teacherAvailableCount">
          <el-input-number 
            v-model="form.teacherAvailableCount" 
            :min="0" 
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="教师最小预留数量" prop="teacherMinReserveCount">
          <el-input-number 
            v-model="form.teacherMinReserveCount" 
            :min="0" 
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="学生可用数量" prop="studentAvailableCount">
          <el-input-number 
            v-model="form.studentAvailableCount" 
            :min="-1" 
            controls-position="right"
            style="width: 100%"
          />
          <div class="form-tip">-1表示不限制</div>
        </el-form-item>
        <el-form-item label="总会议室数量" prop="totalRoomsCount">
          <el-input-number 
            v-model="form.totalRoomsCount" 
            :min="0" 
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getSpecialTimePeriods, createSpecialTimePeriod, updateSpecialTimePeriod, deleteSpecialTimePeriod } from '@/api/specialTime';
import { Calendar, Setting, Timer, UserFilled, House } from '@element-plus/icons-vue';

// 数据加载状态
const loading = ref(false);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 特殊时间段数据
const timePeriodsData = ref([]);

// 对话框显示状态
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);

// 表单数据
const form = reactive({
  id: null,
  weekStartDate: '',
  teacherReservedCount: 0,
  teacherAvailableCount: 5,
  teacherMinReserveCount: 5,
  studentAvailableCount: -1,
  totalRoomsCount: 0
});

// 表单验证规则
const rules = {
  weekStartDate: [
    { required: true, message: '请选择周开始日期', trigger: 'change' }
  ],
  teacherAvailableCount: [
    { required: true, message: '请输入教师可用数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '教师可用数量必须大于等于0', trigger: 'blur' }
  ],
  teacherMinReserveCount: [
    { required: true, message: '请输入教师最小预留数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '教师最小预留数量必须大于等于0', trigger: 'blur' }
  ],
  totalRoomsCount: [
    { required: true, message: '请输入总会议室数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '总会议室数量必须大于等于0', trigger: 'blur' }
  ]
};



// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
};

// 获取特殊时间段列表
const fetchTimePeriods = async () => {
  loading.value = true;
  try {
    // 调用后端API获取数据
    const res = await getSpecialTimePeriods({
      page: currentPage.value,
      pageSize: pageSize.value
    });
    
    // 检查返回的状态码
    if (res.code === 1) {
      // 确保数据格式正确
      if (res.data && res.data.rows) {
        timePeriodsData.value = res.data.rows;
        total.value = res.data.total || 0;
      } else if (Array.isArray(res.data)) {
        // 如果返回的是数组而不是分页对象
        timePeriodsData.value = res.data;
        total.value = res.data.length;
      } else {
        timePeriodsData.value = [];
        total.value = 0;
      }
    } else {
      timePeriodsData.value = [];
      total.value = 0;
      ElMessage.error(res.msg || '获取特殊时间段列表失败');
    }
    loading.value = false;
  } catch (error) {
    console.error('获取特殊时间段列表失败:', error);
    ElMessage.error('获取特殊时间段列表失败');
    loading.value = false;
  }
};

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchTimePeriods();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchTimePeriods();
};

// 添加特殊时间段
const handleAdd = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

// 编辑特殊时间段
const handleEdit = (row) => {
  isEdit.value = true;
  resetForm();
  form.id = row.id;
  form.weekStartDate = row.weekStartDate;
  form.teacherReservedCount = row.teacherReservedCount || 0;
  form.teacherAvailableCount = row.teacherAvailableCount || 5;
  form.teacherMinReserveCount = row.teacherMinReserveCount || 5;
  form.studentAvailableCount = row.studentAvailableCount || -1;
  form.totalRoomsCount = row.totalRoomsCount || 0;
  dialogVisible.value = true;
};

// 删除特殊时间段
const handleDelete = (id) => {
  ElMessageBox.confirm(
    '确定要删除这个特殊时间段吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        // 调用后端API删除数据
        await deleteSpecialTimePeriod(id);
        
        ElMessage.success('删除成功');
        fetchTimePeriods();
      } catch (error) {
        console.error('删除特殊时间段失败:', error);
        ElMessage.error('删除特殊时间段失败');
      }
    })
    .catch(() => {
      // 用户取消删除
    });
};

// 重置表单
const resetForm = () => {
  form.id = null;
  form.weekStartDate = '';
  form.teacherReservedCount = 0;
  form.teacherAvailableCount = 5;
  form.teacherMinReserveCount = 5;
  form.studentAvailableCount = -1;
  form.totalRoomsCount = 0;
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 准备提交的数据
        const submitData = {
          ...form
        };
        
        // 调用后端API保存数据
        let result;
        if (isEdit.value) {
          // 更新现有记录
          result = await updateSpecialTimePeriod(submitData);
        } else {
          // 创建新记录
          result = await createSpecialTimePeriod(submitData);
        }
        
        if (result.code === 1) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
          dialogVisible.value = false;
          fetchTimePeriods();
        } else {
          ElMessage.error(result.msg || '保存失败');
        }
      } catch (error) {
        console.error('保存特殊时间段失败:', error);
        ElMessage.error('保存特殊时间段失败');
      }
    }
  });
};

// 页面加载时获取数据
onMounted(() => {
  fetchTimePeriods();
});
</script>

<script>
export default {
  name: 'SpecialTimeIndex'
};
</script>

<style scoped>
.special-time-container {
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

.el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.el-table {
  margin-bottom: 20px;
  width: 100% !important;
}

.el-table__body-wrapper {
  min-height: 200px;
}

:deep(.el-table__header),
:deep(.el-table__body) {
  width: 100% !important;
}

:deep(.el-table__header-wrapper),
:deep(.el-table__body-wrapper) {
  width: 100% !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.header-with-icon-fixed {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 40px;
  line-height: 40px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
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
</style>