package com.luoyang.tourism.security.jwt;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.config.AppProperties;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/**
 * JWT 签发服务。
 * <p>毕业设计版本只签发 Access Token，不再维护 Refresh Token 和 token_version。</p>
 */
public class JwtService {
    private final AppProperties properties;
    private final JwtEncoder appEncoder;
    private final JwtEncoder adminEncoder;

    public JwtService(AppProperties properties, JwtEncoder appEncoder, JwtEncoder adminEncoder) {
        this.properties = properties;
        this.appEncoder = appEncoder;
        this.adminEncoder = adminEncoder;
    }

    public String issueAccessToken(TokenAudience audience, long subjectId, String username,
                                   Set<String> roles, Set<String> permissions) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(audience == TokenAudience.APP_USER
                ? properties.getJwt().getAppAccessTtl() : properties.getJwt().getAdminAccessTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getJwt().getIssuer())
                .subject(Long.toString(subjectId))
                .audience(List.of(audience.value()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("token_type", "access")
                .claim("username", username)
                .claim("roles", roles == null ? List.of() : List.copyOf(roles))
                .claim("permissions", permissions == null ? List.of() : List.copyOf(permissions))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtEncoder encoder = audience == TokenAudience.APP_USER ? appEncoder : adminEncoder;
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long accessTtlSeconds(TokenAudience audience) {
        return (audience == TokenAudience.APP_USER
                ? properties.getJwt().getAppAccessTtl() : properties.getJwt().getAdminAccessTtl()).toSeconds();
    }
}