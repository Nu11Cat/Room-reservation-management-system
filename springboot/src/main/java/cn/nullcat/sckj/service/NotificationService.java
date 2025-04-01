package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.Notification;
import cn.nullcat.sckj.pojo.PageBean;

public interface NotificationService {
    PageBean getMyNotifications(Integer page, Integer pageSize, Integer type, Integer isRead, Integer userId);

    void readNotifications(Integer id);

    Integer getUnreadCount(Integer userId);
    /**
     * 发送预约状态变更通知
     */
    void sendStatusChangeNotification(Booking booking, Integer oldStatus, Integer newStatus);

    /**
     * 发送预约即将开始通知
     */
    void sendUpcomingNotification(Booking booking);

    /**
     * 发送预约结束通知
     */
    void sendEndNotification(Booking booking);

    void sendNotification(Notification notification);
}
