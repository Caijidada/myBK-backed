package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.entity.Comment;
import com.blog.entity.Like;
import com.blog.exception.BusinessException;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.LikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评论服务类
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final LikeMapper likeMapper;

    /**
     * 获取评论列表
     */
    public PageResult<Comment> getCommentList(Long articleId, int page, int size) {
        Page<Comment> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getIsDeleted, 0)
                .orderByDesc(Comment::getCreatedAt);

        Page<Comment> result = commentMapper.selectPage(pageObj, wrapper);
        return PageResult.of(result);
    }

    /**
     * 创建评论
     */
    @Transactional
    public Long createComment(Long articleId, Long userId, String content, Long parentId, Long replyToId) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setReplyToId(replyToId);
        comment.setLikeCount(0);
        comment.setIsDeleted(0);

        commentMapper.insert(comment);
        return comment.getId();
    }

    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }

        // 软删除
        comment.setIsDeleted(1);
        commentMapper.updateById(comment);
    }

    /**
     * 点赞评论
     */
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 检查是否已点赞
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, "COMMENT")
                .eq(Like::getTargetId, commentId);

        if (likeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已经点赞过了");
        }

        // 添加点赞记录
        Like like = new Like();
        like.setUserId(userId);
        like.setTargetType("COMMENT");
        like.setTargetId(commentId);
        likeMapper.insert(like);

        // 增加点赞数
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentMapper.updateById(comment);
    }

    /**
     * 取消点赞评论
     */
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 删除点赞记录
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, "COMMENT")
                .eq(Like::getTargetId, commentId);

        int deleted = likeMapper.delete(wrapper);
        if (deleted > 0) {
            // 减少点赞数
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentMapper.updateById(comment);
        }
    }
}
