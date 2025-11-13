package com.blog.common;

/**
 * 系统常量
 */
public class Constants {

    /**
     * JWT Token 相关
     */
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_CLAIM_USER_ID = "userId";
    public static final String TOKEN_CLAIM_USERNAME = "username";
    public static final String TOKEN_CLAIM_ROLE = "role";

    /**
     * Redis Key 前缀
     */
    public static final String REDIS_KEY_ARTICLE = "article:";
    public static final String REDIS_KEY_USER = "user:";
    public static final String REDIS_KEY_HOT_ARTICLES = "article:hot";
    public static final String REDIS_KEY_CAPTCHA = "captcha:";
    public static final String REDIS_KEY_TOKEN_BLACKLIST = "token:blacklist:";

    /**
     * 默认分页参数
     */
    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 用户角色
     */
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * 文章状态
     */
    public static final int ARTICLE_STATUS_DRAFT = 0;
    public static final int ARTICLE_STATUS_PUBLISHED = 1;

    /**
     * 用户状态
     */
    public static final int USER_STATUS_DISABLED = 0;
    public static final int USER_STATUS_NORMAL = 1;

    /**
     * 点赞目标类型
     */
    public static final String LIKE_TARGET_ARTICLE = "ARTICLE";
    public static final String LIKE_TARGET_COMMENT = "COMMENT";

    /**
     * 默认头像
     */
    public static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";

    /**
     * 验证码过期时间（分钟）
     */
    public static final long CAPTCHA_EXPIRE_MINUTES = 5;
}