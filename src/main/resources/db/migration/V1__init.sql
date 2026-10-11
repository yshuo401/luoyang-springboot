-- 洛阳旅游系统首版数据库结构
-- Flyway version: V1
-- MySQL 8.x / InnoDB / utf8mb4

CREATE TABLE ly_admin_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(80) NOT NULL,
  role_code VARCHAR(32) NOT NULL DEFAULT 'CONTENT_EDITOR',
  status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_admin_username (username),
  KEY idx_admin_role_status (role_code, status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_app_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  openid VARCHAR(128) NOT NULL,
  unionid VARCHAR(128) NULL,
  nickname VARCHAR(80) NOT NULL DEFAULT '洛阳游客',
  avatar_file_id BIGINT UNSIGNED NULL,
  status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_openid (openid),
  KEY idx_app_status (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_scenic_category (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(60) NOT NULL,
  code VARCHAR(60) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_category_code (code),
  KEY idx_category_public (status, is_deleted, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_sys_file (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  original_name VARCHAR(255) NOT NULL,
  object_key VARCHAR(512) NOT NULL,
  bucket_type VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
  business_type VARCHAR(40) NOT NULL,
  content_type VARCHAR(120) NOT NULL,
  file_ext VARCHAR(16) NOT NULL,
  size_bytes BIGINT UNSIGNED NOT NULL,
  sha256 CHAR(64) NULL,
  uploader_type VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
  uploader_admin_id BIGINT UNSIGNED NULL,
  uploader_app_user_id BIGINT UNSIGNED NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_file_object_key (object_key),
  KEY idx_file_business_status (business_type, status),
  KEY idx_file_uploader_admin (uploader_admin_id),
  CONSTRAINT fk_file_admin FOREIGN KEY (uploader_admin_id) REFERENCES ly_admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_scenic_spot (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  category_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(120) NOT NULL,
  short_description VARCHAR(500) NULL,
  address VARCHAR(255) NOT NULL,
  longitude DECIMAL(10,6) NULL,
  latitude DECIMAL(10,6) NULL,
  opening_hours VARCHAR(255) NULL,
  ticket_info VARCHAR(255) NULL,
  phone VARCHAR(40) NULL,
  suggested_visit_minutes SMALLINT UNSIGNED NULL,
  description MEDIUMTEXT NULL,
  history_intro MEDIUMTEXT NULL,
  cover_file_id BIGINT UNSIGNED NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  is_recommended TINYINT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  created_by BIGINT UNSIGNED NULL,
  updated_by BIGINT UNSIGNED NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_spot_category_public (category_id, status, is_deleted),
  KEY idx_spot_recommend (status, is_recommended, sort_order, id),
  KEY idx_spot_name (name),
  CONSTRAINT fk_spot_category FOREIGN KEY (category_id) REFERENCES ly_scenic_category (id),
  CONSTRAINT fk_spot_cover_file FOREIGN KEY (cover_file_id) REFERENCES ly_sys_file (id),
  CONSTRAINT fk_spot_created_by FOREIGN KEY (created_by) REFERENCES ly_admin_user (id),
  CONSTRAINT fk_spot_updated_by FOREIGN KEY (updated_by) REFERENCES ly_admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_scenic_spot_image (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  scenic_spot_id BIGINT UNSIGNED NOT NULL,
  file_id BIGINT UNSIGNED NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_spot_image_file (scenic_spot_id, file_id),
  KEY idx_spot_image_order (scenic_spot_id, sort_order, id),
  CONSTRAINT fk_spot_image_spot FOREIGN KEY (scenic_spot_id) REFERENCES ly_scenic_spot (id) ON DELETE CASCADE,
  CONSTRAINT fk_spot_image_file FOREIGN KEY (file_id) REFERENCES ly_sys_file (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_travel_guide (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(160) NOT NULL,
  summary VARCHAR(800) NULL,
  content MEDIUMTEXT NULL,
  cover_file_id BIGINT UNSIGNED NULL,
  days_count TINYINT UNSIGNED NOT NULL DEFAULT 1,
  travel_theme VARCHAR(60) NULL,
  suitable_for VARCHAR(255) NULL,
  tags_json JSON NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  is_recommended TINYINT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  published_at DATETIME(3) NULL,
  created_by BIGINT UNSIGNED NULL,
  updated_by BIGINT UNSIGNED NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_guide_public (status, is_recommended, sort_order, id),
  KEY idx_guide_title (title),
  CONSTRAINT fk_guide_cover_file FOREIGN KEY (cover_file_id) REFERENCES ly_sys_file (id),
  CONSTRAINT fk_guide_created_by FOREIGN KEY (created_by) REFERENCES ly_admin_user (id),
  CONSTRAINT fk_guide_updated_by FOREIGN KEY (updated_by) REFERENCES ly_admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_guide_day (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  guide_id BIGINT UNSIGNED NOT NULL,
  day_number TINYINT UNSIGNED NOT NULL,
  title VARCHAR(120) NULL,
  description VARCHAR(1000) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_guide_day_number (guide_id, day_number),
  KEY idx_guide_day_order (guide_id, sort_order, id),
  CONSTRAINT fk_guide_day_guide FOREIGN KEY (guide_id) REFERENCES ly_travel_guide (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_guide_day_spot (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  guide_day_id BIGINT UNSIGNED NOT NULL,
  scenic_spot_id BIGINT UNSIGNED NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  suggested_start_time TIME NULL,
  stay_minutes SMALLINT UNSIGNED NULL,
  transit_note VARCHAR(500) NULL,
  visit_tip VARCHAR(1000) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_day_spot_order (guide_day_id, sort_order),
  KEY idx_day_spot_spot (scenic_spot_id),
  CONSTRAINT fk_day_spot_day FOREIGN KEY (guide_day_id) REFERENCES ly_guide_day (id) ON DELETE CASCADE,
  CONSTRAINT fk_day_spot_spot FOREIGN KEY (scenic_spot_id) REFERENCES ly_scenic_spot (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_food (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  food_type VARCHAR(60) NULL,
  description MEDIUMTEXT NULL,
  recommend_reason VARCHAR(800) NULL,
  address VARCHAR(255) NULL,
  price_range VARCHAR(100) NULL,
  longitude DECIMAL(10,6) NULL,
  latitude DECIMAL(10,6) NULL,
  cover_file_id BIGINT UNSIGNED NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  sort_order INT NOT NULL DEFAULT 0,
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  created_by BIGINT UNSIGNED NULL,
  updated_by BIGINT UNSIGNED NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_food_public (status, sort_order, id),
  KEY idx_food_name (name),
  CONSTRAINT fk_food_cover_file FOREIGN KEY (cover_file_id) REFERENCES ly_sys_file (id),
  CONSTRAINT fk_food_created_by FOREIGN KEY (created_by) REFERENCES ly_admin_user (id),
  CONSTRAINT fk_food_updated_by FOREIGN KEY (updated_by) REFERENCES ly_admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_home_banner (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(120) NOT NULL,
  subtitle VARCHAR(255) NULL,
  image_file_id BIGINT UNSIGNED NOT NULL,
  target_type VARCHAR(24) NOT NULL DEFAULT 'NONE',
  target_id BIGINT UNSIGNED NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  start_at DATETIME(3) NULL,
  end_at DATETIME(3) NULL,
  created_by BIGINT UNSIGNED NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_banner_public (status, sort_order, id),
  CONSTRAINT fk_banner_image_file FOREIGN KEY (image_file_id) REFERENCES ly_sys_file (id),
  CONSTRAINT fk_banner_created_by FOREIGN KEY (created_by) REFERENCES ly_admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_user_favorite (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  app_user_id BIGINT UNSIGNED NOT NULL,
  target_type VARCHAR(24) NOT NULL,
  target_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_favorite_target (app_user_id, target_type, target_id),
  KEY idx_favorite_user_time (app_user_id, created_at, id),
  CONSTRAINT fk_favorite_user FOREIGN KEY (app_user_id) REFERENCES ly_app_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_ai_conversation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  app_user_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(160) NOT NULL DEFAULT '新的洛阳旅游咨询',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  message_count INT UNSIGNED NOT NULL DEFAULT 0,
  last_message_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ai_conversation_owner (app_user_id, status, updated_at),
  CONSTRAINT fk_ai_conversation_user FOREIGN KEY (app_user_id) REFERENCES ly_app_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_ai_message (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  conversation_id BIGINT UNSIGNED NOT NULL,
  role VARCHAR(16) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  sources_json JSON NULL,
  model_name VARCHAR(120) NULL,
  latency_ms INT UNSIGNED NULL,
  input_tokens INT UNSIGNED NULL,
  output_tokens INT UNSIGNED NULL,
  result_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ai_message_order (conversation_id, created_at, id),
  CONSTRAINT fk_ai_message_conversation FOREIGN KEY (conversation_id) REFERENCES ly_ai_conversation (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ly_ai_knowledge_document (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(180) NOT NULL,
  description VARCHAR(1000) NULL,
  file_id BIGINT UNSIGNED NOT NULL,
  content_hash CHAR(64) NULL,
  index_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  enabled TINYINT NOT NULL DEFAULT 1,
  chunk_count INT UNSIGNED NOT NULL DEFAULT 0,
  last_indexed_at DATETIME(3) NULL,
  error_message VARCHAR(1000) NULL,
  created_by_admin_id BIGINT UNSIGNED NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_kb_file (file_id),
  KEY idx_kb_status (enabled, index_status, is_deleted),
  CONSTRAINT fk_kb_file FOREIGN KEY (file_id) REFERENCES ly_sys_file (id),
  CONSTRAINT fk_kb_admin FOREIGN KEY (created_by_admin_id) REFERENCES ly_admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
