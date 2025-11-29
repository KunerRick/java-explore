package com.example.handler;

import com.example.chain.AbstractTaskHandler;
import com.example.chain.HandleResult;
import com.example.chain.TaskContext;
import com.example.service.TaskService;
import com.example.annotation.TaskHandlerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@TaskHandlerInfo(
    name = "CheckSubtitleHandler",
    description = "检查视频字幕信息，确保字幕文件存在且格式正确",
    order = 1
)
public class CheckSubtitleHandler extends AbstractTaskHandler {
    
    @Autowired
    @Lazy
    private TaskService taskService;
    
    @Override
    protected HandleResult doHandle(TaskContext context) {
        log.info("检查字幕信息，任务ID: {}", context.getTaskId());
        
        try {
            // 获取视频ID
            String videoId = context.getData("videoId");
            if (videoId == null) {
                return HandleResult.failure("视频ID为空");
            }
            
            // 模拟检查字幕是否存在
            boolean hasSubtitle = checkSubtitleExists(videoId);
            
            // 保存检查结果到上下文
            context.setData("hasSubtitle", hasSubtitle);
            
            // 更新任务进度
            taskService.updateTaskProgress(
                context.getTaskId(), 10, "检查字幕信息", 
                null, null
            );
            
            // 记录步骤执行结果
            context.setStepResult(getName(), hasSubtitle ? "字幕已存在" : "字幕不存在");
            
            return HandleResult.success("字幕检查完成，" + (hasSubtitle ? "字幕已存在" : "字幕不存在"));
            
        } catch (Exception e) {
            log.error("检查字幕信息失败，任务ID: {}", context.getTaskId(), e);
            return HandleResult.failure("检查字幕信息失败: " + e.getMessage());
        }
    }
    
    private boolean checkSubtitleExists(String videoId) {
        // 模拟字幕检查逻辑
        // 实际实现中可能需要查询数据库或调用外部API
        
        // 简单模拟：偶数ID的视频有字幕
        try {
            int id = Integer.parseInt(videoId.replaceAll("[^0-9]", ""));
            return id % 2 == 0;
        } catch (NumberFormatException e) {
            // 如果无法解析为数字，默认返回false
            return false;
        }
    }
    
    @Override
    public String getName() {
        return "CheckSubtitleHandler";
    }
}