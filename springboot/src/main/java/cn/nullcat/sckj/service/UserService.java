package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.User;

public interface UserService {
    String getPasswordByUserName(String username);

    boolean exitUserName(String username);

    User getById(Integer userIdNow);

    void update(User user);

    void register(User user);

    Integer getUserIdByUsername(String username);

    void clearUserCache(Integer userId);

    PageBean getAllUsers(Integer page, Integer pageSize);

    void add(User user);

    void banOrUnseal(Integer id);

    Integer getStatusById(Integer userId);
}
