-- Kanako 数据库结构 (兼容 MySQL / H2 MODE=MySQL)

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(128) DEFAULT '',
    email       VARCHAR(128),
    avatar      VARCHAR(512),
    role        VARCHAR(16)  NOT NULL DEFAULT 'USER',
    github_id   BIGINT,
    github_login VARCHAR(64),
    bio         VARCHAR(512),
    created_at  DATETIME,
    updated_at  DATETIME
);

CREATE TABLE IF NOT EXISTS blog (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    slug        VARCHAR(255),
    summary     VARCHAR(512),
    cover       VARCHAR(512),
    content_md  LONGTEXT,
    category_id BIGINT,
    status      VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
    view_count  BIGINT DEFAULT 0,
    like_count  INT DEFAULT 0,
    is_top      INT DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME
);

CREATE TABLE IF NOT EXISTS blog_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64) NOT NULL,
    slug        VARCHAR(64),
    description VARCHAR(255),
    sort        INT DEFAULT 0,
    created_at  DATETIME
);

CREATE TABLE IF NOT EXISTS blog_tag (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64) NOT NULL,
    slug        VARCHAR(64),
    created_at  DATETIME
);

CREATE TABLE IF NOT EXISTS blog_post_tag (
    blog_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    PRIMARY KEY (blog_id, tag_id)
);

CREATE TABLE IF NOT EXISTS blog_comment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    blog_id     BIGINT NOT NULL,
    user_id     BIGINT,
    nickname    VARCHAR(64),
    email       VARCHAR(128),
    avatar      VARCHAR(512),
    content     LONGTEXT NOT NULL,
    parent_id   BIGINT DEFAULT 0,
    is_admin    INT DEFAULT 0,
    created_at  DATETIME
);

CREATE TABLE IF NOT EXISTS treehole (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname    VARCHAR(64),
    avatar      VARCHAR(512),
    content     LONGTEXT NOT NULL,
    mood        VARCHAR(32),
    color       VARCHAR(32),
    like_count  INT DEFAULT 0,
    view_count  INT DEFAULT 0,
    created_at  DATETIME
);

CREATE TABLE IF NOT EXISTS treehole_comment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    hole_id     BIGINT NOT NULL,
    nickname    VARCHAR(64),
    avatar      VARCHAR(512),
    content     LONGTEXT NOT NULL,
    created_at  DATETIME
);

CREATE TABLE IF NOT EXISTS friend_link (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64) NOT NULL,
    url         VARCHAR(255) NOT NULL,
    avatar      VARCHAR(512),
    description VARCHAR(255),
    sort        INT DEFAULT 0,
    status      VARCHAR(16) DEFAULT 'SHOW',
    created_at  DATETIME
);

CREATE TABLE IF NOT EXISTS friend_circle (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    link_id     BIGINT,
    content     LONGTEXT,
    images      VARCHAR(1024),
    created_at  DATETIME
);

CREATE TABLE IF NOT EXISTS chat_session (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  VARCHAR(64) NOT NULL,
    user_id     BIGINT,
    title       VARCHAR(255) DEFAULT '新对话',
    created_at  DATETIME,
    updated_at  DATETIME
);

CREATE TABLE IF NOT EXISTS chat_message (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  VARCHAR(64) NOT NULL,
    role        VARCHAR(16) NOT NULL,
    content     LONGTEXT,
    created_at  DATETIME
);

CREATE TABLE IF NOT EXISTS site_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key   VARCHAR(64) NOT NULL,
    config_value LONGTEXT,
    updated_at   DATETIME
);

CREATE TABLE IF NOT EXISTS changelog (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    version     VARCHAR(32),
    date        VARCHAR(32),
    content_md  LONGTEXT,
    created_at  DATETIME
);

CREATE TABLE IF NOT EXISTS analytics_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    path        VARCHAR(255),
    ip          VARCHAR(64),
    user_agent  VARCHAR(512),
    created_at  DATETIME
);

CREATE INDEX IF NOT EXISTS idx_blog_category ON blog (category_id);
CREATE INDEX IF NOT EXISTS idx_blog_status ON blog (status);
CREATE INDEX IF NOT EXISTS idx_comment_blog ON blog_comment (blog_id);
CREATE INDEX IF NOT EXISTS idx_treehole_created ON treehole (created_at);
CREATE INDEX IF NOT EXISTS idx_friend_status ON friend_link (status);
CREATE INDEX IF NOT EXISTS idx_chat_session ON chat_session (session_id);
CREATE INDEX IF NOT EXISTS idx_chat_msg_session ON chat_message (session_id);