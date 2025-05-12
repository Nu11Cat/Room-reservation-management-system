// 创建 cn.nullcat.sckj.aspect.LogAspect 类
package cn.nullcat.sckj.aspect;

import cn.nullcat.sckj.annotation.LogOperation;
import cn.nullcat.sckj.pojo.OperationLog;
import cn.nullcat.sckj.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperationLogService logService;

    @Pointcut("@annotation(cn.nullcat.sckj.annotation.LogOperation)")
    public void logPointCut() {}

    @AfterReturning("logPointCut()")
    public void doAfterReturning(JoinPoint joinPoint) {
        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // 获取当前用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return; // 未登录用户不记录日志
            }

            // 获取IP地址
            String ip = request.getRemoteAddr();

            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogOperation logAnnotation = method.getAnnotation(LogOperation.class);

            // 构建并保存日志
            OperationLog log = new OperationLog();
            log.setUserId(Long.valueOf(userId));
            log.setModule(logAnnotation.module());
            log.setOperation(logAnnotation.operation());
            log.setDescription(logAnnotation.description());
            log.setIp(ip);

            logService.addOperationLog(log);
        } catch (Exception e) {
            // 记录日志时出现异常不影响业务
            e.printStackTrace();
        }
    }

    // 简单的获取IP方法，可以根据需要扩展
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}