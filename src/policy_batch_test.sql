-- MySQL 기준
# CREATE DATABASE IF NOT EXISTS test_db
#     DEFAULT CHARACTER SET utf8mb4
#     DEFAULT COLLATE utf8mb4_unicode_ci;

USE test_db;

CREATE TABLE youth_policy
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_no             VARCHAR(20)  NOT NULL UNIQUE,
    title                 VARCHAR(255) NOT NULL,
    description           TEXT         NOT NULL,
    support_content       TEXT,
    application_method    TEXT,
    screening_method      TEXT,
    submit_documents      TEXT,
    policy_benefit_amount BIGINT,
    etc_notes             TEXT,
    apply_url             VARCHAR(500),
    ref_url_1             VARCHAR(500),
    ref_url_2             VARCHAR(500),
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_financial_support  TINYINT
);



CREATE TABLE youth_policy_condition
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id             BIGINT NOT NULL,
    min_age               INT,
    max_age               INT,
    age_limit_yn          TINYINT(1),
#     region_code           VARCHAR(100),
    marriage_status       VARCHAR(10),
    employment_status     VARCHAR(10),
    education_level       VARCHAR(10),
    major                 VARCHAR(10),
    special_condition     VARCHAR(10),
    income_condition_code VARCHAR(10),
    income_min            BIGINT,
    income_max            BIGINT,
    income_etc            TEXT,
    additional_conditions TEXT,
    participant_target    TEXT,
    FOREIGN KEY (policy_id) REFERENCES youth_policy (id) ON DELETE CASCADE
);


CREATE TABLE youth_policy_period
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id      BIGINT NOT NULL,
    apply_period   VARCHAR(100),
    biz_start_date DATE,
    biz_end_date   DATE,
    biz_period_etc TEXT,
    FOREIGN KEY (policy_id) REFERENCES youth_policy (id) ON DELETE CASCADE
);

CREATE TABLE policy_keyword
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(50) NOT NULL
);


CREATE TABLE youth_policy_keyword
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id  BIGINT NOT NULL,
    keyword_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (policy_id) REFERENCES youth_policy (id) ON DELETE CASCADE,
    FOREIGN KEY (keyword_id) REFERENCES policy_keyword (id) ON DELETE CASCADE
);

-- 정책 전체 보기
SELECT *
FROM youth_policy;

-- 정책별 조건 보기
SELECT *
FROM youth_policy_condition
WHERE policy_id = 1;

-- 정책별 키워드 보기 (조인)
SELECT yp.policy_no, pk.keyword
FROM youth_policy_keyword ypk
         JOIN policy_keyword pk ON ypk.keyword_id = pk.id
         JOIN youth_policy yp ON ypk.policy_id = yp.id;


ALTER TABLE youth_policy_condition
    DROP COLUMN region_code;

-- 지역 마스터 테이블
CREATE TABLE policy_region
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    region_code VARCHAR(10) NOT NULL UNIQUE
);


-- 정책-지역 매핑 테이블
CREATE TABLE youth_policy_region
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id  BIGINT NOT NULL,
    region_id  BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (policy_id) REFERENCES youth_policy (id) ON DELETE CASCADE,
    FOREIGN KEY (region_id) REFERENCES policy_region (id) ON DELETE CASCADE
);
