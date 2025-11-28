package com.example.asynctask.mapper;

import com.example.asynctask.entity.TaskDetail;
import com.example.asynctask.entity.TaskDetail.StepStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务明细Mapper接口
 * 
 * @author System
 */
@Mapper
@Repository
public interface TaskDetailMapper {
    
    /**
     * 插入任务明细
     * 
     * @param detail 明细对象
     * @return 插入行数
     */
    int insert(TaskDetail detail);
    
    /**
     * 根据ID查询明细
     * 
     * @param id 主键ID
     * @return 明细对象
     */
    Optional<TaskDetail> findById(@Param("id") Long id);
    
    /**
     * 根据任务ID查找所有明细
     * 
     * @param taskId 任务ID
     * @return 明细列表
     */
    List<TaskDetail> findByTaskIdOrderByStepOrderAsc(@Param("taskId") String taskId);
    
    /**
     * 根据任务ID和步骤名称查找明细
     * 
     * @param taskId 任务ID
     * @param stepName 步骤名称
     * @return 明细对象
     */
    Optional<TaskDetail> findByTaskIdAndStepName(@Param("taskId") String taskId, @Param("stepName") String stepName);
    
    /**
     * 根据任务ID和步骤顺序查找明细
     * 
     * @param taskId 任务ID
     * @param stepOrder 步骤顺序
     * @return 明细对象
     */
    Optional<TaskDetail> findByTaskIdAndStepOrder(@Param("taskId") String taskId, @Param("stepOrder") Integer stepOrder);
    
    /**
     * 根据任务ID和状态查找明细
     * 
     * @param taskId 任务ID
     * @param stepStatus 步骤状态
     * @return 明细列表
     */
    List<TaskDetail> findByTaskIdAndStepStatus(@Param("taskId") String taskId, @Param("stepStatus") StepStatus stepStatus);
    
    /**
     * 统计任务各状态步骤数量
     * 
     * @param taskId 任务ID
     * @return 统计结果
     */
    List<Object> countStepStatusByTaskId(@Param("taskId") String taskId);
    
    /**
     * 获取任务的下一步骤
     * 
     * @param taskId 任务ID
     * @param currentStepOrder 当前步骤顺序
     * @return 下一步骤
     */
    Optional<TaskDetail> findNextStep(@Param("taskId") String taskId, @Param("currentStepOrder") Integer currentStepOrder);
    
    /**
     * 更新明细
     * 
     * @param detail 明细对象
     * @return 更新行数
     */
    int update(TaskDetail detail);
    
    /**
     * 更新步骤状态
     * 
     * @param id 明细ID
     * @param stepStatus 新状态
     * @return 更新行数
     */
    int updateStepStatus(@Param("id") Long id, @Param("stepStatus") StepStatus stepStatus);
    
    /**
     * 更新步骤状态和结果
     * 
     * @param id 明细ID
     * @param stepStatus 新状态
     * @param stepResult 步骤结果
     * @return 更新行数
     */
    int updateStepStatusAndResult(@Param("id") Long id, @Param("stepStatus") StepStatus stepStatus, @Param("stepResult") String stepResult);
    
    /**
     * 更新步骤状态、结果和上下文数据
     * 
     * @param id 明细ID
     * @param stepStatus 新状态
     * @param stepResult 步骤结果
     * @param contextData 上下文数据
     * @return 更新行数
     */
    int updateStepStatusResultAndContext(@Param("id") Long id, @Param("stepStatus") StepStatus stepStatus, 
                                        @Param("stepResult") String stepResult, @Param("contextData") String contextData);
    
    /**
     * 增加步骤重试次数
     * 
     * @param id 明细ID
     * @return 更新行数
     */
    int incrementRetryCount(@Param("id") Long id);
    
    /**
     * 根据任务类型和步骤名称查找运行中的步骤数量
     * 
     * @param taskIds 任务ID列表
     * @param stepStatus 步骤状态
     * @return 数量
     */
    long countByTaskIdsAndStepStatus(@Param("taskIds") List<String> taskIds, @Param("stepStatus") StepStatus stepStatus);
    
    /**
     * 删除任务的所有明细
     * 
     * @param taskId 任务ID
     * @return 删除行数
     */
    int deleteByTaskId(@Param("taskId") String taskId);
    
    /**
     * 根据ID删除明细
     * 
     * @param id 主键ID
     * @return 删除行数
     */
    int deleteById(@Param("id") Long id);
}