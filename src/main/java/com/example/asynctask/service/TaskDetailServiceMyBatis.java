package com.example.asynctask.service;

import com.example.asynctask.entity.TaskDetail;
import com.example.asynctask.entity.TaskDetail.StepStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务明细服务接口（MyBatis实现）
 * 
 * @author System
 */
public interface TaskDetailServiceMyBatis {
    
    /**
     * 创建任务明细
     * 
     * @param taskId 任务ID
     * @param stepName 步骤名称
     * @param stepOrder 步骤顺序
     * @param contextData 上下文数据
     * @return 明细对象
     */
    TaskDetail createTaskDetail(String taskId, String stepName, int stepOrder, Map<String, Object> contextData);
    
    /**
     * 获取明细
     * 
     * @param id 主键ID
     * @return 明细对象
     */
    TaskDetail getTaskDetail(Long id);
    
    /**
     * 获取任务的所有明细
     * 
     * @param taskId 任务ID
     * @return 明细列表
     */
    List<TaskDetail> getTaskDetailsByTaskId(String taskId);
    
    /**
     * 根据任务ID和步骤名称获取明细
     * 
     * @param taskId 任务ID
     * @param stepName 步骤名称
     * @return 明细对象
     */
    TaskDetail getTaskDetailByTaskIdAndStepName(String taskId, String stepName);
    
    /**
     * 根据任务ID和步骤顺序获取明细
     * 
     * @param taskId 任务ID
     * @param stepOrder 步骤顺序
     * @return 明细对象
     */
    TaskDetail getTaskDetailByTaskIdAndStepOrder(String taskId, int stepOrder);
    
    /**
     * 根据任务ID和状态获取明细
     * 
     * @param taskId 任务ID
     * @param stepStatus 步骤状态
     * @return 明细列表
     */
    List<TaskDetail> getTaskDetailsByTaskIdAndStatus(String taskId, StepStatus stepStatus);
    
    /**
     * 统计任务各状态步骤数量
     * 
     * @param taskId 任务ID
     * @return 统计结果
     */
    Map<StepStatus, Long> countStepStatusByTaskId(String taskId);
    
    /**
     * 获取任务的下一步骤
     * 
     * @param taskId 任务ID
     * @param currentStepOrder 当前步骤顺序
     * @return 下一步骤
     */
    TaskDetail getNextStep(String taskId, int currentStepOrder);
    
    /**
     * 开始步骤
     * 
     * @param id 明细ID
     * @return 是否成功
     */
    boolean startStep(Long id);
    
    /**
     * 完成步骤
     * 
     * @param id 明细ID
     * @param stepResult 步骤结果
     * @return 是否成功
     */
    boolean completeStep(Long id, String stepResult);
    
    /**
     * 完成步骤（带上下文数据）
     * 
     * @param id 明细ID
     * @param stepResult 步骤结果
     * @param contextData 上下文数据
     * @return 是否成功
     */
    boolean completeStep(Long id, String stepResult, Map<String, Object> contextData);
    
    /**
     * 步骤失败
     * 
     * @param id 明细ID
     * @param errorMessage 错误信息
     * @return 是否成功
     */
    boolean failStep(Long id, String errorMessage);
    
    /**
     * 跳过步骤
     * 
     * @param id 明细ID
     * @param reason 跳过原因
     * @return 是否成功
     */
    boolean skipStep(Long id, String reason);
    
    /**
     * 更新步骤状态
     * 
     * @param id 明细ID
     * @param stepStatus 新状态
     * @return 是否成功
     */
    boolean updateStepStatus(Long id, StepStatus stepStatus);
    
    /**
     * 更新步骤状态和结果
     * 
     * @param id 明细ID
     * @param stepStatus 新状态
     * @param stepResult 步骤结果
     * @return 是否成功
     */
    boolean updateStepStatusAndResult(Long id, StepStatus stepStatus, String stepResult);
    
    /**
     * 更新步骤状态、结果和上下文数据
     * 
     * @param id 明细ID
     * @param stepStatus 新状态
     * @param stepResult 步骤结果
     * @param contextData 上下文数据
     * @return 是否成功
     */
    boolean updateStepStatusResultAndContext(Long id, StepStatus stepStatus, String stepResult, Map<String, Object> contextData);
    
    /**
     * 增加步骤重试次数
     * 
     * @param id 明细ID
     * @return 是否成功
     */
    boolean incrementRetryCount(Long id);
    
    /**
     * 根据任务类型和步骤名称查找运行中的步骤数量
     * 
     * @param taskIds 任务ID列表
     * @param stepStatus 步骤状态
     * @return 数量
     */
    long countByTaskIdsAndStepStatus(List<String> taskIds, StepStatus stepStatus);
    
    /**
     * 删除任务的所有明细
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean deleteTaskDetailsByTaskId(String taskId);
    
    /**
     * 根据ID删除明细
     * 
     * @param id 主键ID
     * @return 是否成功
     */
    boolean deleteTaskDetailById(Long id);
}