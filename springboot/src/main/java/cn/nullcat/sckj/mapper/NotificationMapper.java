package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Notification;
import org.apache.ibatis.annotations.Insert;
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
    @Update("update notification set is_read = 1 where id = #{id}")
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

    /**
     * 添加通知
     * @param notification
     */
    @Insert("INSERT INTO notification (" +
            "        user_id," +
            "        title," +
            "        content," +
            "        type," +
            "        is_read," +
            "        create_time," +
            "        update_time," +
            "        is_deleted" +
            "    ) VALUES (" +
            "        #{userId}," +
            "        #{title}," +
            "        #{content}," +
            "        #{type}," +
            "        0," +
            "        NOW()," +
            "        NOW()," +
            "        0" +
            "    )")
    void insert(Notification notification);

    @Insert("<script>" +
            "INSERT INTO notification(user_id, title, content, type, is_read, create_time, update_time, is_deleted) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.userId}, #{item.title}, #{item.content}, #{item.type}, #{item.isRead}, #{item.createTime}, #{item.updateTime}, #{item.isDeleted})" +
            "</foreach>" +
            "</script>")
    void batchInsert(List<Notification> notifications);
}
