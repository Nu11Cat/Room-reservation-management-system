package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统配置")
public class SystemConfig {
    private Long id;                    // 主键ID
    private String configKey;           // 配置键
    private String configValue;         // 配置值
    private String configType;          // 配置类型（BOOKING_RULE-预约规则, APPROVAL_RULE-审批规则, SYSTEM_PARAM-系统参数, NOTIFY_TEMPLATE-通知模板）
    private String description;         // 配置描述
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
    private Boolean isDeleted;          // 是否删除
}
