package com.luoyang.tourism.module.content.service.impl;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.HtmlSanitizer;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.content.pojo.entity.GuideDay;
import com.luoyang.tourism.module.content.pojo.entity.GuideDaySpot;
import com.luoyang.tourism.module.content.pojo.entity.ScenicSpot;
import com.luoyang.tourism.module.content.pojo.entity.TravelGuide;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import com.luoyang.tourism.module.content.service.AdminGuideService;
import com.luoyang.tourism.module.content.service.ContentAssembler;
import com.luoyang.tourism.module.file.service.FileUrlService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AdminGuideServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
@RequiredArgsConstructor
public class AdminGuideServiceImpl implements AdminGuideService {
    private final ContentMapper mapper;
    private final ContentAssembler assembler;
    private final FileUrlService fileUrlService;

@Override
public PageResult<ContentVos.AdminGuide> list(String keyword, Integer status,
                                                  Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<ContentVos.AdminGuide> records = mapper.selectGuides(true, keyword, status, null,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(assembler::adminGuide).toList();
        return Paging.of(records, mapper.countGuides(true, keyword, status), safePage, safeSize);
    }

@Override
public ContentVos.AdminGuide detail(Long id) {
        return assembler.adminGuide(requiredGuide(id));
    }

    @Transactional
@Override
public ContentVos.AdminGuide create(ContentDtos.GuideUpsertRequest request, Long adminId) {
        validateBase(request);
        TravelGuide guide = new TravelGuide();
        apply(guide, request, adminId);
        guide.setStatus(0);
        guide.setCreatedBy(adminId);
        guide.setUpdatedBy(adminId);
        mapper.insertGuide(guide);
        if (request.days() != null) {
            replaceDaysInternal(guide.getId(), request.days());
            if (request.travelDays() == null) {
                TravelGuide refresh = requiredGuide(guide.getId());
                refresh.setTravelDays(request.days().size());
                refresh.setVersion(refresh.getVersion());
                mapper.updateGuide(refresh);
            }
        }
        return assembler.adminGuide(requiredGuide(guide.getId()));
    }

    @Transactional
@Override
public ContentVos.AdminGuide update(Long id, ContentDtos.GuideUpsertRequest request, Long adminId) {
        TravelGuide guide = requiredGuide(id);
        requireVersion(request.version());
        validateBase(request);
        apply(guide, request, adminId);
        guide.setVersion(request.version());
        requireUpdated(mapper.updateGuide(guide));
        if (request.days() != null) {
            replaceDaysInternal(id, request.days());
        }
        return assembler.adminGuide(requiredGuide(id));
    }

    @Transactional
@Override
public ContentVos.AdminGuide replaceDays(Long id, List<ContentDtos.DayRequest> days, Long adminId) {
        TravelGuide guide = requiredGuide(id);
        validateDays(days);
        replaceDaysInternal(id, days);
        guide = requiredGuide(id);
        guide.setTravelDays(days == null ? 0 : days.size());
        guide.setVersion(guide.getVersion());
        mapper.updateGuide(guide);
        return assembler.adminGuide(requiredGuide(id));
    }

@Override
public ContentVos.AdminGuide publish(Long id, Integer version, Long adminId) {
        TravelGuide guide = requiredGuide(id);
        if (guide.getCoverFileId() == null || guide.getContentHtml() == null || guide.getContentHtml().isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "发布前必须配置封面和正文");
        }
        if (mapper.selectGuideDays(id).isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "发布前必须配置每日行程");
        }
        LocalDateTime publishedAt = guide.getPublishedAt() == null ? LocalDateTime.now() : null;
        requireUpdated(mapper.updateGuideStatus(id, 1, version, adminId, publishedAt));
        return assembler.adminGuide(requiredGuide(id));
    }

@Override
public ContentVos.AdminGuide unpublish(Long id, Integer version, Long adminId) {
        requiredGuide(id);
        requireUpdated(mapper.updateGuideStatus(id, 2, version, adminId, null));
        return assembler.adminGuide(requiredGuide(id));
    }

    @Transactional
