package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper {
    /**
     * 获取通知列表
     * @param type
     * @param isRead
     * @param userId
     * @return
     */
    List<Notification> getMyNotifications(Integer type, Integer isRead, Integer userId);

    /**
     * 标记通知已读
     * @param id
     */
    @Update("update notification set is_read = 1")
    void readNotifications(Integer id);

    /**
     * 获取未读通知数量
     * @param userId
     * @return
     */
    @Select("SELECT COUNT(*) FROM notification " +
            "WHERE user_id = #{userId} " +
            "AND is_read = 0 " +
            "AND is_deleted = 0")
    Integer getUnreadById(Integer userId);
}
