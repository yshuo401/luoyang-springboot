package com.luoyang.tourism.module.system.service.impl;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.system.mapper.SystemMapper;
import com.luoyang.tourism.module.system.pojo.entity.AdminOperationLog;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import com.luoyang.tourism.module.system.service.AuditLogService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AuditLogServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final SystemMapper mapper;

@Override
public PageResult<SystemVos.OperationLogVO> list(Long adminId, String module, Integer success,
                                                     LocalDateTime from, LocalDateTime to,
                                                     Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<SystemVos.OperationLogVO> records = mapper.selectOperationLogs(adminId, module, success, from, to,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(this::vo).toList();
        return Paging.of(records, mapper.countOperationLogs(adminId, module, success, from, to),
                safePage, safeSize);
    }

@Override
public void record(AdminOperationLog entry) {
        try {
            mapper.insertOperationLog(entry);
        } catch (RuntimeException exception) {
            log.warn("admin operation log insert failed traceId={}", entry.getTraceId());
        }
    }

    private SystemVos.OperationLogVO vo(AdminOperationLog log) {
        return new SystemVos.OperationLogVO(Long.toString(log.getId()), Long.toString(log.getAdminUserId()),
                log.getUsernameSnapshot(), log.getModule(), log.getOperationType(), log.getHttpMethod(),
                log.getRequestUri(), log.getRequestParams(), log.getResponseCode(),
                Integer.valueOf(1).equals(log.getSuccess()), log.getIp(), log.getTraceId(),
                log.getDurationMs(), TimeUtils.offset(log.getCreatedAt()));
    }
}
