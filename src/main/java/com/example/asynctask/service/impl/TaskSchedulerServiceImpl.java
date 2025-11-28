package com.example.asynctask.service.impl;

import com.example.asynctask.config.AsyncTaskConfig;
import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.service.AsyncTaskService;
import com.example.asynctask.service.TaskExecutorService;
import com.example.asynctask.service.TaskSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务调度器服务实现类
 * 
 * @author System
 */
@Slf4j
@Service
public class TaskSchedulerServiceImpl implements TaskSchedulerService {
    
    @Autowired
    private AsyncTaskService asyncTaskService;
    
    @Autowired
    private TaskExecutorService taskExecutorService;
    
    @Autowired
    private AsyncTaskConfig asyncTaskConfig;
    
    /**
     * 调度器运行状态
     */
    private volatile boolean schedulerRunning = false;
    
    /**
     * 统计信息
     */
    private final AtomicLong totalSchedules = new AtomicLong(0);
    private final AtomicLong successTasks = new AtomicLong(0);
    private final AtomicLong failedTasks = new AtomicLong(0);
    private volatile String lastScheduleTime = "";
    
    @Override
    public void startScheduler() {
        if (schedulerRunning) {
            log.info("调度器已在运行");
            return;
        }
        
        schedulerRunning = true;
        log.info("任务调度器已启动");
    }
    
    @Override
    public void stopScheduler() {
        if (!schedulerRunning) {
            log.info("调度器未在运行");
            return;
        }
        
        schedulerRunning = false;
        log.info("任务调度器已停止");
    }
    
    @Override
    public boolean isSchedulerRunning() {
        return schedulerRunning;
    }
    
    @Override
    public void scheduleOnce() {
        if (!schedulerRunning) {
            log.warn("调度器未启动，无法执行调度");
            return;
        }
        
        try {
            doSchedule();
        } catch (Exception e) {
            log.error("手动调度异常", e);
        }
    }
    
    /**
     * 定时调度任务
     * 使用Spring的@Scheduled注解实现定时任务
     */
    @Scheduled(fixedDelayString = "#{@asyncTaskConfig.scheduler.pollInterval}")
    public void scheduledTask() {
        if (!schedulerRunning) {
            return;
        }
        
        try {
            doSchedule();
        } catch (Exception e) {
            log.error("定时调度异常", e);
        }
    }
    
    /**
     * 执行调度逻辑
     */
    private void doSchedule() {
        totalSchedules.incrementAndGet();
        lastScheduleTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        log.debug("开始执行任务调度");
        
        try {
            // 调度待处理任务
            int pendingTaskCount = schedulePendingTasks();
            
            // 调度可重试的失败任务
            int retryableTaskCount = scheduleRetryableTasks();
            
            // 处理超时任务
            int timeoutTaskCount = handleTimeoutTasks();
            
            log.debug("任务调度完成，待处理任务: {}, 可重试任务: {}, 超时任务: {}", 
                    pendingTaskCount, retryableTaskCount, timeoutTaskCount);
        } catch (Exception e) {
            log.error("任务调度异常", e);
        }
    }
    
    @Override
    public int schedulePendingTasks() {
        int batchSize = asyncTaskConfig.getScheduler().getBatchSize();
        
        // 获取待处理任务
        List<AsyncTask> pendingTasks = asyncTaskService.getPendingTasksWithLock(batchSize);
        if (CollectionUtils.isEmpty(pendingTasks)) {
            return 0;
        }
        
        log.info("调度 {} 个待处理任务", pendingTasks.size());
        
        // 提交任务执行
        for (AsyncTask task : pendingTasks) {
            try {
                taskExecutorService.executeTaskAsync(task.getTaskId());
                successTasks.incrementAndGet();
            } catch (Exception e) {
                log.error("提交任务执行失败: {}", task.getTaskId(), e);
                failedTasks.incrementAndGet();
            }
        }
        
        return pendingTasks.size();
    }
    
    @Override
    public int schedulePendingTasksByType(String taskType) {
        int batchSize = asyncTaskConfig.getScheduler().getBatchSize();
        
        // 获取指定类型的待处理任务
        List<AsyncTask> pendingTasks = asyncTaskService.getPendingTasksByTypeWithLock(taskType, batchSize);
        if (CollectionUtils.isEmpty(pendingTasks)) {
            return 0;
        }
        
        log.info("调度 {} 个待处理任务 (类型: {})", pendingTasks.size(), taskType);
        
        // 提交任务执行
        for (AsyncTask task : pendingTasks) {
            try {
                taskExecutorService.executeTaskAsync(task.getTaskId());
                successTasks.incrementAndGet();
            } catch (Exception e) {
                log.error("提交任务执行失败: {}", task.getTaskId(), e);
                failedTasks.incrementAndGet();
            }
        }
        
        return pendingTasks.size();
    }
    
    @Override
    public int scheduleRetryableTasks() {
        int batchSize = asyncTaskConfig.getScheduler().getBatchSize();
        
        // 获取可重试的失败任务
        List<AsyncTask> retryableTasks = asyncTaskService.getRetryableFailedTasks(batchSize);
        if (CollectionUtils.isEmpty(retryableTasks)) {
            return 0;
        }
        
        log.info("调度 {} 个可重试任务", retryableTasks.size());
        
        // 提交任务重试
        for (AsyncTask task : retryableTasks) {
            try {
                taskExecutorService.retryTask(task.getTaskId());
                successTasks.incrementAndGet();
            } catch (Exception e) {
                log.error("提交任务重试失败: {}", task.getTaskId(), e);
                failedTasks.incrementAndGet();
            }
        }
        
        return retryableTasks.size();
    }
    
    @Override
    public int handleTimeoutTasks() {
        // 获取超时任务
        List<AsyncTask> timeoutTasks = asyncTaskService.getTimeoutTasks();
        if (CollectionUtils.isEmpty(timeoutTasks)) {
            return 0;
        }
        
        log.info("处理 {} 个超时任务", timeoutTasks.size());
        
        // 处理超时任务（标记为失败）
        for (AsyncTask task : timeoutTasks) {
            try {
                asyncTaskService.failTask(task.getTaskId(), "任务执行超时");
                successTasks.incrementAndGet();
            } catch (Exception e) {
                log.error("处理超时任务失败: {}", task.getTaskId(), e);
                failedTasks.incrementAndGet();
            }
        }
        
        return timeoutTasks.size();
    }
    
    @Override
    public SchedulerStats getSchedulerStats() {
        return new SchedulerStatsImpl();
    }
    
    /**
     * 调度器统计信息实现
     */
    private class SchedulerStatsImpl implements SchedulerStats {
        @Override
        public boolean isRunning() {
            return schedulerRunning;
        }
        
        @Override
        public long getTotalSchedules() {
            return totalSchedules.get();
        }
        
        @Override
        public long getSuccessTasks() {
            return successTasks.get();
        }
        
        @Override
        public long getFailedTasks() {
            return failedTasks.get();
        }
        
        @Override
        public String getLastScheduleTime() {
            return lastScheduleTime;
        }
        
        @Override
        public double getAvgTasksPerSchedule() {
            long total = totalSchedules.get();
            if (total == 0) {
                return 0;
            }
            return (double) (successTasks.get() + failedTasks.get()) / total;
        }
    }
}