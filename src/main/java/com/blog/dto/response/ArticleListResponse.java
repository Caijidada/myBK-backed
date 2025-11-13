package com.blog.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表项响应 DTO
 */
@Data
public class ArticleListResponse {

    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isTop;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;

    private String authorName;
    private String authorAvatar;
    private String categoryName;
    private List<String> tagNames;
}