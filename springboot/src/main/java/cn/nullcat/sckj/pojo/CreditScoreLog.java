package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditScoreLog {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "分数变动值", example = "-5")
    private Integer scoreChange;

    @Schema(description = "变动前分数", example = "85")
    private Integer previousScore;

    @Schema(description = "当前分数", example = "80")
    private Integer currentScore;

    @Schema(description = "变动原因", example = "迟到")
    private String changeReason;

    @Schema(description = "关联ID（如评价ID）", example = "5001")
    private Long relatedId;

    @Schema(description = "操作人ID（如管理员ID）", example = "9001")
    private Long operatorId;

    @Schema(description = "创建时间", example = "2024-04-12T15:30:00")
    private String createTime;
}
