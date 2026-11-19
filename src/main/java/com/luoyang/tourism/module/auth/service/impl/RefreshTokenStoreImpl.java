package com.luoyang.tourism.module.auth.service.impl;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.module.auth.service.RefreshTokenStore;
import com.luoyang.tourism.security.jwt.TokenAudience;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * RefreshTokenStoreImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenStoreImpl implements RefreshTokenStore {
    private static final String PREFIX = "tourism:spring:auth:refresh:";

    private final StringRedisTemplate redisTemplate;

@Override
public void store(TokenAudience audience, String jti, long userId, int tokenVersion, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key(audience, jti), userId + ":" + tokenVersion, ttl);
        } catch (RuntimeException exception) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "认证服务暂时不可用");
        }
    }

@Override
public ConsumedToken consume(TokenAudience audience, String jti) {
        if (jti == null || jti.isBlank()) {
            return null;
        }
        try {
            String value = redisTemplate.opsForValue().getAndDelete(key(audience, jti));
            if (value == null) {
                return null;
            }
            String[] parts = value.split(":", 2);
            return new ConsumedToken(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
        } catch (RuntimeException exception) {
            return null;
        }
    }

@Override
public void delete(TokenAudience audience, String jti) {
        if (jti != null) {
            redisTemplate.delete(key(audience, jti));
        }
    }

    private String key(TokenAudience audience, String jti) {
        return PREFIX + audience.value() + ":" + jti;
    }

}
