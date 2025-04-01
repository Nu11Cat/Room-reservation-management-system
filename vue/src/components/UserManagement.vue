<template>
  <div class="user-container">
    <h1>用户管理</h1>
    <div class="toolbar">
      <el-input v-model="searchInput" placeholder="搜索用户名" style="width: 300px" clearable />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleAdd">添加用户</el-button>
    </div>
    <el-table :data="userList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名">
        <template #default="scope">
          {{ scope.row.username || '未设置' }}
        </template>
      </el-table-column>
      <el-table-column prop="realName" label="真实姓名" width="120">
        <template #default="scope">
          {{ scope.row.realName || '未设置' }}
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" width="180">
        <template #default="scope">
          {{ scope.row.email || '未设置' }}
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="140">
        <template #default="scope">
          {{ scope.row.phone || '未设置' }}
        </template>
      </el-table-column>
      <el-table-column prop="roleName" label="角色" width="120">
        <template #default="scope">
          <span v-if="scope.row.roleId === 1">管理员</span>
          <span v-else-if="scope.row.roleId === 2">普通用户</span>
          <span v-else-if="scope.row.roleId === 3">审批人</span>
          <span v-else>未知角色</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="scope">
          <div class="operation-buttons">
            <el-button size="small" type="info" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" :type="scope.row.status === 1 ? 'danger' : 'success'" @click="handleToggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </div>
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
      class="pagination"
    />

    <!-- 添加/编辑用户对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="userForm.phone" />
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

    <!-- 查看用户详情对话框 -->
    <el-dialog title="用户详情" v-model="viewDialogVisible" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ viewUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ viewUser.username }}</el-descriptions-item>
        <el-descriptions-item label="密码">
          <div class="password-field">
            <span v-if="!passwordVisible">••••••••</span>
            <span v-else>{{ viewUser.password || '未设置' }}</span>
            <el-icon class="password-toggle" @click="passwordVisible = !passwordVisible">
              <Hide v-if="passwordVisible" />
              <View v-else />
            </el-icon>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ viewUser.realName || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ viewUser.email || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="手机号码">{{ viewUser.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <span v-if="viewUser.roleId === 1">管理员</span>
          <span v-else-if="viewUser.roleId === 2">普通用户</span>
          <span v-else-if="viewUser.roleId === 3">审批人</span>
          <span v-else>未知角色</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewUser.status === 1 ? 'success' : 'danger'">
            {{ viewUser.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(viewUser.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(viewUser.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getAllUsers, addUser, editUser, banUser, getUserById } from '@/api/user';
import { View, Hide } from '@element-plus/icons-vue';

export default {
  name: 'UserManagementPage',
  components: {
    View,
    Hide
  },
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
    
    // 查看用户详情相关
    const viewDialogVisible = ref(false);
    const passwordVisible = ref(false);
    const viewUser = reactive({
      id: null,
      username: '',
      password: '',
      realName: '',
      email: '',
      phone: '',
      roleId: 0,
      status: 1,
      createTime: '',
      updateTime: ''
    });
    
    const userForm = reactive({
      id: null,
      username: '',
      password: '',
      email: '',
      realName: '',
      phone: '',
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
    
    // 格式化日期时间
    const formatDateTime = (datetime) => {
      if (!datetime) return '';
      const date = new Date(datetime);
      const y = date.getFullYear();
      const m = (date.getMonth() + 1).toString().padStart(2, '0');
      const d = date.getDate().toString().padStart(2, '0');
      const h = date.getHours().toString().padStart(2, '0');
      const min = date.getMinutes().toString().padStart(2, '0');
      return `${y}-${m}-${d} ${h}:${min}`;
    };
    
    // 加载用户列表
    const loadUserList = async () => {
      loading.value = true;
      try {
        const res = await getAllUsers({
          page: currentPage.value,
          pageSize: pageSize.value,
          username: searchInput.value || undefined
        });
        
        // 打印返回的数据结构，方便调试
        console.log('后端返回的用户数据:', res);
        
        if (res.code === 1) {
          const rows = res.data.rows || [];
          // 打印第一条记录，查看字段结构
          if (rows.length > 0) {
            console.log('第一条用户记录:', rows[0]);
          }
          
          userList.value = rows.map(row => ({
            id: row.id,
            username: row.username || '',
            realName: row.realName || '', // 后端返回的是realName
            email: row.email || '',
            phone: row.phone || '',
            // 根据用户名确定角色ID
            roleId: row.roleId,
            status: row.status === undefined ? 1 : row.status,
            createTime: row.createTime, // 后端返回的是createTime
            updateTime: row.updateTime  // 后端返回的是updateTime
          }));
          
          // 打印转换后的数据
          console.log('转换后的用户数据:', userList.value);
          
          total.value = res.data.total || 0;
        } else {
          ElMessage.error(res.msg || '获取用户列表失败');
        }
      } catch (error) {
        console.error('获取用户列表失败:', error);
        ElMessage.error('获取用户列表失败');
      } finally {
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
        email: row.email || '',
        realName: row.realName || '',
        phone: row.phone || '',
        roleId: row.roleId
      });
      dialogVisible.value = true;
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
          const res = await banUser(row.id);
          if (res.code === 1) {
            ElMessage.success(res.msg || `${action}用户成功`);
            loadUserList();
          } else {
            ElMessage.error(res.msg || `${action}用户失败`);
          }
        } catch (error) {
          console.error(`${action}用户失败:`, error);
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
            let res;
            // 转换为后端需要的格式
            const submitData = {
              id: userForm.id,
              username: userForm.username,
              password: userForm.password || undefined,
              email: userForm.email,
              realName: userForm.realName, // 保持驼峰格式
              phone: userForm.phone,
              roleId: userForm.roleId // 保持驼峰格式
            };
            
            console.log('提交的用户数据:', submitData);
            
            if (dialogType.value === 'add') {
              res = await addUser(submitData);
            } else {
              res = await editUser(submitData);
            }
            
            if (res.code === 1) {
              ElMessage.success(res.msg || (dialogType.value === 'add' ? '添加用户成功' : '编辑用户成功'));
              dialogVisible.value = false;
              loadUserList();
            } else {
              ElMessage.error(res.msg || (dialogType.value === 'add' ? '添加用户失败' : '编辑用户失败'));
            }
          } catch (error) {
            console.error(dialogType.value === 'add' ? '添加用户失败:' : '编辑用户失败:', error);
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
      userForm.realName = '';
      userForm.phone = '';
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
    
    // 查看用户详情
    const handleView = async (row) => {
      // 重置密码可见性为隐藏状态
      passwordVisible.value = false;
      
      try {
        // 如果需要获取更详细的用户信息，可以调用API
        const res = await getUserById(row.id);
        if (res.code === 1 && res.data) {
          Object.assign(viewUser, res.data);
        } else {
          // 如果API调用失败，使用表格中的数据
          Object.assign(viewUser, row);
        }
        viewDialogVisible.value = true;
      } catch (error) {
        console.error('获取用户详情失败:', error);
        ElMessage.error('获取用户详情失败');
        // 使用表格中的数据作为后备
        Object.assign(viewUser, row);
        viewDialogVisible.value = true;
      }
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
      viewDialogVisible,
      viewUser,
      passwordVisible,
      formatDateTime,
      handleSearch,
      handleAdd,
      handleEdit,
      handleView,
      handleToggleStatus,
      handleSizeChange,
      handleCurrentChange,
      submitUserForm
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.operation-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.password-field {
  display: flex;
  align-items: center;
}

.password-toggle {
  margin-left: 10px;
  cursor: pointer;
  color: #409EFF;
}
</style> 