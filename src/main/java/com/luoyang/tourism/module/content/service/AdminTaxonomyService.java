package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.content.dto.ContentDtos;
import com.luoyang.tourism.module.content.entity.ScenicCategory;
import com.luoyang.tourism.module.content.entity.Tag;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.content.vo.ContentVos;
import com.luoyang.tourism.module.file.service.FileUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTaxonomyService {
    private final ContentMapper mapper;
    private final FileUrlService fileUrlService;

    public List<ContentVos.AdminCategory> categories(String keyword, Integer status) {
        return mapper.selectCategories(true, keyword, status).stream().map(this::adminCategory).toList();
    }

    public ContentVos.AdminCategory category(Long id) {
        return adminCategory(requiredCategory(id));
    }

    @Transactional
    public ContentVos.AdminCategory createCategory(ContentDtos.CategoryUpsertRequest request, Long adminId) {
        if (mapper.countCategoryCode(request.code(), null) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "分类编码已存在");
        }
        Long parentId = request.parentId() == null ? 0L : request.parentId();
        validateParent(parentId, null);
        validateFile(request.iconFileId());
        ScenicCategory category = new ScenicCategory();
        category.setParentId(parentId);
        category.setName(request.name().trim());
        category.setCode(request.code().trim());
        category.setIconFileId(request.iconFileId());
        category.setDescription(request.description());
        category.setSortOrder(value(request.sortOrder()));
        category.setStatus(status(request.status(), 1));
        category.setCreatedBy(adminId);
        category.setUpdatedBy(adminId);
        mapper.insertCategory(category);
        return adminCategory(requiredCategory(category.getId()));
    }

    @Transactional
    public ContentVos.AdminCategory updateCategory(Long id, ContentDtos.CategoryUpsertRequest request, Long adminId) {
        ScenicCategory current = requiredCategory(id);
        requireVersion(request.version());
        if (mapper.countCategoryCode(request.code(), id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "分类编码已存在");
        }
        Long parentId = request.parentId() == null ? 0L : request.parentId();
        if (id.equals(parentId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "分类不能以自身为父级");
        }
        validateParent(parentId, id);
        validateFile(request.iconFileId());
        current.setParentId(parentId);
        current.setName(request.name().trim());
        current.setCode(request.code().trim());
        current.setIconFileId(request.iconFileId());
        current.setDescription(request.description());
        current.setSortOrder(value(request.sortOrder()));
        current.setStatus(status(request.status(), 1));
        current.setVersion(request.version());
        current.setUpdatedBy(adminId);
        requireUpdated(mapper.updateCategory(current));
        return adminCategory(requiredCategory(id));
    }

    public ContentVos.AdminCategory updateCategoryStatus(Long id, ContentDtos.StatusRequest request, Long adminId) {
        requiredCategory(id);
        requireUpdated(mapper.updateCategoryStatus(id, status(request.status(), 1), request.version(), adminId));
        return adminCategory(requiredCategory(id));
    }

    public void deleteCategory(Long id, Integer version, Long adminId) {
        requiredCategory(id);
        if (mapper.countChildCategories(id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "分类下仍存在子分类");
        }
        if (mapper.countSpotsInCategory(id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "分类下仍存在景点");
        }
        requireUpdated(mapper.softDeleteCategory(id, version, adminId));
    }

    public PageResult<ContentVos.AdminTag> tags(Integer tagType, String keyword, Integer status,
                                                Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<ContentVos.AdminTag> records = mapper.selectTags(true, tagType, keyword, status,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(this::adminTag).toList();
        return Paging.of(records, mapper.countTags(true, tagType, keyword, status), safePage, safeSize);
    }

    @Transactional
    public ContentVos.AdminTag createTag(ContentDtos.TagUpsertRequest request, Long adminId) {
        if (mapper.countTagCode(request.tagType(), request.code(), null) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "标签编码已存在");
        }
        Tag tag = new Tag();
        tag.setTagType(request.tagType());
        tag.setName(request.name().trim());
        tag.setCode(request.code().trim());
        tag.setColor(request.color());
        tag.setDescription(request.description());
        tag.setSortOrder(value(request.sortOrder()));
        tag.setStatus(status(request.status(), 1));
        tag.setCreatedBy(adminId);
        tag.setUpdatedBy(adminId);
        mapper.insertTag(tag);
        return adminTag(requiredTag(tag.getId()));
    }

    @Transactional
    public ContentVos.AdminTag updateTag(Long id, ContentDtos.TagUpsertRequest request, Long adminId) {
        Tag tag = requiredTag(id);
        requireVersion(request.version());
        if (mapper.countTagCode(request.tagType(), request.code(), id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "标签编码已存在");
        }
        tag.setTagType(request.tagType());
        tag.setName(request.name().trim());
        tag.setCode(request.code().trim());
        tag.setColor(request.color());
        tag.setDescription(request.description());
        tag.setSortOrder(value(request.sortOrder()));
        tag.setStatus(status(request.status(), 1));
        tag.setUpdatedBy(adminId);
        tag.setVersion(request.version());
        requireUpdated(mapper.updateTag(tag));
        return adminTag(requiredTag(id));
    }

    public ContentVos.AdminTag updateTagStatus(Long id, ContentDtos.StatusRequest request, Long adminId) {
        requiredTag(id);
        requireUpdated(mapper.updateTagStatus(id, status(request.status(), 1), request.version(), adminId));
        return adminTag(requiredTag(id));
    }

    public void deleteTag(Long id, Integer version, Long adminId) {
        requiredTag(id);
        if (mapper.countSpotTagRefs(id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "标签仍被景点引用，请先迁移或禁用");
        }
        requireUpdated(mapper.softDeleteTag(id, version, adminId));
    }

    public List<ContentVos.AdminFoodCategory> foodCategories(Integer status) {
        return mapper.selectFoodCategories(true, status).stream().map(this::adminFoodCategory).toList();
    }

    @Transactional
    public ContentVos.AdminFoodCategory createFoodCategory(ContentDtos.FoodCategoryUpsertRequest request, Long adminId) {
        if (mapper.countFoodCategoryCode(request.code(), null) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "美食分类编码已存在");
        }
        validateFile(request.iconFileId());
        var category = new com.luoyang.tourism.module.content.entity.FoodCategory();
        category.setName(request.name().trim());
        category.setCode(request.code().trim());
        category.setIconFileId(request.iconFileId());
        category.setSortOrder(value(request.sortOrder()));
        category.setStatus(status(request.status(), 1));
        category.setCreatedBy(adminId);
        category.setUpdatedBy(adminId);
        mapper.insertFoodCategory(category);
        return adminFoodCategory(requiredFoodCategory(category.getId()));
    }

    @Transactional
    public ContentVos.AdminFoodCategory updateFoodCategory(Long id, ContentDtos.FoodCategoryUpsertRequest request,
                                                           Long adminId) {
        var category = requiredFoodCategory(id);
        requireVersion(request.version());
        if (mapper.countFoodCategoryCode(request.code(), id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "美食分类编码已存在");
        }
        validateFile(request.iconFileId());
        category.setName(request.name().trim());
        category.setCode(request.code().trim());
        category.setIconFileId(request.iconFileId());
        category.setSortOrder(value(request.sortOrder()));
        category.setStatus(status(request.status(), 1));
        category.setUpdatedBy(adminId);
        category.setVersion(request.version());
        requireUpdated(mapper.updateFoodCategory(category));
        return adminFoodCategory(requiredFoodCategory(id));
    }

    public ContentVos.AdminFoodCategory updateFoodCategoryStatus(Long id, ContentDtos.StatusRequest request,
                                                                 Long adminId) {
        requiredFoodCategory(id);
        requireUpdated(mapper.updateFoodCategoryStatus(id, status(request.status(), 1), request.version(), adminId));
        return adminFoodCategory(requiredFoodCategory(id));
    }

    public void deleteFoodCategory(Long id, Integer version, Long adminId) {
        requiredFoodCategory(id);
        if (mapper.countFoodsInCategory(id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "美食分类下仍存在美食");
        }
        requireUpdated(mapper.softDeleteFoodCategory(id, version, adminId));
    }

    private ScenicCategory requiredCategory(Long id) {
        ScenicCategory category = mapper.selectCategory(id);
        if (category == null) {
            throw BizException.notFound();
        }
        return category;
    }

    private Tag requiredTag(Long id) {
        Tag tag = mapper.selectTag(id);
        if (tag == null) {
            throw BizException.notFound();
        }
        return tag;
    }

    private com.luoyang.tourism.module.content.entity.FoodCategory requiredFoodCategory(Long id) {
        var category = mapper.selectFoodCategory(id);
        if (category == null) {
            throw BizException.notFound();
        }
        return category;
    }

    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null || parentId == 0) {
            return;
        }
        if (parentId.equals(selfId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "分类不能以自身为父级");
        }
        if (mapper.selectCategory(parentId) == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "父分类不存在");
        }
    }

    private void validateFile(Long fileId) {
        if (fileId != null) {
            fileUrlService.requireUsable(fileId);
        }
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

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private int status(Integer value, int defaultValue) {
        int result = value == null ? defaultValue : value;
        if (result != 0 && result != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "状态值不合法");
        }
        return result;
    }

    private ContentVos.AdminCategory adminCategory(ScenicCategory category) {
        return new ContentVos.AdminCategory(Long.toString(category.getId()), Long.toString(category.getParentId()),
                category.getName(), category.getCode(), stringId(category.getIconFileId()),
                fileUrlService.signedUrl(category.getIconFileId()), category.getDescription(),
                category.getSortOrder(), category.getStatus(), category.getVersion(),
                TimeUtils.offset(category.getUpdatedAt()));
    }

    private ContentVos.AdminTag adminTag(Tag tag) {
        return new ContentVos.AdminTag(Long.toString(tag.getId()), tag.getTagType(), tag.getName(), tag.getCode(),
                tag.getColor(), tag.getDescription(), tag.getSortOrder(), tag.getStatus(), tag.getVersion(),
                TimeUtils.offset(tag.getUpdatedAt()));
    }

    private ContentVos.AdminFoodCategory adminFoodCategory(com.luoyang.tourism.module.content.entity.FoodCategory category) {
        return new ContentVos.AdminFoodCategory(Long.toString(category.getId()), category.getName(),
                category.getCode(), stringId(category.getIconFileId()),
                fileUrlService.signedUrl(category.getIconFileId()), category.getSortOrder(),
                category.getStatus(), category.getVersion(), TimeUtils.offset(category.getUpdatedAt()));
    }

    private String stringId(Long id) {
        return id == null ? null : Long.toString(id);
    }
}
