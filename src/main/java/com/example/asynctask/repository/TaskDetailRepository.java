package com.example.asynctask.repository;

import com.example.asynctask.entity.TaskDetail;
import com.example.asynctask.entity.TaskDetail.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务明细数据访问层
 * 
 * @author System
 */
@Repository
public interface TaskDetailRepository extends JpaRepository<TaskDetail, Long> {
    
    /**
     * 根据任务ID查找所有明细
     * 
     * @param taskId 任务ID
     * @return 明细列表
     */
    List<TaskDetail> findByTaskIdOrderByStepOrderAsc(String taskId);
    
    /**
     * 根据任务ID和步骤名称查找明细
     * 
     * @param taskId 任务ID
     * @param stepName 步骤名称
     * @return 明细对象
     */
    Optional<TaskDetail> findByTaskIdAndStepName(String taskId, String stepName);
    
    /**
     * 根据任务ID和步骤顺序查找明细
     * 
     * @param taskId 任务ID
     * @param stepOrder 步骤顺序
     * @return 明细对象
     */
    Optional<TaskDetail> findByTaskIdAndStepOrder(String taskId, Integer stepOrder);
    
    /**
     * 根据任务ID和状态查找明细
     * 
     * @param taskId 任务ID
     * @param stepStatus 步骤状态
     * @return 明细列表
     */
    List<TaskDetail> findByTaskIdAndStepStatus(String taskId, StepStatus stepStatus);
    
    /**
     * 统计任务各状态步骤数量
     * 
     * @param taskId 任务ID
     * @return 统计结果
     */
    @Query("SELECT td.stepStatus, COUNT(td) FROM TaskDetail td WHERE td.taskId = :taskId GROUP BY td.stepStatus")
    List<Object[]> countStepStatusByTaskId(@Param("taskId") String taskId);
    
    /**
     * 获取任务的下一步骤
     * 
     * @param taskId 任务ID
     * @param currentStepOrder 当前步骤顺序
     * @return 下一步骤
     */
    @Query(value = "SELECT * FROM task_detail WHERE task_id = :taskId AND step_order > :currentStepOrder ORDER BY step_order ASC LIMIT 1", nativeQuery = true)
    Optional<TaskDetail> findNextStep(@Param("taskId") String taskId, @Param("currentStepOrder") Integer currentStepOrder);
    
    /**
     * 更新步骤状态
     * 
     * @param id 明细ID
     * @param stepStatus 新状态
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE TaskDetail td SET td.stepStatus = :stepStatus WHERE td.id = :id")
    int updateStepStatus(@Param("id") Long id, @Param("stepStatus") StepStatus stepStatus);
    
    /**
     * 更新步骤状态和结果
     * 
     * @param id 明细ID
     * @param stepStatus 新状态
     * @param stepResult 步骤结果
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE TaskDetail td SET td.stepStatus = :stepStatus, td.stepResult = :stepResult WHERE td.id = :id")
    int updateStepStatusAndResult(@Param("id") Long id, @Param("stepStatus") StepStatus stepStatus, 
                                 @Param("stepResult") String stepResult);
    
    /**
     * 更新步骤状态、结果和上下文数据
     * 
     * @param id 明细ID
     * @param stepStatus 新状态
     * @param stepResult 步骤结果
     * @param contextData 上下文数据
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE TaskDetail td SET td.stepStatus = :stepStatus, td.stepResult = :stepResult, td.contextData = :contextData WHERE td.id = :id")
    int updateStepStatusResultAndContext(@Param("id") Long id, @Param("stepStatus") StepStatus stepStatus, 
                                        @Param("stepResult") String stepResult, @Param("contextData") String contextData);
    
    /**
     * 增加步骤重试次数
     * 
     * @param id 明细ID
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE TaskDetail td SET td.retryCount = td.retryCount + 1 WHERE td.id = :id")
    int incrementRetryCount(@Param("id") Long id);
    
    /**
     * 根据任务类型和步骤名称查找运行中的步骤数量
     * 
     * @param taskIds 任务ID列表
     * @param stepStatus 步骤状态
     * @return 数量
     */
    @Query("SELECT COUNT(td) FROM TaskDetail td WHERE td.taskId IN :taskIds AND td.stepStatus = :stepStatus")
    long countByTaskIdsAndStepStatus(@Param("taskIds") List<String> taskIds, @Param("stepStatus") StepStatus stepStatus);
    
    /**
     * 删除任务的所有明细
     * 
     * @param taskId 任务ID
     * @return 删除行数
     */
    @Modifying
    @Query("DELETE FROM TaskDetail td WHERE td.taskId = :taskId")
    int deleteByTaskId(@Param("taskId") String taskId);
}