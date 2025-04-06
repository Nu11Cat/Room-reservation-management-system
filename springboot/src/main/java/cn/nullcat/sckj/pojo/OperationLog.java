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
    
    // 以下字段不属于数据库表字段，用于接收联表查询结果
    private String userName;            // 用户名
    private String realName;            // 用户真实姓名
}
