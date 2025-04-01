package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.DTO.RoomUsageDTO;
import java.util.Date;
import java.util.List;

public interface StatisticsService {
    List<RoomUsageDTO> getRoomUsageStatistics(Date startDate, Date endDate);
}