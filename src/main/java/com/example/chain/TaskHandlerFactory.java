package com.example.chain;

import com.example.handler.CheckSubtitleHandler;
import com.example.handler.ExtractSubtitleHandler;
import com.example.handler.VideoScoreHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TaskHandlerFactory {
    
    @Autowired
    @Lazy
    private ApplicationContext applicationContext;
    
    private Map<String, TaskHandler> handlerMap = new HashMap<>();
    private Map<String, List<String>> typeHandlerMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        log.info("开始初始化TaskHandlerFactory...");
        
        // 打印所有bean信息，用于调试
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        log.info("Spring容器中总共有{}个bean", beanNames.length);
        
        // 查找所有包含handler的bean
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("handler")) {
                Object bean = applicationContext.getBean(beanName);
                log.info("发现handler相关Bean: {}  类型: {}", beanName, bean.getClass().getName());
            }
        }
        
        // 初始化处理器映射
        Map<String, TaskHandler> handlers = applicationContext.getBeansOfType(TaskHandler.class);
        log.info("发现{}个TaskHandler实现类", handlers.size());
        
        // 打印发现的TaskHandler
        for (Map.Entry<String, TaskHandler> entry : handlers.entrySet()) {
            log.info("TaskHandler bean: {} -> {}", entry.getKey(), entry.getValue().getClass().getName());
        }
        
        // 如果扫描不到处理器，手动创建并注册
        if (handlers.isEmpty()) {
            log.warn("扫描不到处理器，尝试手动创建...");
            
            // 获取TaskService bean
            com.example.service.TaskService taskService = null;
            try {
                taskService = applicationContext.getBean(com.example.service.TaskService.class);
                log.info("成功获取TaskService bean: {}", taskService.getClass().getName());
            } catch (Exception e) {
                log.error("无法获取TaskService bean", e);
            }
            
            // 手动创建处理器实例
            CheckSubtitleHandler checkSubtitleHandler = new CheckSubtitleHandler();
            ExtractSubtitleHandler extractSubtitleHandler = new ExtractSubtitleHandler();
            VideoScoreHandler videoScoreHandler = new VideoScoreHandler();
            
            // 手动注入依赖
            if (taskService != null) {
                try {
                    // 使用反射设置依赖
                    java.lang.reflect.Field field;
                    
                    field = CheckSubtitleHandler.class.getDeclaredField("taskService");
                    field.setAccessible(true);
                    field.set(checkSubtitleHandler, taskService);
                    log.info("成功为CheckSubtitleHandler注入TaskService");
                    
                    field = ExtractSubtitleHandler.class.getDeclaredField("taskService");
                    field.setAccessible(true);
                    field.set(extractSubtitleHandler, taskService);
                    log.info("成功为ExtractSubtitleHandler注入TaskService");
                    
                    field = VideoScoreHandler.class.getDeclaredField("taskService");
                    field.setAccessible(true);
                    field.set(videoScoreHandler, taskService);
                    log.info("成功为VideoScoreHandler注入TaskService");
                    
                } catch (Exception e) {
                    log.error("手动注入依赖失败", e);
                }
            } else {
                log.error("TaskService为null，无法注入依赖");
            }
            
            // 注册处理器
            handlerMap.put("CheckSubtitleHandler", checkSubtitleHandler);
            handlerMap.put("ExtractSubtitleHandler", extractSubtitleHandler);
            handlerMap.put("VideoScoreHandler", videoScoreHandler);
        } else {
            // 正常注册扫描到的处理器
            for (Map.Entry<String, TaskHandler> entry : handlers.entrySet()) {
                String handlerName = entry.getValue().getName();
                log.info("注册处理器: {} -> {}", handlerName, entry.getKey());
                handlerMap.put(handlerName, entry.getValue());
            }
        }
        
        // 配置任务类型对应的处理器链
        // 视频评分任务链
        typeHandlerMap.put("VIDEO_SCORING", List.of(
            "CheckSubtitleHandler",
            "ExtractSubtitleHandler", 
            "VideoScoreHandler"
        ));
        
        log.info("责任链工厂初始化完成，处理器数量: {}", handlerMap.size());
    }
    
    public TaskHandlerChain buildChain(String taskType) {
        TaskHandlerChain chain = new TaskHandlerChain();
        
        List<String> handlerNames = typeHandlerMap.get(taskType);
        if (handlerNames == null) {
            throw new IllegalArgumentException("不支持的任务类型: " + taskType);
        }
        
        for (String handlerName : handlerNames) {
            TaskHandler handler = handlerMap.get(handlerName);
            if (handler != null) {
                chain.addHandler(handler);
            }
        }
        
        chain.buildChain();
        return chain;
    }
    
    public List<String> getHandlerNames(String taskType) {
        return typeHandlerMap.get(taskType);
    }
}