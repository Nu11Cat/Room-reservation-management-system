package cn.nullcat.sckj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Approval {
    private Long id;                    // 主键ID
    private Long bookingId;             // 预约ID
    private Long approverId;            // 审批人ID
    private Integer status;             // 状态：1-通过 2-拒绝
    private String comment;             // 审批意见
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
    private Boolean isDeleted;          // 是否删除
}
