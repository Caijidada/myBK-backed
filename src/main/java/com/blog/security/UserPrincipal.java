package com.blog.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户认证主体
 * 存储在 SecurityContext 中的用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色
     */
    private String role;
}