package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.DTO.RoomUsageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface StatisticsMapper {
    // 会议室使用率统计
    List<RoomUsageDTO> getRoomUsageStatistics(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate);
}