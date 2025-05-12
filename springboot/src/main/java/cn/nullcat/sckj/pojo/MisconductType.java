package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MisconductType {

    @Schema(description = "不文明行为类型ID", example = "1")
    private Integer id;

    @Schema(description = "不文明行为类型名称", example = "噪音")
    private String typeName;

    @Schema(description = "行为描述", example = "在公共区域大声喧哗")
    private String description;

    @Schema(description = "默认信誉分影响值", example = "10")
    private Integer defaultScoreImpact;

    @Schema(description = "严重程度(1-5)", example = "3")
    private Integer severityLevel;

    @Schema(description = "创建时间", example = "2023-04-01T10:00:00")
    private String createTime;

    @Schema(description = "更新时间", example = "2023-04-01T12:00:00")
    private String updateTime;
}
