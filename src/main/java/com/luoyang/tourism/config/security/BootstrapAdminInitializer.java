package com.luoyang.tourism.config.security;

import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.module.auth.mapper.AuthMapper;
import com.luoyang.tourism.module.auth.pojo.entity.AdminRole;
import com.luoyang.tourism.module.auth.pojo.entity.AdminUser;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * BootstrapAdminInitializer 配置组件。
 * <p>负责基础设施、安全或 Web 相关配置。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminInitializer implements ApplicationRunner {
    private final AppProperties properties;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.BootstrapAdmin config = properties.getBootstrapAdmin();
        if (!config.isEnabled()) {
            return;
        }
        if (config.getPassword() == null || config.getPassword().length() < 8) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must contain at least 8 characters");
        }
        if (authMapper.countAdminByUsername(config.getUsername()) > 0) {
            return;
        }
        AdminRole role = authMapper.selectRoleByCode("SUPER_ADMIN");
        if (role == null) {
            throw new IllegalStateException("SUPER_ADMIN role is missing; check Flyway seed data");
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(config.getUsername().trim());
        admin.setPasswordHash(passwordEncoder.encode(config.getPassword()));
        admin.setNickname(config.getNickname());
        authMapper.insertAdminUser(admin);
        authMapper.insertAdminUserRole(admin.getId(), role.getId(), null);
        log.info("bootstrap administrator created username={}", admin.getUsername());
    }
}
