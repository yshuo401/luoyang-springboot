package com.luoyang.tourism.module.system.pojo.vo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

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

    public record RoleVO(String id, String name, String code, String description, Integer status,
                         Integer sortOrder, Integer version, OffsetDateTime updatedAt) {
    }

    public record PermissionVO(String id, String parentId, String name, String code, Integer type,
                               String routePath, String componentPath, String icon, Integer sortOrder,
                               List<PermissionVO> children) {
    }

    public record ConfigVO(String id, String configKey, String configValue, String valueType,
                           String description, Integer status, Integer version, OffsetDateTime updatedAt) {
    }

    public record OperationLogVO(String id, String adminUserId, String username, String module,
                                 String operationType, String httpMethod, String requestUri,
                                 String requestParams, String responseCode, boolean success,
                                 String ip, String traceId, Integer durationMs, OffsetDateTime createdAt) {
    }

    public record DashboardVO(long spotTotal, long spotPublished, long userTotal, long favoriteTotal,
                              long aiConversationTotal, long aiMessageToday, long knowledgeDocumentTotal,
                              long indexFailedCount, List<AppUserAdminVO> recentUsers,
                              List<Map<String, Object>> trend) {
    }
}
