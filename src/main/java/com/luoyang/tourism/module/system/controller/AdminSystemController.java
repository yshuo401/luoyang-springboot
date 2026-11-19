package com.luoyang.tourism.module.system.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.system.dto.SystemDtos;
import com.luoyang.tourism.module.system.service.AdminSystemService;
import com.luoyang.tourism.module.system.service.RbacService;
import com.luoyang.tourism.module.system.vo.SystemVos;
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
@RequestMapping("/api/v1/admin/system")
@Tag(name = "管理端-管理员与 RBAC")
@SecurityRequirement(name = "bearerAuth")
public class AdminSystemController {
    private final AdminSystemService adminService;
    private final RbacService rbacService;

    @GetMapping("/admins")
    @PreAuthorize("hasAuthority('PERM_system:admin:read')")
    @Operation(summary = "管理员分页列表")
    public ApiResponse<PageResult<SystemVos.AdminUserVO>> admins(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(adminService.list(keyword, status, page, pageSize));
    }

    @GetMapping("/admins/{id}")
    @PreAuthorize("hasAuthority('PERM_system:admin:read')")
    @Operation(summary = "管理员详情")
    public ApiResponse<SystemVos.AdminUserVO> admin(@PathVariable Long id) {
        return ApiResponse.success(adminService.detail(id));
    }

    @PostMapping("/admins")
    @PreAuthorize("hasAuthority('PERM_system:admin:create')")
    @Operation(summary = "新建管理员")
    public ApiResponse<SystemVos.AdminUserVO> createAdmin(@Valid @RequestBody SystemDtos.AdminCreateRequest body,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(adminService.create(body, AuthClaims.userId(jwt)));
    }

    @PutMapping("/admins/{id}")
    @PreAuthorize("hasAuthority('PERM_system:admin:update')")
    @Operation(summary = "更新管理员资料和角色")
    public ApiResponse<SystemVos.AdminUserVO> updateAdmin(@PathVariable Long id,
                                                           @Valid @RequestBody SystemDtos.AdminUpdateRequest body,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(adminService.update(id, body, AuthClaims.userId(jwt)));
    }

    @PostMapping("/admins/{id}/reset-password")
    @PreAuthorize("hasAuthority('PERM_system:admin:reset-password')")
    @Operation(summary = "重置管理员密码")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody SystemDtos.AdminResetPasswordRequest body) {
        adminService.resetPassword(id, body);
        return ApiResponse.success();
    }

    @PatchMapping("/admins/{id}/status")
    @PreAuthorize("hasAuthority('PERM_system:admin:update')")
    @Operation(summary = "启用或禁用管理员")
    public ApiResponse<SystemVos.AdminUserVO> adminStatus(@PathVariable Long id,
                                                           @Valid @RequestBody SystemDtos.AdminStatusRequest body,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(adminService.updateStatus(id, body, AuthClaims.userId(jwt)));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('PERM_system:role:read')")
    @Operation(summary = "角色列表")
    public ApiResponse<List<SystemVos.RoleVO>> roles(@RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) Integer status) {
        return ApiResponse.success(rbacService.roles(keyword, status));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('PERM_system:role:create')")
    @Operation(summary = "新建角色")
    public ApiResponse<SystemVos.RoleVO> createRole(@Valid @RequestBody SystemDtos.RoleUpsertRequest body,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(rbacService.createRole(body, AuthClaims.userId(jwt)));
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('PERM_system:role:update')")
    @Operation(summary = "更新角色")
    public ApiResponse<SystemVos.RoleVO> updateRole(@PathVariable Long id,
                                                    @Valid @RequestBody SystemDtos.RoleUpsertRequest body,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(rbacService.updateRole(id, body, AuthClaims.userId(jwt)));
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('PERM_system:role:delete')")
    @Operation(summary = "删除角色")
    public ApiResponse<Void> deleteRole(@PathVariable Long id, @RequestParam("version") Integer version,
                                        @AuthenticationPrincipal Jwt jwt) {
        rbacService.deleteRole(id, version, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('PERM_system:role:assign')")
    @Operation(summary = "替换角色权限")
    public ApiResponse<Void> permissions(@PathVariable Long id,
                                         @Valid @RequestBody SystemDtos.RolePermissionRequest body,
                                         @AuthenticationPrincipal Jwt jwt) {
        rbacService.replacePermissions(id, body, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }

    @GetMapping("/permissions/tree")
    @PreAuthorize("hasAuthority('PERM_system:permission:read')")
    @Operation(summary = "权限树")
    public ApiResponse<List<SystemVos.PermissionVO>> permissionTree() {
        return ApiResponse.success(rbacService.permissionTree());
    }
}
