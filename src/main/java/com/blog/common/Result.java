package com.blog.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一返回结果
 * 匹配前端 Response<T> 接口
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    // 成功状态码
    public static final Integer SUCCESS_CODE = 200;
    // 失败状态码
    public static final Integer ERROR_CODE = 400;
    // 未认证
    public static final Integer UNAUTHORIZED_CODE = 401;
    // 无权限
    public static final Integer FORBIDDEN_CODE = 403;
    // 未找到
    public static final Integer NOT_FOUND_CODE = 404;

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回（自定义状态码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 未认证
     */
    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<>();
        result.setCode(UNAUTHORIZED_CODE);
        result.setMessage(message);
        return result;
    }

    /**
     * 无权限
     */
    public static <T> Result<T> forbidden(String message) {
        Result<T> result = new Result<>();
        result.setCode(FORBIDDEN_CODE);
        result.setMessage(message);
        return result;
    }

    /**
     * 资源未找到
     */
    public static <T> Result<T> notFound(String message) {
        Result<T> result = new Result<>();
        result.setCode(NOT_FOUND_CODE);
        result.setMessage(message);
        return result;
    }
}