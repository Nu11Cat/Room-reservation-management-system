package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {
    /**
     * 获取所有角色
     * @return 角色列表
     */
    @Select("SELECT * FROM role WHERE is_deleted = 0")
    List<Role> getAllRoles();
    
    /**
     * 根据ID获取角色
     * @param id 角色ID
     * @return 角色信息
     */
    @Select("SELECT * FROM role WHERE id = #{id} AND is_deleted = 0")
    Role getById(Long id);
    
    /**
     * 根据代码获取角色
     * @param code 角色代码
     * @return 角色信息
     */
    @Select("SELECT * FROM role WHERE role_code = #{code} AND is_deleted = 0")
    Role getByCode(String code);
    
    /**
     * 添加角色
     * @param role 角色信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO role(role_code, role_name, description, create_time, update_time, is_deleted) " +
            "VALUES(#{roleCode}, #{roleName}, #{description}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addRole(Role role);
    
    /**
     * 更新角色信息
     * @param role 角色信息
     * @return 影响的行数
     */
    @Update("UPDATE role SET role_name = #{roleName}, role_code = #{roleCode}, description = #{description}, " +
            "update_time = NOW() WHERE id = #{id}")
    int updateRole(Role role);
    
    /**
     * 删除角色（逻辑删除）
     * @param id 角色ID
     * @return 影响的行数
     */
    @Update("UPDATE role SET is_deleted = 1, update_time = NOW() WHERE id = #{id}")
    int deleteRole(Long id);
    
    /**
     * 检查角色编码是否已存在
     * @param code 角色编码
     * @return 记录数
     */
    @Select("SELECT COUNT(*) FROM role WHERE role_code = #{code} AND is_deleted = 0")
    int checkCodeExists(String code);
    
    /**
     * 检查角色名是否已存在
     * @param name 角色名
     * @return 记录数
     */
    @Select("SELECT COUNT(*) FROM role WHERE role_name = #{name} AND is_deleted = 0")
    int checkNameExists(String name);
} 