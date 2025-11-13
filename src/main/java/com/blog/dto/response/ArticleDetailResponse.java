package com.blog.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情响应 DTO
 */
@Data
public class ArticleDetailResponse {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String contentHtml;
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Boolean isLiked;
    private Boolean isFavorited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    /**
     * 作者信息
     */
    private AuthorInfo author;

    /**
     * 分类信息
     */
    private CategoryInfo category;

    /**
     * 标签列表
     */
    private List<TagInfo> tags;

    @Data
    public static class AuthorInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String bio;
    }

    @Data
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String icon;
    }

    @Data
    public static class TagInfo {
        private Long id;
        private String name;
        private String color;
    }
}