package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.DTO.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface StatisticsMapper {
    // 会议室使用率统计
    List<RoomUsageDTO> getRoomUsageStatistics(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate);
                                                     
    // 预订状态统计
    List<BookingStatusDTO> getBookingStatusStatistics(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);
                                                     
    // 用户预订排名
    List<UserRankingDTO> getUserBookingRanking(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate,
                                              @Param("limit") Integer limit);
                                              
    // 会议时长统计
    List<MeetingDurationDTO> getMeetingDurationStatistics(@Param("startDate") Date startDate,
                                                         @Param("endDate") Date endDate);
                                                         
    // 获取总预订数
    Integer getTotalBookings(@Param("startDate") Date startDate,
                            @Param("endDate") Date endDate);
                            
    // 获取总用户数
    Integer getTotalUsers();
    
    // 获取总会议室数
    Integer getTotalRooms();
}