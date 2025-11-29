package com.example.controller;

import com.example.chain.TaskContext;
import com.example.chain.TaskHandlerChain;
import com.example.chain.TaskHandlerFactory;
import com.example.model.AsyncTask;
import com.example.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TaskHandlerFactory handlerFactory;
    
    /**
     * 测试创建任务
     */
    @GetMapping("/create/{taskId}")
    public Map<String, Object> testCreateTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建任务上下文
            TaskContext context = new TaskContext();
            context.setTaskId(taskId);
            context.setTaskType("VIDEO_SCORING");
            context.setData("videoId", taskId);
            context.setData("videoName", "测试视频_" + taskId);
            
            // 创建任务
            String createdTaskId = taskService.createTask(context);
            
            // 查看责任链配置
            TaskHandlerChain chain = handlerFactory.buildChain("VIDEO_SCORING");
            
            result.put("success", true);
            result.put("taskId", createdTaskId);
            result.put("handlerNames", chain.getHandlerNames());
            result.put("message", "测试任务创建成功");
        } catch (Exception e) {
            log.error("创建测试任务失败", e);
            result.put("success", false);
            result.put("message", "创建测试任务失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 测试执行任务
     */
    @GetMapping("/execute/{taskId}")
    public Map<String, Object> testExecuteTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 执行任务
            taskService.executeTaskAsync(taskId);
            
            result.put("success", true);
            result.put("taskId", taskId);
            result.put("message", "任务已提交执行");
        } catch (Exception e) {
            log.error("执行测试任务失败", e);
            result.put("success", false);
            result.put("message", "执行测试任务失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 查看任务状态
     */
    @GetMapping("/status/{taskId}")
    public Map<String, Object> testGetTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            AsyncTask task = taskService.getTask(taskId);
            
            if (task != null) {
                result.put("success", true);
                result.put("task", task);
            } else {
                result.put("success", false);
                result.put("message", "任务不存在");
            }
        } catch (Exception e) {
            log.error("查询测试任务失败", e);
            result.put("success", false);
            result.put("message", "查询测试任务失败: " + e.getMessage());
        }
        
        return result;
    }
}