package com.blog.controller;

import com.blog.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件管理", description = "文件上传")
@RestController
@RequestMapping("/api/upload")
public class FileController {

    // 上传目录：jar包所在目录下的uploads文件夹
    private static final String UPLOAD_DIR = getUploadDir();

    // 允许的图片格式
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    // 最大文件大小 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 获取上传目录（项目根目录下的uploads文件夹）
     */
    private static String getUploadDir() {
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

    /**
     * 上传文章封面
     */
    @Operation(summary = "上传文章封面")
    @PostMapping("/cover")
    public Result<Map<String, String>> uploadCover(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "covers");
    }

    /**
     * 上传用户头像
     */
    @Operation(summary = "上传用户头像")
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "avatars");
    }

    /**
     * 上传编辑器图片
     */
    @Operation(summary = "上传编辑器图片")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "images");
    }

    /**
     * 通用图片上传方法
     */
    private Result<Map<String, String>> uploadImage(MultipartFile file, String subDir) {
        // 验证文件
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过5MB");
        }

        // 验证文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名不能为空");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        boolean isAllowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            return Result.error("只支持上传图片文件（jpg、png、gif、webp）");
        }

        try {
            // 创建上传目录（按日期分类）
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String uploadPath = UPLOAD_DIR + subDir + "/" + dateDir;
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;
            String filePath = uploadPath + "/" + filename;

            // 保存文件
            File destFile = new File(filePath);
            file.transferTo(destFile);

            // 返回访问URL
            String url = "/uploads/" + subDir + "/" + dateDir + "/" + filename;

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("filename", filename);

            log.info("文件上传成功: {}", url);
            return Result.success("上传成功", result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
