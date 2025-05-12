package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.SpecialTimePeriod;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SpecialTimePeriodMapper {

    /**
     * 获取指定周的特殊时间段配置
     * @param weekStartDate 周开始日期（周一）
     * @return 特殊时间段配置
     */
    @Select("SELECT * FROM special_time_period WHERE week_start_date = #{weekStartDate}")
    SpecialTimePeriod getByWeekStartDate(LocalDate weekStartDate);

    /**
     * 获取最近的特殊时间段配置
     * @return 最近的特殊时间段配置
     */
    @Select("SELECT * FROM special_time_period ORDER BY week_start_date DESC LIMIT 1")
    SpecialTimePeriod getLatestConfig();

    /**
     * 插入新的特殊时间段配置
     * @param specialTimePeriod 特殊时间段配置
     */
    @Insert("INSERT INTO special_time_period(week_start_date, teacher_reserved_count, teacher_available_count, teacher_min_reserve_count, student_available_count, total_rooms_count, adjustment_date) " +
            "VALUES(#{weekStartDate}, #{teacherReservedCount}, #{teacherAvailableCount}, #{teacherMinReserveCount}, #{studentAvailableCount}, #{totalRoomsCount}, #{adjustmentDate})")
    void insert(SpecialTimePeriod specialTimePeriod);

    /**
     * 更新特殊时间段配置
     * @param specialTimePeriod 特殊时间段配置
     */
    @Update("UPDATE special_time_period SET teacher_reserved_count = #{teacherReservedCount}, " +
            "teacher_available_count = #{teacherAvailableCount}, teacher_min_reserve_count = #{teacherMinReserveCount}, " +
            "student_available_count = #{studentAvailableCount}, total_rooms_count = #{totalRoomsCount}, " +
            "adjustment_date = #{adjustmentDate} WHERE id = #{id}")
    void update(SpecialTimePeriod specialTimePeriod);

    /**
     * 获取所有特殊时间段配置
     * @return 所有特殊时间段配置列表
     */
    @Select("SELECT * FROM special_time_period ORDER BY week_start_date DESC")
    List<SpecialTimePeriod> getAllConfigs();

    /**
     * 统计教师在特定周的预约数量
     * @param weekStartDate 周开始日期
     * @return 教师预约数量
     */
    @Select("SELECT COUNT(*) FROM booking b JOIN user u ON b.user_id = u.id " +
            "WHERE u.identity = 'teacher' AND b.status = 1 " +
            "AND DATE(b.start_time) >= #{weekStartDate} " +
            "AND DATE(b.start_time) < DATE_ADD(#{weekStartDate}, INTERVAL 7 DAY)")
    int countTeacherBookingsInWeek(LocalDate weekStartDate);

    /**
     * 统计学生在特定周的预约数量
     * @param weekStartDate 周开始日期
     * @return 学生预约数量
     */
    @Select("SELECT COUNT(*) FROM booking b JOIN user u ON b.user_id = u.id " +
            "WHERE u.identity = 'student' AND b.status = 1 " +
            "AND DATE(b.start_time) >= #{weekStartDate} " +
            "AND DATE(b.start_time) < DATE_ADD(#{weekStartDate}, INTERVAL 7 DAY)")
    int countStudentBookingsInWeek(LocalDate weekStartDate);

    /**
     * 统计总会议室数量
     * @return 总会议室数量
     */
    @Select("SELECT COUNT(*) FROM room WHERE status = 1 AND is_deleted = 0")
    int countTotalRooms();
}