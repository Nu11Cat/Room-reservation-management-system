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
        PageHelper.startPage(page, pageSize);
        List<User> list = userMapper.getAllUsers();
        Page<User> p = (Page<User>) list;

        PageBean pageBean = new PageBean(p.getTotal(), p.getResult());
        return pageBean;
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
