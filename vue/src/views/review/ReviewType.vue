<template>
  <div class="review-type-admin">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>不文明行为类型管理</h2>
          <el-button type="primary" @click="openDialog(null)">添加类型</el-button>
        </div>
      </template>

      <!-- 类型列表 -->
      <el-table :data="typeList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="typeName" label="类型名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="defaultScoreImpact" label="分数影响" width="100">
          <template #default="{ row }">
            <span :class="{ 'negative-score': row.defaultScoreImpact < 0 }">
              {{ row.defaultScoreImpact }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="severityLevel" label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severityLevel)">
              {{ getSeverityLabel(row.severityLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="openDialog(row)"
            >编辑</el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
            >删除</el-button>
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
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑类型' : '添加类型'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="类型名称" prop="typeName">
          <el-input v-model="form.typeName" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入类型描述"
          />
        </el-form-item>
        <el-form-item label="分数影响" prop="defaultScoreImpact">
          <el-input-number
            v-model="form.defaultScoreImpact"
            :min="-100"
            :max="0"
            :step="5"
            placeholder="请输入分数影响"
          />
        </el-form-item>
        <el-form-item label="严重程度" prop="severityLevel">
          <el-select v-model="form.severityLevel" placeholder="请选择严重程度">
            <el-option :value="1" label="轻微" />
            <el-option :value="2" label="一般" />
            <el-option :value="3" label="严重" />
            <el-option :value="4" label="非常严重" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getReviewTypeList,
  createReviewType,
  updateReviewType,
  deleteReviewType
} from '@/api/review.js'

// 表格数据
const loading = ref(false)
const typeList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框数据
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  id: null,
  typeName: '',
  description: '',
  defaultScoreImpact: -5,
  severityLevel: 1
})

// 表单验证规则
const rules = {
  typeName: [
    { required: true, message: '请输入类型名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入类型描述', trigger: 'blur' },
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ],
  defaultScoreImpact: [
    { required: true, message: '请输入分数影响', trigger: 'blur' },
    { type: 'number', message: '分数影响必须为数字', trigger: 'blur' }
  ],
  severityLevel: [
    { required: true, message: '请选择严重程度', trigger: 'change' },
    { type: 'number', min: 1, max: 4, message: '严重程度范围为1-4', trigger: 'change' }
  ]
}

// 获取严重程度标签
const getSeverityLabel = (level) => {
  const labels = {
    1: '轻微',
    2: '一般',
    3: '严重',
    4: '非常严重'
  }
  return labels[level] || '未知'
}

// 获取严重程度标签类型
const getSeverityType = (level) => {
  const types = {
    1: 'info',
    2: 'warning',
    3: 'danger',
    4: 'danger'
  }
  return types[level] || 'info'
}

// 加载类型列表
const loadTypeList = async () => {
  loading.value = true
  try {
    const res = await getReviewTypeList({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    typeList.value = res.data.rows || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载类型列表失败:', error)
    ElMessage.error('加载类型列表失败')
  } finally {
    loading.value = false
  }
}

// 打开对话框
const openDialog = (row) => {
  if (row) {
    form.value = { ...row }
  } else {
    form.value = {
      id: null,
      typeName: '',
      description: '',
      defaultScoreImpact: -5,
      severityLevel: 1
    }
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value.id) {
          await updateReviewType(form.value.id, form.value)
          ElMessage.success('更新成功')
        } else {
          await createReviewType(form.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadTypeList()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      }
    }
  })
}

// 删除类型
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该类型吗？')
    await deleteReviewType(row.id)
    ElMessage.success('删除成功')
    loadTypeList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}


// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadTypeList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadTypeList()
}

// 初始化
onMounted(() => {
  loadTypeList()
})
</script>

<style scoped>
.review-type-admin {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.negative-score {
  color: #f56c6c;
}
</style>