package cn.nullcat.sckj.service.Impl;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.mapper.MisconductTypeMapper;
import cn.nullcat.sckj.pojo.MisconductType;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.service.MisconductTypeService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 不文明行为类型服务实现类
 */
@Service
@Slf4j
public class MisconductTypeServiceImpl implements MisconductTypeService {

    @Autowired
    private MisconductTypeMapper misconductTypeMapper;

    /**
     * 获取所有不文明行为类型
     * @return 不文明行为类型列表
     */
    @Override
    public List<MisconductType> getAllTypes() {
        log.info("获取所有不文明行为类型");
        return misconductTypeMapper.getAllTypes();
    }

    /**
     * 分页获取不文明行为类型
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageBean getTypesByPage(Integer page, Integer pageSize) {
        log.info("分页获取不文明行为类型：page={}, pageSize={}", page, pageSize);
        // 设置分页参数
        PageHelper.startPage(page, pageSize);
        // 执行查询
        List<MisconductType> typeList = misconductTypeMapper.getAllTypes();
        // 获取分页数据
        Page<MisconductType> pageData = (Page<MisconductType>) typeList;
        // 封装PageBean
        return new PageBean(pageData.getTotal(), pageData.getResult());
    }

    /**
     * 根据ID获取不文明行为类型
     * @param id 不文明行为类型ID
     * @return 不文明行为类型信息
     */
    @Override
    public MisconductType getTypeById(Integer id) {
        log.info("根据ID获取不文明行为类型：id={}", id);
        return misconductTypeMapper.getTypeById(id);
    }

    /**
     * 添加不文明行为类型
     * @param misconductType 不文明行为类型信息
     * @return 添加后的ID
     */
    @Override
    @LogOperation(module = "不文明行为类型管理", operation = "添加不文明行为类型", description = "添加不文明行为类型")
    public Integer addType(MisconductType misconductType) {
        log.info("添加不文明行为类型：{}", misconductType);
        misconductTypeMapper.addType(misconductType);
        log.info("不文明行为类型添加成功，ID：{}", misconductType.getId());
        return misconductType.getId();
    }

    /**
     * 更新不文明行为类型
     * @param misconductType 不文明行为类型信息
     * @return 是否更新成功
     */
    @Override
    @LogOperation(module = "不文明行为类型管理", operation = "更新不文明行为类型", description = "更新不文明行为类型")
    public boolean updateType(MisconductType misconductType) {
        log.info("更新不文明行为类型：{}", misconductType);
        // 先检查记录是否存在
        MisconductType existingType = misconductTypeMapper.getTypeById(misconductType.getId());
        if (existingType == null) {
            log.warn("不文明行为类型不存在：id={}", misconductType.getId());
            return false;
        }
        
        // 执行更新
        int rows = misconductTypeMapper.updateType(misconductType);
        return rows > 0;
    }

    /**
     * 删除不文明行为类型
     * @param id 不文明行为类型ID
     * @return 是否删除成功
     */
    @Override
    @LogOperation(module = "不文明行为类型管理", operation = "删除不文明行为类型", description = "删除不文明行为类型")
    public boolean deleteType(Integer id) {
        log.info("删除不文明行为类型：id={}", id);
        // 先检查记录是否存在
        MisconductType existingType = misconductTypeMapper.getTypeById(id);
        if (existingType == null) {
            log.warn("不文明行为类型不存在：id={}", id);
            return false;
        }
        
        // 检查是否有关联记录（如已有评价使用该类型）
        boolean hasReferences = misconductTypeMapper.checkTypeReferences(id);
        if (hasReferences) {
            log.warn("不文明行为类型已被引用，无法删除：id={}", id);
            return false;
        }
        
        // 执行删除
        int rows = misconductTypeMapper.deleteType(id);
        return rows > 0;
    }
} 