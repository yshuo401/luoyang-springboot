package com.luoyang.tourism.module.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.HtmlSanitizer;
import com.luoyang.tourism.module.content.dto.ContentDtos;
import com.luoyang.tourism.module.content.entity.Food;
import com.luoyang.tourism.module.content.entity.FoodCategory;
import com.luoyang.tourism.module.content.entity.FoodImage;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.content.vo.ContentVos;
import com.luoyang.tourism.module.file.service.FileUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminFoodService {
    private final ContentMapper mapper;
    private final ContentAssembler assembler;
    private final FileUrlService fileUrlService;
    private final ObjectMapper objectMapper;

    public PageResult<ContentVos.AdminFood> list(String keyword, Long categoryId, Integer status,
                                                 Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<ContentVos.AdminFood> records = mapper.selectFoods(true, keyword, categoryId, null, status,
                        null, Paging.offset(safePage, safeSize), safeSize)
                .stream().map(assembler::adminFood).toList();
        return Paging.of(records, mapper.countFoods(true, keyword, categoryId, null, status),
                safePage, safeSize);
    }

    public ContentVos.AdminFood detail(Long id) {
        return assembler.adminFood(requiredFood(id));
    }

    @Transactional
    public ContentVos.AdminFood create(ContentDtos.FoodUpsertRequest request, Long adminId) {
        validateRequest(request);
        Food food = new Food();
        apply(food, request, adminId);
        food.setStatus(0);
        food.setCreatedBy(adminId);
        food.setUpdatedBy(adminId);
        mapper.insertFood(food);
        replaceImages(food.getId(), request.imageFileIds(), adminId);
        return assembler.adminFood(requiredFood(food.getId()));
    }

    @Transactional
    public ContentVos.AdminFood update(Long id, ContentDtos.FoodUpsertRequest request, Long adminId) {
        Food food = requiredFood(id);
        requireVersion(request.version());
        validateRequest(request);
        apply(food, request, adminId);
        food.setVersion(request.version());
        requireUpdated(mapper.updateFood(food));
        replaceImages(id, request.imageFileIds(), adminId);
        return assembler.adminFood(requiredFood(id));
    }

    public ContentVos.AdminFood publish(Long id, Integer version, Long adminId) {
        Food food = requiredFood(id);
        if (food.getCoverFileId() == null || food.getDescription() == null || food.getDescription().isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "发布前必须配置封面和介绍");
        }
        LocalDateTime publishedAt = food.getPublishedAt() == null ? LocalDateTime.now() : null;
        requireUpdated(mapper.updateFoodStatus(id, 1, version, adminId, publishedAt));
        return assembler.adminFood(requiredFood(id));
    }

    public ContentVos.AdminFood unpublish(Long id, Integer version, Long adminId) {
        requiredFood(id);
        requireUpdated(mapper.updateFoodStatus(id, 2, version, adminId, null));
        return assembler.adminFood(requiredFood(id));
    }

    @Transactional
    public void delete(Long id, Integer version, Long adminId) {
        requiredFood(id);
        requireUpdated(mapper.softDeleteFood(id, version, adminId));
        mapper.deleteFoodImages(id);
    }

    private void validateRequest(ContentDtos.FoodUpsertRequest request) {
        FoodCategory category = mapper.selectFoodCategory(request.foodCategoryId());
        if (category == null || !Integer.valueOf(1).equals(category.getStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST, "美食分类不存在或未启用");
        }
        if (request.coverFileId() != null) {
            fileUrlService.requireUsable(request.coverFileId());
        }
        if (request.imageFileIds() != null) {
            LinkedHashSet<Long> unique = new LinkedHashSet<>(request.imageFileIds());
            if (unique.size() != request.imageFileIds().size()) {
                throw new BizException(ResultCode.BAD_REQUEST, "图片不能重复");
            }
            unique.forEach(fileUrlService::requireUsable);
        }
    }

    private void replaceImages(Long foodId, List<Long> fileIds, Long adminId) {
        mapper.deleteFoodImages(foodId);
        if (fileIds == null) {
            return;
        }
        int order = 0;
        for (Long fileId : fileIds) {
            FoodImage image = new FoodImage();
            image.setFoodId(foodId);
            image.setFileId(fileId);
            image.setSortOrder(order++);
            image.setIsCover(0);
            image.setCreatedBy(adminId);
            mapper.insertFoodImage(image);
        }
    }

    private void apply(Food food, ContentDtos.FoodUpsertRequest request, Long adminId) {
        food.setFoodCategoryId(request.foodCategoryId());
        food.setName(request.name().trim());
        food.setSummary(request.summary());
        food.setCoverFileId(request.coverFileId());
        food.setDescription(HtmlSanitizer.clean(request.description()));
        food.setPriceRange(request.priceRange());
        food.setFlavorTags(toJson(request.flavorTags()));
        food.setRecommendShops(request.recommendShops());
        food.setArea(request.area());
        food.setRecommendationWeight(request.recommendationWeight() == null ? 0 : request.recommendationWeight());
        food.setHeatScore(food.getHeatScore() == null ? 0 : food.getHeatScore());
        food.setUpdatedBy(adminId);
    }

    private String toJson(List<String> values) {
        try {
            return values == null ? null : objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new BizException(ResultCode.BAD_REQUEST, "口味标签格式不正确");
        }
    }

    private Food requiredFood(Long id) {
        Food food = mapper.selectFood(id);
        if (food == null) {
            throw BizException.notFound();
        }
        return food;
    }

    private void requireVersion(Integer version) {
        if (version == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "缺少数据版本号");
        }
    }

    private void requireUpdated(int affected) {
        if (affected != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
    }
}
