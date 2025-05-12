package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "房间/会议室")
public class Room {
    private Long id;                    // 主键ID
    private String name;                // 会议室名称
    private String location;            // 位置
    private Integer capacity;           // 容纳人数
    private String equipment;           // 设备配置（JSON格式）
    private String description;         // 描述
    private Integer status;             // 状态：0-禁用 1-启用
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
    private Boolean isDeleted;          // 是否删除
}
