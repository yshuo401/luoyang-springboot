package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.auth.entity.AdminRole;
import com.luoyang.tourism.module.auth.entity.AdminUser;
import com.luoyang.tourism.module.auth.mapper.AuthMapper;
import com.luoyang.tourism.module.file.service.FileUrlService;
import com.luoyang.tourism.module.system.dto.SystemDtos;
import com.luoyang.tourism.module.system.mapper.SystemMapper;
import com.luoyang.tourism.module.system.vo.SystemVos;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSystemService {
    private final SystemMapper mapper;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileUrlService fileUrlService;

    public PageResult<SystemVos.AdminUserVO> list(String keyword, Integer status,
                                                  Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<SystemVos.AdminUserVO> records = mapper.selectAdmins(keyword, status,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(this::vo).toList();
        return Paging.of(records, mapper.countAdmins(keyword, status), safePage, safeSize);
    }

    public SystemVos.AdminUserVO detail(Long id) {
        return vo(required(id));
    }

    @Transactional
    public SystemVos.AdminUserVO create(SystemDtos.AdminCreateRequest request, Long operatorId) {
        String username = request.username().trim();
        if (authMapper.countAdminByUsername(username) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "管理员用户名已存在");
        }
        List<AdminRole> roles = validateRoles(request.roleIds());
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setNickname(request.nickname().trim());
        admin.setCreatedBy(operatorId);
        admin.setUpdatedBy(operatorId);
        authMapper.insertAdminUser(admin);
        for (AdminRole role : roles) {
            authMapper.insertAdminUserRole(admin.getId(), role.getId(), operatorId);
        }
        return vo(required(admin.getId()));
    }

    @Transactional
    public SystemVos.AdminUserVO update(Long id, SystemDtos.AdminUpdateRequest request, Long operatorId) {
        AdminUser admin = required(id);
        List<AdminRole> roles = validateRoles(request.roleIds());
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
        mapper.deleteAdminRoles(id);
        for (AdminRole role : roles) {
            authMapper.insertAdminUserRole(id, role.getId(), operatorId);
        }
        authMapper.incrementAdminTokenVersion(id);
        return vo(required(id));
    }

    public SystemVos.AdminUserVO updateStatus(Long id, SystemDtos.AdminStatusRequest request, Long operatorId) {
        AdminUser admin = required(id);
        if (request.status() == 0) {
            if (id.equals(operatorId)) {
                throw new BizException(ResultCode.BAD_REQUEST, "不能禁用当前登录管理员");
            }
            if (mapper.countAdminsWithPermissionExcluding("system:admin:update", id) == 0) {
                throw new BizException(ResultCode.UNIQUE_CONFLICT, "必须保留至少一个可管理管理员账号的账号");
            }
        }
        if (mapper.updateAdminStatus(id, request.status(), request.version(), operatorId) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
        return vo(required(id));
    }

    public void resetPassword(Long id, SystemDtos.AdminResetPasswordRequest request) {
        AdminUser admin = required(id);
        int nextVersion = admin.getTokenVersion() + 1;
        mapper.resetAdminPassword(id, passwordEncoder.encode(request.newPassword()), nextVersion);
    }

    public List<SystemVos.RoleBriefVO> rolesOf(Long adminId) {
        return authMapper.selectRolesByAdminId(adminId).stream()
                .map(role -> new SystemVos.RoleBriefVO(Long.toString(role.getId()), role.getName(), role.getCode()))
                .toList();
    }

    private List<AdminRole> validateRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "至少选择一个角色");
        }
        List<AdminRole> roles = roleIds.stream().distinct().map(mapper::selectRole).toList();
        if (roles.size() != roleIds.stream().distinct().count()
                || roles.stream().anyMatch(role -> role == null || !Integer.valueOf(1).equals(role.getStatus()))) {
            throw new BizException(ResultCode.BAD_REQUEST, "角色不存在或未启用");
        }
        return roles;
    }

    private AdminUser required(Long id) {
        AdminUser admin = authMapper.selectAdminById(id);
        if (admin == null) {
            throw BizException.notFound();
        }
        return admin;
    }

    private SystemVos.AdminUserVO vo(AdminUser admin) {
        return new SystemVos.AdminUserVO(Long.toString(admin.getId()), admin.getUsername(), admin.getNickname(),
                fileUrlService.signedUrl(admin.getAvatarFileId()), admin.getEmail(), admin.getPhone(),
                admin.getStatus(), rolesOf(admin.getId()), admin.getVersion(),
                TimeUtils.offset(admin.getLastLoginAt()), TimeUtils.offset(admin.getCreatedAt()));
    }
}
