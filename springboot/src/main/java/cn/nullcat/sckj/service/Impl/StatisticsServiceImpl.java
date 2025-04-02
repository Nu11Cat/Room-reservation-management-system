package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.pojo.DTO.*;
import cn.nullcat.sckj.mapper.StatisticsMapper;
import cn.nullcat.sckj.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public List<RoomUsageDTO> getRoomUsageStatistics(Date startDate, Date endDate) {
        return statisticsMapper.getRoomUsageStatistics(startDate, endDate);
    }
    
    @Override
    public List<BookingStatusDTO> getBookingStatusStatistics(Date startDate, Date endDate) {
        return statisticsMapper.getBookingStatusStatistics(startDate, endDate);
    }
    
    @Override
    public List<UserRankingDTO> getUserBookingRanking(Date startDate, Date endDate, Integer limit) {
        // 默认获取前10名
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return statisticsMapper.getUserBookingRanking(startDate, endDate, limit);
    }
    
    @Override
    public List<MeetingDurationDTO> getMeetingDurationStatistics(Date startDate, Date endDate) {
        return statisticsMapper.getMeetingDurationStatistics(startDate, endDate);
    }
    
    @Override
    public AllStatisticsDTO getAllStatistics(Date startDate, Date endDate) {
        AllStatisticsDTO allStatistics = new AllStatisticsDTO();
        
        // 获取所有统计数据
        allStatistics.setRoomUsage(getRoomUsageStatistics(startDate, endDate));
        allStatistics.setBookingStatus(getBookingStatusStatistics(startDate, endDate));
        allStatistics.setUserRanking(getUserBookingRanking(startDate, endDate, 10));
        allStatistics.setMeetingDuration(getMeetingDurationStatistics(startDate, endDate));
        
        // 获取总数据
        allStatistics.setTotalBookings(statisticsMapper.getTotalBookings(startDate, endDate));
        allStatistics.setTotalUsers(statisticsMapper.getTotalUsers());
        allStatistics.setTotalRooms(statisticsMapper.getTotalRooms());
        
        return allStatistics;
    }
}