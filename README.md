# 异步任务框架

基于Spring Boot和责任链模式的异步任务框架，支持任务调度、重试机制和可视化监控。

## 项目概述

本项目是一个通用的异步任务框架，采用责任链模式设计，支持灵活的任务处理器配置和扩展。框架提供了完整的任务生命周期管理，包括任务创建、调度、执行、监控和重试等功能。

### 核心特性

- **责任链模式**: 灵活的处理器链，支持动态配置和扩展
- **异步执行**: 基于线程池的异步任务处理
- **任务调度**: 定时扫描待处理任务并提交执行
- **重试机制**: 支持失败任务自动重试，可配置重试策略
- **状态管理**: 完整的任务状态跟踪和转换
- **监控界面**: Web界面实时监控任务执行情况
- **RESTful API**: 完整的REST API接口

## 技术栈

- **Spring Boot**: 应用框架
- **Spring Data JPA**: 数据访问层
- **MySQL/H2**: 数据库
- **Thymeleaf**: 模板引擎
- **Bootstrap 5**: 前端UI框架
- **Jackson**: JSON处理
- **Lombok**: 代码简化

## 项目结构

```
src/main/java/com/example/asynctask/
├── AsyncTaskApplication.java          # 应用启动类
├── config/                           # 配置类
│   ├── AsyncTaskConfig.java         # 任务配置属性
│   ├── ThreadPoolConfig.java        # 线程池配置
│   └── BootstrapConfig.java         # 启动配置
├── context/                          # 任务上下文
│   ├── TaskContext.java             # 任务上下文
│   └── TaskHandlerResult.java       # 处理器结果
├── controller/                       # 控制器
│   ├── TaskController.java          # 任务API控制器
│   └── WebController.java           # Web页面控制器
├── dto/                             # 数据传输对象
│   ├── ApiResponse.java             # API响应格式
│   ├── TaskRequest.java             # 任务请求
│   └── TaskResponse.java            # 任务响应
├── entity/                          # 实体类
│   └── AsyncTask.java               # 异步任务实体
├── handler/                         # 处理器
│   ├── TaskHandler.java             # 处理器基类
│   ├── TaskHandlerFactory.java      # 处理器工厂
│   ├── impl/                        # 实现类
│   │   └── DefaultTaskHandlerFactory.java
│   └── video/                       # 视频评分处理器
│       ├── SubtitleCheckHandler.java
│       ├── SubtitleFetchHandler.java
│       └── ScoringHandler.java
├── repository/                      # 数据访问层
│   └── AsyncTaskRepository.java     # 任务仓库
├── service/                         # 服务层
│   ├── AsyncTaskService.java        # 任务服务接口
│   ├── TaskExecutorService.java     # 任务执行器接口
│   ├── TaskSchedulerService.java    # 任务调度器接口
│   └── impl/                        # 实现类
│       ├── AsyncTaskServiceImpl.java
│       ├── TaskExecutorServiceImpl.java
│       └── TaskSchedulerServiceImpl.java
└── util/                           # 工具类
    └── JsonUtils.java              # JSON工具类

src/main/resources/
├── static/                         # 静态资源
│   └── js/                         # JavaScript文件
│       └── index.js                # 首页脚本
├── templates/                      # 模板文件
│   └── index.html                  # 首页
├── application.yml                  # 配置文件
├── application-dev.yml             # 开发环境配置
└── data.sql                        # 初始数据
```

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 5.7+ (可选，默认使用H2内存数据库)

### 运行项目

1. 克隆项目到本地

```bash
git clone <repository-url>
cd java-explore
```

2. 构建项目

```bash
mvn clean install
```

3. 运行项目

```bash
mvn spring-boot:run
```

4. 访问应用

- 首页: http://localhost:8080/async-task/
- API文档: http://localhost:8080/async-task/api/tasks

### 配置数据库

默认使用H2内存数据库，如需使用MySQL，修改`application.yml`中的数据源配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/async_task?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 使用指南

### 创建任务

通过REST API创建任务：

