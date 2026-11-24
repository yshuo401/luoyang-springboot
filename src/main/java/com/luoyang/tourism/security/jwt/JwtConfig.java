package com.luoyang.tourism.security.jwt;

import com.luoyang.tourism.config.AppProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * JWT 配置。
 * <p>只保留 App/Admin 两类 Access Token，两者通过 audience 隔离。</p>
 */
@Configuration
public class JwtConfig {

    @Bean
    public JwtEncoder appJwtEncoder(AppProperties properties) {
        return encoder(key(properties.getJwt().getAppSecret(), "JWT_APP_SECRET"));
    }

    @Bean
    public JwtEncoder adminJwtEncoder(AppProperties properties) {
        return encoder(key(properties.getJwt().getAdminSecret(), "JWT_ADMIN_SECRET"));
    }

    @Bean("appAccessJwtDecoder")
    public JwtDecoder appAccessJwtDecoder(AppProperties properties) {
        return decoder(properties, properties.getJwt().getAppSecret(), TokenAudience.APP_USER);
    }

    @Bean("adminAccessJwtDecoder")
    public JwtDecoder adminAccessJwtDecoder(AppProperties properties) {
        return decoder(properties, properties.getJwt().getAdminSecret(), TokenAudience.ADMIN);
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder(@Qualifier("appAccessJwtDecoder") JwtDecoder app,
                                 @Qualifier("adminAccessJwtDecoder") JwtDecoder admin) {
        return new AudienceDelegatingJwtDecoder(app, admin);
    }

    @Bean
    public JwtService jwtService(AppProperties properties,
                                 JwtEncoder appJwtEncoder,
                                 JwtEncoder adminJwtEncoder) {
        return new JwtService(properties, appJwtEncoder, adminJwtEncoder);
    }

    private JwtEncoder encoder(SecretKey key) {
        JWKSource<SecurityContext> source = new ImmutableJWKSet<>(
                new JWKSet(new OctetSequenceKey.Builder(key).algorithm(JWSAlgorithm.HS256).build()));
        return new NimbusJwtEncoder(source);
    }

    private JwtDecoder decoder(AppProperties properties, String secret, TokenAudience audience) {
        SecretKey key = key(secret, audience == TokenAudience.APP_USER ? "JWT_APP_SECRET" : "JWT_ADMIN_SECRET");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>("aud",
                values -> values != null && values.contains(audience.value()));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.getJwt().getIssuer()),
                audienceValidator));
        return decoder;
    }

    private SecretKey key(String secret, String variableName) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(variableName + " must contain at least 32 UTF-8 bytes");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}