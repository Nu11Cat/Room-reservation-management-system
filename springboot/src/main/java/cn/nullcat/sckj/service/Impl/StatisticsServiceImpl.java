package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.pojo.DTO.RoomUsageDTO;
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
}