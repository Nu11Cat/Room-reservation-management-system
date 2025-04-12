package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户")
public class User {
    @Schema(description = "主键ID")
    private Long id;                    // 主键ID
    @Schema(description = "用户名")
    private String username;            // 用户名
    @Schema(description = "主键密码")
    private String password;            // 密码
    @Schema(description = "真实姓名")
    private String realName;            // 真实姓名
    @Schema(description = "邮箱")
    private String email;               // 邮箱
    @Schema(description = "手机号")
    private String phone;               // 手机号
    @Schema(description = "头像URL")
    private String avatar;              // 头像URL
    @Schema(description = "角色ID")
    private Long roleId;                // 角色ID
    @Schema(description = "状态")
    private Integer status;             // 状态：0-禁用 1-启用
    @Schema(description = "创建时间")
    private Date createTime;            // 创建时间
    @Schema(description = "更新时间")
    private Date updateTime;            // 更新时间
    @Schema(description = "是否删除")
    private Boolean isDeleted;          // 是否删除
    @Schema(description = "真实姓名")
    private String roleName;
    @Schema(description = "旧密码")
    private String oldPassword;
    @Schema(description = "信誉分数")
    private Integer creditScore;
    @Schema(description = "身份类型")
    private String  identity;
}
