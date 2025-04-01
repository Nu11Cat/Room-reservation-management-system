package cn.nullcat.sckj.websocket;

import cn.nullcat.sckj.pojo.Notification;
import cn.nullcat.sckj.utils.JwtUtils;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket/notification/{token}")
@Component
@Slf4j
public class NotificationWebSocket {
    // 用于存放所有在线的客户端
    private static final Map<Integer, NotificationWebSocket> clients = new ConcurrentHashMap<>();

    // 与某个客户端的连接会话
    private Session session;

    // 当前连接用户ID
    private Integer userId;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            // 验证token
            Claims claims = JwtUtils.parseJWT(token);
            Integer userId = (Integer) claims.get("userId");

            this.session = session;
            this.userId = userId;

            // 将当前websocket对象存入map
            clients.put(userId, this);
            log.info("有新的连接加入：{}，当前在线人数为：{}", userId, clients.size());
        } catch (Exception e) {
            log.error("WebSocket连接建立失败", e);
            try {
                session.close();
            } catch (IOException ex) {
                log.error("关闭WebSocket连接失败", ex);
            }
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (this.userId != null) {
            clients.remove(this.userId);
            log.info("有一连接关闭：{}，当前在线人数为：{}", userId, clients.size());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到来自客户端的消息：{}", message);
        // 这里不需要处理客户端消息，因为通知是单向的
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误", error);
        if (this.userId != null) {
            clients.remove(this.userId);
        }
    }

    /**
     * 发送通知给指定用户
     */
    public static void sendNotification(Integer userId, Notification notification) {
        NotificationWebSocket webSocket = clients.get(userId);
        if (webSocket != null) {
            try {
                Map<String, Object> message = Map.of(
                        "type", "notification",
                        "data", notification
                );
                webSocket.session.getBasicRemote().sendText(JSON.toJSONString(message));
                log.info("已向用户{}发送通知: {}", userId, notification.getTitle());
            } catch (IOException e) {
                log.error("发送通知失败", e);
            }
        } else {
            log.info("用户{}不在线，无法发送通知", userId);
        }
    }

    /**
     * 群发通知
     */
    public static void broadcastNotification(Notification notification) {
        for (Map.Entry<Integer, NotificationWebSocket> entry : clients.entrySet()) {
            try {
                Map<String, Object> message = Map.of(
                        "type", "notification",
                        "data", notification
                );
                entry.getValue().session.getBasicRemote().sendText(JSON.toJSONString(message));
            } catch (IOException e) {
                log.error("群发通知失败", e);
            }
        }
        log.info("群发通知完成: {}", notification.getTitle());
    }
}