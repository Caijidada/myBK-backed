package com.blog.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.*;
import org.apache.ibatis.annotations.Mapper;
/**
 * 点赞 Mapper
 */
@Mapper
public interface LikeMapper extends BaseMapper<Like> {
}