package com.example.asynctask.handler;

import com.example.asynctask.entity.TaskEventLog.EventType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * EventType枚举类型处理器
 * 
 * @author System
 */
@Component
@MappedTypes(EventType.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class EventTypeTypeHandler extends BaseTypeHandler<EventType> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EventType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }
    
    @Override
    public EventType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : EventType.valueOf(value);
    }
    
    @Override
    public EventType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : EventType.valueOf(value);
    }
    
    @Override
    public EventType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : EventType.valueOf(value);
    }
}