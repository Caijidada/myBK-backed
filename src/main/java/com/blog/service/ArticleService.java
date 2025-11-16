package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.Constants;
import com.blog.common.PageResult;
import com.blog.dto.request.ArticleRequest;
import com.blog.dto.response.ArticleDetailResponse;
import com.blog.dto.response.ArticleListResponse;
import com.blog.entity.*;
import com.blog.exception.BusinessException;
import com.blog.exception.ResourceNotFoundException;
import com.blog.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;

    /**
     * 获取文章列表（分页）
     */
    public PageResult<ArticleListResponse> getArticleList(
            int page, int size, Long categoryId, Long tagId, String keyword) {

        // 构建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, Constants.ARTICLE_STATUS_PUBLISHED)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .and(keyword != null && !keyword.trim().isEmpty(),
                     w -> w.like(Article::getTitle, keyword)
                           .or()
                           .like(Article::getSummary, keyword)
                           .or()
                           .like(Article::getContent, keyword))
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishedAt);

        // 如果有标签筛选，需要联表查询
        if (tagId != null) {
            List<Long> articleIds = articleTagMapper.selectList(
                    new LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getTagId, tagId)
            ).stream().map(ArticleTag::getArticleId).collect(Collectors.toList());

            if (articleIds.isEmpty()) {
                return new PageResult<>(List.of(), 0L, (long) page, (long) size);
            }
            wrapper.in(Article::getId, articleIds);
        }

        // 分页查询
        Page<Article> articlePage = articleMapper.selectPage(
                new Page<>(page, size), wrapper
        );

        // 转换为 DTO
        List<ArticleListResponse> records = articlePage.getRecords().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                records,
                articlePage.getTotal(),
                articlePage.getCurrent(),
                articlePage.getSize()
        );
    }

    /**
     * 获取文章详情
     */
    public ArticleDetailResponse getArticleDetail(Long id, Long currentUserId) {
        // 查询文章
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsPublished() == Constants.ARTICLE_STATUS_DRAFT) {
            throw new ResourceNotFoundException("文章", id);
        }

        // 增加浏览量
        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);

        // 转换为 DTO
        ArticleDetailResponse response = convertToDetailResponse(article);

        // 如果用户已登录，检查点赞和收藏状态
        if (currentUserId != null) {
            boolean isLiked = likeMapper.selectCount(
                    new LambdaQueryWrapper<Like>()
                            .eq(Like::getUserId, currentUserId)
                            .eq(Like::getTargetType, Constants.LIKE_TARGET_ARTICLE)
                            .eq(Like::getTargetId, id)
            ) > 0;
            response.setIsLiked(isLiked);

            boolean isFavorited = favoriteMapper.selectCount(
                    new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, currentUserId)
                            .eq(Favorite::getArticleId, id)
            ) > 0;
            response.setIsFavorited(isFavorited);
        }

        return response;
    }

    /**
     * 创建文章
     */
    @Transactional
    public Long createArticle(ArticleRequest request, Long userId) {
        // 获取用户信息以检查角色
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户", userId);
        }

        // 创建文章
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());

        // 转换 Boolean 到 Integer (false -> 0, true -> 1)
        Integer publishStatus = Boolean.TRUE.equals(request.getIsPublished())
            ? Constants.ARTICLE_STATUS_PUBLISHED
            : Constants.ARTICLE_STATUS_DRAFT;
        article.setIsPublished(publishStatus);

        // 设置审核状态：管理员文章自动通过，普通用户文章需要审核
        if ("ADMIN".equals(user.getRole())) {
            article.setReviewStatus("APPROVED");
            log.info("管理员文章自动通过审核");
        } else {
            article.setReviewStatus("PENDING");
            log.info("普通用户文章进入待审核状态");
        }

        article.setIsTop(0);
        article.setIsFeatured(0);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setFavoriteCount(0);

        if (publishStatus == Constants.ARTICLE_STATUS_PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }

        articleMapper.insert(article);

        // 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }

        log.info("用户 {} 创建文章: {}, 审核状态: {}", userId, article.getTitle(), article.getReviewStatus());
        return article.getId();
    }

    /**
     * 更新文章
     */
    @Transactional
    public void updateArticle(Long id, ArticleRequest request, Long userId) {
        // 查询文章
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ResourceNotFoundException("文章", id);
        }

        // 检查权限
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限修改此文章");
        }

        // 更新文章
        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());

        // 转换 Boolean 到 Integer (false -> 0, true -> 1)
        Integer publishStatus = Boolean.TRUE.equals(request.getIsPublished())
            ? Constants.ARTICLE_STATUS_PUBLISHED
            : Constants.ARTICLE_STATUS_DRAFT;
        article.setIsPublished(publishStatus);

        if (publishStatus == Constants.ARTICLE_STATUS_PUBLISHED
                && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }

        articleMapper.updateById(article);

        // 更新标签关联
        articleTagMapper.delete(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, id)
        );

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(id);
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }

        log.info("用户 {} 更新文章: {}", userId, article.getTitle());
    }

    /**
     * 删除文章
     */
    @Transactional
    public void deleteArticle(Long id, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ResourceNotFoundException("文章", id);
        }

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限删除此文章");
        }

        articleMapper.deleteById(id);
        log.info("用户 {} 删除文章: {}", userId, article.getTitle());
    }

    /**
     * 转换为列表响应 DTO
     */
    private ArticleListResponse convertToListResponse(Article article) {
        ArticleListResponse response = new ArticleListResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setSummary(article.getSummary());
        response.setCoverImage(article.getCoverImage());
        response.setViewCount(article.getViewCount());
        response.setLikeCount(article.getLikeCount());
        response.setCommentCount(article.getCommentCount());
        response.setIsTop(article.getIsTop() == 1);
        response.setIsFeatured(article.getIsFeatured() == 1);
        response.setPublishedAt(article.getPublishedAt());

        // 作者信息
        User user = userMapper.selectById(article.getUserId());
        if (user != null) {
            response.setAuthorName(user.getNickname());
            response.setAuthorAvatar(user.getAvatar());
        }

        // 分类信息
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
            }
        }

        // 标签信息
        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, article.getId())
        );
        List<String> tagNames = articleTags.stream()
                .map(at -> {
                    Tag tag = tagMapper.selectById(at.getTagId());
                    return tag != null ? tag.getName() : null;
                })
                .filter(name -> name != null)
                .collect(Collectors.toList());
        response.setTagNames(tagNames);

        return response;
    }

    /**
     * 转换为详情响应 DTO
     */
    private ArticleDetailResponse convertToDetailResponse(Article article) {
        ArticleDetailResponse response = new ArticleDetailResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setSummary(article.getSummary());
        response.setContent(article.getContent());
        response.setContentHtml(article.getContentHtml());
        response.setCoverImage(article.getCoverImage());
        response.setViewCount(article.getViewCount());
        response.setLikeCount(article.getLikeCount());
        response.setCommentCount(article.getCommentCount());
        response.setFavoriteCount(article.getFavoriteCount());
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());
        response.setPublishedAt(article.getPublishedAt());

        // 作者信息
        User user = userMapper.selectById(article.getUserId());
        if (user != null) {
            ArticleDetailResponse.AuthorInfo authorInfo = new ArticleDetailResponse.AuthorInfo();
            authorInfo.setId(user.getId());
            authorInfo.setUsername(user.getUsername());
            authorInfo.setNickname(user.getNickname());
            authorInfo.setAvatar(user.getAvatar());
            authorInfo.setBio(user.getBio());
            response.setAuthor(authorInfo);
        }

        // 分类信息
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                ArticleDetailResponse.CategoryInfo categoryInfo = new ArticleDetailResponse.CategoryInfo();
                categoryInfo.setId(category.getId());
                categoryInfo.setName(category.getName());
                categoryInfo.setIcon(category.getIcon());
                response.setCategory(categoryInfo);
            }
        }

        // 标签信息
        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, article.getId())
        );
        List<ArticleDetailResponse.TagInfo> tags = articleTags.stream()
                .map(at -> {
                    Tag tag = tagMapper.selectById(at.getTagId());
                    if (tag != null) {
                        ArticleDetailResponse.TagInfo tagInfo = new ArticleDetailResponse.TagInfo();
                        tagInfo.setId(tag.getId());
                        tagInfo.setName(tag.getName());
                        tagInfo.setColor(tag.getColor());
                        return tagInfo;
                    }
                    return null;
                })
                .filter(tag -> tag != null)
                .collect(Collectors.toList());
        response.setTags(tags);

        return response;
    }

    /**
     * 点赞文章
     */
    @Transactional
    public void likeArticle(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("文章", articleId);
        }

        // 检查是否已点赞
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, Constants.LIKE_TARGET_ARTICLE)
                .eq(Like::getTargetId, articleId);

        if (likeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已经点赞过了");
        }

        // 添加点赞记录
        Like like = new Like();
        like.setUserId(userId);
        like.setTargetType(Constants.LIKE_TARGET_ARTICLE);
        like.setTargetId(articleId);
        likeMapper.insert(like);

        // 增加点赞数
        article.setLikeCount(article.getLikeCount() + 1);
        articleMapper.updateById(article);
    }

    /**
     * 取消点赞文章
     */
    @Transactional
    public void unlikeArticle(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("文章", articleId);
        }

        // 删除点赞记录
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, Constants.LIKE_TARGET_ARTICLE)
                .eq(Like::getTargetId, articleId);

        int deleted = likeMapper.delete(wrapper);
        if (deleted > 0) {
            // 减少点赞数
            article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
            articleMapper.updateById(article);
        }
    }

    /**
     * 收藏文章
     */
    @Transactional
    public void favoriteArticle(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("文章", articleId);
        }

        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId);

        if (favoriteMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已经收藏过了");
        }

        // 添加收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setArticleId(articleId);
        favoriteMapper.insert(favorite);

        // 增加收藏数
        article.setFavoriteCount(article.getFavoriteCount() + 1);
        articleMapper.updateById(article);
    }

    /**
     * 取消收藏文章
     */
    @Transactional
    public void unfavoriteArticle(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("文章", articleId);
        }

        // 删除收藏记录
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId);

        int deleted = favoriteMapper.delete(wrapper);
        if (deleted > 0) {
            // 减少收藏数
            article.setFavoriteCount(Math.max(0, article.getFavoriteCount() - 1));
            articleMapper.updateById(article);
        }
    }

    /**
     * 发布文章
     */
    @Transactional
    public void publishArticle(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("文章", articleId);
        }

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限发布此文章");
        }

        if (article.getIsPublished() == Constants.ARTICLE_STATUS_PUBLISHED) {
            throw new BusinessException("文章已发布");
        }

        article.setIsPublished(Constants.ARTICLE_STATUS_PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());
        articleMapper.updateById(article);
    }

    /**
     * 下架文章
     */
    @Transactional
    public void unpublishArticle(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("文章", articleId);
        }

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限下架此文章");
        }

        article.setIsPublished(Constants.ARTICLE_STATUS_DRAFT);
        articleMapper.updateById(article);
    }

    /**
     * 获取用户的文章列表
     */
    public PageResult<ArticleListResponse> getUserArticles(Long userId, int page, int size, String status) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getUserId, userId);

        if ("published".equals(status)) {
            wrapper.eq(Article::getIsPublished, Constants.ARTICLE_STATUS_PUBLISHED);
        } else if ("draft".equals(status)) {
            wrapper.eq(Article::getIsPublished, Constants.ARTICLE_STATUS_DRAFT);
        }

        wrapper.orderByDesc(Article::getCreatedAt);

        Page<Article> articlePage = articleMapper.selectPage(new Page<>(page, size), wrapper);

        List<ArticleListResponse> records = articlePage.getRecords().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                records,
                articlePage.getTotal(),
                articlePage.getCurrent(),
                articlePage.getSize()
        );
    }

    /**
     * 获取用户的收藏列表
     */
    public PageResult<ArticleListResponse> getUserFavorites(Long userId, int page, int size) {
        // 查询用户收藏的文章ID列表
        Page<Favorite> favoritePage = favoriteMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt)
        );

        List<ArticleListResponse> records = favoritePage.getRecords().stream()
                .map(favorite -> {
                    Article article = articleMapper.selectById(favorite.getArticleId());
                    return article != null ? convertToListResponse(article) : null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());

        return new PageResult<>(
                records,
                favoritePage.getTotal(),
                favoritePage.getCurrent(),
                favoritePage.getSize()
        );
    }

    /**
     * 获取待审核文章列表（仅管理员）
     */
    public PageResult<ArticleListResponse> getPendingArticles(int page, int size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getReviewStatus, "PENDING")
                .orderByDesc(Article::getCreatedAt);

        Page<Article> articlePage = articleMapper.selectPage(new Page<>(page, size), wrapper);

        List<ArticleListResponse> records = articlePage.getRecords().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                records,
                articlePage.getTotal(),
                articlePage.getCurrent(),
                articlePage.getSize()
        );
    }

    /**
     * 批准文章
     */
    @Transactional
    public void approveArticle(Long articleId, Long reviewerId, String note) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("文章", articleId);
        }

        if (!"PENDING".equals(article.getReviewStatus())) {
            throw new BusinessException("文章不在待审核状态");
        }

        article.setReviewStatus("APPROVED");
        article.setReviewerId(reviewerId);
        article.setReviewedAt(LocalDateTime.now());
        article.setReviewNote(note);
        articleMapper.updateById(article);

        log.info("管理员 {} 批准了文章: {}", reviewerId, article.getTitle());
    }

    /**
     * 拒绝文章
     */
    @Transactional
    public void rejectArticle(Long articleId, Long reviewerId, String note) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("文章", articleId);
        }

        if (!"PENDING".equals(article.getReviewStatus())) {
            throw new BusinessException("文章不在待审核状态");
        }

        article.setReviewStatus("REJECTED");
        article.setReviewerId(reviewerId);
        article.setReviewedAt(LocalDateTime.now());
        article.setReviewNote(note);
        articleMapper.updateById(article);

        log.info("管理员 {} 拒绝了文章: {}, 原因: {}", reviewerId, article.getTitle(), note);
    }
}