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
    void matchesFixedPythonHmacVector() {
        String rawBody = "{\"threadId\":\"01ARZ3NDEKTSV4RRFFQ69G5FAV\",\"message\":\"龙门石窟适合玩多久？\"}";
        String canonical = signer.canonical("1700000000", "POST", "/internal/v1/ai/chat", "request-123", rawBody);

        // 期望值由 ai-service 的 build_canonical_request + calculate_signature 对同一输入生成。
        assertThat(signer.sign("test-secret", canonical))
                .isEqualTo("fe230c6e106606a2ccba338faab2808ad9fa1ae1811178d8229bae3e8d3918d4");
    }

    @Test
    void bodyChangeChangesSignature() {
        String canonicalA = signer.canonical("1763445600", "POST", "/internal/v1/ai/chat", "req", "{\"a\":1}");
        String canonicalB = signer.canonical("1763445600", "POST", "/internal/v1/ai/chat", "req", "{\"a\":2}");
        String secret = "internal-test-secret-at-least-thirty-two-bytes";
        assertThat(signer.sign(secret, canonicalA)).isNotEqualTo(signer.sign(secret, canonicalB));
    }
}
