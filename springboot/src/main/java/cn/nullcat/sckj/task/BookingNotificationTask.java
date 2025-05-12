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

        try {
            // 1. 获取即将开始的预约
            List<Booking> upcomingBookings = bookingsMapper.findUpcomingBookings();
            log.info("发现 {} 个即将开始的预约需要发送通知", upcomingBookings.size());

            for (Booking booking : upcomingBookings) {
                try {
                    log.info("发送预约即将开始通知: 预约ID={}, 标题={}", booking.getId(), booking.getTitle());
                    notificationService.sendUpcomingNotification(booking);
                } catch (Exception e) {
                    log.error("发送预约即将开始通知失败: 预约ID=" + booking.getId(), e);
                }
            }

            // 2. 获取刚刚结束的预约
            List<Booking> endedBookings = bookingsMapper.findEndedBookings();
            log.info("发现 {} 个已结束的预约需要发送通知", endedBookings.size());

            for (Booking booking : endedBookings) {
                try {
                    log.info("发送预约已结束通知: 预约ID={}, 标题={}", booking.getId(), booking.getTitle());
                    notificationService.sendEndNotification(booking);
                } catch (Exception e) {
                    log.error("发送预约已结束通知失败: 预约ID=" + booking.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查预约时间任务执行异常", e);
        }

        log.info("预约时间检查完成");
    }
}