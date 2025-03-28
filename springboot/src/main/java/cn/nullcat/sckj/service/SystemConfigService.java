package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.SystemConfig;

import java.util.List;

public interface SystemConfigService {
    SystemConfig getByConfigKey(String key);

    List<SystemConfig> getConfigList(String configType);

    void putConfig(SystemConfig systemConfig);

    List<SystemConfig> getByConfigKeys(List<String> keys);
}
