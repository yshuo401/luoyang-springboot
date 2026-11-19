package com.luoyang.tourism.integration.ai;

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
