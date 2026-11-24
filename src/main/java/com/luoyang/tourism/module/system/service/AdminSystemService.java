package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.system.pojo.dto.SystemDtos;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;

/**
 * 管理员账号业务接口。
 */
public interface AdminSystemService {
    PageResult<SystemVos.AdminUserVO> list(String keyword, Integer status, Integer page, Integer pageSize);

    SystemVos.AdminUserVO detail(Long id);

    SystemVos.AdminUserVO create(SystemDtos.AdminCreateRequest request, Long operatorId);

    SystemVos.AdminUserVO update(Long id, SystemDtos.AdminUpdateRequest request, Long operatorId);

    SystemVos.AdminUserVO updateStatus(Long id, SystemDtos.AdminStatusRequest request, Long operatorId);

    void resetPassword(Long id, SystemDtos.AdminResetPasswordRequest request);
}