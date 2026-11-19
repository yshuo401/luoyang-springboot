package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import java.util.List;

/**
 * RbacService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface RbacService {
    /** 查询角色列表。 */
    public List<SystemVos.RoleVO> roles(String keyword, Integer status);

    /** 执行 createRole 业务操作。 */
    public SystemVos.RoleVO createRole(SystemDtos.RoleUpsertRequest request, Long operatorId);

    /** 执行 updateRole 业务操作。 */
    public SystemVos.RoleVO updateRole(Long id, SystemDtos.RoleUpsertRequest request, Long operatorId);

    /** 执行 deleteRole 业务操作。 */
    public void deleteRole(Long id, Integer version, Long operatorId);

    /** 执行 replacePermissions 业务操作。 */
    public void replacePermissions(Long id, SystemDtos.RolePermissionRequest request, Long operatorId);

    /** 执行 permissionTree 业务操作。 */
    public List<SystemVos.PermissionVO> permissionTree();
}
