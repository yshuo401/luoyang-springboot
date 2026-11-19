package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import java.util.List;

/**
 * AdminSystemService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface AdminSystemService {
    /** 分页查询数据列表。 */
    public PageResult<SystemVos.AdminUserVO> list(String keyword, Integer status, Integer page, Integer pageSize);

    /** 查询详情。 */
    public SystemVos.AdminUserVO detail(Long id);

    /** 创建数据。 */
    public SystemVos.AdminUserVO create(SystemDtos.AdminCreateRequest request, Long operatorId);

    /** 更新数据。 */
    public SystemVos.AdminUserVO update(Long id, SystemDtos.AdminUpdateRequest request, Long operatorId);

    /** 执行 updateStatus 业务操作。 */
    public SystemVos.AdminUserVO updateStatus(Long id, SystemDtos.AdminStatusRequest request, Long operatorId);

    /** 执行 resetPassword 业务操作。 */
    public void resetPassword(Long id, SystemDtos.AdminResetPasswordRequest request);

    /** 执行 rolesOf 业务操作。 */
    public List<SystemVos.RoleBriefVO> rolesOf(Long adminId);
}
