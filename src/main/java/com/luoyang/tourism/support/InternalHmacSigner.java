package com.luoyang.tourism.support;

import com.luoyang.tourism.security.jwt.TokenAudience;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.stream.Collectors;

@Component
public class InternalHmacSigner {
    public String canonical(String timestamp, String method, String pathWithSortedQuery,
                            String requestId, String rawBody) {
        return timestamp + "\n" + method.toUpperCase() + "\n" + pathWithSortedQuery + "\n"
                + requestId + "\n" + (rawBody == null ? "" : rawBody);
    }

    public String pathWithSortedQuery(String path, String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return path;
        }
        String sorted = Arrays.stream(rawQuery.split("&"))
                .filter(part -> !part.isBlank())
                .sorted()
                .collect(Collectors.joining("&"));
        return path + "?" + sorted;
    }

    public String sign(String secret, String canonical) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to calculate internal HMAC", exception);
        }
    }
}
