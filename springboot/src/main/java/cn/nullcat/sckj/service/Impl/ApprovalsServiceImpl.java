package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.ApprovalsMapper;
import cn.nullcat.sckj.mapper.BookingsMapper;
import cn.nullcat.sckj.pojo.Approval;
import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.VO.ApprovalVO;
import cn.nullcat.sckj.service.ApprovalsService;
import cn.nullcat.sckj.service.NotificationService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ApprovalsServiceImpl implements ApprovalsService {
    @Autowired
    private ApprovalsMapper approvalsMapper;
    @Autowired
    private BookingsMapper bookingsMapper;
    @Autowired
    private NotificationService notificationService;

    /**
     * 获取待审批列表
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageBean getPendingApprovals(Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<ApprovalVO> list = approvalsMapper.getPendingApprovals();
        Page<ApprovalVO> p = (Page<ApprovalVO>) list;

        PageBean pageBean = new PageBean(p.getTotal(), p.getResult());
        return pageBean;
    }

    /**
     * 审批预约
     * @param approval
     */
    @Override
    public void approval(Approval approval) {
        approvalsMapper.approval(approval);
        Long bookingId = approvalsMapper.getBookingId(approval.getId());
        bookingsMapper.bookingStatusChange(approval.getStatus(), bookingId);
        Booking booking = bookingsMapper.getById(Math.toIntExact(bookingId));
        Integer oldStatus = approval.getStatus();
        notificationService.sendStatusChangeNotification(booking, oldStatus, approval.getStatus());
    }
}
