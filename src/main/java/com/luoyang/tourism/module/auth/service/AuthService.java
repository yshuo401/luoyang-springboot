package com.luoyang.tourism.module.auth.service;

import com.luoyang.tourism.module.auth.pojo.dto.AuthDtos;
import com.luoyang.tourism.module.auth.pojo.vo.AuthVos;

/**
 * 认证业务接口。
 */
public interface AuthService {
    AuthVos.AppLoginVO wechatLogin(AuthDtos.WechatLoginRequest request, String ip);

    AuthVos.AppUserVO appProfile(Long userId);

    AuthVos.AdminLoginVO adminLogin(AuthDtos.AdminLoginRequest request, String ip);

    AuthVos.AdminProfileVO adminProfile(Long adminId);

    void changeAdminPassword(Long adminId, AuthDtos.ChangePasswordRequest request);
}