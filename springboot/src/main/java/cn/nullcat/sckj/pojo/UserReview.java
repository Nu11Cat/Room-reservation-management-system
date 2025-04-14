package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 用户评价实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReview {
    /**
     * 主键ID
     */
    @Schema(description = "评价ID", example = "1")
    private Long id;
    
    /**
     * 会议室ID
     */
    @Schema(description = "会议室ID", example = "101")
    private Long roomId;
    
    /**
     * 预约ID
     */
    @Schema(description = "预约记录ID", example = "202")
    private Long bookingId;
    
    /**
     * 评价人ID
     */
    @Schema(description = "评价人ID", example = "303")
    private Long reviewerId;
    
    /**
     * 被评价用户ID
     */
    @Schema(description = "被评价用户ID", example = "404")
    private Long reviewedUserId;
    
    /**
     * 评分（1-5）
     */
    @Schema(description = "评分", example = "5")
    private Integer reviewScore;
    
    /**
     * 评价类型（1-使用行为不当，2-服务态度问题，3-其他）
     */
    @Schema(description = "评价类型：1-清洁度, 2-守时, 3-设备使用, 4-噪音, 5-其他", example = "1")
    private Integer reviewType;
    
    /**
     * 评价内容
     */
    @Schema(description = "评价内容", example = "非常清洁")
    private String reviewContent;
    
    /**
     * 证据材料URL，以逗号分隔
     */
    @Schema(description = "证据图片URL", example = "[\"http://example.com/image1.jpg\"]")
    private String evidenceUrls;
    
    /**
     * 处理状态（0-未处理，1-已处理）
     */
    @Schema(description = "是否已处理", example = "0", defaultValue = "0")
    private Integer isProcessed;
    
    /**
     * 信用分影响
     */
    @Schema(description = "信誉分影响值", example = "10")
    private Integer creditImpact;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-04-01T10:00:00")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2023-04-01T12:00:00")
    private Date updateTime;
    
    // 非数据库字段
    /**
     * 评价人姓名
     */
    @Schema(description = "评价人姓名")
    private String reviewerName;
    
    /**
     * 被评价人姓名
     */
    @Schema(description = "被评价人姓名")
    private String reviewedUserName;
    
    /**
     * 会议室名称
     */
    @Schema(description = "会议室名称")
    private String roomName;
    
    /**
     * 证据材料URL列表
     */
    @Schema(description = "证据材料URL列表")
    private List<String> evidenceUrlList;
}
