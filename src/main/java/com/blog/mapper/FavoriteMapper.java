package com.blog.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.*;
import org.apache.ibatis.annotations.Mapper;
/**
 * 收藏 Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
