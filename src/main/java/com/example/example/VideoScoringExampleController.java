package com.example.example;

import com.example.chain.TaskContext;
import com.example.model.AsyncTask;
import com.example.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/example/video-scoring")
public class VideoScoringExampleController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * 创建视频评分任务示例
     */
    @PostMapping
    public Map<String, Object> createVideoScoringTask(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String videoId = (String) request.get("videoId");
            String videoName = (String) request.get("videoName");
            
            if (videoId == null) {
                // 如果没有提供视频ID，生成一个随机ID
                videoId = "VID_" + System.currentTimeMillis();
            }
            
            if (videoName == null) {
                videoName = "视频_" + videoId;
            }
            
            log.info("创建视频评分任务: 视频ID={}, 视频名称={}", videoId, videoName);
            
            // 创建任务上下文
            TaskContext context = new TaskContext();
            context.setTaskId("VIDEO_SCORING_" + UUID.randomUUID().toString().replace("-", ""));
            context.setTaskType("VIDEO_SCORING");
            
            // 添加任务数据
            context.setData("videoId", videoId);
            context.setData("videoName", videoName);
            
            // 创建任务
            String taskId = taskService.createTask(context);
            log.info("任务创建成功: {}", taskId);
            
            // 执行任务
            taskService.executeTaskAsync(taskId);
            log.info("任务已提交异步执行: {}", taskId);
            
            result.put("success", true);
            result.put("taskId", taskId);
            result.put("videoId", videoId);
            result.put("videoName", videoName);
            result.put("message", "视频评分任务创建成功，已提交执行");
            
        } catch (Exception e) {
            log.error("创建视频评分任务失败", e);
            result.put("success", false);
            result.put("message", "创建视频评分任务失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 查询任务状态
     */
    @GetMapping("/{taskId}/status")
    public Map<String, Object> getTaskStatus(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            AsyncTask task = taskService.getTask(taskId);
            
            if (task == null) {
                result.put("success", false);
                result.put("message", "任务不存在");
                return result;
            }
            
            result.put("success", true);
            result.put("taskId", taskId);
            result.put("status", task.getStatus());
            result.put("progress", task.getProgress());
            result.put("currentStep", task.getCurrentStep());
            result.put("startTime", task.getStartTime());
            result.put("endTime", task.getEndTime());
            result.put("errorMessage", task.getErrorMessage());
            
            // 如果任务已完成，尝试解析评分结果
            if ("COMPLETED".equals(task.getStatus()) && task.getContextData() != null) {
                // 这里应该解析contextData中的评分结果
                result.put("message", "任务已完成，评分结果已生成");
            } else if ("FAILED".equals(task.getStatus())) {
                result.put("message", "任务执行失败");
            } else {
                result.put("message", "任务正在执行中");
            }
            
        } catch (Exception e) {
            log.error("查询任务状态失败", e);
            result.put("success", false);
            result.put("message", "查询任务状态失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 创建多个视频评分任务示例
     */
    @PostMapping("/batch")
    public Map<String, Object> createBatchVideoScoringTasks(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Integer count = (Integer) request.getOrDefault("count", 3);
            
            Map<String, Object> createdTasks = new HashMap<>();
            
            for (int i = 1; i <= count; i++) {
                String videoId = "VID_" + System.currentTimeMillis() + "_" + i;
                String videoName = "示例视频_" + i;
                
                // 创建任务上下文
                TaskContext context = new TaskContext();
                context.setTaskId("VIDEO_SCORING_" + UUID.randomUUID().toString().replace("-", ""));
                context.setTaskType("VIDEO_SCORING");
                
                // 添加任务数据
                context.setData("videoId", videoId);
                context.setData("videoName", videoName);
                
                // 创建任务
                String taskId = taskService.createTask(context);
                
                // 执行任务
                taskService.executeTaskAsync(taskId);
                
                createdTasks.put(String.valueOf(i), Map.of(
                    "taskId", taskId,
                    "videoId", videoId,
                    "videoName", videoName
                ));
                
                // 稍微延迟一下，避免同时创建太多任务
                Thread.sleep(500);
            }
            
            result.put("success", true);
            result.put("count", count);
            result.put("tasks", createdTasks);
            result.put("message", "批量创建视频评分任务成功");
            
        } catch (Exception e) {
            log.error("批量创建视频评分任务失败", e);
            result.put("success", false);
            result.put("message", "批量创建视频评分任务失败: " + e.getMessage());
        }
        
        return result;
    }
}