package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章实体类
 */
@Data
@TableName("tb_article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String summary;

    private String content;

    private String contentHtml;

    private String coverImage;

    private Long categoryId;

    private Integer isPublished;

    private Integer isTop;

    private Integer isFeatured;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer favoriteCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    // 审核相关字段
    private String reviewStatus; // PENDING, APPROVED, REJECTED

    private Long reviewerId;

    private LocalDateTime reviewedAt;

    private String reviewNote;
}