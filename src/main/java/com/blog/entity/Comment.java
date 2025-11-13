package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@TableName("tb_comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long userId;

    private Long parentId;

    private Long replyToId;

    private String content;

    private Integer likeCount;

    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}