package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewProcessing {

    @Schema(description = "评价处理记录ID", example = "1")
    private Long id;

    @Schema(description = "评价ID", example = "12345")
    private Long reviewId;

    @Schema(description = "处理人ID", example = "67890")
    private Long processorId;

    @Schema(description = "处理结果：1-有效, 2-无效, 3-需调查", example = "1")
    private Integer processingResult;

    @Schema(description = "最终信誉分变动", example = "10")
    private Integer finalCreditImpact;

    @Schema(description = "处理意见", example = "已核实，处理有效")
    private String processingComment;

    @Schema(description = "创建时间", example = "2023-04-01T10:00:00")
    private String createTime;
}