```bash
curl -X POST http://localhost:8080/async-task/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "VIDEO_SCORING",
    "taskName": "测试视频评分任务",
    "creator": "admin",
    "parameters": {
      "videoId": "test_video_001"
    },
    "priority": 5
  }'
```

或者通过Web界面：

1. 访问 http://localhost:8080/async-task/
2. 点击"创建视频评分任务"按钮
3. 填写表单并提交

### 查看任务状态

通过API获取任务详情：

```bash
curl http://localhost:8080/async-task/api/tasks/{taskId}
```

### 任务状态

- **PENDING**: 待处理
- **PROCESSING**: 处理中
- **COMPLETED**: 已完成
- **FAILED**: 失败
- **PAUSED**: 暂停

### 任务处理器

#### 视频评分任务处理器链

1. **SubtitleCheckHandler**: 检查视频是否有字幕
2. **SubtitleFetchHandler**: 获取视频字幕内容
3. **ScoringHandler**: 基于字幕内容进行视频评分

### 自定义处理器

1. 继承`TaskHandler`基类

```java
@Component
public class CustomHandler extends TaskHandler {
    
    @Override
    protected TaskHandlerResult doHandle(TaskContext context) throws Exception {
        // 实现自定义处理逻辑
        return TaskHandlerResult.success("处理完成");
    }
    
    @Override
    public boolean supports(String taskType) {
        return "CUSTOM_TASK".equals(taskType);
    }
}
```

2. 处理器会自动注册到工厂中，可在任务配置中使用

## 配置说明

### 任务配置

```yaml
async-task:
  # 线程池配置
  thread-pool:
    core-pool-size: 5
    max-pool-size: 20
    queue-capacity: 100
    
  # 调度器配置
  scheduler:
    enabled: true
    poll-interval: 5000  # 轮询间隔(毫秒)
    batch-size: 10       # 每次处理的任务数量
    
  # 重试配置
  retry:
    max-attempts: 3
    initial-delay: 1000
    multiplier: 2.0
```

### 任务处理器配置

任务处理器通过JSON配置：

```json
{
  "handlerChain": ["Handler1", "Handler2", "Handler3"],
  "maxRetries": 3,
  "timeoutSeconds": 300,
  "priority": 5,
  "parameters": {
    "key1": "value1",
    "key2": "value2"
  }
}
```

## API文档

### 任务管理

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/tasks | 创建任务 |
| GET | /api/tasks/{taskId} | 获取任务详情 |
| GET | /api/tasks | 分页查询任务 |
| POST | /api/tasks/{taskId}/execute | 立即执行任务 |
| POST | /api/tasks/{taskId}/retry | 重试任务 |
| POST | /api/tasks/{taskId}/pause | 暂停任务 |
| POST | /api/tasks/{taskId}/cancel | 取消任务 |
| DELETE | /api/tasks/{taskId} | 删除任务 |

### 任务统计

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/tasks/stats | 获取任务统计信息 |

### 调度器管理

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/tasks/schedule | 手动触发任务调度 |
| POST | /api/tasks/scheduler/start | 启动调度器 |
| POST | /api/tasks/scheduler/stop | 停止调度器 |

## 开发指南

### 添加新的任务类型

1. 定义任务类型常量
2. 实现对应的处理器类
3. 在`TaskContext`中添加默认处理器链逻辑
4. 更新文档和配置

### 扩展处理器

1. 继承`TaskHandler`基类
2. 实现`doHandle`方法
3. 添加`@Component`注解
4. 实现特定任务类型的支持

### 自定义任务调度策略

1. 实现`TaskSchedulerService`接口
2. 重写调度逻辑
3. 配置为Spring Bean

## 注意事项

1. 线程池大小应根据服务器资源和任务特性进行调整
2. 超时时间应合理设置，避免长时间阻塞
3. 重试次数不宜过多，避免资源浪费
4. 任务上下文数据大小应合理控制，避免内存占用过高
5. 生产环境建议使用持久化数据库，避免数据丢失

## 许可证

本项目采用MIT许可证，详情请参阅LICENSE文件。

## 贡献

欢迎提交Issue和Pull Request来改进本项目。

## 联系方式

如有问题或建议，请通过Issue联系我们。