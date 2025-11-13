package com.blog.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * 关注实体类
 */
@Data
@TableName("tb_follow")
public class Follow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long followerId;

    private Long followingId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}