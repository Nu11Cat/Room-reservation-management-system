package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.CreditScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/credit-score")
@Tag(name = "信誉积分管理")
public class CreditScoreController {

    @Autowired
    private CreditScoreService creditScoreService;

    @GetMapping("/history")
    @Operation(summary = "查询用户信誉分变动历史")
    public Result getScoreHistory(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        // 如果未指定用户ID，则获取当前登录用户的历史记录
        if (userId == null) {
            userId = Long.valueOf((Integer) request.getAttribute("userId"));
        }
        log.info("查询用户信誉分历史: userId={}, page={}, pageSize={}, startDate={}, endDate={}",
                userId, page, pageSize, startDate, endDate);
        PageBean pageBean = creditScoreService.getScoreHistory(userId, page, pageSize, startDate, endDate);
        return Result.success(pageBean);
    }
}