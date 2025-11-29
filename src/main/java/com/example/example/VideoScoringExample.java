package com.example.example;

import com.example.chain.TaskContext;
import com.example.model.AsyncTask;
import com.example.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
// @Component // 暂时禁用自动运行示例
public class VideoScoringExample implements CommandLineRunner {
    
    @Autowired
    private TaskService taskService;
    
    @Override
    public void run(String... args) throws Exception {
        // 启动后自动运行示例
        if (args.length == 0 || !"--no-example".equals(args[0])) {
            // 添加延迟，确保应用完全启动
            Thread.sleep(5000);
            runExample();
        }
    }
    
    /**
     * 运行视频评分示例
     */
    public void runExample() {
        log.info("开始运行视频评分示例");
        
        // 创建第一个视频评分任务 - 偶数ID，有字幕
        createAndRunVideoScoringTask("1001", "示例视频-有字幕");
        
        // 等待一段时间再创建第二个任务
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 创建第二个视频评分任务 - 奇数ID，无字幕
        createAndRunVideoScoringTask("1002", "示例视频-无字幕");
        
        // 等待一段时间再创建第三个任务
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 创建第三个视频评分任务 - 偶数ID，有字幕
        createAndRunVideoScoringTask("1003", "示例视频-有字幕2");
    }
    
    /**
     * 创建并运行视频评分任务
     */
    private void createAndRunVideoScoringTask(String videoId, String videoName) {
        log.info("创建视频评分任务: 视频ID={}, 视频名称={}", videoId, videoName);
        
        // 创建任务上下文
        TaskContext context = new TaskContext();
        context.setTaskId("VIDEO_SCORING_" + UUID.randomUUID().toString().replace("-", ""));
        context.setTaskType("VIDEO_SCORING");
        
        // 添加任务数据
        context.setData("videoId", videoId);
        context.setData("videoName", videoName);
        
        try {
            // 创建任务
            String taskId = taskService.createTask(context);
            log.info("任务创建成功: {}", taskId);
            
            // 查看任务状态
            AsyncTask task = taskService.getTask(taskId);
            log.info("任务初始状态: {}", task.getStatus());
            
            // 执行任务
            taskService.executeTaskAsync(taskId);
            log.info("任务已提交异步执行: {}", taskId);
            
            // 查询任务状态
            monitorTaskStatus(taskId);
            
        } catch (Exception e) {
            log.error("创建或执行视频评分任务失败: videoId={}", videoId, e);
        }
    }
    
    /**
     * 监控任务状态变化
     */
    private void monitorTaskStatus(String taskId) {
        // 创建一个线程来监控任务状态
        Thread monitorThread = new Thread(() -> {
            String lastStatus = "";
            int noChangeCount = 0;
            
            while (true) {
                try {
                    AsyncTask task = taskService.getTask(taskId);
                    if (task == null) {
                        log.warn("任务不存在: {}", taskId);
                        break;
                    }
                    
                    String currentStatus = task.getStatus();
                    int progress = task.getProgress();
                    String currentStep = task.getCurrentStep();
                    
                    // 只在状态变化或进度变化时打印日志
                    if (!currentStatus.equals(lastStatus)) {
                        log.info("任务状态变化: {} -> {}", lastStatus, currentStatus);
                        lastStatus = currentStatus;
                        noChangeCount = 0;
                    } else {
                        noChangeCount++;
                    }
                    
                    // 每5次循环打印一次进度信息
                    if (noChangeCount % 5 == 0) {
                        log.info("任务进度: {}, 进度百分比: {}%, 当前步骤: {}", 
                                taskId, progress, currentStep != null ? currentStep : "无");
                    }
                    
                    // 如果任务已完成或失败，退出监控
                    if ("COMPLETED".equals(currentStatus) || "FAILED".equals(currentStatus)) {
                        log.info("任务完成: {}, 最终状态: {}, 进度: {}%", 
                                taskId, currentStatus, progress);
                        
                        if ("FAILED".equals(currentStatus) && task.getErrorMessage() != null) {
                            log.info("任务失败原因: {}", task.getErrorMessage());
                        }
                        
                        // 如果任务成功完成，尝试获取评分结果
                        if ("COMPLETED".equals(currentStatus)) {
                            printScoringResult(taskId);
                        }
                        
                        break;
                    }
                    
                    // 如果长时间没有变化，退出监控
                    if (noChangeCount > 60) { // 超过5分钟没有变化
                        log.warn("任务监控超时，退出监控: {}", taskId);
                        break;
                    }
                    
                    // 休眠5秒
                    TimeUnit.SECONDS.sleep(5);
                    
                } catch (InterruptedException e) {
                    log.info("任务监控被中断: {}", taskId);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("监控任务状态异常: {}", taskId, e);
                }
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.setName("TaskMonitor-" + taskId);
        monitorThread.start();
    }
    
    /**
     * 打印评分结果
     */
    private void printScoringResult(String taskId) {
        try {
            AsyncTask task = taskService.getTask(taskId);
            if (task == null || task.getContextData() == null) {
                log.info("无法获取任务评分结果: {}", taskId);
                return;
            }
            
            log.info("========== 任务 {} 评分结果 ==========", taskId);
            
            // 这里应该从contextData中解析评分结果
            // 为了简化示例，我们直接打印任务信息
            log.info("任务状态: {}", task.getStatus());
            log.info("任务进度: {}%", task.getProgress());
            log.info("当前步骤: {}", task.getCurrentStep());
            
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("打印评分结果失败: {}", taskId, e);
        }
    }
}