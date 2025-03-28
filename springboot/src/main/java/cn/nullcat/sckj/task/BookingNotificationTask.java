package cn.nullcat.sckj.task;

import cn.nullcat.sckj.mapper.BookingsMapper;
import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BookingNotificationTask {
    @Autowired
    private BookingsMapper bookingsMapper;
    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void checkBookingTime() {
        log.info("开始检查预约时间...");

        // 1. 获取即将开始的预约
        List<Booking> upcomingBookings = bookingsMapper.findUpcomingBookings();
        for (Booking booking : upcomingBookings) {
            notificationService.sendUpcomingNotification(booking);
        }

        // 2. 获取刚刚结束的预约
        List<Booking> endedBookings = bookingsMapper.findEndedBookings();
        for (Booking booking : endedBookings) {
            notificationService.sendEndNotification(booking);
        }
    }
}