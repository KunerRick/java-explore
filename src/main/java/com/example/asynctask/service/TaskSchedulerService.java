package com.example.asynctask.service;

import java.util.List;

/**
 * 任务调度器服务接口
 * 负责定期扫描待处理任务并提交执行
 * 
 * @author System
 */
public interface TaskSchedulerService {
    
    /**
     * 启动调度器
     */
    void startScheduler();
    
    /**
     * 停止调度器
     */
    void stopScheduler();
    
    /**
     * 检查调度器是否正在运行
     * 
     * @return 是否正在运行
     */
    boolean isSchedulerRunning();
    
    /**
     * 手动触发一次调度
     */
    void scheduleOnce();
    
    /**
     * 调度待处理任务
     * 
     * @return 处理的任务数量
     */
    int schedulePendingTasks();
    
    /**
     * 调度指定类型的待处理任务
     * 
     * @param taskType 任务类型
     * @return 处理的任务数量
     */
    int schedulePendingTasksByType(String taskType);
    
    /**
     * 调度可重试的失败任务
     * 
     * @return 处理的任务数量
     */
    int scheduleRetryableTasks();
    
    /**
     * 处理超时任务
     * 
     * @return 处理的任务数量
     */
    int handleTimeoutTasks();
    
    /**
     * 获取调度统计信息
     * 
     * @return 统计信息
     */
    SchedulerStats getSchedulerStats();
    
    /**
     * 调度器统计信息
     */
    interface SchedulerStats {
        /**
         * 调度器是否正在运行
         */
        boolean isRunning();
        
        /**
         * 总调度次数
         */
        long getTotalSchedules();
        
        /**
         * 成功调度的任务数量
         */
        long getSuccessTasks();
        
        /**
         * 失败的任务数量
         */
        long getFailedTasks();
        
        /**
         * 上次调度时间
         */
        String getLastScheduleTime();
        
        /**
         * 平均每次调度处理的任务数量
         */
        double getAvgTasksPerSchedule();
    }
}