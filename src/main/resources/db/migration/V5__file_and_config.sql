-- 系统域：OSS 文件对象和非敏感配置。
-- 说明：本脚本不声明物理外键，关联完整性由 Service 层维护。

-- 阿里云 OSS 文件对象元数据表。
CREATE TABLE file_object (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    biz_scene VARCHAR(32) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    bucket VARCHAR(128) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    extension VARCHAR(16) NULL,
    file_size BIGINT UNSIGNED NOT NULL,
    sha256 CHAR(64) NULL,
    etag VARCHAR(128) NULL,
    provider VARCHAR(32) NOT NULL DEFAULT 'OSS',
    visibility TINYINT UNSIGNED NOT NULL DEFAULT 1,
    status TINYINT UNSIGNED NOT NULL DEFAULT 0,
    uploaded_by_type TINYINT UNSIGNED NOT NULL,
    uploaded_by BIGINT UNSIGNED NULL,
    metadata JSON NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_object_key (object_key),
    KEY idx_file_object_sha256 (sha256),
    KEY idx_file_object_scene_status (biz_scene, status, is_deleted),
    KEY idx_file_object_uploader (uploaded_by_type, uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 非敏感系统配置表。
CREATE TABLE system_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(128) NOT NULL,
    config_value TEXT NOT NULL,
    value_type VARCHAR(16) NOT NULL DEFAULT 'STRING',
    description VARCHAR(500) NULL,
    is_sensitive TINYINT UNSIGNED NOT NULL DEFAULT 0,
    status TINYINT UNSIGNED NOT NULL DEFAULT 1,
    version INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    created_by BIGINT UNSIGNED NULL,
    updated_by BIGINT UNSIGNED NULL,
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_config_key (config_key),
    KEY idx_system_config_status (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
