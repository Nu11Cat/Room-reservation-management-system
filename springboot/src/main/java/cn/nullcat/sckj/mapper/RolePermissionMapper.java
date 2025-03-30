package cn.nullcat.sckj.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RolePermissionMapper {
    @Select("SELECT COUNT(*) FROM role_permission rp " +
            "WHERE rp.role_id = #{roleId} " +
            "AND rp.permission_id = (SELECT id FROM permission WHERE code = #{permissionCode}) " +
            "AND rp.is_deleted = 0")
    boolean hasPermission(Long roleId, String permissionCode);
}
