package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.system.pojo.entity.AdminOperationLog;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import java.time.LocalDateTime;

/**
 * AuditLogService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface AuditLogService {
    /** 分页查询数据列表。 */
    public PageResult<SystemVos.OperationLogVO> list(Long adminId, String module, Integer success, LocalDateTime from, LocalDateTime to, Integer page, Integer pageSize);

    /** 记录审计日志。 */
    public void record(AdminOperationLog entry);
}
