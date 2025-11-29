package com.example.chain;

import com.example.config.TaskHandlerConfig;
import com.example.config.DebugConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 任务处理器工厂 - 支持配置文件化管理
 */
@Slf4j
@Component
public class TaskHandlerFactory {

    @Autowired
    @Lazy
    private ApplicationContext applicationContext;
    
    @Autowired
    private TaskHandlerConfig handlerConfig;
    
    @Autowired
    private DebugConfig debugConfig;

    private Map<String, TaskHandler> handlerMap = new HashMap<>();
    private Map<String, List<String>> typeHandlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("开始初始化TaskHandlerFactory...");
        
        // 调试模式：打印相关信息（添加空指针检查）
        if (debugConfig != null && (debugConfig.isPrintAllBeans() || debugConfig.isPrintHandlerBeans())) {
            printDebugInfo();
        }

        // 扫描并注册处理器
        scanAndRegisterHandlers();

        // 从配置文件加载任务类型映射
        loadTaskTypeMappings();

        log.info("责任链工厂初始化完成，处理器数量: {}, 任务类型数量: {}", 
                handlerMap.size(), typeHandlerMap.size());
    }
    
    /**
     * 打印调试信息
     */
    private void printDebugInfo() {
        if (debugConfig.isPrintAllBeans()) {
            String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
            log.info("Spring容器中总共有{}个bean", beanNames.length);
        }

        if (debugConfig.isPrintHandlerBeans()) {
            // 查找所有包含handler的bean
            String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
            for (String beanName : beanNames) {
                if (beanName.toLowerCase().contains("handler")) {
                    Object bean = applicationContext.getBean(beanName);
                    log.info("发现handler相关Bean: {}  类型: {}", beanName, bean.getClass().getName());
                }
            }
        }
    }
    
    /**
     * 扫描并注册处理器
     */
    private void scanAndRegisterHandlers() {
        // 获取所有TaskHandler实现
        Map<String, TaskHandler> handlers = applicationContext.getBeansOfType(TaskHandler.class);
        log.info("发现{}个TaskHandler实现类", handlers.size());

        if (handlers.isEmpty()) {
            log.error("未扫描到任何TaskHandler实现类！请检查包扫描配置或@Component注解");
            throw new IllegalStateException("未找到TaskHandler实现类");
        }

        // 注册处理器
        for (Map.Entry<String, TaskHandler> entry : handlers.entrySet()) {
            TaskHandler handler = entry.getValue();
            String handlerName = handler.getName();
            
            // 检查处理器名称是否唯一
            if (handlerMap.containsKey(handlerName)) {
                log.warn("处理器名称冲突: {}, 已存在: {}, 将被覆盖", 
                        handlerName, handlerMap.get(handlerName).getClass().getName());
            }
            
            handlerMap.put(handlerName, handler);
            log.info("注册处理器: {} ({})", handlerName, handler.getClass().getSimpleName());
        }
    }
    
    /**
     * 从配置文件加载任务类型映射
     */
    private void loadTaskTypeMappings() {
        // 添加空指针检查
        if (handlerConfig == null || handlerConfig.getTaskTypes() == null || handlerConfig.getTaskTypes().isEmpty()) {
            log.warn("未配置任务类型映射，使用默认配置");
            loadDefaultMappings();
            return;
        }
        
        for (Map.Entry<String, TaskHandlerConfig.TaskChainConfig> entry : 
                handlerConfig.getTaskTypes().entrySet()) {
            String taskType = entry.getKey();
            TaskHandlerConfig.TaskChainConfig config = entry.getValue();
            
            // 添加空指针检查
            if (config == null || config.getHandlers() == null) {
                log.warn("任务类型[{}]配置为空，已跳过", taskType);
                continue;
            }
            
            // 验证处理器是否存在
            List<String> validHandlers = new ArrayList<>();
            for (String handlerName : config.getHandlers()) {
                if (handlerMap.containsKey(handlerName)) {
                    validHandlers.add(handlerName);
                } else {
                    log.warn("任务类型[{}]配置的处理器[{}]不存在，已跳过", taskType, handlerName);
                }
            }
            
            if (validHandlers.isEmpty()) {
                log.warn("任务类型[{}]没有有效的处理器配置，已跳过", taskType);
                continue;
            }
            
            typeHandlerMap.put(taskType, validHandlers);
            log.info("配置任务类型[{}]: {} - {}", taskType, 
                    config.getDescription() != null ? config.getDescription() : "无描述", 
                    validHandlers);
        }
    }
    
    /**
     * 加载默认映射（降级方案）
     */
    private void loadDefaultMappings() {
        log.info("使用默认任务类型配置");
        
        // 检查默认处理器是否存在
        List<String> defaultHandlers = Arrays.asList(
                "CheckSubtitleHandler", "ExtractSubtitleHandler", "VideoScoreHandler");
        
        List<String> validHandlers = new ArrayList<>();
        for (String handlerName : defaultHandlers) {
            if (handlerMap.containsKey(handlerName)) {
                validHandlers.add(handlerName);
            }
        }
        
        if (!validHandlers.isEmpty()) {
            typeHandlerMap.put("VIDEO_SCORING", validHandlers);
            log.info("默认配置任务类型[VIDEO_SCORING]: {}", validHandlers);
        }
    }

    /**
     * 构建责任链
     */
    public TaskHandlerChain buildChain(String taskType) {
        List<String> handlerNames = typeHandlerMap.get(taskType);
        if (handlerNames == null || handlerNames.isEmpty()) {
            throw new IllegalArgumentException("不支持的任务类型: " + taskType + 
                    ", 支持的类型: " + typeHandlerMap.keySet());
        }

        TaskHandlerChain chain = new TaskHandlerChain();
        for (String handlerName : handlerNames) {
            TaskHandler handler = handlerMap.get(handlerName);
            if (handler != null) {
                chain.addHandler(handler);
            } else {
                log.warn("处理器[{}]不存在，跳过", handlerName);
            }
        }

        if (chain.getHandlers().isEmpty()) {
            throw new IllegalStateException("任务类型[" + taskType + "]没有可用的处理器");
        }

        chain.buildChain();
        return chain;
    }

    /**
     * 获取任务类型的处理器名称列表
     */
    public List<String> getHandlerNames(String taskType) {
        return typeHandlerMap.get(taskType);
    }
    
    /**
     * 获取所有支持的任务类型
     */
    public Set<String> getSupportedTaskTypes() {
        return new HashSet<>(typeHandlerMap.keySet());
    }
    
    /**
     * 获取所有已注册的处理器
     */
    public Map<String, TaskHandler> getRegisteredHandlers() {
        return new HashMap<>(handlerMap);
    }
}