package com.luoyang.tourism.integration.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.support.InternalHmacSigner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;
import java.time.Instant;

@Service
public class AiClient {
    private static final String TIMESTAMP = "X-Internal-Timestamp";
    private static final String REQUEST_ID = "X-Internal-Request-Id";
    private static final String SIGNATURE = "X-Internal-Signature";

    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final InternalHmacSigner signer;
    private final RestClient restClient;

    public AiClient(AppProperties properties, ObjectMapper objectMapper, InternalHmacSigner signer,
                    RestClient.Builder builder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.signer = signer;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getAi().getConnectTimeout()).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getAi().getReadTimeout());
        this.restClient = builder.baseUrl(properties.getAi().getBaseUrl()).requestFactory(requestFactory).build();
    }

    public AiDtos.ChatResult chat(AiDtos.ChatRequest request, String requestId) {
        JsonNode data = send("POST", "/internal/v1/ai/chat", request, requestId);
        try {
            return objectMapper.treeToValue(data, AiDtos.ChatResult.class);
        } catch (Exception exception) {
            throw new AiClientException(50300, "AI 服务响应格式不正确", false);
        }
    }

    public void resetCheckpoint(String threadId, String requestId) {
        send("POST", "/internal/v1/ai/checkpoints/" + threadId + "/reset", null, requestId);
    }

    public AiDtos.IndexJobResult submitIndexJob(AiDtos.IndexJobRequest request, String requestId) {
        JsonNode data = send("POST", "/internal/v1/ai/index/jobs", request, requestId);
        try {
            return objectMapper.treeToValue(data, AiDtos.IndexJobResult.class);
        } catch (Exception exception) {
            throw new AiClientException(50300, "AI 服务响应格式不正确", false);
        }
    }

    public AiDtos.IndexJobResult queryIndexJob(String jobId, String requestId) {
        JsonNode data = send("GET", "/internal/v1/ai/index/jobs/" + jobId, null, requestId);
        try {
            return objectMapper.treeToValue(data, AiDtos.IndexJobResult.class);
        } catch (Exception exception) {
            throw new AiClientException(50300, "AI 服务响应格式不正确", false);
        }
    }

    public void deleteDocumentVectors(String documentId, String requestId) {
        send("POST", "/internal/v1/ai/index/documents/" + documentId + "/delete", null, requestId);
    }

    private JsonNode send(String method, String path, Object payload, String requestId) {
        String actualRequestId = requestId == null || requestId.isBlank() ? UlidCreator.getUlid().toString() : requestId;
        String rawBody = payload == null ? "" : writeJson(payload);
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String canonical = signer.canonical(timestamp, method, path, actualRequestId, rawBody);
        String signature = signer.sign(properties.getInternal().getSharedSecret(), canonical);
        try {
            String response = restClient.method(org.springframework.http.HttpMethod.valueOf(method))
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(TIMESTAMP, timestamp)
                    .header(REQUEST_ID, actualRequestId)
                    .header(SIGNATURE, signature)
                    .body(rawBody.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response == null ? "{}" : response);
            int code = root.path("code").asInt(-1);
            if (code != 0) {
                throw new AiClientException(code, root.path("message").asText("AI 服务调用失败"), code >= 500);
            }
            return root.path("data");
        } catch (AiClientException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw mapHttpError(exception);
        } catch (ResourceAccessException exception) {
            boolean timeout = exception.getMessage() != null && exception.getMessage().toLowerCase().contains("timeout");
            throw new AiClientException(timeout ? 50302 : 50300, "AI 服务连接失败", true);
        } catch (Exception exception) {
            throw new AiClientException(50300, "AI 服务调用失败", true);
        }
    }

    private AiClientException mapHttpError(RestClientResponseException exception) {
        try {
            JsonNode root = objectMapper.readTree(exception.getResponseBodyAsString());
            int code = root.path("code").asInt(50300);
            return new AiClientException(code, root.path("message").asText("AI 服务调用失败"), code >= 500);
        } catch (Exception ignored) {
            return new AiClientException(exception.getStatusCode().value() >= 500 ? 50300 : 40000,
                    "AI 服务调用失败", exception.getStatusCode().value() >= 500);
        }
    }

    private String writeJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new AiClientException(50000, "内部请求序列化失败", false);
        }
    }
}
