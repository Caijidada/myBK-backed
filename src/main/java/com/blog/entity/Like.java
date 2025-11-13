package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 点赞实体类
 */
@Data
@TableName("tb_like")
public class Like {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String targetType;

    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}