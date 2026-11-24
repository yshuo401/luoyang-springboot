package com.luoyang.tourism.integration.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.luoyang.tourism.common.trace.TraceIds;
import com.luoyang.tourism.config.AppProperties;
import java.net.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Spring Boot 到 FastAPI 的内部调用客户端。
 * <p>使用固定内部令牌，不再生成 HMAC、时间戳和防重放信息。</p>
 */
@Slf4j
@Service
public class AiClient {
    private static final String REQUEST_ID = "X-Internal-Request-Id";
    private static final String INTERNAL_TOKEN = "X-Internal-Token";

    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AiClient(AppProperties properties, ObjectMapper objectMapper, RestClient.Builder builder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        HttpClient httpClient = newHttpClient(properties);
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getAi().getReadTimeout());
        this.restClient = builder.baseUrl(properties.getAi().getBaseUrl()).requestFactory(requestFactory).build();
    }

    static HttpClient newHttpClient(AppProperties properties) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(properties.getAi().getConnectTimeout())
                .build();
    }

    public AiDtos.ChatResult chat(AiDtos.ChatRequest request, String requestId) {
        JsonNode data = send("POST", "/internal/v1/ai/chat", request, requestId);
        try {
            return objectMapper.treeToValue(data, AiDtos.ChatResult.class);
        } catch (Exception exception) {
            log.warn("AI 服务响应格式不正确 requestId={} operation=chat", requestId);
            throw new AiClientException(50300, "AI 服务响应格式不正确", false);
        }
    }

    public void resetCheckpoint(String threadId, String requestId) {
        send("POST", "/internal/v1/ai/checkpoints/" + threadId + "/reset", null, requestId);
    }

    public AiDtos.IndexJobResult submitIndexJob(AiDtos.IndexJobRequest request, String requestId) {
        JsonNode data = send("POST", "/internal/v1/ai/index/jobs", request, requestId);
        return toIndexJobResult(data, requestId, "submitIndexJob");
    }

    public AiDtos.IndexJobResult queryIndexJob(String jobId, String requestId) {
        JsonNode data = send("GET", "/internal/v1/ai/index/jobs/" + jobId, null, requestId);
        return toIndexJobResult(data, requestId, "queryIndexJob");
    }

    public void deleteDocumentVectors(String documentId, String requestId) {
        send("POST", "/internal/v1/ai/index/documents/" + documentId + "/delete", null, requestId);
    }

    private AiDtos.IndexJobResult toIndexJobResult(JsonNode data, String requestId, String operation) {
        try {
            return mapIndexJobResult(data);
        } catch (RuntimeException exception) {
            log.warn("AI 服务响应格式不正确 requestId={} operation={}", requestId, operation);
            throw new AiClientException(50300, "AI 服务响应格式不正确", false);
        }
    }

    static AiDtos.IndexJobResult mapIndexJobResult(JsonNode data) {
        Integer totalChunks = optionalInt(data, "totalChunks");
        if (totalChunks == null) {
            totalChunks = optionalInt(data, "chunkCount");
        }
        return new AiDtos.IndexJobResult(
                optionalText(data, "jobId"),
                optionalText(data, "status"),
                optionalText(data, "externalJobId"),
                optionalInt(data, "progress"),
                totalChunks,
                optionalInt(data, "processedChunks"),
                optionalText(data, "errorMessage"));
    }

    private static String optionalText(JsonNode data, String field) {
        JsonNode value = data.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static Integer optionalInt(JsonNode data, String field) {
        JsonNode value = data.get(field);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private JsonNode send(String method, String path, Object payload, String requestId) {
        String actualRequestId = requestId == null || requestId.isBlank()
                ? UlidCreator.getUlid().toString() : requestId;
        try {
            RestClient.RequestBodySpec spec = restClient.method(HttpMethod.valueOf(method))
                    .uri(path)
                    .header(INTERNAL_TOKEN, properties.getInternal().getToken())
                    .header(REQUEST_ID, actualRequestId);
            if (payload != null) {
                spec.contentType(MediaType.APPLICATION_JSON).body(payload);
            }
            String response = spec.retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(response == null ? "{}" : response);
            int code = root.path("code").asInt(-1);
            if (code != 0) {
                boolean retryable = code >= 500;
                logUpstreamFailure(actualRequestId, path, 200, code, retryable);
                throw new AiClientException(code, root.path("message").asText("AI 服务调用失败"), retryable);
            }
            return root.path("data");
        } catch (AiClientException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw mapHttpError(exception, actualRequestId, path);
        } catch (ResourceAccessException exception) {
            boolean timeout = exception.getMessage() != null
                    && exception.getMessage().toLowerCase().contains("timeout");
            log.warn("AI 服务连接失败 traceId={} requestId={} path={} timeout={}",
                    TraceIds.current(), actualRequestId, path, timeout);
            throw new AiClientException(timeout ? 50302 : 50300, "AI 服务连接失败", true);
        } catch (Exception exception) {
            log.warn("AI 服务调用异常 traceId={} requestId={} path={} exceptionType={}",
                    TraceIds.current(), actualRequestId, path, exception.getClass().getSimpleName());
            throw new AiClientException(50300, "AI 服务调用失败", true);
        }
    }

    private AiClientException mapHttpError(RestClientResponseException exception, String requestId, String path) {
        int httpStatus = exception.getStatusCode().value();
        try {
            JsonNode root = objectMapper.readTree(exception.getResponseBodyAsString());
            int fallbackCode = httpStatus >= 500 ? 50300 : 40000;
            int code = root.path("code").asInt(fallbackCode);
            boolean retryable = httpStatus >= 500 && code >= 500;
            logUpstreamFailure(requestId, path, httpStatus, code, retryable);
            return new AiClientException(code, root.path("message").asText("AI 服务调用失败"), retryable);
        } catch (Exception ignored) {
            int fallbackCode = httpStatus == 401 ? 40110 : (httpStatus >= 500 ? 50300 : 40000);
            logUpstreamFailure(requestId, path, httpStatus, fallbackCode, httpStatus >= 500);
            return new AiClientException(fallbackCode, "AI 服务调用失败", httpStatus >= 500);
        }
    }

    private void logUpstreamFailure(String requestId, String path, int httpStatus, int code, boolean retryable) {
        log.warn("AI 服务上游调用失败 traceId={} requestId={} path={} upstreamHttpStatus={} upstreamCode={} retryable={}",
                TraceIds.current(), requestId, path, httpStatus, code, retryable);
    }
}