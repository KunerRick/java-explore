package com.example.asynctask.handler.video;

import com.example.asynctask.context.TaskContext;
import com.example.asynctask.context.TaskHandlerResult;
import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.handler.TaskHandler;
import com.example.asynctask.service.AsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 字幕获取处理器
 * 获取视频字幕内容
 * 
 * @author System
 */
@Slf4j
@Component
public class SubtitleFetchHandler extends TaskHandler {
    
    @Autowired
    private AsyncTaskService asyncTaskService;
    
    @Override
    protected TaskHandlerResult doHandle(TaskContext context) throws Exception {
        String taskId = context.getTaskId();
        log.info("执行字幕获取，任务ID: {}", taskId);
        
        // 获取视频ID
        Object videoIdObj = context.getData("videoId");
        String videoId = videoIdObj != null ? videoIdObj.toString() : null;
        if (!StringUtils.hasText(videoId)) {
            return TaskHandlerResult.failure("视频ID不能为空");
        }
        
        // 模拟获取字幕内容
        Map<String, Object> subtitleContent = fetchSubtitleContent(videoId);
        if (subtitleContent == null) {
            return TaskHandlerResult.failure("获取字幕失败");
        }
        
        // 更新任务进度
        int progress = 50;
        asyncTaskService.updateTaskProgress(taskId, progress);
        
        // 添加步骤日志
        AsyncTask.StepLog stepLog = AsyncTask.StepLog.builder()
                .stepName(getHandlerName())
                .status("SUCCESS")
                .startTime(LocalDateTime.now().toString())
                .endTime(LocalDateTime.now().toString())
                .durationMs(200L)
                .result("字幕获取完成，视频ID: " + videoId + ", 字幕语言: " + subtitleContent.get("language"))
                .build();
        
        asyncTaskService.addStepLog(taskId, stepLog);
        
        // 更新上下文数据
        context.setData("subtitleContent", subtitleContent);
        context.setData("subtitleLanguage", subtitleContent.get("language"));
        context.setData("subtitleDuration", subtitleContent.get("duration"));
        
        // 更新当前步骤
        context.setCurrentStepName(getHandlerName());
        
        // 更新任务上下文数据
        Map<String, Object> contextData = asyncTaskService.getTaskContextData(taskId);
        contextData.putAll(subtitleContent);
        asyncTaskService.updateTaskContextData(taskId, contextData);
        
        return TaskHandlerResult.success("字幕获取完成，视频ID: " + videoId + ", 字幕语言: " + subtitleContent.get("language"));
    }
    
    @Override
    protected TaskHandlerResult postHandle(TaskContext context, TaskHandlerResult result) throws Exception {
        // 记录处理器执行结果到上下文
        if (result != null) {
            context.setData(getHandlerName() + "Result", result.isSuccess() ? "success" : "failed");
            context.setData(getHandlerName() + "Message", result.getMessage());
        }
        return null; // 使用原有结果
    }
    
    @Override
    protected boolean shouldSkip(TaskContext context) {
        // 如果上下文中已经明确标识无字幕，则跳过
        Object hasSubtitleObj = context.getData("hasSubtitle");
        if (hasSubtitleObj instanceof Boolean) {
            return !((Boolean) hasSubtitleObj);
        }
        return false;
    }
    
    @Override
    protected String getSkipReason(TaskContext context) {
        return "视频无字幕，跳过字幕获取";
    }
    
    @Override
    protected TaskHandlerResult handleException(TaskContext context, Exception exception) {
        // 网络异常等可重试
        String errorMessage = "获取字幕异常: " + exception.getMessage();
        log.error(errorMessage, exception);
        
        return TaskHandlerResult.builder()
                .status(TaskHandlerResult.StepStatus.FAILED)
                .errorMessage(errorMessage)
                .exception(exception)
                .retryable(true)
                .retryDelay(2000L) // 2秒后重试
                .build();
    }
    
    /**
     * 获取字幕内容（模拟）
     * 
     * @param videoId 视频ID
     * @return 字幕内容
     */
    private Map<String, Object> fetchSubtitleContent(String videoId) {
        // 模拟获取过程
        try {
            // 模拟网络请求
            Thread.sleep(200);
            
            // 模拟字幕内容
            Random random = new Random();
            Map<String, Object> subtitleContent = new HashMap<>();
            subtitleContent.put("videoId", videoId);
            subtitleContent.put("language", "zh-CN"); // 中文
            subtitleContent.put("duration", 1800 + random.nextInt(1800)); // 30-60分钟
            subtitleContent.put("textLength", 5000 + random.nextInt(10000)); // 字数
            subtitleContent.put("hasKeyInfo", random.nextBoolean()); // 是否包含关键信息
            
            return subtitleContent;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("字幕获取被中断: {}", videoId, e);
            return null;
        }
    }
    
    @Override
    public boolean supports(String taskType) {
        // 只支持视频评分任务类型
        return "VIDEO_SCORING".equals(taskType);
    }
    
    @Override
    public String getDescription() {
        return "获取视频字幕内容，为后续评分提供数据支持";
    }
}