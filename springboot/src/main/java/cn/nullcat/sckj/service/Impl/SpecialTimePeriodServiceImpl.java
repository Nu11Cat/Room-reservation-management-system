package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.exception.BusinessException;
import cn.nullcat.sckj.mapper.SpecialTimePeriodMapper;
import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.SpecialTimePeriod;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.service.SpecialTimePeriodService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Slf4j
public class SpecialTimePeriodServiceImpl implements SpecialTimePeriodService {

    @Autowired
    private SpecialTimePeriodMapper specialTimePeriodMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageBean getSpecialTimePeriods(Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<SpecialTimePeriod> list = specialTimePeriodMapper.getAllConfigs();
        Page<SpecialTimePeriod> p = (Page<SpecialTimePeriod>) list;
        return new PageBean(p.getTotal(), p.getResult());
    }

    @Override
    public SpecialTimePeriod getCurrentWeekConfig() {
        // 获取当前周的周一日期
        LocalDate currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // 查询当前周的配置
        SpecialTimePeriod config = specialTimePeriodMapper.getByWeekStartDate(currentMonday);
        
        // 如果没有当前周的配置，则获取最近的配置
        if (config == null) {
            config = specialTimePeriodMapper.getLatestConfig();
            
            // 如果仍然没有配置，则初始化一个默认配置
            if (config == null) {
                initializeSpecialTimePeriodConfig();
                config = specialTimePeriodMapper.getByWeekStartDate(currentMonday);
            }
        }
        
        return config;
    }

    @Override
    @LogOperation(module = "特殊时间段管理", operation = "手动调整配置", description = "手动调整特殊时间段配置")
    public Result adjustTimePeriodConfig(SpecialTimePeriod specialTimePeriod) {
        // 验证参数
        if (specialTimePeriod.getTeacherAvailableCount() < specialTimePeriod.getTeacherMinReserveCount()) {
            return Result.error("教师可用数量不能小于最小预留数量");
        }
        
        if (specialTimePeriod.getTeacherAvailableCount() + specialTimePeriod.getStudentAvailableCount() > specialTimePeriod.getTotalRoomsCount()) {
            return Result.error("教师和学生可用数量之和不能大于总会议室数量");
        }
        
        // 设置调整时间
        specialTimePeriod.setAdjustmentDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 检查是否存在该周的配置
        SpecialTimePeriod existingConfig = specialTimePeriodMapper.getByWeekStartDate(LocalDate.parse(specialTimePeriod.getWeekStartDate()));
        
        if (existingConfig != null) {
            // 更新现有配置
            specialTimePeriod.setId(existingConfig.getId());
            specialTimePeriodMapper.update(specialTimePeriod);
        } else {
            // 插入新配置
            specialTimePeriodMapper.insert(specialTimePeriod);
        }
        
        return Result.success("调整成功");
    }

    @Override
    @LogOperation(module = "特殊时间段管理", operation = "自动调整配置", description = "自动调整下一周特殊时间段配置")
    public Result autoAdjustNextWeekConfig() {
        try {
            // 获取当前周的周一日期
            LocalDate currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            
            // 获取下一周的周一日期
            LocalDate nextMonday = currentMonday.plusWeeks(1);
            
            // 获取当前周的配置
            SpecialTimePeriod currentConfig = specialTimePeriodMapper.getByWeekStartDate(currentMonday);
            
            // 如果没有当前周的配置，则获取最近的配置
            if (currentConfig == null) {
                currentConfig = specialTimePeriodMapper.getLatestConfig();
                
                // 如果仍然没有配置，则初始化一个默认配置
                if (currentConfig == null) {
                    initializeSpecialTimePeriodConfig();
                    currentConfig = specialTimePeriodMapper.getByWeekStartDate(currentMonday);
                }
            }
            
            // 统计当前周教师的实际预约数量
            int teacherBookingsCount = specialTimePeriodMapper.countTeacherBookingsInWeek(currentMonday);
            
            // 统计总会议室数量
            int totalRoomsCount = specialTimePeriodMapper.countTotalRooms();
            
            // 计算下一周教师可用数量
            int nextTeacherAvailableCount = calculateNextTeacherAvailableCount(currentConfig, teacherBookingsCount);
            
            // 确保教师可用数量不低于最小预留数量
            nextTeacherAvailableCount = Math.max(nextTeacherAvailableCount, currentConfig.getTeacherMinReserveCount());
            
            // 计算学生可用数量（总数减去教师可用数量）
            int nextStudentAvailableCount = totalRoomsCount - nextTeacherAvailableCount;
            
            // 创建下一周的配置
            SpecialTimePeriod nextConfig = new SpecialTimePeriod();
            nextConfig.setWeekStartDate(nextMonday.toString());
            nextConfig.setTeacherReservedCount(0); // 初始为0，随着预约增加而更新
            nextConfig.setTeacherAvailableCount(nextTeacherAvailableCount);
            nextConfig.setTeacherMinReserveCount(currentConfig.getTeacherMinReserveCount());
            nextConfig.setStudentAvailableCount(nextStudentAvailableCount);
            nextConfig.setTotalRoomsCount(totalRoomsCount);
            nextConfig.setAdjustmentDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 检查是否已存在下一周的配置
            SpecialTimePeriod existingNextConfig = specialTimePeriodMapper.getByWeekStartDate(nextMonday);
            
            if (existingNextConfig != null) {
                // 更新现有配置
                nextConfig.setId(existingNextConfig.getId());
                specialTimePeriodMapper.update(nextConfig);
            } else {
                // 插入新配置
                specialTimePeriodMapper.insert(nextConfig);
            }
            
            log.info("自动调整下一周特殊时间段配置成功：当前周教师预约数={}, 下一周教师可用数={}, 下一周学生可用数={}",
                    teacherBookingsCount, nextTeacherAvailableCount, nextStudentAvailableCount);
            
            return Result.success("自动调整成功");
        } catch (Exception e) {
            log.error("自动调整下一周特殊时间段配置失败", e);
            return Result.error("自动调整失败：" + e.getMessage());
        }
    }

