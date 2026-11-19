package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;

/**
 * AdminSpotService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface AdminSpotService {
    /** 分页查询数据列表。 */
    public PageResult<ContentVos.AdminSpot> list(String keyword, Long categoryId, Integer status, Integer page, Integer pageSize);

    /** 查询详情。 */
    public ContentVos.AdminSpot detail(Long id);

    /** 创建数据。 */
    public ContentVos.AdminSpot create(ContentDtos.SpotUpsertRequest request, Long adminId);

    /** 更新数据。 */
    public ContentVos.AdminSpot update(Long id, ContentDtos.SpotUpsertRequest request, Long adminId);

    /** 发布内容。 */
    public ContentVos.AdminSpot publish(Long id, Integer version, Long adminId);

    /** 下架内容。 */
    public ContentVos.AdminSpot unpublish(Long id, Integer version, Long adminId);

    /** 执行 updateWeight 业务操作。 */
    public ContentVos.AdminSpot updateWeight(Long id, ContentDtos.WeightRequest request, Long adminId);

    /** 删除数据。 */
    public void delete(Long id, Integer version, Long adminId);
}
