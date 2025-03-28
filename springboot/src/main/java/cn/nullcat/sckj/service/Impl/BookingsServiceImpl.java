package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.exception.BusinessException;
import cn.nullcat.sckj.mapper.ApprovalsMapper;
import cn.nullcat.sckj.mapper.BookingsMapper;
import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.service.BookingsService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
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
        Booking booking = bookingsMapper.getById(id);
        return booking;
    }

    /**
     * 新增预约
     * @param booking
     */
    @Override
    public void addBooking(Booking booking) {
        // 1. 设置初始状态
        booking.setStatus(0);  // 待审批状态
        // 检查时间冲突
        if (hasConflict(booking.getRoomId(), booking.getStartTime(), booking.getEndTime(), null)) {
            throw new BusinessException("该时间段已被预约");
        }
        // 2. 新增预约
        bookingsMapper.addBooking(booking);

        // 3. 打印日志看看ID是否正确获取
        System.out.println("-*----------------------Generated booking ID: " + booking.getId());

        // 4. 新增审批记录
        approvalsMapper.addApproval(booking.getId());
    }

    /**
     * 修改预约
     * @param booking
     */
    @Override
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
}
