package com.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 博客系统后端启动类
 *
 * @author CaiJi
 * @date 2025 11 13
 */
@SpringBootApplication
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
        System.out.println("\n====================================");
        System.out.println("博客后端系统启动成功！");
        System.out.println("接口文档: http://localhost:8080/swagger-ui.html");
        System.out.println("====================================\n");
    }


}