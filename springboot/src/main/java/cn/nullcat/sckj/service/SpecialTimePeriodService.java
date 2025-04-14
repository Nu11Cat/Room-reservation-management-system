package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.SpecialTimePeriod;

import java.time.LocalDate;

public interface SpecialTimePeriodService {

    /**
     * 获取特殊时间段配置列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页数据
     */
    PageBean getSpecialTimePeriods(Integer page, Integer pageSize);

    /**
     * 获取当前周的特殊时间段配置
     * @return 当前周的特殊时间段配置
     */
    SpecialTimePeriod getCurrentWeekConfig();

    /**
     * 手动调整特殊时间段配置
     * @param specialTimePeriod 特殊时间段配置
     * @return 调整结果
     */
    Result adjustTimePeriodConfig(SpecialTimePeriod specialTimePeriod);

    /**
     * 自动调整下一周的特殊时间段配置
     * 根据本周的预约情况，动态调整下一周的教师和学生可用会议室数量
     * @return 调整结果
     */
    Result autoAdjustNextWeekConfig();

    /**
     * 检查用户在特殊时间段是否可以预约会议室
     * @param userId 用户ID
     * @param roomId 会议室ID
     * @param startDate 开始日期
     * @return 检查结果
     */
    Result checkSpecialTimePeriodAvailability(Long userId, Long roomId, LocalDate startDate);

    /**
     * 初始化特殊时间段配置
     * 如果系统中没有任何配置，则创建初始配置
     */
    void initializeSpecialTimePeriodConfig();
}