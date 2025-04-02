package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.OperationLog;
import cn.nullcat.sckj.pojo.PageBean;

import java.util.Date;

public interface OperationLogService {

    void addOperationLog(OperationLog log);

    PageBean getOperationLogs(Integer page, Integer pageSize,
                              Long userId, String module, String operation,
                              Date startTime, Date endTime);
}