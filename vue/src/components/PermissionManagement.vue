<template>
  <div class="permission-management-container">
    <h2>权限管理系统</h2>
    <el-alert
      title="当前功能由于种种原因稳定性欠佳，请谨慎操作，后续将进行优化升级"
      type="warning"
      :closable="false"
      show-icon
    />
    
    <el-tabs v-model="activeTab" class="permission-tabs">
      <!-- 用户角色管理 -->
      <el-tab-pane label="用户角色管理" name="user-roles">
        <div class="tab-content">
          <div class="operation-bar">
            <div>
              <el-input
                v-model="userSearchKeyword"
                placeholder="搜索用户"
                prefix-icon="el-icon-search"
                clearable
                style="width: 250px"
              />
            </div>
            <div>
              <el-button type="primary" @click="loadUsers">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
          
          <el-table :data="filteredUserList" border style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="用户ID" width="80" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="email" label="邮箱" width="180" />
            <el-table-column prop="roleName" label="当前角色" width="120">
              <template #default="scope">
                <el-tag :type="getRoleTagType(scope.row.roleId)">
                  {{ scope.row.roleName }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="修改角色" width="220">
              <template #default="scope">
                <el-select v-model="scope.row.roleId" placeholder="选择角色">
                  <el-option 
                    v-for="role in roleList" 
                    :key="role.id" 
                    :label="role.roleName" 
                    :value="role.id" 
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button size="small" type="primary" @click="saveUserRole(scope.row)">保存</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :total="totalUsers"
              :page-size="pageSize"
              :current-page="currentPage"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 角色管理 -->
      <el-tab-pane label="角色管理" name="role-management">
        <div class="tab-content">
          <div class="operation-bar">
            <div>
              <el-button type="success" @click="openRoleDialog('add')">
                <el-icon><Plus /></el-icon>
                创建角色
              </el-button>
            </div>
            <div>
              <el-button type="primary" @click="loadRoles">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
          
          <el-table :data="roleList" border style="width: 100%" v-loading="rolesLoading">
            <el-table-column prop="id" label="角色ID" width="80" />
            <el-table-column prop="roleName" label="角色名称" width="150" />
            <el-table-column prop="roleCode" label="角色编码" width="150" />
            <el-table-column prop="description" label="角色描述" />
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button size="small" type="primary" @click="openRoleDialog('edit', scope.row)">编辑</el-button>
                <el-button size="small" type="success" @click="openPermissionDialog(scope.row)">权限设置</el-button>
                <el-button size="small" type="danger" @click="confirmDeleteRole(scope.row)" :disabled="scope.row.id <= 3">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 角色权限矩阵 -->
      <el-tab-pane label="权限矩阵" name="role-matrix">
        <div class="tab-content">
          <div class="permission-matrix" v-loading="matrixLoading">
            <div class="matrix-header">
              <div class="role-cell header-role-cell">角色</div>
              
              <template v-for="(group, groupIndex) in permissionGroups" :key="groupIndex">
                <div class="header-group">
                  <div class="header-title">{{ group.title }}</div>
                  <div class="header-cells">
                    <div class="header-cell" v-for="(perm, permIndex) in group.permissions" :key="permIndex">
                      {{ perm.name }}
                    </div>
                  </div>
                </div>
              </template>
            </div>
            
            <div class="matrix-body">
              <div v-for="role in roleList" :key="role.id" class="matrix-row">
                <div class="role-cell">{{ role.roleName }}</div>
                
                <template v-for="(group, groupIndex) in permissionGroups" :key="groupIndex">
                  <div class="permission-group">
                    <div class="permission-cell" v-for="(perm, permIndex) in group.permissions" :key="permIndex">
                      <el-checkbox 
                        v-model="rolePermissions[role.id][perm.code]" 
                        @change="handlePermissionChange(role.id, perm.code)"
                      />
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 角色编辑对话框 -->
    <el-dialog
      :title="roleDialogTitle"
      v-model="roleDialogVisible"
      width="500px"
    >
      <el-form :model="currentRole" label-width="100px" :rules="roleFormRules" ref="roleFormRef">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="currentRole.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="currentRole.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="currentRole.description" type="textarea" placeholder="请输入角色描述" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole" :loading="roleSaving">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 权限设置对话框 -->
    <el-dialog
      title="设置角色权限"
      v-model="permissionDialogVisible"
      width="600px"
    >
      <div v-if="currentRole.id">
        <h3>{{ currentRole.roleName }} 的权限设置</h3>
        
        <el-tree
          :data="permissionTree"
          show-checkbox
          node-key="id"
          ref="permissionTreeRef"
          :default-checked-keys="selectedPermissionIds"
          :props="{ label: 'name', children: 'children' }"
        />
      </div>
      
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRolePermissions" :loading="permissionSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, reactive, onMounted, nextTick, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh } from '@element-plus/icons-vue';
import { getAllUsers } from '@/api/user';
import { 
  getRoleList, 
  // eslint-disable-next-line no-unused-vars
  getRoleDetail,
  getRolePermissions, 
  getRolePermissionIds,
  addRole,
  updateRole,
  deleteRole,
  setRolePermissions,
  updateUserRole as updateUserRoleAPI 
} from '@/api/role';
// eslint-disable-next-line no-unused-vars
import { getAllPermissions } from '@/api/permission';

