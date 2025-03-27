package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.pojo.Approval;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.ApprovalsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/approvals")
public class ApprovalsController {
    @Autowired
    private ApprovalsService approvalsService;

    /**
     *  获取待审批列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/pending")
    public Result getPendingApprovals(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        PageBean pageBean = approvalsService.getPendingApprovals(page,pageSize);
        return Result.success(pageBean);
    }

    /**
     * 审批预约
     * @param approval
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public Result approval(@RequestBody Approval approval, @PathVariable Long id, HttpServletRequest request) {
        Integer userIdNow = (Integer) request.getAttribute("userId");
        approval.setId(id);
        approval.setApproverId(Long.valueOf(userIdNow));
        approvalsService.approval(approval);
        return Result.success("审批成功");
    }

}
