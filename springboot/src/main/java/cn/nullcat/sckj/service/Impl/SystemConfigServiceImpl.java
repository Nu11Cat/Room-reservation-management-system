package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.SystemConfigMapper;
import cn.nullcat.sckj.pojo.SystemConfig;
import cn.nullcat.sckj.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplateForObject;

    private static final String CONFIG_KEY_PREFIX = "system:config:";
    private static final Long CONFIG_EXPIRE = 24L; // 24小时
    private static final TimeUnit CONFIG_EXPIRE_UNIT = TimeUnit.HOURS;
    /**
     * 获取系统配置
     * @param key
     * @return
     */
    @Override
    public SystemConfig getByConfigKey(String key) {
        //SystemConfig systemConfig = systemConfigMapper.getByConfigKey(key);
        //return systemConfig;
        // 1. 先从Redis获取
        String redisKey = CONFIG_KEY_PREFIX + key;
        Object value = redisTemplateForObject.opsForValue().get(redisKey);
        if (value != null) {
            return (SystemConfig) value;
        }

        // 2. Redis没有，从MySQL获取
        SystemConfig systemConfig = systemConfigMapper.getByConfigKey(key);
        if (systemConfig != null) {
            // 3. 存入Redis
            redisTemplateForObject.opsForValue().set(redisKey, systemConfig, CONFIG_EXPIRE, CONFIG_EXPIRE_UNIT);
        }
        return systemConfig;
    }

    /**
     * 获取配置列表
     * @param configType
     * @return
     */
    @Override
    public List<SystemConfig> getConfigList(String configType) {
        //List<SystemConfig> systemConfigs = systemConfigMapper.getConfigList(configType);
        //return systemConfigs;
        // 1. 从MySQL获取
        List<SystemConfig> configs = systemConfigMapper.getConfigList(configType);

        // 2. 更新Redis
        for (SystemConfig config : configs) {
            String redisKey = CONFIG_KEY_PREFIX + config.getConfigKey();
            redisTemplateForObject.opsForValue().set(redisKey, config, CONFIG_EXPIRE, CONFIG_EXPIRE_UNIT);
        }

        return configs;
    }

    /**
     * 修改系统配置
     * @param systemConfig
     */
    @Override
    public void putConfig(SystemConfig systemConfig) {
        // 1. 更新MySQL
        systemConfigMapper.putConfig(systemConfig);

        // 2. 更新Redis
        String redisKey = CONFIG_KEY_PREFIX + systemConfig.getConfigKey();
        redisTemplateForObject.opsForValue().set(redisKey, systemConfig, CONFIG_EXPIRE, CONFIG_EXPIRE_UNIT);
    }

    /**
     * 批量获取配置
     * @param keys
     * @return
     */
    @Override
    public List<SystemConfig> getByConfigKeys(List<String> keys) {
        //List<SystemConfig> systemConfigs = systemConfigMapper.getByConfigKeys(keys);
        //return systemConfigs;
        List<SystemConfig> result = new ArrayList<>();
        List<String> notFoundKeys = new ArrayList<>();

        // 1. 先从Redis获取
        for (String key : keys) {
            String redisKey = CONFIG_KEY_PREFIX + key;
            Object value = redisTemplateForObject.opsForValue().get(redisKey);
            if (value != null) {
                result.add((SystemConfig) value);
            } else {
                notFoundKeys.add(key);
            }
        }

        // 2. Redis没有的，从MySQL获取
        if (!notFoundKeys.isEmpty()) {
            List<SystemConfig> mysqlConfigs = systemConfigMapper.getByConfigKeys(notFoundKeys);
            // 3. 存入Redis
            for (SystemConfig config : mysqlConfigs) {
                String redisKey = CONFIG_KEY_PREFIX + config.getConfigKey();
                redisTemplateForObject.opsForValue().set(redisKey, config, CONFIG_EXPIRE, CONFIG_EXPIRE_UNIT);
            }
            result.addAll(mysqlConfigs);
        }

        return result;
    }
}
