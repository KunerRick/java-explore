package com.example.asynctask.controller;

import com.example.asynctask.dto.ApiResponse;
import com.example.asynctask.dto.TaskRequest;
import com.example.asynctask.dto.TaskResponse;
import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.service.AsyncTaskServiceMyBatis;
import com.example.asynctask.service.TaskExecutorService;
import com.example.asynctask.service.TaskSchedulerService;
import com.example.asynctask.util.JsonUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 任务控制器
 * 
 * @author System
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
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
                    taskConfig.getParameters());
            
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
            
            // 创建任务
            AsyncTask task = asyncTaskService.createTask(
                    "VIDEO_SCORING", 
                    taskName + "-" + videoId, 
                    creator, 
                    parameters);
            
            // 设置优先级
            task.setPriority(priority);
            asyncTaskService.updateTaskContextData(task.getTaskId(), parameters);
            
            return ApiResponse.success("视频评分任务创建成功", TaskResponse.fromEntity(task));
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
                return ApiResponse.notFound("任务不存在: " + taskId);
            }
            
            return ApiResponse.success(TaskResponse.fromEntity(task));
        } catch (Exception e) {
            log.error("获取任务详情失败", e);
            return ApiResponse.fail("获取任务详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询任务
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status) {
        try {
            // 创建分页参数
            PageHelper.startPage(page, size);
            
            List<AsyncTask> taskList;
            
            // 根据条件查询
            if (StringUtils.hasText(taskType)) {
                taskList = asyncTaskService.getTasksByType(taskType);
            } else if (StringUtils.hasText(status)) {
                AsyncTask.TaskStatus taskStatus = AsyncTask.TaskStatus.valueOf(status.toUpperCase());
                taskList = asyncTaskService.getTasksByStatus(taskStatus);
            } else {
                taskList = asyncTaskService.getTasks();
            }
            
            // 创建分页信息
            PageInfo<AsyncTask> pageInfo = new PageInfo<>(taskList);
            
            // 转换为响应DTO
            List<TaskResponse> responseList = pageInfo.getList().stream()
                    .map(TaskResponse::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
            
            // 创建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("content", responseList);
            result.put("totalElements", pageInfo.getTotal());
            result.put("totalPages", pageInfo.getPages());
            result.put("size", pageInfo.getPageSize());
            result.put("number", pageInfo.getPageNum() - 1); // 转换为0-based
            result.put("first", pageInfo.isIsFirstPage());
            result.put("last", pageInfo.isIsLastPage());
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("分页查询任务失败", e);
            return ApiResponse.fail("分页查询任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交任务执行
     */
    @PostMapping("/{taskId}/submit")
    public ApiResponse<String> submitTask(@PathVariable String taskId) {
        try {
            boolean result = asyncTaskService.submitTask(taskId);
            if (result) {
                return ApiResponse.success("任务提交成功");
            } else {
                return ApiResponse.fail("任务提交失败");
            }
        } catch (Exception e) {
            log.error("提交任务执行失败", e);
            return ApiResponse.fail("提交任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 立即执行任务
     */
    @PostMapping("/{taskId}/execute")
    public ApiResponse<String> executeTask(@PathVariable String taskId) {
        try {
            taskExecutorService.executeTaskAsync(taskId);
            return ApiResponse.success("任务已提交执行");
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
            var result = taskExecutorService.retryTask(taskId);
            if (result != null && result.isSuccess()) {
                return ApiResponse.success("任务重试成功");
            } else {
                return ApiResponse.fail("任务重试失败: " + (result != null ? result.getErrorMessage() : "未知错误"));
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
            boolean result = asyncTaskService.pauseTask(taskId);
            if (result) {
                return ApiResponse.success("任务暂停成功");
            } else {
                return ApiResponse.fail("任务暂停失败");
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
            boolean result = asyncTaskService.cancelTask(taskId);
            if (result) {
                return ApiResponse.success("任务取消成功");
            } else {
                return ApiResponse.fail("任务取消失败");
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
            boolean result = asyncTaskService.deleteTask(taskId);
            if (result) {
                return ApiResponse.success("任务删除成功");
            } else {
                return ApiResponse.fail("任务删除失败");
            }
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return ApiResponse.fail("删除任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getTaskStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 状态统计
            Map<String, Long> statusStats = asyncTaskService.countTasksByStatus();
            stats.put("statusStats", statusStats);
            
            // 视频评分任务统计
            Map<String, Long> videoScoringStats = asyncTaskService.countTasksByStatusAndType("VIDEO_SCORING");
            stats.put("videoScoringStats", videoScoringStats);
            
            // 调度器统计
            if (taskSchedulerService.isSchedulerRunning()) {
                com.example.asynctask.service.TaskSchedulerService.SchedulerStats schedulerStats = taskSchedulerService.getSchedulerStats();
                Map<String, Object> schedulerInfo = new HashMap<>();
                schedulerInfo.put("running", schedulerStats.isRunning());
                schedulerInfo.put("totalSchedules", schedulerStats.getTotalSchedules());
                schedulerInfo.put("successTasks", schedulerStats.getSuccessTasks());
                schedulerInfo.put("failedTasks", schedulerStats.getFailedTasks());
                schedulerInfo.put("lastScheduleTime", schedulerStats.getLastScheduleTime());
                schedulerInfo.put("avgTasksPerSchedule", schedulerStats.getAvgTasksPerSchedule());
                stats.put("schedulerStats", schedulerInfo);
            } else {
                stats.put("schedulerStats", Map.of("running", false));
            }
            
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取任务统计信息失败", e);
            return ApiResponse.fail("获取任务统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发任务调度
     */
    @PostMapping("/schedule")
    public ApiResponse<String> scheduleTasks() {
        try {
            taskSchedulerService.scheduleOnce();
            return ApiResponse.success("任务调度已触发");
        } catch (Exception e) {
            log.error("手动触发任务调度失败", e);
            return ApiResponse.fail("手动触发任务调度失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动任务调度器
     */
    @PostMapping("/scheduler/start")
    public ApiResponse<String> startScheduler() {
        try {
            taskSchedulerService.startScheduler();
            return ApiResponse.success("任务调度器已启动");
        } catch (Exception e) {
            log.error("启动任务调度器失败", e);
            return ApiResponse.fail("启动任务调度器失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止任务调度器
     */
    @PostMapping("/scheduler/stop")
    public ApiResponse<String> stopScheduler() {
        try {
            taskSchedulerService.stopScheduler();
            return ApiResponse.success("任务调度器已停止");
        } catch (Exception e) {
            log.error("停止任务调度器失败", e);
            return ApiResponse.fail("停止任务调度器失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建Mock任务数据
     */
    @PostMapping("/mock")
    public ApiResponse<Map<String, Object>> createMockTasks(@RequestParam(defaultValue = "10") int count) {
        try {
            if (count < 1 || count > 100) {
                return ApiResponse.fail("Mock任务数量必须在1-100之间");
            }
            
            List<String> createdTaskIds = new ArrayList<>();
            Random random = new Random();
            
            for (int i = 0; i < count; i++) {
                // 随机生成任务参数
                String taskName = "Mock任务-" + (i + 1);
                String creator = "mock-user-" + (random.nextInt(5) + 1);
                int priority = random.nextInt(10) + 1;
                
                // 随机选择任务类型和状态
                String taskType = "VIDEO_SCORING";
                
                // 准备任务参数
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("videoId", "mock-video-" + (random.nextInt(1000) + 1));
                parameters.put("mockData", true);
                parameters.put("createdBy", "MockAPI");
                
                // 创建任务
                AsyncTask task = asyncTaskService.createTask(taskType, taskName, creator, parameters);
                task.setPriority(priority);
                
                // 随机设置任务状态
                AsyncTask.TaskStatus[] statuses = AsyncTask.TaskStatus.values();
                AsyncTask.TaskStatus randomStatus = statuses[random.nextInt(statuses.length)];
                task.setStatus(randomStatus);
                
                // 设置随机进度
                if (randomStatus == AsyncTask.TaskStatus.PROCESSING) {
                    task.setProgress(random.nextInt(100) + 1);
                } else if (randomStatus == AsyncTask.TaskStatus.COMPLETED) {
                    task.setProgress(100);
                } else {
                    task.setProgress(0);
                }
                
                // 随机设置创建和更新时间
                long now = System.currentTimeMillis();
                long randomTime = now - random.nextInt(7 * 24 * 60 * 60 * 1000); // 7天内随机时间
                task.setCreatedTime(new java.util.Date(randomTime).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
                task.setUpdatedTime(new java.util.Date(randomTime + random.nextInt(60 * 60 * 1000)).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
                
                // 保存任务
                // 使用现有的updateTaskContextData方法来保存任务状态和进度
                Map<String, Object> contextData = new HashMap<>();
                contextData.put("status", randomStatus);
                contextData.put("progress", task.getProgress());
                asyncTaskService.updateTaskContextData(task.getTaskId(), contextData);
                createdTaskIds.add(task.getTaskId());
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("createdCount", count);
            result.put("taskIds", createdTaskIds);
            result.put("message", "Mock任务数据创建成功");
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("创建Mock任务数据失败", e);
            return ApiResponse.fail("创建Mock任务数据失败: " + e.getMessage());
        }
    }
}