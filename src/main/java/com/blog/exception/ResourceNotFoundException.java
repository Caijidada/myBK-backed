package com.blog.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(404, String.format("%s (ID: %d) 不存在", resourceName, id));
    }
}