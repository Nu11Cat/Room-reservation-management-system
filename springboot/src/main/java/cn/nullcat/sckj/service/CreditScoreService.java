package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;

import java.util.Date;

public interface CreditScoreService {

    /**
     * 获取用户信誉分变动历史
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 历史记录分页数据
     */
    PageBean getScoreHistory(Long userId, Integer page, Integer pageSize,
                            Date startDate, Date endDate);

    /**
     * 更新用户信誉分
     * @param userId 用户ID
     * @param scoreChange 分数变动
     * @param reason 变动原因
     * @param relatedId 关联ID
     * @param operatorId 操作人ID
     * @return 更新结果
     */
    Result updateUserScore(Long userId, Integer scoreChange, String reason,
                          Long relatedId, Long operatorId);
}