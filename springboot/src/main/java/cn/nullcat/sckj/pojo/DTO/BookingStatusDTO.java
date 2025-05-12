package cn.nullcat.sckj.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingStatusDTO {
    private Integer status;     // 状态：0-待审批 1-已通过 2-已拒绝 3-已取消
    private String statusName;  // 状态名称
    private Integer count;      // 数量
    private Double percentage;  // 百分比
} 