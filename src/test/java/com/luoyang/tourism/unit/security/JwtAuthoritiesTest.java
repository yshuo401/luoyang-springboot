package com.luoyang.tourism.unit.security;

import com.luoyang.tourism.config.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthoritiesTest {
    @Test
    void mapsRolesAndPermissionsToAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("1")
                .claim("roles", List.of("ADMIN", "SUPER_ADMIN"))
                .claim("permissions", List.of("content:category:create", "system:admin:read"))
                .build();
        var authentication = new SecurityConfig().jwtAuthenticationConverter().convert(jwt);
        assertThat(authentication.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .contains("ROLE_ADMIN", "ROLE_SUPER_ADMIN",
                        "PERM_content:category:create", "PERM_system:admin:read");
    }
}
