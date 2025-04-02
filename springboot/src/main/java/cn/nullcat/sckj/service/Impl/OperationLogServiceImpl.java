// 创建 cn.nullcat.sckj.service.Impl.OperationLogServiceImpl 类
package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.OperationLogMapper;
import cn.nullcat.sckj.pojo.OperationLog;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.service.OperationLogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public void addOperationLog(OperationLog log) {
        log.setCreateTime(new Date());
        operationLogMapper.insert(log);
    }

    @Override
    public PageBean getOperationLogs(Integer page, Integer pageSize,
                                     Long userId, String module, String operation,
                                     Date startTime, Date endTime) {
        PageHelper.startPage(page, pageSize);
        List<OperationLog> list = operationLogMapper.getOperationLogs(userId, module, operation, startTime, endTime);
        Page<OperationLog> p = (Page<OperationLog>) list;

        PageBean pageBean = new PageBean(p.getTotal(), p.getResult());
        return pageBean;
    }


    @Override
    public List<String> findAllModules() {
        return operationLogMapper.findAllModules();
    }

    @Override
    public List<String> findAllOperationTypes() {
        return operationLogMapper.findAllOperationTypes();
    }
}