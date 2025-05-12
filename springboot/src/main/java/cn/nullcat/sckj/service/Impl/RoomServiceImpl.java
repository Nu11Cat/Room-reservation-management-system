package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.mapper.RoomMapper;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Room;
import cn.nullcat.sckj.service.RoomService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomMapper roomMapper;

    /**
     * 获取会议室列表
     * @param page
     * @param pageSize
     * @param name
     * @param location
     * @param capacity
     * @param status
     * @return
     */
    @Override
    public PageBean getAllRomms(Integer page, Integer pageSize, String name, String location, Integer capacity, Integer status) {
        PageHelper.startPage(page, pageSize);
        List<Room> list = roomMapper.getAllRomms(name,location,capacity, status);
        Page<Room> p = (Page<Room>) list;

        PageBean pageBean = new PageBean(p.getTotal(), p.getResult());
        return pageBean;
    }

    /**
     * 根据id获取会议室详情
     * @param id
     * @return
     */
    @Override
    public Room getById(Integer id) {
        Room room = roomMapper.getById(id);
        return room;
    }

    /**
     * 添加会议室
     * @param room
     */
    @Override
    @LogOperation(module = "会议室管理", operation = "添加会议室", description = "添加会议室")
    public void addRoom(Room room) {
        room.setStatus(1);
        room.setCreateTime(new Date());
        room.setUpdateTime(new Date());
        room.setIsDeleted(false);
        roomMapper.addRoom(room);
    }

    /**
     * 更新会议室信息
     * @param room
     */
    @Override
    @LogOperation(module = "会议室管理", operation = "更新会议室信息", description = "更新会议室信息")
    public void updateRoom(Room room) {
        roomMapper.updateRoom(room);
    }

    /**
     * 删除会议室
     * @param id
     */
    @Override
    @LogOperation(module = "会议室管理", operation = "删除会议室", description = "删除会议室")
    public void deleteById(Integer id) {
        roomMapper.deleteById(id);
    }

}
