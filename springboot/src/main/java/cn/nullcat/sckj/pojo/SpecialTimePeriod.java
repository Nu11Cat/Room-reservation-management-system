package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialTimePeriod {

    @Schema(description = "唯一ID", example = "1")
    private Long id;

    @Schema(description = "本周的开始日期（周一）", example = "2024-04-08")
    private String weekStartDate;

    @Schema(description = "教师本周已预约的会议室数量", example = "3")
    private Integer teacherReservedCount;

    @Schema(description = "教师本周可预约的会议室数量", example = "5")
    private Integer teacherAvailableCount;

    @Schema(description = "预留给教师的最少会议室数量", example = "2")
    private Integer teacherMinReserveCount;

    @Schema(description = "学生本周可预约的会议室数量", example = "4")
    private Integer studentAvailableCount;

    @Schema(description = "总会议室数量", example = "10")
    private Integer totalRoomsCount;

    @Schema(description = "数据调整时间", example = "2024-04-12T09:30:00")
    private String adjustmentDate;

    @Schema(description = "创建时间", example = "2024-04-10T15:00:00")
    private String createTime;

    @Schema(description = "更新时间", example = "2024-04-11T18:00:00")
    private String updateTime;
}
