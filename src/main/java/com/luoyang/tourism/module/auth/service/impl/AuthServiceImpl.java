package com.luoyang.tourism.module.auth.service.impl;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.integration.wechat.WechatClient;
import com.luoyang.tourism.module.auth.mapper.AuthMapper;
import com.luoyang.tourism.module.auth.pojo.dto.AuthDtos;
import com.luoyang.tourism.module.auth.pojo.entity.AdminPermission;
import com.luoyang.tourism.module.auth.pojo.entity.AdminRole;
import com.luoyang.tourism.module.auth.pojo.entity.AdminUser;
import com.luoyang.tourism.module.auth.pojo.entity.AppUser;
import com.luoyang.tourism.module.auth.pojo.vo.AuthVos;
import com.luoyang.tourism.module.auth.service.AuthService;
import com.luoyang.tourism.module.auth.service.RefreshTokenStore;
import com.luoyang.tourism.module.file.service.FileUrlService;
import com.luoyang.tourism.security.jwt.JwtService;
import com.luoyang.tourism.security.jwt.TokenAudience;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final int MAX_FAILED_LOGIN = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final AuthMapper mapper;
    private final WechatClient wechatClient;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    private final PasswordEncoder passwordEncoder;
    private final FileUrlService fileUrlService;
    private final AppProperties properties;

    @Transactional
@Override
public AppLoginResult wechatLogin(AuthDtos.WechatLoginRequest request, String ip) {
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
        IssuedTokens<AuthVos.AppLoginVO> tokens = issueAppTokens(user);
        return new AppLoginResult(tokens.body(), tokens.refreshToken());
    }

@Override
public AppLoginResult refreshApp(AuthDtos.RefreshRequest request) {
        Jwt jwt = jwtService.decodeRefreshToken(request.refreshToken(), TokenAudience.APP_USER);
        consume(TokenAudience.APP_USER, jwt);
        AppUser user = mapper.selectAppUserById(Long.parseLong(jwt.getSubject()));
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())
                || user.getTokenVersion() != claimTokenVersion(jwt)) {
            throw new BizException(ResultCode.REFRESH_INVALID);
        }
        IssuedTokens<AuthVos.AppLoginVO> tokens = issueAppTokens(user);
        return new AppLoginResult(tokens.body(), tokens.refreshToken());
    }

@Override
public AuthVos.AppUserVO appProfile(Long userId) {
        AppUser user = requiredAppUser(userId);
        return appUserVo(user);
    }

    @Transactional
@Override
public AdminLoginResult adminLogin(AuthDtos.AdminLoginRequest request, String ip) {
        String username = request.username().trim();
        // 管理员登录统一处理“不存在、锁定、禁用、密码错误”，避免泄露账号状态。
        AdminUser admin = mapper.selectAdminByUsername(username);
        if (admin == null) {
            // 账号不存在时也执行一次 BCrypt 校验，缩小响应时序差异。
            passwordEncoder.matches(request.password(), passwordEncoder.encode("not-a-real-password"));
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(ResultCode.ADMIN_DISABLED, "账号已锁定，请稍后再试");
        }
        if (!Integer.valueOf(1).equals(admin.getStatus())) {
            throw new BizException(ResultCode.ADMIN_DISABLED);
        }
        // 连续失败达到阈值后执行短时锁定，降低暴力破解风险。
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            int failures = (admin.getFailedLoginCount() == null ? 0 : admin.getFailedLoginCount()) + 1;
            LocalDateTime lockedUntil = failures >= MAX_FAILED_LOGIN ? LocalDateTime.now().plus(LOCK_DURATION) : null;
            mapper.updateAdminLoginFailure(admin.getId(), failures, lockedUntil);
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        mapper.updateAdminLoginSuccess(admin.getId(), ip, LocalDateTime.now());
        admin = requiredAdmin(admin.getId());
        IssuedTokens<AuthVos.AdminLoginVO> tokens = issueAdminTokens(admin);
        return new AdminLoginResult(tokens.body(), tokens.refreshToken());
    }

@Override
public AdminLoginResult refreshAdmin(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BizException(ResultCode.REFRESH_INVALID);
        }
        Jwt jwt = jwtService.decodeRefreshToken(refreshToken, TokenAudience.ADMIN);
        consume(TokenAudience.ADMIN, jwt);
        AdminUser admin = mapper.selectAdminById(Long.parseLong(jwt.getSubject()));
        if (admin == null || !Integer.valueOf(1).equals(admin.getStatus())
                || admin.getTokenVersion() != claimTokenVersion(jwt)) {
            throw new BizException(ResultCode.REFRESH_INVALID);
        }
        IssuedTokens<AuthVos.AdminLoginVO> tokens = issueAdminTokens(admin);
        return new AdminLoginResult(tokens.body(), tokens.refreshToken());
    }

@Override
public void appLogout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            Jwt jwt = jwtService.decodeRefreshToken(refreshToken, TokenAudience.APP_USER);
            refreshTokenStore.delete(TokenAudience.APP_USER, jwt.getId());
        } catch (RuntimeException ignored) {
            // Logout remains idempotent.
        }
    }

