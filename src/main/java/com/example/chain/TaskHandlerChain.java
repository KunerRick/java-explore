package com.example.chain;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TaskHandlerChain {
    
    private TaskHandler head;
    private List<TaskHandler> handlers = new ArrayList<>();
    
    public void addHandler(TaskHandler handler) {
        handlers.add(handler);
    }
    
    public void buildChain() {
        if (handlers.isEmpty()) {
            return;
        }
        
        head = handlers.get(0);
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }
        
        log.info("责任链构建完成，包含{}个处理器", handlers.size());
    }
    
    public HandleResult execute(TaskContext context) {
        if (head == null) {
            log.warn("责任链为空，无法执行任务: {}", context.getTaskId());
            return HandleResult.failure("责任链为空");
        }
        
        log.info("开始执行责任链，任务ID: {}, 任务类型: {}", context.getTaskId(), context.getTaskType());
        return head.handle(context);
    }
    
    public List<String> getHandlerNames() {
        List<String> names = new ArrayList<>();
        for (TaskHandler handler : handlers) {
            names.add(handler.getName());
        }
        return names;
    }
}