package cn.nullcat.sckj.pojo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "全部数据统计")
public class AllStatisticsDTO {
    @Schema(description = "会议室使用率")
    private List<RoomUsageDTO> roomUsage;               // 会议室使用率
    @Schema(description = "预订状态统计")
    private List<BookingStatusDTO> bookingStatus;       // 预订状态统计
    @Schema(description = "用户预订排名")
    private List<UserRankingDTO> userRanking;           // 用户预订排名
    @Schema(description = "会议时长统计")
    private List<MeetingDurationDTO> meetingDuration;   // 会议时长统计
    @Schema(description = "总预订数")
    private Integer totalBookings;                      // 总预订数
    @Schema(description = "总用户数")
    private Integer totalUsers;                         // 总用户数
    @Schema(description = "总会议室数")
    private Integer totalRooms;                         // 总会议室数
} 