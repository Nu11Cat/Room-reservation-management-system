package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
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
        User user = userMapper.getById(userIdNow);
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
}
