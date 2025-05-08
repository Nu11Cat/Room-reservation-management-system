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
    url: '/review/process/statistics',
    method: 'get'
  })
}