package com.luoyang.tourism.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.support.CachedBodyHttpServletRequest;
import com.luoyang.tourism.support.InternalHmacSigner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * InternalHmacFilter 安全组件。
 * <p>负责 JWT、权限、限流或请求安全校验。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class InternalHmacFilter extends OncePerRequestFilter {
    private static final String TIMESTAMP = "X-Internal-Timestamp";
    private static final String REQUEST_ID = "X-Internal-Request-Id";
    private static final String SIGNATURE = "X-Internal-Signature";

    private final AppProperties properties;
    private final InternalHmacSigner signer;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String timestamp = request.getHeader(TIMESTAMP);
        String requestId = request.getHeader(REQUEST_ID);
        String signature = request.getHeader(SIGNATURE);
        if (blank(timestamp) || blank(requestId) || blank(signature)
                || blank(properties.getInternal().getSharedSecret())) {
            reject(response);
            return;
        }

        // 限制时间窗，降低内部请求被截获后无限重放的风险。
        try {
            long epochSeconds = Long.parseLong(timestamp);
            if (Math.abs(Instant.now().getEpochSecond() - epochSeconds)
                    > properties.getInternal().getTimestampToleranceSeconds()) {
                reject(response);
                return;
            }
        } catch (NumberFormatException exception) {
            reject(response);
            return;
        }

        // 缓存原始请求体，保证验签使用的字节与后续业务反序列化完全一致。
        CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);
        String rawBody = new String(wrapped.body(), StandardCharsets.UTF_8);
        String path = signer.pathWithSortedQuery(wrapped.getRequestURI(), wrapped.getQueryString());
        String canonical = signer.canonical(timestamp, wrapped.getMethod(), path, requestId, rawBody);
        String expected = signer.sign(properties.getInternal().getSharedSecret(), canonical);
        try {
            byte[] expectedBytes = HexFormat.of().parseHex(expected);
            byte[] suppliedBytes = HexFormat.of().parseHex(signature.toLowerCase());
            if (!MessageDigest.isEqual(expectedBytes, suppliedBytes)) {
                reject(response);
                return;
            }
        } catch (IllegalArgumentException exception) {
            reject(response);
            return;
        }
        wrapped.setAttribute("internalVerified", true);
        filterChain.doFilter(wrapped, response);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private void reject(HttpServletResponse response) throws IOException {
        ResultCode code = ResultCode.HMAC_INVALID;
        response.setStatus(code.httpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(code.code(), code.defaultMessage()));
    }
}
