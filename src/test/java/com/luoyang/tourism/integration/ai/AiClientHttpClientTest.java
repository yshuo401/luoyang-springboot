package com.luoyang.tourism.integration.ai;

import com.luoyang.tourism.config.AppProperties;
import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AiClientHttpClientTest {
    @Test
    void forcesHttp11ForFastApiCompatibility() {
        HttpClient client = AiClient.newHttpClient(new AppProperties());
        assertThat(client.version()).isEqualTo(HttpClient.Version.HTTP_1_1);
    }
}