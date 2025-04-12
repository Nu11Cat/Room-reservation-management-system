package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.MisconductType;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.service.MisconductTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/misconduct/types")
@Tag(name = "不文明行为类型管理")
public class MisconductTypeController {

    @Autowired
    private MisconductTypeService misconductTypeService;

    /**
     * 获取所有不文明行为类型
     * @return 所有不文明行为类型列表
     */
    @GetMapping
    @Operation(summary = "获取所有不文明行为类型")
    @RequirePermission("misconduct:view")
    public Result getAllTypes() {
        log.info("获取所有不文明行为类型");
        List<MisconductType> types = misconductTypeService.getAllTypes();
        return Result.success(types);
    }

    /**
     * 分页获取不文明行为类型
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取不文明行为类型")
    @RequirePermission("misconduct:view")
    public Result getTypesByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("分页获取不文明行为类型：page={}, pageSize={}", page, pageSize);
        PageBean pageBean = misconductTypeService.getTypesByPage(page, pageSize);
        return Result.success(pageBean);
    }

    /**
     * 根据ID获取不文明行为类型
     * @param id 不文明行为类型ID
     * @return 不文明行为类型详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取不文明行为类型")
    @RequirePermission("misconduct:view")
    public Result getTypeById(@PathVariable Integer id) {
        log.info("根据ID获取不文明行为类型：id={}", id);
        MisconductType type = misconductTypeService.getTypeById(id);
        if (type == null) {
            return Result.error("不文明行为类型不存在");
        }
        return Result.success(type);
    }

    /**
     * 添加不文明行为类型
     * @param misconductType 不文明行为类型信息
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加不文明行为类型")
    @RequirePermission("misconduct:add")
    public Result addType(@RequestBody MisconductType misconductType) {
        log.info("添加不文明行为类型：{}", misconductType);
        
        // 参数验证
        if (misconductType.getTypeName() == null || misconductType.getTypeName().trim().isEmpty()) {
            return Result.error("类型名称不能为空");
        }
        
        if (misconductType.getDefaultScoreImpact() == null) {
            return Result.error("默认信誉分影响值不能为空");
        }
        
        if (misconductType.getSeverityLevel() == null || 
            misconductType.getSeverityLevel() < 1 || 
            misconductType.getSeverityLevel() > 5) {
            return Result.error("严重程度必须在1-5之间");
        }
        
        misconductTypeService.addType(misconductType);
        return Result.success("添加成功");
    }

    /**
     * 更新不文明行为类型
     * @param id 不文明行为类型ID
     * @param misconductType 不文明行为类型信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新不文明行为类型")
    @RequirePermission("misconduct:edit")
    public Result updateType(@PathVariable Integer id, @RequestBody MisconductType misconductType) {
        log.info("更新不文明行为类型：id={}, {}", id, misconductType);
        
        // 设置ID确保一致性
        misconductType.setId(id);
        
        // 参数验证
        if (misconductType.getTypeName() != null && misconductType.getTypeName().trim().isEmpty()) {
            return Result.error("类型名称不能为空");
        }
        
        if (misconductType.getSeverityLevel() != null && 
            (misconductType.getSeverityLevel() < 1 || misconductType.getSeverityLevel() > 5)) {
            return Result.error("严重程度必须在1-5之间");
        }
        
        boolean updated = misconductTypeService.updateType(misconductType);
        if (!updated) {
            return Result.error("不文明行为类型不存在");
        }
        return Result.success("更新成功");
    }

    /**
     * 删除不文明行为类型
     * @param id 不文明行为类型ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除不文明行为类型")
    @RequirePermission("misconduct:delete")
    public Result deleteType(@PathVariable Integer id) {
        log.info("删除不文明行为类型：id={}", id);
        boolean deleted = misconductTypeService.deleteType(id);
        if (!deleted) {
            return Result.error("不文明行为类型不存在或无法删除");
        }
        return Result.success("删除成功");
    }
} 