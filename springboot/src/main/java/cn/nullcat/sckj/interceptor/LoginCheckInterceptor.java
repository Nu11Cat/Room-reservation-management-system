package cn.nullcat.sckj.interceptor;
import cn.nullcat.sckj.mapper.RolePermissionMapper;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.User;
import cn.nullcat.sckj.utils.JwtUtils;
import cn.nullcat.sckj.utils.TokenUtils;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor{
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse rep, Object handler) throws Exception {
        // 1. 获取请求URL
        String url = req.getRequestURI().toString();
        // 2. 如果是登录接口，直接放行
        if (url.contains("login")) {
            return true;
        }
        // 3. 获取token
        String jwt = req.getHeader("token");
        if (!StringUtils.hasLength(jwt)) {
            Result error = Result.error("NOT_LOGIN");
            String notLogin = JSONObject.toJSONString(error);
            rep.getWriter().write(notLogin);
            return false;
        }
        try {
            // 4. 解析token
            Claims claims = JwtUtils.parseJWT(jwt);
            //
            Integer userId = (Integer) claims.get("userId");
            // 5. 验证token
            if (!tokenUtils.validateToken(jwt, userId)) {
                Result error = Result.error("NOT_LOGIN");
                String notLogin = JSONObject.toJSONString(error);
                rep.getWriter().write(notLogin);
                return false;
            }
             //
            // 6. 获取用户信息（包含角色ID）
            User user = tokenUtils.getUserInfo(userId);
            if (user == null) {
                Result error = Result.error("NOT_LOGIN");
                String notLogin = JSONObject.toJSONString(error);
                rep.getWriter().write(notLogin);
                return false;
            }

            // 7. 检查审批权限
            if (url.contains("/approvals/")) {
                if (!rolePermissionMapper.hasPermission(user.getRoleId(), "booking:approve")) {
                    Result error = Result.error("NO_PERMISSION");
                    rep.getWriter().write(JSONObject.toJSONString(error));
                    return false;
                }
            }
            // 8. 将用户ID存入request
            req.setAttribute("userId", claims.get("userId"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Result error = Result.error("NOT_LOGIN");
            String notLogin = JSONObject.toJSONString(error);
            rep.getWriter().write(notLogin);
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
