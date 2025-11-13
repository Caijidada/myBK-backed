package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Constants;
import com.blog.dto.request.LoginRequest;
import com.blog.dto.request.RegisterRequest;
import com.blog.dto.response.LoginResponse;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        log.info("查询到的用户: {}", user);
        log.info("输入的密码: {}", request.getPassword());
        log.info("数据库密码: {}", user.getPassword());
        log.info("密码匹配结果: {}", passwordEncoder.matches(request.getPassword(), user.getPassword()));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == Constants.USER_STATUS_DISABLED) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成 Token
        String accessToken = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(), user.getUsername(), user.getRole()
        );

        log.info("用户 {} 登录成功", user.getUsername());


        // 返回登录响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .role(user.getRole())
                        .build())
                .build();
    }

    /**
     * 用户注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否存在
        count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, request.getEmail())
        );
        if (count > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setAvatar(Constants.DEFAULT_AVATAR);
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        user.setEmailVerified(0);

        userMapper.insert(user);

        log.info("用户 {} 注册成功", user.getUsername());
    }

    /**
     * 刷新 Token
     */
    public LoginResponse refreshToken(String refreshToken) {
        // 验证 Refresh Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(401, "无效的刷新Token");
        }

        // 从 Token 中获取用户信息
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);

        // 查询用户（确保用户仍然存在）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        // 生成新的 Access Token
        String newAccessToken = jwtTokenProvider.generateToken(userId, username, role);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 可以继续使用旧的 Refresh Token
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .role(user.getRole())
                        .build())
                .build();
    }
}