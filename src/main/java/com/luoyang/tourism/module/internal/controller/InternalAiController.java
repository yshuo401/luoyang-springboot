package com.luoyang.tourism.module.internal.pojo.dto;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.module.aichat.service.AiChatService;
import com.luoyang.tourism.module.appuser.pojo.vo.AppUserVos;
import com.luoyang.tourism.module.appuser.service.RecommendationService;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import com.luoyang.tourism.module.content.service.PublicContentService;
import com.luoyang.tourism.module.knowledge.pojo.dto.KnowledgeDtos;
import com.luoyang.tourism.module.knowledge.service.KnowledgePersistenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * InternalAiController 控制器。
 * <p>负责接收 HTTP 请求、校验参数，并委托对应的业务接口处理。</p>
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/ai")
@Tag(name = "内部 AI 数据", description = "仅供 FastAPI 通过 X-Internal-Token 调用")
public class InternalAiController {
    private final PublicContentService contentService;
    private final RecommendationService recommendationService;
    private final AiChatService chatService;
    private final KnowledgePersistenceService knowledgePersistenceService;

    @GetMapping("/spots/search")
    @Operation(summary = "搜索已发布景点")
    public ApiResponse<List<InternalAiDtos.SpotSummary>> searchSpots(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tagIds,
            @RequestParam(defaultValue = "5") Integer limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 10);
        List<InternalAiDtos.SpotSummary> items = contentService.spots(keyword, categoryId, parseIds(tagIds),
                        "HEAT_DESC", null, null, 1, safeLimit).records().stream()
                .map(card -> spotSummary(contentService.spot(Long.valueOf(card.id()), null))).toList();
        return ApiResponse.success(items);
    }

    @GetMapping("/spots/{id}")
    @Operation(summary = "景点工具摘要")
    public ApiResponse<InternalAiDtos.SpotSummary> spot(@PathVariable Long id) {
        return ApiResponse.success(spotSummary(contentService.spot(id, null)));
    }

    @GetMapping("/guides/search")
    @Operation(summary = "搜索已发布攻略")
    public ApiResponse<List<InternalAiDtos.GuideSummary>> searchGuides(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "5") Integer limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 10);
        List<InternalAiDtos.GuideSummary> items = contentService.guides(keyword, "HEAT_DESC", 1, safeLimit)
                .records().stream().map(this::guideSummary).toList();
        return ApiResponse.success(items);
    }

    @GetMapping("/guides/{id}")
    @Operation(summary = "攻略工具摘要")
    public ApiResponse<InternalAiDtos.GuideSummary> guide(@PathVariable Long id) {
        return ApiResponse.success(guideSummary(contentService.guide(id, null)));
    }

    @GetMapping("/foods/search")
    @Operation(summary = "搜索已发布美食")
    public ApiResponse<List<InternalAiDtos.FoodSummary>> searchFoods(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "5") Integer limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 10);
        List<InternalAiDtos.FoodSummary> items = contentService.foods(keyword, categoryId, null,
                        "HEAT_DESC", 1, safeLimit).records().stream().map(this::foodSummary).toList();
        return ApiResponse.success(items);
    }

    @GetMapping("/foods/{id}")
    @Operation(summary = "美食工具摘要")
    public ApiResponse<InternalAiDtos.FoodSummary> food(@PathVariable Long id) {
        return ApiResponse.success(foodSummary(contentService.food(id, null)));
    }

    @PostMapping("/recommendations")
    @Operation(summary = "调用 Java 确定性推荐")
    public ApiResponse<List<InternalAiDtos.RecommendationItem>> recommendations(
            @Valid @RequestBody InternalAiDtos.RecommendationRequest request) {
        List<AppUserVos.RecommendationVO> values = recommendationService.recommend(request.userId(),
                request.scene(), request.limit(), request.categoryId(), request.longitude(), request.latitude());
        return ApiResponse.success(values.stream().map(value -> new InternalAiDtos.RecommendationItem(
                value.spot().id(), value.spot().name(), value.spot().categoryName(), value.spot().summary(),
                value.score(), value.reason(), "/pages/spot/detail?id=" + value.spot().id())).toList());
    }

    @PostMapping("/knowledge/chunks/batch")
    @Operation(summary = "批量写入知识分片元数据")
    public ApiResponse<KnowledgeDtos.ChunkBatchResult> upsertKnowledgeChunks(
            @Valid @RequestBody KnowledgeDtos.ChunkBatchRequest request) {
        return ApiResponse.success(knowledgePersistenceService.upsertChunks(request));
    }

    @GetMapping("/conversations/{threadId}/messages")
    @Operation(summary = "checkpoint 丢失时读取恢复消息")
    public ApiResponse<List<InternalAiDtos.RecoveryItem>> conversationMessages(
            @PathVariable String threadId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(chatService.recentMessages(threadId, limit).stream()
                .filter(item -> "user".equals(item.role()) || "assistant".equals(item.role()))
                .map(item -> new InternalAiDtos.RecoveryItem(item.role(), item.content())).toList());
    }

    private InternalAiDtos.SpotSummary spotSummary(ContentVos.SpotDetail detail) {
        return new InternalAiDtos.SpotSummary(detail.id(), detail.name(), detail.categoryName(), detail.summary(),
                detail.address(), detail.openTimeDesc(), detail.ticketInfo(), detail.playDurationMinutes(),
                detail.tags().stream().map(ContentVos.TagVO::name).toList(),
                "/pages/spot/detail?id=" + detail.id());
    }

    private InternalAiDtos.GuideSummary guideSummary(ContentVos.GuideCard card) {
        return new InternalAiDtos.GuideSummary(card.id(), card.title(), card.summary(), card.dayCount(),
                "/pages/guide/detail?id=" + card.id());
    }

    private InternalAiDtos.GuideSummary guideSummary(ContentVos.GuideDetail detail) {
        return new InternalAiDtos.GuideSummary(detail.id(), detail.title(), detail.summary(), detail.dayCount(),
                "/pages/guide/detail?id=" + detail.id());
    }

    private InternalAiDtos.FoodSummary foodSummary(ContentVos.FoodCard card) {
        return new InternalAiDtos.FoodSummary(card.id(), card.name(), card.categoryName(), card.summary(),
                card.area(), card.priceRange(), "/pages/food/detail?id=" + card.id());
    }

    private InternalAiDtos.FoodSummary foodSummary(ContentVos.FoodDetail detail) {
        return new InternalAiDtos.FoodSummary(detail.id(), detail.name(), detail.categoryName(), detail.summary(),
                detail.area(), detail.priceRange(), "/pages/food/detail?id=" + detail.id());
    }

    private List<Long> parseIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return Arrays.stream(raw.split(",")).map(String::trim).filter(value -> !value.isEmpty())
                    .map(Long::valueOf).distinct().limit(10).toList();
        } catch (NumberFormatException exception) {
            throw new BizException(ResultCode.BAD_REQUEST, "tagIds 格式不正确");
        }
    }
}
