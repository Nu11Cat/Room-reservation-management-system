package cn.nullcat.sckj.mapper;

import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


public interface UserMapper {
    /**
     * 注册
     * @param user
     */
    @Insert("insert into user(username, password, real_name, email, phone, avatar, role_id, status, create_time, update_time, is_deleted) " +
            "VALUES(#{username},#{password},#{realName},#{email},#{phone},#{avatar},#{roleId},#{status},#{createTime},#{updateTime},#{isDeleted}) ")
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
    @Update("update user set real_name = #{realName}, email = #{email}, phone = #{phone}, avatar = #{avatar}, update_time = NOW() where id = #{id}")
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
    @Select("select id, username, real_name, email, phone, status, create_time, update_time from user")
    List<Booking> getAllUsers();

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
}
