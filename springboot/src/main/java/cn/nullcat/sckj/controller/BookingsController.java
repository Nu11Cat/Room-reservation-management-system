package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.BookingsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
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
    public Result updateBooking(@PathVariable Long id,@RequestBody Booking booking, HttpServletRequest request) {
        Integer userIdNow = (Integer) request.getAttribute("userId");
        booking.setUserId(Long.valueOf(userIdNow));
        booking.setId(id);
        bookingsService.updateBooking(booking);
        return Result.success("修改成功");
    }

    /**
     * 取消预约
     *
     * @param id
     * @return
     */
    @PutMapping("/{id}/cancel")
    public Result cancelBooking(@PathVariable Integer id) {
        bookingsService.cancelBooking(id);
        return Result.success("取消成功");
    }
}
