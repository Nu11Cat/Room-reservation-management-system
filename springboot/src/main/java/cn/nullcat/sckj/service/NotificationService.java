package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.PageBean;

public interface NotificationService {
    PageBean getMyNotifications(Integer page, Integer pageSize, Integer type, Integer isRead, Integer userId);

    void readNotifications(Integer id);

    Integer getUnreadCount(Integer userId);
}
