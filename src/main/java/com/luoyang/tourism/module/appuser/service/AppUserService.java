package com.luoyang.tourism.module.appuser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.enums.BizType;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.trace.TraceIds;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.appuser.dto.AppUserDtos;
import com.luoyang.tourism.module.appuser.entity.UserBehavior;
import com.luoyang.tourism.module.appuser.entity.UserFavorite;
import com.luoyang.tourism.module.appuser.entity.UserPreference;
import com.luoyang.tourism.module.appuser.mapper.AppUserMapper;
import com.luoyang.tourism.module.appuser.vo.AppUserVos;
import com.luoyang.tourism.module.auth.entity.AppUser;
import com.luoyang.tourism.module.auth.mapper.AuthMapper;
import com.luoyang.tourism.module.auth.vo.AuthVos;
import com.luoyang.tourism.module.content.entity.Food;
import com.luoyang.tourism.module.content.entity.ScenicCategory;
import com.luoyang.tourism.module.content.entity.ScenicSpot;
import com.luoyang.tourism.module.content.entity.Tag;
import com.luoyang.tourism.module.content.entity.TravelGuide;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.file.entity.FileObject;
import com.luoyang.tourism.module.file.mapper.FileMapper;
import com.luoyang.tourism.module.file.service.FileUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AuthMapper authMapper;
    private final AppUserMapper mapper;
    private final ContentMapper contentMapper;
    private final FileMapper fileMapper;
    private final FileUrlService fileUrlService;
    private final ObjectMapper objectMapper;

    public AuthVos.AppUserVO profile(Long userId) {
        return profileVo(requiredUser(userId));
    }

    @Transactional
    public AuthVos.AppUserVO updateProfile(Long userId, AppUserDtos.ProfileUpdateRequest request) {
        AppUser user = requiredUser(userId);
        if (request.avatarFileId() != null) {
            FileObject file = fileMapper.selectById(request.avatarFileId());
            if (file == null || file.getStatus() == null || file.getStatus() != 1
                    || file.getUploadedByType() == null || file.getUploadedByType() != 1
                    || file.getUploadedBy() == null || !file.getUploadedBy().equals(userId)) {
                throw new BizException(ResultCode.BAD_REQUEST, "头像文件不存在或不属于当前用户");
            }
        }
        if (request.nickname() != null) {
            user.setNickname(request.nickname().trim());
        }
        if (request.avatarFileId() != null) {
            user.setAvatarFileId(request.avatarFileId());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.city() != null) {
            user.setCity(request.city().trim());
        }
        if (authMapper.updateAppProfile(user) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
        return profileVo(requiredUser(userId));
    }

    public AppUserVos.PreferenceVO preferences(Long userId) {
        List<UserPreference> rows = mapper.selectPreferences(userId, 1);
        List<String> categoryIds = rows.stream().filter(row -> row.getPreferenceType() == 1)
                .map(row -> Long.toString(row.getPreferenceKey())).toList();
        List<String> tagIds = rows.stream().filter(row -> row.getPreferenceType() == 2)
                .map(row -> Long.toString(row.getPreferenceKey())).toList();
        List<String> foodCategoryIds = rows.stream().filter(row -> row.getPreferenceType() == 3)
                .map(row -> Long.toString(row.getPreferenceKey())).toList();
        OffsetDateTime updatedAt = rows.stream().map(UserPreference::getUpdatedAt).filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder()).map(TimeUtils::offset).orElse(null);
        return new AppUserVos.PreferenceVO(categoryIds, tagIds, foodCategoryIds, updatedAt);
    }

    @Transactional
    public AppUserVos.PreferenceVO replacePreferences(Long userId, AppUserDtos.PreferenceUpdateRequest request) {
        List<Long> categoryIds = unique(request.categoryIds());
        List<Long> tagIds = unique(request.tagIds());
        List<Long> foodCategoryIds = unique(request.foodCategoryIds());
        for (Long id : categoryIds) {
            ScenicCategory category = contentMapper.selectCategory(id);
            if (category == null || !Integer.valueOf(1).equals(category.getStatus())) {
                throw new BizException(ResultCode.BAD_REQUEST, "景点分类不存在或未启用");
            }
        }
        if (!tagIds.isEmpty() && contentMapper.selectTagsByIds(tagIds).size() != tagIds.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "标签不存在或未启用");
        }
        for (Long id : foodCategoryIds) {
            var category = contentMapper.selectFoodCategory(id);
            if (category == null || !Integer.valueOf(1).equals(category.getStatus())) {
                throw new BizException(ResultCode.BAD_REQUEST, "美食分类不存在或未启用");
            }
        }
        mapper.deleteExplicitPreferences(userId);
        categoryIds.forEach(id -> insertPreference(userId, 1, id));
        tagIds.forEach(id -> insertPreference(userId, 2, id));
        foodCategoryIds.forEach(id -> insertPreference(userId, 3, id));
        return preferences(userId);
    }

    public PageResult<AppUserVos.FavoriteVO> favorites(Long userId, String bizType,
                                                       Integer page, Integer pageSize) {
        Integer type = bizType == null || bizType.isBlank() ? null : BizType.fromApi(bizType).code();
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<AppUserVos.FavoriteVO> records = mapper.selectFavorites(userId, type,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(this::favoriteVo).toList();
        return Paging.of(records, mapper.countFavorites(userId, type), safePage, safeSize);
    }

    @Transactional
    public AppUserVos.FavoriteVO addFavorite(Long userId, AppUserDtos.FavoriteRequest request) {
        BizType type = BizType.fromApi(request.bizType());
        Target target = resolveTarget(type, request.bizId(), true);
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setBizType(type.code());
        favorite.setBizId(request.bizId());
        mapper.insertFavorite(favorite);
        UserFavorite saved = mapper.selectFavorite(userId, type.code(), request.bizId());
        AppUserVos.FavoriteVO vo = favoriteVo(saved);
        return new AppUserVos.FavoriteVO(vo.id(), vo.bizType(), vo.bizId(), target.summary(),
                true, vo.createdAt());
    }

    public void removeFavorite(Long userId, String bizType, Long bizId) {
        BizType type = BizType.fromApi(bizType);
        mapper.deleteFavorite(userId, type.code(), bizId);
    }

    public boolean isFavorite(Long userId, BizType type, Long bizId) {
        return userId != null && mapper.isFavorite(userId, type.code(), bizId) > 0;
    }

    public void reportBehavior(Long userId, AppUserDtos.BehaviorRequest request) {
        if (request.bizType() != null && request.bizId() != null) {
            BizType type = BizType.fromCode(request.bizType());
            resolveTarget(type, request.bizId(), true);
        }
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorType(request.behaviorType());
        behavior.setBizType(request.bizType());
        behavior.setBizId(request.bizId());
        behavior.setKeyword(trim(request.keyword()));
        behavior.setDurationMs(request.durationMs());
        behavior.setSource(trim(request.source()));
        behavior.setMetadata(toMetadataJson(request.metadata()));
        behavior.setTraceId(TraceIds.current());
        mapper.insertBehavior(behavior);
    }

    private void insertPreference(Long userId, int type, Long key) {
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setPreferenceType(type);
        preference.setPreferenceKey(key);
        preference.setWeight(BigDecimal.ONE);
        preference.setSource(1);
        preference.setLastInteractedAt(LocalDateTime.now());
        mapper.insertPreference(preference);
    }

    private AppUserVos.FavoriteVO favoriteVo(UserFavorite favorite) {
        BizType type = BizType.fromCode(favorite.getBizType());
        Target target = resolveTarget(type, favorite.getBizId(), false);
        return new AppUserVos.FavoriteVO(Long.toString(favorite.getId()), type.apiValue(),
                Long.toString(favorite.getBizId()), target == null ? null : target.summary(),
                target != null && target.available(), TimeUtils.offset(favorite.getCreatedAt()));
    }

    private Target resolveTarget(BizType type, Long bizId, boolean requirePublished) {
        switch (type) {
            case SPOT -> {
                ScenicSpot spot = contentMapper.selectSpot(bizId);
                boolean available = spot != null && Integer.valueOf(1).equals(spot.getStatus());
                if (requirePublished && !available) {
                    throw new BizException(ResultCode.BAD_REQUEST, "景点不存在或未发布");
                }
                return spot == null ? null : new Target(available,
                        new AppUserVos.FavoriteTargetVO(Long.toString(spot.getId()), spot.getName(),
                                fileUrlService.signedUrl(spot.getCoverFileId())));
            }
            case GUIDE -> {
                TravelGuide guide = contentMapper.selectGuide(bizId);
                boolean available = guide != null && Integer.valueOf(1).equals(guide.getStatus());
                if (requirePublished && !available) {
                    throw new BizException(ResultCode.BAD_REQUEST, "攻略不存在或未发布");
                }
                return guide == null ? null : new Target(available,
                        new AppUserVos.FavoriteTargetVO(Long.toString(guide.getId()), guide.getTitle(),
                                fileUrlService.signedUrl(guide.getCoverFileId())));
            }
            case FOOD -> {
                Food food = contentMapper.selectFood(bizId);
                boolean available = food != null && Integer.valueOf(1).equals(food.getStatus());
                if (requirePublished && !available) {
                    throw new BizException(ResultCode.BAD_REQUEST, "美食不存在或未发布");
                }
                return food == null ? null : new Target(available,
                        new AppUserVos.FavoriteTargetVO(Long.toString(food.getId()), food.getName(),
                                fileUrlService.signedUrl(food.getCoverFileId())));
            }
            default -> throw new BizException(ResultCode.UNSUPPORTED_VALUE);
        }
    }

    private String toMetadataJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        Map<String, Object> safe = new java.util.LinkedHashMap<>();
        metadata.forEach((key, value) -> {
            String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
            if (!(normalized.contains("password") || normalized.contains("token") || normalized.contains("secret")
                    || normalized.contains("openid") || normalized.contains("phone") || normalized.contains("idcard"))) {
                safe.put(key, value);
            }
        });
        try {
            String json = objectMapper.writeValueAsString(safe);
            if (json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 2048) {
                throw new BizException(ResultCode.BAD_REQUEST, "行为元数据超过 2KB");
            }
            return json;
        } catch (JsonProcessingException exception) {
            throw new BizException(ResultCode.BAD_REQUEST, "行为元数据格式不正确");
        }
    }

    private AuthVos.AppUserVO profileVo(AppUser user) {
        return new AuthVos.AppUserVO(Long.toString(user.getId()), user.getNickname(),
                fileUrlService.signedUrl(user.getAvatarFileId()), user.getGender(), user.getCity(),
                TimeUtils.offset(user.getCreatedAt()));
    }

    private AppUser requiredUser(Long userId) {
        AppUser user = authMapper.selectAppUserById(userId);
        if (user == null) {
            throw BizException.notFound();
        }
        return user;
    }

    private List<Long> unique(List<Long> values) {
        return values == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(values));
    }

    private String trim(String value) {
        return value == null ? null : (value.trim().isEmpty() ? null : value.trim());
    }

    private record Target(boolean available, AppUserVos.FavoriteTargetVO summary) {
    }
}
