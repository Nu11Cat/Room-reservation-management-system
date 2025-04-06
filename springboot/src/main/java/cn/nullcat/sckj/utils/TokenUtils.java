package cn.nullcat.sckj.utils;

import cn.nullcat.sckj.pojo.User;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.Date;

@Slf4j
@Component
public class TokenUtils {
    @Autowired
    private RedisTemplate<String, String> redisTemplateForString;
    @Autowired
    private RedisTemplate<String, Object> redisTemplateForObject;
    private static final String TOKEN_PREFIX = "token:";
    private static final String USER_PREFIX = "user:";
    private static final Long TOKEN_EXPIRE = 604800000L; // 7天


    public void saveToken(String token, Integer userId) {
        String key = TOKEN_PREFIX + userId;
        redisTemplateForString.opsForValue().set(key, token, TOKEN_EXPIRE, TimeUnit.MILLISECONDS);
    }

    public boolean validateToken(String token, Integer userId) {
        String key = TOKEN_PREFIX + userId;
        Object savedToken = redisTemplateForString.opsForValue().get(key);
        return token.equals(savedToken);
    }

    public void removeToken(Integer userId) {

        String key = TOKEN_PREFIX + userId;
        redisTemplateForString.delete(key);
    }
    // 保存用户信息
    // 存储用户信息时转换为JSON字符串
    public void saveUserInfo(User user) {
        String key = USER_PREFIX + user.getId();
        String value = JSON.toJSONString(user);
        redisTemplateForObject.opsForValue().set(key, value, TOKEN_EXPIRE, TimeUnit.MILLISECONDS);
    }

    // 获取用户信息时解析JSON字符串
    public User getUserInfo(Integer userId) {
        String key = USER_PREFIX + userId;
        Object value = redisTemplateForObject.opsForValue().get(key);
        
        if (value == null) {
            return null;
        }
        
        // 处理不同类型情况
        if (value instanceof String) {
            return JSON.parseObject((String)value, User.class);
        } else if (value instanceof User) {
            return (User)value;
        } else {
            // 如果是JSONObject，尝试转换为User对象
            return JSON.parseObject(JSON.toJSONString(value), User.class);
        }
    }

    // 删除用户信息
    public void removeUserInfo(Integer userId) {
        String key = USER_PREFIX + userId;
        redisTemplateForObject.delete(key);
    }

    // 更新用户信息
    public void updateUserInfo(User user) {
        // 先获取现有的完整用户信息
        String key = USER_PREFIX + user.getId();
        Object existingValue = redisTemplateForObject.opsForValue().get(key);
        User completeUser = null;
        
        // 处理现有数据
        if (existingValue != null) {
            if (existingValue instanceof String) {
                completeUser = JSON.parseObject((String)existingValue, User.class);
            } else if (existingValue instanceof User) {
                completeUser = (User)existingValue;
            } else {
                completeUser = JSON.parseObject(JSON.toJSONString(existingValue), User.class);
            }
        }
        
        // 如果有现有数据，合并新的字段值
        if (completeUser != null) {
            // 只更新不为null的字段
            if (user.getEmail() != null) completeUser.setEmail(user.getEmail());
            if (user.getPhone() != null) completeUser.setPhone(user.getPhone());
            if (user.getPassword() != null) completeUser.setPassword(user.getPassword());
            if (user.getRealName() != null) completeUser.setRealName(user.getRealName());
            if (user.getAvatar() != null) completeUser.setAvatar(user.getAvatar());
            if (user.getStatus() != null) completeUser.setStatus(user.getStatus());
            if (user.getRoleId() != null) completeUser.setRoleId(user.getRoleId());
            if (user.getIsDeleted() != null) completeUser.setIsDeleted(user.getIsDeleted());
            
            // 更新时间
            completeUser.setUpdateTime(new Date());
            
            // 存储合并后的完整用户信息
            String value = JSON.toJSONString(completeUser);
            redisTemplateForObject.opsForValue().set(key, value, TOKEN_EXPIRE, TimeUnit.MILLISECONDS);
        } else {
            // 如果不存在现有数据，直接保存新数据
            saveUserInfo(user);
        }
    }

}