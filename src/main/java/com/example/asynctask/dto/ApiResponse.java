package com.example.asynctask.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API统一响应格式
 * 
 * @author System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 响应码
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private long timestamp;
    
    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    /**
     * 创建成功响应（带消息）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建失败响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder()
                .code(500)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建失败响应（带错误码）
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建参数错误响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .code(400)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建未找到响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .code(404)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}