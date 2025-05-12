package cn.nullcat.sckj.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalVO {
    // 审批表字段
    private Long id;                    // 审批ID
    private Long bookingId;             // 预约ID
    private Long approverId;            // 审批人ID
    private Integer status;             // 审批状态
    private String comment;             // 审批意见
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
    private Boolean isDeleted;          // 是否删除

    // 预约表字段
    private String title;               // 会议主题
    private Date startTime;             // 开始时间
    private Date endTime;               // 结束时间
    private Long applicantId;           // 申请人ID

    // 用户表字段
    private String applicantName;       // 申请人姓名

    // 会议室表字段
    private String roomName;            // 会议室名称
}