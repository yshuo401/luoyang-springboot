package com.luoyang.tourism.config.security;

import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.module.auth.entity.AdminRole;
import com.luoyang.tourism.module.auth.entity.AdminUser;
import com.luoyang.tourism.module.auth.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
