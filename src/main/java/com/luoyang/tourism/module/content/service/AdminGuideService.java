package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import java.util.List;

/**
 * AdminGuideService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface AdminGuideService {
    /** 分页查询数据列表。 */
    public PageResult<ContentVos.AdminGuide> list(String keyword, Integer status, Integer page, Integer pageSize);

    /** 查询详情。 */
    public ContentVos.AdminGuide detail(Long id);

    /** 创建数据。 */
    public ContentVos.AdminGuide create(ContentDtos.GuideUpsertRequest request, Long adminId);

    /** 更新数据。 */
    public ContentVos.AdminGuide update(Long id, ContentDtos.GuideUpsertRequest request, Long adminId);

    /** 执行 replaceDays 业务操作。 */
    public ContentVos.AdminGuide replaceDays(Long id, List<ContentDtos.DayRequest> days, Long adminId);

    /** 发布内容。 */
    public ContentVos.AdminGuide publish(Long id, Integer version, Long adminId);

    /** 下架内容。 */
    public ContentVos.AdminGuide unpublish(Long id, Integer version, Long adminId);

    /** 删除数据。 */
    public void delete(Long id, Integer version, Long adminId);
}
