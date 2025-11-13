package com.blog.security;

import com.blog.common.Constants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 工具类
 * 适配 JJWT 0.12.x 新版本 API
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Access Token
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.TOKEN_CLAIM_USER_ID, userId);
        claims.put(Constants.TOKEN_CLAIM_USERNAME, username);
        claims.put(Constants.TOKEN_CLAIM_ROLE, role);

        return createToken(claims, expiration);
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.TOKEN_CLAIM_USER_ID, userId);
        claims.put(Constants.TOKEN_CLAIM_USERNAME, username);
        claims.put(Constants.TOKEN_CLAIM_ROLE, role);

        return createToken(claims, refreshExpiration);
    }

    /**
     * 创建 Token（使用新版本 API）
     */
    private String createToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)                    // 新版本：claims() 替代 setClaims()
                .issuedAt(now)                     // 新版本：issuedAt() 替代 setIssuedAt()
                .expiration(expiryDate)            // 新版本：expiration() 替代 setExpiration()
                .signWith(getSigningKey())         // 新版本：signWith(key) 自动选择算法
                .compact();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userIdObj = claims.get(Constants.TOKEN_CLAIM_USER_ID);
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            }
        }
        return null;
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get(Constants.TOKEN_CLAIM_USERNAME, String.class) : null;
    }

    /**
     * 从 Token 中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get(Constants.TOKEN_CLAIM_ROLE, String.class) : null;
    }

    /**
     * 从 Token 中获取 Claims（使用新版本 API）
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()                   // 新版本：parser() 替代 parserBuilder()
                    .verifyWith(getSigningKey())   // 新版本：verifyWith() 替代 setSigningKey()
                    .build()
                    .parseSignedClaims(token)      // 新版本：parseSignedClaims() 替代 parseClaimsJws()
                    .getPayload();                 // 新版本：getPayload() 替代 getBody()
        } catch (JwtException e) {
            log.error("解析 Token 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Token 是否有效（使用新版本 API）
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("无效的 JWT 签名");
        } catch (MalformedJwtException e) {
            log.error("无效的 JWT token");
        } catch (ExpiredJwtException e) {
            log.error("JWT token 已过期");
        } catch (UnsupportedJwtException e) {
            log.error("不支持的 JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims 字符串为空");
        }
        return false;
    }

    /**
     * 获取 Token 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 判断 Token 是否已过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }
}