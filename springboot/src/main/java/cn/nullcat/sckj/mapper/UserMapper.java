package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 注册
     * @param user
     */
    @Insert("insert into user(username, password, real_name, email, phone, avatar, role_id, status, create_time, update_time, is_deleted, credit_score, identity) " +
            "VALUES(#{username},#{password},#{realName},#{email},#{phone},#{avatar},#{roleId},#{status},#{createTime},#{updateTime},#{isDeleted},#{creditScore},#{identity}) ")
    void register(User user);

    /**
     *
     * @param username
     * @return
     */
    @Select("select user.password from user where username = #{username}")
    String getPasswordByUserName(String username);

    /**
     *
     * @param username
     * @return
     */
    @Select("select * from user where username = #{username}")
    User getByUserName(String username);

    /**
     *
     * @param userIdNow
     * @return
     */
    @Select("select * from user where id = #{userIdNow}")
    User getById(Integer userIdNow);

    /**
     * 更改个人信息
     * @param user
     */
    void updateInfo(User user);

    /**
     * 修改密码
     * @param user
     */
    @Update("update user set password = #{password}, update_time = NOW() where id = #{id}")
    void updatePassword(User user);

    /**
     *
     * @param username
     * @return
     */
    @Select("select Id from user where username = #{username}")
    Integer getUserIdByUsername(String username);

    /**
     * 获取全部用户信息
     * @return
     */
    List<User> getAllUsers();

    /**
     * 封禁用户
     * @param id
     */
    @Update("update user set status = 0 where id = #{id}")
    void banById(Integer id);

    /**
     * 解封用户
     * @param id
     */
    @Update("update user set status = 1 where id = #{id}")
    void unsealById(Integer id);

    /**
     * 获取全部用户id
     * @return
     */
    @Select("select id from user")
    List<Long> getAllUserIds();

    /**
     *
     * @param userId
     * @return
     */
    @Select("select status from user where id = #{userId}")
    Integer getStatusById(Integer userId);

    /**
     * 获取指定角色的所有用户ID
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Select("SELECT id FROM user WHERE role_id = #{roleId}")
    List<Long> getUserIdsByRoleId(Long roleId);

    /**
     * 获取所有用户的简单列表（ID、用户名和身份）
     * @return 用户列表
     */
    @Select("SELECT id, username, identity, credit_score as creditScore FROM user WHERE status = 1")
    List<User> getUserSimpleList();

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响的行数
     */
    @Update("UPDATE user SET role_id = #{roleId}, update_time = NOW() WHERE id = #{userId}")
    int updateUserRole(Long userId, Long roleId);

    /**
     * 根据用户ID查询用户角色ID
     * @param userId 用户ID
     * @return 角色ID
     */
    Integer selectRoleIdByUserId(Long userId);
}
