package com.luoyang.tourism.unit.integration;

import com.luoyang.tourism.support.InternalHmacSigner;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InternalHmacSignerTest {
    private final InternalHmacSigner signer = new InternalHmacSigner();

    @Test
    void sortsQueryParametersAndProducesDeterministicSignature() {
        String path = signer.pathWithSortedQuery("/internal/v1/ai/spots/search", "limit=5&keyword=龙门&tagIds=2,1");
        String canonical = signer.canonical("1763445600", "get", path, "request-1", "");
        String first = signer.sign("internal-test-secret-at-least-thirty-two-bytes", canonical);
        String second = signer.sign("internal-test-secret-at-least-thirty-two-bytes", canonical);
        assertThat(path).isEqualTo("/internal/v1/ai/spots/search?keyword=龙门&limit=5&tagIds=2,1");
        assertThat(first).hasSize(64).matches("[0-9a-f]+").isEqualTo(second);
    }

    @Test
    void bodyChangeChangesSignature() {
        String canonicalA = signer.canonical("1763445600", "POST", "/internal/v1/ai/chat", "req", "{\"a\":1}");
        String canonicalB = signer.canonical("1763445600", "POST", "/internal/v1/ai/chat", "req", "{\"a\":2}");
        String secret = "internal-test-secret-at-least-thirty-two-bytes";
        assertThat(signer.sign(secret, canonicalA)).isNotEqualTo(signer.sign(secret, canonicalB));
    }
}
