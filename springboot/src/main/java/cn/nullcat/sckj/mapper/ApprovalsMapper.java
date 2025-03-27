package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Approval;
import cn.nullcat.sckj.pojo.VO.ApprovalVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApprovalsMapper {
    /**
     * 获取待审批列表
     * @return
     */
    List<ApprovalVO> getPendingApprovals();

    /**
     * 审批预约
     * @param approval
     */
    @Update("update approval " +
            "set " +
            "status = #{status}," +
            "comment = #{comment}," +
            "update_time = NOW()," +
            "approver_id = #{approverId}" +
            " where id = #{id}")
    void approval(Approval approval);

    /**
     * 新增待审核
     * @param bookingId
     */
    @Insert("insert into approval(booking_id,status, create_time, update_time, is_deleted) " +
            "VALUES(#{bookingId},0, NOW(), NOW(), 0)")
    void addApproval(Long bookingId);

    /**
     *
     * @param id
     * @return
     */
    @Select("select booking_id from approval where id = #{id}")
    Long getBookingId(Long id);
}
