package com.luoyang.tourism.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 内部接口令牌过滤器。
 * <p>FastAPI 只在本机运行，使用一个固定令牌即可，不再实现 HMAC、时间窗和防重放。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class InternalTokenFilter extends OncePerRequestFilter {
    private static final String HEADER = "X-Internal-Token";

    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String expected = properties.getInternal().getToken();
        String supplied = request.getHeader(HEADER);
        if (expected == null || expected.isBlank() || supplied == null || !expected.equals(supplied)) {
            ResultCode code = ResultCode.UNAUTHORIZED;
            response.setStatus(code.httpStatus().value());
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.error(code.code(), "内部调用令牌不正确"));
            return;
        }
        filterChain.doFilter(request, response);
    }
}