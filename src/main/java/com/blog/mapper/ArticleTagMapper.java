package com.blog.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.*;
import org.apache.ibatis.annotations.Mapper;
/**
 * 文章标签关联 Mapper
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
}