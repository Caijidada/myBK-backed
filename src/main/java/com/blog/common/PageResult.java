package com.blog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 * 匹配前端 PageResponse<T> 接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long page;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 构造分页结果（自动计算总页数）
     */
    public PageResult(List<T> records, Long total, Long page, Long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }

    /**
     * 从 MyBatis Plus 的 Page 对象转换
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }
}