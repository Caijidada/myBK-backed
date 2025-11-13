package com.blog.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.*;
import org.apache.ibatis.annotations.Mapper;
/**
 * 关注 Mapper
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
}