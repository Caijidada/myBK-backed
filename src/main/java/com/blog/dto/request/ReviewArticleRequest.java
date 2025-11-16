package com.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 文章审核请求 DTO
 */
@Data
public class ReviewArticleRequest {

    @NotBlank(message = "审核结果不能为空")
    @Pattern(regexp = "APPROVE|REJECT", message = "审核结果只能是APPROVE或REJECT")
    private String action;

    private String note; // 审核备注（可选）
}
