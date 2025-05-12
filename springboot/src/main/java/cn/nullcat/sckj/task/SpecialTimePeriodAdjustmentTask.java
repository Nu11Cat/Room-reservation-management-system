package cn.nullcat.sckj.task;

import cn.nullcat.sckj.service.SpecialTimePeriodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 特殊时间段预约调整定时任务
 * 负责周期性检查和自动调整教师/学生可用会议室数量
 */
@Component
@Slf4j
public class SpecialTimePeriodAdjustmentTask {

    @Autowired
    private SpecialTimePeriodService specialTimePeriodService;

    /**
     * 每周日晚上23:00自动调整下一周的特殊时间段配置
     * 根据本周的预约情况，动态调整下一周的教师和学生可用会议室数量
     */
    @Scheduled(cron = "0 0 23 * * 0")
    public void autoAdjustWeeklyConfig() {
        log.info("开始执行每周特殊时间段自动调整任务...");
        
        try {
            specialTimePeriodService.autoAdjustNextWeekConfig();
            log.info("每周特殊时间段自动调整任务执行成功");
        } catch (Exception e) {
            log.error("每周特殊时间段自动调整任务执行失败", e);
        }
    }

    /**
     * 每天凌晨2:00检查并初始化特殊时间段配置
     * 确保系统中始终有有效的特殊时间段配置
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkAndInitializeConfig() {
        log.info("开始检查特殊时间段配置...");
        
        try {
            // 获取当前配置，如果不存在则会自动初始化
            specialTimePeriodService.getCurrentWeekConfig();
            log.info("特殊时间段配置检查完成");
        } catch (Exception e) {
            log.error("特殊时间段配置检查失败", e);
        }
    }
}