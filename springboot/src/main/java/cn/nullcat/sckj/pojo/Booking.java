package cn.nullcat.sckj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private Long id;                    // 主键ID
    private Long roomId;                // 会议室ID
    private Long userId;                // 预约用户ID
    private String title;               // 会议主题
    private Date startTime;             // 开始时间
    private Date endTime;               // 结束时间
    private String attendees;           // 参会人员（JSON格式）
    private String description;         // 会议描述
    private Integer status;             // 状态：0-待审批 1-已通过 2-已拒绝 3-已取消
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
    private Boolean isDeleted;          // 是否删除

    private String roomName;
    private String userName;

    private Integer isStartNotified;    // 是否已发送开始通知
    private Integer isEndNotified;      // 是否已发送结束通知
}
