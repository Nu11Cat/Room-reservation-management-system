package cn.nullcat.sckj.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeetingDurationDTO {
    private String durationRange;  // 时长范围（例如："0-1小时", "1-2小时"）
    private Integer count;         // 数量
    private Double percentage;     // 百分比
} 