package com.luoyang.tourism.integration.ai;

/**
 * AiClientException 外部服务集成组件。
 * <p>集中封装第三方 HTTP、对象存储或模型服务调用。</p>
 */
public class AiClientException extends RuntimeException {
    private final int code;
    private final boolean retryable;

    public AiClientException(int code, String message, boolean retryable) {
        super(message);
        this.code = code;
        this.retryable = retryable;
    }

    public int code() {
        return code;
    }

    public boolean retryable() {
        return retryable;
    }
}
