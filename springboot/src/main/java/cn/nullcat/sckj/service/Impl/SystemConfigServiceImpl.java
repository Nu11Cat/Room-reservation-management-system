package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.mapper.SystemConfigMapper;
import cn.nullcat.sckj.pojo.SystemConfig;
import cn.nullcat.sckj.service.SystemConfigService;
import cn.nullcat.sckj.websocket.NotificationWebSocket;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplateForObject;

    // Redis配置相关常量
    private static final String CONFIG_HASH_KEY = "system:config:hash";  // 存储所有配置的Hash结构
    private static final String CONFIG_KEY_PREFIX = "system:config:";     // 保留用于单个配置的key前缀
    private static final Long CONFIG_EXPIRE = 24L;                        // 24小时
    private static final TimeUnit CONFIG_EXPIRE_UNIT = TimeUnit.HOURS;

    /**
     * 获取系统配置
     * @param key 配置键名
     * @return 配置对象
     */
    @Override
    public SystemConfig getByConfigKey(String key) {
        // 1. 先从Redis Hash中获取
        Object value = redisTemplateForObject.opsForHash().get(CONFIG_HASH_KEY, key);
        if (value != null) {
            log.debug("从Redis Hash获取配置[{}]", key);
            return (SystemConfig) value;
        }

        // 2. Redis没有，尝试单个键值对方式获取（兼容老数据）
        String redisKey = CONFIG_KEY_PREFIX + key;
        value = redisTemplateForObject.opsForValue().get(redisKey);
        if (value != null) {
            log.debug("从Redis Key获取配置[{}]", key);
            SystemConfig config = (SystemConfig) value;
            // 同步到Hash结构中
            redisTemplateForObject.opsForHash().put(CONFIG_HASH_KEY, key, config);
            return config;
        }

        // 3. Redis都没有，从MySQL获取
        log.debug("从MySQL获取配置[{}]", key);
        SystemConfig systemConfig = systemConfigMapper.getByConfigKey(key);
        if (systemConfig != null) {
            // 4. 存入Redis
            redisTemplateForObject.opsForHash().put(CONFIG_HASH_KEY, key, systemConfig);
        }
        return systemConfig;
    }

    /**
     * 获取所有配置（不分类型）
     * @return 所有配置的集合
     */
    @Override
    public Map<String, SystemConfig> getAllConfigs() {
        // 1. 尝试从Redis获取所有配置
        Map<Object, Object> entries = redisTemplateForObject.opsForHash().entries(CONFIG_HASH_KEY);
        
        if (entries != null && !entries.isEmpty()) {
            log.debug("从Redis Hash获取所有配置，共{}条", entries.size());
            Map<String, SystemConfig> result = new HashMap<>(entries.size());
            
            // 修复JSON对象转换问题
            entries.forEach((k, v) -> {
                SystemConfig config;
                try {
                    if (v instanceof com.alibaba.fastjson.JSONObject) {
                        // 将JSONObject转换为SystemConfig
                        config = ((com.alibaba.fastjson.JSONObject) v).toJavaObject(SystemConfig.class);
                        log.debug("转换JSONObject到SystemConfig: {}", k.toString());
                    } else if (v instanceof SystemConfig) {
                        // 直接使用
                        config = (SystemConfig) v;
                    } else {
                        // 未知类型，尝试JSON转换
                        String jsonStr = JSON.toJSONString(v);
                        config = JSON.parseObject(jsonStr, SystemConfig.class);
                        log.warn("未知类型转换: {} ({})", k.toString(), v.getClass().getName());
                    }
                    result.put(k.toString(), config);
                } catch (Exception e) {
                    log.error("配置转换失败，键: {}, 错误: {}", k.toString(), e.getMessage());
                }
            });
            
            return result;
        }
        
        // 2. Redis没有，从数据库加载所有配置
        log.debug("从MySQL获取所有配置");
        List<SystemConfig> allConfigs = systemConfigMapper.getAllConfigs();
        Map<String, SystemConfig> result = new HashMap<>(allConfigs.size());
        
        // 3. 填充结果并同时更新Redis
        for (SystemConfig config : allConfigs) {
            String key = config.getConfigKey();
            result.put(key, config);
            redisTemplateForObject.opsForHash().put(CONFIG_HASH_KEY, key, config);
        }
        
        return result;
    }

    /**
     * 获取配置列表
     * @param configType 配置类型
     * @return 特定类型的配置列表
     */
    @Override
    public List<SystemConfig> getConfigList(String configType) {
        // 1. 获取所有配置
        Map<String, SystemConfig> allConfigs = getAllConfigs();
        
        // 2. 过滤特定类型的配置
        if (configType != null && !configType.isEmpty()) {
            return allConfigs.values().stream()
                    .filter(config -> configType.equals(config.getConfigType()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>(allConfigs.values());
    }

    /**
     * 修改系统配置
     * @param systemConfig 要修改的配置
     */
    @Override
    @LogOperation(module = "系统管理", operation = "修改系统配置", description = "修改系统配置")
    public void putConfig(SystemConfig systemConfig) {
        String key = systemConfig.getConfigKey();
        
        // 1. 更新MySQL
        systemConfigMapper.putConfig(systemConfig);
        log.info("更新MySQL配置: {} = {}", key, systemConfig.getConfigValue());

        // 2. 更新Redis Hash结构
        redisTemplateForObject.opsForHash().put(CONFIG_HASH_KEY, key, systemConfig);
        
        // 3. 删除旧的单个键值对（兼容旧代码）
        String redisKey = CONFIG_KEY_PREFIX + key;
        redisTemplateForObject.delete(redisKey);
        
        // 4. 发布配置变更事件
        publishConfigChangeEvent(key, systemConfig.getConfigValue());
        
        log.info("配置[{}]已更新，新值: {}", key, systemConfig.getConfigValue());
    }

    /**
     * 批量获取配置
     * @param keys 配置键名列表
     * @return 配置对象列表
     */
    @Override
    public List<SystemConfig> getByConfigKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 1. 从Redis Hash批量获取
        List<Object> values = redisTemplateForObject.opsForHash().multiGet(CONFIG_HASH_KEY, 
                keys.stream().map(Object::toString).collect(Collectors.toList()));
        
        // 检查是否所有值都在Redis中
        boolean allFound = true;
        List<SystemConfig> result = new ArrayList<>(keys.size());
        List<String> notFoundKeys = new ArrayList<>();
        
        for (int i = 0; i < keys.size(); i++) {
            Object value = i < values.size() ? values.get(i) : null;
            if (value != null) {
                // 修复类型转换问题
                try {
                    SystemConfig config;
                    if (value instanceof com.alibaba.fastjson.JSONObject) {
                        // JSON对象转换为SystemConfig
                        config = ((com.alibaba.fastjson.JSONObject) value).toJavaObject(SystemConfig.class);
                    } else if (value instanceof SystemConfig) {
                        // 直接使用
                        config = (SystemConfig) value;
                    } else {
                        // 其他类型尝试JSON转换
                        String jsonStr = JSON.toJSONString(value);
                        config = JSON.parseObject(jsonStr, SystemConfig.class);
                        log.warn("类型转换: {} ({})", keys.get(i), value.getClass().getName());
                    }
                    result.add(config);
                } catch (Exception e) {
                    log.error("配置转换失败，键: {}, 错误: {}", keys.get(i), e.getMessage());
                    notFoundKeys.add(keys.get(i));
                    allFound = false;
                }
            } else {
                notFoundKeys.add(keys.get(i));
                allFound = false;
            }
        }
        
        // 2. Redis中未找到的，从MySQL获取
        if (!allFound) {
            List<SystemConfig> mysqlConfigs = systemConfigMapper.getByConfigKeys(notFoundKeys);
            
            // 3. 更新Redis并添加到结果集
            for (SystemConfig config : mysqlConfigs) {
                redisTemplateForObject.opsForHash().put(CONFIG_HASH_KEY, config.getConfigKey(), config);
                result.add(config);
            }
        }
        
        return result;
    }
    
    /**
     * 记录配置变更日志
     * @param key 变更的配置键
     * @param value 新的配置值
     */
    private void publishConfigChangeEvent(String key, String value) {
        try {
            // 仅记录配置变更日志，不再通过WebSocket广播
            log.info("系统配置已更新: {} = {}", key, value);
            log.info("注意：此配置变更将在系统重启后生效");
        } catch (Exception e) {
            log.error("记录配置变更日志失败", e);
        }
    }
}
