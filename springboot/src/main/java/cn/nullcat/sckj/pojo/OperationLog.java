package cn.nullcat.sckj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationLog {
    private Long id;                    // 主键ID
    private Long userId;                // 操作用户ID
    private String module;              // 操作模块
    private String operation;           // 操作类型
    private String description;         // 操作描述
    private String ip;                  // 操作IP
    private Date createTime;            // 创建时间
}
