package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.ReviewProcessingMapper;
import cn.nullcat.sckj.mapper.UserReviewMapper;
import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.ReviewProcessing;
import cn.nullcat.sckj.pojo.UserReview;
import cn.nullcat.sckj.service.CreditScoreService;
import cn.nullcat.sckj.service.ReviewProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 评价处理服务实现
 */
@Slf4j
@Service
public class ReviewProcessingServiceImpl implements ReviewProcessingService {

    @Autowired
    private ReviewProcessingMapper reviewProcessingMapper;
    
    @Autowired
    private UserReviewMapper userReviewMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CreditScoreService creditScoreService;
    
    @Override
    public PageBean getPendingReviews(Integer page, Integer pageSize, 
                                    Integer reviewType, Date startDate, Date endDate) {
        log.info("查询待处理评价列表, 页码: {}, 每页大小: {}, 评价类型: {}, 开始日期: {}, 结束日期: {}", 
                page, pageSize, reviewType, startDate, endDate);
        
        // 计算分页参数
        int offset = (page - 1) * pageSize;
        
        // 查询待处理的评价列表
        List<UserReview> rows = reviewProcessingMapper.selectPendingReviews(
                offset, pageSize, reviewType, startDate, endDate);
        
        // 查询总记录数
        Long total = reviewProcessingMapper.countPendingReviews(reviewType, startDate, endDate);
        
        // 返回分页结果
        PageBean pageBean = new PageBean();
        pageBean.setTotal(total);
        pageBean.setRows(rows);
        
        return pageBean;
    }
    
    @Override
    @Transactional
    public Result processReview(Long reviewId, Integer creditImpact, Long processorId, String processingNotes) {
        log.info("处理评价, 评价ID: {}, 处理人: {}, 信用分影响: {}", reviewId, processorId, creditImpact);
        
        // 获取评价信息
        UserReview userReview = userReviewMapper.selectById(reviewId);
        if (userReview == null) {
            log.error("评价不存在, ID: {}", reviewId);
            return Result.error("评价不存在");
        }
        
        // 检查评价是否已处理
        if (userReview.getIsProcessed() == 1) {
            log.error("评价已处理, ID: {}", reviewId);
            return Result.error("评价已处理，不能重复处理");
        }
        
        // 创建处理记录
        ReviewProcessing reviewProcessing = new ReviewProcessing();
        reviewProcessing.setReviewId(reviewId);
        reviewProcessing.setProcessorId(processorId);
        // 根据信用分影响判断处理结果：正分为有效(1)，0分为无效(2)，负分为需调查(3)
        int processingResult = creditImpact > 0 ? 1 : (creditImpact == 0 ? 2 : 3);
        reviewProcessing.setProcessingResult(processingResult);
        reviewProcessing.setFinalCreditImpact(creditImpact);
        reviewProcessing.setProcessingComment(processingNotes);
        reviewProcessing.setCreateTime(new Date());
        
        // 更新评价状态为已处理
        userReview.setIsProcessed(1);
        // 更新信用分影响值
        userReview.setCreditImpact(creditImpact);
        userReview.setUpdateTime(new Date());
        
        // 保存处理记录
        int rows = reviewProcessingMapper.insert(reviewProcessing);
        if (rows <= 0) {
            log.error("保存处理记录失败");
            return Result.error("保存处理记录失败");
        }
        
        // 更新评价
        rows = userReviewMapper.updateById(userReview);
        if (rows <= 0) {
            log.error("更新评价状态失败");
            return Result.error("更新评价状态失败");
        }
        
        // 更新用户信誉分
        if (creditImpact != 0 && userReview.getReviewedUserId() != null) {
            String reason = "评价处理";
            if (processingNotes != null && !processingNotes.isEmpty()) {
                reason = reason + ": " + processingNotes;
            }
            
            Result scoreResult = creditScoreService.updateUserScore(
                userReview.getReviewedUserId(), 
                creditImpact, 
                reason, 
                reviewId, 
                processorId
            );
            
            if (scoreResult.getCode() != 1) {
                log.warn("更新用户信誉分失败: {}", scoreResult.getMsg());
            } else {
                log.info("用户 {} 信誉分已更新: {}", userReview.getReviewedUserId(), creditImpact);
            }
        }
        
        log.info("评价处理成功, 处理记录ID: {}", reviewProcessing.getId());
        return Result.success(reviewProcessing.getId(), "处理成功");
    }
    