@Override
public void adminLogout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            Jwt jwt = jwtService.decodeRefreshToken(refreshToken, TokenAudience.ADMIN);
            refreshTokenStore.delete(TokenAudience.ADMIN, jwt.getId());
        } catch (RuntimeException ignored) {
            // Logout must remain idempotent and must not expose token state.
        }
    }

@Override
public AuthVos.AdminProfileVO adminProfile(Long adminId) {
        return adminProfile(requiredAdmin(adminId));
    }

    @Transactional
@Override
public void changeAdminPassword(Long adminId, AuthDtos.ChangePasswordRequest request) {
        AdminUser admin = requiredAdmin(adminId);
        if (!passwordEncoder.matches(request.oldPassword(), admin.getPasswordHash())) {
            throw new BizException(ResultCode.BAD_REQUEST, "原密码不正确");
        }
        if (passwordEncoder.matches(request.newPassword(), admin.getPasswordHash())) {
            throw new BizException(ResultCode.BAD_REQUEST, "新密码不能与原密码相同");
        }
        int nextVersion = admin.getTokenVersion() + 1;
        mapper.updateAdminPassword(adminId, passwordEncoder.encode(request.newPassword()), nextVersion, LocalDateTime.now());
    }

    private IssuedTokens<AuthVos.AppLoginVO> issueAppTokens(AppUser user) {
        Set<String> roles = Set.of("APP_USER");
        String access = jwtService.issueAccessToken(TokenAudience.APP_USER, user.getId(), user.getNickname(),
                user.getTokenVersion(), roles, Set.of());
        String refresh = jwtService.issueRefreshToken(TokenAudience.APP_USER, user.getId(), user.getNickname(),
                user.getTokenVersion());
        Jwt refreshJwt = jwtService.decodeRefreshToken(refresh, TokenAudience.APP_USER);
        refreshTokenStore.store(TokenAudience.APP_USER, refreshJwt.getId(), user.getId(), user.getTokenVersion(),
                properties.getJwt().getAppRefreshTtl());
        AuthVos.AppLoginVO body = new AuthVos.AppLoginVO(access, refresh,
                properties.getJwt().getAppAccessTtl().toSeconds(), appUserVo(user));
        return new IssuedTokens<>(body, refresh);
    }

    private IssuedTokens<AuthVos.AdminLoginVO> issueAdminTokens(AdminUser admin) {
        // ADMIN 是路由层通用角色；SUPER_ADMIN 等业务角色继续保留在 JWT 中供 RBAC 使用。
        Set<String> roles = new java.util.LinkedHashSet<>();
        roles.add("ADMIN");
        roles.addAll(roleCodes(admin.getId()));
        Set<String> permissions = permissionCodes(admin.getId());
        String access = jwtService.issueAccessToken(TokenAudience.ADMIN, admin.getId(), admin.getUsername(),
                admin.getTokenVersion(), roles, permissions);
        String refresh = jwtService.issueRefreshToken(TokenAudience.ADMIN, admin.getId(), admin.getUsername(),
                admin.getTokenVersion());
        Jwt refreshJwt = jwtService.decodeRefreshToken(refresh, TokenAudience.ADMIN);
        refreshTokenStore.store(TokenAudience.ADMIN, refreshJwt.getId(), admin.getId(), admin.getTokenVersion(),
                properties.getJwt().getAdminRefreshTtl());
        AuthVos.AdminLoginVO body = new AuthVos.AdminLoginVO(access,
                properties.getJwt().getAdminAccessTtl().toSeconds(), adminProfile(admin));
        return new IssuedTokens<>(body, refresh);
    }

    /**
     * 消费 Refresh Token，并校验 Redis 状态、用户 ID 和 token_version。
     * 同一个 jti 只能成功消费一次，从而实现刷新令牌轮换。
     */
    private void consume(TokenAudience audience, Jwt jwt) {
        RefreshTokenStore.ConsumedToken consumed = refreshTokenStore.consume(audience, jwt.getId());
        if (consumed == null || consumed.userId() != Long.parseLong(jwt.getSubject())
                || consumed.tokenVersion() != claimTokenVersion(jwt)) {
            throw new BizException(ResultCode.REFRESH_INVALID);
        }
    }

    private int claimTokenVersion(Jwt jwt) {
        Long value = jwt.getClaim("token_version");
        return value == null ? -1 : value.intValue();
    }

    private Set<String> roleCodes(Long adminId) {
        return mapper.selectRolesByAdminId(adminId).stream().map(AdminRole::getCode)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> permissionCodes(Long adminId) {
        return mapper.selectPermissionsByAdminId(adminId).stream().map(AdminPermission::getPermissionCode)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private AuthVos.AdminProfileVO adminProfile(AdminUser admin) {
        List<String> roles = mapper.selectRolesByAdminId(admin.getId()).stream().map(AdminRole::getCode).toList();
        List<String> permissions = mapper.selectPermissionsByAdminId(admin.getId()).stream()
                .map(AdminPermission::getPermissionCode).toList();
        return new AuthVos.AdminProfileVO(Long.toString(admin.getId()), admin.getUsername(), admin.getNickname(),
                fileUrlService.signedUrl(admin.getAvatarFileId()), roles, permissions);
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

    private record IssuedTokens<T>(T body, String refreshToken) {
    }

}
