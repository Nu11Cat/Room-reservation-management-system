package cn.nullcat.sckj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;                    // 主键ID
    private String username;            // 用户名
    private String password;            // 密码
    private String realName;            // 真实姓名
    private String email;               // 邮箱
    private String phone;               // 手机号
    private String avatar;              // 头像URL
    private Long roleId;                // 角色ID
    private Integer status;             // 状态：0-禁用 1-启用
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
    private Boolean isDeleted;          // 是否删除


    private String oldPassword;
}
