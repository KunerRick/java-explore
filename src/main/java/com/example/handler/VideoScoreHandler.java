package com.example.handler;

import com.example.chain.AbstractTaskHandler;
import com.example.chain.HandleResult;
import com.example.chain.TaskContext;
import com.example.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
@Slf4j
public class VideoScoreHandler extends AbstractTaskHandler {
    
    @Autowired
    @Lazy
    private TaskService taskService;
    
    @Override
    protected HandleResult doHandle(TaskContext context) {
        log.info("进行视频评分，任务ID: {}", context.getTaskId());
        
        try {
            // 获取视频ID
            String videoId = context.getData("videoId");
            if (videoId == null) {
                return HandleResult.failure("视频ID为空");
            }
            
            // 获取字幕内容
            String subtitleContent = context.getData("subtitleContent");
            if (subtitleContent == null || subtitleContent.isEmpty()) {
                return HandleResult.failure("字幕内容为空，无法进行评分");
            }
            
            // 模拟视频评分过程
            log.info("开始基于字幕内容进行视频评分");
            
            // 模拟评分计算延迟
            Thread.sleep(3000);
            
            // 计算评分结果
            Map<String, Object> scoreResult = calculateVideoScore(videoId, subtitleContent);
            
            // 保存评分结果到上下文
            context.getData().putAll(scoreResult);
            
            // 更新任务进度
            taskService.updateTaskProgress(
                context.getTaskId(), 100, "视频评分完成", 
                null, null
            );
            
            // 记录步骤执行结果
            context.setStepResult(getName(), "评分完成，总分: " + scoreResult.get("totalScore"));
            
            return HandleResult.success("视频评分完成");
            
        } catch (Exception e) {
            log.error("视频评分失败，任务ID: {}", context.getTaskId(), e);
            return HandleResult.failure("视频评分失败: " + e.getMessage());
        }
    }
    
    private Map<String, Object> calculateVideoScore(String videoId, String subtitleContent) {
        // 模拟视频评分逻辑
        // 实际实现中可能需要使用NLP模型分析字幕内容
        
        Random random = new Random(videoId.hashCode());
        
        // 计算各项评分
        double contentScore = 60 + random.nextDouble() * 40;    // 内容质量 60-100
        double languageScore = 60 + random.nextDouble() * 40;   // 语言表达 60-100
        double structureScore = 60 + random.nextDouble() * 40;  // 结构逻辑 60-100
        double creativityScore = 60 + random.nextDouble() * 40; // 创新性 60-100
        
        // 计算总分（加权平均）
        double totalScore = (contentScore * 0.4 + languageScore * 0.3 + 
                            structureScore * 0.2 + creativityScore * 0.1);
        
        // 确定评级
        String grade;
        if (totalScore >= 90) {
            grade = "优秀";
        } else if (totalScore >= 80) {
            grade = "良好";
        } else if (totalScore >= 70) {
            grade = "中等";
        } else if (totalScore >= 60) {
            grade = "及格";
        } else {
            grade = "不及格";
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("videoId", videoId);
        result.put("contentScore", Math.round(contentScore));
        result.put("languageScore", Math.round(languageScore));
        result.put("structureScore", Math.round(structureScore));
        result.put("creativityScore", Math.round(creativityScore));
        result.put("totalScore", Math.round(totalScore));
        result.put("grade", grade);
        result.put("subtitleLength", subtitleContent.length());
        
        return result;
    }
    
    @Override
    public String getName() {
        return "VideoScoreHandler";
    }
}