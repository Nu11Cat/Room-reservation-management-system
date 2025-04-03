<template>
  <div class="permission-management-container">
    <h2>权限管理系统</h2>
    <el-alert
      title="当前处于展示模式，修改操作不会生效"
      type="info"
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
              <el-button type="success" disabled>
                <el-icon><Plus /></el-icon>
                创建角色
              </el-button>
            </div>
          </div>
          
          <el-table :data="filteredUserList" border style="width: 100%">
            <el-table-column prop="id" label="用户ID" width="80" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="email" label="邮箱" width="180" />
            <el-table-column prop="roleName" label="当前角色" width="120">
              <template #default="scope">
                <el-tag :type="getRoleTagType(scope.row.roleId)">
                  {{ getRoleName(scope.row.roleId) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="修改角色" width="220">
              <template #default="scope">
                <el-select v-model="scope.row.roleId" disabled placeholder="选择角色">
                  <el-option :label="'管理员'" :value="1" />
                  <el-option :label="'普通用户'" :value="2" />
                  <el-option :label="'审批人'" :value="3" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default>
                <el-button size="small" type="primary" disabled>保存</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :total="mockUserList.length"
              :page-size="pageSize"
              :current-page="currentPage"
              @size-change="val => pageSize = val"
              @current-change="val => currentPage = val"
            />
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 角色权限矩阵 -->
      <el-tab-pane label="角色权限矩阵" name="role-matrix">
        <div class="tab-content">
          <div class="permission-matrix">
            <div class="matrix-header">
              <div class="role-cell header-role-cell">角色</div>
              <div class="header-group">
                <div class="header-title">系统管理</div>
                <div class="header-cells">
                  <div class="header-cell">用户管理</div>
                  <div class="header-cell">系统配置</div>
                  <div class="header-cell">系统日志</div>
                  <div class="header-cell">权限管理</div>
                </div>
              </div>
              <div class="header-group">
                <div class="header-title">会议室</div>
                <div class="header-cells">
                  <div class="header-cell">查看</div>
                  <div class="header-cell">添加</div>
                  <div class="header-cell">编辑</div>
                  <div class="header-cell">删除</div>
                </div>
              </div>
              <div class="header-group">
                <div class="header-title">预订管理</div>
                <div class="header-cells">
                  <div class="header-cell">预订</div>
                  <div class="header-cell">查看</div>
                  <div class="header-cell">编辑</div>
                  <div class="header-cell">取消</div>
                </div>
              </div>
              <div class="header-group">
                <div class="header-title">审批</div>
                <div class="header-cells">
                  <div class="header-cell">审批预订</div>
                </div>
              </div>
              <div class="header-group">
                <div class="header-title">统计</div>
                <div class="header-cells">
                  <div class="header-cell">使用统计</div>
                </div>
              </div>
              <div class="header-group">
                <div class="header-title">通知</div>
                <div class="header-cells">
                  <div class="header-cell">查看通知</div>
                  <div class="header-cell">发送通知</div>
                </div>
              </div>
            </div>
            
            <div class="matrix-body">
              <div v-for="role in roleList" :key="role.id" class="matrix-row">
                <div class="role-cell">{{ role.name }}</div>
                
                <!-- 系统管理权限 -->
                <div class="permission-group">
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'system:user')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'system:config')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'system:log')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'system:permission')" disabled />
                  </div>
                </div>
                
                <!-- 会议室权限 -->
                <div class="permission-group">
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'room:view')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'room:add')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'room:edit')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'room:delete')" disabled />
                  </div>
                </div>
                
                <!-- 预订管理权限 -->
                <div class="permission-group">
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'booking:create')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'booking:view')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'booking:edit')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'booking:cancel')" disabled />
                  </div>
                </div>
                
                <!-- 审批权限 -->
                <div class="permission-group">
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'approval:process')" disabled />
                  </div>
                </div>
                
                <!-- 统计分析权限 -->
                <div class="permission-group">
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'statistics:view')" disabled />
                  </div>
                </div>
                
                <!-- 通知权限 -->
                <div class="permission-group">
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'notification:view')" disabled />
                  </div>
                  <div class="permission-cell">
                    <el-checkbox :value="hasPermission(role.id, 'notification:send')" disabled />
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="action-buttons">
            <el-button type="success" disabled>
              <el-icon><Plus /></el-icon>
              创建角色
            </el-button>
            <el-button type="primary" disabled>保存角色权限设置</el-button>
            <el-button disabled>重置</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

