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
 * 字幕检查处理器
 * 检查视频是否有字幕可用
 * 
 * @author System
 */
@Slf4j
@Component
public class SubtitleCheckHandler extends TaskHandler {
    
    @Autowired
    private AsyncTaskService asyncTaskService;
    
    @Override
    protected TaskHandlerResult doHandle(TaskContext context) throws Exception {
        String taskId = context.getTaskId();
        log.info("执行字幕检查，任务ID: {}", taskId);
        
        // 获取视频ID（模拟）
        Object videoIdObj = context.getData("videoId");
        String videoId = videoIdObj != null ? videoIdObj.toString() : null;
        if (!StringUtils.hasText(videoId)) {
            // 从上下文参数中获取视频ID
            Object parametersObj = context.getData("parameters");
            if (parametersObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parameters = (Map<String, Object>) parametersObj;
                if (parameters != null) {
                    Object idObj = parameters.get("videoId");
                    videoId = idObj != null ? idObj.toString() : null;
                }
            }
        }
        
        if (!StringUtils.hasText(videoId)) {
            // 生成默认视频ID
            videoId = "video_" + System.currentTimeMillis();
            context.setData("videoId", videoId);
        }
        
        // 模拟检查字幕是否存在
        boolean hasSubtitle = checkSubtitleExists(videoId);
        
        // 更新任务进度
        int progress = 20;
        asyncTaskService.updateTaskProgress(taskId, progress);
        
        // 添加步骤日志
        AsyncTask.StepLog stepLog = AsyncTask.StepLog.builder()
                .stepName(getHandlerName())
                .status("SUCCESS")
                .startTime(LocalDateTime.now().toString())
                .endTime(LocalDateTime.now().toString())
                .durationMs(100L)
                .result("字幕检查完成，视频ID: " + videoId + ", 字幕状态: " + (hasSubtitle ? "有" : "无"))
                .build();
        
        asyncTaskService.addStepLog(taskId, stepLog);
        
        // 更新上下文数据
        context.setData("hasSubtitle", hasSubtitle);
        context.setData("subtitleCheckResult", hasSubtitle ? "字幕可用" : "字幕不可用");
        
        // 如果没有字幕，跳过后续步骤
        if (!hasSubtitle) {
            log.info("视频 {} 无字幕，跳过后续步骤", videoId);
            context.breakChain("视频无字幕，无法执行后续处理");
            return TaskHandlerResult.skip("视频无字幕，跳过后续处理");
        }
        
        // 更新当前步骤
        context.setCurrentStepName(getHandlerName());
        
        // 更新任务上下文数据
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("videoId", videoId);
        contextData.put("hasSubtitle", hasSubtitle);
        asyncTaskService.updateTaskContextData(taskId, contextData);
        
        return TaskHandlerResult.success("字幕检查完成，视频ID: " + videoId + ", 字幕可用");
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
        return "上下文中已标识视频无字幕，跳过检查";
    }
    
    /**
     * 检查字幕是否存在（模拟）
     * 
     * @param videoId 视频ID
     * @return 是否有字幕
     */
    private boolean checkSubtitleExists(String videoId) {
        // 模拟检查过程
        try {
            // 模拟网络请求或数据库查询
            Thread.sleep(100);
            
            // 模拟随机结果（80%概率有字幕）
            Random random = new Random();
            return random.nextDouble() < 0.8;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("字幕检查被中断: {}", videoId, e);
            return false;
        }
    }
    
    @Override
    public boolean supports(String taskType) {
        // 只支持视频评分任务类型
        return "VIDEO_SCORING".equals(taskType);
    }
    
    @Override
    public String getDescription() {
        return "检查视频是否有字幕可用，为后续字幕处理做准备";
    }
}