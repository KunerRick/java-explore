// 获取上下文路径
function getContextPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
}

document.addEventListener('DOMContentLoaded', function() {
    // 加载统计数据
    loadStats();
    loadRecentTasks();

    // 刷新按钮
    document.getElementById('refreshBtn').addEventListener('click', function() {
        loadStats();
        loadRecentTasks();
    });

    // 创建视频评分任务按钮
    document.getElementById('submitCreateTask').addEventListener('click', function() {
        createVideoScoringTask();
    });

    // 手动调度任务按钮
    document.getElementById('scheduleTasksBtn').addEventListener('click', function() {
        scheduleTasks();
    });

    // 启动调度器按钮
    document.getElementById('startSchedulerBtn').addEventListener('click', function() {
        startScheduler();
    });

    // 停止调度器按钮
    document.getElementById('stopSchedulerBtn').addEventListener('click', function() {
        stopScheduler();
    });
});

/**
 * 加载统计数据
 */
function loadStats() {
    fetch(getContextPath() + '/api/tasks/stats')
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                updateStatsDisplay(data.data);
            } else {
                showToast('加载统计数据失败: ' + data.message, 'danger');
            }
        })
        .catch(error => {
            console.error('Error loading stats:', error);
            showToast('加载统计数据异常', 'danger');
        });
}

/**
 * 更新统计显示
 */
function updateStatsDisplay(stats) {
    // 更新任务状态统计
    const statusStats = stats.statusStats || {};
    let totalTasks = 0;
    
    for (const status in statusStats) {
        totalTasks += statusStats[status];
    }
    
    document.getElementById('totalTasks').textContent = totalTasks;
    document.getElementById('pendingTasks').textContent = statusStats.PENDING || 0;
    document.getElementById('processingTasks').textContent = statusStats.PROCESSING || 0;
    document.getElementById('completedTasks').textContent = statusStats.COMPLETED || 0;
    
    // 更新调度器状态
    const schedulerStats = stats.schedulerStats || {};
    const isRunning = schedulerStats.running || false;
    
    const statusElement = document.getElementById('schedulerStatus');
    statusElement.textContent = isRunning ? '运行中' : '已停止';
    statusElement.className = 'badge bg-' + (isRunning ? 'success' : 'danger');
    
    document.getElementById('totalSchedules').textContent = schedulerStats.totalSchedules || '-';
    document.getElementById('successTasks').textContent = schedulerStats.successTasks || '-';
    document.getElementById('failedTasks').textContent = schedulerStats.failedTasks || '-';
}

/**
 * 加载最近任务
 */
function loadRecentTasks() {
    fetch(getContextPath() + '/api/tasks?page=0&size=5&sortBy=createdTime&sortDir=desc')
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                updateRecentTasksTable(data.data.content);
            } else {
                showToast('加载最近任务失败: ' + data.message, 'danger');
            }
        })
        .catch(error => {
            console.error('Error loading recent tasks:', error);
            showToast('加载最近任务异常', 'danger');
        });
}

/**
 * 更新最近任务表格
 */
function updateRecentTasksTable(tasks) {
    const tbody = document.getElementById('recentTasksTable');
    
    if (tasks.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">暂无任务</td></tr>';
        return;
    }
    
    tbody.innerHTML = tasks.map(task => `
        <tr>
            <td>${task.taskId}</td>
            <td>${task.taskName}</td>
            <td>${task.taskType}</td>
            <td>${getStatusBadge(task.status)}</td>
            <td>
                <div class="progress" style="height: 20px;">
                    <div class="progress-bar" role="progressbar" style="width: ${task.progress}%;" aria-valuenow="${task.progress}" aria-valuemin="0" aria-valuemax="100">${task.progress}%</div>
                </div>
            </td>
            <td>${formatDateTime(task.createdTime)}</td>
            <td>
                <button class="btn btn-sm btn-outline-primary me-1" onclick="viewTask('${task.taskId}')">
                    <i class="bi bi-eye"></i>
                </button>
                ${getActionButtons(task)}
            </td>
        </tr>
    `).join('');
}

/**
 * 获取状态徽章
 */
function getStatusBadge(status) {
    const statusConfig = {
        'PENDING': { text: '待处理', class: 'bg-warning' },
        'PROCESSING': { text: '处理中', class: 'bg-info' },
        'COMPLETED': { text: '已完成', class: 'bg-success' },
        'FAILED': { text: '失败', class: 'bg-danger' },
        'PAUSED': { text: '暂停', class: 'bg-secondary' }
    };
    
    const config = statusConfig[status] || { text: status, class: 'bg-secondary' };
    return `<span class="badge ${config.class}">${config.text}</span>`;
}

/**
 * 获取操作按钮
 */
function getActionButtons(task) {
    let buttons = '';
    
    if (task.status === 'PENDING') {
        buttons += `<button class="btn btn-sm btn-outline-success me-1" onclick="executeTask('${task.taskId}')">
            <i class="bi bi-play-fill"></i>
        </button>`;
    }
    
    if (task.status === 'FAILED') {
        buttons += `<button class="btn btn-sm btn-outline-warning me-1" onclick="retryTask('${task.taskId}')">
            <i class="bi bi-arrow-clockwise"></i>
        </button>`;
    }
    
    if (task.status === 'PROCESSING') {
        buttons += `<button class="btn btn-sm btn-outline-warning me-1" onclick="pauseTask('${task.taskId}')">
            <i class="bi bi-pause-fill"></i>
        </button>`;
    }
    
    if (task.status === 'PROCESSING' || task.status === 'PENDING') {
        buttons += `<button class="btn btn-sm btn-outline-danger me-1" onclick="cancelTask('${task.taskId}')">
            <i class="bi bi-x-circle"></i>
        </button>`;
    }
    
    return buttons;
}

