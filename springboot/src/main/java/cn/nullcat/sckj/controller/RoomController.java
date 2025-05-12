package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.Room;
import cn.nullcat.sckj.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/rooms")
@Tag(name = "会议室管理")
public class RoomController {
    @Autowired
    private RoomService roomService;

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
    @GetMapping
    @RequirePermission("room:list")
    @Operation(summary ="获取会议室列表")
    public Result getRooms(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           String name,
                           String location,
                           Integer capacity,
                           Integer status) {
        log.info("会室分页条件查询:{},{},{},{},{},{}",page,pageSize,name,location,capacity,status);
        PageBean pageBean = roomService.getAllRomms(page,pageSize,name,location,capacity,status);
        return Result.success(pageBean);
    }

    /**
     * 根据id获取会议室详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary ="根据id获取会议室详情")
    @RequirePermission("room:view")
    public Result getRoom(@PathVariable Integer id) {
        Room room = roomService.getById(id);
        return Result.success(room);
    }

    /**
     * 新增会议室
     * @param room
     * @return
     */
    @PostMapping
    @RequirePermission("room:add")
    @Operation(summary ="新增会议室")
    public Result addRoom(@RequestBody Room room) {
        roomService.addRoom(room);
        return Result.success("添加成功");
    }

    /**
     * 修改会议室信息
     * @param id
     * @param room
     * @return
     */
    @PutMapping("/{id}")
    @Operation(summary ="修改会议室信息")
    @RequirePermission("room:edit")
    public Result updateRoom(@PathVariable Long id, @RequestBody Room room) {
        room.setId(id);
        roomService.updateRoom(room);
        return Result.success("修改成功");
    }

    /**
     * 删除会议室
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary ="删除会议室")
    @RequirePermission("room:delete")
    public Result deleteRoom(@PathVariable Integer id) {
        roomService.deleteById(id);
        return Result.success("删除成功");
    }
}
