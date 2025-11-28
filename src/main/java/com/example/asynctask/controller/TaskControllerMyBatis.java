package com.example.asynctask.controller;

import com.example.asynctask.dto.ApiResponse;
import com.example.asynctask.dto.TaskRequest;
import com.example.asynctask.dto.TaskResponse;
import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.service.AsyncTaskServiceMyBatis;
import com.example.asynctask.service.TaskExecutorService;
import com.example.asynctask.service.TaskSchedulerService;
import com.example.asynctask.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 任务控制器（MyBatis实现）
 * 
 * @author System
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
public class TaskControllerMyBatis {
    
    @Autowired
    private AsyncTaskServiceMyBatis asyncTaskService;
    
    @Autowired
    private TaskExecutorService taskExecutorService;
    
    @Autowired
    private TaskSchedulerService taskSchedulerService;
    
    /**
     * 创建任务
     */
    @PostMapping
    public ApiResponse<TaskResponse> createTask(@Validated @RequestBody TaskRequest request) {
        try {
            // 创建任务配置
            com.example.asynctask.util.JsonUtils.TaskConfig taskConfig = com.example.asynctask.util.JsonUtils.TaskConfig.builder()
                    .handlerChain(StringUtils.hasText(request.getHandlerChain()) ? 
                            java.util.Arrays.asList(request.getHandlerChain().split(",")) : null)
                    .maxRetries(request.getMaxRetries())
                    .timeoutSeconds(request.getTimeoutSeconds())
                    .priority(request.getPriority())
                    .parameters(request.getParameters())
                    .build();
            
            // 创建任务
            AsyncTask task = asyncTaskService.createTask(
                    request.getTaskType(), 
                    request.getTaskName(), 
                    request.getCreator(), 
                    taskConfig);
            
            return ApiResponse.success("任务创建成功", TaskResponse.fromEntity(task));
        } catch (Exception e) {
            log.error("创建任务失败", e);
            return ApiResponse.fail("创建任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建视频评分任务（简化接口）
     */
    @PostMapping("/video-scoring")
    public ApiResponse<TaskResponse> createVideoScoringTask(
            @RequestParam String videoId,
            @RequestParam(required = false, defaultValue = "视频评分任务") String taskName,
            @RequestParam(required = false, defaultValue = "system") String creator,
            @RequestParam(required = false, defaultValue = "5") Integer priority) {
        try {
            // 准备任务参数
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("videoId", videoId);
            
            // 创建任务配置
            JsonUtils.TaskConfig taskConfig = JsonUtils.TaskConfig.builder()
                    .handlerChain(Arrays.asList("subtitleFetch", "subtitleCheck", "scoring"))
                    .maxRetries(3)
                    .timeoutSeconds(300)
                    .priority(priority)
                    .parameters(parameters)
                    .build();
            
            // 创建任务
            AsyncTask task = asyncTaskService.createTask(
                    "VIDEO_SCORING", 
                    taskName + "-" + videoId, 
                    creator, 
                    taskConfig);
            
            return ApiResponse.success("任务创建成功", TaskResponse.fromEntity(task));
        } catch (Exception e) {
            log.error("创建视频评分任务失败", e);
            return ApiResponse.fail("创建视频评分任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    public ApiResponse<TaskResponse> getTask(@PathVariable String taskId) {
        try {
            AsyncTask task = asyncTaskService.getTask(taskId);
            if (task == null) {
                return ApiResponse.fail("任务不存在");
            }
            
            return ApiResponse.success("获取任务成功", TaskResponse.fromEntity(task));
        } catch (Exception e) {
            log.error("获取任务失败", e);
            return ApiResponse.fail("获取任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页获取任务列表
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getTasks(
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) AsyncTask.TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Map<String, Object> result = asyncTaskService.getTasksByPage(taskType, status, page, size, sortBy, sortDir);
            
            return ApiResponse.success("获取任务列表成功", result);
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return ApiResponse.fail("获取任务列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行任务
     */
    @PostMapping("/{taskId}/execute")
    public ApiResponse<String> executeTask(@PathVariable String taskId) {
        try {
            boolean success = asyncTaskService.startTask(taskId);
            if (success) {
                // 如果成功启动，交给执行器执行
                taskExecutorService.executeTask(taskId);
                return ApiResponse.success("任务已提交执行");
            } else {
                return ApiResponse.fail("任务执行失败，请检查任务状态");
            }
        } catch (Exception e) {
            log.error("执行任务失败", e);
            return ApiResponse.fail("执行任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 重试任务
     */
    @PostMapping("/{taskId}/retry")
    public ApiResponse<String> retryTask(@PathVariable String taskId) {
        try {
            boolean success = asyncTaskService.retryTask(taskId);
            if (success) {
                // 如果成功重置为PENDING，交给执行器执行
                taskExecutorService.executeTask(taskId);
                return ApiResponse.success("任务已提交重试");
            } else {
                return ApiResponse.fail("任务重试失败，请检查任务状态");
            }
        } catch (Exception e) {
            log.error("重试任务失败", e);
            return ApiResponse.fail("重试任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 暂停任务
     */
    @PostMapping("/{taskId}/pause")
    public ApiResponse<String> pauseTask(@PathVariable String taskId) {
        try {
            boolean success = asyncTaskService.pauseTask(taskId);
            if (success) {
                return ApiResponse.success("任务已暂停");
            } else {
                return ApiResponse.fail("任务暂停失败，请检查任务状态");
            }
        } catch (Exception e) {
            log.error("暂停任务失败", e);
            return ApiResponse.fail("暂停任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消任务
     */
    @PostMapping("/{taskId}/cancel")
    public ApiResponse<String> cancelTask(@PathVariable String taskId) {
        try {
            boolean success = asyncTaskService.cancelTask(taskId);
            if (success) {
                return ApiResponse.success("任务已取消");
            } else {
                return ApiResponse.fail("任务取消失败，请检查任务状态");
            }
        } catch (Exception e) {
            log.error("取消任务失败", e);
            return ApiResponse.fail("取消任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    public ApiResponse<String> deleteTask(@PathVariable String taskId) {
        try {
            AsyncTask task = asyncTaskService.getTask(taskId);
            if (task == null) {
                return ApiResponse.fail("任务不存在");
            }
            
            // 只有失败、完成或取消的任务才能删除
            if (task.getStatus() == AsyncTask.TaskStatus.PROCESSING || 
                task.getStatus() == AsyncTask.TaskStatus.PENDING) {
                return ApiResponse.fail("只能删除已完成、失败或取消的任务");
            }
            
            boolean success = asyncTaskService.deleteTask(taskId);
            if (success) {
                return ApiResponse.success("任务已删除");
            } else {
                return ApiResponse.fail("任务删除失败");
            }
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return ApiResponse.fail("删除任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动调度任务
     */
    @PostMapping("/schedule")
    public ApiResponse<String> scheduleTasks() {
        try {
            taskSchedulerService.scheduleOnce();
            return ApiResponse.success("任务调度已触发");
        } catch (Exception e) {
            log.error("调度任务失败", e);
            return ApiResponse.fail("调度任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动调度器
     */
    @PostMapping("/scheduler/start")
    public ApiResponse<String> startScheduler() {
        try {
            taskSchedulerService.startScheduler();
            return ApiResponse.success("调度器已启动");
        } catch (Exception e) {
            log.error("启动调度器失败", e);
            return ApiResponse.fail("启动调度器失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止调度器
     */
    @PostMapping("/scheduler/stop")
    public ApiResponse<String> stopScheduler() {
        try {
            taskSchedulerService.stopScheduler();
            return ApiResponse.success("调度器已停止");
        } catch (Exception e) {
            log.error("停止调度器失败", e);
            return ApiResponse.fail("停止调度器失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 统计任务状态
            Map<AsyncTask.TaskStatus, Long> statusStats = asyncTaskService.countByStatus();
            Map<String, Long> statusCountMap = new HashMap<>();
            for (Map.Entry<AsyncTask.TaskStatus, Long> entry : statusStats.entrySet()) {
                statusCountMap.put(entry.getKey().name(), entry.getValue());
            }
            result.put("statusStats", statusCountMap);
            
            // 调度器状态
            result.put("schedulerStats", taskSchedulerService.getSchedulerStats());
            
            return ApiResponse.success("获取统计信息成功", result);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ApiResponse.fail("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建Mock任务数据
     */
    @PostMapping("/mock")
    public ApiResponse<String> createMockTasks(@RequestParam(defaultValue = "10") int count) {
        try {
            List<AsyncTask> tasks = new ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                String taskType = i % 3 == 0 ? "VIDEO_SCORING" : "TEXT_PROCESSING";
                String taskName = "Mock任务-" + i;
                String creator = "mock-user";
                
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("mockData", i);
                
                // 随机设置状态
                AsyncTask.TaskStatus[] statuses = AsyncTask.TaskStatus.values();
                AsyncTask.TaskStatus status = statuses[new Random().nextInt(statuses.length)];
                
                AsyncTask task = asyncTaskService.createTask(taskType, taskName, creator, parameters);
                
                // 如果不是PENDING状态，更新任务状态
                if (status != AsyncTask.TaskStatus.PENDING) {
                    switch (status) {
                        case PROCESSING:
                            asyncTaskService.startTask(task.getTaskId());
                            break;
                        case COMPLETED:
                            asyncTaskService.startTask(task.getTaskId());
                            asyncTaskService.completeTask(task.getTaskId(), "Mock任务完成");
                            break;
                        case FAILED:
                            asyncTaskService.startTask(task.getTaskId());
                            asyncTaskService.failTask(task.getTaskId(), "Mock任务失败");
                            break;
                        case PAUSED:
                            asyncTaskService.startTask(task.getTaskId());
                            asyncTaskService.pauseTask(task.getTaskId());
                            break;
                    }
                }
                
                tasks.add(task);
            }
            
            return ApiResponse.success("成功创建 " + count + " 个Mock任务");
        } catch (Exception e) {
            log.error("创建Mock任务失败", e);
            return ApiResponse.fail("创建Mock任务失败: " + e.getMessage());
        }
    }
}