package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.NotificationMapper;
import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.Notification;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.service.NotificationService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 获取通知列表
     * @param page
     * @param pageSize
     * @param type
     * @param isRead
     * @param userId
     * @return
     */
    @Override
    public PageBean getMyNotifications(Integer page, Integer pageSize, Integer type, Integer isRead, Integer userId) {
        PageHelper.startPage(page, pageSize);
        List<Notification> list = notificationMapper.getMyNotifications(type,isRead,userId);
        Page<Notification> p = (Page<Notification>) list;
        PageBean pageBean = new PageBean(p.getTotal(), p.getResult());
        return pageBean;
    }

    /**
     * 标记通知已读
     * @param id
     */
    @Override
    public void readNotifications(Integer id) {
        notificationMapper.readNotifications(id);
    }

    /**
     * 获取未读通知数量
     * @param userId
     * @return
     */
    @Override
    public Integer getUnreadCount(Integer userId) {
        return notificationMapper.getUnreadById(userId);
    }
    /**
     * 发送预约状态变更通知
     */
    @Override
    public void sendStatusChangeNotification(Booking booking, Integer oldStatus, Integer newStatus) {
        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setTitle("预约状态变更");
        notification.setContent(getStatusChangeContent(booking, oldStatus, newStatus));
        notification.setType(1); // 1-预约状态变更
        notificationMapper.insert(notification);
    }
    /**
     * 发送预约即将开始通知
     */
    @Override
    public void sendUpcomingNotification(Booking booking) {
        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setTitle("预约即将开始");
        notification.setContent(getUpcomingContent(booking));
        notification.setType(2); // 2-预约提醒
        notificationMapper.insert(notification);
    }
    /**
     * 发送预约结束通知
     */
    @Override
    public void sendEndNotification(Booking booking) {
        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setTitle("预约已结束");
        notification.setContent(getEndContent(booking));
        notification.setType(2); // 2-预约提醒
        notificationMapper.insert(notification);
    }


    private String getStatusChangeContent(Booking booking, Integer oldStatus, Integer newStatus) {
        String statusText = getStatusText(newStatus);
        return String.format("您的预约[%s]状态已变更为：%s", booking.getTitle(), statusText);
    }

    private String getUpcomingContent(Booking booking) {
        return String.format("您的预约[%s]将在30分钟后开始", booking.getTitle());
    }

    private String getEndContent(Booking booking) {
        return String.format("您的预约[%s]已结束", booking.getTitle());
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待审批";
            case 1: return "已通过";
            case 2: return "已拒绝";
            case 3: return "已取消";
            default: return "未知状态";
        }
    }
}