    @Override
    @Transactional
    public Result batchProcessReviews(List<Long> reviewIds, Integer creditImpact, Long processorId, String processingNotes) {
        log.info("批量处理评价, 评价IDs: {}, 处理人: {}, 信用分影响: {}", reviewIds, processorId, creditImpact);
        
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Result.error("评价ID列表不能为空");
        }
        
        List<Long> successIds = new ArrayList<>();
        List<Long> failIds = new ArrayList<>();
        
        for (Long reviewId : reviewIds) {
            Result result = processReview(reviewId, creditImpact, processorId, processingNotes);
            if (result.getCode() == 1) {
                successIds.add(reviewId);
            } else {
                failIds.add(reviewId);
            }
        }
        
        if (failIds.isEmpty()) {
            return Result.success("全部处理成功");
        } else if (successIds.isEmpty()) {
            return Result.error("全部处理失败");
        } else {
            return Result.success(
                    new Object[]{successIds, failIds}, 
                    String.format("处理完成，%d 个成功，%d 个失败", successIds.size(), failIds.size())
            );
        }
    }
    
    @Override
    public PageBean getProcessedReviews(Integer page, Integer pageSize, Long processorId,
                                     Integer reviewType, Date startDate, Date endDate) {
        log.info("查询已处理评价列表, 页码: {}, 每页大小: {}, 处理人: {}, 评价类型: {}, 开始日期: {}, 结束日期: {}", 
                page, pageSize, processorId, reviewType, startDate, endDate);
        
        // 计算分页参数
        int offset = (page - 1) * pageSize;
        
        // 查询已处理的评价列表
        List<ReviewProcessing> rows = reviewProcessingMapper.selectProcessedReviews(
                processorId, null, offset, pageSize, startDate, endDate);
        
        // 查询总记录数
        Long total = reviewProcessingMapper.countProcessedReviews(processorId, null, startDate, endDate);
        
        // 过滤评价类型
        if (reviewType != null) {
            rows = rows.stream()
                    .filter(rp -> rp.getUserReview() != null && 
                            reviewType.equals(rp.getUserReview().getReviewType()))
                    .toList();
            // 由于是内存过滤，总数需要重新计算
            total = (long) rows.size();
        }
        
        // 返回分页结果
        PageBean pageBean = new PageBean();
        pageBean.setTotal(total);
        pageBean.setRows(rows);
        
        return pageBean;
    }
    
    @Override
    @Transactional
    public Result undoProcessing(Long reviewId, Long operatorId) {
        log.info("撤销处理, 评价ID: {}, 操作人: {}", reviewId, operatorId);
        
        // 获取评价信息
        UserReview userReview = userReviewMapper.selectById(reviewId);
        if (userReview == null) {
            log.error("评价不存在, ID: {}", reviewId);
            return Result.error("评价不存在");
        }
        
        // 检查评价是否已处理
        if (userReview.getIsProcessed() != 1) {
            log.error("评价未处理, 无法撤销, ID: {}", reviewId);
            return Result.error("评价未处理，无法撤销");
        }
        
        // 获取处理记录
        ReviewProcessing processing = reviewProcessingMapper.selectByReviewId(reviewId);
        if (processing == null) {
            log.error("处理记录不存在, 评价ID: {}", reviewId);
            return Result.error("处理记录不存在");
        }
        
        // 检查操作权限（只有处理人自己或管理员可以撤销）
        if (!processing.getProcessorId().equals(operatorId) && !isUserAdmin(operatorId)) {
            return Result.error("没有权限撤销此处理记录");
        }
        
        // 更新评价状态为未处理
        userReview.setIsProcessed(0);
        // 清除信用分影响值
        userReview.setCreditImpact(0);
        userReview.setUpdateTime(new Date());
        
        // 更新评价
        int rows = userReviewMapper.updateById(userReview);
        if (rows <= 0) {
            log.error("更新评价状态失败");
            return Result.error("撤销处理失败");
        }
        
        // 恢复用户信誉分（如果之前有影响）
        if (processing.getFinalCreditImpact() != 0 && userReview.getReviewedUserId() != null) {
            // 撤销时应用相反的分数变动
            int reverseCreditImpact = -processing.getFinalCreditImpact();
            
            Result scoreResult = creditScoreService.updateUserScore(
                userReview.getReviewedUserId(), 
                reverseCreditImpact, 
                "撤销评价处理", 
                reviewId, 
                operatorId
            );
            
            if (scoreResult.getCode() != 1) {
                log.warn("恢复用户信誉分失败: {}", scoreResult.getMsg());
            } else {
                log.info("用户 {} 信誉分已恢复: {}", userReview.getReviewedUserId(), reverseCreditImpact);
            }
        }
        
        // 删除处理记录 (或者标记为已撤销)
        // 此处简化处理，直接删除处理记录
        // int deleteResult = reviewProcessingMapper.deleteById(processing.getId());
        
        log.info("撤销处理成功, 评价ID: {}", reviewId);
        return Result.success("撤销处理成功");
    }
    
    @Override
    public Result getProcessingStatistics(Date startDate, Date endDate) {
        log.info("获取处理统计信息, 开始日期: {}, 结束日期: {}", startDate, endDate);
        
        try {
            // 1. 获取评价总数、已处理数和未处理数
            Map<String, Object> countStats = reviewProcessingMapper.countReviewsStatistics(startDate, endDate);

            // 2. 获取评价类型分布
            List<Map<String, Object>> reviewTypeStats = reviewProcessingMapper.countReviewTypeDistribution(startDate, endDate);

            // 3. 获取处理结果分布
            List<Map<String, Object>> processingResultStats = reviewProcessingMapper.countProcessingResultDistribution(startDate, endDate);

            // 4. 获取平均处理时间
            Double avgProcessingTime = reviewProcessingMapper.calculateAverageProcessingTime(startDate, endDate);

            // 5. 计算处理率
            Long totalReviews = (Long) countStats.get("total_reviews");
            Long processedReviews = (Long) countStats.get("processed_reviews");
            double processingRate = 0.0;
            if (totalReviews != null && totalReviews > 0) {
                processingRate = (processedReviews * 100.0) / totalReviews;
            }

            // 6. 整合所有统计数据
            Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("counts", countStats);
            statistics.put("processingRate", Math.round(processingRate * 100) / 100.0); // 保留两位小数
            statistics.put("reviewTypeDistribution", reviewTypeStats);
            statistics.put("processingResultDistribution", processingResultStats);
            statistics.put("averageProcessingTime", avgProcessingTime);

            // 7. 添加统计时间范围信息
            Map<String, Object> timeRange = new java.util.HashMap<>();
            timeRange.put("startDate", startDate);
            timeRange.put("endDate", endDate);
            statistics.put("timeRange", timeRange);

            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取处理统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 判断用户是否为管理员
     * @param userId 用户ID
     * @return 是否为管理员
     */
    private boolean isUserAdmin(Long userId) {
        // 查询用户信息
        cn.nullcat.sckj.pojo.User user = userMapper.getById(Math.toIntExact(userId));
        
        // 检查用户角色是否为管理员（角色ID为1或2的是管理员）
        boolean isAdmin = user != null && (user.getRoleId() == 1 || user.getRoleId() == 2);
        
        log.info("用户 {} 是管理员: {}", userId, isAdmin);
        
        return isAdmin;
    }
}