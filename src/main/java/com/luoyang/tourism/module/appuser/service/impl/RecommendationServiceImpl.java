package com.luoyang.tourism.module.appuser.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.trace.TraceIds;
import com.luoyang.tourism.module.appuser.mapper.AppUserMapper;
import com.luoyang.tourism.module.appuser.pojo.entity.UserPreference;
import com.luoyang.tourism.module.appuser.pojo.entity.UserRecommendationLog;
import com.luoyang.tourism.module.appuser.pojo.vo.AppUserVos;
import com.luoyang.tourism.module.appuser.service.RecommendationService;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.content.pojo.entity.ScenicSpot;
import com.luoyang.tourism.module.content.pojo.entity.Tag;
import com.luoyang.tourism.module.content.service.ContentAssembler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RecommendationServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private static final String ALGORITHM_VERSION = "rules-v1";

    private final AppUserMapper userMapper;
    private final ContentMapper contentMapper;
    private final ContentAssembler assembler;
    private final ObjectMapper objectMapper;

    @Transactional
@Override
public List<AppUserVos.RecommendationVO> recommend(Long userId, String scene, Integer limit,
                                                       Long categoryId, Double longitude, Double latitude) {
        int safeLimit = limit == null ? 10 : Math.min(Math.max(limit, 1), 20);
        String safeScene = scene == null || scene.isBlank()
                ? "HOME" : scene.trim().toUpperCase(Locale.ROOT);
        if (safeScene.length() > 32) {
            throw new BizException(ResultCode.BAD_REQUEST, "推荐场景过长");
        }

        List<UserPreference> preferences = userMapper.selectPreferences(userId, null);
        Set<Long> preferenceCategories = new HashSet<>();
        Set<Long> preferenceTags = new HashSet<>();
        for (UserPreference preference : preferences) {
            if (preference.getPreferenceType() == 1) {
                preferenceCategories.add(preference.getPreferenceKey());
            } else if (preference.getPreferenceType() == 2) {
                preferenceTags.add(preference.getPreferenceKey());
            }
        }

        Set<Long> favoriteSpotIds = new HashSet<>(userMapper.selectFavoriteBizIds(userId, 1));
        Set<Long> favoriteCategories = new HashSet<>();
        Set<Long> favoriteTags = new HashSet<>();
        for (Long spotId : favoriteSpotIds) {
            ScenicSpot favorite = contentMapper.selectSpot(spotId);
            if (favorite != null) {
                favoriteCategories.add(favorite.getCategoryId());
                contentMapper.selectTagsBySpotId(spotId).forEach(tag -> favoriteTags.add(tag.getId()));
            }
        }

        List<ScoredSpot> scored = new ArrayList<>();
        for (ScenicSpot spot : contentMapper.selectPublishedCandidates(200)) {
            if (categoryId != null && !categoryId.equals(spot.getCategoryId())) {
                continue;
            }
            List<Tag> tags = contentMapper.selectTagsBySpotId(spot.getId());
            // 规则分由基础热度、显式偏好、收藏分类、标签和距离特征组成；AI 不得改变候选顺序。
            double score = baseScore(spot);
            boolean categoryMatched = preferenceCategories.contains(spot.getCategoryId());
            boolean favoriteCategoryMatched = favoriteCategories.contains(spot.getCategoryId());
            int tagMatched = 0;
            for (Tag tag : tags) {
                if (preferenceTags.contains(tag.getId()) || favoriteTags.contains(tag.getId())) {
                    tagMatched++;
                }
            }
            // 显式分类偏好权重最高，收藏分类次之，标签和距离作为补充特征。
            if (categoryMatched) {
                score += 0.25;
            }
            if (favoriteCategoryMatched) {
                score += 0.12;
            }
            score += Math.min(tagMatched * 0.06, 0.18);
            if (longitude != null && latitude != null && spot.getLongitude() != null && spot.getLatitude() != null) {
                long distance = distanceMeters(longitude, latitude, spot.getLongitude().doubleValue(),
                        spot.getLatitude().doubleValue());
                score += Math.max(0, 1 - Math.min(distance / 50000d, 1)) * 0.08;
            }
            score = Math.min(score, 0.99);
            String reason = reason(spot, categoryMatched, favoriteCategoryMatched, tagMatched);
            scored.add(new ScoredSpot(spot, score, reason));
        }
        scored.sort(Comparator.comparingDouble(ScoredSpot::score).reversed()
                .thenComparing(item -> item.spot().getId(), Comparator.reverseOrder()));
        List<ScoredSpot> selected = scored.stream().limit(safeLimit).toList();

        List<AppUserVos.RecommendationVO> result = new ArrayList<>();
        String traceId = TraceIds.current();
        List<Map<String, Object>> items = new ArrayList<>();
        for (ScoredSpot item : selected) {
            result.add(new AppUserVos.RecommendationVO(
                    assembler.spotCard(item.spot(), longitude, latitude),
                    Math.round(item.score() * 10000d) / 10000d, item.reason(), ALGORITHM_VERSION, traceId));
            Map<String, Object> logItem = new LinkedHashMap<>();
            logItem.put("spotId", Long.toString(item.spot().getId()));
            logItem.put("score", Math.round(item.score() * 10000d) / 10000d);
            logItem.put("reason", item.reason());
            items.add(logItem);
        }

        UserRecommendationLog log = new UserRecommendationLog();
        log.setUserId(userId);
        log.setScene(safeScene);
        log.setAlgorithmVersion(ALGORITHM_VERSION);
        log.setRequestContext(json(Map.of("scene", safeScene, "limit", safeLimit,
                "categoryId", categoryId == null ? "" : Long.toString(categoryId),
                "hasLocation", longitude != null && latitude != null)));
        log.setItemsJson(json(items));
        log.setTraceId(traceId);
        userMapper.insertRecommendationLog(log);
        return result;
    }

    /**
     * 计算只依赖内容热度与运营权重的基础分，便于冷启动用户仍有可解释的推荐结果。
     */
    private double baseScore(ScenicSpot spot) {
        double heat = spot.getHeatScore() == null ? 0 : spot.getHeatScore();
        double weight = spot.getRecommendationWeight() == null ? 0 : spot.getRecommendationWeight();
        return Math.min(heat / 2000d, 1d) * 0.45 + Math.min(weight / 100d, 1d) * 0.10;
    }

    private String reason(ScenicSpot spot, boolean categoryMatched, boolean favoriteCategoryMatched, int tagMatched) {
        String category = spot.getCategoryName() == null ? "该分类" : spot.getCategoryName();
        if ((categoryMatched || favoriteCategoryMatched) && tagMatched > 0) {
            return "与你关注的" + category + "和兴趣标签匹配";
        }
        if (categoryMatched || favoriteCategoryMatched) {
            return "与你关注的" + category + "分类匹配";
        }
        if (tagMatched > 0) {
            return "与你收藏或选择的兴趣标签匹配";
        }
        return "近期热度较高且适合作为洛阳旅行候选";
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(ResultCode.INTERNAL_ERROR);
        }
    }

    private long distanceMeters(double lon1, double lat1, double lon2, double lat2) {
        double earth = 6371000;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        return Math.round(earth * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    private record ScoredSpot(ScenicSpot spot, double score, String reason) {
    }
}
