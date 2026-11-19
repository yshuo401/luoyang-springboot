package com.luoyang.tourism.module.auth.service;

import com.luoyang.tourism.security.jwt.TokenAudience;
import java.time.Duration;

/**
 * RefreshTokenStore 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface RefreshTokenStore {
    /** 执行 store 业务操作。 */
    public void store(TokenAudience audience, String jti, long userId, int tokenVersion, Duration ttl);

    /** 执行 consume 业务操作。 */
    public ConsumedToken consume(TokenAudience audience, String jti);

    /** 删除数据。 */
    public void delete(TokenAudience audience, String jti);

    public record ConsumedToken(long userId, int tokenVersion) {
    }
}
