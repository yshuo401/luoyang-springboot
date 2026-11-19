package com.luoyang.tourism.module.content.service.impl;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.enums.BizType;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.module.appuser.service.AppUserService;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.content.pojo.entity.Food;
import com.luoyang.tourism.module.content.pojo.entity.ScenicCategory;
import com.luoyang.tourism.module.content.pojo.entity.ScenicSpot;
import com.luoyang.tourism.module.content.pojo.entity.TravelGuide;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import com.luoyang.tourism.module.content.service.ContentAssembler;
import com.luoyang.tourism.module.content.service.PublicContentService;
import com.luoyang.tourism.module.file.service.FileUrlService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * PublicContentServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
@RequiredArgsConstructor
public class PublicContentServiceImpl implements PublicContentService {
    private static final Set<String> SORTS = Set.of("HEAT_DESC", "LATEST", "SORT_ORDER", "RECOMMEND");

    private final ContentMapper mapper;
    private final ContentAssembler assembler;
    private final FileUrlService fileUrlService;
    private final AppUserService appUserService;

@Override
public ContentVos.HomeVO home() {
        List<ContentVos.BannerVO> banners = mapper.selectPublicBanners("HOME_TOP", LocalDateTime.now())
                .stream().map(assembler::banner).toList();
        List<ContentVos.CategoryVO> categories = categories(false);
        List<ContentVos.SpotCard> spots = mapper.selectSpots(false, null, null, List.of(),
                null, "HEAT_DESC", 0, 6).stream().map(assembler::spotCard).toList();
        List<ContentVos.GuideCard> guides = mapper.selectGuides(false, null, null, "HEAT_DESC", 0, 4)
                .stream().map(assembler::guideCard).toList();
        List<ContentVos.FoodCard> foods = mapper.selectFoods(false, null, null, null,
                null, "HEAT_DESC", 0, 4).stream().map(assembler::foodCard).toList();
        return new ContentVos.HomeVO(banners, categories, spots, guides, foods);
    }

@Override
public List<ContentVos.CategoryVO> categories(boolean flat) {
        List<ScenicCategory> categories = mapper.selectCategories(false, null, null);
        Map<Long, MutableCategory> nodes = new LinkedHashMap<>();
        for (ScenicCategory category : categories) {
            long count = mapper.countPublishedSpotsInCategory(category.getId());
            nodes.put(category.getId(), new MutableCategory(category, count));
        }
        List<ContentVos.CategoryVO> roots = new ArrayList<>();
        for (MutableCategory node : nodes.values()) {
            MutableCategory parent = node.category.getParentId() == null || node.category.getParentId() == 0
                    ? null : nodes.get(node.category.getParentId());
            if (parent == null) {
                roots.add(toCategory(node));
            } else {
                parent.children.add(node);
            }
        }
        roots.sort(Comparator.comparing(ContentVos.CategoryVO::sortOrder));
        if (flat) {
            return categories.stream().map(category -> new ContentVos.CategoryVO(
                    Long.toString(category.getId()), Long.toString(category.getParentId()), category.getName(),
                    category.getCode(), fileUrlService.signedUrl(category.getIconFileId()), category.getDescription(),
                    category.getSortOrder(), mapper.countPublishedSpotsInCategory(category.getId()), List.of()))
                    .toList();
        }
        return roots;
    }

@Override
public PageResult<ContentVos.SpotCard> spots(String keyword, Long categoryId, List<Long> tagIds,
                                                 String sort, Double longitude, Double latitude,
                                                 Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        String safeSort = sort(sort == null ? "HEAT_DESC" : sort);
        List<Long> safeTags = tagIds == null ? List.of() : tagIds;
        List<ContentVos.SpotCard> records = mapper.selectSpots(false, trim(keyword), categoryId,
                        safeTags, null, safeSort, Paging.offset(safePage, safeSize), safeSize)
                .stream().map(spot -> assembler.spotCard(spot, longitude, latitude)).toList();
        long total = mapper.countSpots(false, trim(keyword), categoryId, safeTags, null);
        return Paging.of(records, total, safePage, safeSize);
    }

@Override
public ContentVos.SpotDetail spot(Long id, Long userId) {
        ScenicSpot spot = mapper.selectSpot(id);
        if (spot == null || !Integer.valueOf(1).equals(spot.getStatus())) {
            throw BizException.notFound();
        }
        return assembler.spotDetail(spot, userId == null ? null : appUserService.isFavorite(userId, BizType.SPOT, id));
    }

@Override
public List<ContentVos.SpotCard> relatedSpots(Long id, Integer limit) {
        ScenicSpot spot = mapper.selectSpot(id);
        if (spot == null || !Integer.valueOf(1).equals(spot.getStatus())) {
            throw BizException.notFound();
        }
        int safeLimit = limit == null ? 6 : Math.min(Math.max(limit, 1), 10);
        return mapper.selectRelatedSpots(id, spot.getCategoryId(), safeLimit).stream()
                .map(assembler::spotCard).toList();
    }

@Override
public PageResult<ContentVos.GuideCard> guides(String keyword, String sort, Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        String safeSort = sort(sort == null ? "HEAT_DESC" : sort);
        List<ContentVos.GuideCard> records = mapper.selectGuides(false, trim(keyword), null, safeSort,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(assembler::guideCard).toList();
        return Paging.of(records, mapper.countGuides(false, trim(keyword), null), safePage, safeSize);
    }

@Override
public ContentVos.GuideDetail guide(Long id, Long userId) {
        TravelGuide guide = mapper.selectGuide(id);
        if (guide == null || !Integer.valueOf(1).equals(guide.getStatus())) {
            throw BizException.notFound();
        }
        return assembler.guideDetail(guide, true,
                userId == null ? null : appUserService.isFavorite(userId, BizType.GUIDE, id));
    }

@Override
public PageResult<ContentVos.FoodCard> foods(String keyword, Long categoryId, String flavor,
                                                  String sort, Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        String safeSort = sort(sort == null ? "HEAT_DESC" : sort);
        List<ContentVos.FoodCard> records = mapper.selectFoods(false, trim(keyword), categoryId,
                        trim(flavor), null, safeSort, Paging.offset(safePage, safeSize), safeSize)
                .stream().map(assembler::foodCard).toList();
        return Paging.of(records, mapper.countFoods(false, trim(keyword), categoryId, trim(flavor), null),
                safePage, safeSize);
    }

@Override
public ContentVos.FoodDetail food(Long id, Long userId) {
        Food food = mapper.selectFood(id);
        if (food == null || !Integer.valueOf(1).equals(food.getStatus())) {
            throw BizException.notFound();
        }
        return assembler.foodDetail(food,
                userId == null ? null : appUserService.isFavorite(userId, BizType.FOOD, id));
    }

@Override
public PageResult<ContentVos.SearchItem> search(String keyword, String type, Integer page, Integer pageSize) {
        String query = trim(keyword);
        if (query == null) {
            throw new BizException(com.luoyang.tourism.common.api.ResultCode.BAD_REQUEST, "搜索关键词不能为空");
        }
        String category = type == null ? "ALL" : type.toUpperCase(Locale.ROOT);
        if (!Set.of("ALL", "SPOT", "GUIDE", "FOOD").contains(category)) {
            throw new BizException(com.luoyang.tourism.common.api.ResultCode.UNSUPPORTED_VALUE);
        }
        List<ContentVos.SearchItem> all = new ArrayList<>();
        if (category.equals("ALL") || category.equals("SPOT")) {
            mapper.selectSpots(false, query, null, List.of(), null, "HEAT_DESC", 0, 50).forEach(spot ->
                    all.add(new ContentVos.SearchItem("SPOT", Long.toString(spot.getId()), spot.getName(),
                            spot.getSummary(), fileUrlService.signedUrl(spot.getCoverFileId()), "NAME",
                            spot.getHeatScore() == null ? 0 : spot.getHeatScore())));
        }
        if (category.equals("ALL") || category.equals("GUIDE")) {
            mapper.selectGuides(false, query, null, "HEAT_DESC", 0, 50).forEach(guide ->
                    all.add(new ContentVos.SearchItem("GUIDE", Long.toString(guide.getId()), guide.getTitle(),
                            guide.getSummary(), fileUrlService.signedUrl(guide.getCoverFileId()), "TITLE",
                            guide.getHeatScore() == null ? 0 : guide.getHeatScore())));
        }
        if (category.equals("ALL") || category.equals("FOOD")) {
            mapper.selectFoods(false, query, null, null, null, "HEAT_DESC", 0, 50).forEach(food ->
                    all.add(new ContentVos.SearchItem("FOOD", Long.toString(food.getId()), food.getName(),
                            food.getSummary(), fileUrlService.signedUrl(food.getCoverFileId()), "NAME",
                            food.getHeatScore() == null ? 0 : food.getHeatScore())));
        }
        all.sort(Comparator.comparing(ContentVos.SearchItem::sortScore).reversed());
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        int from = (int) Math.min(Paging.offset(safePage, safeSize), all.size());
        int to = Math.min(from + safeSize, all.size());
        return Paging.of(new ArrayList<>(all.subList(from, to)), all.size(), safePage, safeSize);
    }

    private ContentVos.CategoryVO toCategory(MutableCategory node) {
        List<ContentVos.CategoryVO> children = node.children.stream()
                .sorted(Comparator.comparing(child -> child.category.getSortOrder()))
                .map(this::toCategory).toList();
        ScenicCategory category = node.category;
        return new ContentVos.CategoryVO(Long.toString(category.getId()), Long.toString(category.getParentId()),
                category.getName(), category.getCode(), fileUrlService.signedUrl(category.getIconFileId()),
                category.getDescription(), category.getSortOrder(), node.spotCount, children);
    }

    private String sort(String value) {
        String normalized = value == null ? "" : value.toUpperCase(Locale.ROOT);
        if (!SORTS.contains(normalized)) {
            throw new BizException(com.luoyang.tourism.common.api.ResultCode.UNSUPPORTED_VALUE);
        }
        return "RECOMMEND".equals(normalized) ? "HEAT_DESC" : normalized;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class MutableCategory {
        private final ScenicCategory category;
        private final long spotCount;
        private final List<MutableCategory> children = new ArrayList<>();

        private MutableCategory(ScenicCategory category, long spotCount) {
            this.category = category;
            this.spotCount = spotCount;
        }
    }
}
