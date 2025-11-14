package com.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取uploads目录的绝对路径
        String uploadPath = getUploadDir();

        // 配置上传文件的访问路径
        // 访问 /uploads/** 会映射到实际的uploads目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    /**
     * 获取上传目录（项目根目录下的uploads文件夹）
     */
    private String getUploadDir() {
        // 使用当前工作目录
        String workDir = System.getProperty("user.dir");
        String uploadPath = workDir + File.separator + "uploads" + File.separator;

        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return uploadPath;
    }
}
