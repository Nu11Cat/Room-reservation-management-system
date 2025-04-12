package cn.nullcat.sckj.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReview {

    @Schema(description = "评价ID", example = "1")
    private Long id;

    @Schema(description = "会议室ID", example = "101")
    private Long roomId;

    @Schema(description = "预约记录ID", example = "202")
    private Long bookingId;

    @Schema(description = "评价人ID", example = "303")
    private Long reviewerId;

    @Schema(description = "被评价用户ID", example = "404")
    private Long reviewedUserId;

    @Schema(description = "评分", example = "5")
    private Integer reviewScore;

    @Schema(description = "评价类型：1-清洁度, 2-守时, 3-设备使用, 4-噪音, 5-其他", example = "1")
    private Integer reviewType;

    @Schema(description = "评价内容", example = "非常清洁")
    private String reviewContent;

    @Schema(description = "证据图片URL", example = "[\"http://example.com/image1.jpg\"]")
    private String evidenceUrls;

    @Schema(description = "是否已处理", example = "0", defaultValue = "0")
    private Integer isProcessed;

    @Schema(description = "信誉分影响值", example = "10")
    private Integer creditImpact;

    @Schema(description = "创建时间", example = "2023-04-01T10:00:00")
    private String createTime;

    @Schema(description = "更新时间", example = "2023-04-01T12:00:00")
    private String updateTime;
}
