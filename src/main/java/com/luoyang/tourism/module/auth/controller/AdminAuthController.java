package com.luoyang.tourism.module.auth.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.util.RequestUtils;
import com.luoyang.tourism.module.auth.pojo.dto.AuthDtos;
import com.luoyang.tourism.module.auth.pojo.vo.AuthVos;
import com.luoyang.tourism.module.auth.service.AuthService;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端认证接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
@Tag(name = "管理端-认证", description = "管理员登录、资料和改密")
public class AdminAuthController {
    private final AuthService service;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public ApiResponse<AuthVos.AdminLoginVO> login(@Valid @RequestBody AuthDtos.AdminLoginRequest body,
                                                    HttpServletRequest request) {
        return ApiResponse.success(service.adminLogin(body, RequestUtils.clientIp(request)));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "当前管理员资料")
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
}