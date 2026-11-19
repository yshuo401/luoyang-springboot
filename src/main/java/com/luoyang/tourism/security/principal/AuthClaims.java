package com.luoyang.tourism.security.principal;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * AuthClaims 认证声明读取工具。
 */
public final class AuthClaims {
    private AuthClaims() {
    }

    public static long userId(Jwt jwt) {
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (RuntimeException exception) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
    }

    public static int tokenVersion(Jwt jwt) {
        Long value = jwt.getClaim("token_version");
        return value == null ? 0 : value.intValue();
    }

    public static String username(Jwt jwt) {
        String value = jwt.getClaimAsString("username");
        return value == null ? "" : value;
    }
}
