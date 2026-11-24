package com.luoyang.tourism.module.system.pojo.vo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 后台用户与仪表盘响应模型。
 */
public final class SystemVos {
    private SystemVos() {
    }

    public record AppUserAdminVO(String id, String nickname, String avatarUrl, Integer gender, String city,
                                 Integer status, String openidMasked, OffsetDateTime lastLoginAt,
                                 OffsetDateTime createdAt) {
    }

    public record RoleBriefVO(String id, String name, String code) {
    }

    public record AdminUserVO(String id, String username, String nickname, String avatarUrl,
                              String email, String phone, Integer status, List<RoleBriefVO> roles,
                              Integer version, OffsetDateTime lastLoginAt, OffsetDateTime createdAt) {
    }

    public record DashboardVO(long spotTotal, long spotPublished, long userTotal, long favoriteTotal,
                              long aiConversationTotal, long aiMessageToday, long knowledgeDocumentTotal,
                              long indexFailedCount, List<AppUserAdminVO> recentUsers,
                              List<Map<String, Object>> trend) {
    }
}