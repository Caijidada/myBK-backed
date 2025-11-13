package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
@TableName("tb_tag")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String color;

    private Integer articleCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