@Override
public void delete(Long id, Integer version, Long adminId) {
        requiredGuide(id);
        requireUpdated(mapper.softDeleteGuide(id, version, adminId));
        mapper.deleteGuideDaySpotsHard(id);
        mapper.deleteGuideDaysHard(id);
    }

    private void replaceDaysInternal(Long guideId, List<ContentDtos.DayRequest> days) {
        validateDays(days);
        mapper.deleteGuideDaySpotsHard(guideId);
        mapper.deleteGuideDaysHard(guideId);
        if (days == null) {
            return;
        }
        int expectedDay = 1;
        for (ContentDtos.DayRequest dayRequest : days) {
            if (dayRequest.dayNo() != expectedDay++) {
                throw new BizException(ResultCode.BAD_REQUEST, "行程天数必须从 1 开始连续");
            }
            GuideDay day = new GuideDay();
            day.setGuideId(guideId);
            day.setDayNo(dayRequest.dayNo());
            day.setTitle(dayRequest.title().trim());
            day.setSummary(dayRequest.summary());
            day.setTrafficTip(dayRequest.trafficTip());
            day.setAccommodationTip(dayRequest.accommodationTip());
            mapper.insertGuideDay(day);
            if (dayRequest.spots() != null) {
                int order = 0;
                for (ContentDtos.DaySpotRequest spotRequest : dayRequest.spots()) {
                    GuideDaySpot relation = new GuideDaySpot();
                    relation.setGuideDayId(day.getId());
                    relation.setGuideId(guideId);
                    relation.setSpotId(spotRequest.spotId());
                    relation.setSortOrder(spotRequest.sortOrder() == null ? order : spotRequest.sortOrder());
                    relation.setStayMinutes(spotRequest.stayMinutes());
                    relation.setNote(spotRequest.note());
                    mapper.insertGuideDaySpot(relation);
                    order++;
                }
            }
        }
    }

    private void validateDays(List<ContentDtos.DayRequest> days) {
        if (days == null || days.isEmpty()) {
            return;
        }
        Set<Integer> dayNos = new HashSet<>();
        Set<Long> spotIds = new HashSet<>();
        for (ContentDtos.DayRequest day : days) {
            if (!dayNos.add(day.dayNo())) {
                throw new BizException(ResultCode.BAD_REQUEST, "行程天数不能重复");
            }
            if (day.spots() == null) {
                continue;
            }
            for (ContentDtos.DaySpotRequest spot : day.spots()) {
                if (!spotIds.add(spot.spotId())) {
                    throw new BizException(ResultCode.BAD_REQUEST, "同一攻略中景点不能重复");
                }
                ScenicSpot detail = mapper.selectSpot(spot.spotId());
                if (detail == null || !Integer.valueOf(1).equals(detail.getStatus())) {
                    throw new BizException(ResultCode.BAD_REQUEST, "行程景点不存在或未发布");
                }
            }
        }
    }

    private void validateBase(ContentDtos.GuideUpsertRequest request) {
        if (request.coverFileId() != null) {
            fileUrlService.requireUsable(request.coverFileId());
        }
    }

    private void apply(TravelGuide guide, ContentDtos.GuideUpsertRequest request, Long adminId) {
        guide.setTitle(request.title().trim());
        guide.setSlug(request.slug());
        guide.setSummary(request.summary());
        guide.setCoverFileId(request.coverFileId());
        guide.setAuthorName(request.authorName());
        guide.setContentHtml(HtmlSanitizer.clean(request.contentHtml()));
        guide.setTravelDays(request.travelDays());
        guide.setDifficulty(request.difficulty());
        guide.setSuitableSeason(request.suitableSeason());
        guide.setRecommendationWeight(request.recommendationWeight() == null ? 0 : request.recommendationWeight());
        guide.setHeatScore(guide.getHeatScore() == null ? 0 : guide.getHeatScore());
        guide.setUpdatedBy(adminId);
    }

    private TravelGuide requiredGuide(Long id) {
        TravelGuide guide = mapper.selectGuide(id);
        if (guide == null) {
            throw BizException.notFound();
        }
        return guide;
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
