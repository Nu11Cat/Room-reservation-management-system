package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper {
    /**
     * 获取所有权限
     * @return 权限列表
     */
    @Select("SELECT * FROM permission WHERE is_deleted = 0")
    List<Permission> getAllPermissions();
    
    /**
     * 根据ID获取权限
     * @param id 权限ID
     * @return 权限信息
     */
    @Select("SELECT * FROM permission WHERE id = #{id} AND is_deleted = 0")
    Permission getById(Long id);
    
    /**
     * 根据代码获取权限
     * @param code 权限代码
     * @return 权限信息
     */
    @Select("SELECT * FROM permission WHERE code = #{code} AND is_deleted = 0")
    Permission getByCode(String code);
    
    /**
     * 根据权限代码批量获取权限
     * @param codes 权限代码列表
     * @return 权限ID列表
     */
    @Select("<script>" +
            "SELECT id FROM permission WHERE code IN " +
            "<foreach collection='list' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            " AND is_deleted = 0" +
            "</script>")
    List<Long> getPermissionIdsByCodes(List<String> codes);
} 