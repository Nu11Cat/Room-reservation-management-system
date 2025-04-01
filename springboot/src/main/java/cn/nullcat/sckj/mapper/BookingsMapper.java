package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Booking;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Mapper
public interface BookingsMapper {
    /**
     * 获取预约列表
     * @param roomId
     * @param userId
     * @param status
     * @param begin
     * @param end
     * @return
     */
    List<Booking> getBookings(Integer roomId, Integer userId, Integer status, LocalDate begin, LocalDate end);

    /**
     * 获取预约详情
     * @param id
     * @return
     */
    @Select("select * from booking where id = #{id}")
    Booking getById(Integer id);

    /**
     *
     * @param booking
     */
    @Insert("insert into " +
            "booking(room_id, user_id, title, start_time, end_time, attendees, description,status,create_time,update_time,is_deleted)" +
            "values (#{roomId},#{userId},#{title},#{startTime},#{endTime},#{attendees},#{description},0,now(),now(),0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addBooking(Booking booking);

    /**
     *
     * @param booking
     */
    @Update("update booking set " +
            "title = #{title}," +
            "start_time = #{startTime}," +
            "end_time=#{endTime}," +
            "attendees=#{attendees}," +
            "description=#{description}," +
            "update_time = NOW()" +
            "where id = #{id}")
    void updateBooking(Booking booking);

    /**
     *
     * @param id
     */
    @Update("update booking set" +
            " status = 3," +
            " update_time = NOW()" +
            "where id = #{id}")
    void cancelBooking(Integer id);

    /**
     *
     * @param a
     * @param bookId
     */
    @Update("update booking set" +
            " status = #{a}," +
            " update_time = NOW()" +
            " where id = #{bookId}")
    void bookingStatusChange(Integer a, Long bookId);

    /**
     * 查询指定时间段内的有效预约
     * @param roomId 会议室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 冲突的预约列表
     */
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId,
                                          @Param("startTime") Date startTime,
                                          @Param("endTime") Date endTime);
    /**
     * 查询即将开始的预约（30分钟内）
     */
    List<Booking> findUpcomingBookings();

    /**
     * 查询刚刚结束的预约（5分钟内）
     */
    List<Booking> findEndedBookings();


    /**
     * 更新预订的开始通知状态
     * @param id 预订ID
     */
    @Update("UPDATE booking SET is_start_notified = 1, update_time = NOW() WHERE id = #{id}")
    void updateStartNotified(Long id);

    /**
     * 更新预订的结束通知状态
     * @param id 预订ID
     */
    @Update("UPDATE booking SET is_end_notified = 1, update_time = NOW() WHERE id = #{id}")
    void updateEndNotified(Long id);
}
