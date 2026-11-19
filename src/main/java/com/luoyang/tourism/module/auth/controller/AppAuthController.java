package com.luoyang.tourism.module.auth.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.util.RequestUtils;
import com.luoyang.tourism.module.auth.dto.AuthDtos;
import com.luoyang.tourism.module.auth.service.AuthService;
import com.luoyang.tourism.module.auth.vo.AuthVos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/auth")
@Tag(name = "小程序-认证", description = "微信登录、刷新令牌和退出")
public class AppAuthController {
    private final AuthService service;

    @PostMapping("/wechat-login")
    @Operation(summary = "微信小程序登录")
    public ApiResponse<AuthVos.AppLoginVO> login(@Valid @RequestBody AuthDtos.WechatLoginRequest body,
                                                  HttpServletRequest request) {
        return ApiResponse.success(service.wechatLogin(body, RequestUtils.clientIp(request)).body());
    }

    @PostMapping("/refresh")
    @Operation(summary = "轮换小程序 Refresh Token")
    public ApiResponse<AuthVos.AppLoginVO> refresh(@Valid @RequestBody AuthDtos.RefreshRequest body) {
        return ApiResponse.success(service.refreshApp(body).body());
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "退出并作废 Refresh Token")
    public ApiResponse<Void> logout(@Valid @RequestBody AuthDtos.RefreshRequest body,
                                    @AuthenticationPrincipal Jwt jwt) {
        service.appLogout(body.refreshToken());
        return ApiResponse.success();
    }
}
