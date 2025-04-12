package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.service.UserService;
import cn.nullcat.sckj.utils.TokenUtils;
import cn.nullcat.sckj.websocket.NotificationWebSocket;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private TokenUtils tokenUtils;
    /**
     * 校验密码：根据用户名获取密码
     * @param username
     * @return
     */
    @Override
    public String getPasswordByUserName(String username) {
        String password = userMapper.getPasswordByUserName(username);
        return password;
    }

    /**
     * 校验用户名是否存在
     * @param username
     * @return
     */
    @Override
    public boolean exitUserName(String username) {
        User user = userMapper.getByUserName(username);
        return user!=null;
    }

    /**
     * 获取个人信息：根据id获取信息
     * @param userIdNow
     * @return
     */
    @Override
    public User getById(Integer userIdNow) {
        // 1. 先从Redis获取
        User user = tokenUtils.getUserInfo(userIdNow);
        if (user != null) {
            return user;
        }

        // 2. Redis没有，从数据库获取
        user = userMapper.getById(userIdNow);
        if (user != null) {
            // 3. 保存到Redis
            tokenUtils.saveUserInfo(user);
        }
         //
        return user;
    }

    /**
     * 更新个人信息/密码
     * @param user
     */
    @Override
    @LogOperation(module = "用户管理", operation = "更新个人信息/密码", description = "更新个人信息/密码")
    public void update(User user) {
        if(user.getPassword()==null) {
            userMapper.updateInfo(user);
        }else{
            userMapper.updatePassword(user);
        }
        // 2. 更新Redis
        tokenUtils.updateUserInfo(user);
    }

    /**
     * 注册用户
     * @param user
     */
    @Override
    @LogOperation(module = "用户管理", operation = "注册用户", description = "注册用户")
    public void register(User user) {
        user.setAvatar("");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setStatus(1);
        user.setRoleId(2L);
        user.setIsDeleted(false);
        // 设置默认信誉分数和身份
        user.setCreditScore(100);  // 默认信誉分为100
        user.setIdentity("student");  // 默认身份为学生
        userMapper.register(user);
    }

    /**
     *
     * @param username
     * @return
     */
    @Override
    public Integer getUserIdByUsername(String username) {
        return userMapper.getUserIdByUsername(username);
    }

    /**
     * 清除redis用户token
     * @param userId
     */
    @Override
    public void clearUserCache(Integer userId) {
        String key = "user:" + userId;
        redisTemplate.delete(key);
    }

    /**
     * 获取全部用户信息
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    @LogOperation(module = "用户管理", operation = "获取全部用户信息", description = "获取全部用户信息")
    public PageBean getAllUsers(Integer page, Integer pageSize) {
        //设置分页参数
        PageHelper.startPage(page, pageSize);
        //执行查询
        List<User> userList = userMapper.getAllUsers();
        //获取分页后数据
        Page<User> p = (Page<User>) userList;
        //封装pageBean
        PageBean pageBean = new PageBean(p.getTotal(), p.getResult());
        return pageBean;
    }

    /**
     * 获取用户列表（包含角色信息）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 用户分页数据
     */
    @Override
    public PageBean getAllUsersWithRole(Integer page, Integer pageSize) {
        // 复用现有的getAllUsers方法，因为角色ID已包含在User对象中
        return getAllUsers(page, pageSize);
    }

    /**
     * 获取所有用户的简单列表
     * @return 用户简单列表
     */
    @Override
    public List<User> getUserSimpleList() {
        return userMapper.getUserSimpleList();
    }

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    @Override
    @LogOperation(module = "用户管理", operation = "更新用户角色", description = "更新用户角色")
    public void updateUserRole(Integer userId, Long roleId) {
        // 转换Integer类型的userId为Long类型
        Long userIdLong = Long.valueOf(userId);
        
        // 更新用户角色
        userMapper.updateUserRole(userIdLong, roleId);
        
        // 获取更新后的用户信息
        User user = userMapper.getById(userId);
        if (user != null) {
            // 清除Redis缓存，确保用户下次访问时获取最新的权限
            clearUserCache(userId);
            // 更新Redis中的用户信息
            user.setRoleId(roleId); // 确保Redis中的用户对象也更新了角色ID
            tokenUtils.updateUserInfo(user);
        }
    }

    /**
     * 添加用户
     * @param user
     */
    @Override
    @LogOperation(module = "用户管理", operation = "添加用户", description = "添加用户")
    public void add(User user) {
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setStatus(1);
        user.setIsDeleted(false);
        user.setRoleId(2L);
        // 如果未设置信誉分和身份，则设置默认值
        if (user.getCreditScore() == null) {
            user.setCreditScore(100);  // 默认信誉分为100
        }
        if (user.getIdentity() == null) {
            user.setIdentity("student");  // 默认身份为学生
        }
        userMapper.register(user);
    }

    /**
     * 封禁/解封用户
     * @param id
     */
    @Override
    @LogOperation(module = "用户管理", operation = "封禁/解封用户", description = "封禁/解封用户")
    public void banOrUnseal(Integer id) {
        if (userMapper.getById(id).getStatus() == 1) {
            userMapper.banById(id);
            // 添加WebSocket通知
            NotificationWebSocket.sendBanNotification(id, "您的账号已被管理员禁用");
            // 清除用户Redis缓存和Token
            tokenUtils.removeToken(id);
            tokenUtils.removeUserInfo(id);
        } else {
            userMapper.unsealById(id);
        }
    }

    @Override
    public Integer getStatusById(Integer userId) {
        return userMapper.getStatusById(userId);
    }

    /**
     * 获取所有用户的简单列表（ID和用户名）
     * @return 用户列表
     */
    @Override
    public List<User> getUserList() {
        return userMapper.getUserSimpleList();
    }
}
