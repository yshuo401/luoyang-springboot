package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.module.content.dto.ContentDtos;
import com.luoyang.tourism.module.content.entity.Banner;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.content.vo.ContentVos;
import com.luoyang.tourism.module.file.service.FileUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBannerService {
    private final ContentMapper mapper;
    private final ContentAssembler assembler;
    private final FileUrlService fileUrlService;

    public PageResult<ContentVos.AdminBanner> list(String position, Integer status,
                                                   Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<ContentVos.AdminBanner> records = mapper.selectBanners(position, status,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(assembler::adminBanner).toList();
        return Paging.of(records, mapper.countBanners(position, status), safePage, safeSize);
    }

    public ContentVos.AdminBanner detail(Long id) {
        return assembler.adminBanner(requiredBanner(id));
    }

    @Transactional
    public ContentVos.AdminBanner create(ContentDtos.BannerUpsertRequest request, Long adminId) {
        validate(request);
        Banner banner = new Banner();
        apply(banner, request, adminId);
        banner.setCreatedBy(adminId);
        banner.setUpdatedBy(adminId);
        mapper.insertBanner(banner);
        return assembler.adminBanner(requiredBanner(banner.getId()));
    }

    @Transactional
    public ContentVos.AdminBanner update(Long id, ContentDtos.BannerUpsertRequest request, Long adminId) {
        Banner banner = requiredBanner(id);
        requireVersion(request.version());
        validate(request);
        apply(banner, request, adminId);
        banner.setVersion(request.version());
        requireUpdated(mapper.updateBanner(banner));
        return assembler.adminBanner(requiredBanner(id));
    }

    public ContentVos.AdminBanner updateStatus(Long id, ContentDtos.StatusRequest request, Long adminId) {
        requiredBanner(id);
        int status = request.status();
        if (status != 0 && status != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "轮播图状态值不合法");
        }
        requireUpdated(mapper.updateBannerStatus(id, status, request.version(), adminId));
        return assembler.adminBanner(requiredBanner(id));
    }

    public void delete(Long id, Integer version, Long adminId) {
        requiredBanner(id);
        requireUpdated(mapper.softDeleteBanner(id, version, adminId));
    }

    private void validate(ContentDtos.BannerUpsertRequest request) {
        fileUrlService.requireUsable(request.imageFileId());
        if (request.startAt() != null && request.endAt() != null && !request.startAt().isBefore(request.endAt())) {
            throw new BizException(ResultCode.BAD_REQUEST, "轮播图开始时间必须早于结束时间");
        }
        int linkType = request.linkType();
        if (linkType == 0) {
            return;
        }
        if (linkType == 4) {
            if (request.targetPath() == null || !request.targetPath().startsWith("/pages/")) {
                throw new BizException(ResultCode.BAD_REQUEST, "小程序页面路径不合法");
            }
            return;
        }
        if (request.targetId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "跳转目标不能为空");
        }
        boolean published = switch (linkType) {
            case 1 -> {
                var spot = mapper.selectSpot(request.targetId());
                yield spot != null && Integer.valueOf(1).equals(spot.getStatus());
            }
            case 2 -> {
                var guide = mapper.selectGuide(request.targetId());
                yield guide != null && Integer.valueOf(1).equals(guide.getStatus());
            }
            case 3 -> {
                var food = mapper.selectFood(request.targetId());
                yield food != null && Integer.valueOf(1).equals(food.getStatus());
            }
            default -> false;
        };
        if (!published) {
            throw new BizException(ResultCode.BAD_REQUEST, "跳转目标不存在或未发布");
        }
    }

    private void apply(Banner banner, ContentDtos.BannerUpsertRequest request, Long adminId) {
        banner.setTitle(request.title().trim());
        banner.setSubtitle(request.subtitle());
        banner.setImageFileId(request.imageFileId());
        banner.setLinkType(request.linkType());
        banner.setTargetId(request.targetId());
        banner.setTargetPath(request.targetPath());
        banner.setPosition(request.position().trim());
        banner.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        banner.setStatus(request.status() == null ? 1 : request.status());
        banner.setStartAt(request.startAt());
        banner.setEndAt(request.endAt());
        banner.setUpdatedBy(adminId);
    }

    private Banner requiredBanner(Long id) {
        Banner banner = mapper.selectBanner(id);
        if (banner == null) {
            throw BizException.notFound();
        }
        return banner;
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
