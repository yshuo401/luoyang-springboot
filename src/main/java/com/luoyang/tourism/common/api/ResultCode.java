package com.luoyang.tourism.common.api;

import org.springframework.http.HttpStatus;

/**
 * ResultCode 通用组件。
 * <p>为各业务模块提供统一的基础能力。</p>
 */
public enum ResultCode {
    SUCCESS(0, HttpStatus.OK, "success"),
    BAD_REQUEST(40000, HttpStatus.BAD_REQUEST, "参数校验失败"),
    UNSUPPORTED_VALUE(40001, HttpStatus.BAD_REQUEST, "不支持的枚举或排序"),
    UNAUTHORIZED(40100, HttpStatus.UNAUTHORIZED, "请先登录"),
    TOKEN_INVALID(40101, HttpStatus.UNAUTHORIZED, "登录凭证无效或已过期"),
    REFRESH_INVALID(40102, HttpStatus.UNAUTHORIZED, "刷新令牌无效或已使用"),
    ADMIN_DISABLED(40103, HttpStatus.UNAUTHORIZED, "管理员账号不可用"),
    HMAC_INVALID(40110, HttpStatus.UNAUTHORIZED, "内部服务签名校验失败"),
    FORBIDDEN(40300, HttpStatus.FORBIDDEN, "权限不足"),
    RESOURCE_NOT_OWNED(40301, HttpStatus.FORBIDDEN, "资源不属于当前用户"),
    TOKEN_TYPE_MISMATCH(40302, HttpStatus.FORBIDDEN, "Token 类型与路由不匹配"),
    NOT_FOUND(40400, HttpStatus.NOT_FOUND, "资源不存在"),
    CHECKPOINT_NOT_FOUND(40410, HttpStatus.NOT_FOUND, "AI 会话状态不存在"),
    UNIQUE_CONFLICT(40900, HttpStatus.CONFLICT, "数据唯一性冲突"),
    IDEMPOTENCY_CONFLICT(40901, HttpStatus.CONFLICT, "幂等键重复但请求内容不一致"),
    VERSION_CONFLICT(40902, HttpStatus.CONFLICT, "数据已被其他人修改，请刷新后重试"),
    AI_BUSY(40903, HttpStatus.CONFLICT, "当前会话正在生成回答"),
    RATE_LIMITED(42900, HttpStatus.TOO_MANY_REQUESTS, "请求过于频繁"),
    INTERNAL_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "服务内部错误"),
    DATA_INCONSISTENCY(50001, HttpStatus.INTERNAL_SERVER_ERROR, "数据一致性错误"),
    AI_UNAVAILABLE(50300, HttpStatus.SERVICE_UNAVAILABLE, "智能问答暂时不可用"),
    VECTOR_UNAVAILABLE(50301, HttpStatus.SERVICE_UNAVAILABLE, "知识检索暂时不可用"),
    MODEL_TIMEOUT(50302, HttpStatus.SERVICE_UNAVAILABLE, "模型调用超时或限流"),
    OSS_UNAVAILABLE(50303, HttpStatus.SERVICE_UNAVAILABLE, "文件服务暂时不可用");

    private final int code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ResultCode(int code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int code() {
        return code;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
