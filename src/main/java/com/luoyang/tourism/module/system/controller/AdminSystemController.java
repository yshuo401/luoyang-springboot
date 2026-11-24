package com.luoyang.tourism.module.system.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import com.luoyang.tourism.module.system.service.AdminSystemService;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员账号管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/system")
@Tag(name = "管理端-管理员账号")
@SecurityRequirement(name = "bearerAuth")
public class AdminSystemController {
    private final AdminSystemService adminService;

    @GetMapping("/admins")
    @Operation(summary = "管理员分页列表")
    public ApiResponse<PageResult<SystemVos.AdminUserVO>> admins(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(adminService.list(keyword, status, page, pageSize));
    }

    @GetMapping("/admins/{id}")
    @Operation(summary = "管理员详情")
    public ApiResponse<SystemVos.AdminUserVO> admin(@PathVariable Long id) {
        return ApiResponse.success(adminService.detail(id));
    }

    @PostMapping("/admins")
    @Operation(summary = "新建管理员")
    public ApiResponse<SystemVos.AdminUserVO> createAdmin(@Valid @RequestBody SystemDtos.AdminCreateRequest body,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(adminService.create(body, AuthClaims.userId(jwt)));
    }

    @PutMapping("/admins/{id}")
    @Operation(summary = "更新管理员资料")
    public ApiResponse<SystemVos.AdminUserVO> updateAdmin(@PathVariable Long id,
                                                           @Valid @RequestBody SystemDtos.AdminUpdateRequest body,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(adminService.update(id, body, AuthClaims.userId(jwt)));
    }

    @PostMapping("/admins/{id}/reset-password")
    @Operation(summary = "重置管理员密码")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody SystemDtos.AdminResetPasswordRequest body) {
        adminService.resetPassword(id, body);
        return ApiResponse.success();
    }

    @PatchMapping("/admins/{id}/status")
    @Operation(summary = "启用或禁用管理员")
    public ApiResponse<SystemVos.AdminUserVO> adminStatus(@PathVariable Long id,
                                                           @Valid @RequestBody SystemDtos.AdminStatusRequest body,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(adminService.updateStatus(id, body, AuthClaims.userId(jwt)));
    }
}