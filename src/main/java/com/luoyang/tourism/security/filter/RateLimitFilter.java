package com.luoyang.tourism.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.util.RequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * RateLimitFilter 安全组件。
 * <p>负责 JWT、权限、限流或请求安全校验。</p>
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 50)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !(path.endsWith("/api/v1/app/auth/wechat-login")
                || path.endsWith("/api/v1/app/auth/refresh")
                || path.endsWith("/api/v1/admin/auth/login")
                || path.endsWith("/api/v1/admin/auth/refresh")
                || path.endsWith("/api/v1/app/ai/chat"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean aiChat = path.endsWith("/api/v1/app/ai/chat");
        String identity;
        int limit;
        if (aiChat) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken token)) {
                filterChain.doFilter(request, response);
                return;
            }
            identity = token.getToken().getSubject();
            limit = 10;
        } else {
            identity = RequestUtils.clientIp(request);
            limit = 20;
        }

        String bucket = Long.toString(Instant.now().getEpochSecond() / 60);
        String key = "tourism:spring:rate:" + (aiChat ? "ai:" : "auth:") + identity + ":" + bucket;
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(65));
            }
            if (count != null && count > limit) {
                ResultCode code = ResultCode.RATE_LIMITED;
                response.setStatus(code.httpStatus().value());
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), ApiResponse.error(code.code(), code.defaultMessage()));
                return;
            }
        } catch (RuntimeException ignored) {
            // Rate limiting must not take down the business path when Redis is temporarily unavailable.
        }
        filterChain.doFilter(request, response);
    }
}
