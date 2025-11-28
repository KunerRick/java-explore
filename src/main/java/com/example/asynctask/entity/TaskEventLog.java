package com.example.asynctask.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 任务事件日志表实体
 * 
 * @author System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEventLog {
    
    private Long id;
    
    private String taskId;
    
    private EventType eventType;
    
    private String eventData;
    
    private String operator;
    
    private LocalDateTime createTime;
    
    /**
     * 事件类型枚举
     */
    public enum EventType {
        TASK_CREATED,     // 任务创建
        TASK_STARTED,     // 任务开始
        TASK_FINISHED,    // 任务完成
        TASK_FAILED,      // 任务失败
        STEP_STARTED,     // 步骤开始
        STEP_FINISHED,    // 步骤完成
        STEP_FAILED,      // 步骤失败
        TASK_CANCELLED    // 任务取消
    }
}