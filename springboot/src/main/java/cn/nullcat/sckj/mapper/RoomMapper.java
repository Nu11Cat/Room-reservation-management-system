package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Room;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomMapper {
    /**
     * 分页条件获取会议室列表
     * @param name
     * @param location
     * @param capacity
     * @param status
     * @return
     */
    List<Room> getAllRomms(String name, String location, Integer capacity, Integer status);

    /**
     * 根据id获取会议室详情
     * @param id
     * @return
     */
    @Select("select * from room where id = #{id}")
    Room getById(Integer id);

    /**
     * 新增会议室
     * @param room
     */
    @Insert("insert into " +
            "room(name, location, capacity, equipment, description, status, create_time, update_time, is_deleted) " +
            "VALUES(#{name},#{location},#{capacity},#{equipment},#{description},#{status},#{createTime},#{updateTime},#{isDeleted}) ")
    void addRoom(Room room);

    /**
     * 修改会议室
     * @param room
     */
    @Update("update room set" +
            " name = #{name}," +
            "location = #{location}," +
            "capacity = #{capacity}," +
            "equipment = #{equipment}," +
            "description = #{description}," +
            "status = #{status}," +
            "update_time = NOW()")
    void updateRoom(Room room);

    /**
     * 删除会议室
     * @param id
     */
    @Delete("delete from room where id =#{id}")
    void deleteById(Integer id);
}
