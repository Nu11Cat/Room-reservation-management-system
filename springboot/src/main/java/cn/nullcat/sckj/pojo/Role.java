package cn.nullcat.sckj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    private Long id;             // 角色ID
    private String roleName;         // 角色名称
    private String roleCode;      //角色编码
    private String description;  // 角色描述
    private Date createTime;     // 创建时间
    private Date updateTime;     // 更新时间
    private Boolean isDeleted;   // 是否删除
}
