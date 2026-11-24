package com.luoyang.tourism.unit.security;

import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.security.jwt.JwtConfig;
import com.luoyang.tourism.security.jwt.JwtService;
import com.luoyang.tourism.security.jwt.TokenAudience;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private JwtService jwtService;
    private JwtDecoder appAccessDecoder;
    private JwtDecoder adminAccessDecoder;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.getJwt().setIssuer("luoyang-tourism-test");
        properties.getJwt().setAppSecret("test-app-secret-at-least-thirty-two-bytes");
        properties.getJwt().setAdminSecret("test-admin-secret-at-least-thirty-two-bytes");
        JwtConfig config = new JwtConfig();
        JwtEncoder appEncoder = config.appJwtEncoder(properties);
        JwtEncoder adminEncoder = config.adminJwtEncoder(properties);
        appAccessDecoder = config.appAccessJwtDecoder(properties);
        adminAccessDecoder = config.adminAccessJwtDecoder(properties);
        jwtService = config.jwtService(properties, appEncoder, adminEncoder);
    }

    @Test
    void appAccessTokenHasCorrectAudienceAndRoles() {
        String token = jwtService.issueAccessToken(TokenAudience.APP_USER, 10001L, "tourist",
                Set.of("APP_USER"), Set.of());
        Jwt jwt = appAccessDecoder.decode(token);
        assertThat(jwt.getSubject()).isEqualTo("10001");
        assertThat(jwt.getAudience()).containsExactly("user-wx");
        assertThat(jwt.getClaimAsString("token_type")).isEqualTo("access");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("APP_USER");
    }

    @Test
    void adminAccessTokenCannotBeUsedByAppDecoder() {
        String token = jwtService.issueAccessToken(TokenAudience.ADMIN, 1L, "admin",
                Set.of("ADMIN"), Set.of());
        assertThat(adminAccessDecoder.decode(token).getAudience()).containsExactly("admin-web");
        assertThatThrownBy(() -> appAccessDecoder.decode(token)).isInstanceOf(JwtException.class);
    }
}