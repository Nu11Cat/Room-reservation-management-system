package cn.nullcat.sckj.service.Impl;

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
import java.util.List;

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
}
