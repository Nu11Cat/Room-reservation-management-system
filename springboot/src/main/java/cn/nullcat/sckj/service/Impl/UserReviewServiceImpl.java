package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.BookingsMapper;
import cn.nullcat.sckj.mapper.MisconductTypeMapper;
import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.mapper.UserReviewMapper;
import cn.nullcat.sckj.pojo.*;
import cn.nullcat.sckj.service.UserReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户评价服务实现类
 */
@Slf4j
@Service
public class UserReviewServiceImpl implements UserReviewService {

    @Autowired
    private UserReviewMapper userReviewMapper;

    @Autowired
    private UserMapper userMapper;

   // @Autowired
    //private BookingMapper bookingMapper;
    @Autowired
    private BookingsMapper bookingsMapper;

    @Override
    @Transactional
    public Long submitReview(UserReview userReview) {
        // 验证评价对象和预订信息
        if (userReview.getBookingId() == null) {
            throw new RuntimeException("预订ID不能为空");
        }

        // 获取预订信息
        Booking booking = bookingsMapper.selectById(userReview.getBookingId());
        if (booking == null) {
            throw new RuntimeException("预订信息不存在");
        }

        // 验证评价时间窗口（预订结束后24小时内）
        Date now = new Date();
        long timeDiff = now.getTime() - booking.getEndTime().getTime();
        if (timeDiff < 0) {
            throw new RuntimeException("预订尚未结束，不能评价");
        }
        if (timeDiff > 24 * 60 * 60 * 1000) {
            throw new RuntimeException("评价时间已超过预订结束后24小时");
        }

        // 验证评价权限（只能评价上一个使用者）
        if (!booking.getUserId().equals(userReview.getReviewerId()) &&
            !booking.getUserId().equals(userReview.getReviewedUserId())) {
            throw new RuntimeException("只能评价与预订相关的用户");
        }

        // 设置创建时间和更新时间
        userReview.setCreateTime(now);
        userReview.setUpdateTime(now);

        // 设置默认未处理状态
        userReview.setIsProcessed(0);
        
        // 处理不文明行为类型列表
        if (userReview.getMisconductTypeList() != null && !userReview.getMisconductTypeList().isEmpty()) {
            // 验证评分和不文明行为类型的匹配
            if (userReview.getReviewScore() >= 3) {
                throw new RuntimeException("评分大于等于3分时不能选择不文明行为类型");
            }
            // 将List转换为JSON字符串
            String misconductTypesJson = com.alibaba.fastjson.JSON.toJSONString(userReview.getMisconductTypeList());
            userReview.setMisconductTypes(misconductTypesJson);
        } else if (userReview.getReviewScore() < 3) {
            throw new RuntimeException("评分小于3分时必须选择不文明行为类型");
        }

        // 保存评价到数据库
        int rows = userReviewMapper.insert(userReview);
        if (rows <= 0) {
            log.error("保存评价失败: {}", userReview);
            throw new RuntimeException("保存评价失败");
        }

        log.info("提交评价成功，ID: {}, 评分: {}, 类型: {}, 不文明行为类型: {}",
                userReview.getId(), userReview.getReviewScore(), userReview.getReviewType(), userReview.getMisconductTypes());

        return userReview.getId();
    }

    @Override
    public PageBean getOutgoingReviews(Long userId, Integer page, Integer pageSize,
                                       Integer reviewType, Date startDate, Date endDate) {
        log.info("查询用户发出的评价, 用户ID: {}, 页码: {}, 每页大小: {}", userId, page, pageSize);

        // 计算分页偏移量
        int offset = (page - 1) * pageSize;

        // 查询用户发出的评价列表
        List<UserReview> reviews = userReviewMapper.selectOutgoingReviews(
                userId, offset, pageSize, reviewType, startDate, endDate);

        // 处理评价列表（如转换evidenceUrls字符串为列表等）
        processReviewList(reviews);

        // 查询总记录数
        Long total = userReviewMapper.countOutgoingReviews(
                userId, reviewType, startDate, endDate);

        log.info("查询到用户 {} 发出的评价 {} 条，总记录数: {}", userId, reviews.size(), total);

        // 封装分页结果
        PageBean pageBean = new PageBean();
        pageBean.setTotal(total);
        pageBean.setRows(reviews);

        return pageBean;
    }

    @Override
    public PageBean getIncomingReviews(Long userId, Integer page, Integer pageSize,
                                       Integer reviewType, Date startDate, Date endDate) {
        log.info("查询用户收到的评价, 用户ID: {}, 页码: {}, 每页大小: {}", userId, page, pageSize);

        // 计算分页偏移量
        int offset = (page - 1) * pageSize;

        // 查询用户收到的评价列表
        List<UserReview> reviews = userReviewMapper.selectIncomingReviews(
                userId, offset, pageSize, reviewType, startDate, endDate);

        // 处理评价列表（如转换evidenceUrls字符串为列表等）
        processReviewList(reviews);

        // 查询总记录数
        Long total = userReviewMapper.countIncomingReviews(
                userId, reviewType, startDate, endDate);

        log.info("查询到用户 {} 收到的评价 {} 条，总记录数: {}", userId, reviews.size(), total);

        // 封装分页结果
        PageBean pageBean = new PageBean();
        pageBean.setTotal(total);
        pageBean.setRows(reviews);

        return pageBean;
    }

