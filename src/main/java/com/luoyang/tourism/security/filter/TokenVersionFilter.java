package com.luoyang.tourism.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.module.auth.entity.AdminUser;
import com.luoyang.tourism.module.auth.entity.AppUser;
import com.luoyang.tourism.module.auth.mapper.AuthMapper;
import com.luoyang.tourism.security.principal.AuthClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@RequiredArgsConstructor
public class TokenVersionFilter extends OncePerRequestFilter {
    private final AuthMapper authMapper;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/app/") && !path.startsWith("/api/v1/admin/")) {
            return true;
        }
        return path.startsWith("/api/v1/app/auth/") || path.startsWith("/api/v1/admin/auth/login")
                || path.startsWith("/api/v1/admin/auth/refresh") || path.startsWith("/api/v1/admin/auth/logout");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token) || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            long userId = AuthClaims.userId(token.getToken());
            int tokenVersion = AuthClaims.tokenVersion(token.getToken());
            boolean valid;
            if (request.getRequestURI().startsWith("/api/v1/app/")) {
                AppUser user = authMapper.selectAppUserById(userId);
                valid = user != null && Integer.valueOf(1).equals(user.getStatus())
                        && user.getTokenVersion() == tokenVersion;
            } else {
                AdminUser admin = authMapper.selectAdminById(userId);
                valid = admin != null && Integer.valueOf(1).equals(admin.getStatus())
                        && admin.getTokenVersion() == tokenVersion;
            }
            if (!valid) {
                SecurityContextHolder.clearContext();
                writeError(response, ResultCode.TOKEN_INVALID);
                return;
            }
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            writeError(response, ResultCode.INTERNAL_ERROR);
        }
    }

    private void writeError(HttpServletResponse response, ResultCode code) throws IOException {
        response.setStatus(code.httpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(code.code(), code.defaultMessage()));
    }
}
