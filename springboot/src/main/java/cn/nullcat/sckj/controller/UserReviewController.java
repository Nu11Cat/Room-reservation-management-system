package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.UserReview;
import cn.nullcat.sckj.service.UserReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/review")
@Tag(name = "用户评价管理")
public class UserReviewController {

    @Autowired
    private UserReviewService userReviewService;

    /**
     * 提交评价
     * @param userReview 评价信息
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "提交评价")
    public Result submitReview(@RequestBody UserReview userReview, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 提交评价: {}", userId, userReview);
        
        // 设置评价人ID
        userReview.setReviewerId(Long.valueOf(userId));
        
        // 校验必填字段
        if (userReview.getRoomId() == null) {
            return Result.error("会议室ID不能为空");
        }
        if (userReview.getBookingId() == null) {
            return Result.error("预约记录ID不能为空");
        }
        if (userReview.getReviewedUserId() == null) {
            return Result.error("被评价用户ID不能为空");
        }
        if (userReview.getReviewScore() == null || userReview.getReviewScore() < 1 || userReview.getReviewScore() > 5) {
            return Result.error("评分必须在1-5之间");
        }
        if (userReview.getReviewType() == null) {
            return Result.error("评价类型不能为空");
        }
        
        // 检查是否是自评
        if (userReview.getReviewedUserId().equals(Long.valueOf(userId))) {
            return Result.error("不能评价自己");
        }
        
        // 提交评价
        Long reviewId = userReviewService.submitReview(userReview);
        return Result.success(reviewId, "评价提交成功");
    }

    /**
     * 查看我发出的评价
     * @param page 页码
     * @param pageSize 每页大小
     * @param reviewType 评价类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 分页结果
     */
    @GetMapping("/outgoing")
    @Operation(summary = "查看我发出的评价")
    public Result getMyOutgoingReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer reviewType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 查看发出的评价: page={}, pageSize={}, reviewType={}, startDate={}, endDate={}",
                userId, page, pageSize, reviewType, startDate, endDate);
        
        PageBean pageBean = userReviewService.getOutgoingReviews(
                Long.valueOf(userId), page, pageSize, reviewType, startDate, endDate);
        return Result.success(pageBean);
    }

    /**
     * 查看我收到的评价
     * @param page 页码
     * @param pageSize 每页大小
     * @param reviewType 评价类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 分页结果
     */
    @GetMapping("/incoming")
    @Operation(summary = "查看我收到的评价")
    public Result getMyIncomingReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer reviewType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 查看收到的评价: page={}, pageSize={}, reviewType={}, startDate={}, endDate={}",
                userId, page, pageSize, reviewType, startDate, endDate);
        
        PageBean pageBean = userReviewService.getIncomingReviews(
                Long.valueOf(userId), page, pageSize, reviewType, startDate, endDate);
        return Result.success(pageBean);
    }

    /**
     * 获取评价详情
     * @param id 评价ID
     * @param request HTTP请求
     * @return 评价详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取评价详情")
    public Result getReviewDetail(@PathVariable Long id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 查看评价详情: id={}", userId, id);
        
        UserReview review = userReviewService.getReviewById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        
        // 非管理员只能查看与自己相关的评价
        boolean isAdmin = userReviewService.isUserAdmin(Long.valueOf(userId));
        if (!isAdmin && !review.getReviewerId().equals(Long.valueOf(userId)) && 
                !review.getReviewedUserId().equals(Long.valueOf(userId))) {
            return Result.error("无权查看此评价");
        }
        
        return Result.success(review);
    }

    /**
     * 修改评价（仅限评价人且未处理的评价）
     * @param id 评价ID
     * @param userReview 评价信息
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改评价")
    public Result updateReview(@PathVariable Long id, @RequestBody UserReview userReview, 
                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 修改评价: id={}, review={}", userId, id, userReview);
        
        // 设置ID
        userReview.setId(id);
        
        // 校验必填字段
        if (userReview.getReviewScore() != null && (userReview.getReviewScore() < 1 || userReview.getReviewScore() > 5)) {
            return Result.error("评分必须在1-5之间");
        }
        
        // 检查权限和状态
        UserReview existingReview = userReviewService.getReviewById(id);
        if (existingReview == null) {
            return Result.error("评价不存在");
        }
        
        // 只有评价人可以修改，且评价必须未处理
        if (!existingReview.getReviewerId().equals(Long.valueOf(userId))) {
            return Result.error("只能修改自己发出的评价");
        }
        
        if (existingReview.getIsProcessed() == 1) {
            return Result.error("已处理的评价不能修改");
        }
        
        // 执行修改
        boolean updated = userReviewService.updateReview(userReview);
        return updated ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除评价（仅限评价人且未处理的评价）
     * @param id 评价ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价")
    public Result deleteReview(@PathVariable Long id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 删除评价: id={}", userId, id);
        
        // 检查权限和状态
        UserReview existingReview = userReviewService.getReviewById(id);
        if (existingReview == null) {
            return Result.error("评价不存在");
        }
        
        // 只有评价人可以删除，且评价必须未处理
        if (!existingReview.getReviewerId().equals(Long.valueOf(userId))) {
            return Result.error("只能删除自己发出的评价");
        }
        
        if (existingReview.getIsProcessed() == 1) {
            return Result.error("已处理的评价不能删除");
        }
        
        // 执行删除
        boolean deleted = userReviewService.deleteReview(id);
        return deleted ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 处理评价（仅限管理员）
     * @param id 评价ID
     * @param processForm 处理表单
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/{id}/process")
    @Operation(summary = "处理评价")
    public Result processReview(
            @PathVariable Long id,
            @RequestBody Map<String, Object> processForm,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 处理评价: id={}, form={}", userId, id, processForm);
        
        // 检查是否是管理员
        boolean isAdmin = userReviewService.isUserAdmin(Long.valueOf(userId));
        if (!isAdmin) {
            return Result.error("只有管理员可以处理评价");
        }
        
        // 获取处理结果和备注
        Integer processResult = (Integer) processForm.get("processResult");
        String processNote = (String) processForm.get("processNote");
        
        if (processResult == null) {
            return Result.error("处理结果不能为空");
        }
        
        // 执行处理
        boolean processed = userReviewService.processReview(id, processResult, processNote);
        return processed ? Result.success("处理成功") : Result.error("处理失败");
    }
    
    /**
     * 批量处理评价（仅限管理员）
     * @param batchForm 批量处理表单
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/batch-process")
    @Operation(summary = "批量处理评价")
    public Result batchProcessReview(
            @RequestBody Map<String, Object> batchForm,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 批量处理评价: form={}", userId, batchForm);
        
        // 检查是否是管理员
        boolean isAdmin = userReviewService.isUserAdmin(Long.valueOf(userId));
        if (!isAdmin) {
            return Result.error("只有管理员可以处理评价");
        }
        
        // 获取评价ID列表、处理结果和备注
        @SuppressWarnings("unchecked")
        List<Long> reviewIds = (List<Long>) batchForm.get("reviewIds");
        Integer processResult = (Integer) batchForm.get("processResult");
        String processNote = (String) batchForm.get("processNote");
        
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Result.error("评价ID列表不能为空");
        }
        
        if (processResult == null) {
            return Result.error("处理结果不能为空");
        }
        
        // 执行批量处理
        boolean processed = userReviewService.batchProcessReview(reviewIds, processResult, processNote);
        return processed ? Result.success("批量处理成功") : Result.error("批量处理失败");
    }
    
    /**
     * 撤销处理评价（仅限管理员）
     * @param id 评价ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/{id}/undo-process")
    @Operation(summary = "撤销处理评价")
    public Result undoProcessReview(
            @PathVariable Long id,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 撤销处理评价: id={}", userId, id);
        
        // 检查是否是管理员
        boolean isAdmin = userReviewService.isUserAdmin(Long.valueOf(userId));
        if (!isAdmin) {
            return Result.error("只有管理员可以撤销处理评价");
        }
        
        // 执行撤销处理
        boolean undone = userReviewService.undoProcessReview(id);
        return undone ? Result.success("撤销处理成功") : Result.error("撤销处理失败");
    }
    
    /**
     * 获取评价统计信息（仅限管理员）
     * @param request HTTP请求
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取评价统计信息")
    public Result getReviewStatistics(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户 {} 获取评价统计信息", userId);
        
        // 检查是否是管理员
        boolean isAdmin = userReviewService.isUserAdmin(Long.valueOf(userId));
        if (!isAdmin) {
            return Result.error("只有管理员可以查看统计信息");
        }
        
        // 获取统计信息
        Map<String, Long> statistics = userReviewService.getReviewStatistics();
        return Result.success(statistics);
    }
    
    /**
     * 获取所有不文明行为类型
     * @return 不文明行为类型列表
     */
    @GetMapping("/misconduct-types")
    @Operation(summary = "获取所有不文明行为类型")
    public Result getAllMisconductTypes() {
        log.info("获取所有不文明行为类型");
        
        // 这里应该调用服务层方法获取不文明行为类型列表
        // 由于目前没有实现该服务，这里返回一个模拟数据
        List<Map<String, Object>> types = new ArrayList<>();
        
        Map<String, Object> type1 = new HashMap<>();
        type1.put("id", 1);
        type1.put("typeName", "吸烟");
        types.add(type1);
        
        Map<String, Object> type2 = new HashMap<>();
        type2.put("id", 2);
        type2.put("typeName", "大声喧哗");
        types.add(type2);
        
        Map<String, Object> type3 = new HashMap<>();
        type3.put("id", 3);
        type3.put("typeName", "损坏设备");
        types.add(type3);
        
        Map<String, Object> type4 = new HashMap<>();
        type4.put("id", 4);
        type4.put("typeName", "超时使用");
        types.add(type4);
        
        Map<String, Object> type5 = new HashMap<>();
        type5.put("id", 5);
        type5.put("typeName", "其他");
        types.add(type5);
        
        return Result.success(types);
    }
}