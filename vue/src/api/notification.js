import request from '@/utils/request';

/**
 * 获取当前用户的通知列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页条数，默认10
 * @param {number} params.type 通知类型 (可选)：0-系统通知，1-预订通知，2-审批通知
 * @param {number} params.is_read 是否已读 (可选)：0-未读，1-已读
 * @returns {Promise}
 */
export function getNotificationList(params) {
  console.log('通知列表请求参数:', params);
  return request({
    url: '/notification',
    method: 'get',
    params
  });
}

/**
 * 标记通知为已读
 * @param {number} id 通知ID
 * @returns {Promise}
 */
export function markNotificationRead(id) {
  return request({
    url: `/notification/${id}/read`,
    method: 'put'
  });
}

/**
 * 获取未读通知数量
 * @returns {Promise}
 */
export function getUnreadCount() {
  return request({
    url: '/notification/unread/count',
    method: 'get'
  });
}

/**
 * 发送通知 (管理员功能)
 * @param {Object} data 通知数据
 * @param {number} data.receiverType 接收者类型：0-指定用户，1-指定角色，2-所有用户
 * @param {number} data.receiverId 接收者ID (receiverType=0时必填)
 * @param {number} data.roleId 角色ID (receiverType=1时必填)
 * @param {number} data.type 通知类型：0-系统通知，1-预订通知，2-审批通知
 * @param {string} data.title 通知标题
 * @param {string} data.content 通知内容
 * @returns {Promise}
 */
export function sendNotification(data) {
  return request({
    url: '/notification/send',
    method: 'post',
    data
  });
}

/**
 * 删除通知 (管理员功能)
 * @param {number} id 通知ID
 * @returns {Promise}
 */
export function deleteNotification(id) {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  });
}

/**
 * 批量标记通知为已读
 * @param {Array} ids 通知ID数组
 * @returns {Promise}
 */
export function batchMarkRead(ids) {
  return request({
    url: '/notification/batch/read',
    method: 'put',
    data: { ids }
  });
} 