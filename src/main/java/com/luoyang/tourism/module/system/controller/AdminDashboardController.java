package com.luoyang.tourism.module.system.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.module.system.service.DashboardService;
import com.luoyang.tourism.module.system.vo.SystemVos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "管理端-仪表盘")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {
    private final DashboardService service;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('PERM_dashboard:read')")
    @Operation(summary = "运营统计概览")
    public ApiResponse<SystemVos.DashboardVO> overview() {
        return ApiResponse.success(service.overview());
    }
}
