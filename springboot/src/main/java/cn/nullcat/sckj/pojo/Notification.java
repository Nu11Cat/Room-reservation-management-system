package cn.nullcat.sckj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private Long id;                    // 主键ID
    private Long userId;                // 接收用户ID
    private String title;               // 通知标题
    private String content;             // 通知内容
    private Integer type;               // 类型：1-系统通知 2-预约通知 3-审批通知
    private Boolean isRead;             // 是否已读
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
    private Boolean isDeleted;          // 是否删除
    
    // 新增字段
    private Integer receiverType;       // 接收者类型：0-指定用户，1-指定角色，2-所有用户
    private Long receiverId;            // 接收者ID (receiverType=0时必填)
    private Long roleId;                // 角色ID (receiverType=1时必填)
}
