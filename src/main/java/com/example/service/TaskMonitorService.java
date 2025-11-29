package com.example.service;

import com.example.mapper.AsyncTaskMapper;
import com.example.model.AsyncTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TaskMonitorService {
    
    @Autowired
    private AsyncTaskMapper asyncTaskMapper;
    
    // 实时统计数据
    private final Map<String, AtomicInteger> taskStatusCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> taskTypeCount = new ConcurrentHashMap<>();
    private final AtomicInteger currentActiveTasks = new AtomicInteger(0);
    private final AtomicInteger maxConcurrentTasks = new AtomicInteger(0);
    
    /**
     * 更新任务状态统计
     */
    public void updateTaskStatus(String oldStatus, String newStatus) {
        if (oldStatus != null) {
            taskStatusCount.computeIfAbsent(oldStatus, k -> new AtomicInteger(0)).decrementAndGet();
        }
        
        taskStatusCount.computeIfAbsent(newStatus, k -> new AtomicInteger(0)).incrementAndGet();
        
        // 更新活跃任务数
        if ("PROCESSING".equals(newStatus)) {
            int current = currentActiveTasks.incrementAndGet();
            maxConcurrentTasks.updateAndGet(max -> Math.max(max, current));
        } else if ("PROCESSING".equals(oldStatus)) {
            currentActiveTasks.decrementAndGet();
        }
    }
    
    /**
     * 更新任务类型统计
     */
    public void updateTaskType(String taskType) {
        taskTypeCount.computeIfAbsent(taskType, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * 定时刷新统计数据
     */
    @Scheduled(fixedDelay = 30000) // 每30秒执行一次
    public void refreshStatistics() {
        try {
            // 重置统计数据
            taskStatusCount.clear();
            taskTypeCount.clear();
            
            // 查询所有任务并统计
            List<AsyncTask> allTasks = asyncTaskMapper.selectByStatus("PENDING"); // 这里简化查询，实际应该查询所有
            
            // 统计状态分布
            Map<String, Integer> statusStats = new HashMap<>();
            statusStats.put("PENDING", 0);
            statusStats.put("PROCESSING", 0);
            statusStats.put("COMPLETED", 0);
            statusStats.put("FAILED", 0);
            
            // 更新统计
            for (Map.Entry<String, Integer> entry : statusStats.entrySet()) {
                taskStatusCount.put(entry.getKey(), new AtomicInteger(entry.getValue()));
            }
            
            log.info("任务统计刷新完成: 状态分布={}, 当前活跃任务数={}, 最大并发数={}", 
                    taskStatusCount, currentActiveTasks.get(), maxConcurrentTasks.get());
                    
        } catch (Exception e) {
            log.error("刷新统计数据异常", e);
        }
    }
    
    /**
     * 获取任务监控统计信息
     */
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 状态分布
        Map<String, Integer> statusDistribution = new HashMap<>();
        taskStatusCount.forEach((k, v) -> statusDistribution.put(k, v.get()));
        statistics.put("statusDistribution", statusDistribution);
        
        // 类型分布
        Map<String, Integer> typeDistribution = new HashMap<>();
        taskTypeCount.forEach((k, v) -> typeDistribution.put(k, v.get()));
        statistics.put("typeDistribution", typeDistribution);
        
        // 并发信息
        statistics.put("currentActiveTasks", currentActiveTasks.get());
        statistics.put("maxConcurrentTasks", maxConcurrentTasks.get());
        
        return statistics;
    }
    
    /**
     * 检查长时间运行的任务
     */
    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void checkLongRunningTasks() {
        try {
            List<AsyncTask> processingTasks = asyncTaskMapper.selectByStatus("PROCESSING");
            
            for (AsyncTask task : processingTasks) {
                if (task.getStartTime() != null) {
                    long minutes = ChronoUnit.MINUTES.between(task.getStartTime(), LocalDateTime.now());
                    int timeoutMinutes = task.getTimeoutSeconds() > 0 ? task.getTimeoutSeconds() / 60 : 30;
                    
                    if (minutes > timeoutMinutes) {
                        log.warn("检测到长时间运行任务: {}, 已运行{}分钟, 超时时间{}分钟", 
                                task.getTaskId(), minutes, timeoutMinutes);
                        
                        // 这里可以添加处理长时间运行任务的逻辑
                        // 例如：标记为超时、取消任务等
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查长时间运行任务异常", e);
        }
    }
}