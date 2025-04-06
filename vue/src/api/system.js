import request from '@/utils/request';

/**
 * 获取系统配置
 * @param {string} key 配置键名
 * @returns {Promise}
 */
export function getConfig(key) {
  return request({
    url: '/system/config',
    method: 'get',
    params: { key }
  });
}

/**
 * 获取配置列表
 * @param {string} configType 配置类型
 * @returns {Promise}
 */
export function getConfigList(configType) {
  return request({
    url: '/system/config/list',
    method: 'get',
    params: { configType }
  });
}

/**
 * 修改系统配置
 * @param {Object} data 配置数据
 * @param {string} data.configKey 配置键名
 * @param {string} data.configValue 配置值
 * @param {string} data.configType 配置类型
 * @param {string} data.description 配置描述（可选）
 * @returns {Promise}
 */
export function updateConfig(data) {
  return request({
    url: '/system/config',
    method: 'put',
    data
  });
}

/**
 * 批量获取配置
 * @param {Array} keys 配置键名数组
 * @returns {Promise}
 */
export function batchGetConfig(keys) {
  return request({
    url: '/system/config/batch',
    method: 'post',
    data: { keys }
  });
}

/**
 * 批量更新配置
 * @param {Array} configs 配置数组，每项包含configKey和configValue
 * @returns {Promise}
 */
export function batchUpdateConfig(configs) {
  return request({
    url: '/system/config/batch',
    method: 'put',
    data: { configs }
  });
} 