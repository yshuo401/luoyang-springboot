package com.luoyang.tourism.integration.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.luoyang.tourism.common.trace.TraceIds;
import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.support.InternalHmacSigner;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * AiClient 外部服务集成组件。
 * <p>集中封装 Spring Boot 到 FastAPI 的内部 HMAC 调用，不向调用方暴露底层 HTTP 细节。</p>
 */
@Slf4j
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
        HttpClient httpClient = newHttpClient(properties);
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getAi().getReadTimeout());
        this.restClient = builder.baseUrl(properties.getAi().getBaseUrl()).requestFactory(requestFactory).build();
    }

    /**
     * FastAPI/Uvicorn 当前只稳定支持 HTTP/1.1。JDK HttpClient 默认会尝试 h2c 明文升级，
     * 某些 Uvicorn 版本下会导致原始请求体与签名校验不一致，因此这里显式锁定 HTTP/1.1。
     */
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
        try {
            return objectMapper.treeToValue(data, AiDtos.IndexJobResult.class);
        } catch (Exception exception) {
            log.warn("AI 服务响应格式不正确 requestId={} operation=submitIndexJob", requestId);
            throw new AiClientException(50300, "AI 服务响应格式不正确", false);
        }
    }

    public AiDtos.IndexJobResult queryIndexJob(String jobId, String requestId) {
        JsonNode data = send("GET", "/internal/v1/ai/index/jobs/" + jobId, null, requestId);
        try {
            return objectMapper.treeToValue(data, AiDtos.IndexJobResult.class);
        } catch (Exception exception) {
            log.warn("AI 服务响应格式不正确 requestId={} operation=queryIndexJob", requestId);
            throw new AiClientException(50300, "AI 服务响应格式不正确", false);
        }
    }

    public void deleteDocumentVectors(String documentId, String requestId) {
        send("POST", "/internal/v1/ai/index/documents/" + documentId + "/delete", null, requestId);
    }

    private JsonNode send(String method, String path, Object payload, String requestId) {
        String actualRequestId = requestId == null || requestId.isBlank()
                ? UlidCreator.getUlid().toString() : requestId;
        String rawBody = payload == null ? "" : writeJson(payload);
        String timestamp = Long.toString(Instant.now().getEpochSecond());

        // 签名覆盖方法、路径、requestId 和原始请求体；任何一项变化都必须重新计算。
        String canonical = signer.canonical(timestamp, method, path, actualRequestId, rawBody);
        String signature = signer.sign(properties.getInternal().getSharedSecret(), canonical);
        try {
            String response = restClient.method(HttpMethod.valueOf(method))
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(TIMESTAMP, timestamp)
                    .header(REQUEST_ID, actualRequestId)
                    .header(SIGNATURE, signature)
                    .body(rawBody.getBytes(StandardCharsets.UTF_8))
                    .retrieve()
                    .body(String.class);
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
            // 不记录响应正文、问题文本或 Prompt，避免用户隐私与内部提示词进入日志。
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
            boolean retryable = httpStatus >= 500;
            logUpstreamFailure(requestId, path, httpStatus, fallbackCode, retryable);
            return new AiClientException(fallbackCode, "AI 服务调用失败", retryable);
        }
    }

    private void logUpstreamFailure(String requestId, String path, int httpStatus, int code, boolean retryable) {
        // 只记录定位字段，不记录签名、完整问题或响应正文。
        log.warn("AI 服务上游调用失败 traceId={} requestId={} path={} upstreamHttpStatus={} upstreamCode={} retryable={}",
                TraceIds.current(), requestId, path, httpStatus, code, retryable);
    }

    private String writeJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new AiClientException(50000, "内部请求序列化失败", false);
        }
    }
}