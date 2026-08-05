package com.kanako.common.jwt;

import com.kanako.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 可选登录：有 token 就解析并放入上下文
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.parse(auth.substring(7));
                String role = claims.get("role", String.class);
                Long uid = claims.get("uid", Long.class);
                String username = claims.get("username", String.class);
                UserContext.set(new LoginUser(uid, username, role == null ? "USER" : role));
            } catch (JwtException | IllegalArgumentException ignored) {
                // token 无效视为未登录
            }
        }
        // 强制登录/管理员注解
        if (handlerMethod.hasMethodAnnotation(RequireAdmin.class)) {
            LoginUser user = UserContext.get();
            if (user == null) {
                throw new BusinessException(401, "请先登录");
            }
            if (!user.isAdmin()) {
                throw new BusinessException(403, "无管理员权限");
            }
        } else if (handlerMethod.hasMethodAnnotation(RequireLogin.class)) {
            if (UserContext.get() == null) {
                throw new BusinessException(401, "请先登录");
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}