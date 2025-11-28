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
 * 评分处理器
 * 基于字幕内容进行视频评分
 * 
 * @author System
 */
@Slf4j
@Component
public class ScoringHandler extends TaskHandler {
    
    @Autowired
    private AsyncTaskService asyncTaskService;
    
    @Override
    protected TaskHandlerResult doHandle(TaskContext context) throws Exception {
        String taskId = context.getTaskId();
        log.info("执行视频评分，任务ID: {}", taskId);
        
        // 获取视频ID
        Object videoIdObj = context.getData("videoId");
        String videoId = videoIdObj != null ? videoIdObj.toString() : null;
        if (!StringUtils.hasText(videoId)) {
            return TaskHandlerResult.failure("视频ID不能为空");
        }
        
        // 获取字幕内容
        Object subtitleContentObj = context.getData("subtitleContent");
        Map<String, Object> subtitleContent = null;
        if (subtitleContentObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tempContent = (Map<String, Object>) subtitleContentObj;
            subtitleContent = tempContent;
        }
        if (subtitleContent == null) {
            return TaskHandlerResult.failure("字幕内容不能为空");
        }
        
        // 执行评分
        Map<String, Object> scoringResult = performScoring(videoId, subtitleContent);
        if (scoringResult == null) {
            return TaskHandlerResult.failure("评分失败");
        }
        
        // 更新任务进度
        int progress = 100;
        asyncTaskService.updateTaskProgress(taskId, progress);
        
        // 添加步骤日志
        AsyncTask.StepLog stepLog = AsyncTask.StepLog.builder()
                .stepName(this.getClass().getSimpleName())
                .status("SUCCESS")
                .startTime(LocalDateTime.now().toString())
                .endTime(LocalDateTime.now().toString())
                .durationMs(500L)
                .result("评分完成，视频ID: " + videoId + ", 总分: " + scoringResult.get("totalScore"))
                .build();
        
        asyncTaskService.addStepLog(taskId, stepLog);
        
        // 更新上下文数据
        context.setData("scoringResult", scoringResult);
        context.setData("totalScore", scoringResult.get("totalScore"));
        
        // 更新当前步骤
        context.setCurrentStepName(this.getClass().getSimpleName());
        
        // 更新任务上下文数据
        Map<String, Object> contextData = asyncTaskService.getTaskContextData(taskId);
        contextData.put("scoringResult", scoringResult);
        asyncTaskService.updateTaskContextData(taskId, contextData);
        
        // 更新任务结果摘要
        String resultSummary = String.format("视频评分完成，总分: %.2f, 评级: %s", 
                scoringResult.get("totalScore"), scoringResult.get("grade"));
        
        return TaskHandlerResult.success(resultSummary, scoringResult);
    }
    
    @Override
    protected TaskHandlerResult postHandle(TaskContext context, TaskHandlerResult result) throws Exception {
        // 记录处理器执行结果到上下文
        if (result != null) {
            context.setData(this.getClass().getSimpleName() + "Result", result.isSuccess() ? "success" : "failed");
            context.setData(this.getClass().getSimpleName() + "Message", result.getMessage());
            
            // 如果成功，将评分结果存入上下文
            if (result.isSuccess() && result.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> scoringResult = (Map<String, Object>) result.getData();
                context.setData("finalScore", scoringResult.get("totalScore"));
                context.setData("grade", scoringResult.get("grade"));
            }
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
        return "视频无字幕，跳过评分";
    }
    
    @Override
    protected TaskHandlerResult handleException(TaskContext context, Exception exception) {
        // 评分异常通常不可重试，可能是算法问题
        String errorMessage = "评分异常: " + exception.getMessage();
        log.error(errorMessage, exception);
        
        return TaskHandlerResult.nonRetryableFailure(errorMessage, exception);
    }
    
    /**
     * 执行评分（模拟）
     * 
     * @param videoId 视频ID
     * @param subtitleContent 字幕内容
     * @return 评分结果
     */
    private Map<String, Object> performScoring(String videoId, Map<String, Object> subtitleContent) {
        // 模拟评分过程
        try {
            // 模拟评分算法计算
            Thread.sleep(500);
            
            Random random = new Random();
            
            // 字幕长度评分 (0-20分)
            Integer textLength = (Integer) subtitleContent.get("textLength");
            double lengthScore = Math.min(20, textLength / 1000.0);
            
            // 语言多样性评分 (0-20分)
            double languageScore = 15 + random.nextDouble() * 5;
            
            // 关键信息评分 (0-30分)
            Boolean hasKeyInfo = (Boolean) subtitleContent.get("hasKeyInfo");
            double keyInfoScore = hasKeyInfo ? 25 + random.nextDouble() * 5 : random.nextDouble() * 15;
            
            // 内容质量评分 (0-30分)
            double qualityScore = random.nextDouble() * 30;
            
            // 计算总分
            double totalScore = lengthScore + languageScore + keyInfoScore + qualityScore;
            
            // 确定评级
            String grade;
            if (totalScore >= 90) {
                grade = "A";
            } else if (totalScore >= 80) {
                grade = "B";
            } else if (totalScore >= 70) {
                grade = "C";
            } else if (totalScore >= 60) {
                grade = "D";
            } else {
                grade = "E";
            }
            
            // 构建评分结果
            Map<String, Object> scoringResult = new HashMap<>();
            scoringResult.put("videoId", videoId);
            scoringResult.put("totalScore", Math.round(totalScore * 100) / 100.0); // 保留两位小数
            scoringResult.put("grade", grade);
            scoringResult.put("lengthScore", Math.round(lengthScore * 100) / 100.0);
            scoringResult.put("languageScore", Math.round(languageScore * 100) / 100.0);
            scoringResult.put("keyInfoScore", Math.round(keyInfoScore * 100) / 100.0);
            scoringResult.put("qualityScore", Math.round(qualityScore * 100) / 100.0);
            
            return scoringResult;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("评分过程被中断: {}", videoId, e);
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
        return "基于字幕内容进行视频评分，包括长度、语言多样性、关键信息和内容质量等多个维度";
    }
}