package com.luoyang.tourism.security.jwt;

import com.nimbusds.jwt.JWTParser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.text.ParseException;

public class AudienceDelegatingJwtDecoder implements JwtDecoder {
    private final JwtDecoder appDecoder;
    private final JwtDecoder adminDecoder;

    public AudienceDelegatingJwtDecoder(JwtDecoder appDecoder, JwtDecoder adminDecoder) {
        this.appDecoder = appDecoder;
        this.adminDecoder = adminDecoder;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        boolean adminAudience = false;
        try {
            var claims = JWTParser.parse(token).getJWTClaimsSet();
            adminAudience = claims.getAudience() != null
                    && claims.getAudience().contains(TokenAudience.ADMIN.value());
        } catch (ParseException ignored) {
            // The selected decoder returns the stable JWT error.
        }
        return adminAudience ? adminDecoder.decode(token) : appDecoder.decode(token);
    }
}
