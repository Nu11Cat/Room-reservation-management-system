package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.ReviewProcessing;
import cn.nullcat.sckj.pojo.UserReview;
import cn.nullcat.sckj.service.ReviewProcessingService;
import cn.nullcat.sckj.service.UserReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/review/process")
@Tag(name = "评价处理管理")
public class ReviewProcessingController {

    @Autowired
    private ReviewProcessingService reviewProcessingService;
    
    @Autowired
    private UserReviewService userReviewService;
    
    /**
     * 处理评价
     * @param request HTTP请求
     * @return 处理结果
     */
    @PostMapping
    @RequirePermission("review:process")
    @Operation(summary = "处理评价")
    public Result processReview(
            @RequestParam Long reviewId,
            @RequestParam Integer creditImpact,
            @RequestParam(required = false) String processingNotes,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 处理评价: reviewId={}, creditImpact={}", userId, reviewId, creditImpact);
        
        // 检查评价是否存在
        UserReview review = userReviewService.getReviewById(reviewId);
        if (review == null) {
            return Result.error("评价不存在");
        }
        
        // 检查评价是否已处理
        if (review.getIsProcessed() == 1) {
            return Result.error("评价已处理，不能重复处理");
        }
        
        // 处理评价
        return reviewProcessingService.processReview(reviewId, creditImpact, Long.valueOf(userId), processingNotes);
    }
    
    /**
     * 批量处理评价
     * @param reviewIds 评价ID列表
     * @param creditImpact 信用分影响
     * @param processingNotes 处理备注
     * @param request HTTP请求
     * @return 处理结果
     */
    @PostMapping("/batch")
    @RequirePermission("review:process")
    @Operation(summary = "批量处理评价")
    public Result batchProcessReviews(
            @RequestBody List<Long> reviewIds,
            @RequestParam Integer creditImpact,
            @RequestParam(required = false) String processingNotes,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 批量处理评价: reviewIds={}, creditImpact={}", userId, reviewIds, creditImpact);
        
        return reviewProcessingService.batchProcessReviews(reviewIds, creditImpact, Long.valueOf(userId), processingNotes);
    }
    
    /**
     * 查询待处理评价列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param reviewType 评价类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 评价列表
     */
    @GetMapping("/pending")
    @RequirePermission("review:view")
    @Operation(summary = "查询待处理评价列表")
    public Result getPendingReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer reviewType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 查询待处理评价列表: page={}, pageSize={}, reviewType={}, startDate={}, endDate={}",
                userId, page, pageSize, reviewType, startDate, endDate);
        
        PageBean pageBean = reviewProcessingService.getPendingReviews(
                page, pageSize, reviewType, startDate, endDate);
                
        return Result.success(pageBean);
    }
    
    /**
     * 查询已处理评价列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param reviewType 评价类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 评价列表
     */
    @GetMapping("/processed")
    @RequirePermission("review:view")
    @Operation(summary = "查询已处理评价列表")
    public Result getProcessedReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer reviewType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 查询已处理评价列表: page={}, pageSize={}, reviewType={}, startDate={}, endDate={}",
                userId, page, pageSize, reviewType, startDate, endDate);
        
        // 判断是否为管理员，管理员可查看所有人的处理记录
        boolean isAdmin = userReviewService.isUserAdmin(Long.valueOf(userId));
        Long processorId = isAdmin ? null : Long.valueOf(userId);
        
        PageBean pageBean = reviewProcessingService.getProcessedReviews(
                page, pageSize, processorId, reviewType, startDate, endDate);
                
        return Result.success(pageBean);
    }
    
    /**
     * 撤销处理
     * @param reviewId 评价ID
     * @param request HTTP请求
     * @return 撤销结果
     */
    @PostMapping("/undo")
    @RequirePermission("review:process")
    @Operation(summary = "撤销处理")
    public Result undoProcessing(
            @RequestParam Long reviewId,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 撤销处理, 评价ID: {}", userId, reviewId);
        
        return reviewProcessingService.undoProcessing(reviewId, Long.valueOf(userId));
    }
    
    /**
     * 获取处理统计信息
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @RequirePermission("review:view")
    @Operation(summary = "获取处理统计信息")
    public Result getProcessingStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 获取处理统计信息, 开始日期: {}, 结束日期: {}", userId, startDate, endDate);
        
        return reviewProcessingService.getProcessingStatistics(startDate, endDate);
    }
} 