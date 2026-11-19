package com.luoyang.tourism.module.system.service.impl;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.system.mapper.SystemMapper;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.entity.SystemConfig;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import com.luoyang.tourism.module.system.service.SystemConfigService;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SystemConfigServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {
    private static final Set<String> TYPES = Set.of("STRING", "NUMBER", "BOOLEAN", "JSON");
    private static final List<String> SENSITIVE_WORDS = List.of(
            "password", "secret", "access_key", "accesskey", "jwt", "api_key", "apikey", "private_key", "token");

    private final SystemMapper mapper;

@Override
public PageResult<SystemVos.ConfigVO> list(String keyword, Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<SystemVos.ConfigVO> records = mapper.selectConfigs(keyword, Paging.offset(safePage, safeSize), safeSize)
                .stream().map(this::vo).toList();
        return Paging.of(records, mapper.countConfigs(keyword), safePage, safeSize);
    }

    @Transactional
@Override
public SystemVos.ConfigVO update(String key, SystemDtos.ConfigUpdateRequest request, Long operatorId) {
        // 系统配置只允许保存非敏感业务参数，密钥类字段必须走环境变量或本地凭据文件。
        validateKey(key);
        String valueType = request.valueType().toUpperCase(Locale.ROOT);
        if (!TYPES.contains(valueType)) {
            throw new BizException(ResultCode.BAD_REQUEST, "配置值类型不支持");
        }
        SystemConfig config = mapper.selectConfig(key);
        if (config == null) {
            if (request.version() != 0) {
                throw new BizException(ResultCode.VERSION_CONFLICT);
            }
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(request.configValue());
            config.setValueType(valueType);
            config.setDescription(request.description());
            config.setStatus(1);
            config.setCreatedBy(operatorId);
            config.setUpdatedBy(operatorId);
            mapper.insertConfig(config);
            return vo(mapper.selectConfig(key));
        }
        if (Integer.valueOf(1).equals(config.getIsSensitive())) {
            throw new BizException(ResultCode.FORBIDDEN, "敏感配置不允许写入系统配置表");
        }
        config.setConfigValue(request.configValue());
        config.setValueType(valueType);
        config.setDescription(request.description());
        config.setUpdatedBy(operatorId);
        config.setVersion(request.version());
        if (mapper.updateConfig(config) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
        return vo(mapper.selectConfig(key));
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new BizException(ResultCode.BAD_REQUEST, "配置键不能为空且不能超过 128 字符");
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        if (SENSITIVE_WORDS.stream().anyMatch(normalized::contains)) {
            throw new BizException(ResultCode.FORBIDDEN, "敏感配置键禁止写入系统配置表");
        }
    }

    private SystemVos.ConfigVO vo(SystemConfig config) {
        return new SystemVos.ConfigVO(Long.toString(config.getId()), config.getConfigKey(),
                config.getConfigValue(), config.getValueType(), config.getDescription(), config.getStatus(),
                config.getVersion(), TimeUtils.offset(config.getUpdatedAt()));
    }
}
