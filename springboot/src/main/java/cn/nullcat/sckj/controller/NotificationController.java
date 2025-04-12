package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.Notification;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@Tag(name = "通知管理")
@RequestMapping("/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    /**
     * 获取通知列表
     * @param page
     * @param pageSize
     * @param type
     * @param is_read
     * @return
     */
    @GetMapping
    @Operation(summary ="获取通知列表")
    @RequirePermission("notification:list")
    public Result getMyNotifications(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    Integer type,
                                    Integer is_read,
                                    HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        PageBean pageBean = notificationService.getMyNotifications(page,pageSize,type,is_read,userId);
        return Result.success(pageBean);
    }
    /**
     * 标记通知已读
     * @param id
     * @return
     */
    @PutMapping("/{id}/read")
    @Operation(summary ="标记通知已读")
    @RequirePermission("notification:view")
    public Result readNotifications(@PathVariable Integer id) {
        notificationService.readNotifications(id);
        return Result.success("已标记已读");
    }

    /**
     * 获取未读通知数量
     * @param request
     * @return
     */
    @GetMapping("/unread/count")
    @Operation(summary ="获取未读通知数量")
    @RequirePermission("notification:view")
    public Result getUnreadCount(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer unread = notificationService.getUnreadCount(userId);
        return Result.success(unread);
    }

    /**
     * 发布通知
     * @param notification
     * @return
     */
    @PostMapping("/send")
    @Operation(summary ="发布通知")
    @LogOperation(module = "通知管理", operation = "发布通知", description = "发布通知")
    @RequirePermission("notification:send")
    public Result send(@RequestBody Notification notification) {
        notificationService.sendNotification(notification);
        return Result.success("发布成功");
    }
}
