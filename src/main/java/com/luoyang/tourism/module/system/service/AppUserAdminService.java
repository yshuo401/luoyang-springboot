package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import java.time.LocalDateTime;

/**
 * AppUserAdminService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface AppUserAdminService {
    /** 分页查询数据列表。 */
    public PageResult<SystemVos.AppUserAdminVO> list(String keyword, Integer status, LocalDateTime createdFrom, LocalDateTime createdTo, Integer page, Integer pageSize);

    /** 查询详情。 */
    public SystemVos.AppUserAdminVO detail(Long id);

    /** 执行 updateStatus 业务操作。 */
    public SystemVos.AppUserAdminVO updateStatus(Long id, SystemDtos.AppUserStatusRequest request);
}
