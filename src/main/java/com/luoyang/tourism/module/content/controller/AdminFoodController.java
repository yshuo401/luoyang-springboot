package com.luoyang.tourism.module.content.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import com.luoyang.tourism.module.content.service.AdminFoodService;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * AdminFoodController 控制器。
 * <p>负责接收 HTTP 请求、校验参数，并委托对应的业务接口处理。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/foods")
@Tag(name = "管理端-美食", description = "美食内容管理")
@SecurityRequirement(name = "bearerAuth")
public class AdminFoodController {
    private final AdminFoodService service;

    @GetMapping
    @Operation(summary = "美食分页列表")
    public ApiResponse<PageResult<ContentVos.AdminFood>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.list(keyword, categoryId, status, page, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "美食详情")
    public ApiResponse<ContentVos.AdminFood> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }

    @PostMapping
    @Operation(summary = "新建美食草稿")
    public ApiResponse<ContentVos.AdminFood> create(@Valid @RequestBody ContentDtos.FoodUpsertRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.create(request, AuthClaims.userId(jwt)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新美食")
    public ApiResponse<ContentVos.AdminFood> update(@PathVariable Long id,
                                                     @Valid @RequestBody ContentDtos.FoodUpsertRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.update(id, request, AuthClaims.userId(jwt)));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布美食")
    public ApiResponse<ContentVos.AdminFood> publish(@PathVariable Long id,
                                                      @RequestParam("version") Integer version,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.publish(id, version, AuthClaims.userId(jwt)));
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "下架美食")
    public ApiResponse<ContentVos.AdminFood> unpublish(@PathVariable Long id,
                                                        @RequestParam("version") Integer version,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.unpublish(id, version, AuthClaims.userId(jwt)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "软删除美食")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestParam("version") Integer version,
                                    @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, version, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }
}
