package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.mapper.PermissionMapper;
import cn.nullcat.sckj.pojo.Permission;
import cn.nullcat.sckj.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/permissions")
public class PermissionController {
    @Autowired
    private PermissionMapper permissionMapper;
    
    /**
     * 获取所有权限
     * @return 权限列表
     */
    @GetMapping
    //@RequirePermission("system:permission:view")
    public Result getAllPermissions() {
        List<Permission> permissions = permissionMapper.getAllPermissions();
        return Result.success(permissions);
    }
    
    /**
     * 获取权限详情
     * @param id 权限ID
     * @return 权限信息
     */
    @GetMapping("/{id}")
    //@RequirePermission("system:permission:view")
    public Result getPermissionById(@PathVariable Long id) {
        Permission permission = permissionMapper.getById(id);
        if (permission == null) {
            return Result.error("权限不存在");
        }
        return Result.success(permission);
    }
} 