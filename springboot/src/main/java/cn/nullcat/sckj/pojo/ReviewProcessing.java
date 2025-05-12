package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评价处理记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评价处理记录")
public class ReviewProcessing {
    
    /**
     * 主键ID
     */
    @Schema(description = "处理记录ID")
    private Long id;
    
    /**
     * 评价ID
     */
    @Schema(description = "评价ID")
    private Long reviewId;
    
    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID")
    private Long processorId;
    
    /**
     * 处理结果：1-有效,2-无效,3-需调查
     */
    @Schema(description = "处理结果：1-有效,2-无效,3-需调查")
    private Integer processingResult;
    
    /**
     * 最终信誉分变动
     */
    @Schema(description = "最终信誉分变动")
    private Integer finalCreditImpact;
    
    /**
     * 处理意见
     */
    @Schema(description = "处理意见")
    private String processingComment;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    
    // 非数据库字段
    /**
     * 处理人姓名
     */
    @Schema(description = "处理人姓名")
    private String processorName;
    
    /**
     * 关联的评价信息
     */
    @Schema(description = "关联的评价信息")
    private UserReview userReview;
}
