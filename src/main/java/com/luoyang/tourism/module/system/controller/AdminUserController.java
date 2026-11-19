package com.luoyang.tourism.module.system.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import com.luoyang.tourism.module.system.service.AppUserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminUserController 控制器。
 * <p>负责接收 HTTP 请求、校验参数，并委托对应的业务接口处理。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
@Tag(name = "管理端-小程序用户")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {
    private final AppUserAdminService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_system:user:read')")
    @Operation(summary = "小程序用户分页列表")
    public ApiResponse<PageResult<SystemVos.AppUserAdminVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.list(keyword, status, createdFrom, createdTo, page, pageSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_system:user:read')")
    @Operation(summary = "小程序用户详情")
    public ApiResponse<SystemVos.AppUserAdminVO> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PERM_system:user:update')")
    @Operation(summary = "启用或禁用小程序用户")
    public ApiResponse<SystemVos.AppUserAdminVO> status(@PathVariable Long id,
                                                        @Valid @RequestBody SystemDtos.AppUserStatusRequest body) {
        return ApiResponse.success(service.updateStatus(id, body));
    }
}