    @Override
    public UserReview getReviewById(Long id) {
        log.info("根据ID查询评价详情, ID: {}", id);

        // 查询评价
        UserReview review = userReviewMapper.selectById(id);

        // 处理评价数据
        if (review != null) {
            processReview(review);
            log.info("查询到评价详情: {}", review);
        } else {
            log.warn("评价不存在, ID: {}", id);
        }

        return review;
    }

    @Override
    @Transactional
    public boolean updateReview(UserReview userReview) {
        log.info("更新评价, ID: {}, 内容: {}", userReview.getId(), userReview);

        // 设置更新时间
        userReview.setUpdateTime(new Date());
        
        // 处理不文明行为类型列表
        if (userReview.getMisconductTypeList() != null && !userReview.getMisconductTypeList().isEmpty()) {
            // 将List转换为JSON字符串
            String misconductTypesJson = com.alibaba.fastjson.JSON.toJSONString(userReview.getMisconductTypeList());
            userReview.setMisconductTypes(misconductTypesJson);
        }

        // 执行更新
        int rows = userReviewMapper.updateById(userReview);

        boolean success = rows > 0;
        if (success) {
            log.info("评价更新成功, ID: {}", userReview.getId());
        } else {
            log.warn("评价更新失败, ID: {}", userReview.getId());
        }

        return success;
    }

    @Override
    @Transactional
    public boolean deleteReview(Long reviewId) {
        log.info("删除评价, ID: {}", reviewId);

        // 执行删除
        int rows = userReviewMapper.deleteById(reviewId);

        boolean success = rows > 0;
        if (success) {
            log.info("评价删除成功, ID: {}", reviewId);
        } else {
            log.warn("评价删除失败, ID: {}", reviewId);
        }

        return success;
    }

    @Override
    public boolean isUserAdmin(Long userId) {
        log.info("检查用户是否为管理员, 用户ID: {}", userId);

        // 查询用户信息
        User user = userMapper.getById(Math.toIntExact(userId));

        // 检查用户角色是否为管理员（角色ID为1或2的是管理员）
        boolean isAdmin = user != null && (user.getRoleId() == 1 || user.getRoleId() == 2);

        log.info("用户 {} 是管理员: {}", userId, isAdmin);

        return isAdmin;
    }
    
    @Autowired
    private MisconductTypeMapper misconductTypeMapper;

    /**
     * 计算不文明行为类型对信誉分的影响
     * @param misconductTypes 不文明行为类型列表
     * @return 信誉分影响值
     */
    private int calculateCreditImpact(List<Integer> misconductTypes) {
        if (misconductTypes == null || misconductTypes.isEmpty()) {
            return 0;
        }
        
        int totalImpact = 0;
        for (Integer typeId : misconductTypes) {
            MisconductType type = misconductTypeMapper.getTypeById(typeId);
            if (type != null) {
                totalImpact += type.getDefaultScoreImpact();
            }
        }
        
        return totalImpact;
    }
    
    /**
     * 处理评价并计算信誉分影响
     * @param reviewId 评价ID
     * @param processResult 处理结果（1-通过，0-不通过）
     * @param processNote 处理备注
     * @return 处理结果
     */
    @Override
    @Transactional
    public boolean processReview(Long reviewId, Integer processResult, String processNote) {
        log.info("处理评价, ID: {}, 处理结果: {}, 处理备注: {}", reviewId, processResult, processNote);
        
        // 查询评价信息
        UserReview review = userReviewMapper.selectById(reviewId);
        if (review == null) {
            log.error("评价不存在, ID: {}", reviewId);
            return false;
        }
        
        // 如果已经处理过，则不再处理
        if (review.getIsProcessed() == 1) {
            log.warn("评价已处理, ID: {}", reviewId);
            return false;
        }
        
        // 处理评价
        review.setIsProcessed(1);
        review.setProcessTime(new Date());
        review.setProcessResult(processResult);
        review.setProcessNote(processNote);
        review.setUpdateTime(new Date());
        
        // 处理不文明行为类型并计算信誉分影响
        List<Integer> misconductTypeList = null;
        if (review.getMisconductTypes() != null && !review.getMisconductTypes().isEmpty()) {
            try {
                misconductTypeList = com.alibaba.fastjson.JSON.parseArray(review.getMisconductTypes(), Integer.class);
                review.setMisconductTypeList(misconductTypeList);
            } catch (Exception e) {
                log.error("解析不文明行为类型列表失败: {}", e.getMessage());
            }
        }
        
        // 只有当处理结果为通过且是差评时，才计算信誉分影响
        if (processResult == 1 && review.getReviewType() == 2 && misconductTypeList != null && !misconductTypeList.isEmpty()) {
            int creditImpact = calculateCreditImpact(misconductTypeList);
            review.setCreditImpact(creditImpact);
            log.info("计算信誉分影响: {}, 不文明行为类型: {}", creditImpact, review.getMisconductTypes());
        } else {
            review.setCreditImpact(0);
        }
        
        // 更新评价
        int rows = userReviewMapper.updateById(review);
        boolean success = rows > 0;
        
        if (success) {
            log.info("评价处理成功, ID: {}", reviewId);
        } else {
            log.error("评价处理失败, ID: {}", reviewId);
        }
        
        return success;
    }
    
