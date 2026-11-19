package com.luoyang.tourism.common.exception;

import com.luoyang.tourism.common.api.ResultCode;

/**
 * BizException 通用组件。
 * <p>为各业务模块提供统一的基础能力。</p>
 */
public class BizException extends RuntimeException {
    private final ResultCode resultCode;

    public BizException(ResultCode resultCode) {
        this(resultCode, resultCode.defaultMessage());
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode resultCode() {
        return resultCode;
    }

    public static BizException notFound() {
        return new BizException(ResultCode.NOT_FOUND);
    }

    public static BizException forbidden() {
        return new BizException(ResultCode.FORBIDDEN);
    }
}
