package com.luoyang.tourism.common.exception;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.trace.TraceIds;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * GlobalExceptionHandler 通用组件。
 * <p>为各业务模块提供统一的基础能力。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBiz(BizException exception) {
        ResultCode code = exception.resultCode();
        return ResponseEntity.status(code.httpStatus()).body(ApiResponse.error(code.code(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct().collect(Collectors.joining("; "));
        return badRequest(message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBind(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct().collect(Collectors.joining("; "));
        return badRequest(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        return badRequest(message);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        return badRequest("请求参数格式不正确");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateKeyException exception) {
        return ResponseEntity.status(ResultCode.UNIQUE_CONFLICT.httpStatus())
                .body(ApiResponse.error(ResultCode.UNIQUE_CONFLICT.code(), ResultCode.UNIQUE_CONFLICT.defaultMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleIntegrity(DataIntegrityViolationException exception) {
        log.warn("database integrity failure traceId={}", TraceIds.current());
        return ResponseEntity.status(ResultCode.DATA_INCONSISTENCY.httpStatus())
                .body(ApiResponse.error(ResultCode.DATA_INCONSISTENCY.code(), ResultCode.DATA_INCONSISTENCY.defaultMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleDenied(AccessDeniedException exception) {
        return ResponseEntity.status(ResultCode.FORBIDDEN.httpStatus())
                .body(ApiResponse.error(ResultCode.FORBIDDEN.code(), ResultCode.FORBIDDEN.defaultMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRoute(NoResourceFoundException exception) {
        return ResponseEntity.status(ResultCode.NOT_FOUND.httpStatus())
                .body(ApiResponse.error(ResultCode.NOT_FOUND.code(), ResultCode.NOT_FOUND.defaultMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("unhandled exception traceId={}", TraceIds.current(), exception);
        return ResponseEntity.status(ResultCode.INTERNAL_ERROR.httpStatus())
                .body(ApiResponse.error(ResultCode.INTERNAL_ERROR.code(), ResultCode.INTERNAL_ERROR.defaultMessage()));
    }

    private ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        String safeMessage = message == null || message.isBlank() ? ResultCode.BAD_REQUEST.defaultMessage() : message;
        return ResponseEntity.status(ResultCode.BAD_REQUEST.httpStatus())
                .body(ApiResponse.error(ResultCode.BAD_REQUEST.code(), safeMessage));
    }
}
