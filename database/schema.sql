-- ============================================================
-- AI FARMER ASSISTANT PLATFORM — DATABASE SCHEMA
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS farmer_assistant
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE farmer_assistant;

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            ENUM('FARMER','ADMIN','OFFICER') NOT NULL DEFAULT 'FARMER',
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    profile_image_url VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: farmers
-- ============================================================
CREATE TABLE IF NOT EXISTS farmers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE,
    state           VARCHAR(100),
    district        VARCHAR(100),
    village         VARCHAR(100),
    land_acres      DECIMAL(10,2),
    soil_type       ENUM('CLAY','SANDY','LOAMY','SILT','CHALKY','PEAT') DEFAULT 'LOAMY',
    primary_crop    VARCHAR(100),
    secondary_crop  VARCHAR(100),
    water_source    VARCHAR(100),
    aadhaar_masked  VARCHAR(20),
    kcc_number      VARCHAR(50),
    latitude        DOUBLE,
    longitude       DOUBLE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_farmers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_farmers_state_district (state, district),
    INDEX idx_farmers_soil_type (soil_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: agricultural_officers
-- ============================================================
CREATE TABLE IF NOT EXISTS agricultural_officers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE,
    designation     VARCHAR(200),
    district        VARCHAR(100),
    state           VARCHAR(100),
    employee_id     VARCHAR(50) UNIQUE,
    department      VARCHAR(200),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_officers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: crop_recommendations
-- ============================================================
CREATE TABLE IF NOT EXISTS crop_recommendations (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    state               VARCHAR(100),
    district            VARCHAR(100),
    season              VARCHAR(20) NOT NULL,
    soil_type           VARCHAR(50),
    nitrogen            DOUBLE,
    phosphorus          DOUBLE,
    potassium           DOUBLE,
    temperature         DOUBLE,
    humidity            DOUBLE,
    rainfall            DOUBLE,
    ph_level            DOUBLE,
    recommended_crop    VARCHAR(100),
    confidence_score    DOUBLE,
    expected_yield      VARCHAR(100),
    profit_estimate     VARCHAR(100),
    water_requirement   VARCHAR(100),
    fertilizer_advice   TEXT,
    gemini_explanation  LONGTEXT,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_crop_rec_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_crop_rec_user (user_id),
    INDEX idx_crop_rec_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: disease_reports
-- ============================================================
CREATE TABLE IF NOT EXISTS disease_reports (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    image_url           VARCHAR(500) NOT NULL,
    image_s3_key        VARCHAR(500),
    crop_type           VARCHAR(100),
    disease_name        VARCHAR(200),
    confidence_score    DOUBLE,
    treatment           LONGTEXT,
    medicine            TEXT,
    prevention          LONGTEXT,
    gemini_explanation  LONGTEXT,
    is_resolved         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_disease_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_disease_user (user_id),
    INDEX idx_disease_name (disease_name),
    INDEX idx_disease_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: market_prices
-- ============================================================
CREATE TABLE IF NOT EXISTS market_prices (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    commodity       VARCHAR(100) NOT NULL,
    market_name     VARCHAR(200),
    state           VARCHAR(100),
    district        VARCHAR(100),
    min_price       DECIMAL(10,2),
    max_price       DECIMAL(10,2),
    modal_price     DECIMAL(10,2),
    price_unit      VARCHAR(50) DEFAULT 'per quintal',
    trade_date      DATE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_market_commodity (commodity),
    INDEX idx_market_state (state),
    INDEX idx_market_date (trade_date),
    UNIQUE KEY uq_market_price (commodity, market_name, trade_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: government_schemes
-- ============================================================
CREATE TABLE IF NOT EXISTS government_schemes (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(300) NOT NULL,
    description         LONGTEXT,
    eligibility         LONGTEXT,
    benefits            LONGTEXT,
    documents_required  LONGTEXT,
    official_url        VARCHAR(500),
    category            ENUM('SUBSIDY','LOAN','INSURANCE','TRAINING','EQUIPMENT','SEED','FERTILIZER') NOT NULL,
    applicable_states   TEXT,
    applicable_crops    TEXT,
    min_land_acres      DECIMAL(10,2),
    max_land_acres      DECIMAL(10,2),
    deadline            DATE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_by          BIGINT,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_scheme_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_scheme_category (category),
    INDEX idx_scheme_active (is_active),
    FULLTEXT INDEX ft_scheme_search (title, description, eligibility)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: scheme_bookmarks
-- ============================================================
CREATE TABLE IF NOT EXISTS scheme_bookmarks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    scheme_id   BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookmark_user   FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmark_scheme FOREIGN KEY (scheme_id) REFERENCES government_schemes(id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_scheme (user_id, scheme_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: notifications
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(300) NOT NULL,
    message     TEXT NOT NULL,
    type        ENUM('INFO','WARNING','ALERT','SUCCESS') NOT NULL DEFAULT 'INFO',
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, is_read),
    INDEX idx_notif_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: chat_messages
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_messages (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    session_id  VARCHAR(36) NOT NULL,
    role        ENUM('USER','ASSISTANT') NOT NULL,
    message     LONGTEXT NOT NULL,
    image_url   VARCHAR(500),
    language    VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chat_session (session_id),
    INDEX idx_chat_user_session (user_id, session_id),
    INDEX idx_chat_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: refresh_tokens
-- ============================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  DATETIME NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_token (token),
    INDEX idx_refresh_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: otp_tokens
-- ============================================================
CREATE TABLE IF NOT EXISTS otp_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    otp         VARCHAR(6) NOT NULL,
    purpose     ENUM('EMAIL_VERIFY','PASSWORD_RESET') NOT NULL,
    expires_at  DATETIME NOT NULL,
    is_used     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_otp_user_purpose (user_id, purpose)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
