package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.MisconductType;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 不文明行为类型数据访问层接口
 */
@Mapper
public interface MisconductTypeMapper {

    /**
     * 获取所有不文明行为类型
     * @return 不文明行为类型列表
     */
    @Select("SELECT * FROM misconduct_type ORDER BY severity_level DESC")
    List<MisconductType> getAllTypes();

    /**
     * 根据ID获取不文明行为类型
     * @param id 不文明行为类型ID
     * @return 不文明行为类型信息
     */
    @Select("SELECT * FROM misconduct_type WHERE id = #{id}")
    MisconductType getTypeById(@Param("id") Integer id);

    /**
     * 添加不文明行为类型
     * @param misconductType 不文明行为类型信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO misconduct_type(type_name, description, default_score_impact, severity_level) " +
            "VALUES(#{typeName}, #{description}, #{defaultScoreImpact}, #{severityLevel})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addType(MisconductType misconductType);

    /**
     * 更新不文明行为类型
     * @param misconductType 不文明行为类型信息
     * @return 影响的行数
     */
    int updateType(MisconductType misconductType);

    /**
     * 删除不文明行为类型
     * @param id 不文明行为类型ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM misconduct_type WHERE id = #{id}")
    int deleteType(@Param("id") Integer id);

    /**
     * 检查类型是否被引用
     * @param id 不文明行为类型ID
     * @return 是否被引用
     */
    @Select("SELECT COUNT(*) > 0 FROM user_review WHERE review_type = #{id}")
    boolean checkTypeReferences(@Param("id") Integer id);
} 