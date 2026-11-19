package com.luoyang.tourism.module.content.pojo.vo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class ContentVos {
    private ContentVos() {
    }

    public record TagVO(String id, String name, String color) {
    }

    public record CategoryVO(String id, String parentId, String name, String code, String iconUrl,
                             String description, Integer sortOrder, long spotCount,
                             List<CategoryVO> children) {
    }

    public record ImageVO(String id, String url, String caption, Integer sortOrder) {
    }

    public record SpotCard(String id, String name, String subtitle, String summary,
                           String categoryId, String categoryName, String coverUrl,
                           List<TagVO> tags, String address, BigDecimal longitude, BigDecimal latitude,
                           Integer heatScore, Long distanceMeters) {
    }

    public record SpotDetail(String id, String name, String subtitle, String summary,
                             String categoryId, String categoryName, String coverUrl,
                             List<TagVO> tags, String address, BigDecimal longitude, BigDecimal latitude,
                             String openTimeDesc, String ticketInfo, Integer playDurationMinutes,
                             String phone, String trafficInfo, String tips, Integer heatScore,
                             String contentHtml, List<ImageVO> images, Boolean favorite,
                             List<String> relatedSpotIds, OffsetDateTime publishedAt) {
    }

    public record GuideCard(String id, String title, String summary, String coverUrl, Integer dayCount,
                            List<TagVO> tags, Integer heatScore, OffsetDateTime publishedAt) {
    }

    public record GuideDayVO(Integer dayNo, String title, String summary, String trafficTip,
                             String accommodationTip, List<SpotCard> spots) {
    }

    public record GuideDetail(String id, String title, String summary, String coverUrl, String authorName,
                              Integer dayCount, Integer difficulty, String suitableSeason,
                              String contentHtml, List<GuideDayVO> days, Boolean favorite,
                              Integer heatScore, OffsetDateTime publishedAt) {
    }

    public record FoodCard(String id, String name, String summary, String coverUrl,
                           String categoryId, String categoryName, String priceRange,
                           List<String> flavorTags, String area, Integer heatScore) {
    }

    public record FoodDetail(String id, String name, String summary, String coverUrl,
                             String categoryId, String categoryName, String description,
                             String priceRange, List<String> flavorTags, String recommendShops,
                             String area, List<ImageVO> images, Boolean favorite,
                             Integer heatScore, OffsetDateTime publishedAt) {
    }

    public record BannerVO(String id, String title, String subtitle, String imageUrl,
                           Integer linkType, String targetId, String targetPath) {
    }

    public record HomeVO(List<BannerVO> banners, List<CategoryVO> categories, List<SpotCard> hotSpots,
                         List<GuideCard> featuredGuides, List<FoodCard> featuredFoods) {
    }

    public record SearchItem(String bizType, String bizId, String title, String summary,
                             String coverUrl, String matchedField, Integer sortScore) {
    }

    public record AdminCategory(String id, String parentId, String name, String code, String iconFileId,
                                String iconUrl, String description, Integer sortOrder, Integer status,
                                Integer version, OffsetDateTime updatedAt) {
    }

    public record AdminTag(String id, Integer tagType, String name, String code, String color,
                           String description, Integer sortOrder, Integer status, Integer version,
                           OffsetDateTime updatedAt) {
    }

    public record AdminSpot(String id, String categoryId, String categoryName, String name, String subtitle,
                            String summary, String coverFileId, String coverUrl, String contentHtml,
                            String address, BigDecimal longitude, BigDecimal latitude,
                            String openTimeDesc, String ticketInfo, Integer playDurationMinutes,
                            String phone, String trafficInfo, String tips, Integer heatScore,
                            Integer recommendationWeight, Integer status, List<TagVO> tags,
                            List<ImageVO> images, Integer version, OffsetDateTime updatedAt) {
    }

    public record AdminGuide(String id, String title, String slug, String summary, String coverFileId,
                             String coverUrl, String authorName, String contentHtml, Integer travelDays,
                             Integer difficulty, String suitableSeason, Integer heatScore,
                             Integer recommendationWeight, Integer status, List<GuideDayVO> days,
                             Integer version, OffsetDateTime updatedAt) {
    }

    public record AdminFoodCategory(String id, String name, String code, String iconFileId, String iconUrl,
                                    Integer sortOrder, Integer status, Integer version,
                                    OffsetDateTime updatedAt) {
    }

    public record AdminFood(String id, String categoryId, String categoryName, String name, String summary,
                            String coverFileId, String coverUrl, String description, String priceRange,
                            List<String> flavorTags, String recommendShops, String area, Integer heatScore,
                            Integer recommendationWeight, Integer status, List<ImageVO> images,
                            Integer version, OffsetDateTime updatedAt) {
    }

    public record AdminBanner(String id, String title, String subtitle, String imageFileId, String imageUrl,
                              Integer linkType, String targetId, String targetPath, String position,
                              Integer sortOrder, Integer status, OffsetDateTime startAt,
                              OffsetDateTime endAt, Integer version, OffsetDateTime updatedAt) {
    }
}
