package com.example.asynctask.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 任务明细表实体
 * 
 * @author System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetail {
    
    private Long id;
    
    private String taskId;
    
    private String stepName;
    
    private Integer stepOrder;
    
    private StepStatus stepStatus;
    
    private String stepResult;
    
    private String contextData;
    
    private String errorMessage;
    
    private Integer retryCount;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Long durationMs;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
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
}