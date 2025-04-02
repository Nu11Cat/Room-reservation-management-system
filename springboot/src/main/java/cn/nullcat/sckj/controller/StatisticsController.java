package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.DTO.RoomUsageDTO;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.StatisticsService;
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
@RequestMapping("/statistics")
public class StatisticsController {
    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/room-usage")
    @RequirePermission("report:view")
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

}