package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.NotificationMapper;
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
}
