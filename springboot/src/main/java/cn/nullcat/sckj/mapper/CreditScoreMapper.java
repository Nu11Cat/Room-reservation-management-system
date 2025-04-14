package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.CreditScoreLog;
import cn.nullcat.sckj.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CreditScoreMapper {

    /**
     * 插入信誉分变动记录
     * @param log 变动记录
     * @return 影响行数
     */
    int insertScoreLog(CreditScoreLog log);

    /**
     * 查询用户信誉分变动历史
     * @param userId 用户ID
     * @param offset 分页偏移
     * @param limit 分页大小
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 变动记录列表
     */
    List<CreditScoreLog> selectUserScoreHistory(
        @Param("userId") Long userId,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );

    /**
     * 统计用户信誉分变动记录总数
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 记录总数
     */
    Long countUserScoreHistory(
        @Param("userId") Long userId,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
}