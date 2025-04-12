package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.Role;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.service.UserService;
import cn.nullcat.sckj.utils.JwtUtils;
import cn.nullcat.sckj.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户相关")
public class UserController {
    @Autowired
    private UserService userservice;
    @Autowired
    private TokenUtils tokenUtils;
    /**
     * 用户登录
     * @param user
     * @return
     */
    @Operation(summary ="用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        if(user.getUsername()==null || user.getPassword()==null) {
            return Result.error("用户名或密码不能为空");
        }
        if(!userservice.exitUserName(user.getUsername())) {
            return Result.error("该用户名不存在");
        }
        String password = userservice.getPasswordByUserName(user.getUsername());
        if(!password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        Integer userId = userservice.getUserIdByUsername(user.getUsername()); // 根据用户名获取用户 ID
        Integer status = userservice.getStatusById(userId);
        if(status == 0) { //根据id查询封禁状态
            tokenUtils.removeToken(userId);
            tokenUtils.removeUserInfo(userId);
            return Result.error("你的账号已被封禁，无法登录");

        }
        //生成令牌并下发
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",userId);
        claims.put("username",user.getUsername());
        claims.put("password",password);
        claims.put("roleId", user.getRoleId());  // 添加这一行
        User fullUser = userservice.getById(userId);
        claims.put("roleId", fullUser.getRoleId());
        String jwt = JwtUtils.generateJwt(claims);
        tokenUtils.saveToken(jwt, userId);
        tokenUtils.saveUserInfo(fullUser);
        return Result.success(jwt);
    }
    /**
     * 用户注册
     * @param user
     * @return
     */
    @Operation(summary ="用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if(user.getUsername()==null || user.getPassword()==null) {
            return Result.error("用户名或密码不能为空");
        }
        if(userservice.exitUserName(user.getUsername())){
            return Result.error("该用户名已存在");
        }
        userservice.register(user);
        return Result.success("注册成功");
    }
    /**
     * 退出登录
     * @return
     */
    @Operation(summary ="退出登录")
    @PostMapping("/logout")
    public Result logOut(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        tokenUtils.removeToken(userId);
        userservice.clearUserCache(userId);
        tokenUtils.removeUserInfo(userId);
        return Result.success("退出成功");
    }
    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    @Operation(summary ="获取当前登录用户信息")
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {
        Integer userIdNow = (Integer) request.getAttribute("userId");
        User user = userservice.getById(userIdNow);
        return Result.success(user);
    }

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @PutMapping("/info")
    @Operation(summary ="修改用户信息")
    public Result update(@RequestBody User user,HttpServletRequest request) {
        Integer userIdNow = (Integer) request.getAttribute("userId");
        user.setId(Long.valueOf(userIdNow));
        userservice.update(user);
        return Result.success("修改成功");
    }

    /**
     * 修改密码
     * @param user
     * @return
     */
    @PutMapping("/password")
    @Operation(summary ="修改密码")
    public Result updatePassword(@RequestBody User user,HttpServletRequest request) {
        Integer userIdNow = (Integer) request.getAttribute("userId");
        if(!userservice.getById(userIdNow).getPassword().equals(user.getOldPassword())) {
            return Result.success("旧密码错误");
        }
        user.setId(Long.valueOf(userIdNow));
        userservice.update(user);
        return Result.success("修改成功");
    }

    /**
     * 获取全部用户信息
     * @return
     */
    @GetMapping("/users")
    @RequirePermission("system:user")
    @Operation(summary ="获取全部用户信息")
    public Result getAllUsers(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageBean pageBean = userservice.getAllUsers(page,pageSize);
        return Result.success(pageBean);
    }

    /**
     * 查看指定用户
     * @param id
     * @return
     */
    @GetMapping("/getById")
    @RequirePermission("system:user:view")
    @Operation(summary ="查看指定用户")
    public Result getById(@RequestParam Integer id) {
        User user = userservice.getById(id);
        return Result.success(user);
    }

    /**
     * 添加用户
     * @param user
     * @return
     */
    @PostMapping("/add")
    @Operation(summary ="添加用户")
    @RequirePermission("system:user:add")
    public Result add(@RequestBody User user) {
        userservice.add(user);
        return Result.success("添加成功！");
    }

    /**
     * 编辑指定用户
     * @param user
     * @return
     */
    @PutMapping("/editById")
    @Operation(summary ="编辑指定用户")
    @RequirePermission("system:user:edit")
    public Result editByUser(@RequestBody User user) {
        userservice.update(user);
        return Result.success("修改成功");
    }

    /**
     * 封禁/解封用户
     * @param id
     * @return
     */
    @DeleteMapping("/ban/{id}")
    @Operation(summary ="封禁/解封用户")
    @RequirePermission("system:user:delete")
    public Result banById(@PathVariable Integer id) {
        userservice.banOrUnseal(id);
        return Result.success("封禁成功");
    }

    /**
     * 获取所有用户的简单列表（ID和用户名）
     * @return
     */
    @GetMapping("/list")
    @Operation(summary ="获取所有用户的简单列表（ID和用户名）")
    @RequirePermission("notification:send")
    public Result getUserList() {
        List<User> userList = userservice.getUserList();
        return Result.success(userList);
    }

    /**
     * 修改用户角色
     * @param userId 用户ID
     * @param roleIdMap 角色ID Map
     * @return 操作结果
     */
    @PutMapping("/{userId}/role")
    @Operation(summary ="修改用户角色")
    @RequirePermission("system:user:edit")
    public Result updateUserRole(@PathVariable Integer userId, @RequestBody Map<String, Long> roleIdMap) {
        Long roleId = roleIdMap.get("roleId");
        if (roleId == null) {
            return Result.error("角色ID不能为空");
        }
        userservice.updateUserRole(userId, roleId);
        return Result.success("用户角色修改成功");
    }
}
