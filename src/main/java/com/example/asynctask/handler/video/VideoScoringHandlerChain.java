package com.example.asynctask.handler.video;

import com.example.asynctask.handler.TaskHandler;
import com.example.asynctask.handler.impl.DefaultTaskHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * 视频评分任务处理器链
 * 实现责任链模式：检查字幕 → 获取字幕 → 评分处理
 * 
 * @author System
 */
@Slf4j
@Component
public class VideoScoringHandlerChain {
    
    @Autowired
    private DefaultTaskHandlerFactory handlerFactory;
    
    @Autowired
    private SubtitleCheckHandler subtitleCheckHandler;
    
    @Autowired
    private SubtitleFetchHandler subtitleFetchHandler;
    
    @Autowired
    private ScoringHandler scoringHandler;
    
    /**
     * 视频评分任务处理器链
     */
    private TaskHandler videoScoringChain;
    
    /**
     * 初始化处理器链
     */
    @PostConstruct
    public void init() {
        // 构建处理器链：检查字幕 → 获取字幕 → 评分
        videoScoringChain = handlerFactory.createChain(
            Arrays.asList(
                "SubtitleCheckHandler",
                "SubtitleFetchHandler", 
                "ScoringHandler"
            )
        );
        
        if (videoScoringChain != null) {
            log.info("视频评分任务处理器链初始化完成: {} -> {} -> {}", 
                subtitleCheckHandler.getHandlerName(),
                subtitleFetchHandler.getHandlerName(),
                scoringHandler.getHandlerName());
        } else {
            log.error("视频评分任务处理器链初始化失败");
        }
    }
    
    /**
     * 获取视频评分处理器链
     * 
     * @return 处理器链
     */
    public TaskHandler getVideoScoringChain() {
        return videoScoringChain;
    }
    
    /**
     * 检查是否支持指定的任务类型
     * 
     * @param taskType 任务类型
     * @return 是否支持
     */
    public boolean supports(String taskType) {
        return "VIDEO_SCORING".equals(taskType);
    }
    
    /**
     * 获取处理器链描述
     * 
     * @return 描述信息
     */
    public String getDescription() {
        return "视频评分任务处理器链：检查字幕 → 获取字幕 → 评分处理";
    }
}