package com.example.asynctask.context;

import com.example.asynctask.entity.AsyncTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务上下文
 * 用于在责任链中传递数据和状态
 * 
 * @author System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskContext {
    
    /**
     * 任务对象
     */
    private AsyncTask task;
    
    /**
     * 当前执行的步骤名称
     */
    private String currentStepName;
    
    /**
     * 上下文数据，用于步骤间共享数据
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();
    
    /**
     * 是否中断执行链
     */
    @Builder.Default
    private boolean breakChain = false;
    
    /**
     * 中断原因
     */
    private String breakReason;
    
    /**
     * 是否跳过当前步骤
     */
    @Builder.Default
    private boolean skipCurrent = false;
    
    /**
     * 跳过原因
     */
    private String skipReason;
    
    /**
     * 设置上下文数据
     * 
     * @param key 键
     * @param value 值
     * @return 当前上下文对象
     */
    public TaskContext setData(String key, Object value) {
        data.put(key, value);
        return this;
    }
    
    /**
     * 获取上下文数据
     * 
     * @param key 键
     * @param <T> 值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) data.get(key);
    }
    
    /**
     * 获取上下文数据，如果不存在则返回默认值
     * 
     * @param key 键
     * @param defaultValue 默认值
     * @param <T> 值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, T defaultValue) {
        Object value = data.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * 检查是否包含指定键的数据
     * 
     * @param key 键
     * @return 是否包含
     */
    public boolean containsData(String key) {
        return data.containsKey(key);
    }
    
    /**
     * 移除上下文数据
     * 
     * @param key 键
     * @return 被移除的值
     */
    public Object removeData(String key) {
        return data.remove(key);
    }
    
    /**
     * 清空上下文数据
     * 
     * @return 当前上下文对象
     */
    public TaskContext clearData() {
        data.clear();
        return this;
    }
    
    /**
     * 中断执行链
     * 
     * @param reason 中断原因
     * @return 当前上下文对象
     */
    public TaskContext breakChain(String reason) {
        this.breakChain = true;
        this.breakReason = reason;
        return this;
    }
    
    /**
     * 跳过当前步骤
     * 
     * @param reason 跳过原因
     * @return 当前上下文对象
     */
    public TaskContext skipCurrent(String reason) {
        this.skipCurrent = true;
        this.skipReason = reason;
        return this;
    }
    
    /**
     * 重置中断和跳过状态
     * 
     * @return 当前上下文对象
     */
    public TaskContext reset() {
        this.breakChain = false;
        this.breakReason = null;
        this.skipCurrent = false;
        this.skipReason = null;
        return this;
    }
    
    /**
     * 获取任务ID
     * 
     * @return 任务ID
     */
    public String getTaskId() {
        return task != null ? task.getTaskId() : null;
    }
    
    /**
     * 获取任务类型
     * 
     * @return 任务类型
     */
    public String getTaskType() {
        return task != null ? task.getTaskType() : null;
    }
    
    /**
     * 获取当前步骤名称
     * 
     * @return 步骤名称
     */
    public String getCurrentStepName() {
        return currentStepName != null ? currentStepName : 
               (task != null ? task.getCurrentStep() : null);
    }
    
    /**
     * 设置当前步骤名称
     * 
     * @param stepName 步骤名称
     * @return 当前上下文对象
     */
    public TaskContext setCurrentStepName(String stepName) {
        this.currentStepName = stepName;
        if (task != null) {
            task.setCurrentStep(stepName);
        }
        return this;
    }
}