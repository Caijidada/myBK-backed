package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.response.ArticleListResponse;
import com.blog.entity.User;
import com.blog.security.UserPrincipal;
import com.blog.service.ArticleService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ArticleService articleService;

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userService.getUserInfo(currentUser.getUserId());
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(
            @RequestBody User user,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        userService.updateUserInfo(currentUser.getUserId(), user);
        return Result.success("更新成功", null);
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestBody Map<String, String> passwordData,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        userService.changePassword(currentUser.getUserId(), oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }

    /**
     * 获取我的文章
     */
    @Operation(summary = "获取我的文章")
    @GetMapping("/articles")
    public Result<PageResult<ArticleListResponse>> getMyArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        PageResult<ArticleListResponse> result = articleService.getUserArticles(
                currentUser.getUserId(), page, size, status
        );
        return Result.success(result);
    }

    /**
     * 获取我的收藏
     */
    @Operation(summary = "获取我的收藏")
    @GetMapping("/favorites")
    public Result<PageResult<ArticleListResponse>> getMyFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        PageResult<ArticleListResponse> result = articleService.getUserFavorites(
                currentUser.getUserId(), page, size
        );
        return Result.success(result);
    }
}
