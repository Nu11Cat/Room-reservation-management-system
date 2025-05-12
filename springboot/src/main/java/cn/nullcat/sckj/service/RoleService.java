package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.Role;

import java.util.List;

public interface RoleService {
    /**
     * 获取所有角色
     * @return 角色列表
     */
    List<Role> getAllRoles();
    
    /**
     * 根据ID获取角色
     * @param id 角色ID
     * @return 角色信息
     */
    Role getById(Long id);
    
    /**
     * 获取角色拥有的权限
     * @param roleId 角色ID
     * @return 权限代码列表
     */
    List<String> getRolePermissions(Long roleId);
    
    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean updateUserRole(Long userId, Long roleId);
    
    /**
     * 创建新角色
     * @param role 角色信息
     * @return 角色ID
     */
    Long addRole(Role role);
    
    /**
     * 更新角色信息
     * @param role 角色信息
     * @return 是否成功
     */
    boolean updateRole(Role role);
    
    /**
     * 删除角色
     * @param id 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long id);
    
    /**
     * 设置角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean setRolePermissions(Long roleId, List<Long> permissionIds);
    
    /**
     * 根据权限编码设置角色权限
     * @param roleId 角色ID
     * @param permissionCodes 权限编码列表
     * @return 是否成功
     */
    boolean setRolePermissionsByCodes(Long roleId, List<String> permissionCodes);
    
    /**
     * 为角色添加权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean addRolePermission(Long roleId, Long permissionId);
    
    /**
     * 移除角色权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean removeRolePermission(Long roleId, Long permissionId);
    
    /**
     * 获取角色权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);
    
    /**
     * 获取所有权限编码
     * @return 权限编码列表
     */
    List<String> getAllPermissionCodes();
} 