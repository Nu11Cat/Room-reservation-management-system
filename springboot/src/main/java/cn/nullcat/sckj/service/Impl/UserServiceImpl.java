package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.service.UserService;
import cn.nullcat.sckj.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

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
}
