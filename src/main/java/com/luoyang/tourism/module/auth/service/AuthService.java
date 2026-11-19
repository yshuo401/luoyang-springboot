package com.luoyang.tourism.module.auth.service;

import com.luoyang.tourism.module.auth.pojo.dto.AuthDtos;
import com.luoyang.tourism.module.auth.pojo.vo.AuthVos;

/**
 * AuthService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface AuthService {
    /** 执行 wechatLogin 业务操作。 */
    public AppLoginResult wechatLogin(AuthDtos.WechatLoginRequest request, String ip);

    /** 执行 refreshApp 业务操作。 */
    public AppLoginResult refreshApp(AuthDtos.RefreshRequest request);

    /** 执行 appProfile 业务操作。 */
    public AuthVos.AppUserVO appProfile(Long userId);

    /** 执行 adminLogin 业务操作。 */
    public AdminLoginResult adminLogin(AuthDtos.AdminLoginRequest request, String ip);

    /** 执行 refreshAdmin 业务操作。 */
    public AdminLoginResult refreshAdmin(String refreshToken);

    /** 执行 appLogout 业务操作。 */
    public void appLogout(String refreshToken);

    /** 执行 adminLogout 业务操作。 */
    public void adminLogout(String refreshToken);

    /** 执行 adminProfile 业务操作。 */
    public AuthVos.AdminProfileVO adminProfile(Long adminId);

    /** 执行 changeAdminPassword 业务操作。 */
    public void changeAdminPassword(Long adminId, AuthDtos.ChangePasswordRequest request);

    public record AppLoginResult(AuthVos.AppLoginVO body, String refreshToken) {
    }

    public record AdminLoginResult(AuthVos.AdminLoginVO body, String refreshToken) {
    }
}
