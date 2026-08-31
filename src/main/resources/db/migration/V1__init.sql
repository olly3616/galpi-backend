-- 갈피 초기 스키마 (V1). 운영/스테이징에서 Flyway가 실행한다.
-- 엔티티(JPA)와 1:1로 맞춘 MySQL 8 스키마. 이후 스키마 변경은 V2__*.sql 로 추가한다.
-- 문자셋은 이모지/다국어를 위해 utf8mb4.

CREATE TABLE users (
    id                bigint       NOT NULL AUTO_INCREMENT,
    email             varchar(255) NOT NULL,
    password          varchar(255) NOT NULL,
    nickname          varchar(20)  NOT NULL,
    bio               varchar(500),
    profile_image_url varchar(1024),
    created_at        datetime(6),
    updated_at        datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE works (
    id            bigint       NOT NULL AUTO_INCREMENT,
    source        varchar(20)  NOT NULL,
    title         varchar(500) NOT NULL,
    author        varchar(255),
    publisher     varchar(255),
    cover_url     varchar(1024),
    isbn          varchar(20),
    owner_user_id bigint,
    created_at    datetime(6),
    updated_at    datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_work_isbn UNIQUE (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bookshelf (
    id         bigint NOT NULL AUTO_INCREMENT,
    user_id    bigint NOT NULL,
    work_id    bigint NOT NULL,
    created_at datetime(6),
    updated_at datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_bookshelf_user_work UNIQUE (user_id, work_id),
    CONSTRAINT fk_bookshelf_work FOREIGN KEY (work_id) REFERENCES works (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quotes (
    id             bigint        NOT NULL AUTO_INCREMENT,
    user_id        bigint        NOT NULL,
    work_id        bigint        NOT NULL,
    character_name varchar(100),
    content        varchar(5000) NOT NULL,
    memo           varchar(2000),
    visibility     varchar(20)   NOT NULL,
    deleted_at     datetime(6),
    created_at     datetime(6),
    updated_at     datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_quotes_work FOREIGN KEY (work_id) REFERENCES works (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quote_schedules (
    id           bigint      NOT NULL AUTO_INCREMENT,
    user_id      bigint      NOT NULL,
    quote_id     bigint      NOT NULL,
    send_time    time(6)     NOT NULL,
    repeat_type  varchar(20) NOT NULL,
    days_of_week varchar(50),
    send_date    date,
    is_active    bit(1)      NOT NULL,
    last_sent_at datetime(6),
    created_at   datetime(6),
    updated_at   datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_schedules_quote FOREIGN KEY (quote_id) REFERENCES quotes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE follows (
    id           bigint NOT NULL AUTO_INCREMENT,
    follower_id  bigint NOT NULL,
    following_id bigint NOT NULL,
    created_at   datetime(6),
    updated_at   datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_follow_follower_following UNIQUE (follower_id, following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE likes (
    id         bigint NOT NULL AUTO_INCREMENT,
    user_id    bigint NOT NULL,
    quote_id   bigint NOT NULL,
    created_at datetime(6),
    updated_at datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_like_user_quote UNIQUE (user_id, quote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE device_tokens (
    id         bigint       NOT NULL AUTO_INCREMENT,
    user_id    bigint       NOT NULL,
    token      varchar(512) NOT NULL,
    platform   varchar(20)  NOT NULL,
    created_at datetime(6),
    updated_at datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_device_tokens_token UNIQUE (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_tokens (
    id         bigint      NOT NULL AUTO_INCREMENT,
    user_id    bigint      NOT NULL,
    token_hash varchar(64) NOT NULL,
    expires_at datetime(6) NOT NULL,
    created_at datetime(6),
    updated_at datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
