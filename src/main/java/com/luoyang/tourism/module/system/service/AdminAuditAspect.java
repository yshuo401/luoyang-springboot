package com.luoyang.tourism.module.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.trace.TraceIds;
import com.luoyang.tourism.common.util.RequestUtils;
import com.luoyang.tourism.module.system.entity.AdminOperationLog;
import com.luoyang.tourism.security.principal.AuthClaims;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class AdminAuditAspect {
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Around("execution(* com.luoyang.tourism.module..controller..*(..))")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (!shouldAudit(request)) {
            return joinPoint.proceed();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            return joinPoint.proceed();
        }
        Jwt jwt = token.getToken();
        long started = System.currentTimeMillis();
        boolean success = false;
        String responseCode = null;
        try {
            Object result = joinPoint.proceed();
            success = true;
            if (result instanceof ApiResponse<?> apiResponse) {
                responseCode = Integer.toString(apiResponse.code());
                success = apiResponse.code() == 0;
            }
            return result;
        } catch (BizException exception) {
            responseCode = Integer.toString(exception.resultCode().code());
            throw exception;
        } catch (Throwable throwable) {
            responseCode = Integer.toString(ResultCode.INTERNAL_ERROR.code());
            throw throwable;
        } finally {
            AdminOperationLog log = new AdminOperationLog();
            log.setAdminUserId(AuthClaims.userId(jwt));
            log.setUsernameSnapshot(AuthClaims.username(jwt));
            log.setModule(module(request.getRequestURI()));
            log.setOperationType(operation(request));
            log.setHttpMethod(request.getMethod());
            log.setRequestUri(request.getRequestURI());
            log.setRequestParams(parameters(joinPoint));
            log.setResponseCode(responseCode);
            log.setSuccess(success ? 1 : 0);
            log.setIp(RequestUtils.clientIp(request));
            log.setUserAgent(truncate(request.getHeader("User-Agent"), 512));
            log.setTraceId(TraceIds.current());
            log.setDurationMs((int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - started));
            auditLogService.record(log);
        }
    }

    private boolean shouldAudit(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/admin/")) {
            return false;
        }
        if (path.equals("/api/v1/admin/auth/login") || path.equals("/api/v1/admin/auth/refresh")
                || path.equals("/api/v1/admin/auth/logout")) {
            return false;
        }
        return !("GET".equals(request.getMethod()) || "OPTIONS".equals(request.getMethod()));
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String parameters(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            Map<String, Object> safe = new LinkedHashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null || arg instanceof ServletRequest || arg instanceof ServletResponse
                        || arg instanceof MultipartFile || arg instanceof Jwt) {
                    continue;
                }
                String name = names != null && i < names.length ? names[i] : ("arg" + i);
                safe.put(name, sanitize(objectMapper.convertValue(arg, Object.class)));
            }
            return truncate(objectMapper.writeValueAsString(safe), 3000);
        } catch (Exception exception) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Object sanitize(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String normalized = key.toLowerCase(Locale.ROOT);
                if (normalized.contains("password") || normalized.contains("secret") || normalized.contains("token")
                        || normalized.contains("accesskey") || normalized.contains("access_key")
                        || normalized.contains("apikey") || normalized.contains("api_key")) {
                    result.put(key, "***");
                } else {
                    result.put(key, sanitize(entry.getValue()));
                }
            }
            return result;
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>();
            for (Object item : list) {
                result.add(sanitize(item));
            }
            return result;
        }
        if (value instanceof String string) {
            return truncate(string, 500);
        }
        return value;
    }

    private String module(String path) {
        String[] parts = path.split("/");
        String value = parts.length > 4 ? parts[4] : "ADMIN";
        return value.toUpperCase(Locale.ROOT);
    }

    private String operation(HttpServletRequest request) {
        if (request.getRequestURI().endsWith("/publish")) {
            return "PUBLISH";
        }
        if (request.getRequestURI().endsWith("/unpublish")) {
            return "UNPUBLISH";
        }
        if (request.getRequestURI().endsWith("/status")) {
            return "STATUS";
        }
        return request.getMethod();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
