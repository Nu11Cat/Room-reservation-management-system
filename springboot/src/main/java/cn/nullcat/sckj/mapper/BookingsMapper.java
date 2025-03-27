package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Booking;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
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
}
