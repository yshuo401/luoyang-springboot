package com.luoyang.tourism.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Ai ai = new Ai();
    private Internal internal = new Internal();
    private Oss oss = new Oss();
    private Wechat wechat = new Wechat();
    private BootstrapAdmin bootstrapAdmin = new BootstrapAdmin();
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String issuer = "luoyang-tourism";
        private String appSecret;
        private String adminSecret;
        private Duration appAccessTtl = Duration.ofHours(2);
        private Duration appRefreshTtl = Duration.ofDays(30);
        private Duration adminAccessTtl = Duration.ofMinutes(30);
        private Duration adminRefreshTtl = Duration.ofDays(7);
    }

    @Data
    public static class Ai {
        private String baseUrl = "http://127.0.0.1:8000";
        private Duration connectTimeout = Duration.ofSeconds(2);
        private Duration readTimeout = Duration.ofSeconds(28);
    }

    @Data
    public static class Internal {
        private String sharedSecret;
        private long timestampToleranceSeconds = 300;
    }

    @Data
    public static class Oss {
        private String endpoint;
        private String bucket = "luoyang-smart-tourism";
        private String accessKeyId;
        private String accessKeySecret;
        private long signedUrlExpirySeconds = 1800;
        private long maxImageBytes = 10 * 1024 * 1024L;
        private long maxDocumentBytes = 50 * 1024 * 1024L;

        public boolean isConfigured() {
            return endpoint != null && !endpoint.isBlank()
                    && bucket != null && !bucket.isBlank()
                    && accessKeyId != null && !accessKeyId.isBlank()
                    && accessKeySecret != null && !accessKeySecret.isBlank();
        }
    }

    @Data
    public static class Wechat {
        private String appId;
        private String appSecret;
        private boolean mockEnabled = false;
    }

    @Data
    public static class BootstrapAdmin {
        private boolean enabled = false;
        private String username = "admin";
        private String password;
        private String nickname = "系统管理员";
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));
    }
}
