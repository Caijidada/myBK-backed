package com.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

/**
 * 文章创建/更新请求 DTO
 */
@Data
public class ArticleRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;

    private Long categoryId;

    private List<Long> tagIds;

    /**
     * 是否发布：0-草稿，1-发布
     */
    private Integer isPublished;
}