export default {
  name: 'PermissionManagement',
  components: {
    Plus
  },
  setup() {
    const activeTab = ref('user-roles');
    const userSearchKeyword = ref('');
    const currentPage = ref(1);
    const pageSize = ref(10);
    
    // 模拟用户数据
    const mockUserList = [
      { id: 1, username: 'admin', realName: '超级管理员', email: 'admin@example.com', roleId: 1 },
      { id: 2, username: 'approver', realName: '审批专员', email: 'approver@example.com', roleId: 3 },
      { id: 3, username: 'user1', realName: '张三', email: 'zhang@example.com', roleId: 2 },
      { id: 4, username: 'user2', realName: '李四', email: 'li@example.com', roleId: 2 },
      { id: 5, username: 'user3', realName: '王五', email: 'wang@example.com', roleId: 2 },
      { id: 6, username: 'approver2', realName: '审批专员2', email: 'approver2@example.com', roleId: 3 },
      { id: 7, username: 'user4', realName: '赵六', email: 'zhao@example.com', roleId: 2 },
      { id: 8, username: 'admin2', realName: '管理员2', email: 'admin2@example.com', roleId: 1 },
      { id: 9, username: 'user5', realName: '孙七', email: 'sun@example.com', roleId: 2 },
      { id: 10, username: 'user6', realName: '周八', email: 'zhou@example.com', roleId: 2 },
      { id: 11, username: 'user7', realName: '吴九', email: 'wu@example.com', roleId: 2 },
      { id: 12, username: 'user8', realName: '郑十', email: 'zheng@example.com', roleId: 2 }
    ];
    
    // 角色列表
    const roleList = [
      { id: 1, name: '管理员' },
      { id: 2, name: '普通用户' },
      { id: 3, name: '审批人' }
    ];
    
    // 权限映射
    const permissionMap = {
      1: [ // 管理员权限
        'system:user', 'system:config', 'system:log', 'system:permission',
        'room:view', 'room:add', 'room:edit', 'room:delete',
        'booking:create', 'booking:view', 'booking:edit', 'booking:cancel',
        'approval:process',
        'statistics:view',
        'notification:view', 'notification:send'
      ],
      2: [ // 普通用户权限
        'room:view',
        'booking:create', 'booking:view', 'booking:edit', 'booking:cancel',
        'notification:view'
      ],
      3: [ // 审批人权限
        'room:view',
        'booking:view',
        'approval:process',
        'notification:view'
      ]
    };
    
    // 过滤后的用户列表
    const filteredUserList = computed(() => {
      let filteredList = [...mockUserList];
      
      if (userSearchKeyword.value) {
        const keyword = userSearchKeyword.value.toLowerCase();
        filteredList = filteredList.filter(user => 
          user.username.toLowerCase().includes(keyword) || 
          user.realName.toLowerCase().includes(keyword) || 
          user.email.toLowerCase().includes(keyword)
        );
      }
      
      // 分页
      const startIndex = (currentPage.value - 1) * pageSize.value;
      return filteredList.slice(startIndex, startIndex + pageSize.value);
    });
    
    // 根据角色ID获取角色名称
    const getRoleName = (roleId) => {
      const role = roleList.find(r => r.id === roleId);
      return role ? role.name : '未知角色';
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
    
    // 判断角色是否有某项权限
    const hasPermission = (roleId, permission) => {
      return permissionMap[roleId] && permissionMap[roleId].includes(permission);
    };
    
    // 显示保存消息
    const showSaveMessage = () => {
      ElMessage({
        message: '当前为展示模式，操作未实际执行',
        type: 'info'
      });
    };
    
    // 处理单元格合并
    const objectSpanMethod = ({ columnIndex }) => {
      // 如果是角色名称列，不做特殊处理
      if (columnIndex === 0) {
        return {
          rowspan: 1,
          colspan: 1
        };
      }
      return null;
    };
    
    return {
      activeTab,
      userSearchKeyword,
      currentPage,
      pageSize,
      mockUserList,
      filteredUserList,
      roleList,
      getRoleName,
      getRoleTagType,
      hasPermission,
      showSaveMessage,
      objectSpanMethod
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

.el-table :deep(.el-checkbox__input.is-disabled .el-checkbox__inner) {
  cursor: not-allowed;
}

/* 新增的权限矩阵样式 */
.permission-matrix {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow-x: auto;
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
}
</style> 