package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Constants;
import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    /**
     * 获取所有分类
     */
    @Operation(summary = "获取分类列表")
    @GetMapping
    public Result<List<Category>> getCategoryList() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder)
        );

        // 动态计算每个分类的文章数量（只统计已发布的文章）
        for (Category category : categories) {
            long count = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>()
                            .eq(Article::getCategoryId, category.getId())
                            .eq(Article::getIsPublished, Constants.ARTICLE_STATUS_PUBLISHED)
            );
            category.setArticleCount((int) count);
        }

        return Result.success(categories);
    }

    /**
     * 获取分类详情
     */
    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<Category> getCategoryDetail(@PathVariable Long id) {
        Category category = categoryMapper.selectById(id);
        return Result.success(category);
    }
}