package com.luoyang.tourism.security.jwt;

import com.github.f4b6a3.ulid.UlidCreator;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.config.AppProperties;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * JwtService 安全组件。
 * <p>负责 JWT、权限、限流或请求安全校验。</p>
 */
public class JwtService {
    private final AppProperties properties;
    private final JwtEncoder appEncoder;
    private final JwtEncoder adminEncoder;
    private final JwtDecoder appRefreshDecoder;
    private final JwtDecoder adminRefreshDecoder;

    public JwtService(AppProperties properties, JwtEncoder appEncoder, JwtEncoder adminEncoder,
                      JwtDecoder appRefreshDecoder, JwtDecoder adminRefreshDecoder) {
        this.properties = properties;
        this.appEncoder = appEncoder;
        this.adminEncoder = adminEncoder;
        this.appRefreshDecoder = appRefreshDecoder;
        this.adminRefreshDecoder = adminRefreshDecoder;
    }

    public String issueAccessToken(TokenAudience audience, long subjectId, String username,
                                   int tokenVersion, Set<String> roles, Set<String> permissions) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(audience == TokenAudience.APP_USER
                ? properties.getJwt().getAppAccessTtl() : properties.getJwt().getAdminAccessTtl());
        JwtClaimsSet claims = baseClaims(audience, subjectId, username, tokenVersion, now, expiresAt)
                .claim("token_type", "access")
                .claim("roles", roles == null ? List.of() : List.copyOf(roles))
                .claim("permissions", permissions == null ? List.of() : List.copyOf(permissions))
                .build();
        return encode(audience, claims);
    }

    public String issueRefreshToken(TokenAudience audience, long subjectId, String username, int tokenVersion) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(audience == TokenAudience.APP_USER
                ? properties.getJwt().getAppRefreshTtl() : properties.getJwt().getAdminRefreshTtl());
        JwtClaimsSet claims = baseClaims(audience, subjectId, username, tokenVersion, now, expiresAt)
                .claim("token_type", "refresh")
                .id(UlidCreator.getUlid().toString())
                .claim("roles", List.of())
                .claim("permissions", List.of())
                .build();
        return encode(audience, claims);
    }

    public Jwt decodeRefreshToken(String token, TokenAudience audience) {
        try {
            Jwt jwt = (audience == TokenAudience.APP_USER ? appRefreshDecoder : adminRefreshDecoder).decode(token);
            return jwt;
        } catch (JwtException exception) {
            throw new BizException(ResultCode.REFRESH_INVALID);
        }
    }

    public long refreshTtlSeconds(TokenAudience audience) {
        return (audience == TokenAudience.APP_USER
                ? properties.getJwt().getAppRefreshTtl() : properties.getJwt().getAdminRefreshTtl()).toSeconds();
    }

    private JwtClaimsSet.Builder baseClaims(TokenAudience audience, long subjectId, String username,
                                            int tokenVersion, Instant issuedAt, Instant expiresAt) {
        return JwtClaimsSet.builder()
                .issuer(properties.getJwt().getIssuer())
                .subject(Long.toString(subjectId))
                .audience(List.of(audience.value()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("username", username)
                .claim("token_version", tokenVersion);
    }

    private String encode(TokenAudience audience, JwtClaimsSet claims) {
        JwtEncoder encoder = audience == TokenAudience.APP_USER ? appEncoder : adminEncoder;
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
