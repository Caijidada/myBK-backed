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
            int page, int size, Long categoryId, Long tagId) {

        // 构建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublished, Constants.ARTICLE_STATUS_PUBLISHED)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
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
        // 创建文章
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());
        article.setIsPublished(request.getIsPublished());
        article.setIsTop(0);
        article.setIsFeatured(0);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setFavoriteCount(0);

        if (request.getIsPublished() == Constants.ARTICLE_STATUS_PUBLISHED) {
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

        log.info("用户 {} 创建文章: {}", userId, article.getTitle());
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
        article.setIsPublished(request.getIsPublished());

        if (request.getIsPublished() == Constants.ARTICLE_STATUS_PUBLISHED
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
}