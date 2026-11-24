package com.luoyang.tourism.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * AppProperties 应用配置。
 * <p>毕业设计版本只保留运行所必需的配置项。</p>
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Ai ai = new Ai();
    @Valid
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
        private Duration appAccessTtl = Duration.ofDays(7);
        private Duration adminAccessTtl = Duration.ofDays(1);
    }

    @Data
    public static class Ai {
        private String baseUrl = "http://127.0.0.1:8000";
        private Duration connectTimeout = Duration.ofSeconds(2);
        private Duration readTimeout = Duration.ofSeconds(60);
    }

    @Data
    public static class Internal {
        /** FastAPI 与 Spring Boot 共用的简单内部调用令牌。 */
        @NotBlank(message = "app.internal.token 不能为空")
        @Size(min = 8, max = 256, message = "app.internal.token 长度必须为 8-256 个字符")
        private String token;
    }

    @Data
    public static class Oss {
        private boolean enabled = false;
        private String endpoint;
        private String bucket = "luoyang-smart-tourism";
        private String accessKeyId;
        private String accessKeySecret;
        private long signedUrlExpirySeconds = 1800;
        private long maxImageBytes = 10 * 1024 * 1024L;
        private long maxDocumentBytes = 50 * 1024 * 1024L;

        public boolean isConfigured() {
            return enabled && endpoint != null && !endpoint.isBlank()
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