package com.luoyang.tourism.module.content.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.dto.ContentDtos;
import com.luoyang.tourism.module.content.service.AdminSpotService;
import com.luoyang.tourism.module.content.vo.ContentVos;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/spots")
@Tag(name = "管理端-景点", description = "景点草稿、发布、下架和关系维护")
@SecurityRequirement(name = "bearerAuth")
public class AdminSpotController {
    private final AdminSpotService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_content:spot:read')")
    @Operation(summary = "景点分页列表")
    public ApiResponse<PageResult<ContentVos.AdminSpot>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.list(keyword, categoryId, status, page, pageSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_content:spot:read')")
    @Operation(summary = "景点详情（含草稿）")
    public ApiResponse<ContentVos.AdminSpot> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_content:spot:create')")
    @Operation(summary = "新建景点草稿")
    public ApiResponse<ContentVos.AdminSpot> create(@Valid @RequestBody ContentDtos.SpotUpsertRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.create(request, AuthClaims.userId(jwt)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_content:spot:update')")
    @Operation(summary = "更新景点")
    public ApiResponse<ContentVos.AdminSpot> update(@PathVariable Long id,
                                                     @Valid @RequestBody ContentDtos.SpotUpsertRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.update(id, request, AuthClaims.userId(jwt)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('PERM_content:spot:publish')")
    @Operation(summary = "发布景点")
    public ApiResponse<ContentVos.AdminSpot> publish(@PathVariable Long id,
                                                      @RequestParam Integer version,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.publish(id, version, AuthClaims.userId(jwt)));
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('PERM_content:spot:publish')")
    @Operation(summary = "下架景点")
    public ApiResponse<ContentVos.AdminSpot> unpublish(@PathVariable Long id,
                                                        @RequestParam Integer version,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.unpublish(id, version, AuthClaims.userId(jwt)));
    }

    @PatchMapping("/{id}/weight")
    @PreAuthorize("hasAuthority('PERM_content:spot:update')")
    @Operation(summary = "调整推荐运营权重")
    public ApiResponse<ContentVos.AdminSpot> weight(@PathVariable Long id,
                                                     @Valid @RequestBody ContentDtos.WeightRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateWeight(id, request, AuthClaims.userId(jwt)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_content:spot:delete')")
    @Operation(summary = "软删除景点")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestParam Integer version,
                                    @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, version, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }
}
