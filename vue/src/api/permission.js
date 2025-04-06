import request from '@/utils/request';

// 获取所有权限列表
export function getAllPermissions() {
  return request({
    url: '/permissions',
    method: 'get'
  });
}

// 获取权限详情
export function getPermissionDetail(permissionId) {
  return request({
    url: `/permissions/${permissionId}`,
    method: 'get'
  });
} 