/**
 * 创建视频评分任务
 */
function createVideoScoringTask() {
    const videoId = document.getElementById('videoId').value;
    const taskName = document.getElementById('taskName').value || '视频评分任务';
    const creator = document.getElementById('creator').value || 'system';
    const priority = document.getElementById('priority').value || 5;
    
    if (!videoId) {
        showToast('请输入视频ID', 'warning');
        return;
    }
    
    const params = new URLSearchParams({
        videoId: videoId,
        taskName: taskName,
        creator: creator,
        priority: priority
    });
    
    fetch(getContextPath() + '/api/tasks/video-scoring?' + params.toString(), {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showToast('任务创建成功', 'success');
            bootstrap.Modal.getInstance(document.getElementById('createTaskModal')).hide();
            document.getElementById('createTaskForm').reset();
            loadStats();
            loadRecentTasks();
        } else {
            showToast('创建任务失败: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error creating task:', error);
        showToast('创建任务异常', 'danger');
    });
}

/**
 * 手动调度任务
 */
function scheduleTasks() {
    fetch(getContextPath() + '/api/tasks/schedule', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showToast('任务调度已触发', 'success');
            setTimeout(() => {
                loadStats();
                loadRecentTasks();
            }, 1000);
        } else {
            showToast('触发调度失败: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error scheduling tasks:', error);
        showToast('触发调度异常', 'danger');
    });
}

/**
 * 启动调度器
 */
function startScheduler() {
    fetch(getContextPath() + '/api/tasks/scheduler/start', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showToast('调度器已启动', 'success');
            loadStats();
        } else {
            showToast('启动调度器失败: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error starting scheduler:', error);
        showToast('启动调度器异常', 'danger');
    });
}

/**
 * 停止调度器
 */
function stopScheduler() {
    fetch(getContextPath() + '/api/tasks/scheduler/stop', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showToast('调度器已停止', 'success');
            loadStats();
        } else {
            showToast('停止调度器失败: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error stopping scheduler:', error);
        showToast('停止调度器异常', 'danger');
    });
}

/**
 * 查看任务详情
 */
function viewTask(taskId) {
    window.location.href = getContextPath() + '/task-detail?taskId=' + taskId;
}

/**
 * 创建Mock任务数据
 */
function createMockTasks() {
    const count = prompt('请输入要创建的Mock任务数量（1-100）:', '10');
    
    if (count === null) {
        return; // 用户取消了操作
    }
    
    const numCount = parseInt(count);
    if (isNaN(numCount) || numCount < 1 || numCount > 100) {
        showToast('请输入1-100之间的有效数字', 'warning');
        return;
    }
    
    fetch(getContextPath() + `/api/tasks/mock?count=${numCount}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showToast(`成功创建 ${numCount} 个Mock任务数据`, 'success');
            setTimeout(() => {
                loadStats();
                loadRecentTasks();
            }, 1000);
        } else {
            showToast('创建Mock任务数据失败: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error creating mock tasks:', error);
        showToast('创建Mock任务数据异常', 'danger');
    });
}

/**
 * 执行任务
 */
function executeTask(taskId) {
    fetch(getContextPath() + `/api/tasks/${taskId}/execute`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showToast('任务已提交执行', 'success');
            setTimeout(loadRecentTasks, 1000);
        } else {
            showToast('执行任务失败: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error executing task:', error);
        showToast('执行任务异常', 'danger');
    });
}

/**
 * 重试任务
 */
function retryTask(taskId) {
    fetch(getContextPath() + `/api/tasks/${taskId}/retry`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showToast('任务重试已提交', 'success');
            setTimeout(loadRecentTasks, 1000);
        } else {
            showToast('重试任务失败: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error retrying task:', error);
        showToast('重试任务异常', 'danger');
    });
}

/**
 * 暂停任务
 */
function pauseTask(taskId) {
    fetch(getContextPath() + `/api/tasks/${taskId}/pause`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showToast('任务已暂停', 'success');
            setTimeout(loadRecentTasks, 1000);
        } else {
            showToast('暂停任务失败: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error pausing task:', error);
        showToast('暂停任务异常', 'danger');
    });
}

/**
 * 取消任务
 */
function cancelTask(taskId) {
    if (confirm('确定要取消这个任务吗？')) {
        fetch(getContextPath() + `/api/tasks/${taskId}/cancel`, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showToast('任务已取消', 'success');
                setTimeout(loadRecentTasks, 1000);
            } else {
                showToast('取消任务失败: ' + data.message, 'danger');
            }
        })
        .catch(error => {
            console.error('Error cancelling task:', error);
            showToast('取消任务异常', 'danger');
        });
    }
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    
    const date = new Date(dateTimeStr);
    return date.toLocaleString('zh-CN');
}

/**
 * 显示提示消息
 */
function showToast(message, type = 'info') {
    // 创建toast容器（如果不存在）
    let toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toastContainer';
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }
    
    // 创建toast元素
    const toastId = 'toast-' + Date.now();
    const toastHtml = `
        <div id="${toastId}" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="toast-header">
                <strong class="me-auto">系统提示</strong>
                <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    
    // 显示toast
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement);
    toast.show();
    
    // 自动移除
    toastElement.addEventListener('hidden.bs.toast', function() {
        toastElement.remove();
    });
}