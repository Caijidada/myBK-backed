package com.blog.controller;

import com.blog.common.Constants;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.request.ReviewArticleRequest;
import com.blog.dto.response.ArticleListResponse;
import com.blog.security.UserPrincipal;
import com.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 文章审核控制器（仅管理员）
 */
@Tag(name = "文章审核管理", description = "管理员文章审核相关接口")
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ArticleReviewController {

    private final ArticleService articleService;

    /**
     * 获取待审核文章列表
     */
    @Operation(summary = "获取待审核文章列表")
    @GetMapping("/pending")
    public Result<PageResult<ArticleListResponse>> getPendingArticles(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 1) page = Constants.DEFAULT_PAGE_NUM;
        if (size < 1 || size > Constants.MAX_PAGE_SIZE) size = Constants.DEFAULT_PAGE_SIZE;

        PageResult<ArticleListResponse> result = articleService.getPendingArticles(page, size);
        return Result.success(result);
    }

    /**
     * 审核文章（批准或拒绝）
     */
    @Operation(summary = "审核文章")
    @PostMapping("/{id}/review")
    public Result<String> reviewArticle(
            @Parameter(description = "文章ID") @PathVariable Long id,
            @Valid @RequestBody ReviewArticleRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long reviewerId = userPrincipal.getUserId();

        if ("APPROVE".equals(request.getAction())) {
            articleService.approveArticle(id, reviewerId, request.getNote());
            return Result.success("文章已批准");
        } else if ("REJECT".equals(request.getAction())) {
            articleService.rejectArticle(id, reviewerId, request.getNote());
            return Result.success("文章已拒绝");
        }

        return Result.error(400, "无效的审核操作");
    }
}