    /**
     * 批量处理评价
     * @param reviewIds 评价ID列表
     * @param processResult 处理结果（1-通过，0-不通过）
     * @param processNote 处理备注
     * @return 处理结果
     */
    @Override
    @Transactional
    public boolean batchProcessReview(List<Long> reviewIds, Integer processResult, String processNote) {
        log.info("批量处理评价, 评价数量: {}, 处理结果: {}, 处理备注: {}", reviewIds.size(), processResult, processNote);
        
        if (reviewIds == null || reviewIds.isEmpty()) {
            return false;
        }
        
        boolean allSuccess = true;
        for (Long reviewId : reviewIds) {
            boolean success = processReview(reviewId, processResult, processNote);
            if (!success) {
                allSuccess = false;
                log.warn("批量处理评价失败, ID: {}", reviewId);
            }
        }
        
        return allSuccess;
    }
    
    /**
     * 撤销处理评价
     * @param reviewId 评价ID
     * @return 处理结果
     */
    @Override
    @Transactional
    public boolean undoProcessReview(Long reviewId) {
        log.info("撤销处理评价, ID: {}", reviewId);
        
        // 查询评价信息
        UserReview review = userReviewMapper.selectById(reviewId);
        if (review == null) {
            log.error("评价不存在, ID: {}", reviewId);
            return false;
        }
        
        // 如果未处理，则不需要撤销
        if (review.getIsProcessed() == 0) {
            log.warn("评价未处理, 无需撤销, ID: {}", reviewId);
            return false;
        }
        
        // 撤销处理
        review.setIsProcessed(0);
        review.setProcessTime(null);
        review.setProcessResult(null);
        review.setProcessNote(null);
        review.setCreditImpact(null);
        review.setUpdateTime(new Date());
        
        // 更新评价
        int rows = userReviewMapper.updateById(review);
        boolean success = rows > 0;
        
        if (success) {
            log.info("评价撤销处理成功, ID: {}", reviewId);
        } else {
            log.error("评价撤销处理失败, ID: {}", reviewId);
        }
        
        return success;
    }
    
    /**
     * 获取评价统计信息
     * @return 统计信息
     */
    @Override
    public Map<String, Long> getReviewStatistics() {
        log.info("获取评价统计信息");
        
        Map<String, Long> statistics = new HashMap<>();
        
        // 查询待处理评价数量
        Long pendingCount = userReviewMapper.countByProcessed(0);
        statistics.put("pendingCount", pendingCount);
        
        // 查询已处理评价数量
        Long processedCount = userReviewMapper.countByProcessed(1);
        statistics.put("processedCount", processedCount);
        
        // 查询总评价数量
        Long totalCount = pendingCount + processedCount;
        statistics.put("totalCount", totalCount);
        
        log.info("评价统计信息: {}", statistics);
        
        return statistics;
    }

    /**
     * 处理评价列表的附加数据
     * @param reviews 评价列表
     */
    private void processReviewList(List<UserReview> reviews) {
        if (reviews != null && !reviews.isEmpty()) {
            for (UserReview review : reviews) {
                processReview(review);
            }
        }
    }

    /**
     * 处理单个评价的附加数据
     * @param review 评价对象
     */
    private void processReview(UserReview review) {
        if (review != null) {
            // 处理证据材料URL列表
            if (review.getEvidenceUrls() != null && !review.getEvidenceUrls().isEmpty()) {
                List<String> urlList = Arrays.asList(review.getEvidenceUrls().split(","));
                review.setEvidenceUrlList(urlList);
            }
            
            // 处理不文明行为类型列表
            if (review.getMisconductTypes() != null && !review.getMisconductTypes().isEmpty()) {
                List<Integer> typeList = com.alibaba.fastjson.JSON.parseArray(review.getMisconductTypes(), Integer.class);
                review.setMisconductTypeList(typeList);
            }
        }
    }
}