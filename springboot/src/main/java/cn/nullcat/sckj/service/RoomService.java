package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Room;

public interface RoomService {
    PageBean getAllRomms(Integer page, Integer pageSize, String name, String location, Integer capacity, Integer status);

    Room getById(Integer id);

    void addRoom(Room room);

    void updateRoom(Room room);

    void deleteById(Integer id);
}
