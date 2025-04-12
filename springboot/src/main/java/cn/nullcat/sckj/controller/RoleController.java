package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.mapper.PermissionMapper;
import cn.nullcat.sckj.pojo.Permission;
import cn.nullcat.sckj.pojo.Role;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "角色管理")
@RequestMapping("/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 获取所有角色
     * @return 角色列表
     */
    @GetMapping
    @Operation(summary ="获取所有角色")

    @RequirePermission("system:role:view")
    public Result getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 获取角色详情
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    @RequirePermission("system:role:view")
    @Operation(summary ="获取角色详情")
    public Result getRoleById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    /**
     * 获取角色权限
     * @param id 角色ID
     * @return 权限列表
     */
    @GetMapping("/{id}/permissions")
    @Operation(summary ="获取角色权限")
    @RequirePermission("system:role:view")
    public Result getRolePermissions(@PathVariable Long id) {
        List<String> permissions = roleService.getRolePermissions(id);
        return Result.success(permissions);
    }
    
    /**
     * 获取角色权限ID列表
     * @param id 角色ID
     * @return 权限ID列表
     */
    @GetMapping("/{id}/permissionIds")
    @Operation(summary ="获取角色权限ID列表")
    @RequirePermission("system:role:view")
    public Result getRolePermissionIds(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getRolePermissionIds(id);
        return Result.success(permissionIds);
    }

    /**
     * 创建角色
     * @param role 角色信息
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary ="创建角色")
    @RequirePermission("system:role:add")
    public Result addRole(@RequestBody Role role) {
        try {
            Long roleId = roleService.addRole(role);
            return Result.success(roleId, "角色创建成功");
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新角色
     * @param id 角色ID
     * @param role 角色信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary ="更新角色")
    @RequirePermission("system:role:edit")
    public Result updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id); // 确保ID一致
        try {
            boolean success = roleService.updateRole(role);
            if (success) {
                return Result.success("角色更新成功");
            } else {
                return Result.error("角色更新失败");
            }
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除角色
     * @param id 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary ="删除角色")
    @RequirePermission("system:role:delete")
    public Result deleteRole(@PathVariable Long id) {
        try {
            boolean success = roleService.deleteRole(id);
            if (success) {
                return Result.success("角色删除成功");
            } else {
                return Result.error("角色删除失败");
            }
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 设置角色权限
     * @param id 角色ID
     * @param permissionCodesMap 权限编码列表
     * @return 操作结果
     */
    @PutMapping("/{id}/permissions")
    @Operation(summary ="设置角色权限")
    @RequirePermission("system:role:edit")
    public Result setRolePermissions(@PathVariable Long id, @RequestBody Map<String, List<String>> permissionCodesMap) {
        List<String> permissionCodes = permissionCodesMap.get("permissionCodes");
        if (permissionCodes == null) {
            return Result.error("权限编码列表不能为空");
        }
        
        try {
            boolean success = roleService.setRolePermissionsByCodes(id, permissionCodes);
            if (success) {
                return Result.success("角色权限设置成功");
            } else {
                return Result.error("角色权限设置失败");
            }
        } catch (Exception e) {
            log.error("设置角色权限失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 为角色添加权限
     * @param roleId 角色ID
     * @param permissionMap 权限编码
     * @return 操作结果
     */
    @PostMapping("/{roleId}/permissions")
    @RequirePermission("system:role:edit")
    @Operation(summary ="为角色添加权限")
    public Result addRolePermission(@PathVariable Long roleId, @RequestBody Map<String, String> permissionMap) {
        String permissionCode = permissionMap.get("permissionCode");
        if (permissionCode == null) {
            return Result.error("权限编码不能为空");
        }
        
        try {
            // 通过权限编码获取权限对象
            Permission permission = permissionMapper.getByCode(permissionCode);
            if (permission == null) {
                return Result.error("权限不存在: " + permissionCode);
            }
            
            boolean success = roleService.addRolePermission(roleId, permission.getId());
            if (success) {
                return Result.success("添加角色权限成功");
            } else {
                return Result.error("添加角色权限失败");
            }
        } catch (Exception e) {
            log.error("添加角色权限失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 移除角色权限
     * @param roleId 角色ID
     * @param permissionCode 权限编码
     * @return 操作结果
     */
    @DeleteMapping("/{roleId}/permissions/code/{permissionCode}")
    @Operation(summary ="移除角色权限")
    @RequirePermission("system:role:edit")
    public Result removeRolePermissionByCode(@PathVariable Long roleId, @PathVariable String permissionCode) {
        try {
            // 通过权限编码获取权限对象
            Permission permission = permissionMapper.getByCode(permissionCode);
            if (permission == null) {
                return Result.error("权限不存在: " + permissionCode);
            }
            
            boolean success = roleService.removeRolePermission(roleId, permission.getId());
            if (success) {
                return Result.success("移除角色权限成功");
            } else {
                return Result.error("移除角色权限失败");
            }
        } catch (Exception e) {
            log.error("移除角色权限失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 移除角色权限 (兼容旧方法，通过ID移除)
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 操作结果
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary ="通过ID移除角色权限")
    @RequirePermission("system:role:edit")
    public Result removeRolePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        try {
            boolean success = roleService.removeRolePermission(roleId, permissionId);
            if (success) {
                return Result.success("移除角色权限成功");
            } else {
                return Result.error("移除角色权限失败");
            }
        } catch (Exception e) {
            log.error("移除角色权限失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param role 角色ID
     * @return 操作结果
     */
    @PutMapping("/user/{userId}/role")
    @Operation(summary ="更新用户角色")
    @RequirePermission("system:user:edit")
    public Result updateUserRole(@PathVariable Long userId, @RequestBody Role role) {
        boolean success = roleService.updateUserRole(userId, role.getId());
        if (success) {
            return Result.success("用户角色更新成功");
        } else {
            return Result.error("用户角色更新失败");
        }
    }

    /**
     * 获取所有权限编码
     * @return 权限编码列表
     */
    @GetMapping("/permissions/codes")
    @Operation(summary ="获取所有权限编码")
    @RequirePermission("system:role:view")
    public Result getAllPermissionCodes() {
        try {
            List<String> permissionCodes = roleService.getAllPermissionCodes();
            return Result.success(permissionCodes);
        } catch (Exception e) {
            log.error("获取所有权限编码失败", e);
            return Result.error(e.getMessage());
        }
    }
} 