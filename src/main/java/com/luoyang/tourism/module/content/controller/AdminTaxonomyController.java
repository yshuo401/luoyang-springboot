package com.luoyang.tourism.module.content.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.dto.ContentDtos;
import com.luoyang.tourism.module.content.service.AdminTaxonomyService;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "管理端-分类标签", description = "景点分类、标签与美食分类管理")
@SecurityRequirement(name = "bearerAuth")
public class AdminTaxonomyController {
    private final AdminTaxonomyService service;

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PERM_content:category:read')")
    @Operation(summary = "分类列表")
    public ApiResponse<List<ContentVos.AdminCategory>> categories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(service.categories(keyword, status));
    }

    @GetMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('PERM_content:category:read')")
    @Operation(summary = "分类详情")
    public ApiResponse<ContentVos.AdminCategory> category(@PathVariable Long id) {
        return ApiResponse.success(service.category(id));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('PERM_content:category:create')")
    @Operation(summary = "新建分类")
    public ApiResponse<ContentVos.AdminCategory> createCategory(
            @Valid @RequestBody ContentDtos.CategoryUpsertRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.createCategory(request, AuthClaims.userId(jwt)));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('PERM_content:category:update')")
    @Operation(summary = "更新分类")
    public ApiResponse<ContentVos.AdminCategory> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ContentDtos.CategoryUpsertRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateCategory(id, request, AuthClaims.userId(jwt)));
    }

    @PatchMapping("/categories/{id}/status")
    @PreAuthorize("hasAuthority('PERM_content:category:update')")
    @Operation(summary = "启用或禁用分类")
    public ApiResponse<ContentVos.AdminCategory> updateCategoryStatus(
            @PathVariable Long id,
            @Valid @RequestBody ContentDtos.StatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateCategoryStatus(id, request, AuthClaims.userId(jwt)));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('PERM_content:category:delete')")
    @Operation(summary = "删除分类")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id,
                                            @RequestParam("version") Integer version,
                                            @AuthenticationPrincipal Jwt jwt) {
        service.deleteCategory(id, version, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }

    @GetMapping("/tags")
    @PreAuthorize("hasAuthority('PERM_content:tag:read')")
    @Operation(summary = "标签分页列表")
    public ApiResponse<PageResult<ContentVos.AdminTag>> tags(
            @RequestParam(required = false) Integer tagType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.tags(tagType, keyword, status, page, pageSize));
    }

    @PostMapping("/tags")
    @PreAuthorize("hasAuthority('PERM_content:tag:create')")
    @Operation(summary = "新建标签")
    public ApiResponse<ContentVos.AdminTag> createTag(
            @Valid @RequestBody ContentDtos.TagUpsertRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.createTag(request, AuthClaims.userId(jwt)));
    }

    @PutMapping("/tags/{id}")
    @PreAuthorize("hasAuthority('PERM_content:tag:update')")
    @Operation(summary = "更新标签")
    public ApiResponse<ContentVos.AdminTag> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody ContentDtos.TagUpsertRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateTag(id, request, AuthClaims.userId(jwt)));
    }

    @PatchMapping("/tags/{id}/status")
    @PreAuthorize("hasAuthority('PERM_content:tag:update')")
    @Operation(summary = "启用或禁用标签")
    public ApiResponse<ContentVos.AdminTag> updateTagStatus(
            @PathVariable Long id,
            @Valid @RequestBody ContentDtos.StatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateTagStatus(id, request, AuthClaims.userId(jwt)));
    }

    @DeleteMapping("/tags/{id}")
    @PreAuthorize("hasAuthority('PERM_content:tag:delete')")
    @Operation(summary = "删除标签")
    public ApiResponse<Void> deleteTag(@PathVariable Long id,
                                       @RequestParam("version") Integer version,
                                       @AuthenticationPrincipal Jwt jwt) {
        service.deleteTag(id, version, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }

    @GetMapping("/food-categories")
    @PreAuthorize("hasAuthority('PERM_content:food-category:read')")
    @Operation(summary = "美食分类列表")
    public ApiResponse<List<ContentVos.AdminFoodCategory>> foodCategories(
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(service.foodCategories(status));
    }

    @PostMapping("/food-categories")
    @PreAuthorize("hasAuthority('PERM_content:food-category:create')")
    @Operation(summary = "新建美食分类")
    public ApiResponse<ContentVos.AdminFoodCategory> createFoodCategory(
            @Valid @RequestBody ContentDtos.FoodCategoryUpsertRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.createFoodCategory(request, AuthClaims.userId(jwt)));
    }

    @PutMapping("/food-categories/{id}")
    @PreAuthorize("hasAuthority('PERM_content:food-category:update')")
    @Operation(summary = "更新美食分类")
    public ApiResponse<ContentVos.AdminFoodCategory> updateFoodCategory(
            @PathVariable Long id,
            @Valid @RequestBody ContentDtos.FoodCategoryUpsertRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateFoodCategory(id, request, AuthClaims.userId(jwt)));
    }

    @PatchMapping("/food-categories/{id}/status")
    @PreAuthorize("hasAuthority('PERM_content:food-category:update')")
    @Operation(summary = "启用或禁用美食分类")
    public ApiResponse<ContentVos.AdminFoodCategory> updateFoodCategoryStatus(
            @PathVariable Long id,
            @Valid @RequestBody ContentDtos.StatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateFoodCategoryStatus(id, request, AuthClaims.userId(jwt)));
    }

    @DeleteMapping("/food-categories/{id}")
    @PreAuthorize("hasAuthority('PERM_content:food-category:delete')")
    @Operation(summary = "删除美食分类")
    public ApiResponse<Void> deleteFoodCategory(@PathVariable Long id,
                                                @RequestParam("version") Integer version,
                                                @AuthenticationPrincipal Jwt jwt) {
        service.deleteFoodCategory(id, version, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }
}
