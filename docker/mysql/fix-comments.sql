-- 修复表备注
USE blog;

ALTER TABLE tb_user COMMENT '用户表';
ALTER TABLE tb_category COMMENT '分类表';
ALTER TABLE tb_article COMMENT '文章表';
ALTER TABLE tb_tag COMMENT '标签表';
ALTER TABLE tb_article_tag COMMENT '文章标签关联表';
ALTER TABLE tb_comment COMMENT '评论表';
ALTER TABLE tb_like COMMENT '点赞表';
ALTER TABLE tb_favorite COMMENT '收藏表';
ALTER TABLE tb_follow COMMENT '关注表';
ALTER TABLE tb_notification COMMENT '通知表';

SELECT '表备注修复完成！' AS result;
