package cn.nullcat.sckj.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RolePermissionMapper {
    @Select("SELECT COUNT(*) FROM role_permission rp " +
            "WHERE rp.role_id = #{roleId} " +
            "AND rp.permission_id = (SELECT id FROM permission WHERE code = #{permissionCode}) " +
            "AND rp.is_deleted = 0")
    boolean hasPermission(Long roleId, String permissionCode);

    /**
     * 获取指定角色的所有权限代码
     * @param roleId 角色ID
     * @return 权限代码列表
     */
    @Select("SELECT p.code FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.is_deleted = 0")
    List<String> getPermissionsByRoleId(Long roleId);
    
    /**
     * 获取指定角色的所有权限ID
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Select("SELECT permission_id FROM role_permission WHERE role_id = #{roleId} AND is_deleted = 0")
    List<Long> getPermissionIdsByRoleId(Long roleId);
    
    /**
     * 为角色添加权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 影响的行数
     */
    @Insert("INSERT INTO role_permission(role_id, permission_id, create_time, update_time, is_deleted) " +
            "VALUES(#{roleId}, #{permissionId}, NOW(), NOW(), 0)")
    int addRolePermission(Long roleId, Long permissionId);
    
    /**
     * 移除角色权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 影响的行数
     */
    @Update("UPDATE role_permission SET is_deleted = 1, update_time = NOW() " +
            "WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int removeRolePermission(Long roleId, Long permissionId);
    
    /**
     * 删除角色的所有权限（逻辑删除）
     * @param roleId 角色ID
     * @return 影响的行数
     */
    @Update("UPDATE role_permission SET is_deleted = 1, update_time = NOW() " +
            "WHERE role_id = #{roleId}")
    int removeAllRolePermissions(Long roleId);
    
    /**
     * 检查角色权限关系是否已存在
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 记录数
     */
    @Select("SELECT COUNT(*) FROM role_permission " +
            "WHERE role_id = #{roleId} AND permission_id = #{permissionId} AND is_deleted = 0")
    int checkRolePermissionExists(Long roleId, Long permissionId);
}
