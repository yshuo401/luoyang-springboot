package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;

/**
 * AdminBannerService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface AdminBannerService {
    /** 分页查询数据列表。 */
    public PageResult<ContentVos.AdminBanner> list(String position, Integer status, Integer page, Integer pageSize);

    /** 查询详情。 */
    public ContentVos.AdminBanner detail(Long id);

    /** 创建数据。 */
    public ContentVos.AdminBanner create(ContentDtos.BannerUpsertRequest request, Long adminId);

    /** 更新数据。 */
    public ContentVos.AdminBanner update(Long id, ContentDtos.BannerUpsertRequest request, Long adminId);

    /** 执行 updateStatus 业务操作。 */
    public ContentVos.AdminBanner updateStatus(Long id, ContentDtos.StatusRequest request, Long adminId);

    /** 删除数据。 */
    public void delete(Long id, Integer version, Long adminId);
}
