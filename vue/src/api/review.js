import request from '@/utils/request'

// 获取我发出的评价列表
export function getOutgoingReviews(params) {
  return request({
    url: '/review/outgoing',
    method: 'get',
    params
  })
}

// 获取我收到的评价列表
export function getIncomingReviews(params) {
  return request({
    url: '/review/incoming',
    method: 'get',
    params
  })
}

// 提交评价
export function submitReview(data) {
  return request({
    url: '/review',
    method: 'post',
    data
  })
}

// 修改评价
export function updateReview(id, data) {
  return request({
    url: `/review/${id}`,
    method: 'put',
    data
  })
}

// 删除评价
export function deleteReview(id) {
  return request({
    url: `/review/${id}`,
    method: 'delete'
  })
}

// 获取评价列表（管理员）
export function getReviewList(params) {
  return request({
    url: '/review/process/pending',
    method: 'get',
    params
  })
}

// 获取评价详情
export function getReviewDetail(id) {
  return request({
    url: `/review/${id}`,
    method: 'get'
  })
}

// 处理评价
export function processReview(id, data) {
  return request({
    url: `/review/process/${id}`,
    method: 'post',
    data
  })
}

// 批量处理评价
export function batchProcessReview(data) {
  return request({
    url: '/review/process/batch',
    method: 'post',
    data
  })
}

// 撤销处理评价
export function undoProcessReview(id) {
  return request({
    url: `/review/process/undo/${id}`,
    method: 'post'
  })
}

// 获取已处理评价列表
export function getProcessedReviewList(params) {
  return request({
    url: '/review/process/processed',
    method: 'get',
    params
  })
}

// 获取评价处理统计信息
export function getReviewStatistics() {
  return request({
    url: '/review/statistics',
    method: 'get'
  })
}

// 获取不文明行为类型列表
export function getReviewTypeList(params) {
  return request({
    url: '/misconduct/types/page',
    method: 'get',
    params
  })
}

// 获取所有不文明行为类型（不分页）
export function getAllReviewTypes() {
  return request({
    url: '/misconduct/types/all',
    method: 'get'
  })
}

// 获取不文明行为类型详情
export function getReviewTypeDetail(id) {
  return request({
    url: `/misconduct/types/${id}`,
    method: 'get'
  })
}

// 创建不文明行为类型
export function createReviewType(data) {
  return request({
    url: '/misconduct/types',
    method: 'post',
    data
  })
}

// 更新不文明行为类型
export function updateReviewType(id, data) {
  return request({
    url: `/misconduct/types/${id}`,
    method: 'put',
    data
  })
}

// 删除不文明行为类型
export function deleteReviewType(id) {
  return request({
    url: `/misconduct/types/${id}`,
    method: 'delete'
  })
}