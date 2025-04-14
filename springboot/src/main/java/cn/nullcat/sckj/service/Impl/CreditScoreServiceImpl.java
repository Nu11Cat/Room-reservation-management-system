package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.mapper.CreditScoreMapper;
import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.pojo.CreditScoreLog;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.service.CreditScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CreditScoreServiceImpl implements CreditScoreService {

    @Autowired
    private CreditScoreMapper creditScoreMapper;

    @Autowired
    private UserMapper userMapper;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageBean getScoreHistory(Long userId, Integer page, Integer pageSize,
                                   Date startDate, Date endDate) {
        // 计算分页偏移
        int offset = (page - 1) * pageSize;

        // 查询历史记录
        List<CreditScoreLog> logs = creditScoreMapper.selectUserScoreHistory(
                userId, offset, pageSize, startDate, endDate);

        // 查询总记录数
        Long total = creditScoreMapper.countUserScoreHistory(userId, startDate, endDate);

        // 返回分页数据
        return new PageBean(total, logs);
    }

    @Override
    @Transactional
    public Result updateUserScore(Long userId, Integer scoreChange, String reason,
                                 Long relatedId, Long operatorId) {
        // 获取用户当前信誉分
        User user = userMapper.getById(Math.toIntExact(userId));
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 计算新的信誉分
        int previousScore = user.getCreditScore();
        int newScore = previousScore + scoreChange;

        // 确保信誉分在有效范围内（0-100）
        newScore = Math.max(0, Math.min(100, newScore));

        // 更新用户信誉分
        user.setCreditScore(newScore);
        userMapper.updateInfo(user);

        // 记录变动日志
        CreditScoreLog log = new CreditScoreLog();
        log.setUserId(userId);
        log.setScoreChange(scoreChange);
        log.setPreviousScore(previousScore);
        log.setCurrentScore(newScore);
        log.setChangeReason(reason);
        log.setRelatedId(relatedId);
        log.setOperatorId(operatorId);
        log.setCreateTime(dateFormat.format(new Date()));

        creditScoreMapper.insertScoreLog(log);

        // 返回更新后的信誉分信息
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("previousScore", previousScore);
        data.put("currentScore", newScore);
        data.put("scoreChange", scoreChange);
        return Result.success(data);
    }
}