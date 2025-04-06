package cn.nullcat.sckj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private Long id;           // 权限ID
    private Long parentId;     // 父级ID
    private String name;       // 权限名称
    private String code;       // 权限代码
    private Integer type;      // 类型：1-菜单 2-按钮
    private String path;       // 路径
    private String component;  // 组件
    private String icon;       // 图标
    private Integer sort;      // 排序
    private Integer status;    // 状态
    private Date createTime;   // 创建时间
    private Date updateTime;   // 更新时间
    private Boolean isDeleted; // 是否删除
}