export default {
  name: 'PermissionManagement',
  components: {
    Plus,
    Refresh
  },
  setup() {
    const activeTab = ref('user-roles');
    const userSearchKeyword = ref('');
    const currentPage = ref(1);
    const pageSize = ref(10);
    const loading = ref(false);
    const rolesLoading = ref(false);
    const matrixLoading = ref(false);
    
    // 实际用户和角色数据
    const userList = ref([]);
    const roleList = ref([]);
    const permissionList = ref([]);
    const totalUsers = ref(0);
    
    // 角色对话框
    const roleDialogVisible = ref(false);
    const roleDialogTitle = ref('创建角色');
    const roleFormRef = ref(null);
    const roleSaving = ref(false);
    const currentRole = reactive({
      id: null,
      roleName: '',
      roleCode: '',
      description: ''
    });
    
    // 权限对话框
    const permissionDialogVisible = ref(false);
    const permissionTreeRef = ref(null);
    const permissionSaving = ref(false);
    const selectedPermissionIds = ref([]);
    
    // 表单验证规则
    const roleFormRules = {
      roleName: [
        { required: true, message: '请输入角色名称', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      roleCode: [
        { required: true, message: '请输入角色编码', trigger: 'blur' },
        { pattern: /^[A-Z_]+$/, message: '角色编码必须为大写字母和下划线', trigger: 'blur' }
      ]
    };
    
    // 角色权限矩阵
    const rolePermissions = reactive({});
    
    // 权限分组
    const permissionGroups = ref([
      {
        title: '系统管理',
        permissions: [
          { name: '用户管理', code: 'system:user' },
          { name: '系统配置', code: 'system:config' },
          { name: '系统日志', code: 'system:log' },
          { name: '权限管理', code: 'system:permission' }
        ]
      },
      {
        title: '会议室',
        permissions: [
          { name: '查看', code: 'room:view' },
          { name: '添加', code: 'room:add' },
          { name: '编辑', code: 'room:edit' },
          { name: '删除', code: 'room:delete' }
        ]
      },
      {
        title: '预订管理',
        permissions: [
          { name: '预订', code: 'booking:create' },
          { name: '查看', code: 'booking:view' },
          { name: '编辑', code: 'booking:edit' },
          { name: '取消', code: 'booking:cancel' }
        ]
      },
      {
        title: '审批',
        permissions: [
          { name: '审批预订', code: 'approval:process' }
        ]
      },
      {
        title: '统计',
        permissions: [
          { name: '使用统计', code: 'statistics:view' }
        ]
      },
      {
        title: '通知',
        permissions: [
          { name: '查看通知', code: 'notification:view' },
          { name: '发送通知', code: 'notification:send' }
        ]
      }
    ]);
    
    // 生成权限树形结构
    const permissionTree = computed(() => {
      const tree = [];
      
      permissionGroups.value.forEach(group => {
        const children = group.permissions.map(perm => ({
          id: perm.code,
          name: perm.name,
          code: perm.code
        }));
        
        tree.push({
          id: group.title,
          name: group.title,
          children
        });
      });
      
      return tree;
    });
    
    // 加载用户数据
    const loadUsers = async () => {
      loading.value = true;
      try {
        // 确保角色列表已加载
        if (roleList.value.length === 0) {
          await loadRoles();
        }
        
        const res = await getAllUsers({
          page: currentPage.value,
          pageSize: pageSize.value
        });
        
        if (res.code === 1) {
          userList.value = res.data.rows;
          totalUsers.value = res.data.total;
        } else {
          ElMessage.error(res.msg || '获取用户列表失败');
        }
      } catch (error) {
        console.error('加载用户数据失败:', error);
        ElMessage.error('加载用户数据失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 加载角色数据
    const loadRoles = async () => {
      rolesLoading.value = true;
      try {
        const res = await getRoleList();
        if (res.code === 1) {
          roleList.value = res.data;
          
          // 初始化角色权限矩阵
          roleList.value.forEach(role => {
            rolePermissions[role.id] = rolePermissions[role.id] || {};
            loadRolePermissions(role.id);
          });
        } else {
          ElMessage.error(res.msg || '获取角色列表失败');
        }
      } catch (error) {
        console.error('加载角色数据失败:', error);
        ElMessage.error('加载角色数据失败');
      } finally {
        rolesLoading.value = false;
      }
    };
    
    // 加载权限数据
    const loadPermissions = async () => {
      // 直接使用前端定义的权限组，不依赖API
      console.log('使用前端预定义的权限分组');
      // 如果将来API可用，可以取消下面的注释
      /*
      try {
        const res = await getAllPermissions();
        if (res.code === 1) {
          permissionList.value = res.data;
        } else {
          console.warn('获取权限列表失败，使用默认权限分组');
        }
      } catch (error) {
        console.error('加载权限数据失败:', error);
        console.warn('使用默认权限分组');
      }
      */
    };
    
    // 加载角色权限
    const loadRolePermissions = async (roleId) => {
      try {
        const res = await getRolePermissions(roleId);
        if (res.code === 1) {
          const permissions = res.data;
          
          // 更新角色权限矩阵
          permissionGroups.value.forEach(group => {
            group.permissions.forEach(perm => {
              rolePermissions[roleId][perm.code] = permissions.includes(perm.code);
            });
          });
        }
      } catch (error) {
        console.error(`加载角色ID ${roleId} 的权限失败:`, error);
      }
    };
    
    // 加载角色权限ID列表
    const loadRolePermissionIds = async (roleId) => {
      try {
        console.log(`加载角色ID ${roleId} 的权限ID列表`);
        const res = await getRolePermissionIds(roleId);
        console.log('权限ID列表响应:', res);
        if (res.code === 1) {
          selectedPermissionIds.value = res.data;
          console.log('设置选中的权限ID:', selectedPermissionIds.value);
        }
      } catch (error) {
        console.error(`加载角色ID ${roleId} 的权限ID列表失败:`, error);
        selectedPermissionIds.value = [];
      }
    };
    
    // 过滤后的用户列表
    const filteredUserList = computed(() => {
      let filteredList = [...userList.value];
      
      if (userSearchKeyword.value) {
        const keyword = userSearchKeyword.value.toLowerCase();
        filteredList = filteredList.filter(user => 
          user.username.toLowerCase().includes(keyword) || 
          (user.realName && user.realName.toLowerCase().includes(keyword)) || 
          (user.email && user.email.toLowerCase().includes(keyword))
        );
      }
      
      return filteredList;
    });
    
    // 根据角色ID获取角色名称
    const getRoleName = (roleId) => {
      const role = roleList.value.find(r => r.id === roleId);
      return role ? role.roleName : '未知角色';
    };
    
    // 根据角色ID获取标签类型
    const getRoleTagType = (roleId) => {
      switch (roleId) {
        case 1: return 'danger';  // 管理员
        case 2: return '';        // 普通用户
        case 3: return 'warning'; // 审批人
        default: return 'info';
      }
    };
    
    // 打开角色对话框
    const openRoleDialog = (type, role = null) => {
      roleDialogTitle.value = type === 'add' ? '创建角色' : '编辑角色';
      
      if (type === 'add') {
        Object.assign(currentRole, {
          id: null,
          roleName: '',
          roleCode: '',
          description: ''
        });
      } else {
        Object.assign(currentRole, {
          id: role.id,
          roleName: role.roleName,
          roleCode: role.roleCode,
          description: role.description || ''
        });
      }
      
      roleDialogVisible.value = true;
      
      nextTick(() => {
        if (roleFormRef.value) {
          roleFormRef.value.clearValidate();
        }
      });
    };
    
    // 保存角色
    const saveRole = async () => {
      if (!roleFormRef.value) return;
      
      await roleFormRef.value.validate(async (valid) => {
        if (!valid) return;
        
        roleSaving.value = true;
        
        try {
          let res;
          
          if (currentRole.id) {
            // 编辑现有角色
            res = await updateRole(currentRole.id, {
              roleName: currentRole.roleName,
              roleCode: currentRole.roleCode,
              description: currentRole.description
            });
          } else {
            // 创建新角色
            res = await addRole({
              roleName: currentRole.roleName,
              roleCode: currentRole.roleCode,
              description: currentRole.description
            });
          }
          
          if (res.code === 1) {
            ElMessage.success(currentRole.id ? '角色更新成功' : '角色创建成功');
            roleDialogVisible.value = false;
            loadRoles(); // 重新加载角色列表
          } else {
            ElMessage.error(res.msg || (currentRole.id ? '角色更新失败' : '角色创建失败'));
          }
        } catch (error) {
          console.error('保存角色失败:', error);
          ElMessage.error('保存角色失败');
        } finally {
          roleSaving.value = false;
        }
      });
    };
    
    // 确认删除角色
    const confirmDeleteRole = (role) => {
      if (role.id <= 3) {
        ElMessage.warning('不能删除系统预设角色');
        return;
      }
      
      ElMessageBox.confirm(`确定要删除角色 "${role.roleName}" 吗?`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteRole(role.id);
          if (res.code === 1) {
            ElMessage.success('角色删除成功');
            loadRoles(); // 重新加载角色列表
          } else {
            ElMessage.error(res.msg || '角色删除失败');
          }
        } catch (error) {
          console.error('删除角色失败:', error);
          ElMessage.error('删除角色失败');
        }
      }).catch(() => {
        // 用户取消删除
      });
    };
    
    // 打开权限设置对话框
    const openPermissionDialog = async (role) => {
      Object.assign(currentRole, {
        id: role.id,
        roleName: role.roleName,
        roleCode: role.roleCode,
        description: role.description || ''
      });
      
      // 加载角色的权限ID列表
      await loadRolePermissionIds(role.id);
      
      permissionDialogVisible.value = true;
    };
    
    // 保存角色权限设置
    const saveRolePermissions = async () => {
      if (!permissionTreeRef.value || !currentRole.id) return;
      
      permissionSaving.value = true;
      
      try {
        // 获取所选权限ID
        const selectedPermissions = permissionTreeRef.value.getCheckedKeys(true);
        // 过滤掉不是权限代码的项（如分组标题）
        const permissionCodes = selectedPermissions.filter(id => id.includes(':'));
        
        const res = await setRolePermissions(currentRole.id, permissionCodes);
        if (res.code === 1) {
          ElMessage.success('角色权限设置成功');
          permissionDialogVisible.value = false;
          
          // 更新权限矩阵
          loadRolePermissions(currentRole.id);
        } else {
          ElMessage.error(res.msg || '角色权限设置失败');
        }
      } catch (error) {
        console.error('保存角色权限失败:', error);
        ElMessage.error('保存角色权限失败');
      } finally {
        permissionSaving.value = false;
      }
    };
    
    // 处理权限变更
    const handlePermissionChange = async (roleId, permissionCode) => {
      try {
        matrixLoading.value = true;
        
        // 获取当前角色的所有权限
        const permissionCodes = [];
        
        permissionGroups.value.forEach(group => {
          group.permissions.forEach(perm => {
            if (rolePermissions[roleId][perm.code]) {
              permissionCodes.push(perm.code);
            }
          });
        });
        
        // 更新角色权限
        const res = await setRolePermissions(roleId, permissionCodes);
        if (res.code !== 1) {
          ElMessage.error(res.msg || '更新权限失败');
          
          // 恢复原状态
          rolePermissions[roleId][permissionCode] = !rolePermissions[roleId][permissionCode];
        }
      } catch (error) {
        console.error('更新权限失败:', error);
        ElMessage.error('更新权限失败');
        
        // 恢复原状态
        rolePermissions[roleId][permissionCode] = !rolePermissions[roleId][permissionCode];
      } finally {
        matrixLoading.value = false;
      }
    };
    
    // 更新用户角色
    const updateUserRole = async (userId, roleId) => {
      try {
        const res = await updateUserRoleAPI(userId, roleId);
        if (res.code === 1) {
          ElMessage.success('用户角色更新成功');
          // 重新加载用户列表
          loadUsers();
        } else {
          ElMessage.error(res.msg || '用户角色更新失败');
        }
      } catch (error) {
        console.error('更新用户角色失败:', error);
        ElMessage.error('更新用户角色失败');
      }
    };
    
    // 保存用户角色变更
    const saveUserRole = (user) => {
      const newRole = roleList.value.find(r => r.id === user.roleId);
      if (!newRole) {
        ElMessage.error('角色不存在');
        return;
      }
      
      ElMessageBox.confirm(`确定将用户 ${user.username} 的角色修改为 ${newRole.roleName}?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        updateUserRole(user.id, user.roleId);
      }).catch(() => {
        // 取消操作，不做任何处理
      });
    };
    
    // 监听分页变化，重新加载数据
    const handleSizeChange = (newSize) => {
      pageSize.value = newSize;
      loadUsers();
    };
    
    const handleCurrentChange = (newPage) => {
      currentPage.value = newPage;
      loadUsers();
    };
    
    // 组件挂载时加载数据
    onMounted(async () => {
      // 先加载角色数据，确保角色名称正确显示
      await loadRoles();
      // 然后再加载用户数据
      loadUsers();
      
      // 延迟加载权限数据，减少同时发生的大量渲染
      setTimeout(() => {
        loadPermissions();
      }, 300);
    });
    
    // 避免打开角色管理和权限矩阵标签时出现问题
    watch(activeTab, (newTab) => {
      if (newTab === 'role-management' || newTab === 'role-matrix') {
        if (!roleList.value || roleList.value.length === 0) {
          loadRoles();
        }
      }
    });
    
    return {
      activeTab,
      userSearchKeyword,
      currentPage,
      pageSize,
      loading,
      rolesLoading,
      matrixLoading,
      userList,
      roleList,
      permissionList,
      totalUsers,
      filteredUserList,
      permissionGroups,
      rolePermissions,
      getRoleName,
      getRoleTagType,
      saveUserRole,
      handleSizeChange,
      handleCurrentChange,
      loadUsers,
      loadRoles,
      
      // 角色管理
      roleDialogVisible,
      roleDialogTitle,
      roleFormRef,
      roleSaving,
      currentRole,
      roleFormRules,
      openRoleDialog,
      saveRole,
      confirmDeleteRole,
      
      // 权限设置
      permissionDialogVisible,
      permissionTreeRef,
      permissionSaving,
      permissionTree,
      selectedPermissionIds,
      openPermissionDialog,
      saveRolePermissions,
      handlePermissionChange
    };
  }
};
</script>

<style scoped>
.permission-management-container {
  padding: 20px;
}

.permission-tabs {
  margin-top: 20px;
}

.tab-content {
  margin-top: 20px;
}

.operation-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.matrix-table-container {
  width: 100%;
  overflow-x: auto;
}

.el-table :deep(.cell) {
  text-align: center;
}

.el-table :deep(th.el-table__cell) {
  background-color: #f5f7fa;
}

/* 权限矩阵样式 */
.permission-matrix {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow-x: auto;
  margin-bottom: 20px;
  contain: layout;
  height: auto;
  position: relative;
}

.matrix-header {
  display: flex;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.matrix-body {
  background-color: #fff;
}

.matrix-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
}

.matrix-row:last-child {
  border-bottom: none;
}

.role-cell {
  width: 120px;
  min-width: 120px;
  padding: 12px 0;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #ebeef5;
  background-color: #f5f7fa;
  flex-shrink: 0;
}

.header-role-cell {
  padding: 8px 0;
}

.header-group {
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}

.header-group:last-child {
  border-right: none;
}

.header-title {
  text-align: center;
  padding: 8px 0;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
  width: 100%;
}

.header-cells {
  display: flex;
}

.header-cell {
  width: 80px;
  min-width: 80px;
  padding: 8px 0;
  text-align: center;
  flex-shrink: 0;
}

.permission-group {
  display: flex;
  border-right: 1px solid #ebeef5;
}

.permission-group:last-child {
  border-right: none;
}

.permission-cell {
  width: 80px;
  min-width: 80px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: visible;
}
</style> 