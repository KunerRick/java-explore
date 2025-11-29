# 异步任务管理系统

基于Spring Boot和责任链模式的异步任务管理系统，支持视频评分等复杂业务流程。

## 功能特性

- **责任链模式**: 使用责任链模式实现任务处理流程
- **数据持久化**: 使用MySQL+MyBatis存储任务数据和执行过程
- **异步执行**: 支持异步任务执行和并发控制
- **任务监控**: 提供任务执行状态监控和统计
- **任务恢复**: 支持服务重启后任务恢复
- **API接口**: 提供RESTful API进行任务管理

## 系统架构

```
┌─────────────────┐    ┌──────────────┐    ┌─────────────────┐
│   TaskController│───▶│  TaskService │───▶│ TaskHandlerChain│
└─────────────────┘    └──────────────┘    └─────────────────┘
                               │                     │
                               ▼                     ▼
                        ┌──────────────┐    ┌─────────────────┐
                        │ AsyncTaskRepo│    │ AbstractHandler │
                        └──────────────┘    └─────────────────┘
                                                     │
                                                     ▼
                                            ┌─────────────────┐
                                            │SpecificHandlers │
                                            │(VideoScore...)  │
                                            └─────────────────┘
```

## 快速开始

### 1. 环境准备

- JDK 8+
- MySQL 5.7+
- Maven 3.6+

### 2. 数据库准备

1. 创建MySQL数据库：
```sql
CREATE DATABASE test;
```

2. 执行`database_schema.sql`创建表结构

### 3. 配置修改

修改`src/main/resources/application.yml`中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test
    username: root
    password: ewqazxck
```

### 4. 运行应用

```bash
mvn spring-boot:run
```

### 5. 测试API

应用启动后，可以通过以下API测试系统：

#### 创建视频评分任务
```bash
curl -X POST http://localhost:8080/api/example/video-scoring \
  -H "Content-Type: application/json" \
  -d '{"videoId": "1001", "videoName": "测试视频"}'
```

#### 查询任务状态
```bash
curl http://localhost:8080/api/example/video-scoring/{taskId}/status
```

#### 批量创建任务
```bash
curl -X POST http://localhost:8080/api/example/video-scoring/batch \
  -H "Content-Type: application/json" \
  -d '{"count": 5}'
```

#### 查看任务统计
```bash
curl http://localhost:8080/api/tasks/statistics
```

## 视频评分流程

视频评分任务包含以下步骤：

1. **CheckSubtitleHandler**: 检查视频是否已有字幕
2. **ExtractSubtitleHandler**: 如果没有字幕，则提取字幕
3. **VideoScoreHandler**: 基于字幕内容进行评分

## 自定义任务处理器

要添加新的任务处理器，请遵循以下步骤：

1. 继承`AbstractTaskHandler`类
2. 实现`doHandle`方法
3. 使用`@Component`注解标记为Spring组件
4. 在`TaskHandlerFactory`中配置处理器链

示例：
```java
@Component
public class CustomHandler extends AbstractTaskHandler {
    @Override
    protected HandleResult doHandle(TaskContext context) {
        // 实现自定义逻辑
        return HandleResult.success("处理成功");
    }
    
    @Override
    public String getName() {
        return "CustomHandler";
    }
}
```

## 监控与日志

- 任务执行日志会记录在控制台
- 可通过API查看任务统计信息
- 支持任务执行超时检测和自动恢复

## 注意事项

- 系统使用乐观锁控制并发更新
- 任务状态变更会自动持久化到数据库
- 支持任务重试机制，默认最大重试3次
- 服务重启后会自动恢复中断的任务

## 技术栈

- Spring Boot 2.7.10
- MySQL 8.0
- MyBatis
- Lombok
- Jackson