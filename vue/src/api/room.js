import request from '@/utils/request';

/**
 * 获取会议室列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.name - 会议室名称（可选）
 * @param {string} params.location - 位置（可选）
 * @param {number} params.capacity - 容量（可选）
 * @param {number} params.status - 状态（可选）
 * @returns {Promise}
 */
export function getRoomList(params) {
  return request({
    url: '/rooms',
    method: 'get',
    params
  });
}

/**
 * 获取会议室详情
 * @param {number} id - 会议室ID
 * @returns {Promise}
 */
export function getRoomDetail(id) {
  return request({
    url: `/rooms/${id}`,
    method: 'get'
  });
}

/**
 * 新增会议室
 * @param {Object} data - 会议室信息
 * @returns {Promise}
 */
export function addRoom(data) {
  return request({
    url: '/rooms',
    method: 'post',
    data
  });
}

/**
 * 修改会议室
 * @param {number} id - 会议室ID
 * @param {Object} data - 会议室信息
 * @returns {Promise}
 */
export function updateRoom(id, data) {
  return request({
    url: `/rooms/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除会议室
 * @param {number} id - 会议室ID
 * @returns {Promise}
 */
export function deleteRoom(id) {
  return request({
    url: `/rooms/${id}`,
    method: 'delete'
  });
} 