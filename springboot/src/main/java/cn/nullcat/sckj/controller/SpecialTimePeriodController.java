package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.SpecialTimePeriod;
import cn.nullcat.sckj.service.SpecialTimePeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/special-time-period")
@Tag(name = "特殊时间段管理")
public class SpecialTimePeriodController {

    @Autowired
    private SpecialTimePeriodService specialTimePeriodService;

    /**
     * 获取特殊时间段配置列表
     */
    @GetMapping
    @Operation(summary = "获取特殊时间段配置列表")
    public Result getSpecialTimePeriods(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取特殊时间段配置列表: page={}, pageSize={}", page, pageSize);
        PageBean pageBean = specialTimePeriodService.getSpecialTimePeriods(page, pageSize);
        return Result.success(pageBean);
    }

    /**
     * 获取当前周的特殊时间段配置
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前周的特殊时间段配置")
    public Result getCurrentWeekConfig() {
        log.info("获取当前周的特殊时间段配置");
        SpecialTimePeriod config = specialTimePeriodService.getCurrentWeekConfig();
        return Result.success(config);
    }

    /**
     * 手动调整特殊时间段配置
     */
    @PostMapping("/adjust")
    //@RequirePermission("special-time:adjust")
    @Operation(summary = "手动调整特殊时间段配置")
    public Result adjustTimePeriodConfig(@RequestBody SpecialTimePeriod specialTimePeriod) {
        log.info("手动调整特殊时间段配置: {}", specialTimePeriod);
        return specialTimePeriodService.adjustTimePeriodConfig(specialTimePeriod);
    }

    /**
     * 自动调整下一周的特殊时间段配置
     */
    @PostMapping("/auto-adjust")
    @Operation(summary = "自动调整下一周的特殊时间段配置")
    public Result autoAdjustNextWeekConfig() {
        log.info("自动调整下一周的特殊时间段配置");
        return specialTimePeriodService.autoAdjustNextWeekConfig();
    }

    /**
     * 检查用户在特殊时间段是否可以预约会议室
     */
    @GetMapping("/check")
    @Operation(summary = "检查用户在特殊时间段是否可以预约会议室")
    public Result checkSpecialTimePeriodAvailability(
            @RequestParam Long userId,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate) {
        log.info("检查特殊时间段可用性: userId={}, roomId={}, startDate={}", userId, roomId, startDate);
        return specialTimePeriodService.checkSpecialTimePeriodAvailability(userId, roomId, startDate);
    }

    /**
     * 初始化特殊时间段配置
     */
    @PostMapping("/initialize")
    @Operation(summary = "初始化特殊时间段配置")
    public Result initializeSpecialTimePeriodConfig() {
        log.info("初始化特殊时间段配置");
        specialTimePeriodService.initializeSpecialTimePeriodConfig();
        return Result.success("初始化成功");
    }
}