package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Comment;
import com.blog.security.UserPrincipal;
import com.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论控制器
 */
@Tag(name = "评论管理", description = "评论的增删查改")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取评论列表
     */
    @Operation(summary = "获取评论列表")
    @GetMapping
    public Result<PageResult<Comment>> getCommentList(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<Comment> result = commentService.getCommentList(articleId, page, size);
        return Result.success(result);
    }

    /**
     * 发表评论
     */
    @Operation(summary = "发表评论")
    @PostMapping
    public Result<Long> createComment(
            @RequestBody Map<String, Object> commentData,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Long articleId = Long.valueOf(commentData.get("articleId").toString());
        String content = commentData.get("content").toString();
        Long parentId = commentData.get("parentId") != null ?
                Long.valueOf(commentData.get("parentId").toString()) : null;
        Long replyToId = commentData.get("replyToId") != null ?
                Long.valueOf(commentData.get("replyToId").toString()) : null;

        Long commentId = commentService.createComment(
                articleId, currentUser.getUserId(), content, parentId, replyToId
        );
        return Result.success("评论成功", commentId);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        commentService.deleteComment(id, currentUser.getUserId());
        return Result.success("删除成功", null);
    }

    /**
     * 点赞评论
     */
    @Operation(summary = "点赞评论")
    @PostMapping("/{id}/like")
    public Result<Void> likeComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        commentService.likeComment(id, currentUser.getUserId());
        return Result.success("点赞成功", null);
    }

    /**
     * 取消点赞评论
     */
    @Operation(summary = "取消点赞评论")
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        commentService.unlikeComment(id, currentUser.getUserId());
        return Result.success("取消点赞成功", null);
    }
}
