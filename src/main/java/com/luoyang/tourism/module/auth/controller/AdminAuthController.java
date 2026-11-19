package com.luoyang.tourism.module.auth.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.RequestUtils;
import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.module.auth.dto.AuthDtos;
import com.luoyang.tourism.module.auth.service.AuthService;
import com.luoyang.tourism.module.auth.vo.AuthVos;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
@Tag(name = "管理端-认证", description = "管理员登录、刷新、退出和改密")
public class AdminAuthController {
    public static final String REFRESH_COOKIE = "admin_refresh_token";

    private final AuthService service;
    private final AppProperties properties;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public ApiResponse<AuthVos.AdminLoginVO> login(@Valid @RequestBody AuthDtos.AdminLoginRequest body,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {
        AuthService.AdminLoginResult result = service.adminLogin(body, RequestUtils.clientIp(request));
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken(), request.isSecure()).toString());
        return ApiResponse.success(result.body());
    }

    @PostMapping("/refresh")
    @Operation(summary = "轮换管理员 Refresh Cookie")
    public ApiResponse<AuthVos.AdminLoginVO> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BizException(ResultCode.REFRESH_INVALID);
        }
        AuthService.AdminLoginResult result = service.refreshAdmin(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken(), request.isSecure()).toString());
        return ApiResponse.success(result.body());
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员退出")
    public ApiResponse<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        service.adminLogout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie(request.isSecure()).toString());
        return ApiResponse.success();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "当前管理员资料和权限")
    public ApiResponse<AuthVos.AdminProfileVO> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.adminProfile(AuthClaims.userId(jwt)));
    }

    @PutMapping("/password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "修改当前管理员密码")
    public ApiResponse<Void> password(@Valid @RequestBody AuthDtos.ChangePasswordRequest body,
                                      @AuthenticationPrincipal Jwt jwt) {
        service.changeAdminPassword(AuthClaims.userId(jwt), body);
        return ApiResponse.success();
    }

    private ResponseCookie refreshCookie(String token, boolean secure) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true).secure(secure).sameSite("Strict")
                .path("/api/v1/admin/auth")
                .maxAge(properties.getJwt().getAdminRefreshTtl())
                .build();
    }

    private ResponseCookie expiredCookie(boolean secure) {
        return ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true).secure(secure).sameSite("Strict")
                .path("/api/v1/admin/auth").maxAge(0).build();
    }
}
