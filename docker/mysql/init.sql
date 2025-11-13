-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog;

-- 关闭外键检查，方便重复执行
SET FOREIGN_KEY_CHECKS=0;

-- 删除已存在的表（可重复执行）
DROP TABLE IF EXISTS tb_notification;
DROP TABLE IF EXISTS tb_follow;
DROP TABLE IF EXISTS tb_favorite;
DROP TABLE IF EXISTS tb_like;
DROP TABLE IF EXISTS tb_comment;
DROP TABLE IF EXISTS tb_article_tag;
DROP TABLE IF EXISTS tb_tag;
DROP TABLE IF EXISTS tb_article;
DROP TABLE IF EXISTS tb_category;
DROP TABLE IF EXISTS tb_user;

-- 开启外键检查
SET FOREIGN_KEY_CHECKS=1;

-- ===================== 创建表 =====================
CREATE TABLE tb_user (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                         username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
                         password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
                         email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
                         nickname VARCHAR(50) COMMENT '昵称',
                         avatar VARCHAR(255) COMMENT '头像URL',
                         bio TEXT COMMENT '个人简介',
                         role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '角色',
                         status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
                         email_verified TINYINT DEFAULT 0 COMMENT '邮箱是否验证',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         last_login_at TIMESTAMP NULL COMMENT '最后登录时间',

                         INDEX idx_username (username),
                         INDEX idx_email (email),
                         INDEX idx_status (status),
                         INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE tb_category (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
                             name VARCHAR(50) UNIQUE NOT NULL COMMENT '分类名称',
                             description TEXT COMMENT '分类描述',
                             icon VARCHAR(100) COMMENT '图标',
                             sort_order INT DEFAULT 0 COMMENT '排序',
                             article_count INT DEFAULT 0 COMMENT '文章数量',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                             INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

CREATE TABLE tb_article (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
                            user_id BIGINT NOT NULL COMMENT '作者ID',
                            title VARCHAR(255) NOT NULL COMMENT '标题',
                            summary TEXT COMMENT '摘要',
                            content LONGTEXT NOT NULL COMMENT '内容（Markdown）',
                            content_html LONGTEXT COMMENT '内容（HTML）',
                            cover_image VARCHAR(255) COMMENT '封面图',
                            category_id BIGINT COMMENT '分类ID',
                            is_published TINYINT DEFAULT 0 COMMENT '是否发布：0-草稿，1-已发布',
                            is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
                            is_featured TINYINT DEFAULT 0 COMMENT '是否精选',
                            view_count INT DEFAULT 0 COMMENT '浏览量',
                            like_count INT DEFAULT 0 COMMENT '点赞数',
                            comment_count INT DEFAULT 0 COMMENT '评论数',
                            favorite_count INT DEFAULT 0 COMMENT '收藏数',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            published_at TIMESTAMP NULL COMMENT '发布时间',

                            FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
                            FOREIGN KEY (category_id) REFERENCES tb_category(id) ON DELETE SET NULL,
                            INDEX idx_user_id (user_id),
                            INDEX idx_category_id (category_id),
                            INDEX idx_published (is_published, published_at DESC),
                            INDEX idx_view_count (view_count DESC),
                            INDEX idx_created_at (created_at DESC),
                            FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

CREATE TABLE tb_tag (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
                        name VARCHAR(50) UNIQUE NOT NULL COMMENT '标签名称',
                        color VARCHAR(20) COMMENT '标签颜色',
                        article_count INT DEFAULT 0 COMMENT '文章数量',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                        INDEX idx_article_count (article_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE tb_article_tag (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                                article_id BIGINT NOT NULL COMMENT '文章ID',
                                tag_id BIGINT NOT NULL COMMENT '标签ID',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                                UNIQUE KEY uk_article_tag (article_id, tag_id),
                                FOREIGN KEY (article_id) REFERENCES tb_article(id) ON DELETE CASCADE,
                                FOREIGN KEY (tag_id) REFERENCES tb_tag(id) ON DELETE CASCADE,
                                INDEX idx_article_id (article_id),
                                INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

CREATE TABLE tb_comment (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
                            article_id BIGINT NOT NULL COMMENT '文章ID',
                            user_id BIGINT NOT NULL COMMENT '用户ID',
                            parent_id BIGINT NULL COMMENT '父评论ID',
                            reply_to_id BIGINT NULL COMMENT '回复的用户ID',
                            content TEXT NOT NULL COMMENT '评论内容',
                            like_count INT DEFAULT 0 COMMENT '点赞数',
                            is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                            FOREIGN KEY (article_id) REFERENCES tb_article(id) ON DELETE CASCADE,
                            FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
                            FOREIGN KEY (parent_id) REFERENCES tb_comment(id) ON DELETE CASCADE,
                            INDEX idx_article_id (article_id, created_at DESC),
                            INDEX idx_user_id (user_id),
                            INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE tb_like (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                         user_id BIGINT NOT NULL COMMENT '用户ID',
                         target_type ENUM('ARTICLE', 'COMMENT') NOT NULL COMMENT '目标类型',
                         target_id BIGINT NOT NULL COMMENT '目标ID',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                         UNIQUE KEY uk_user_target (user_id, target_type, target_id),
                         INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

CREATE TABLE tb_favorite (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                             user_id BIGINT NOT NULL COMMENT '用户ID',
                             article_id BIGINT NOT NULL COMMENT '文章ID',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                             UNIQUE KEY uk_user_article (user_id, article_id),
                             FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
                             FOREIGN KEY (article_id) REFERENCES tb_article(id) ON DELETE CASCADE,
                             INDEX idx_user_id (user_id, created_at DESC),
                             INDEX idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

CREATE TABLE tb_follow (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                           follower_id BIGINT NOT NULL COMMENT '关注者ID',
                           following_id BIGINT NOT NULL COMMENT '被关注者ID',
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                           UNIQUE KEY uk_follower_following (follower_id, following_id),
                           FOREIGN KEY (follower_id) REFERENCES tb_user(id) ON DELETE CASCADE,
                           FOREIGN KEY (following_id) REFERENCES tb_user(id) ON DELETE CASCADE,
                           INDEX idx_follower_id (follower_id),
                           INDEX idx_following_id (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

CREATE TABLE tb_notification (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
                                 user_id BIGINT NOT NULL COMMENT '接收者ID',
                                 type ENUM('LIKE', 'COMMENT', 'FOLLOW', 'SYSTEM') NOT NULL COMMENT '通知类型',
                                 title VARCHAR(100) NOT NULL COMMENT '通知标题',
                                 content TEXT COMMENT '通知内容',
                                 related_id BIGINT COMMENT '关联ID',
                                 is_read TINYINT DEFAULT 0 COMMENT '是否已读',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                                 FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
                                 INDEX idx_user_id (user_id, is_read, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ===================== 插入初始数据 =====================
INSERT INTO tb_user (username, password, email, nickname, role) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@blog.com', '管理员', 'ADMIN');

INSERT INTO tb_category (name, description, icon) VALUES
                                                      ('前端开发', '前端技术相关', 'fa-html5'),
                                                      ('后端开发', '后端技术相关', 'fa-server'),
                                                      ('数据库', '数据库相关', 'fa-database');

INSERT INTO tb_tag (name, color) VALUES
                                     ('JavaScript', '#F7DF1E'),
                                     ('Vue.js', '#42B883'),
                                     ('Spring Boot', '#6DB33F');