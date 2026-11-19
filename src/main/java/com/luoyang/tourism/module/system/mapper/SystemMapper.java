package com.luoyang.tourism.module.system.mapper;

import com.luoyang.tourism.module.auth.pojo.entity.AdminPermission;
import com.luoyang.tourism.module.auth.pojo.entity.AdminRole;
import com.luoyang.tourism.module.auth.pojo.entity.AdminUser;
import com.luoyang.tourism.module.auth.pojo.entity.AppUser;
import com.luoyang.tourism.module.system.pojo.entity.AdminOperationLog;
import com.luoyang.tourism.module.system.pojo.entity.SystemConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * SystemMapper MyBatis 数据访问接口。
 * <p>SQL 只在本层维护，业务层不得拼接用户可控 SQL。</p>
 */
@Mapper
public interface SystemMapper {
    List<AppUser> selectAppUsers(@Param("keyword") String keyword, @Param("status") Integer status,
                                 @Param("createdFrom") LocalDateTime createdFrom,
                                 @Param("createdTo") LocalDateTime createdTo,
                                 @Param("offset") long offset, @Param("limit") int limit);
    long countAppUsers(@Param("keyword") String keyword, @Param("status") Integer status,
                       @Param("createdFrom") LocalDateTime createdFrom,
                       @Param("createdTo") LocalDateTime createdTo);

    List<AdminUser> selectAdmins(@Param("keyword") String keyword, @Param("status") Integer status,
                                 @Param("offset") long offset, @Param("limit") int limit);
    long countAdmins(@Param("keyword") String keyword, @Param("status") Integer status);
    List<Long> selectRoleIdsByAdminId(@Param("adminId") Long adminId);
    int updateAdminProfile(AdminUser admin);
    int updateAdminStatus(@Param("id") Long id, @Param("status") Integer status,
                          @Param("version") Integer version, @Param("updatedBy") Long updatedBy);
    int resetAdminPassword(@Param("id") Long id, @Param("passwordHash") String passwordHash,
                           @Param("newTokenVersion") Integer newTokenVersion);
    int deleteAdminRoles(@Param("adminId") Long adminId);
    int countAdminsWithPermissionExcluding(@Param("permissionCode") String permissionCode,
                                           @Param("excludeId") Long excludeId);

    List<AdminRole> selectRoles(@Param("keyword") String keyword, @Param("status") Integer status);
    AdminRole selectRole(@Param("id") Long id);
    int insertRole(AdminRole role);
    int updateRole(AdminRole role);
    int softDeleteRole(@Param("id") Long id, @Param("version") Integer version,
                       @Param("updatedBy") Long updatedBy);
    int countRoleUsage(@Param("roleId") Long roleId);
    int countRoleCode(@Param("code") String code, @Param("excludeId") Long excludeId);
    List<AdminPermission> selectPermissions();
    List<Long> selectPermissionIdsByRole(@Param("roleId") Long roleId);
    int deleteRolePermissions(@Param("roleId") Long roleId);
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId,
                             @Param("createdBy") Long createdBy);
    int countPermissionsByIds(@Param("ids") List<Long> ids);
    int incrementTokenVersionForRoleUsers(@Param("roleId") Long roleId);

    List<SystemConfig> selectConfigs(@Param("keyword") String keyword, @Param("offset") long offset,
                                     @Param("limit") int limit);
    long countConfigs(@Param("keyword") String keyword);
    SystemConfig selectConfig(@Param("key") String key);
    int insertConfig(SystemConfig config);
    int updateConfig(SystemConfig config);

    int insertOperationLog(AdminOperationLog log);
    List<AdminOperationLog> selectOperationLogs(@Param("adminId") Long adminId,
                                                @Param("module") String module,
                                                @Param("success") Integer success,
                                                @Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to,
                                                @Param("offset") long offset, @Param("limit") int limit);
    long countOperationLogs(@Param("adminId") Long adminId, @Param("module") String module,
                            @Param("success") Integer success,
                            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countSpotTotal();
    long countSpotPublished();
    long countUserTotal();
    long countFavoriteTotal();
    long countConversationTotal();
    long countMessagesToday();
    long countKnowledgeTotal();
    long countIndexFailed();
    List<Map<String, Object>> selectSevenDayTrend();
}
