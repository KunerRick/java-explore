package com.example.controller;

import com.example.chain.TaskContext;
import com.example.model.AsyncTask;
import com.example.service.TaskMonitorService;
import com.example.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TaskMonitorService taskMonitorService;
    
    /**
     * 创建任务
     */
    @PostMapping
    public Map<String, Object> createTask(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String taskId = (String) request.get("taskId");
            String taskType = (String) request.get("taskType");
            
            if (taskId == null || taskType == null) {
                result.put("success", false);
                result.put("message", "任务ID和类型不能为空");
                return result;
            }
            
            TaskContext context = new TaskContext();
            context.setTaskId(taskId);
            context.setTaskType(taskType);
            
            // 添加额外数据
            Map<String, Object> data = (Map<String, Object>) request.get("data");
            if (data != null) {
                context.getData().putAll(data);
            }
            
            String createdTaskId = taskService.createTask(context);
            
            result.put("success", true);
            result.put("taskId", createdTaskId);
            result.put("message", "任务创建成功");
        } catch (Exception e) {
            log.error("创建任务失败", e);
            result.put("success", false);
            result.put("message", "创建任务失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 执行任务
     */
    @PostMapping("/{taskId}/execute")
    public Map<String, Object> executeTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            taskService.executeTaskAsync(taskId);
            
            result.put("success", true);
            result.put("message", "任务已提交执行");
        } catch (Exception e) {
            log.error("执行任务失败", e);
            result.put("success", false);
            result.put("message", "执行任务失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    public Map<String, Object> getTask(@PathVariable String taskId) {
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
            log.error("获取任务失败", e);
            result.put("success", false);
            result.put("message", "获取任务失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取待处理任务列表
     */
    @GetMapping("/pending")
    public Map<String, Object> getPendingTasks(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<AsyncTask> tasks = taskService.getPendingTasks(limit);
            
            result.put("success", true);
            result.put("tasks", tasks);
        } catch (Exception e) {
            log.error("获取待处理任务失败", e);
            result.put("success", false);
            result.put("message", "获取待处理任务失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取任务监控统计
     */
    @GetMapping("/statistics")
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> statistics = taskMonitorService.getTaskStatistics();
            
            result.put("success", true);
            result.put("data", statistics);
        } catch (Exception e) {
            log.error("获取任务统计失败", e);
            result.put("success", false);
            result.put("message", "获取任务统计失败: " + e.getMessage());
        }
        
        return result;
    }
}