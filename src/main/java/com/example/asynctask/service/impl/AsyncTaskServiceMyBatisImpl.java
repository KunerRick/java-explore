package com.example.asynctask.service.impl;

import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.mapper.AsyncTaskMapper;
import com.example.asynctask.service.AsyncTaskServiceMyBatis;
import com.example.asynctask.util.JsonUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 异步任务服务实现类（MyBatis实现）
 * 
 * @author System
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AsyncTaskServiceMyBatisImpl implements AsyncTaskServiceMyBatis {
    
    @Autowired
    private AsyncTaskMapper asyncTaskMapper;
    
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
        
        asyncTaskMapper.insert(task);
        return task;
    }
    
    @Override
    public AsyncTask getTask(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        
        Optional<AsyncTask> optional = asyncTaskMapper.findByTaskId(taskId);
        return optional.orElse(null);
    }
    
    @Override
    public AsyncTask getTaskWithLock(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        
        Optional<AsyncTask> optional = asyncTaskMapper.findByTaskIdWithLock(taskId);
        return optional.orElse(null);
    }
    
    @Override
    public Map<String, Object> getTasksByPage(String taskType, AsyncTask.TaskStatus status, int page, int size, String sortBy, String sortDir) {
        int offset = page * size;
        
        // 确定排序字段和方向
        String orderBy = "priority DESC, created_time DESC";
        if (StringUtils.hasText(sortBy)) {
            String direction = StringUtils.hasText(sortDir) && sortDir.equalsIgnoreCase("ASC") ? "ASC" : "DESC";
            orderBy = sortBy + " " + direction;
        }
        
        List<AsyncTask> tasks = asyncTaskMapper.findByPage(taskType, status, offset, size);
        long total = asyncTaskMapper.countByTaskTypeAndStatus(taskType, status);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", tasks);
        result.put("totalElements", total);
        result.put("totalPages", (total + size - 1) / size);
        result.put("size", size);
        result.put("number", page);
        result.put("first", page == 0);
        result.put("last", offset >= total - size);
        
        return result;
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
        return asyncTaskMapper.findPendingTasks(limit);
    }
    
    @Override
    public List<AsyncTask> getPendingTasksWithLock(int limit) {
        return asyncTaskMapper.findPendingTasksWithLock(limit);
    }
    
    @Override
    public List<AsyncTask> getPendingTasksByTypeWithLock(String taskType, int limit) {
        return asyncTaskMapper.findPendingTasksByTaskTypeWithLock(taskType, limit);
    }
    
    @Override
    @Transactional
    public boolean startTask(String taskId) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        try {
            int rows = asyncTaskMapper.updateStatusAndStepWithVersion(
                    taskId, AsyncTask.TaskStatus.PROCESSING, null, task.getVersion());
            
            if (rows > 0) {
                log.info("任务开始执行: {}", taskId);
                return true;
            } else {
                log.warn("任务开始失败，可能已被其他线程处理: {}", taskId);
                return false;
            }
        } catch (Exception e) {
            log.warn("任务开始失败，乐观锁冲突: {}", taskId, e);
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
        Map<String, Object> contextData = JsonUtils.parseContextData(task.getContextData());
        if (StringUtils.hasText(resultSummary)) {
            contextData.put("resultSummary", resultSummary);
        }
        
        int rows = asyncTaskMapper.updateStatusStepAndContextWithVersion(
                taskId, AsyncTask.TaskStatus.COMPLETED, null, JsonUtils.toJsonString(contextData), task.getVersion());
        
        if (rows > 0) {
            // 更新进度和结束时间
            asyncTaskMapper.updateProgressWithVersion(taskId, 100, task.getVersion() + 1);
            log.info("任务完成: {}", taskId);
            return true;
        } else {
            log.warn("任务完成失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
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
        int rows = asyncTaskMapper.updateErrorMessageWithVersion(taskId, errorMessage, task.getVersion());
        
        if (rows > 0) {
            asyncTaskMapper.updateStatusWithVersion(taskId, AsyncTask.TaskStatus.FAILED, task.getVersion() + 1);
            log.info("任务失败: {}, 错误: {}", taskId, errorMessage);
            return true;
        } else {
            log.warn("任务失败操作失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean pauseTask(String taskId) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        int rows = asyncTaskMapper.updateStatusWithVersion(taskId, AsyncTask.TaskStatus.PAUSED, task.getVersion());
        
        if (rows > 0) {
            log.info("任务已暂停: {}", taskId);
            return true;
        } else {
            log.warn("任务暂停失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean cancelTask(String taskId) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        int rows = asyncTaskMapper.updateStatusWithVersion(taskId, AsyncTask.TaskStatus.FAILED, task.getVersion());
        
        if (rows > 0) {
            asyncTaskMapper.updateErrorMessageWithVersion(taskId, "任务已取消", task.getVersion() + 1);
            log.info("任务已取消: {}", taskId);
            return true;
        } else {
            log.warn("任务取消失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean retryTask(String taskId) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        if (task.getStatus() != AsyncTask.TaskStatus.FAILED) {
            log.warn("任务状态不是FAILED，无法重试: {}, 状态: {}", taskId, task.getStatus());
            return false;
        }
        
        if (task.getRetryCount() >= task.getMaxRetries()) {
            log.warn("任务已达到最大重试次数，无法重试: {}", taskId);
            return false;
        }
        
        int rows = asyncTaskMapper.updateStatusWithVersion(taskId, AsyncTask.TaskStatus.PENDING, task.getVersion());
        
        if (rows > 0) {
            asyncTaskMapper.incrementRetryCountWithVersion(taskId, task.getVersion() + 1);
            log.info("任务已重试: {}", taskId);
            return true;
        } else {
            log.warn("任务重试失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateProgress(String taskId, int progress) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        int rows = asyncTaskMapper.updateProgressWithVersion(taskId, progress, task.getVersion());
        
        if (rows > 0) {
            log.debug("任务进度已更新: {}, 进度: {}", taskId, progress);
            return true;
        } else {
            log.warn("任务进度更新失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateCurrentStep(String taskId, String currentStep) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        int rows = asyncTaskMapper.updateStatusAndStepWithVersion(
                taskId, task.getStatus(), currentStep, task.getVersion());
        
        if (rows > 0) {
            log.debug("任务当前步骤已更新: {}, 步骤: {}", taskId, currentStep);
            return true;
        } else {
            log.warn("任务当前步骤更新失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateContextData(String taskId, Map<String, Object> contextData) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        int rows = asyncTaskMapper.updateStatusStepAndContextWithVersion(
                taskId, task.getStatus(), task.getCurrentStep(), JsonUtils.toJsonString(contextData), task.getVersion());
        
        if (rows > 0) {
            log.debug("任务上下文数据已更新: {}", taskId);
            return true;
        } else {
            log.warn("任务上下文数据更新失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean addStepLog(String taskId, AsyncTask.StepLog stepLog) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        List<AsyncTask.StepLog> stepLogs = JsonUtils.parseStepLogs(task.getStepLogs());
        if (stepLogs == null) {
            stepLogs = new ArrayList<>();
        }
        stepLogs.add(stepLog);
        
        int rows = asyncTaskMapper.updateStepLogsWithVersion(taskId, JsonUtils.toJsonString(stepLogs), task.getVersion());
        
        if (rows > 0) {
            log.debug("任务步骤日志已添加: {}, 步骤: {}", taskId, stepLog.getStepName());
            return true;
        } else {
            log.warn("任务步骤日志添加失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateStepLogs(String taskId, List<AsyncTask.StepLog> stepLogs) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        int rows = asyncTaskMapper.updateStepLogsWithVersion(taskId, JsonUtils.toJsonString(stepLogs), task.getVersion());
        
        if (rows > 0) {
            log.debug("任务步骤日志已更新: {}", taskId);
            return true;
        } else {
            log.warn("任务步骤日志更新失败，可能已被其他线程处理: {}", taskId);
            return false;
        }
    }
    
    @Override
    public Map<AsyncTask.TaskStatus, Long> countByStatus() {
        List<Object> results = asyncTaskMapper.countByStatus();
        Map<AsyncTask.TaskStatus, Long> countMap = new HashMap<>();
        
        for (Object result : results) {
            Object[] row = (Object[]) result;
            AsyncTask.TaskStatus status = (AsyncTask.TaskStatus) row[0];
            Long count = (Long) row[1];
            countMap.put(status, count);
        }
        
        return countMap;
    }
    
    @Override
    public Map<AsyncTask.TaskStatus, Long> countByStatusAndTaskType(String taskType) {
        List<Object> results = asyncTaskMapper.countByStatusAndTaskType(taskType);
        Map<AsyncTask.TaskStatus, Long> countMap = new HashMap<>();
        
        for (Object result : results) {
            Object[] row = (Object[]) result;
            AsyncTask.TaskStatus status = (AsyncTask.TaskStatus) row[0];
            Long count = (Long) row[1];
            countMap.put(status, count);
        }
        
        return countMap;
    }
    
    @Override
    public List<AsyncTask> findTimeoutTasks(AsyncTask.TaskStatus status, LocalDateTime currentTime) {
        return asyncTaskMapper.findTimeoutTasks(status, currentTime);
    }
    
    @Override
    public List<AsyncTask> findRetryableFailedTasks(int limit) {
        return asyncTaskMapper.findRetryableFailedTasks(limit);
    }
    
    @Override
    @Transactional
    public boolean deleteTask(String taskId) {
        AsyncTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }
        
        int rows = asyncTaskMapper.deleteByTaskId(taskId);
        
        if (rows > 0) {
            log.info("任务已删除: {}", taskId);
            return true;
        } else {
            log.warn("任务删除失败: {}", taskId);
            return false;
        }
    }
    
    @Override
    public List<String> getDefaultHandlerChain(String taskType) {
        // 根据任务类型返回默认处理链
        switch (taskType) {
            case "VIDEO_SCORING":
                List<String> videoChain = new ArrayList<>();
                videoChain.add("videoScoringChain");
                return videoChain;
            default:
                List<String> defaultChain = new ArrayList<>();
                defaultChain.add("defaultChain");
                return defaultChain;
        }
    }
    
    @Override
    public Map<String, Object> getTasksByPage(String taskType, String status, int page, int size, String sortBy, String sortDir) {
        // 使用PageHelper进行分页
        PageHelper.startPage(page, size);
        
        List<AsyncTask> taskList;
        
        // 根据条件查询
        if (StringUtils.hasText(taskType)) {
            if (StringUtils.hasText(status)) {
                AsyncTask.TaskStatus taskStatus = AsyncTask.TaskStatus.valueOf(status.toUpperCase());
                taskList = asyncTaskMapper.findByTaskTypeAndStatus(taskType, taskStatus);
            } else {
                taskList = asyncTaskMapper.findByTaskType(taskType);
            }
        } else if (StringUtils.hasText(status)) {
            AsyncTask.TaskStatus taskStatus = AsyncTask.TaskStatus.valueOf(status.toUpperCase());
            taskList = asyncTaskMapper.findByStatus(taskStatus);
        } else {
            taskList = asyncTaskMapper.findAll();
        }
        
        // 创建分页信息
        PageInfo<AsyncTask> pageInfo = new PageInfo<>(taskList);
        
        // 创建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("content", pageInfo.getList());
        result.put("totalElements", pageInfo.getTotal());
        result.put("totalPages", pageInfo.getPages());
        result.put("size", pageInfo.getPageSize());
        result.put("number", pageInfo.getPageNum() - 1); // 转换为0-based
        result.put("first", pageInfo.isIsFirstPage());
        result.put("last", pageInfo.isIsLastPage());
        
        return result;
    }
    
    @Override
    public Map<String, Long> countTasksByStatus() {
        List<Map<String, Object>> statusCounts = asyncTaskMapper.countByStatus();
        Map<String, Long> result = new HashMap<>();
        
        for (Map<String, Object> item : statusCounts) {
            String status = (String) item.get("status");
            Long count = ((Number) item.get("count")).longValue();
            result.put(status, count);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Long> countTasksByStatusAndType(String taskType) {
        List<Map<String, Object>> statusCounts = asyncTaskMapper.countByStatusAndType(taskType);
        Map<String, Long> result = new HashMap<>();
        
        for (Map<String, Object> item : statusCounts) {
            String status = (String) item.get("status");
            Long count = ((Number) item.get("count")).longValue();
            result.put(status, count);
        }
        
        return result;
    }
    
    @Override
    public boolean updateTaskContextData(String taskId, Map<String, Object> contextData) {
        try {
            AsyncTask task = asyncTaskMapper.findByTaskId(taskId);
            if (task == null) {
                log.warn("任务不存在: {}", taskId);
                return false;
            }
            
            // 更新上下文数据
            task.setContextData(JsonUtils.toJsonString(contextData));
            asyncTaskMapper.update(task);
            
            return true;
        } catch (Exception e) {
            log.error("更新任务上下文数据失败: taskId={}, error={}", taskId, e.getMessage());
            return false;
        }
    }
    
    @Override
    public String generateTaskId(String taskType) {
        // 生成任务ID，格式：类型 + 时间戳 + 随机数
        return taskType.toLowerCase() + "_" + System.currentTimeMillis() + "_" + 
               String.format("%04d", new Random().nextInt(10000));
    }
}