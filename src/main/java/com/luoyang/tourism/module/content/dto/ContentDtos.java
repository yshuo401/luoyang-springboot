package com.luoyang.tourism.module.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ContentDtos {
    private ContentDtos() {
    }

    public record CategoryUpsertRequest(
            Long parentId,
            @NotBlank @Size(max = 64) String name,
            @NotBlank @Size(max = 64) String code,
            Long iconFileId,
            @Size(max = 500) String description,
            Integer sortOrder,
            Integer status,
            Integer version
    ) {
    }

    public record TagUpsertRequest(
            @NotNull @Min(1) @Max(3) Integer tagType,
            @NotBlank @Size(max = 64) String name,
            @NotBlank @Size(max = 64) String code,
            @Size(max = 16) String color,
            @Size(max = 255) String description,
            Integer sortOrder,
            Integer status,
            Integer version
    ) {
    }

    public record SpotUpsertRequest(
            @NotNull Long categoryId,
            @NotBlank @Size(max = 128) String name,
            @Size(max = 255) String subtitle,
            @Size(max = 1000) String summary,
            Long coverFileId,
            @Size(max = 200000) String contentHtml,
            @Size(max = 255) String address,
            @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
            @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
            @Size(max = 500) String openTimeDesc,
            @Size(max = 1000) String ticketInfo,
            @Min(0) Integer playDurationMinutes,
            @Size(max = 32) String phone,
            @Size(max = 5000) String trafficInfo,
            @Size(max = 5000) String tips,
            @Size(max = 10) List<Long> tagIds,
            @Size(max = 20) List<Long> imageFileIds,
            @Min(0) Integer recommendationWeight,
            Integer version
    ) {
    }

    public record DaySpotRequest(
            @NotNull Long spotId,
            Integer sortOrder,
            @Min(0) Integer stayMinutes,
            @Size(max = 500) String note
    ) {
    }

    public record DayRequest(
            @NotNull @Min(1) Integer dayNo,
            @NotBlank @Size(max = 128) String title,
            @Size(max = 1000) String summary,
            @Size(max = 5000) String trafficTip,
            @Size(max = 5000) String accommodationTip,
            @Size(max = 30) List<@Valid DaySpotRequest> spots
    ) {
    }

    public record GuideUpsertRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 200) String slug,
            @Size(max = 1000) String summary,
            Long coverFileId,
            @Size(max = 64) String authorName,
            @Size(max = 200000) String contentHtml,
            @Min(1) @Max(60) Integer travelDays,
            @Min(1) @Max(3) Integer difficulty,
            @Size(max = 64) String suitableSeason,
            @Min(0) Integer recommendationWeight,
            @Size(max = 30) List<@Valid DayRequest> days,
            Integer version
    ) {
    }

    public record FoodUpsertRequest(
            @NotNull Long foodCategoryId,
            @NotBlank @Size(max = 128) String name,
            @Size(max = 1000) String summary,
            Long coverFileId,
            @Size(max = 200000) String description,
            @Size(max = 64) String priceRange,
            @Size(max = 20) List<@Size(max = 32) String> flavorTags,
            @Size(max = 10000) String recommendShops,
            @Size(max = 128) String area,
            @Size(max = 20) List<Long> imageFileIds,
            @Min(0) Integer recommendationWeight,
            Integer version
    ) {
    }

    public record FoodCategoryUpsertRequest(
            @NotBlank @Size(max = 64) String name,
            @NotBlank @Size(max = 64) String code,
            Long iconFileId,
            Integer sortOrder,
            Integer status,
            Integer version
    ) {
    }

    public record BannerUpsertRequest(
            @NotBlank @Size(max = 128) String title,
            @Size(max = 255) String subtitle,
            @NotNull Long imageFileId,
            @NotNull @Min(0) @Max(4) Integer linkType,
            Long targetId,
            @Size(max = 255) String targetPath,
            @NotBlank @Size(max = 32) String position,
            Integer sortOrder,
            Integer status,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Integer version
    ) {
    }

    public record StatusRequest(@NotNull Integer status, @NotNull Integer version) {
    }

    public record WeightRequest(@NotNull @Min(0) Integer recommendationWeight, @NotNull Integer version) {
    }
}
