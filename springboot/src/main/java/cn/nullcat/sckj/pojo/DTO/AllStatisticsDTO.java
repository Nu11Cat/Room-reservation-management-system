package cn.nullcat.sckj.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllStatisticsDTO {
    private List<RoomUsageDTO> roomUsage;               // 会议室使用率
    private List<BookingStatusDTO> bookingStatus;       // 预订状态统计
    private List<UserRankingDTO> userRanking;           // 用户预订排名
    private List<MeetingDurationDTO> meetingDuration;   // 会议时长统计
    private Integer totalBookings;                      // 总预订数
    private Integer totalUsers;                         // 总用户数
    private Integer totalRooms;                         // 总会议室数
} 