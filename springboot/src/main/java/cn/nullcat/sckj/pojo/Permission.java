package cn.nullcat.sckj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private Long id;                    // 主键ID
    private Long parentId;              // 父级ID
    private String name;                // 权限名称
    private String code;                // 权限编码
    private String type;                // 类型：menu-菜单 button-按钮
    private String path;                // 菜单路径
    private Integer sort;               // 排序号
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
    private Boolean isDeleted;          // 是否删除
}
