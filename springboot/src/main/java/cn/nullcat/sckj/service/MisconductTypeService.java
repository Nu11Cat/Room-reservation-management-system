package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.MisconductType;
import cn.nullcat.sckj.pojo.PageBean;

import java.util.List;

/**
 * 不文明行为类型服务接口
 */
public interface MisconductTypeService {
    
    /**
     * 获取所有不文明行为类型
     * @return 不文明行为类型列表
     */
    List<MisconductType> getAllTypes();
    
    /**
     * 分页获取不文明行为类型
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageBean getTypesByPage(Integer page, Integer pageSize);
    
    /**
     * 根据ID获取不文明行为类型
     * @param id 不文明行为类型ID
     * @return 不文明行为类型信息
     */
    MisconductType getTypeById(Integer id);
    
    /**
     * 添加不文明行为类型
     * @param misconductType 不文明行为类型信息
     * @return 添加后的ID
     */
    Integer addType(MisconductType misconductType);
    
    /**
     * 更新不文明行为类型
     * @param misconductType 不文明行为类型信息
     * @return 是否更新成功
     */
    boolean updateType(MisconductType misconductType);
    
    /**
     * 删除不文明行为类型
     * @param id 不文明行为类型ID
     * @return 是否删除成功
     */
    boolean deleteType(Integer id);
} 