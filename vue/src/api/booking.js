import request from '@/utils/request';

/**
 * 获取预约列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} params.roomId - 会议室ID（可选）
 * @param {number} params.userId - 预约用户ID（可选）
 * @param {number} params.status - 状态（可选）
 * @param {string} params.begin - 开始时间（可选）
 * @param {string} params.end - 结束时间（可选）
 * @returns {Promise}
 */
export function getBookingList(params) {
  return request({
    url: '/bookings',
    method: 'get',
    params
  });
}

/**
 * 获取预约详情
 * @param {number} id - 预约ID
 * @returns {Promise}
 */
export function getBookingDetail(id) {
  return request({
    url: `/bookings/${id}`,
    method: 'get'
  });
}

/**
 * 添加会议室预订
 * @param {Object} data - 预约信息
 * @param {number} data.roomId - 会议室ID
 * @param {string} data.title - 会议主题
 * @param {string} data.startTime - 开始时间，格式：YYYY-MM-DD HH:MM:SS
 * @param {string} data.endTime - 结束时间，格式：YYYY-MM-DD HH:MM:SS
 * @param {string} data.attendees - 参会人员
 * @param {string} data.description - 会议描述
 * @returns {Promise}
 */
export function addBooking(data) {
  return request({
    url: '/bookings',
    method: 'post',
    data
  });
}

/**
 * 修改预约
 * @param {number} id - 预约ID
 * @param {Object} data - 预约信息
 * @returns {Promise}
 */
export function updateBooking(id, data) {
  return request({
    url: `/bookings/${id}`,
    method: 'put',
    data
  });
}

/**
 * 取消预约
 * @param {number} id - 预约ID
 * @returns {Promise}
 */
export function cancelBooking(id) {
  return request({
    url: `/bookings/${id}/cancel`,
    method: 'put'
  });
}

/**
 * 获取会议室某日预订列表
 * @param {Object} params
 * @param {number} params.roomId - 会议室ID
 * @param {string} params.date - 日期，格式：YYYY-MM-DD
 * @returns {Promise}
 */
export function getBookingsByRoom(params) {
  return request({
    url: '/bookings',
    method: 'get',
    params
  });
}

/**
 * 检查时间段是否可用
 * @param {Object} data
 * @param {number} data.roomId - 会议室ID
 * @param {string} data.startTime - 开始时间，格式：YYYY-MM-DD HH:MM:SS
 * @param {string} data.endTime - 结束时间，格式：YYYY-MM-DD HH:MM:SS
 * @returns {Promise}
 */
export function checkTimeSlot(data) {
  return request({
    url: '/bookings/check',
    method: 'post',
    data
  });
}

/**
 * 获取用户的预订列表
 * @param {Object} params
 * @param {number} params.status - 预订状态，0:待审批, 1:已批准, 2:已拒绝, 3:已取消
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise}
 */
export function getUserBookings(params) {
  return request({
    url: '/bookings/user',
    method: 'get',
    params
  });
}

/**
 * 获取待审批的预订列表
 * @param {Object} params
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise}
 */
export function getPendingBookings(params) {
  return request({
    url: '/bookings/pending',
    method: 'get',
    params
  });
}

/**
 * 审批预订
 * @param {Object} data
 * @param {number} data.id - 预订ID
 * @param {number} data.status - 审批结果，1:批准, 2:拒绝
 * @param {string} data.comment - 审批意见
 * @returns {Promise}
 */
export function approveBooking(data) {
  return request({
    url: `/bookings/${data.id}/approve`,
    method: 'put',
    data: {
      status: data.status,
      comment: data.comment
    }
  });
} 