import request from '@/utils/request';

/**
 * 获取特殊时间段配置列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise}
 */
export function getSpecialTimePeriods(params) {
  return request({
    url: '/special-time-period',
    method: 'get',
    params
  });
}

/**
 * 获取当前周配置
 * @returns {Promise}
 */
export function getCurrentTimePeriod() {
  return request({
    url: '/special-time-period/current',
    method: 'get'
  });
}

/**
 * 手动调整特殊时间段配置
 * @param {Object} data - 特殊时间段配置信息
 * @returns {Promise}
 */
export function updateSpecialTimePeriod(data) {
  return request({
    url: '/special-time-period/adjust',
    method: 'post',
    data
  });
}

/**
 * 自动调整下一周的特殊时间段配置
 * @returns {Promise}
 */
export function autoAdjustSpecialTimePeriod() {
  return request({
    url: '/special-time-period/auto-adjust',
    method: 'post'
  });
}

/**
 * 检查用户在特殊时间段是否可以预约会议室
 * @param {Object} params - 查询参数
 * @param {number} params.userId - 用户ID
 * @param {number} params.roomId - 会议室ID
 * @param {string} params.startDate - 开始日期（格式：yyyy-MM-dd）
 * @returns {Promise}
 */
export function checkSpecialTimePeriod(params) {
  return request({
    url: '/special-time-period/check',
    method: 'get',
    params
  });
}

/**
 * 初始化特殊时间段配置
 * @returns {Promise}
 */
export function initializeSpecialTimePeriod() {
  return request({
    url: '/special-time-period/initialize',
    method: 'post'
  });
}

/**
 * 删除特殊时间段配置
 * @param {number} id - 特殊时间段ID
 * @returns {Promise}
 */
export function deleteSpecialTimePeriod(id) {
  return request({
    url: `/special-time-period/${id}`,
    method: 'delete'
  });
}

/**
 * 创建特殊时间段配置
 * @param {Object} data - 特殊时间段配置信息
 * @returns {Promise}
 */
export function createSpecialTimePeriod(data) {
  return request({
    url: '/special-time-period',
    method: 'post',
    data
  });
}