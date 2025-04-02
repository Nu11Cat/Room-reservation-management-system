package cn.nullcat.sckj.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRankingDTO {
    private Long userId;        // 用户ID
    private String username;    // 用户名
    private String realName;    // 真实姓名
    private Integer bookingCount;  // 预订次数
} 