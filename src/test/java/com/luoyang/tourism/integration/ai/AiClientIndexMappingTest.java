package com.luoyang.tourism.integration.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AiClientIndexMappingTest {
    @Test
    void mapsFastApiChunkCountToJavaTotalChunks() throws Exception {
        var data = new ObjectMapper().readTree("""
                {
                  "jobId": "01M7YBZJX2P1MVT622V4NJWJP7",
                  "externalJobId": "01M7YBZJX2P1MVT622V4NJWJP7",
                  "documentId": "2",
                  "documentVersion": 1,
                  "status": "SUCCEEDED",
                  "progress": 100,
                  "chunkCount": 9,
                  "errorCode": null,
                  "errorMessage": null
                }
                """);

        AiDtos.IndexJobResult result = AiClient.mapIndexJobResult(data);

        assertThat(result.status()).isEqualTo("SUCCEEDED");
        assertThat(result.totalChunks()).isEqualTo(9);
        assertThat(result.externalJobId()).isEqualTo("01M7YBZJX2P1MVT622V4NJWJP7");
    }
}