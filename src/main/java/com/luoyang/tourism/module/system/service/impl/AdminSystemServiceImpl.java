package com.luoyang.tourism.module.system.service.impl;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.auth.mapper.AuthMapper;
import com.luoyang.tourism.module.auth.pojo.entity.AdminUser;
import com.luoyang.tourism.module.file.service.FileUrlService;
import com.luoyang.tourism.module.system.mapper.SystemMapper;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import com.luoyang.tourism.module.system.service.AdminSystemService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员账号业务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminSystemServiceImpl implements AdminSystemService {
    private static final SystemVos.RoleBriefVO ADMIN_ROLE =
            new SystemVos.RoleBriefVO("1", "管理员", "ADMIN");

    private final SystemMapper mapper;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileUrlService fileUrlService;

    @Override
    public PageResult<SystemVos.AdminUserVO> list(String keyword, Integer status,
                                                  Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<SystemVos.AdminUserVO> records = mapper.selectAdmins(keyword, status,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(this::vo).toList();
        return Paging.of(records, mapper.countAdmins(keyword, status), safePage, safeSize);
    }

    @Override
    public SystemVos.AdminUserVO detail(Long id) {
        return vo(required(id));
    }

    @Override
    @Transactional
    public SystemVos.AdminUserVO create(SystemDtos.AdminCreateRequest request, Long operatorId) {
        String username = request.username().trim();
        if (authMapper.countAdminByUsername(username) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "管理员用户名已存在");
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setNickname(request.nickname().trim());
        admin.setCreatedBy(operatorId);
        admin.setUpdatedBy(operatorId);
        authMapper.insertAdminUser(admin);
        return vo(required(admin.getId()));
    }

    @Override
    @Transactional
    public SystemVos.AdminUserVO update(Long id, SystemDtos.AdminUpdateRequest request, Long operatorId) {
        AdminUser admin = required(id);
        if (request.avatarFileId() != null) {
            fileUrlService.requireUsable(request.avatarFileId());
            admin.setAvatarFileId(request.avatarFileId());
        }
        admin.setNickname(request.nickname().trim());
        admin.setEmail(request.email());
        admin.setPhone(request.phone());
        admin.setUpdatedBy(operatorId);
        admin.setVersion(request.version());
        if (mapper.updateAdminProfile(admin) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
        return vo(required(id));
    }

    @Override
    public SystemVos.AdminUserVO updateStatus(Long id, SystemDtos.AdminStatusRequest request, Long operatorId) {
        if (request.status() == 0 && id.equals(operatorId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "不能禁用当前登录管理员");
        }
        if (mapper.updateAdminStatus(id, request.status(), request.version(), operatorId) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
        return vo(required(id));
    }

    @Override
    public void resetPassword(Long id, SystemDtos.AdminResetPasswordRequest request) {
        AdminUser admin = required(id);
        mapper.resetAdminPassword(id, passwordEncoder.encode(request.newPassword()),
                admin.getTokenVersion() + 1);
    }

    private AdminUser required(Long id) {
        AdminUser admin = authMapper.selectAdminById(id);
        if (admin == null) {
            throw BizException.notFound();
        }
        return admin;
    }

    private SystemVos.AdminUserVO vo(AdminUser admin) {
        return new SystemVos.AdminUserVO(Long.toString(admin.getId()), admin.getUsername(),
                admin.getNickname(), fileUrlService.signedUrl(admin.getAvatarFileId()),
                admin.getEmail(), admin.getPhone(), admin.getStatus(), List.of(ADMIN_ROLE),
                admin.getVersion(), TimeUtils.offset(admin.getLastLoginAt()),
                TimeUtils.offset(admin.getCreatedAt()));
    }
}