package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface OperationLogMapper {

    @Insert("INSERT INTO operation_log(user_id, module, operation, description, ip, create_time, is_deleted) " +
            "VALUES(#{userId}, #{module}, #{operation}, #{description}, #{ip}, #{createTime}, 0)")
    void insert(OperationLog log);

    List<OperationLog> getOperationLogs(@Param("userId") Long userId,
                                        @Param("module") String module,
                                        @Param("operation") String operation,
                                        @Param("startTime") Date startTime,
                                        @Param("endTime") Date endTime);

    List<String> findAllModules();

    List<String> findAllOperationTypes();
}