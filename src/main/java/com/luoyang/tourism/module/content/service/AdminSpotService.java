package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.HtmlSanitizer;
import com.luoyang.tourism.module.content.dto.ContentDtos;
import com.luoyang.tourism.module.content.entity.ScenicCategory;
import com.luoyang.tourism.module.content.entity.ScenicImage;
import com.luoyang.tourism.module.content.entity.ScenicSpot;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.content.vo.ContentVos;
import com.luoyang.tourism.module.file.service.FileUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminSpotService {
    private final ContentMapper mapper;
    private final ContentAssembler assembler;
    private final FileUrlService fileUrlService;

    public PageResult<ContentVos.AdminSpot> list(String keyword, Long categoryId, Integer status,
                                                 Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<ContentVos.AdminSpot> records = mapper.selectSpots(true, keyword, categoryId, List.of(),
                        status, null, Paging.offset(safePage, safeSize), safeSize)
                .stream().map(assembler::adminSpot).toList();
        return Paging.of(records, mapper.countSpots(true, keyword, categoryId, List.of(), status),
                safePage, safeSize);
    }

    public ContentVos.AdminSpot detail(Long id) {
        return assembler.adminSpot(requiredSpot(id));
    }

    @Transactional
    public ContentVos.AdminSpot create(ContentDtos.SpotUpsertRequest request, Long adminId) {
        validateRequest(request);
        ScenicSpot spot = new ScenicSpot();
        apply(spot, request, adminId);
        spot.setStatus(0);
        spot.setCreatedBy(adminId);
        spot.setUpdatedBy(adminId);
        mapper.insertSpot(spot);
        replaceRelations(spot.getId(), request, adminId);
        return assembler.adminSpot(requiredSpot(spot.getId()));
    }

    @Transactional
    public ContentVos.AdminSpot update(Long id, ContentDtos.SpotUpsertRequest request, Long adminId) {
        ScenicSpot spot = requiredSpot(id);
        requireVersion(request.version());
        validateRequest(request);
        apply(spot, request, adminId);
        spot.setVersion(request.version());
        requireUpdated(mapper.updateSpot(spot));
        replaceRelations(id, request, adminId);
        return assembler.adminSpot(requiredSpot(id));
    }

    public ContentVos.AdminSpot publish(Long id, Integer version, Long adminId) {
        ScenicSpot spot = requiredSpot(id);
        if (blank(spot.getCoverFileId()) || blank(spot.getContentHtml())) {
            throw new BizException(ResultCode.BAD_REQUEST, "发布前必须配置封面和正文");
        }
        LocalDateTime publishedAt = spot.getPublishedAt() == null ? LocalDateTime.now() : null;
        requireUpdated(mapper.updateSpotStatus(id, 1, version, adminId, publishedAt));
        return assembler.adminSpot(requiredSpot(id));
    }

    public ContentVos.AdminSpot unpublish(Long id, Integer version, Long adminId) {
        requiredSpot(id);
        requireUpdated(mapper.updateSpotStatus(id, 2, version, adminId, null));
        return assembler.adminSpot(requiredSpot(id));
    }

    public ContentVos.AdminSpot updateWeight(Long id, ContentDtos.WeightRequest request, Long adminId) {
        requiredSpot(id);
        requireUpdated(mapper.updateSpotWeight(id, request.recommendationWeight(), request.version(), adminId));
        return assembler.adminSpot(requiredSpot(id));
    }

    @Transactional
    public void delete(Long id, Integer version, Long adminId) {
        requiredSpot(id);
        requireUpdated(mapper.softDeleteSpot(id, version, adminId));
        mapper.deleteSpotImages(id);
        mapper.deleteSpotTags(id);
    }

    private void validateRequest(ContentDtos.SpotUpsertRequest request) {
        ScenicCategory category = mapper.selectCategory(request.categoryId());
        if (category == null || !Integer.valueOf(1).equals(category.getStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST, "景点分类不存在或未启用");
        }
        if (request.coverFileId() != null) {
            fileUrlService.requireUsable(request.coverFileId());
        }
        validateTags(request.tagIds());
        validateImages(request.imageFileIds());
    }

    private void validateTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        Set<Long> unique = new LinkedHashSet<>(tagIds);
        if (unique.size() != tagIds.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "标签不能重复");
        }
        if (mapper.selectTagsByIds(new ArrayList<>(unique)).size() != unique.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "标签不存在或未启用");
        }
    }

    private void validateImages(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        Set<Long> unique = new LinkedHashSet<>(fileIds);
        if (unique.size() != fileIds.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "图片不能重复");
        }
        unique.forEach(fileUrlService::requireUsable);
    }

    private void replaceRelations(Long spotId, ContentDtos.SpotUpsertRequest request, Long adminId) {
        mapper.deleteSpotTags(spotId);
        if (request.tagIds() != null) {
            new LinkedHashSet<>(request.tagIds()).forEach(tagId -> mapper.insertSpotTag(spotId, tagId, adminId));
        }
        mapper.deleteSpotImages(spotId);
        if (request.imageFileIds() != null) {
            int order = 0;
            for (Long fileId : request.imageFileIds()) {
                ScenicImage image = new ScenicImage();
                image.setSpotId(spotId);
                image.setFileId(fileId);
                image.setSortOrder(order++);
                image.setIsCover(0);
                image.setCreatedBy(adminId);
                mapper.insertSpotImage(image);
            }
        }
    }

    private void apply(ScenicSpot spot, ContentDtos.SpotUpsertRequest request, Long adminId) {
        spot.setCategoryId(request.categoryId());
        spot.setName(request.name().trim());
        spot.setSubtitle(request.subtitle());
        spot.setSummary(request.summary());
        spot.setCoverFileId(request.coverFileId());
        spot.setContentHtml(HtmlSanitizer.clean(request.contentHtml()));
        spot.setAddress(request.address());
        spot.setLongitude(request.longitude());
        spot.setLatitude(request.latitude());
        spot.setOpenTimeDesc(request.openTimeDesc());
        spot.setTicketInfo(request.ticketInfo());
        spot.setPlayDurationMinutes(request.playDurationMinutes());
        spot.setPhone(request.phone());
        spot.setTrafficInfo(request.trafficInfo());
        spot.setTips(request.tips());
        spot.setRecommendationWeight(request.recommendationWeight() == null ? 0 : request.recommendationWeight());
        spot.setHeatScore(spot.getHeatScore() == null ? 0 : spot.getHeatScore());
        spot.setUpdatedBy(adminId);
    }

    private ScenicSpot requiredSpot(Long id) {
        ScenicSpot spot = mapper.selectSpot(id);
        if (spot == null) {
            throw BizException.notFound();
        }
        return spot;
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

    private boolean blank(Object value) {
        return value == null || value.toString().isBlank();
    }
}
