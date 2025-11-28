package com.example.asynctask.service.impl;

import com.example.asynctask.entity.TaskDetail;
import com.example.asynctask.entity.TaskDetail.StepStatus;
import com.example.asynctask.mapper.TaskDetailMapper;
import com.example.asynctask.service.TaskDetailServiceMyBatis;
import com.example.asynctask.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务明细服务实现类（MyBatis实现）
 * 
 * @author System
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskDetailServiceMyBatisImpl implements TaskDetailServiceMyBatis {
    
    @Autowired
    private TaskDetailMapper taskDetailMapper;
    
    @Override
    @Transactional
    public TaskDetail createTaskDetail(String taskId, String stepName, int stepOrder, Map<String, Object> contextData) {
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        
        if (!StringUtils.hasText(stepName)) {
            throw new IllegalArgumentException("步骤名称不能为空");
        }
        
        TaskDetail detail = TaskDetail.builder()
                .taskId(taskId)
                .stepName(stepName)
                .stepOrder(stepOrder)
                .stepStatus(StepStatus.PENDING)
                .contextData(JsonUtils.toJsonString(contextData != null ? contextData : new HashMap<>()))
                .retryCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        taskDetailMapper.insert(detail);
        return detail;
    }
    
    @Override
    public TaskDetail getTaskDetail(Long id) {
        if (id == null) {
            return null;
        }
        
        Optional<TaskDetail> optional = taskDetailMapper.findById(id);
        return optional.orElse(null);
    }
    
    @Override
    public List<TaskDetail> getTaskDetailsByTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return new ArrayList<>();
        }
        
        return taskDetailMapper.findByTaskIdOrderByStepOrderAsc(taskId);
    }
    
    @Override
    public TaskDetail getTaskDetailByTaskIdAndStepName(String taskId, String stepName) {
        if (!StringUtils.hasText(taskId) || !StringUtils.hasText(stepName)) {
            return null;
        }
        
        Optional<TaskDetail> optional = taskDetailMapper.findByTaskIdAndStepName(taskId, stepName);
        return optional.orElse(null);
    }
    
    @Override
    public TaskDetail getTaskDetailByTaskIdAndStepOrder(String taskId, int stepOrder) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        
        Optional<TaskDetail> optional = taskDetailMapper.findByTaskIdAndStepOrder(taskId, stepOrder);
        return optional.orElse(null);
    }
    
    @Override
    public List<TaskDetail> getTaskDetailsByTaskIdAndStatus(String taskId, StepStatus stepStatus) {
        if (!StringUtils.hasText(taskId) || stepStatus == null) {
            return new ArrayList<>();
        }
        
        return taskDetailMapper.findByTaskIdAndStepStatus(taskId, stepStatus);
    }
    
    @Override
    public Map<StepStatus, Long> countStepStatusByTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return new HashMap<>();
        }
        
        List<Object> results = taskDetailMapper.countStepStatusByTaskId(taskId);
        Map<StepStatus, Long> countMap = new HashMap<>();
        
        for (Object result : results) {
            Object[] row = (Object[]) result;
            StepStatus status = (StepStatus) row[0];
            Long count = (Long) row[1];
            countMap.put(status, count);
        }
        
        return countMap;
    }
    
    @Override
    public TaskDetail getNextStep(String taskId, int currentStepOrder) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        
        Optional<TaskDetail> optional = taskDetailMapper.findNextStep(taskId, currentStepOrder);
        return optional.orElse(null);
    }
    
    @Override
    @Transactional
    public boolean startStep(Long id) {
        TaskDetail detail = getTaskDetail(id);
        if (detail == null) {
            log.warn("步骤不存在: {}", id);
            return false;
        }
        
        if (detail.getStepStatus() != StepStatus.PENDING) {
            log.warn("步骤状态不是PENDING，无法开始: {}, 状态: {}", id, detail.getStepStatus());
            return false;
        }
        
        int rows = taskDetailMapper.updateStepStatus(id, StepStatus.RUNNING);
        
        if (rows > 0) {
            log.info("步骤已开始: {}", id);
            return true;
        } else {
            log.warn("步骤开始失败: {}", id);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean completeStep(Long id, String stepResult) {
        return completeStep(id, stepResult, null);
    }
    
    @Override
    @Transactional
    public boolean completeStep(Long id, String stepResult, Map<String, Object> contextData) {
        TaskDetail detail = getTaskDetail(id);
        if (detail == null) {
            log.warn("步骤不存在: {}", id);
            return false;
        }
        
        if (detail.getStepStatus() != StepStatus.RUNNING) {
            log.warn("步骤状态不是RUNNING，无法完成: {}, 状态: {}", id, detail.getStepStatus());
            return false;
        }
        
        // 计算耗时
        long durationMs = 0;
        if (detail.getStartTime() != null) {
            durationMs = java.time.Duration.between(detail.getStartTime(), LocalDateTime.now()).toMillis();
        }
        
        int rows;
        if (contextData != null) {
            rows = taskDetailMapper.updateStepStatusResultAndContext(
                    id, StepStatus.SUCCESS, stepResult, JsonUtils.toJsonString(contextData));
        } else {
            rows = taskDetailMapper.updateStepStatusAndResult(id, StepStatus.SUCCESS, stepResult);
        }
        
        if (rows > 0) {
            // 更新耗时
            TaskDetail updatedDetail = getTaskDetail(id);
            updatedDetail.setDurationMs(durationMs);
            taskDetailMapper.update(updatedDetail);
            
            log.info("步骤已完成: {}", id);
            return true;
        } else {
            log.warn("步骤完成失败: {}", id);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean failStep(Long id, String errorMessage) {
        TaskDetail detail = getTaskDetail(id);
        if (detail == null) {
            log.warn("步骤不存在: {}", id);
            return false;
        }
        
        if (detail.getStepStatus() != StepStatus.RUNNING) {
            log.warn("步骤状态不是RUNNING，无法标记为失败: {}, 状态: {}", id, detail.getStepStatus());
            return false;
        }
        
        // 计算耗时
        long durationMs = 0;
        if (detail.getStartTime() != null) {
            durationMs = java.time.Duration.between(detail.getStartTime(), LocalDateTime.now()).toMillis();
        }
        
        int rows = taskDetailMapper.updateStepStatusResultAndContext(
                id, StepStatus.FAILED, null, JsonUtils.toJsonString(Map.of("errorMessage", errorMessage)));
        
        if (rows > 0) {
            // 更新耗时
            TaskDetail updatedDetail = getTaskDetail(id);
            updatedDetail.setDurationMs(durationMs);
            taskDetailMapper.update(updatedDetail);
            
            log.info("步骤已失败: {}, 错误: {}", id, errorMessage);
            return true;
        } else {
            log.warn("步骤失败操作失败: {}", id);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean skipStep(Long id, String reason) {
        TaskDetail detail = getTaskDetail(id);
        if (detail == null) {
            log.warn("步骤不存在: {}", id);
            return false;
        }
        
        if (detail.getStepStatus() != StepStatus.PENDING) {
            log.warn("步骤状态不是PENDING，无法跳过: {}, 状态: {}", id, detail.getStepStatus());
            return false;
        }
        
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("skipReason", reason);
        
        int rows = taskDetailMapper.updateStepStatusResultAndContext(
                id, StepStatus.SKIPPED, "步骤已跳过", JsonUtils.toJsonString(contextData));
        
        if (rows > 0) {
            log.info("步骤已跳过: {}, 原因: {}", id, reason);
            return true;
        } else {
            log.warn("步骤跳过失败: {}", id);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateStepStatus(Long id, StepStatus stepStatus) {
        TaskDetail detail = getTaskDetail(id);
        if (detail == null) {
            log.warn("步骤不存在: {}", id);
            return false;
        }
        
        int rows = taskDetailMapper.updateStepStatus(id, stepStatus);
        
        if (rows > 0) {
            log.debug("步骤状态已更新: {}, 新状态: {}", id, stepStatus);
            return true;
        } else {
            log.warn("步骤状态更新失败: {}", id);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateStepStatusAndResult(Long id, StepStatus stepStatus, String stepResult) {
        TaskDetail detail = getTaskDetail(id);
        if (detail == null) {
            log.warn("步骤不存在: {}", id);
            return false;
        }
        
        int rows = taskDetailMapper.updateStepStatusAndResult(id, stepStatus, stepResult);
        
        if (rows > 0) {
            log.debug("步骤状态和结果已更新: {}, 状态: {}", id, stepStatus);
            return true;
        } else {
            log.warn("步骤状态和结果更新失败: {}", id);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateStepStatusResultAndContext(Long id, StepStatus stepStatus, String stepResult, Map<String, Object> contextData) {
        TaskDetail detail = getTaskDetail(id);
        if (detail == null) {
            log.warn("步骤不存在: {}", id);
            return false;
        }
        
        int rows = taskDetailMapper.updateStepStatusResultAndContext(
                id, stepStatus, stepResult, JsonUtils.toJsonString(contextData));
        
        if (rows > 0) {
            log.debug("步骤状态、结果和上下文已更新: {}, 状态: {}", id, stepStatus);
            return true;
        } else {
            log.warn("步骤状态、结果和上下文更新失败: {}", id);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean incrementRetryCount(Long id) {
        TaskDetail detail = getTaskDetail(id);
        if (detail == null) {
            log.warn("步骤不存在: {}", id);
            return false;
        }
        
        int rows = taskDetailMapper.incrementRetryCount(id);
        
        if (rows > 0) {
            log.debug("步骤重试次数已增加: {}", id);
            return true;
        } else {
            log.warn("步骤重试次数增加失败: {}", id);
            return false;
        }
    }
    
    @Override
    public long countByTaskIdsAndStepStatus(List<String> taskIds, StepStatus stepStatus) {
        if (taskIds == null || taskIds.isEmpty() || stepStatus == null) {
            return 0;
        }
        
        return taskDetailMapper.countByTaskIdsAndStepStatus(taskIds, stepStatus);
    }
    
    @Override
    @Transactional
    public boolean deleteTaskDetailsByTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return false;
        }
        
        int rows = taskDetailMapper.deleteByTaskId(taskId);
        
        if (rows > 0) {
            log.info("任务明细已删除: taskId={}, 删除行数={}", taskId, rows);
            return true;
        } else {
            log.warn("任务明细删除失败: taskId={}", taskId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteTaskDetailById(Long id) {
        if (id == null) {
            return false;
        }
        
        int rows = taskDetailMapper.deleteById(id);
        
        if (rows > 0) {
            log.info("任务明细已删除: id={}", id);
            return true;
        } else {
            log.warn("任务明细删除失败: id={}", id);
            return false;
        }
    }
}