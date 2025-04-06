import request from '@/utils/request';

// 获取所有角色列表
export function getRoleList() {
  return request({
    url: '/roles',
    method: 'get'
  });
}

// 获取角色详情
export function getRoleDetail(roleId) {
  return request({
    url: `/roles/${roleId}`,
    method: 'get'
  });
}

// 获取角色权限
export function getRolePermissions(roleId) {
  return request({
    url: `/roles/${roleId}/permissions`,
    method: 'get'
  });
}

// 获取角色权限ID列表
export function getRolePermissionIds(roleId) {
  return request({
    url: `/roles/${roleId}/permissionIds`,
    method: 'get'
  });
}

// 创建角色
export function addRole(roleData) {
  return request({
    url: '/roles',
    method: 'post',
    data: roleData
  });
}

// 更新角色
export function updateRole(roleId, roleData) {
  return request({
    url: `/roles/${roleId}`,
    method: 'put',
    data: roleData
  });
}

// 删除角色
export function deleteRole(roleId) {
  return request({
    url: `/roles/${roleId}`,
    method: 'delete'
  });
}

// 设置角色权限
export function setRolePermissions(roleId, permissionCodes) {
  return request({
    url: `/roles/${roleId}/permissions`,
    method: 'put',
    data: { permissionCodes }
  });
}

// 为角色添加权限
export function addRolePermission(roleId, permissionCode) {
  return request({
    url: `/roles/${roleId}/permissions`,
    method: 'post',
    data: { permissionCode }
  });
}

// 移除角色权限
export function removeRolePermission(roleId, permissionId) {
  return request({
    url: `/roles/${roleId}/permissions/${permissionId}`,
    method: 'delete'
  });
}

// 修改用户角色
export function updateUserRole(userId, roleId) {
  return request({
    url: `/user/${userId}/role`,
    method: 'put',
    data: { roleId }
  });
}

// 移除角色权限（通过权限编码）
export function removeRolePermissionByCode(roleId, permissionCode) {
  return request({
    url: `/roles/${roleId}/permissions/code/${permissionCode}`,
    method: 'delete'
  });
} 