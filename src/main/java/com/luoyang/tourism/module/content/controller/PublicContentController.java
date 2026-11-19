package com.luoyang.tourism.module.content.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.module.content.service.PublicContentService;
import com.luoyang.tourism.module.content.vo.ContentVos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public")
@Tag(name = "公共内容", description = "无需登录的只读内容接口")
public class PublicContentController {
    private final PublicContentService service;

    @GetMapping("/home")
    @Operation(summary = "首页聚合")
    public ApiResponse<ContentVos.HomeVO> home() {
        return ApiResponse.success(service.home());
    }

    @GetMapping("/categories")
    @Operation(summary = "景点分类树或扁平列表")
    public ApiResponse<List<ContentVos.CategoryVO>> categories(
            @RequestParam(defaultValue = "false") boolean flat) {
        return ApiResponse.success(service.categories(flat));
    }

    @GetMapping("/spots")
    @Operation(summary = "分页查询已发布景点")
    public ApiResponse<PageResult<ContentVos.SpotCard>> spots(
            @RequestParam(required = false) @Size(max = 64) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tagIds,
            @RequestParam(defaultValue = "HEAT_DESC") String sort,
            @RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.spots(keyword, categoryId, parseIds(tagIds), sort,
                longitude, latitude, page, pageSize));
    }

    @GetMapping("/spots/{id}")
    @Operation(summary = "景点详情")
    public ApiResponse<ContentVos.SpotDetail> spot(@PathVariable Long id) {
        return ApiResponse.success(service.spot(id));
    }

    @GetMapping("/spots/{id}/related")
    @Operation(summary = "相关景点")
    public ApiResponse<List<ContentVos.SpotCard>> related(@PathVariable Long id,
                                                          @RequestParam(defaultValue = "6") Integer limit) {
        return ApiResponse.success(service.relatedSpots(id, limit));
    }

    @GetMapping("/guides")
    @Operation(summary = "分页查询已发布攻略")
    public ApiResponse<PageResult<ContentVos.GuideCard>> guides(
            @RequestParam(required = false) @Size(max = 64) String keyword,
            @RequestParam(defaultValue = "HEAT_DESC") String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.guides(keyword, sort, page, pageSize));
    }

    @GetMapping("/guides/{id}")
    @Operation(summary = "攻略详情及每日行程")
    public ApiResponse<ContentVos.GuideDetail> guide(@PathVariable Long id) {
        return ApiResponse.success(service.guide(id));
    }

    @GetMapping("/foods")
    @Operation(summary = "分页查询已发布美食")
    public ApiResponse<PageResult<ContentVos.FoodCard>> foods(
            @RequestParam(required = false) @Size(max = 64) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @Size(max = 32) String flavor,
            @RequestParam(defaultValue = "HEAT_DESC") String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.foods(keyword, categoryId, flavor, sort, page, pageSize));
    }

    @GetMapping("/foods/{id}")
    @Operation(summary = "美食详情")
    public ApiResponse<ContentVos.FoodDetail> food(@PathVariable Long id) {
        return ApiResponse.success(service.food(id));
    }

    @GetMapping("/search")
    @Operation(summary = "全站关键词搜索")
    public ApiResponse<PageResult<ContentVos.SearchItem>> search(
            @RequestParam @Size(min = 1, max = 64) String keyword,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.search(keyword, type, page, pageSize));
    }

    private List<Long> parseIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(Long::valueOf)
                    .distinct()
                    .limit(10)
                    .toList();
        } catch (NumberFormatException exception) {
            throw new BizException(ResultCode.BAD_REQUEST, "标签 ID 格式不正确");
        }
    }
}
