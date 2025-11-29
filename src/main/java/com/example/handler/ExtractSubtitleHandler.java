package com.example.handler;

import com.example.chain.AbstractTaskHandler;
import com.example.chain.HandleResult;
import com.example.chain.TaskContext;
import com.example.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExtractSubtitleHandler extends AbstractTaskHandler {
    
    @Autowired
    @Lazy
    private TaskService taskService;
    
    @Override
    protected HandleResult doHandle(TaskContext context) {
        log.info("提取字幕信息，任务ID: {}", context.getTaskId());
        
        try {
            // 检查是否已有字幕
            Boolean hasSubtitle = context.getData("hasSubtitle");
            if (hasSubtitle != null && hasSubtitle) {
                log.info("字幕已存在，跳过字幕提取，任务ID: {}", context.getTaskId());
                return HandleResult.success("字幕已存在，跳过字幕提取");
            }
            
            // 获取视频ID
            String videoId = context.getData("videoId");
            if (videoId == null) {
                return HandleResult.failure("视频ID为空");
            }
            
            // 模拟字幕提取过程
            log.info("开始提取字幕，视频ID: {}", videoId);
            
            // 模拟字幕提取延迟
            Thread.sleep(2000);
            
            // 模拟字幕提取结果
            String subtitleContent = extractSubtitle(videoId);
            
            // 保存字幕内容到上下文
            context.setData("subtitleContent", subtitleContent);
            context.setData("hasSubtitle", true);
            
            // 更新任务进度
            taskService.updateTaskProgress(
                context.getTaskId(), 50, "字幕提取完成", 
                null, null
            );
            
            // 记录步骤执行结果
            context.setStepResult(getName(), "字幕提取成功，长度: " + subtitleContent.length());
            
            return HandleResult.success("字幕提取成功");
            
        } catch (Exception e) {
            log.error("提取字幕失败，任务ID: {}", context.getTaskId(), e);
            return HandleResult.failure("提取字幕失败: " + e.getMessage());
        }
    }
    
    private String extractSubtitle(String videoId) {
        // 模拟字幕提取逻辑
        // 实际实现中可能需要调用音视频处理库或外部API
        
        // 简单模拟：生成一段示例字幕
        return "这是视频 " + videoId + " 的字幕内容。\n" +
                "第一段对话内容...\n" +
                "第二段对话内容...\n" +
                "字幕提取完成。";
    }
    
    @Override
    public String getName() {
        return "ExtractSubtitleHandler";
    }
}