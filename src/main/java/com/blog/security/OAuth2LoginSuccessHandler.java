package com.blog.security;

import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * OAuth2 登录成功处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            // 获取OAuth2提供商信息
            String registrationId = getRegistrationId(request);
            log.info("OAuth2 登录成功，提供商: {}", registrationId);

            // 从OAuth2用户信息中提取数据
            Map<String, Object> attributes = oauth2User.getAttributes();
            log.info("OAuth2 用户原始属性: {}", attributes);

            String email = extractEmail(attributes, registrationId);
            String name = extractName(attributes, registrationId);
            String avatar = extractAvatar(attributes, registrationId);
            String login = extractLogin(attributes, registrationId);

            log.info("OAuth2 用户信息 - Email: {}, Name: {}, Login: {}", email, name, login);

            // GitHub 可能不返回邮箱，使用login作为fallback
            if (email == null || email.trim().isEmpty()) {
                if (login != null && !login.trim().isEmpty()) {
                    email = login + "@" + registrationId + ".oauth";
                    log.warn("邮箱为空，使用生成的邮箱: {}", email);
                } else {
                    log.error("无法获取有效的用户标识");
                    throw new RuntimeException("无法获取有效的用户标识");
                }
            }

        // 查找或创建用户
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            // 创建新用户
            user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]);  // 使用邮箱前缀作为用户名
            user.setNickname(name);
            user.setAvatar(avatar);
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("创建新OAuth2用户: {}", email);
        } else {
            // 更新现有用户信息
            if (avatar != null && !avatar.isEmpty()) {
                user.setAvatar(avatar);
            }
            if (name != null && !name.isEmpty()) {
                user.setNickname(name);
            }
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
            log.info("更新OAuth2用户信息: {}", email);
        }

        // 生成JWT Token
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());

            // 重定向到前端，带上tokens
            String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/callback")
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 登录处理失败", e);
            // 重定向到错误页面
            String errorUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/login")
                    .queryParam("error", "oauth2_failed")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    private String getRegistrationId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("google")) {
            return "google";
        } else if (uri.contains("github")) {
            return "github";
        }
        return "unknown";
    }

    private String extractEmail(Map<String, Object> attributes, String registrationId) {
        if ("google".equals(registrationId)) {
            return (String) attributes.get("email");
        } else if ("github".equals(registrationId)) {
            return (String) attributes.get("email");
        }
        return null;
    }

    private String extractName(Map<String, Object> attributes, String registrationId) {
        if ("google".equals(registrationId)) {
            return (String) attributes.get("name");
        } else if ("github".equals(registrationId)) {
            return (String) attributes.get("name");
        }
        return null;
    }

    private String extractAvatar(Map<String, Object> attributes, String registrationId) {
        if ("google".equals(registrationId)) {
            return (String) attributes.get("picture");
        } else if ("github".equals(registrationId)) {
            return (String) attributes.get("avatar_url");
        }
        return null;
    }

    private String extractLogin(Map<String, Object> attributes, String registrationId) {
        if ("github".equals(registrationId)) {
            return (String) attributes.get("login");
        } else if ("google".equals(registrationId)) {
            // Google 使用 sub (subject identifier)
            return (String) attributes.get("sub");
        }
        return null;
    }
}
