package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.mapper.UserMapper;
import cn.nullcat.sckj.mapper.UserReviewMapper;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.pojo.UserReview;
import cn.nullcat.sckj.service.UserReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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

    @Override
    @Transactional
    public Long submitReview(UserReview userReview) {
        // 设置创建时间和更新时间
        Date now = new Date();
        userReview.setCreateTime(now);
        userReview.setUpdateTime(now);

        // 设置默认未处理状态
        userReview.setIsProcessed(0);

        // 保存评价到数据库
        int rows = userReviewMapper.insert(userReview);
        if (rows <= 0) {
            log.error("保存评价失败: {}", userReview);
            throw new RuntimeException("保存评价失败");
        }

        log.info("提交评价成功，ID: {}, 评分: {}, 类型: {}",
                userReview.getId(), userReview.getReviewScore(), userReview.getReviewType());

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
        // 处理证据URL列表
        if (review.getEvidenceUrls() != null && !review.getEvidenceUrls().isEmpty()) {
            List<String> urlList = Arrays.stream(review.getEvidenceUrls().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            review.setEvidenceUrlList(urlList);
        }
    }
} 