package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.ReviewProcessing;
import cn.nullcat.sckj.pojo.UserReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 评价处理记录数据访问接口
 */
@Mapper
public interface ReviewProcessingMapper {

    /**
     * 插入处理记录
     * @param reviewProcessing 处理记录
     * @return 影响行数
     */
    int insert(ReviewProcessing reviewProcessing);
    
    /**
     * 根据ID查询处理记录
     * @param id 记录ID
     * @return 处理记录
     */
    ReviewProcessing selectById(Long id);
    
    /**
     * 根据评价ID查询处理记录
     * @param reviewId 评价ID
     * @return 处理记录
     */
    ReviewProcessing selectByReviewId(Long reviewId);
    
    /**
     * 查询待处理的评价列表
     * @param offset 分页偏移
     * @param limit 分页大小
     * @param reviewType 评价类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 评价列表
     */
    List<UserReview> selectPendingReviews(
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("reviewType") Integer reviewType,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 查询待处理评价总数
     * @param reviewType 评价类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 评价总数
     */
    Long countPendingReviews(
        @Param("reviewType") Integer reviewType,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 查询已处理的评价列表
     * @param processorId 处理人ID（可选，若为null则查询所有）
     * @param processingResult 处理结果（可选）
     * @param offset 分页偏移
     * @param limit 分页大小 
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 处理记录列表
     */
    List<ReviewProcessing> selectProcessedReviews(
        @Param("processorId") Long processorId,
        @Param("processingResult") Integer processingResult,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 查询已处理评价总数
     * @param processorId 处理人ID（可选，若为null则查询所有）
     * @param processingResult 处理结果（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 处理记录总数
     */
    Long countProcessedReviews(
        @Param("processorId") Long processorId,
        @Param("processingResult") Integer processingResult,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 统计评价总数、已处理数和未处理数
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计结果Map
     */
    Map<String, Object> countReviewsStatistics(
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 统计评价类型分布
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 评价类型分布列表
     */
    List<Map<String, Object>> countReviewTypeDistribution(
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 统计处理结果分布
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 处理结果分布列表
     */
    List<Map<String, Object>> countProcessingResultDistribution(
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 计算平均处理时间（小时）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 平均处理时间（小时）
     */
    Double calculateAverageProcessingTime(
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
}