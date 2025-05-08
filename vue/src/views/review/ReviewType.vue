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
        <el-table-column prop="name" label="类型名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
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
              type="success"
              link
              v-if="row.status === 0"
              @click="handleStatusChange(row.id, 1)"
            >启用</el-button>
            <el-button
              type="warning"
              link
              v-if="row.status === 1"
              @click="handleStatusChange(row.id, 0)"
            >禁用</el-button>
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
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入类型描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
  deleteReviewType,
  updateReviewTypeStatus
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
  name: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入类型名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入类型描述', trigger: 'blur' },
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 加载类型列表
const loadTypeList = async () => {
  loading.value = true
  try {
    const res = await getReviewTypeList({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    typeList.value = res.rows || []
    total.value = res.total || 0
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
      name: '',
      description: '',
      status: 1
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

// 更改状态
const handleStatusChange = async (id, status) => {
  try {
    await updateReviewTypeStatus(id, { status })
    ElMessage.success('状态更新成功')
    loadTypeList()
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
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
</style>