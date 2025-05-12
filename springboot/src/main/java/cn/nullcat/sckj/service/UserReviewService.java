package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.UserReview;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户评价服务接口
 */
public interface UserReviewService {
    
    /**
     * 提交用户评价
     * @param userReview 用户评价信息
     * @return 评价ID
     */
    Long submitReview(UserReview userReview);
    
    /**
     * 获取用户发出的评价列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param reviewType 评价类型(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 评价分页列表
     */
    PageBean getOutgoingReviews(Long userId, Integer page, Integer pageSize, 
                             Integer reviewType, Date startDate, Date endDate);
    
    /**
     * 获取用户收到的评价列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param reviewType 评价类型(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 评价分页列表
     */
    PageBean getIncomingReviews(Long userId, Integer page, Integer pageSize,
                             Integer reviewType, Date startDate, Date endDate);
    
    /**
     * 根据ID获取评价
     * @param id 评价ID
     * @return 评价详情
     */
    UserReview getReviewById(Long id);
    
    /**
     * 更新评价
     * @param userReview 评价信息
     * @return 更新结果
     */
    boolean updateReview(UserReview userReview);
    
    /**
     * 删除评价
     * @param reviewId 评价ID
     * @return 删除结果
     */
    boolean deleteReview(Long reviewId);
    
    /**
     * 检查用户是否是管理员
     * @param userId 用户ID
     * @return 是否是管理员
     */
    boolean isUserAdmin(Long userId);
    
    /**
     * 处理评价
     * @param reviewId 评价ID
     * @param processResult 处理结果（1-通过，0-不通过）
     * @param processNote 处理备注
     * @return 处理结果
     */
    boolean processReview(Long reviewId, Integer processResult, String processNote);
    
    /**
     * 批量处理评价
     * @param reviewIds 评价ID列表
     * @param processResult 处理结果（1-通过，0-不通过）
     * @param processNote 处理备注
     * @return 处理结果
     */
    boolean batchProcessReview(List<Long> reviewIds, Integer processResult, String processNote);
    
    /**
     * 撤销处理评价
     * @param reviewId 评价ID
     * @return 处理结果
     */
    boolean undoProcessReview(Long reviewId);
    
    /**
     * 获取评价统计信息
     * @return 统计信息
     */
    Map<String, Long> getReviewStatistics();
}