    /**
     * 计算下一周教师可用数量
     * 根据当前周教师实际预约数量动态调整
     */
    private int calculateNextTeacherAvailableCount(SpecialTimePeriod currentConfig, int teacherBookingsCount) {
        // 当前教师可用数量
        int currentTeacherAvailableCount = currentConfig.getTeacherAvailableCount();
        
        // 最小预留数量
        int minReserveCount = currentConfig.getTeacherMinReserveCount();
        
        // 如果教师实际预约数量低于当前可用数量的80%，则减少10%的可用数量
        if (teacherBookingsCount < currentTeacherAvailableCount * 0.8) {
            return Math.max((int)(currentTeacherAvailableCount * 0.9), minReserveCount);
        }
        // 如果教师实际预约数量接近或超过当前可用数量的90%，则增加10%的可用数量
        else if (teacherBookingsCount > currentTeacherAvailableCount * 0.9) {
            return (int)(currentTeacherAvailableCount * 1.1);
        }
        // 否则保持不变
        else {
            return currentTeacherAvailableCount;
        }
    }

    @Override
    public Result checkSpecialTimePeriodAvailability(Long userId, Long roomId, LocalDate startDate) {
        try {
            // 获取用户信息
            User user = userMapper.getById(Math.toIntExact(userId));
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 获取预约日期所在周的周一
            LocalDate weekMonday = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            
            // 获取该周的特殊时间段配置
            SpecialTimePeriod config = specialTimePeriodMapper.getByWeekStartDate(weekMonday);
            
            // 如果没有该周的配置，则获取最近的配置
            if (config == null) {
                config = specialTimePeriodMapper.getLatestConfig();
                
                // 如果仍然没有配置，则初始化一个默认配置
                if (config == null) {
                    initializeSpecialTimePeriodConfig();
                    config = specialTimePeriodMapper.getByWeekStartDate(weekMonday);
                }
            }
            
            // 检查用户身份和可用数量
            String identity = user.getIdentity();
            
            if ("teacher".equals(identity)) {
                // 教师预约数量已达上限
                if (config.getTeacherReservedCount() >= config.getTeacherAvailableCount()) {
                    return Result.error("本周教师预约数量已达上限，无法预约");
                }
            } else if ("student".equals(identity)) {
                // 学生预约数量已达上限
                int studentReservedCount = specialTimePeriodMapper.countStudentBookingsInWeek(weekMonday);
                if (studentReservedCount >= config.getStudentAvailableCount()) {
                    return Result.error("本周学生预约数量已达上限，无法预约");
                }
            }
            
            return Result.success("可以预约");
        } catch (Exception e) {
            log.error("检查特殊时间段可用性失败", e);
            return Result.error("检查失败：" + e.getMessage());
        }
    }

    @Override
    public void initializeSpecialTimePeriodConfig() {
        try {
            // 获取当前周的周一日期
            LocalDate currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            
            // 检查是否已存在配置
            SpecialTimePeriod existingConfig = specialTimePeriodMapper.getByWeekStartDate(currentMonday);
            if (existingConfig != null) {
                return; // 已存在配置，不需要初始化
            }
            
            // 统计总会议室数量
            int totalRoomsCount = specialTimePeriodMapper.countTotalRooms();
            
            // 默认教师可用数量为20%的总会议室数量，最少为5个
            int teacherAvailableCount = Math.max((int)(totalRoomsCount * 0.2), 5);
            
            // 学生可用数量为剩余的会议室数量
            int studentAvailableCount = totalRoomsCount - teacherAvailableCount;
            
            // 创建初始配置
            SpecialTimePeriod initialConfig = new SpecialTimePeriod();
            initialConfig.setWeekStartDate(currentMonday.toString());
            initialConfig.setTeacherReservedCount(0);
            initialConfig.setTeacherAvailableCount(teacherAvailableCount);
            initialConfig.setTeacherMinReserveCount(5); // 默认最少预留5个会议室给教师
            initialConfig.setStudentAvailableCount(studentAvailableCount);
            initialConfig.setTotalRoomsCount(totalRoomsCount);
            initialConfig.setAdjustmentDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 插入初始配置
            specialTimePeriodMapper.insert(initialConfig);
            
            log.info("初始化特殊时间段配置成功：教师可用数={}, 学生可用数={}, 总会议室数={}",
                    teacherAvailableCount, studentAvailableCount, totalRoomsCount);
        } catch (Exception e) {
            log.error("初始化特殊时间段配置失败", e);
        }
    }
}