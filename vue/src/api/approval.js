import request from '@/utils/request';

/**
 * 获取待审批列表
 * @param {Object} params 分页参数
 * @returns {Promise}
 */
export function getPendingApprovals(params) {
  return request({
    url: '/approvals/pending',
    method: 'get',
    params
  });
}

/**
 * 获取已审批列表
 * @param {Object} params 分页和筛选参数
 * @returns {Promise}
 */
export function getApprovedList(params) {
  return request({
    url: '/approvals/approved',
    method: 'get',
    params
  });
}

/**
 * 审批预约
 * @param {Number} id 审批ID
 * @param {Object} data 审批数据
 * @param {Number} data.status 审批结果，1:批准, 2:拒绝
 * @param {String} data.comment 审批意见
 * @returns {Promise}
 */
export function approveBooking(id, data) {
  return request({
    url: `/approvals/${id}`,
    method: 'put',
    data
  });
} 