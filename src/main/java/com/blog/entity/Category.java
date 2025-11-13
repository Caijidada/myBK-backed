package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分类实体类
 */
@Data
@TableName("tb_category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String icon;

    private Integer sortOrder;

    private Integer articleCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}