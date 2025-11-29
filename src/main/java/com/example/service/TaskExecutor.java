package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
public class TaskExecutor {
    
    @Autowired
    private TaskService taskService;
    
    // 并发控制 - 最多同时执行5个任务
    private final Semaphore semaphore = new Semaphore(5);
    
    // 异步执行任务
    @Async("asyncTaskExecutor")
    public CompletableFuture<Void> executeTaskAsync(String taskId) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 获取信号量，控制并发
                semaphore.acquire();
                log.info("开始异步执行任务: {}, 当前可用许可: {}", taskId, semaphore.availablePermits());
                
                taskService.executeTask(taskId);
            } catch (InterruptedException e) {
                log.error("任务执行被中断，任务ID: {}", taskId, e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("任务执行异常，任务ID: {}", taskId, e);
            } finally {
                // 释放信号量
                semaphore.release();
                log.info("任务执行完成或异常，释放许可，任务ID: {}, 当前可用许可: {}", taskId, semaphore.availablePermits());
            }
        });
    }
    
    // 定时扫描待处理任务
    @Scheduled(fixedDelay = 10000) // 每10秒执行一次
    public void processPendingTasks() {
        try {
            List<com.example.model.AsyncTask> pendingTasks = taskService.getPendingTasks(20);
            if (pendingTasks.isEmpty()) {
                return;
            }
            
            log.info("发现{}个待处理任务", pendingTasks.size());
            
            for (com.example.model.AsyncTask task : pendingTasks) {
                if ("PENDING".equals(task.getStatus())) {
                    // 异步执行任务
                    executeTaskAsync(task.getTaskId());
                }
            }
        } catch (Exception e) {
            log.error("处理待处理任务异常", e);
        }
    }
    
    // 定时恢复中断的任务
    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void recoverInterruptedTasks() {
        try {
            log.info("开始检查需要恢复的任务");
            taskService.recoverInterruptedTasks();
        } catch (Exception e) {
            log.error("恢复中断任务异常", e);
        }
    }
    
    // 应用启动时执行一次检查
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=== 应用启动完成，开始执行启动时检查 ===");
        
        try {
            // 1. 检查待处理任务
            List<com.example.model.AsyncTask> pendingTasks = taskService.getPendingTasks(20);
            log.info("启动检查：发现{}个待处理任务", pendingTasks.size());
            
            if (!pendingTasks.isEmpty()) {
                log.info("启动检查：开始处理待处理任务");
                for (com.example.model.AsyncTask task : pendingTasks) {
                    if ("PENDING".equals(task.getStatus())) {
                        executeTaskAsync(task.getTaskId());
                        log.info("启动检查：已提交异步执行任务: {}", task.getTaskId());
                    }
                }
            }
            
            // 2. 检查需要恢复的任务
            log.info("启动检查：检查需要恢复的任务");
            taskService.recoverInterruptedTasks();
            
            // 3. 系统状态检查
            log.info("启动检查：系统状态检查完成");
            log.info("启动检查：当前并发控制信号量可用许可: {}", semaphore.availablePermits());
            
        } catch (Exception e) {
            log.error("启动检查异常", e);
        }
        
        log.info("=== 启动检查完成 ===");
    }
}