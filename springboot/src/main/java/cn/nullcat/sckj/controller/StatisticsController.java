package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.DTO.*;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@Tag(name = "数据统计")
@RequestMapping("/statistics")
public class StatisticsController {
    @Autowired
    private StatisticsService statisticsService;

    /**
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/room-usage")
    @RequirePermission("report:view")
    @Operation(summary ="获取会议室使用率")

    public Result getRoomUsageStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        // 如果没有指定日期范围，默认为最近30天
        if (startDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            endDate = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            startDate = cal.getTime();
        }

        List<RoomUsageDTO> data = statisticsService.getRoomUsageStatistics(startDate, endDate);
        return Result.success(data);
    }

    @GetMapping("/booking-status")
    @RequirePermission("report:view")
    @Operation(summary ="获取预约状态统计")
    public Result getBookingStatusStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        // 如果没有指定日期范围，默认为最近30天
        if (startDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            endDate = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            startDate = cal.getTime();
        }

        List<BookingStatusDTO> data = statisticsService.getBookingStatusStatistics(startDate, endDate);
        return Result.success(data);
    }

    @GetMapping("/user-ranking")
    @RequirePermission("report:view")
    @Operation(summary ="获取用户预约排名")

    public Result getUserBookingRanking(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        // 如果没有指定日期范围，默认为最近30天
        if (startDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            endDate = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            startDate = cal.getTime();
        }

        List<UserRankingDTO> data = statisticsService.getUserBookingRanking(startDate, endDate, limit);
        return Result.success(data);
    }

    @GetMapping("/meeting-duration")
    @RequirePermission("report:view")
    @Operation(summary ="获取会议持续时间统计信息")
    public Result getMeetingDurationStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        // 如果没有指定日期范围，默认为最近30天
        if (startDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            endDate = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            startDate = cal.getTime();
        }

        List<MeetingDurationDTO> data = statisticsService.getMeetingDurationStatistics(startDate, endDate);
        return Result.success(data);
    }

    @GetMapping("/all")
    @Operation(summary ="获取全部数据")
    @RequirePermission("report:view")
    public Result getAllStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        // 如果没有指定日期范围，默认为最近30天
        if (startDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            endDate = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            startDate = cal.getTime();
        }

        AllStatisticsDTO data = statisticsService.getAllStatistics(startDate, endDate);
        return Result.success(data);
    }
}