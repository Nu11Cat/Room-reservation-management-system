package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.exception.BusinessException;
import cn.nullcat.sckj.mapper.PermissionMapper;
import cn.nullcat.sckj.mapper.RoleMapper;
import cn.nullcat.sckj.mapper.RolePermissionMapper;
import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.pojo.Permission;
import cn.nullcat.sckj.pojo.Role;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 获取所有角色
     * @return 角色列表
     */
    @Override
    public List<Role> getAllRoles() {
        return roleMapper.getAllRoles();
    }

    /**
     * 根据ID获取角色
     * @param id 角色ID
     * @return 角色信息
     */
    @Override
    public Role getById(Long id) {
        return roleMapper.getById(id);
    }

    /**
     * 获取角色拥有的权限
     * @param roleId 角色ID
     * @return 权限代码列表
     */
    @Override
    public List<String> getRolePermissions(Long roleId) {
        // 确保角色存在
        Role role = roleMapper.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        return rolePermissionMapper.getPermissionsByRoleId(roleId);
    }

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    @Override
    @LogOperation(module = "权限管理", operation = "更新用户角色", description = "更新用户角色")
    public boolean updateUserRole(Long userId, Long roleId) {
        // 确保用户存在
        User user = userMapper.getById(Math.toIntExact(userId));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 确保角色存在
        Role role = roleMapper.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 更新用户角色
        return userMapper.updateUserRole(userId, roleId) > 0;
    }
    
    /**
     * 创建新角色
     * @param role 角色信息
     * @return 角色ID
     */
    @Override
    @LogOperation(module = "权限管理", operation = "创建角色", description = "创建新角色")
    @Transactional
    public Long addRole(Role role) {
        // 检查角色名和代码是否已存在
        if (roleMapper.checkCodeExists(role.getRoleCode()) > 0) {
            throw new BusinessException("角色编码已存在");
        }
        if (roleMapper.checkNameExists(role.getRoleName()) > 0) {
            throw new BusinessException("角色名称已存在");
        }
        
        int result = roleMapper.addRole(role);
        if (result > 0) {
            return role.getId();
        } else {
            throw new BusinessException("创建角色失败");
        }
    }
    
    /**
     * 更新角色信息
     * @param role 角色信息
     * @return 是否成功
     */
    @Override
    @LogOperation(module = "权限管理", operation = "更新角色", description = "更新角色信息")
    @Transactional
    public boolean updateRole(Role role) {
        // 确保角色存在
        Role existingRole = roleMapper.getById(role.getId());
        if (existingRole == null) {
            throw new BusinessException("角色不存在");
        }

        if (!existingRole.getRoleName().equals(role.getRoleName()) && roleMapper.checkNameExists(role.getRoleName()) > 0) {
            throw new BusinessException("角色名称已被其他角色使用");
        }
        
        return roleMapper.updateRole(role) > 0;
    }
    
    /**
     * 删除角色
     * @param id 角色ID
     * @return 是否成功
     */
    @Override
    @LogOperation(module = "权限管理", operation = "删除角色", description = "删除角色")
    @Transactional
    public boolean deleteRole(Long id) {
        // 检查角色是否被用户使用
        List<Long> userIds = userMapper.getUserIdsByRoleId(id);
        if (userIds != null && !userIds.isEmpty()) {
            throw new BusinessException("该角色已被用户使用，无法删除");
        }
        
        // 删除角色的所有权限关联
        rolePermissionMapper.removeAllRolePermissions(id);
        
        // 删除角色
        return roleMapper.deleteRole(id) > 0;
    }
    
    /**
     * 设置角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    @Override
    @LogOperation(module = "权限管理", operation = "设置角色权限", description = "批量设置角色权限")
    @Transactional
    public boolean setRolePermissions(Long roleId, List<Long> permissionIds) {
        // 确保角色存在
        if (roleMapper.getById(roleId) == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 先移除所有现有权限
        rolePermissionMapper.removeAllRolePermissions(roleId);
        
        // 然后添加新的权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                rolePermissionMapper.addRolePermission(roleId, permissionId);
            }
        }
        
        return true;
    }
    
    /**
     * 为角色添加权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    @Override
    @LogOperation(module = "权限管理", operation = "添加角色权限", description = "为角色添加单个权限")
    public boolean addRolePermission(Long roleId, Long permissionId) {
        // 确保角色存在
        if (roleMapper.getById(roleId) == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 检查权限关联是否已存在
        if (rolePermissionMapper.checkRolePermissionExists(roleId, permissionId) > 0) {
            return true; // 已存在则视为添加成功
        }
        
        return rolePermissionMapper.addRolePermission(roleId, permissionId) > 0;
    }
    
    /**
     * 移除角色权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    @Override
    @LogOperation(module = "权限管理", operation = "移除角色权限", description = "移除角色的单个权限")
    public boolean removeRolePermission(Long roleId, Long permissionId) {
        // 确保角色存在
        if (roleMapper.getById(roleId) == null) {
            throw new BusinessException("角色不存在");
        }
        
        return rolePermissionMapper.removeRolePermission(roleId, permissionId) > 0;
    }
    
    /**
     * 获取角色权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        // 确保角色存在
        if (roleMapper.getById(roleId) == null) {
            throw new BusinessException("角色不存在");
        }
        
        return rolePermissionMapper.getPermissionIdsByRoleId(roleId);
    }

    /**
     * 设置角色权限（通过权限编码）
     * @param roleId 角色ID
     * @param permissionCodes 权限编码列表
     * @return 是否成功
     */
    @Override
    @LogOperation(module = "权限管理", operation = "设置角色权限", description = "通过权限编码批量设置角色权限")
    @Transactional
    public boolean setRolePermissionsByCodes(Long roleId, List<String> permissionCodes) {
        // 确保角色存在
        if (roleMapper.getById(roleId) == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 先移除所有现有权限
        rolePermissionMapper.removeAllRolePermissions(roleId);
        
        // 如果没有权限编码，直接返回成功
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return true;
        }
        
        // 批量获取权限ID
        List<Long> permissionIds = permissionMapper.getPermissionIdsByCodes(permissionCodes);
        log.info("根据权限编码 {} 获取到 {} 个权限ID: {}", permissionCodes, permissionIds.size(), permissionIds);
        
        // 批量添加角色权限
        for (Long permissionId : permissionIds) {
            try {
                rolePermissionMapper.addRolePermission(roleId, permissionId);
            } catch (Exception e) {
                log.warn("添加角色权限失败: roleId={}, permissionId={}, error={}", roleId, permissionId, e.getMessage());
            }
        }
        
        return true;
    }

    /**
     * 获取所有权限编码
     * @return 权限编码列表
     */
    @Override
    public List<String> getAllPermissionCodes() {
        List<Permission> permissions = permissionMapper.getAllPermissions();
        return permissions.stream()
                .map(Permission::getCode)
                .collect(java.util.stream.Collectors.toList());
    }
} 