package com.luoyang.tourism.module.auth.service.impl;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.integration.wechat.WechatClient;
import com.luoyang.tourism.module.auth.mapper.AuthMapper;
import com.luoyang.tourism.module.auth.pojo.dto.AuthDtos;
import com.luoyang.tourism.module.auth.pojo.entity.AdminUser;
import com.luoyang.tourism.module.auth.pojo.entity.AppUser;
import com.luoyang.tourism.module.auth.pojo.vo.AuthVos;
import com.luoyang.tourism.module.auth.service.AuthService;
import com.luoyang.tourism.module.file.service.FileUrlService;
import com.luoyang.tourism.security.jwt.JwtService;
import com.luoyang.tourism.security.jwt.TokenAudience;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证业务实现。
 * <p>简化后只签发 Access Token；管理员固定为 ADMIN 角色。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final int MAX_FAILED_LOGIN = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final AuthMapper mapper;
    private final WechatClient wechatClient;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final FileUrlService fileUrlService;

    @Override
    @Transactional
    public AuthVos.AppLoginVO wechatLogin(AuthDtos.WechatLoginRequest request, String ip) {
        WechatClient.WechatSession session = wechatClient.login(request.code());
        AppUser user = mapper.selectAppUserByOpenid(session.openid());
        if (user == null) {
            user = new AppUser();
            user.setOpenid(session.openid());
            user.setUnionid(session.unionid());
            user.setNickname("洛阳游客");
            user.setGender(0);
            user.setCity("洛阳");
            mapper.insertAppUser(user);
            user = mapper.selectAppUserById(user.getId());
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        mapper.updateAppLogin(user.getId(), ip, LocalDateTime.now());
        String accessToken = jwtService.issueAccessToken(TokenAudience.APP_USER, user.getId(),
                user.getNickname(), Set.of("APP_USER"), Set.of());
        return new AuthVos.AppLoginVO(accessToken,
                jwtService.accessTtlSeconds(TokenAudience.APP_USER), appUserVo(user));
    }

    @Override
    public AuthVos.AppUserVO appProfile(Long userId) {
        return appUserVo(requiredAppUser(userId));
    }

    @Override
    @Transactional
    public AuthVos.AdminLoginVO adminLogin(AuthDtos.AdminLoginRequest request, String ip) {
        AdminUser admin = mapper.selectAdminByUsername(request.username().trim());
        if (admin == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(ResultCode.ADMIN_DISABLED, "账号已锁定，请稍后再试");
        }
        if (!Integer.valueOf(1).equals(admin.getStatus())) {
            throw new BizException(ResultCode.ADMIN_DISABLED);
        }
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            int failures = (admin.getFailedLoginCount() == null ? 0 : admin.getFailedLoginCount()) + 1;
            LocalDateTime lockedUntil = failures >= MAX_FAILED_LOGIN
                    ? LocalDateTime.now().plus(LOCK_DURATION) : null;
            mapper.updateAdminLoginFailure(admin.getId(), failures, lockedUntil);
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        mapper.updateAdminLoginSuccess(admin.getId(), ip, LocalDateTime.now());
        admin = requiredAdmin(admin.getId());
        String accessToken = jwtService.issueAccessToken(TokenAudience.ADMIN, admin.getId(),
                admin.getUsername(), Set.of("ADMIN"), Set.of());
        return new AuthVos.AdminLoginVO(accessToken,
                jwtService.accessTtlSeconds(TokenAudience.ADMIN), adminProfile(admin));
    }

    @Override
    public AuthVos.AdminProfileVO adminProfile(Long adminId) {
        return adminProfile(requiredAdmin(adminId));
    }

    @Override
    @Transactional
    public void changeAdminPassword(Long adminId, AuthDtos.ChangePasswordRequest request) {
        AdminUser admin = requiredAdmin(adminId);
        if (!passwordEncoder.matches(request.oldPassword(), admin.getPasswordHash())) {
            throw new BizException(ResultCode.BAD_REQUEST, "原密码不正确");
        }
        if (passwordEncoder.matches(request.newPassword(), admin.getPasswordHash())) {
            throw new BizException(ResultCode.BAD_REQUEST, "新密码不能与原密码相同");
        }
        mapper.updateAdminPassword(adminId, passwordEncoder.encode(request.newPassword()),
                admin.getTokenVersion(), LocalDateTime.now());
    }

    private AuthVos.AdminProfileVO adminProfile(AdminUser admin) {
        return new AuthVos.AdminProfileVO(Long.toString(admin.getId()), admin.getUsername(),
                admin.getNickname(), fileUrlService.signedUrl(admin.getAvatarFileId()),
                List.of("ADMIN"), List.of());
    }

    private AuthVos.AppUserVO appUserVo(AppUser user) {
        return new AuthVos.AppUserVO(Long.toString(user.getId()), user.getNickname(),
                fileUrlService.signedUrl(user.getAvatarFileId()), user.getGender(), user.getCity(),
                TimeUtils.offset(user.getCreatedAt()));
    }

    private AppUser requiredAppUser(Long id) {
        AppUser user = mapper.selectAppUserById(id);
        if (user == null) {
            throw BizException.notFound();
        }
        return user;
    }

    private AdminUser requiredAdmin(Long id) {
        AdminUser admin = mapper.selectAdminById(id);
        if (admin == null) {
            throw new BizException(ResultCode.ADMIN_DISABLED);
        }
        return admin;
    }
}