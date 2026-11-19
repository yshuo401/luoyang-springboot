CREATE TABLE app_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    openid VARCHAR(64) NOT NULL,
    unionid VARCHAR(64) NULL,
    nickname VARCHAR(64) NULL,
    avatar_file_id BIGINT UNSIGNED NULL,
    gender TINYINT UNSIGNED NOT NULL DEFAULT 0,
    city VARCHAR(64) NULL,
    status TINYINT UNSIGNED NOT NULL DEFAULT 1,
    token_version INT UNSIGNED NOT NULL DEFAULT 0,
    last_login_at DATETIME(3) NULL,
    last_login_ip VARCHAR(45) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version INT UNSIGNED NOT NULL DEFAULT 0,
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_openid (openid),
    KEY idx_app_user_unionid (unionid),
    KEY idx_app_user_status (status, is_deleted),
    KEY idx_app_user_created (created_at),
    KEY idx_app_user_avatar_file (avatar_file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_preference (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    preference_type TINYINT UNSIGNED NOT NULL,
    preference_key BIGINT UNSIGNED NOT NULL,
    weight DECIMAL(8,4) NOT NULL DEFAULT 0,
    source TINYINT UNSIGNED NOT NULL DEFAULT 1,
    last_interacted_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_preference (user_id, preference_type, preference_key),
    KEY idx_user_preference_key (preference_type, preference_key, weight),
    KEY idx_user_preference_user (user_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_behavior (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    behavior_type TINYINT UNSIGNED NOT NULL,
    biz_type TINYINT UNSIGNED NULL,
    biz_id BIGINT UNSIGNED NULL,
    keyword VARCHAR(128) NULL,
    duration_ms INT UNSIGNED NULL,
    source VARCHAR(32) NULL,
    metadata JSON NULL,
    trace_id VARCHAR(64) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_user_behavior_user_time (user_id, created_at),
    KEY idx_user_behavior_biz (biz_type, biz_id, created_at),
    KEY idx_user_behavior_type_time (behavior_type, created_at),
    KEY idx_user_behavior_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_favorite (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    biz_type TINYINT UNSIGNED NOT NULL,
    biz_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_favorite (user_id, biz_type, biz_id),
    KEY idx_user_favorite_user_time (user_id, created_at),
    KEY idx_user_favorite_biz (biz_type, biz_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_recommendation_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    scene VARCHAR(32) NOT NULL,
    algorithm_version VARCHAR(32) NOT NULL,
    request_context JSON NULL,
    items_json JSON NOT NULL,
    trace_id VARCHAR(64) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_recommendation_user_time (user_id, created_at),
    KEY idx_recommendation_scene_time (scene, created_at),
    KEY idx_recommendation_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
