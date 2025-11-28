package com.example.asynctask.context;

import com.example.asynctask.entity.AsyncTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务处理器执行结果
 * 
 * @author System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskHandlerResult {
    
    /**
     * 步骤状态枚举
     */
    public enum StepStatus {
        PENDING,    // 待处理
        RUNNING,    // 运行中
        SUCCESS,    // 成功
        FAILED,     // 失败
        SKIPPED     // 跳过
    }
    
    /**
     * 步骤状态
     */
    private StepStatus status;
    
    /**
     * 处理结果信息
     */
    private String message;
    
    /**
     * 处理结果数据
     */
    private Object data;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 异常对象
     */
    private Throwable exception;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;
    
    /**
     * 是否可以重试
     */
    @Builder.Default
    private boolean retryable = true;
    
    /**
     * 重试间隔（毫秒）
     */
    @Builder.Default
    private Long retryDelay = 1000L;
    
    /**
     * 创建成功结果
     * 
     * @param message 结果信息
     * @return 结果对象
     */
    public static TaskHandlerResult success(String message) {
        return TaskHandlerResult.builder()
                .status(StepStatus.SUCCESS)
                .message(message)
                .build();
    }
    
    /**
     * 创建成功结果（带数据）
     * 
     * @param message 结果信息
     * @param data 结果数据
     * @return 结果对象
     */
    public static TaskHandlerResult success(String message, Object data) {
        return TaskHandlerResult.builder()
                .status(StepStatus.SUCCESS)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 创建失败结果
     * 
     * @param errorMessage 错误信息
     * @return 结果对象
     */
    public static TaskHandlerResult failure(String errorMessage) {
        return TaskHandlerResult.builder()
                .status(StepStatus.FAILED)
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * 创建失败结果（带异常）
     * 
     * @param errorMessage 错误信息
     * @param exception 异常对象
     * @return 结果对象
     */
    public static TaskHandlerResult failure(String errorMessage, Throwable exception) {
        return TaskHandlerResult.builder()
                .status(StepStatus.FAILED)
                .errorMessage(errorMessage)
                .exception(exception)
                .build();
    }
    
    /**
     * 创建跳过结果
     * 
     * @param reason 跳过原因
     * @return 结果对象
     */
    public static TaskHandlerResult skip(String reason) {
        return TaskHandlerResult.builder()
                .status(StepStatus.SKIPPED)
                .message(reason)
                .build();
    }
    
    /**
     * 创建不可重试的失败结果
     * 
     * @param errorMessage 错误信息
     * @return 结果对象
     */
    public static TaskHandlerResult nonRetryableFailure(String errorMessage) {
        return TaskHandlerResult.builder()
                .status(StepStatus.FAILED)
                .errorMessage(errorMessage)
                .retryable(false)
                .build();
    }
    
    /**
     * 创建不可重试的失败结果（带异常）
     * 
     * @param errorMessage 错误信息
     * @param exception 异常对象
     * @return 结果对象
     */
    public static TaskHandlerResult nonRetryableFailure(String errorMessage, Throwable exception) {
        return TaskHandlerResult.builder()
                .status(StepStatus.FAILED)
                .errorMessage(errorMessage)
                .exception(exception)
                .retryable(false)
                .build();
    }
    
    /**
     * 检查是否成功
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return StepStatus.SUCCESS.equals(status);
    }
    
    /**
     * 检查是否失败
     * 
     * @return 是否失败
     */
    public boolean isFailed() {
        return StepStatus.FAILED.equals(status);
    }
    
    /**
     * 检查是否跳过
     * 
     * @return 是否跳过
     */
    public boolean isSkipped() {
        return StepStatus.SKIPPED.equals(status);
    }
    
    /**
     * 检查是否可以重试
     * 
     * @return 是否可以重试
     */
    public boolean isRetryable() {
        return isFailed() && retryable;
    }
}