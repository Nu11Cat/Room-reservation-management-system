import request from '@/utils/request';

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  });
}

// 用户注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  });
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  });
}

// 修改用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  });
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  });
}

// 用户登出
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  });
}

// 获取所有用户
export function getAllUsers(params) {
  return request({
    url: '/user/users',
    method: 'get',
    params
  });
}

// 获取指定用户
export function getUserById(id) {
  return request({
    url: '/user/getById',
    method: 'get',
    params: { id }
  });
}

// 添加用户
export function addUser(data) {
  return request({
    url: '/user/add',
    method: 'post',
    data
  });
}

// 修改指定用户
export function editUser(data) {
  return request({
    url: '/user/editById',
    method: 'put',
    data
  });
}

// 禁用或启用用户
export function banUser(id) {
  return request({
    url: `/user/ban/${id}`,
    method: 'delete'
  });
}

// 获取用户简单列表（不分页）
export function getUsers() {
  return request({
    url: '/user/list',
    method: 'get'
  });
} 