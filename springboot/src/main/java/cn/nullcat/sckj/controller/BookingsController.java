package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.BookingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@Tag(name = "预约管理")
@RequestMapping("/bookings")
public class BookingsController {
    @Autowired
    private BookingsService bookingsService;

    /**
     * 获取预约列表
     * @param page
     * @param pageSize
     * @param roomId
     * @param userId
     * @param status
     * @param begin
     * @param end
     * @return
     */
    @GetMapping
    @RequirePermission("booking:list")
    @Operation(summary ="获取预约列表")
    public Result getBookings(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              Integer roomId,
                              Integer userId,
                              Integer status,
                              @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                              @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("全部分页条件查询:{},{},{},{},{},{},{}",page,pageSize,roomId,userId,status,begin,end);
        PageBean pageBean = bookingsService.getBookings(page,pageSize,roomId,userId,status,begin,end);
        return Result.success(pageBean);
    }

    /**
     * 根据id获取预约详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @RequirePermission("booking:view")
    @Operation(summary ="根据id获取预约详情")
    public Result getBookings(@PathVariable Integer id) {
        Booking booking = bookingsService.getById(id);
        return Result.success(booking);
    }

    /**
     * 新增预约
     * @param booking
     * @return
     */
    @PostMapping
    @RequirePermission("booking:add")
    @Operation(summary ="新增预约")
    public Result addBooking(@RequestBody Booking booking, HttpServletRequest request) {
        Integer userIdNow = (Integer) request.getAttribute("userId");
        booking.setUserId(Long.valueOf(userIdNow));
        bookingsService.addBooking(booking);
        return Result.success("预约成功");
    }

    /**
     * 修改预约
     * @param id
     * @param booking
     * @return
     */
    @PutMapping("/{id}")
    @RequirePermission("booking:edit")
    @Operation(summary ="修改预约")
    public Result updateBooking(@PathVariable Long id,@RequestBody Booking booking, HttpServletRequest request) {
        Integer userIdNow = (Integer) request.getAttribute("userId");
        booking.setUserId(Long.valueOf(userIdNow));
        booking.setId(id);
        bookingsService.updateBooking(booking);
        return Result.success("修改成功");
    }

    /**
     * 取消预约
     * @param id
     * @return
     */
    @PutMapping("/{id}/cancel")
    @RequirePermission("booking:cancel")
    @Operation(summary ="取消预约")
    public Result cancelBooking(@PathVariable Integer id) {
        bookingsService.cancelBooking(id);
        return Result.success("取消成功");
    }
}
