package com.luoyang.tourism.module.auth.mapper;

import com.luoyang.tourism.module.auth.entity.AdminPermission;
import com.luoyang.tourism.module.auth.entity.AdminRole;
import com.luoyang.tourism.module.auth.entity.AdminUser;
import com.luoyang.tourism.module.auth.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AuthMapper {
    AppUser selectAppUserByOpenid(@Param("openid") String openid);
    AppUser selectAppUserById(@Param("id") Long id);
    int insertAppUser(AppUser user);
    int updateAppLogin(@Param("id") Long id, @Param("ip") String ip, @Param("now") LocalDateTime now);
    int updateAppProfile(AppUser user);
    int incrementAppTokenVersion(@Param("id") Long id);
    int updateAppUserStatus(@Param("id") Long id, @Param("status") Integer status);

    AdminUser selectAdminByUsername(@Param("username") String username);
    AdminUser selectAdminById(@Param("id") Long id);
    List<AdminRole> selectRolesByAdminId(@Param("adminId") Long adminId);
    List<AdminPermission> selectPermissionsByAdminId(@Param("adminId") Long adminId);
    AdminRole selectRoleByCode(@Param("code") String code);
    int insertAdminUser(AdminUser admin);
    int insertAdminUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId,
                            @Param("createdBy") Long createdBy);
    int updateAdminLoginSuccess(@Param("id") Long id, @Param("ip") String ip, @Param("now") LocalDateTime now);
    int updateAdminLoginFailure(@Param("id") Long id, @Param("failedCount") Integer failedCount,
                                @Param("lockedUntil") LocalDateTime lockedUntil);
    int updateAdminPassword(@Param("id") Long id, @Param("passwordHash") String passwordHash,
                            @Param("tokenVersion") Integer tokenVersion, @Param("now") LocalDateTime now);
    int incrementAdminTokenVersion(@Param("id") Long id);
    int countAdminByUsername(@Param("username") String username);
}
