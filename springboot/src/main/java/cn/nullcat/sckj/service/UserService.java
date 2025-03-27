package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.User;

public interface UserService {
    String getPasswordByUserName(String username);

    boolean exitUserName(String username);

    User getById(Integer userIdNow);

    void update(User user);

    void register(User user);

    Integer getUserIdByUsername(String username);
}
