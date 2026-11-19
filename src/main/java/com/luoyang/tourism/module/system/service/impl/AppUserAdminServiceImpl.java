package com.luoyang.tourism.module.system.service.impl;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.auth.mapper.AuthMapper;
import com.luoyang.tourism.module.auth.pojo.entity.AppUser;
import com.luoyang.tourism.module.file.service.FileUrlService;
import com.luoyang.tourism.module.system.mapper.SystemMapper;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import com.luoyang.tourism.module.system.service.AppUserAdminService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AppUserAdminServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
@RequiredArgsConstructor
public class AppUserAdminServiceImpl implements AppUserAdminService {
    private final SystemMapper mapper;
    private final AuthMapper authMapper;
    private final FileUrlService fileUrlService;

@Override
public PageResult<SystemVos.AppUserAdminVO> list(String keyword, Integer status,
                                                      LocalDateTime createdFrom, LocalDateTime createdTo,
                                                      Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<SystemVos.AppUserAdminVO> records = mapper.selectAppUsers(keyword, status, createdFrom, createdTo,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(this::vo).toList();
        return Paging.of(records, mapper.countAppUsers(keyword, status, createdFrom, createdTo),
                safePage, safeSize);
    }

@Override
public SystemVos.AppUserAdminVO detail(Long id) {
        AppUser user = required(id);
        return vo(user);
    }

@Override
public SystemVos.AppUserAdminVO updateStatus(Long id, SystemDtos.AppUserStatusRequest request) {
        required(id);
        if (request.status() != 0 && request.status() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户状态值不合法");
        }
        authMapper.updateAppUserStatus(id, request.status());
        return vo(required(id));
    }

    private AppUser required(Long id) {
        AppUser user = authMapper.selectAppUserById(id);
        if (user == null) {
            throw BizException.notFound();
        }
        return user;
    }

    private SystemVos.AppUserAdminVO vo(AppUser user) {
        return new SystemVos.AppUserAdminVO(Long.toString(user.getId()), user.getNickname(),
                fileUrlService.signedUrl(user.getAvatarFileId()), user.getGender(), user.getCity(),
                user.getStatus(), maskOpenid(user.getOpenid()), TimeUtils.offset(user.getLastLoginAt()),
                TimeUtils.offset(user.getCreatedAt()));
    }

    private String maskOpenid(String openid) {
        if (openid == null || openid.length() < 8) {
            return "****";
        }
        return openid.substring(0, 4) + "****" + openid.substring(openid.length() - 4);
    }
}
