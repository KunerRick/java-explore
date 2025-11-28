# JPA到MyBatis迁移指南

本文档说明如何将项目从JPA迁移到MyBatis，以及相关的注意事项。

## 迁移步骤

### 1. 更新依赖

将以下依赖添加到pom.xml：

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.2.2</version>
</dependency>
```

移除JPA相关依赖：

```xml
<!-- <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency> -->
```

### 2. 配置文件更新

在application.yml中添加MyBatis配置：

```yaml
# MyBatis配置
mybatis:
  config-location: classpath:mybatis-config.xml
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.asynctask.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
    lazy-loading-enabled: true
    default-statement-timeout: 30
    default-fetch-size: 100
    use-actual-param-name: true
```

移除JPA相关配置：

```yaml
# 移除以下配置
# jpa:
#   hibernate:
#     ddl-auto: update
#   show-sql: true
#   properties:
#     hibernate:
#       dialect: org.hibernate.dialect.MySQL8Dialect
```

### 3. 创建MyBatis配置文件

创建`src/main/resources/mybatis-config.xml`文件，配置MyBatis的全局设置。

### 4. 创建Mapper接口和XML

为每个实体类创建对应的Mapper接口和XML文件：

- AsyncTaskMapper.java 和 AsyncTaskMapper.xml
- TaskDetailMapper.java 和 TaskDetailMapper.xml
- TaskEventLogMapper.java 和 TaskEventLogMapper.xml

### 5. 创建类型处理器

创建自定义类型处理器，处理JSON和枚举类型：

- JsonTypeHandler.java
- TaskStatusTypeHandler.java
- StepStatusTypeHandler.java
- EventTypeTypeHandler.java

### 6. 更新Service层

创建新的Service接口和实现类，使用MyBatis而不是JPA：

- AsyncTaskServiceMyBatis.java 和 AsyncTaskServiceMyBatisImpl.java
- TaskDetailServiceMyBatis.java 和 TaskDetailServiceMyBatisImpl.java
- TaskEventLogServiceMyBatis.java 和 TaskEventLogServiceMyBatisImpl.java

### 7. 更新Controller层

创建新的Controller类，使用MyBatis Service：

- TaskControllerMyBatis.java

### 8. 添加配置类

创建MyBatis相关配置类：

- MyBatisConfig.java - 用于扫描Mapper接口
- MyBatisCacheConfig.java - 配置缓存
- MyBatisPerformanceConfig.java - 性能监控

### 9. 启用MyBatis扫描

在应用启动类上添加`@MapperScan`注解，或在配置类中添加。

## 性能优化

### 1. 二级缓存配置

在Mapper XML文件中添加缓存配置：

```xml
<!-- 启用二级缓存，使用LRU策略，60秒过期，最多缓存1000个对象 -->
<cache eviction="LRU" flushInterval="60000" size="1000" readOnly="true"/>
```

### 2. SQL优化

- 为常用查询添加索引提示
- 使用批量操作减少数据库交互
- 优化查询语句，避免不必要的字段

### 3. 连接池配置

确保数据库连接池配置合理，特别是对于高并发场景。

### 4. 慢SQL监控

使用MyBatis插件监控慢SQL，及时优化：

```java
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class MyBatisPerformanceConfig implements Interceptor {
    // 实现慢SQL监控
}
```

## 数据迁移

如果需要从现有的JPA实现迁移数据，可以：

1. 使用临时profile运行JPA应用，导出数据
2. 使用MyBatis应用导入数据
3. 或者创建专门的迁移工具类

## 注意事项

1. **事务管理**：确保MyBatis和Spring的事务管理正确配置
2. **乐观锁**：MyBatis中需要手动实现乐观锁逻辑
3. **分页查询**：MyBatis需要手动实现分页，或使用PageHelper等插件
4. **懒加载**：MyBatis的懒加载机制与JPA有所不同
5. **缓存策略**：合理配置缓存，避免数据不一致

## 测试

迁移完成后，进行全面测试：

1. 功能测试：确保所有功能正常工作
2. 性能测试：比较迁移前后的性能
3. 并发测试：确保高并发场景下数据一致性

## 回滚方案

如果迁移过程中出现问题，可以：

1. 保留JPA实现作为备份
2. 使用版本控制回滚
3. 恢复数据库备份

## 最佳实践

1. **渐进式迁移**：可以分模块逐步迁移，降低风险
2. **A/B测试**：同时运行两套实现，逐步切换流量
3. **充分测试**：在生产环境部署前进行充分测试
4. **监控告警**：添加完善的监控和告警机制
5. **文档更新**：及时更新相关文档和操作手册