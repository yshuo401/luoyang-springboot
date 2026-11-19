package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import java.util.List;

/**
 * AdminTaxonomyService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface AdminTaxonomyService {
    /** 执行 categories 业务操作。 */
    public List<ContentVos.AdminCategory> categories(String keyword, Integer status);

    /** 执行 category 业务操作。 */
    public ContentVos.AdminCategory category(Long id);

    /** 执行 createCategory 业务操作。 */
    public ContentVos.AdminCategory createCategory(ContentDtos.CategoryUpsertRequest request, Long adminId);

    /** 执行 updateCategory 业务操作。 */
    public ContentVos.AdminCategory updateCategory(Long id, ContentDtos.CategoryUpsertRequest request, Long adminId);

    /** 执行 updateCategoryStatus 业务操作。 */
    public ContentVos.AdminCategory updateCategoryStatus(Long id, ContentDtos.StatusRequest request, Long adminId);

    /** 执行 deleteCategory 业务操作。 */
    public void deleteCategory(Long id, Integer version, Long adminId);

    /** 执行 tags 业务操作。 */
    public PageResult<ContentVos.AdminTag> tags(Integer tagType, String keyword, Integer status, Integer page, Integer pageSize);

    /** 执行 createTag 业务操作。 */
    public ContentVos.AdminTag createTag(ContentDtos.TagUpsertRequest request, Long adminId);

    /** 执行 updateTag 业务操作。 */
    public ContentVos.AdminTag updateTag(Long id, ContentDtos.TagUpsertRequest request, Long adminId);

    /** 执行 updateTagStatus 业务操作。 */
    public ContentVos.AdminTag updateTagStatus(Long id, ContentDtos.StatusRequest request, Long adminId);

    /** 执行 deleteTag 业务操作。 */
    public void deleteTag(Long id, Integer version, Long adminId);

    /** 执行 foodCategories 业务操作。 */
    public List<ContentVos.AdminFoodCategory> foodCategories(Integer status);

    /** 执行 createFoodCategory 业务操作。 */
    public ContentVos.AdminFoodCategory createFoodCategory(ContentDtos.FoodCategoryUpsertRequest request, Long adminId);

    /** 执行 updateFoodCategory 业务操作。 */
    public ContentVos.AdminFoodCategory updateFoodCategory(Long id, ContentDtos.FoodCategoryUpsertRequest request, Long adminId);

    /** 执行 updateFoodCategoryStatus 业务操作。 */
    public ContentVos.AdminFoodCategory updateFoodCategoryStatus(Long id, ContentDtos.StatusRequest request, Long adminId);

    /** 执行 deleteFoodCategory 业务操作。 */
    public void deleteFoodCategory(Long id, Integer version, Long adminId);
}
