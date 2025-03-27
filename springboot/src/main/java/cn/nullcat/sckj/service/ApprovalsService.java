package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.Approval;
import cn.nullcat.sckj.pojo.PageBean;

public interface ApprovalsService {

    PageBean getPendingApprovals(Integer page, Integer pageSize);

    void approval(Approval approval);
}
