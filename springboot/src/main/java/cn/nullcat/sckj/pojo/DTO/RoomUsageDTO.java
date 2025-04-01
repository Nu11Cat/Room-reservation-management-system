package cn.nullcat.sckj.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomUsageDTO {
    private Long roomId;        // 会议室ID
    private String roomName;    // 会议室名称
    private String location;    // 位置
    private Integer capacity;   // 容量
    private Integer bookingCount; // 预订次数
    private Double usageRate;   // 使用率(%)
}