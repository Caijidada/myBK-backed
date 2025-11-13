package com.blog.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * 收藏实体类
 */
@Data
@TableName("tb_favorite")
public class Favorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long articleId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}