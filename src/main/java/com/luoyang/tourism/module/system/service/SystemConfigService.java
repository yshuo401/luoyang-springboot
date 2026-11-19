package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;

/**
 * SystemConfigService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface SystemConfigService {
    /** 分页查询数据列表。 */
    public PageResult<SystemVos.ConfigVO> list(String keyword, Integer page, Integer pageSize);

    /** 更新数据。 */
    public SystemVos.ConfigVO update(String key, SystemDtos.ConfigUpdateRequest request, Long operatorId);
}
