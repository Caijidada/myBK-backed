package com.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_LENGTH = 6;
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;

    /**
     * 发送验证码邮件
     */
    public void sendCaptcha(String toEmail) {
        // 生成6位数字验证码
        String captcha = generateCaptcha();

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("【博客系统】邮箱验证码");
            message.setText(String.format(
                "您的验证码是：%s\n\n" +
                "验证码有效期为 %d 分钟，请尽快使用。\n\n" +
                "如果这不是您的操作，请忽略此邮件。",
                captcha, CAPTCHA_EXPIRE_MINUTES
            ));

            mailSender.send(message);

            // 保存验证码到Redis，设置5分钟过期
            redisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + toEmail,
                captcha,
                CAPTCHA_EXPIRE_MINUTES,
                TimeUnit.MINUTES
            );

            log.info("验证码邮件已发送到: {}", toEmail);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 验证验证码
     */
    public boolean verifyCaptcha(String email, String captcha) {
        String key = CAPTCHA_PREFIX + email;
        String storedCaptcha = redisTemplate.opsForValue().get(key);

        if (storedCaptcha == null) {
            return false;
        }

        // 验证成功后删除验证码
        if (storedCaptcha.equals(captcha)) {
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }

    /**
     * 生成随机验证码
     */
    private String generateCaptcha() {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            captcha.append(random.nextInt(10));
        }
        return captcha.toString();
    }
}
