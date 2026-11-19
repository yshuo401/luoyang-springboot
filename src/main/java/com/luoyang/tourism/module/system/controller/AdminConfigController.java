package com.luoyang.tourism.module.system.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import com.luoyang.tourism.module.system.service.SystemConfigService;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminConfigController 控制器。
 * <p>负责接收 HTTP 请求、校验参数，并委托对应的业务接口处理。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/system/configs")
@Tag(name = "管理端-系统配置")
@SecurityRequirement(name = "bearerAuth")
public class AdminConfigController {
    private final SystemConfigService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_system:config:read')")
    @Operation(summary = "系统配置分页列表")
    public ApiResponse<PageResult<SystemVos.ConfigVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.list(keyword, page, pageSize));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAuthority('PERM_system:config:update')")
    @Operation(summary = "更新非敏感系统配置")
    public ApiResponse<SystemVos.ConfigVO> update(@PathVariable String key,
                                                   @Valid @RequestBody SystemDtos.ConfigUpdateRequest body,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.update(key, body, AuthClaims.userId(jwt)));
    }
}
