package com.luoyang.tourism.module.auth.mapper;

import com.luoyang.tourism.module.auth.pojo.entity.AdminUser;
import com.luoyang.tourism.module.auth.pojo.entity.AppUser;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录和用户资料数据访问。
 */
@Mapper
public interface AuthMapper {
    AppUser selectAppUserByOpenid(@Param("openid") String openid);
    AppUser selectAppUserById(@Param("id") Long id);
    int insertAppUser(AppUser user);
    int updateAppLogin(@Param("id") Long id, @Param("ip") String ip, @Param("now") LocalDateTime now);
    int updateAppProfile(AppUser user);
    int updateAppUserStatus(@Param("id") Long id, @Param("status") Integer status);

    AdminUser selectAdminByUsername(@Param("username") String username);
    AdminUser selectAdminById(@Param("id") Long id);
    int insertAdminUser(AdminUser admin);
    int updateAdminLoginSuccess(@Param("id") Long id, @Param("ip") String ip, @Param("now") LocalDateTime now);
    int updateAdminLoginFailure(@Param("id") Long id, @Param("failedCount") Integer failedCount,
                                @Param("lockedUntil") LocalDateTime lockedUntil);
    int updateAdminPassword(@Param("id") Long id, @Param("passwordHash") String passwordHash,
                            @Param("tokenVersion") Integer tokenVersion, @Param("now") LocalDateTime now);
    int countAdminByUsername(@Param("username") String username);
}