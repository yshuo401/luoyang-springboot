package com.luoyang.tourism.config.security;

import com.luoyang.tourism.config.AppProperties;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内部 HMAC 启动自检。
 * <p>只输出密钥长度和指纹，便于 Java/Python 两侧核对配置，不记录真实密钥。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InternalHmacStartupCheck {
    private final AppProperties properties;

    @PostConstruct
    void validate() {
        String secret = properties.getInternal().getSharedSecret();
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            throw new IllegalStateException("app.internal.shared-secret 缺失或长度不足 32");
        }
        log.info("内部 HMAC 配置已加载 length={} fingerprint={}", secret.length(), fingerprint(secret));
    }

    private String fingerprint(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (Exception exception) {
            return "unavailable";
        }
    }
}