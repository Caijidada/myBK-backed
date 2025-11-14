package com.blog.controller;

import com.blog.common.Constants;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.request.ArticleRequest;
import com.blog.dto.response.ArticleDetailResponse;
import com.blog.dto.response.ArticleListResponse;
import com.blog.security.UserPrincipal;
import com.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 */
@Tag(name = "文章管理", description = "文章的增删改查")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 获取文章列表（分页）
     */
    @Operation(summary = "获取文章列表")
    @GetMapping
    public Result<PageResult<ArticleListResponse>> getArticleList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "标签ID") @RequestParam(required = false) Long tagId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword
    ) {
        // 参数校验
        if (page < 1) page = Constants.DEFAULT_PAGE_NUM;
        if (size < 1 || size > Constants.MAX_PAGE_SIZE) size = Constants.DEFAULT_PAGE_SIZE;

        PageResult<ArticleListResponse> result = articleService.getArticleList(page, size, categoryId, tagId, keyword);
        return Result.success(result);
    }

    /**
     * 获取文章详情
     */
    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailResponse> getArticleDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        ArticleDetailResponse response = articleService.getArticleDetail(id, userId);
        return Result.success(response);
    }

    /**
     * 创建文章
     */
    @Operation(summary = "创建文章")
    @PostMapping
    public Result<Long> createArticle(
            @Valid @RequestBody ArticleRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Long articleId = articleService.createArticle(request, currentUser.getUserId());
        return Result.success("文章创建成功", articleId);
    }

    /**
     * 更新文章
     */
    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<Void> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        articleService.updateArticle(id, request, currentUser.getUserId());
        return Result.success("文章更新成功", null);
    }

    /**
     * 删除文章
     */
    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        articleService.deleteArticle(id, currentUser.getUserId());
        return Result.success("文章删除成功", null);
    }

    /**
     * 发布文章
     */
    @Operation(summary = "发布文章")
    @PutMapping("/{id}/publish")
    public Result<Void> publishArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        articleService.publishArticle(id, currentUser.getUserId());
        return Result.success("文章发布成功", null);
    }

    /**
     * 下架文章
     */
    @Operation(summary = "下架文章")
    @PutMapping("/{id}/unpublish")
    public Result<Void> unpublishArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        articleService.unpublishArticle(id, currentUser.getUserId());
        return Result.success("文章下架成功", null);
    }

    /**
     * 点赞文章
     */
    @Operation(summary = "点赞文章")
    @PostMapping("/{id}/like")
    public Result<Void> likeArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        articleService.likeArticle(id, currentUser.getUserId());
        return Result.success("点赞成功", null);
    }

    /**
     * 取消点赞文章
     */
    @Operation(summary = "取消点赞文章")
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        articleService.unlikeArticle(id, currentUser.getUserId());
        return Result.success("取消点赞成功", null);
    }

    /**
     * 收藏文章
     */
    @Operation(summary = "收藏文章")
    @PostMapping("/{id}/favorite")
    public Result<Void> favoriteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        articleService.favoriteArticle(id, currentUser.getUserId());
        return Result.success("收藏成功", null);
    }

    /**
     * 取消收藏文章
     */
    @Operation(summary = "取消收藏文章")
    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavoriteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        articleService.unfavoriteArticle(id, currentUser.getUserId());
        return Result.success("取消收藏成功", null);
    }
}