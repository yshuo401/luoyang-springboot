package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;

/**
 * AdminFoodService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface AdminFoodService {
    /** 分页查询数据列表。 */
    public PageResult<ContentVos.AdminFood> list(String keyword, Long categoryId, Integer status, Integer page, Integer pageSize);

    /** 查询详情。 */
    public ContentVos.AdminFood detail(Long id);

    /** 创建数据。 */
    public ContentVos.AdminFood create(ContentDtos.FoodUpsertRequest request, Long adminId);

    /** 更新数据。 */
    public ContentVos.AdminFood update(Long id, ContentDtos.FoodUpsertRequest request, Long adminId);

    /** 发布内容。 */
    public ContentVos.AdminFood publish(Long id, Integer version, Long adminId);

    /** 下架内容。 */
    public ContentVos.AdminFood unpublish(Long id, Integer version, Long adminId);

    /** 删除数据。 */
    public void delete(Long id, Integer version, Long adminId);
}
