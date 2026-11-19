package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.auth.entity.AdminPermission;
import com.luoyang.tourism.module.auth.entity.AdminRole;
import com.luoyang.tourism.module.system.dto.SystemDtos;
import com.luoyang.tourism.module.system.mapper.SystemMapper;
import com.luoyang.tourism.module.system.vo.SystemVos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RbacService {
    private final SystemMapper mapper;

    public List<SystemVos.RoleVO> roles(String keyword, Integer status) {
        return mapper.selectRoles(keyword, status).stream().map(this::roleVo).toList();
    }

    @Transactional
    public SystemVos.RoleVO createRole(SystemDtos.RoleUpsertRequest request, Long operatorId) {
        if (mapper.countRoleCode(request.code(), null) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "角色编码已存在");
        }
        AdminRole role = new AdminRole();
        role.setName(request.name().trim());
        role.setCode(request.code().trim());
        role.setDescription(request.description());
        role.setStatus(request.status());
        role.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        role.setCreatedBy(operatorId);
        role.setUpdatedBy(operatorId);
        mapper.insertRole(role);
        return roleVo(requiredRole(role.getId()));
    }

    @Transactional
    public SystemVos.RoleVO updateRole(Long id, SystemDtos.RoleUpsertRequest request, Long operatorId) {
        AdminRole role = requiredRole(id);
        if (role.getCode().equals("SUPER_ADMIN") && !"SUPER_ADMIN".equalsIgnoreCase(request.code())) {
            throw new BizException(ResultCode.BAD_REQUEST, "超级管理员角色编码不可修改");
        }
        if (mapper.countRoleCode(request.code(), id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "角色编码已存在");
        }
        if (request.version() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "缺少数据版本号");
        }
        role.setName(request.name().trim());
        role.setCode(request.code().trim());
        role.setDescription(request.description());
        role.setStatus(request.status());
        role.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        role.setUpdatedBy(operatorId);
        role.setVersion(request.version());
        if (mapper.updateRole(role) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
        mapper.incrementTokenVersionForRoleUsers(id);
        return roleVo(requiredRole(id));
    }

    @Transactional
    public void deleteRole(Long id, Integer version, Long operatorId) {
        AdminRole role = requiredRole(id);
        if ("SUPER_ADMIN".equals(role.getCode())) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "超级管理员角色不可删除");
        }
        if (mapper.countRoleUsage(id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "角色仍被管理员使用");
        }
        mapper.incrementTokenVersionForRoleUsers(id);
        if (mapper.softDeleteRole(id, version, operatorId) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
    }

    @Transactional
    public void replacePermissions(Long id, SystemDtos.RolePermissionRequest request, Long operatorId) {
        AdminRole role = requiredRole(id);
        if (request.version() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "缺少数据版本号");
        }
        List<Long> permissionIds = request.permissionIds() == null ? List.of() : request.permissionIds().stream().distinct().toList();
        if (!permissionIds.isEmpty() && mapper.countPermissionsByIds(permissionIds) != permissionIds.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "权限不存在或未启用");
        }
        role.setVersion(request.version());
        role.setUpdatedBy(operatorId);
        if (mapper.updateRole(role) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
        mapper.deleteRolePermissions(id);
        for (Long permissionId : permissionIds) {
            mapper.insertRolePermission(id, permissionId, operatorId);
        }
        mapper.incrementTokenVersionForRoleUsers(id);
    }

    public List<SystemVos.PermissionVO> permissionTree() {
        List<AdminPermission> permissions = mapper.selectPermissions();
        Map<Long, MutablePermission> nodes = new LinkedHashMap<>();
        for (AdminPermission permission : permissions) {
            nodes.put(permission.getId(), new MutablePermission(permission));
        }
        List<SystemVos.PermissionVO> roots = new ArrayList<>();
        for (MutablePermission node : nodes.values()) {
            MutablePermission parent = node.value.getParentId() == 0 ? null : nodes.get(node.value.getParentId());
            if (parent == null) {
                roots.add(toPermission(node));
            } else {
                parent.children.add(node);
            }
        }
        roots.sort(Comparator.comparing(permission -> permission.sortOrder() == null ? 0 : permission.sortOrder()));
        return roots;
    }

    private SystemVos.PermissionVO toPermission(MutablePermission node) {
        AdminPermission value = node.value;
        List<SystemVos.PermissionVO> children = node.children.stream()
                .sorted(Comparator.comparing(child -> child.value.getSortOrder() == null ? 0 : child.value.getSortOrder()))
                .map(this::toPermission).toList();
        return new SystemVos.PermissionVO(Long.toString(value.getId()), Long.toString(value.getParentId()),
                value.getPermissionName(), value.getPermissionCode(), value.getPermissionType(),
                value.getRoutePath(), value.getComponentPath(), value.getIcon(), value.getSortOrder(), children);
    }

    private AdminRole requiredRole(Long id) {
        AdminRole role = mapper.selectRole(id);
        if (role == null) {
            throw BizException.notFound();
        }
        return role;
    }

    private SystemVos.RoleVO roleVo(AdminRole role) {
        return new SystemVos.RoleVO(Long.toString(role.getId()), role.getName(), role.getCode(),
                role.getDescription(), role.getStatus(), role.getSortOrder(), role.getVersion(),
                TimeUtils.offset(role.getUpdatedAt()));
    }

    private static final class MutablePermission {
        private final AdminPermission value;
        private final List<MutablePermission> children = new ArrayList<>();

        private MutablePermission(AdminPermission value) {
            this.value = value;
        }
    }
}
