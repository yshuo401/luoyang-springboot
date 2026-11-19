package com.luoyang.tourism.module.content.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import com.luoyang.tourism.module.content.service.AdminBannerService;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * AdminBannerController 控制器。
 * <p>负责接收 HTTP 请求、校验参数，并委托对应的业务接口处理。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/banners")
@Tag(name = "管理端-轮播图", description = "首页轮播图管理")
@SecurityRequirement(name = "bearerAuth")
public class AdminBannerController {
    private final AdminBannerService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_content:banner:read')")
    @Operation(summary = "轮播图分页列表")
    public ApiResponse<PageResult<ContentVos.AdminBanner>> list(
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.list(position, status, page, pageSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_content:banner:read')")
    @Operation(summary = "轮播图详情")
    public ApiResponse<ContentVos.AdminBanner> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_content:banner:create')")
    @Operation(summary = "新建轮播图")
    public ApiResponse<ContentVos.AdminBanner> create(@Valid @RequestBody ContentDtos.BannerUpsertRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.create(request, AuthClaims.userId(jwt)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_content:banner:update')")
    @Operation(summary = "更新轮播图")
    public ApiResponse<ContentVos.AdminBanner> update(@PathVariable Long id,
                                                       @Valid @RequestBody ContentDtos.BannerUpsertRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.update(id, request, AuthClaims.userId(jwt)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PERM_content:banner:update')")
    @Operation(summary = "启用或禁用轮播图")
    public ApiResponse<ContentVos.AdminBanner> status(@PathVariable Long id,
                                                       @Valid @RequestBody ContentDtos.StatusRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateStatus(id, request, AuthClaims.userId(jwt)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_content:banner:delete')")
    @Operation(summary = "软删除轮播图")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestParam("version") Integer version,
                                    @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, version, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }
}
