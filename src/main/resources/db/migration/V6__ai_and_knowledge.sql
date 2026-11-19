-- AI 与知识库域：会话、消息、文档、分片和索引任务。
-- 说明：本脚本不声明物理外键，关联完整性由 Service 层维护。

-- AI 会话表，thread_id 与 LangGraph checkpointer 对齐。
CREATE TABLE ai_conversation (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    thread_id CHAR(26) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(100) NOT NULL,
    status TINYINT UNSIGNED NOT NULL DEFAULT 1,
    last_message_at DATETIME(3) NULL,
    message_count INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version INT UNSIGNED NOT NULL DEFAULT 0,
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_conversation_thread_id (thread_id),
    KEY idx_ai_conversation_user_time (user_id, is_deleted, last_message_at),
    KEY idx_ai_conversation_status (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- AI 消息表，是用户可见消息的事实源。
CREATE TABLE ai_message (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NULL,
    sequence_no INT UNSIGNED NOT NULL,
    role VARCHAR(16) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    status TINYINT UNSIGNED NOT NULL,
    client_message_id CHAR(36) NULL,
    trace_id VARCHAR(64) NULL,
    model_name VARCHAR(128) NULL,
    token_usage JSON NULL,
    latency_ms INT UNSIGNED NULL,
    grounded TINYINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_message_sequence (conversation_id, sequence_no),
    UNIQUE KEY uk_ai_message_client (conversation_id, client_message_id),
    KEY idx_ai_message_conversation_time (conversation_id, created_at),
    KEY idx_ai_message_user (user_id, created_at),
    KEY idx_ai_message_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- AI 回答来源追溯表。
CREATE TABLE ai_message_source (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    message_id BIGINT UNSIGNED NOT NULL,
    source_type TINYINT UNSIGNED NOT NULL,
    source_id BIGINT UNSIGNED NULL,
    chunk_id BIGINT UNSIGNED NULL,
    source_title VARCHAR(255) NOT NULL,
    source_url VARCHAR(512) NULL,
    snippet VARCHAR(1000) NULL,
    score DECIMAL(8,6) NULL,
    rank_no INT UNSIGNED NOT NULL DEFAULT 0,
    metadata JSON NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_ai_message_source_message (message_id, rank_no),
    KEY idx_ai_message_source_type (source_type, source_id),
    KEY idx_ai_message_source_chunk (chunk_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 知识库文档版本表。
CREATE TABLE knowledge_document (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL,
    file_id BIGINT UNSIGNED NOT NULL,
    doc_type TINYINT UNSIGNED NOT NULL,
    version_no INT UNSIGNED NOT NULL DEFAULT 1,
    checksum CHAR(64) NULL,
    status TINYINT UNSIGNED NOT NULL DEFAULT 0,
    index_status TINYINT UNSIGNED NOT NULL DEFAULT 0,
    index_error VARCHAR(1000) NULL,
    chunk_count INT UNSIGNED NOT NULL DEFAULT 0,
    source_url VARCHAR(512) NULL,
    effective_at DATETIME(3) NULL,
    published_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    created_by BIGINT UNSIGNED NULL,
    updated_by BIGINT UNSIGNED NULL,
    version INT UNSIGNED NOT NULL DEFAULT 0,
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_knowledge_document_file_version (file_id, version_no),
    KEY idx_knowledge_document_status (status, index_status, is_deleted),
    KEY idx_knowledge_document_category (category, status, is_deleted),
    KEY idx_knowledge_document_checksum (checksum)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 知识分片元数据表。
CREATE TABLE knowledge_chunk (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    document_id BIGINT UNSIGNED NOT NULL,
    chunk_index INT UNSIGNED NOT NULL,
    vector_id VARCHAR(128) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    content_hash CHAR(64) NULL,
    char_count INT UNSIGNED NOT NULL DEFAULT 0,
    metadata JSON NULL,
    enabled TINYINT UNSIGNED NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_knowledge_chunk_vector (vector_id),
    UNIQUE KEY uk_knowledge_chunk_document_index (document_id, chunk_index),
    KEY idx_knowledge_chunk_document_enabled (document_id, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 知识索引任务表。
CREATE TABLE ai_index_job (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    document_id BIGINT UNSIGNED NOT NULL,
    document_version INT UNSIGNED NOT NULL,
    job_type TINYINT UNSIGNED NOT NULL,
    status TINYINT UNSIGNED NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(128) NOT NULL,
    external_job_id VARCHAR(64) NULL,
    progress INT UNSIGNED NOT NULL DEFAULT 0,
    total_chunks INT UNSIGNED NOT NULL DEFAULT 0,
    processed_chunks INT UNSIGNED NOT NULL DEFAULT 0,
    error_message VARCHAR(1000) NULL,
    requested_by BIGINT UNSIGNED NULL,
    started_at DATETIME(3) NULL,
    finished_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_index_job_idempotency (idempotency_key),
    KEY idx_ai_index_job_document (document_id, created_at),
    KEY idx_ai_index_job_status (status, created_at),
    KEY idx_ai_index_job_external (external_job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
