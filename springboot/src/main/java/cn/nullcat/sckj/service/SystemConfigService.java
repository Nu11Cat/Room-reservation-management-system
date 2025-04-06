package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.SystemConfig;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {
    /**
     * 获取单个系统配置
     * @param key 配置键
     * @return 配置对象
     */
    SystemConfig getByConfigKey(String key);

    /**
     * 获取所有系统配置
     * @return 配置键值对映射
     */
    Map<String, SystemConfig> getAllConfigs();

    /**
     * 获取指定类型的配置列表
     * @param configType 配置类型
     * @return 配置列表
     */
    List<SystemConfig> getConfigList(String configType);

    /**
     * 修改系统配置
     * @param systemConfig 配置对象
     */
    void putConfig(SystemConfig systemConfig);

    /**
     * 批量获取配置
     * @param keys 配置键列表
     * @return 配置列表
     */
    List<SystemConfig> getByConfigKeys(List<String> keys);
}
