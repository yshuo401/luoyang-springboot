package com.luoyang.tourism.module.appuser.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.appuser.dto.AppUserDtos;
import com.luoyang.tourism.module.appuser.service.AppUserService;
import com.luoyang.tourism.module.appuser.service.RecommendationService;
import com.luoyang.tourism.module.appuser.vo.AppUserVos;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app")
@Tag(name = "小程序-互动与推荐", description = "收藏、行为上报和确定性推荐")
@SecurityRequirement(name = "bearerAuth")
public class AppActivityController {
    private final AppUserService userService;
    private final RecommendationService recommendationService;

    @GetMapping("/favorites")
    @Operation(summary = "收藏分页列表")
    public ApiResponse<PageResult<AppUserVos.FavoriteVO>> favorites(
            @RequestParam(required = false) String bizType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(userService.favorites(AuthClaims.userId(jwt), bizType, page, pageSize));
    }

    @PostMapping("/favorites")
    @Operation(summary = "添加收藏（幂等）")
    public ApiResponse<AppUserVos.FavoriteVO> addFavorite(
            @Valid @RequestBody AppUserDtos.FavoriteRequest body,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(userService.addFavorite(AuthClaims.userId(jwt), body));
    }

    @DeleteMapping("/favorites/{bizType}/{bizId}")
    @Operation(summary = "取消收藏（幂等）")
    public ApiResponse<Void> removeFavorite(@PathVariable String bizType, @PathVariable Long bizId,
                                            @AuthenticationPrincipal Jwt jwt) {
        userService.removeFavorite(AuthClaims.userId(jwt), bizType, bizId);
        return ApiResponse.success();
    }

    @PostMapping("/behaviors")
    @Operation(summary = "上报用户行为")
    public ApiResponse<Void> reportBehavior(@Valid @RequestBody AppUserDtos.BehaviorRequest body,
                                            @AuthenticationPrincipal Jwt jwt) {
        userService.reportBehavior(AuthClaims.userId(jwt), body);
        return ApiResponse.success();
    }

    @GetMapping("/recommendations")
    @Operation(summary = "个性化景点推荐")
    public ApiResponse<List<AppUserVos.RecommendationVO>> recommendations(
            @RequestParam(defaultValue = "HOME") String scene,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(recommendationService.recommend(AuthClaims.userId(jwt), scene, limit,
                categoryId, longitude, latitude));
    }
}
