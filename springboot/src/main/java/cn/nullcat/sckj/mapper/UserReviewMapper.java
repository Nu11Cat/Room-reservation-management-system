package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.UserReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 用户评价数据访问接口
 */
@Mapper
public interface UserReviewMapper {

    /**
     * 插入评价记录
     * @param userReview 评价信息
     * @return 影响的行数
     */
    int insert(UserReview userReview);
    
    /**
     * 根据ID查询评价
     * @param id 评价ID
     * @return 评价信息
     */
    UserReview selectById(Long id);
    
    /**
     * 更新评价信息
     * @param userReview 评价信息
     * @return 影响的行数
     */
    int updateById(UserReview userReview);
    
    /**
     * 删除评价
     * @param id 评价ID
     * @return 影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 查询用户发出的评价列表
     * @param userId 用户ID
     * @param offset 分页偏移
     * @param limit 分页大小
     * @param reviewType 评价类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 评价列表
     */
    List<UserReview> selectOutgoingReviews(
        @Param("userId") Long userId,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("reviewType") Integer reviewType,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 查询用户发出的评价总数
     * @param userId 用户ID
     * @param reviewType 评价类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 评价总数
     */
    Long countOutgoingReviews(
        @Param("userId") Long userId,
        @Param("reviewType") Integer reviewType,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 查询用户收到的评价列表
     * @param userId 用户ID
     * @param offset 分页偏移
     * @param limit 分页大小
     * @param reviewType 评价类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 评价列表
     */
    List<UserReview> selectIncomingReviews(
        @Param("userId") Long userId,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("reviewType") Integer reviewType,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 查询用户收到的评价总数
     * @param userId 用户ID
     * @param reviewType 评价类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 评价总数
     */
    Long countIncomingReviews(
        @Param("userId") Long userId,
        @Param("reviewType") Integer reviewType,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
}
