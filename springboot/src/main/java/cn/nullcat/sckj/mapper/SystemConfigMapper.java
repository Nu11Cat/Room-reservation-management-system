package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SystemConfigMapper {
    /**
     * 获取系统配置
     * @param key
     * @return
     */
    @Select("select * from system_config where config_key = #{key}")
    SystemConfig getByConfigKey(String key);

    /**
     * 获取所有配置
     * @return 所有系统配置
     */
    @Select("select * from system_config where is_deleted = 0")
    List<SystemConfig> getAllConfigs();

    /**
     * 获取配置列表
     * @param configType
     * @return
     */
    List<SystemConfig> getConfigList(String configType);

    /**
     *  修改系统配置
     * @param systemConfig
     */
    @Update("update system_config set" +
            " config_value = #{configValue}," +
            " config_type = #{configType}," +
            " description = #{description}" +
            " where config_key = #{configKey}")
    void putConfig(SystemConfig systemConfig);

    /**
     * 批量获取配置
     * @param keys
     * @return
     */
    List<SystemConfig> getByConfigKeys(List<String> keys);
}
