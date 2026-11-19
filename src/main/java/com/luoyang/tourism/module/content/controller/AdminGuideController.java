package com.luoyang.tourism.module.content.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import com.luoyang.tourism.module.content.service.AdminGuideService;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminGuideController 控制器。
 * <p>负责接收 HTTP 请求、校验参数，并委托对应的业务接口处理。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/guides")
@Tag(name = "管理端-攻略", description = "攻略与每日行程管理")
@SecurityRequirement(name = "bearerAuth")
public class AdminGuideController {
    private final AdminGuideService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_content:guide:read')")
    @Operation(summary = "攻略分页列表")
    public ApiResponse<PageResult<ContentVos.AdminGuide>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.list(keyword, status, page, pageSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_content:guide:read')")
    @Operation(summary = "攻略详情")
    public ApiResponse<ContentVos.AdminGuide> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_content:guide:create')")
    @Operation(summary = "新建攻略草稿")
    public ApiResponse<ContentVos.AdminGuide> create(@Valid @RequestBody ContentDtos.GuideUpsertRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.create(request, AuthClaims.userId(jwt)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_content:guide:update')")
    @Operation(summary = "更新攻略")
    public ApiResponse<ContentVos.AdminGuide> update(@PathVariable Long id,
                                                      @Valid @RequestBody ContentDtos.GuideUpsertRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.update(id, request, AuthClaims.userId(jwt)));
    }

    @PutMapping("/{id}/days")
    @PreAuthorize("hasAuthority('PERM_content:guide:update')")
    @Operation(summary = "原子替换每日行程")
    public ApiResponse<ContentVos.AdminGuide> replaceDays(@PathVariable Long id,
                                                           @RequestBody List<ContentDtos.DayRequest> days,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.replaceDays(id, days, AuthClaims.userId(jwt)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('PERM_content:guide:publish')")
    @Operation(summary = "发布攻略")
    public ApiResponse<ContentVos.AdminGuide> publish(@PathVariable Long id,
                                                       @RequestParam("version") Integer version,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.publish(id, version, AuthClaims.userId(jwt)));
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('PERM_content:guide:publish')")
    @Operation(summary = "下架攻略")
    public ApiResponse<ContentVos.AdminGuide> unpublish(@PathVariable Long id,
                                                         @RequestParam("version") Integer version,
                                                         @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.unpublish(id, version, AuthClaims.userId(jwt)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_content:guide:delete')")
    @Operation(summary = "软删除攻略")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestParam("version") Integer version,
                                    @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, version, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }
}
