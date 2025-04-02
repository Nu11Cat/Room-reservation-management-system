import request from '@/utils/request'

// 获取操作日志列表
export function getOperationLogs(params) {
  return request({
    url: '/system/logs',
    method: 'get',
    params
  })
}

// 获取所有日志操作模块
export function getLogModules() {
  return request({
    url: '/system/log-modules',
    method: 'get'
  })
}

// 获取所有日志操作类型
export function getLogOperations() {
  return request({
    url: '/system/log-operations',
    method: 'get'
  })
} 