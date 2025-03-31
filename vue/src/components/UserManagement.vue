<template>
  <div class="user-container">
    <h1>用户管理</h1>
    <div class="toolbar">
      <el-input v-model="searchInput" placeholder="搜索用户" style="width: 300px" clearable />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleAdd">添加用户</el-button>
    </div>
    <el-table :data="userList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="roleName" label="角色" width="120" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" :type="scope.row.status === 1 ? 'danger' : 'success'" @click="handleToggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 添加/编辑用户对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userForm.roleId" placeholder="请选择角色">
            <el-option label="管理员" :value="1" />
            <el-option label="普通用户" :value="2" />
            <el-option label="审批人" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUserForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
  name: 'UserManagementPage',
  setup() {
    const loading = ref(false);
    const searchInput = ref('');
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    const userList = ref([]);
    
    const dialogVisible = ref(false);
    const dialogType = ref('add'); // add 或 edit
    const dialogTitle = ref('添加用户');
    const submitLoading = ref(false);
    const userFormRef = ref(null);
    
    const userForm = reactive({
      id: null,
      username: '',
      password: '',
      email: '',
      roleId: 2
    });
    
    const userRules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
    };
    
    // 加载用户列表
    const loadUserList = async () => {
      loading.value = true;
      try {
        // 这里将来替换为API调用
        setTimeout(() => {
          total.value = 0;
          userList.value = [];
          loading.value = false;
        }, 500);
      } catch (error) {
        console.error('获取用户列表失败:', error);
        ElMessage.error('获取用户列表失败');
        loading.value = false;
      }
    };
    
    onMounted(() => {
      loadUserList();
    });
    
    // 搜索
    const handleSearch = () => {
      currentPage.value = 1;
      loadUserList();
    };
    
    // 添加用户
    const handleAdd = () => {
      dialogType.value = 'add';
      dialogTitle.value = '添加用户';
      resetUserForm();
      dialogVisible.value = true;
    };
    
    // 编辑用户
    const handleEdit = (row) => {
      dialogType.value = 'edit';
      dialogTitle.value = '编辑用户';
      Object.assign(userForm, {
        id: row.id,
        username: row.username,
        email: row.email,
        roleId: row.roleId
      });
      dialogVisible.value = true;
    };
    
    // 删除用户
    const handleDelete = (row) => {
      ElMessageBox.confirm(
        `确定要删除用户 ${row.username} 吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          // 这里将来替换为API调用
          await new Promise(resolve => setTimeout(resolve, 500));
          ElMessage.success(`删除用户 ${row.username} 成功`);
          loadUserList();
        } catch (error) {
          ElMessage.error('删除用户失败');
        }
      }).catch(() => {});
    };
    
    // 启用/禁用用户
    const handleToggleStatus = (row) => {
      const action = row.status === 1 ? '禁用' : '启用';
      ElMessageBox.confirm(
        `确定要${action}用户 ${row.username} 吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          // 这里将来替换为API调用
          await new Promise(resolve => setTimeout(resolve, 500));
          ElMessage.success(`${action}用户 ${row.username} 成功`);
          loadUserList();
        } catch (error) {
          ElMessage.error(`${action}用户失败`);
        }
      }).catch(() => {});
    };
    
    // 提交用户表单
    const submitUserForm = () => {
      userFormRef.value.validate(async (valid) => {
        if (valid) {
          submitLoading.value = true;
          try {
            // 这里将来替换为API调用
            await new Promise(resolve => setTimeout(resolve, 500));
            ElMessage.success(dialogType.value === 'add' ? '添加用户成功' : '编辑用户成功');
            dialogVisible.value = false;
            loadUserList();
          } catch (error) {
            ElMessage.error(dialogType.value === 'add' ? '添加用户失败' : '编辑用户失败');
          } finally {
            submitLoading.value = false;
          }
        }
      });
    };
    
    // 重置用户表单
    const resetUserForm = () => {
      userForm.id = null;
      userForm.username = '';
      userForm.password = '';
      userForm.email = '';
      userForm.roleId = 2;
      if (userFormRef.value) {
        userFormRef.value.resetFields();
      }
    };
    
    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size;
      loadUserList();
    };
    
    const handleCurrentChange = (page) => {
      currentPage.value = page;
      loadUserList();
    };
    
    return {
      loading,
      searchInput,
      userList,
      currentPage,
      pageSize,
      total,
      dialogVisible,
      dialogType,
      dialogTitle,
      submitLoading,
      userForm,
      userRules,
      userFormRef,
      handleSearch,
      handleAdd,
      handleEdit,
      handleDelete,
      handleToggleStatus,
      submitUserForm,
      handleSizeChange,
      handleCurrentChange
    };
  }
};
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style> 