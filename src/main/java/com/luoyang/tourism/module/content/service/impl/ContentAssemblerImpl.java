package com.luoyang.tourism.module.content.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.content.pojo.entity.Banner;
import com.luoyang.tourism.module.content.pojo.entity.Food;
import com.luoyang.tourism.module.content.pojo.entity.FoodImage;
import com.luoyang.tourism.module.content.pojo.entity.GuideDay;
import com.luoyang.tourism.module.content.pojo.entity.GuideDaySpot;
import com.luoyang.tourism.module.content.pojo.entity.ScenicImage;
import com.luoyang.tourism.module.content.pojo.entity.ScenicSpot;
import com.luoyang.tourism.module.content.pojo.entity.Tag;
import com.luoyang.tourism.module.content.pojo.entity.TravelGuide;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import com.luoyang.tourism.module.content.service.ContentAssembler;
import com.luoyang.tourism.module.file.service.FileUrlService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ContentAssemblerImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Component
@RequiredArgsConstructor
public class ContentAssemblerImpl implements ContentAssembler {
    private final ContentMapper mapper;
    private final FileUrlService fileUrlService;
    private final ObjectMapper objectMapper;

@Override
public ContentVos.TagVO tag(Tag tag) {
        return new ContentVos.TagVO(Long.toString(tag.getId()), tag.getName(), tag.getColor());
    }

@Override
public List<ContentVos.TagVO> spotTags(Long spotId) {
        return mapper.selectTagsBySpotId(spotId).stream().map(this::tag).toList();
    }

@Override
public ContentVos.SpotCard spotCard(ScenicSpot spot) {
        return new ContentVos.SpotCard(Long.toString(spot.getId()), spot.getName(), spot.getSubtitle(),
                spot.getSummary(), Long.toString(spot.getCategoryId()), spot.getCategoryName(),
                fileUrlService.signedUrl(spot.getCoverFileId()), spotTags(spot.getId()), spot.getAddress(),
                spot.getLongitude(), spot.getLatitude(), spot.getHeatScore(), null);
    }

@Override
public ContentVos.SpotCard spotCard(ScenicSpot spot, Double userLongitude, Double userLatitude) {
        Long distance = distanceMeters(userLongitude, userLatitude, spot.getLongitude(), spot.getLatitude());
        ContentVos.SpotCard base = spotCard(spot);
        return new ContentVos.SpotCard(base.id(), base.name(), base.subtitle(), base.summary(), base.categoryId(),
                base.categoryName(), base.coverUrl(), base.tags(), base.address(), base.longitude(),
                base.latitude(), base.heatScore(), distance);
    }

@Override
public ContentVos.SpotDetail spotDetail(ScenicSpot spot) {
        return spotDetail(spot, null);
    }

@Override
public ContentVos.SpotDetail spotDetail(ScenicSpot spot, Boolean favorite) {
        List<ContentVos.ImageVO> images = mapper.selectSpotImages(spot.getId()).stream()
                .map(this::scenicImage).toList();
        List<String> related = mapper.selectRelatedSpots(spot.getId(), spot.getCategoryId(), 6).stream()
                .map(value -> Long.toString(value.getId())).toList();
        return new ContentVos.SpotDetail(Long.toString(spot.getId()), spot.getName(), spot.getSubtitle(),
                spot.getSummary(), Long.toString(spot.getCategoryId()), spot.getCategoryName(),
                fileUrlService.signedUrl(spot.getCoverFileId()), spotTags(spot.getId()), spot.getAddress(),
                spot.getLongitude(), spot.getLatitude(), spot.getOpenTimeDesc(), spot.getTicketInfo(),
                spot.getPlayDurationMinutes(), spot.getPhone(), spot.getTrafficInfo(), spot.getTips(),
                spot.getHeatScore(), spot.getContentHtml(), images, favorite, related,
                TimeUtils.offset(spot.getPublishedAt()));
    }

@Override
public ContentVos.GuideCard guideCard(TravelGuide guide) {
        return new ContentVos.GuideCard(Long.toString(guide.getId()), guide.getTitle(), guide.getSummary(),
                fileUrlService.signedUrl(guide.getCoverFileId()), guide.getTravelDays(), List.of(),
                guide.getHeatScore(), TimeUtils.offset(guide.getPublishedAt()));
    }

@Override
public ContentVos.GuideDetail guideDetail(TravelGuide guide, boolean publicOnly) {
        return guideDetail(guide, publicOnly, null);
    }

@Override
public ContentVos.GuideDetail guideDetail(TravelGuide guide, boolean publicOnly, Boolean favorite) {
        List<GuideDay> days = mapper.selectGuideDays(guide.getId());
        List<GuideDaySpot> daySpots = mapper.selectGuideDaySpots(guide.getId());
        Map<Long, List<GuideDaySpot>> grouped = new LinkedHashMap<>();
        daySpots.stream().sorted(Comparator.comparing(GuideDaySpot::getSortOrder))
                .forEach(value -> grouped.computeIfAbsent(value.getGuideDayId(), key -> new ArrayList<>()).add(value));
        List<ContentVos.GuideDayVO> dayVos = new ArrayList<>();
        for (GuideDay day : days) {
            List<ContentVos.SpotCard> spots = new ArrayList<>();
            for (GuideDaySpot relation : grouped.getOrDefault(day.getId(), List.of())) {
                ScenicSpot spot = mapper.selectSpot(relation.getSpotId());
                if (spot != null && (!publicOnly || Integer.valueOf(1).equals(spot.getStatus()))) {
                    spots.add(spotCard(spot));
                }
            }
            dayVos.add(new ContentVos.GuideDayVO(day.getDayNo(), day.getTitle(), day.getSummary(),
                    day.getTrafficTip(), day.getAccommodationTip(), spots));
        }
        return new ContentVos.GuideDetail(Long.toString(guide.getId()), guide.getTitle(), guide.getSummary(),
                fileUrlService.signedUrl(guide.getCoverFileId()), guide.getAuthorName(), guide.getTravelDays(),
                guide.getDifficulty(), guide.getSuitableSeason(), guide.getContentHtml(), dayVos, favorite,
                guide.getHeatScore(), TimeUtils.offset(guide.getPublishedAt()));
    }

@Override
public ContentVos.FoodCard foodCard(Food food) {
        return new ContentVos.FoodCard(Long.toString(food.getId()), food.getName(), food.getSummary(),
                fileUrlService.signedUrl(food.getCoverFileId()), Long.toString(food.getFoodCategoryId()),
                food.getCategoryName(), food.getPriceRange(), flavorTags(food.getFlavorTags()),
                food.getArea(), food.getHeatScore());
    }

@Override
public ContentVos.FoodDetail foodDetail(Food food) {
        return foodDetail(food, null);
    }

@Override
public ContentVos.FoodDetail foodDetail(Food food, Boolean favorite) {
        List<ContentVos.ImageVO> images = mapper.selectFoodImages(food.getId()).stream()
                .map(this::foodImage).toList();
        return new ContentVos.FoodDetail(Long.toString(food.getId()), food.getName(), food.getSummary(),
                fileUrlService.signedUrl(food.getCoverFileId()), Long.toString(food.getFoodCategoryId()),
                food.getCategoryName(), food.getDescription(), food.getPriceRange(),
                flavorTags(food.getFlavorTags()), food.getRecommendShops(), food.getArea(), images, favorite,
                food.getHeatScore(), TimeUtils.offset(food.getPublishedAt()));
    }

@Override
public ContentVos.BannerVO banner(Banner banner) {
        return new ContentVos.BannerVO(Long.toString(banner.getId()), banner.getTitle(), banner.getSubtitle(),
                fileUrlService.signedUrl(banner.getImageFileId()), banner.getLinkType(),
                banner.getTargetId() == null ? null : Long.toString(banner.getTargetId()), banner.getTargetPath());
    }

@Override
public ContentVos.AdminSpot adminSpot(ScenicSpot spot) {
        List<ContentVos.ImageVO> images = mapper.selectSpotImages(spot.getId()).stream()
                .map(this::scenicImage).toList();
        return new ContentVos.AdminSpot(Long.toString(spot.getId()), Long.toString(spot.getCategoryId()),
                spot.getCategoryName(), spot.getName(), spot.getSubtitle(), spot.getSummary(),
                stringId(spot.getCoverFileId()), fileUrlService.signedUrl(spot.getCoverFileId()),
                spot.getContentHtml(), spot.getAddress(), spot.getLongitude(), spot.getLatitude(),
                spot.getOpenTimeDesc(), spot.getTicketInfo(), spot.getPlayDurationMinutes(), spot.getPhone(),
                spot.getTrafficInfo(), spot.getTips(), spot.getHeatScore(), spot.getRecommendationWeight(),
                spot.getStatus(), spotTags(spot.getId()), images, spot.getVersion(),
                TimeUtils.offset(spot.getUpdatedAt()));
    }

@Override
public ContentVos.AdminGuide adminGuide(TravelGuide guide) {
        return new ContentVos.AdminGuide(Long.toString(guide.getId()), guide.getTitle(), guide.getSlug(),
                guide.getSummary(), stringId(guide.getCoverFileId()),
                fileUrlService.signedUrl(guide.getCoverFileId()), guide.getAuthorName(), guide.getContentHtml(),
                guide.getTravelDays(), guide.getDifficulty(), guide.getSuitableSeason(), guide.getHeatScore(),
                guide.getRecommendationWeight(), guide.getStatus(),
                guideDetail(guide, false).days(), guide.getVersion(), TimeUtils.offset(guide.getUpdatedAt()));
    }

@Override
public ContentVos.AdminFood adminFood(Food food) {
        List<ContentVos.ImageVO> images = mapper.selectFoodImages(food.getId()).stream()
                .map(this::foodImage).toList();
        return new ContentVos.AdminFood(Long.toString(food.getId()), Long.toString(food.getFoodCategoryId()),
                food.getCategoryName(), food.getName(), food.getSummary(), stringId(food.getCoverFileId()),
                fileUrlService.signedUrl(food.getCoverFileId()), food.getDescription(), food.getPriceRange(),
                flavorTags(food.getFlavorTags()), food.getRecommendShops(), food.getArea(), food.getHeatScore(),
                food.getRecommendationWeight(), food.getStatus(), images, food.getVersion(),
                TimeUtils.offset(food.getUpdatedAt()));
    }

@Override
public ContentVos.AdminBanner adminBanner(Banner banner) {
        return new ContentVos.AdminBanner(Long.toString(banner.getId()), banner.getTitle(), banner.getSubtitle(),
                Long.toString(banner.getImageFileId()), fileUrlService.signedUrl(banner.getImageFileId()),
                banner.getLinkType(), stringId(banner.getTargetId()), banner.getTargetPath(), banner.getPosition(),
                banner.getSortOrder(), banner.getStatus(), TimeUtils.offset(banner.getStartAt()),
                TimeUtils.offset(banner.getEndAt()), banner.getVersion(), TimeUtils.offset(banner.getUpdatedAt()));
    }

@Override
public List<String> flavorTags(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() { });
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private ContentVos.ImageVO scenicImage(ScenicImage image) {
        return new ContentVos.ImageVO(Long.toString(image.getId()), fileUrlService.signedUrl(image.getFileId()),
                image.getCaption(), image.getSortOrder());
    }

    private ContentVos.ImageVO foodImage(FoodImage image) {
        return new ContentVos.ImageVO(Long.toString(image.getId()), fileUrlService.signedUrl(image.getFileId()),
                image.getCaption(), image.getSortOrder());
    }

    private String stringId(Long id) {
        return id == null ? null : Long.toString(id);
    }

    private Long distanceMeters(Double lon1, Double lat1, java.math.BigDecimal lon2, java.math.BigDecimal lat2) {
        if (lon1 == null || lat1 == null || lon2 == null || lat2 == null) {
            return null;
        }
        double earth = 6371000;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2.doubleValue());
        double dPhi = Math.toRadians(lat2.doubleValue() - lat1);
        double dLambda = Math.toRadians(lon2.doubleValue() - lon1);
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        return Math.round(earth * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}
