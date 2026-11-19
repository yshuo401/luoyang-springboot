package com.luoyang.tourism.module.appuser.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.module.appuser.pojo.dto.AppUserDtos;
import com.luoyang.tourism.module.appuser.pojo.vo.AppUserVos;
import com.luoyang.tourism.module.appuser.service.AppUserService;
import com.luoyang.tourism.module.auth.pojo.vo.AuthVos;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AppProfileController 控制器。
 * <p>负责接收 HTTP 请求、校验参数，并委托对应的业务接口处理。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/profile")
@Tag(name = "小程序-个人资料", description = "用户资料与显式偏好")
@SecurityRequirement(name = "bearerAuth")
public class AppProfileController {
    private final AppUserService service;

    @GetMapping
    @Operation(summary = "当前用户资料")
    public ApiResponse<AuthVos.AppUserVO> profile(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.profile(AuthClaims.userId(jwt)));
    }

    @PutMapping
    @Operation(summary = "修改当前用户资料")
    public ApiResponse<AuthVos.AppUserVO> updateProfile(@Valid @RequestBody AppUserDtos.ProfileUpdateRequest body,
                                                         @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateProfile(AuthClaims.userId(jwt), body));
    }

    @GetMapping("/preferences")
    @Operation(summary = "获取显式兴趣偏好")
    public ApiResponse<AppUserVos.PreferenceVO> preferences(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.preferences(AuthClaims.userId(jwt)));
    }

    @PutMapping("/preferences")
    @Operation(summary = "替换显式兴趣偏好")
    public ApiResponse<AppUserVos.PreferenceVO> replacePreferences(
            @Valid @RequestBody AppUserDtos.PreferenceUpdateRequest body,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.replacePreferences(AuthClaims.userId(jwt), body));
    }
}
