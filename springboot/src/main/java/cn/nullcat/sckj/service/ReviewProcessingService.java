package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.ReviewProcessing;
import cn.nullcat.sckj.pojo.UserReview;

import java.util.Date;
import java.util.List;

/**
 * 用户评价处理服务接口
 * 用于管理员处理用户评价
 */
public interface ReviewProcessingService {
    
    /**
     * 获取待处理的评价列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param reviewType 评价类型(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 评价分页列表
     */
    PageBean getPendingReviews(Integer page, Integer pageSize, 
                             Integer reviewType, Date startDate, Date endDate);
    
    /**
     * 处理用户评价
     * @param reviewId 评价ID
     * @param creditImpact 信用分影响
     * @param processorId 处理人ID
     * @param processingNotes 处理备注
     * @return 处理结果
     */
    Result processReview(Long reviewId, Integer creditImpact, Long processorId, String processingNotes);
    
    /**
     * 批量处理用户评价
     * @param reviewIds 评价ID列表
     * @param creditImpact 信用分影响
     * @param processorId 处理人ID
     * @param processingNotes 处理备注
     * @return 处理结果
     */
    Result batchProcessReviews(List<Long> reviewIds, Integer creditImpact, Long processorId, String processingNotes);
    
    /**
     * 获取已处理的评价列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param processorId 处理人ID(可选)
     * @param reviewType 评价类型(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 评价分页列表
     */
    PageBean getProcessedReviews(Integer page, Integer pageSize, Long processorId,
                              Integer reviewType, Date startDate, Date endDate);
    
    /**
     * 撤销处理
     * @param reviewId 评价ID
     * @param operatorId 操作人ID
     * @return 撤销结果
     */
    Result undoProcessing(Long reviewId, Long operatorId);
    
    /**
     * 获取处理统计信息
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Result getProcessingStatistics(Date startDate, Date endDate);
} 