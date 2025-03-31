package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.service.UserService;
import cn.nullcat.sckj.utils.JwtUtils;
import cn.nullcat.sckj.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userservice;
    @Autowired
    private TokenUtils tokenUtils;
    // 查询用户列表
    // TODO
    @GetMapping
    @RequirePermission("system:user:view")
    public Result listUsers() { return  Result.success();}
    // 添加用户
    @PostMapping
    @RequirePermission("system:user:add")
    public Result addUser(@RequestBody User user) { return  Result.success(); }
    // 删除用户
    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    public Result deleteUser(@PathVariable Long id) { return  Result.success();  }
    // 获取用户详情
    @GetMapping("/{id}")
    @RequirePermission("system:user:view")
    public Result getUserById(@PathVariable Long id) { return  Result.success();  }
    /**
     * 用户登录
     * @param user
     * @return
     */
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
    @RequirePermission("system:user:edit")
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
    public Result updatePassword(@RequestBody User user,HttpServletRequest request) {
        Integer userIdNow = (Integer) request.getAttribute("userId");
        if(!userservice.getById(userIdNow).getPassword().equals(user.getOldPassword())) {
            return Result.success("旧密码错误");
        }
        user.setId(Long.valueOf(userIdNow));
        userservice.update(user);
        return Result.success("修改成功");
    }
}
