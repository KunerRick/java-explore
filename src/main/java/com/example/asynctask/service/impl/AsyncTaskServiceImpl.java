package com.example.asynctask.service.impl;

import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.repository.AsyncTaskRepository;
import com.example.asynctask.service.AsyncTaskService;
import com.example.asynctask.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 异步任务服务实现类
 * 
 * @author System
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AsyncTaskServiceImpl implements AsyncTaskService {
    
    @Autowired
    private AsyncTaskRepository asyncTaskRepository;
    
    @Override
    @Transactional
    public AsyncTask createTask(String taskType, String taskName, String creator) {
        return createTask(taskType, taskName, creator, new HashMap<>());
    }
    
    @Override
    @Transactional
    public AsyncTask createTask(String taskType, String taskName, String creator, Map<String, Object> parameters) {
        // 创建默认任务配置
        JsonUtils.TaskConfig taskConfig = JsonUtils.TaskConfig.builder()
                .handlerChain(getDefaultHandlerChain(taskType))
                .maxRetries(3)
                .timeoutSeconds(300)
                .priority(5)
                .parameters(parameters != null ? parameters : new HashMap<>())
                .build();
        
        return createTask(taskType, taskName, creator, taskConfig);
    }
    
    @Override
    @Transactional
    public AsyncTask createTask(String taskType, String taskName, String creator, JsonUtils.TaskConfig taskConfig) {
        if (!StringUtils.hasText(taskType)) {
            throw new IllegalArgumentException("任务类型不能为空");
        }
        
        if (!StringUtils.hasText(creator)) {
            throw new IllegalArgumentException("创建人不能为空");
        }
        
        // 如果没有提供任务配置，创建默认配置
        if (taskConfig == null) {
            taskConfig = JsonUtils.TaskConfig.builder()
                    .handlerChain(getDefaultHandlerChain(taskType))
                    .maxRetries(3)
                    .timeoutSeconds(300)
                    .priority(5)
                    .parameters(new HashMap<>())
                    .build();
        }
        
        // 生成任务ID
        String taskId = generateTaskId(taskType);
        
        // 创建任务
        AsyncTask task = AsyncTask.builder()
                .taskId(taskId)
                .taskType(taskType)
                .taskName(taskName)
                .creator(creator)
                .status(AsyncTask.TaskStatus.PENDING)
                .progress(0)
                .maxRetries(taskConfig.getMaxRetries())
                .retryCount(0)
                .priority(taskConfig.getPriority())
                .timeoutSeconds(taskConfig.getTimeoutSeconds())
                .taskConfig(JsonUtils.toJsonString(taskConfig))
                .contextData(JsonUtils.toJsonString(new HashMap<String, Object>()))
                .stepLogs(JsonUtils.toJsonString(new ArrayList<AsyncTask.StepLog>()))
                .build();
        
        return asyncTaskRepository.save(task);
    }
    
    @Override
    public AsyncTask getTask(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        
        Optional<AsyncTask> optional = asyncTaskRepository.findByTaskId(taskId);
        return optional.orElse(null);
    }
    
    @Override
    public AsyncTask getTaskWithLock(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        
        Optional<AsyncTask> optional = asyncTaskRepository.findByTaskIdWithLock(taskId);
        return optional.orElse(null);
    }
    
    @Override
    public List<AsyncTask> getTasks() {
        return asyncTaskRepository.findAll();
    }
    
    @Override
    public List<AsyncTask> getTasksByType(String taskType) {
        return asyncTaskRepository.findByTaskType(taskType);
    }
    
    @Override
    public List<AsyncTask> getTasksByStatus(AsyncTask.TaskStatus status) {
        return asyncTaskRepository.findByStatus(status);
    }
    
    @Override
    @Transactional
    public boolean submitTask(String taskId) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        if (task.getStatus() != AsyncTask.TaskStatus.PENDING) {
            log.warn("任务状态不是PENDING，无法提交: {}, 状态: {}", taskId, task.getStatus());
            return false;
        }
        
        // 提交任务就是改变状态为PENDING（已经是PENDING状态，所以这里不做任何操作）
        log.info("任务已提交: {}", taskId);
        return true;
    }
    
    @Override
    public List<AsyncTask> getPendingTasks(int limit) {
        return asyncTaskRepository.findPendingTasks(limit);
    }
    
    @Override
    public List<AsyncTask> getPendingTasksWithLock(int limit) {
        return asyncTaskRepository.findPendingTasksWithLock(limit);
    }
    
    @Override
    public List<AsyncTask> getPendingTasksByTypeWithLock(String taskType, int limit) {
        return asyncTaskRepository.findPendingTasksByTaskTypeWithLock(taskType, limit);
    }
    
    @Override
    @Transactional
    public boolean startTask(String taskId) {
        try {
            int rows = asyncTaskRepository.updateStatusAndStepWithVersion(
                    taskId, AsyncTask.TaskStatus.PROCESSING, null, 0);
            
            if (rows > 0) {
                log.info("任务开始执行: {}", taskId);
                return true;
            } else {
                log.warn("任务开始失败，可能已被其他线程处理: {}", taskId);
                return false;
            }
        } catch (OptimisticLockingFailureException e) {
            log.warn("任务开始失败，乐观锁冲突: {}", taskId, e);
            return false;
        } catch (Exception e) {
            log.error("任务开始异常: {}", taskId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean completeTask(String taskId) {
        return completeTask(taskId, null);
    }
    
    @Override
    @Transactional
    public boolean completeTask(String taskId, String resultSummary) {
        AsyncTask task = getTaskWithLock(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        // 更新任务为完成状态
        task.setStatus(AsyncTask.TaskStatus.COMPLETED);
        task.setProgress(100);
        task.setEndTime(LocalDateTime.now());
        
        if (StringUtils.hasText(resultSummary)) {
            // 这里可以将结果摘要添加到上下文数据中，或者新增一个字段
            Map<String, Object> contextData = JsonUtils.parseContextData(task.getContextData());
            contextData.put("resultSummary", resultSummary);
            task.setContextData(JsonUtils.toJsonString(contextData));
        }
        
        asyncTaskRepository.save(task);
        log.info("任务完成: {}", taskId);
        return true;
    }
    
    @Override
    @Transactional
    public boolean failTask(String taskId, String errorMessage) {
        AsyncTask task = getTaskWithLock(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        // 更新任务为失败状态
        task.setStatus(AsyncTask.TaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        task.setEndTime(LocalDateTime.now());
        
        asyncTaskRepository.save(task);
        log.info("任务失败: {}, 错误: {}", taskId, errorMessage);
        return true;
    }
    
    @Override
    @Transactional
    public boolean pauseTask(String taskId) {
        try {
            int rows = asyncTaskRepository.updateStatusWithVersion(
                    taskId, AsyncTask.TaskStatus.PAUSED, 0);
            
            if (rows > 0) {
                log.info("任务暂停: {}", taskId);
                return true;
            } else {
                log.warn("任务暂停失败，可能已被其他线程处理: {}", taskId);
                return false;
            }
        } catch (OptimisticLockingFailureException e) {
            log.warn("任务暂停失败，乐观锁冲突: {}", taskId, e);
            return false;
        } catch (Exception e) {
            log.error("任务暂停异常: {}", taskId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean cancelTask(String taskId) {
        try {
            int rows = asyncTaskRepository.updateStatusWithVersion(
                    taskId, AsyncTask.TaskStatus.FAILED, 0);
            
            if (rows > 0) {
                log.info("任务取消: {}", taskId);
                return true;
            } else {
                log.warn("任务取消失败，可能已被其他线程处理: {}", taskId);
                return false;
            }
        } catch (OptimisticLockingFailureException e) {
            log.warn("任务取消失败，乐观锁冲突: {}", taskId, e);
            return false;
        } catch (Exception e) {
            log.error("任务取消异常: {}", taskId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateTaskProgress(String taskId, Integer progress) {
        if (progress == null || progress < 0 || progress > 100) {
            log.warn("无效的进度值: {}", progress);
            return false;
        }
        
        try {
            AsyncTask task = getTaskWithLock(taskId);
            if (task == null) {
                log.warn("任务不存在: {}", taskId);
                return false;
            }
            
            task.setProgress(progress);
            asyncTaskRepository.save(task);
            log.debug("更新任务进度: {} -> {}%", taskId, progress);
            return true;
        } catch (OptimisticLockingFailureException e) {
            log.warn("更新任务进度失败，乐观锁冲突: {}", taskId, e);
            return false;
        } catch (Exception e) {
            log.error("更新任务进度异常: {}", taskId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateTaskContextData(String taskId, Map<String, Object> contextData) {
        try {
            AsyncTask task = getTaskWithLock(taskId);
            if (task == null) {
                log.warn("任务不存在: {}", taskId);
                return false;
            }
            
            task.setContextData(JsonUtils.toJsonString(contextData));
            asyncTaskRepository.save(task);
            log.debug("更新任务上下文数据: {}", taskId);
            return true;
        } catch (OptimisticLockingFailureException e) {
            log.warn("更新任务上下文数据失败，乐观锁冲突: {}", taskId, e);
            return false;
        } catch (Exception e) {
            log.error("更新任务上下文数据异常: {}", taskId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean addStepLog(String taskId, AsyncTask.StepLog stepLog) {
        try {
            AsyncTask task = getTaskWithLock(taskId);
            if (task == null) {
                log.warn("任务不存在: {}", taskId);
                return false;
            }
            
            String newStepLogs = JsonUtils.addStepLog(task.getStepLogs(), stepLog);
            task.setStepLogs(newStepLogs);
            asyncTaskRepository.save(task);
            log.debug("添加步骤日志: {} -> {}", taskId, stepLog.getStepName());
            return true;
        } catch (OptimisticLockingFailureException e) {
            log.warn("添加步骤日志失败，乐观锁冲突: {}", taskId, e);
            return false;
        } catch (Exception e) {
            log.error("添加步骤日志异常: {}", taskId, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getTaskContextData(String taskId) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return new HashMap<>();
        }
        
        return JsonUtils.parseContextData(task.getContextData());
    }
    
    @Override
    public List<AsyncTask.StepLog> getTaskStepLogs(String taskId) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return new ArrayList<>();
        }
        
        return JsonUtils.parseStepLogs(task.getStepLogs());
    }
    
    @Override
    public JsonUtils.TaskConfig getTaskConfig(String taskId) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return null;
        }
        
        return JsonUtils.parseTaskConfig(task.getTaskConfig());
    }
    
    @Override
    public List<AsyncTask> getRetryableFailedTasks(int limit) {
        return asyncTaskRepository.findRetryableFailedTasks(limit);
    }
    
    @Override
    @Transactional
    public boolean retryTask(String taskId) {
        try {
            int rows = asyncTaskRepository.incrementRetryCountWithVersion(taskId, 0);
            if (rows > 0) {
                rows = asyncTaskRepository.updateStatusWithVersion(
                        taskId, AsyncTask.TaskStatus.PENDING, 0);
                
                if (rows > 0) {
                    log.info("任务重试: {}", taskId);
                    return true;
                }
            }
            
            log.warn("任务重试失败: {}", taskId);
            return false;
        } catch (OptimisticLockingFailureException e) {
            log.warn("任务重试失败，乐观锁冲突: {}", taskId, e);
            return false;
        } catch (Exception e) {
            log.error("任务重试异常: {}", taskId, e);
            return false;
        }
    }
    
    @Override
    public List<AsyncTask> getTimeoutTasks() {
        return asyncTaskRepository.findTimeoutTasks(AsyncTask.TaskStatus.PROCESSING, LocalDateTime.now());
    }
    
    @Override
    public Map<String, Long> countTasksByStatus() {
        List<Object[]> results = asyncTaskRepository.countByStatus();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> ((AsyncTask.TaskStatus) result[0]).name(),
                        result -> (Long) result[1]
                ));
    }
    
    @Override
    public Map<String, Long> countTasksByStatusAndType(String taskType) {
        List<Object[]> results = asyncTaskRepository.countByStatusAndTaskType(taskType);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> ((AsyncTask.TaskStatus) result[0]).name(),
                        result -> (Long) result[1]
                ));
    }
    
    @Override
    @Transactional
    public boolean deleteTask(String taskId) {
        try {
            AsyncTask task = getTask(taskId);
            if (task == null) {
                log.warn("任务不存在: {}", taskId);
                return false;
            }
            
            // 只有已完成的任务才能删除
            if (task.getStatus() != AsyncTask.TaskStatus.COMPLETED && 
                task.getStatus() != AsyncTask.TaskStatus.FAILED) {
                log.warn("任务状态不允许删除: {}, 状态: {}", taskId, task.getStatus());
                return false;
            }
            
            asyncTaskRepository.delete(task);
            log.info("任务已删除: {}", taskId);
            return true;
        } catch (Exception e) {
            log.error("删除任务异常: {}", taskId, e);
            return false;
        }
    }
    
    /**
     * 生成任务ID
     * 
     * @param taskType 任务类型
     * @return 任务ID
     */
    private String generateTaskId(String taskType) {
        return taskType.toLowerCase() + "_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 1000);
    }
    
    /**
     * 获取默认处理器链
     * 
     * @param taskType 任务类型
     * @return 处理器链
     */
    private List<String> getDefaultHandlerChain(String taskType) {
        // 根据任务类型返回默认的处理器链
        switch (taskType.toUpperCase()) {
            case "VIDEO_SCORING":
                return List.of("SubtitleCheckHandler", "SubtitleFetchHandler", "ScoringHandler");
            default:
                return List.of("DefaultHandler");
        }
    }
}