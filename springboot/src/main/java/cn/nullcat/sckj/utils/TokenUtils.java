package cn.nullcat.sckj.utils;

import cn.nullcat.sckj.pojo.User;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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
        String value = (String) redisTemplateForObject.opsForValue().get(key);
        return value != null ? JSON.parseObject(value, User.class) : null;
    }

    // 删除用户信息
    public void removeUserInfo(Integer userId) {
        String key = USER_PREFIX + userId;
        redisTemplateForObject.delete(key);
    }

    // 更新用户信息
    public void updateUserInfo(User user) {
        String key = USER_PREFIX + user.getId();
        redisTemplateForObject.opsForValue().set(key, user, TOKEN_EXPIRE, TimeUnit.MILLISECONDS);
    }

}