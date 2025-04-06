package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.exception.BusinessException;
import cn.nullcat.sckj.mapper.ApprovalsMapper;
import cn.nullcat.sckj.mapper.BookingsMapper;
import cn.nullcat.sckj.mapper.RolePermissionMapper;
import cn.nullcat.sckj.mapper.SystemConfigMapper;
import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.SystemConfig;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.service.BookingsService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingsServiceImpl implements BookingsService {

    @Autowired
    private BookingsMapper bookingsMapper;
    @Autowired
    private ApprovalsMapper approvalsMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    @Autowired
    private HttpServletRequest request; // 添加这行
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
    @Override
    public PageBean getBookings(Integer page, Integer pageSize, Integer roomId, Integer userId, Integer status, LocalDate begin, LocalDate end) {
//        // 获取当前登录用户ID
//        Integer currentUserId = (Integer) request.getAttribute("userId");
//        User currentUser = userMapper.getById(currentUserId);
//
//        // 如果不是管理员或审批人，且没有指定查询用户，则只查询自己的预约
//        boolean hasManagePermission = rolePermissionMapper.hasPermission(currentUser.getRoleId(), "booking:manage");
//        boolean hasApprovalPermission = rolePermissionMapper.hasPermission(currentUser.getRoleId(), "booking:approve");
//
//        if (!hasManagePermission && !hasApprovalPermission && userId == null) {
//            userId = currentUserId;
//        }


        PageHelper.startPage(page, pageSize);
        List<Booking> list = bookingsMapper.getBookings(roomId,userId,status,begin, end);
        Page<Booking> p = (Page<Booking>) list;

        PageBean pageBean = new PageBean(p.getTotal(), p.getResult());
        return pageBean;
    }

    /**
     * 获取预约详情
     * @param id
     * @return
     */
    @Override
    public Booking getById(Integer id) {
        // 获取预约详情
        Booking booking = bookingsMapper.getById(id);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }

        // 获取当前登录用户ID
        Integer currentUserId = (Integer) request.getAttribute("userId");
        User currentUser = userMapper.getById(currentUserId);

        // 检查是否有权限查看此预约
        boolean hasPermission = hasBookingPermission(currentUser.getId(), booking.getId().longValue());
        if (!hasPermission) {
            throw new BusinessException("没有权限查看此预约");
        }
        return booking;
    }

    /**
     * 新增预约
     * @param booking
     */
    @Override
    @LogOperation(module = "预约管理", operation = "新增预约", description = "新增预约")
    public void addBooking(Booking booking) {
        // 检查时间冲突
        if (hasConflict(booking.getRoomId(), booking.getStartTime(), booking.getEndTime(), null)) {
            throw new BusinessException("该时间段已被预约");
        }
        
        // 从系统配置中获取是否需要审批
        SystemConfig approvalConfig = systemConfigMapper.getByConfigKey("APPROVAL_REQUIRED");
        boolean approvalRequired = approvalConfig != null && 
                                  ("true".equalsIgnoreCase(approvalConfig.getConfigValue()) || 
                                   "1".equals(approvalConfig.getConfigValue()));
        
        // 设置初始状态 - 根据审批配置设置
        if (approvalRequired) {
            booking.setStatus(0);  // 待审批状态
        } else {
            booking.setStatus(1);  // 直接通过状态
        }
        
        // 新增预约
        bookingsMapper.addBooking(booking);
        
        // 打印日志看看ID是否正确获取
        System.out.println("-*----------------------Generated booking ID: " + booking.getId());
        
        // 只有在需要审批的情况下才新增审批记录
        if (approvalRequired) {
            approvalsMapper.addApproval(booking.getId());
        }
    }

    /**
     * 修改预约
     * @param booking
     */
    @Override
    @LogOperation(module = "预约管理", operation = "修改预约", description = "修改预约")
    public void updateBooking(Booking booking) {
        if (hasConflict(booking.getRoomId(), booking.getStartTime(), booking.getEndTime(), booking.getId())) {
            throw new BusinessException("该时间段已被预约");
        }
        bookingsMapper.updateBooking(booking);
    }

    /**
     * 取消预约
     * @param id
     */
    @Override
    @LogOperation(module = "预约管理", operation = "取消预约", description = "取消预约")
    public void cancelBooking(Integer id) {
        bookingsMapper.cancelBooking(id);
    }


    /**
     * 检查预约时间是否冲突
     * @param roomId 会议室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeBookingId 排除的预约ID（用于修改预约时）
     * @return true-有冲突，false-无冲突
     */
    public boolean hasConflict(Long roomId, Date startTime, Date endTime, Long excludeBookingId) {
        // 1. 查询冲突的预约
        List<Booking> conflictingBookings = bookingsMapper.findConflictingBookings(roomId, startTime, endTime);

        // 2. 如果有排除的预约ID，从冲突列表中移除
        if (excludeBookingId != null) {
            conflictingBookings = conflictingBookings.stream()
                    .filter(booking -> !booking.getId().equals(excludeBookingId))
                    .collect(Collectors.toList());
        }

        // 3. 如果存在冲突的预约，返回true
        return !conflictingBookings.isEmpty();
    }
    /**
     * 检查用户是否有权限操作指定的预订记录
     */
    public boolean hasBookingPermission(Long userId, Long bookingId) {
        // 1. 获取预订记录
        Booking booking = bookingsMapper.getById(Math.toIntExact(bookingId));
        if (booking == null) {
            return false;
        }

        // 2. 检查是否是记录的创建者
        if (booking.getUserId().equals(userId)) {
            return true;
        }

        // 3. 获取用户角色
        User user = userMapper.getById(Math.toIntExact(userId));
        if (user == null) {
            return false;
        }

        // 检查是否有管理权限（管理员）
        if (rolePermissionMapper.hasPermission(user.getRoleId(), "booking:manage")) {
            return true;
        }

        // 检查是否有审批权限且记录状态为待审批
        if (rolePermissionMapper.hasPermission(user.getRoleId(), "booking:approve") &&
                booking.getStatus() == 0) { // 0表示待审批
            return true;
        }

        return false;
    }
}
