package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 移除敏感信息
        user.setPassword(null);
        return user;
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(Long userId, User updateData) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 只允许更新以下字段
        if (updateData.getNickname() != null) {
            user.setNickname(updateData.getNickname());
        }
        if (updateData.getAvatar() != null) {
            user.setAvatar(updateData.getAvatar());
        }
        if (updateData.getBio() != null) {
            user.setBio(updateData.getBio());
        }

        userMapper.updateById(user);
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
