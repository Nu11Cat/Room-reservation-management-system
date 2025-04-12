// 创建 cn.nullcat.sckj.controller.SystemLogController 类
package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/system")
@Tag(name = "系统操作日志")
public class SystemLogController {

    @Autowired
    private OperationLogService logService;

    /**
     *
     * @param page
     * @param pageSize
     * @param userId
     * @param module
     * @param operation
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/logs")
    @Operation(summary ="分页条件获取操作日志列表")
    @RequirePermission("system:config:view")
    public Result getOperationLogs(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   Long userId, String module, String operation,
                                   @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
                                   @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        PageBean pageBean = logService.getOperationLogs(
                page, pageSize, userId, module, operation, startTime, endTime);
        return Result.success(pageBean);
    }
    /**
     * 获取所有日志操作模块
     */
    @GetMapping("/log-modules")
    @RequirePermission("system:config:view")
    @Operation(summary ="获取所有日志操作模块")

    public Result getLogModules() {
        List<String> modules = logService.findAllModules();
        return Result.success(modules);
    }

    /**
     * 获取所有日志操作类型
     */
    @Operation(summary ="获取所有日志操作类型")
    @GetMapping("/log-operations")
    @RequirePermission("system:config:view")
    public Result getLogOperations() {
        List<String> operations = logService.findAllOperationTypes();
        return Result.success(operations);
    }

}