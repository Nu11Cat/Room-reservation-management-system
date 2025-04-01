import request from '@/utils/request';

/**
 * 获取会议室使用率统计
 * @param {Object} params 查询参数
 * @param {string} params.startDate 开始日期，格式：YYYY-MM-DD
 * @param {string} params.endDate 结束日期，格式：YYYY-MM-DD
 * @returns {Promise}
 */
export function getRoomUsageStatistics(params) {
  return request({
    url: '/statistics/room-usage',
    method: 'get',
    params
  });
}

/**
 * 获取每日预订时段分布
 * @param {Object} params 查询参数
 * @param {string} params.startDate 开始日期，格式：YYYY-MM-DD
 * @param {string} params.endDate 结束日期，格式：YYYY-MM-DD
 * @returns {Promise}
 */
export function getHourlyDistribution(params) {
  return request({
    url: '/statistics/hourly-distribution',
    method: 'get',
    params
  });
}

/**
 * 获取预订状态统计
 * @param {Object} params 查询参数
 * @param {string} params.startDate 开始日期，格式：YYYY-MM-DD
 * @param {string} params.endDate 结束日期，格式：YYYY-MM-DD
 * @returns {Promise}
 */
export function getBookingStatusStatistics(params) {
  return request({
    url: '/statistics/booking-status',
    method: 'get',
    params
  });
}

/**
 * 获取部门/用户预订排名
 * @param {Object} params 查询参数
 * @param {string} params.startDate 开始日期，格式：YYYY-MM-DD
 * @param {string} params.endDate 结束日期，格式：YYYY-MM-DD
 * @param {string} params.type 排名类型, 'user'或'department'
 * @returns {Promise}
 */
export function getUserBookingRanking(params) {
  return request({
    url: '/statistics/user-ranking',
    method: 'get',
    params
  });
}

/**
 * 获取会议时长统计
 * @param {Object} params 查询参数
 * @param {string} params.startDate 开始日期，格式：YYYY-MM-DD
 * @param {string} params.endDate 结束日期，格式：YYYY-MM-DD
 * @returns {Promise}
 */
export function getMeetingDurationStatistics(params) {
  return request({
    url: '/statistics/meeting-duration',
    method: 'get',
    params
  });
}

/**
 * 获取所有统计数据
 * @param {Object} params 查询参数
 * @param {string} params.startDate 开始日期，格式：YYYY-MM-DD
 * @param {string} params.endDate 结束日期，格式：YYYY-MM-DD
 * @returns {Promise}
 */
export function getAllStatistics(params) {
  return request({
    url: '/statistics/all',
    method: 'get',
    params
  });
} 