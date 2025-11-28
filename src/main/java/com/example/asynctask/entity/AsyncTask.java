package com.example.asynctask.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 异步任务主表实体（单表极简设计）
 * 
 * @author System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTask {
    
    private Long id;
    
    private String taskId;
    
    private String taskType;
    
    private String taskName;
    
    private TaskStatus status;
    
    private Integer progress;
    
    private String currentStep;
    
    private String taskConfig;
    
    private String contextData;
    
    private String stepLogs;
    
    private String errorMessage;
    
    private Integer maxRetries;
    
    private Integer retryCount;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer priority;
    
    private Integer timeoutSeconds;
    
    private Integer version;
    
    private String creator;
    
    private String operator;

    
    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING,      // 待处理
        PROCESSING,   // 处理中
        COMPLETED,    // 已完成
        FAILED,       // 失败
        PAUSED        // 暂停
    }
    
    /**
     * 步骤日志项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepLog {
        private String stepName;
        private String status;
        private String startTime;
        private String endTime;
        private Long durationMs;
        private String result;
        private String errorMessage;
        private Integer retryCount;
    }
}