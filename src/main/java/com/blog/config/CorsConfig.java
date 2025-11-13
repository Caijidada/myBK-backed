package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * 允许前端 http://localhost:3000 访问
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的域名（开发环境）
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://127.0.0.1:3000");

        // 生产环境需要修改为实际域名
        // config.addAllowedOrigin("https://yourdomain.com");

        // 允许所有请求方法
        config.addAllowedMethod("*");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许携带凭证（如 Cookie）
        config.setAllowCredentials(true);

        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        // 暴露的响应头
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}