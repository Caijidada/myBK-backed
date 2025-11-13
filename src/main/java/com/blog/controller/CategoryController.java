package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Result;
import com.blog.entity.Category;
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