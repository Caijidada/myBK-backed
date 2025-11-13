package com.blog.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.mapper.TagMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagMapper tagMapper;

    /**
     * 获取所有标签
     */
    @Operation(summary = "获取标签列表")
    @GetMapping
    public Result<List<Tag>> getTagList() {
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .orderByDesc(Tag::getArticleCount)
        );
        return Result.success(tags);
    }

    /**
     * 搜索标签
     */
    @Operation(summary = "搜索标签")
    @GetMapping("/search")
    public Result<List<Tag>> searchTags(@RequestParam String keyword) {
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .like(Tag::getName, keyword)
                        .orderByDesc(Tag::getArticleCount)
                        .last("LIMIT 10")
        );
        return Result.success(tags);
    }
}