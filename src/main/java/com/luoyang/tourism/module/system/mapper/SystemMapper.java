package com.luoyang.tourism.module.system.mapper;

import com.luoyang.tourism.module.auth.pojo.entity.AdminUser;
import com.luoyang.tourism.module.auth.pojo.entity.AppUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 后台用户、管理员和仪表盘数据访问。
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
    int updateAdminProfile(AdminUser admin);
    int updateAdminStatus(@Param("id") Long id, @Param("status") Integer status,
                          @Param("version") Integer version, @Param("updatedBy") Long updatedBy);
    int resetAdminPassword(@Param("id") Long id, @Param("passwordHash") String passwordHash,
                           @Param("newTokenVersion") Integer newTokenVersion);

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