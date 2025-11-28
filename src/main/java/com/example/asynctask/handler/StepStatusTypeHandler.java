package com.example.asynctask.handler;

import com.example.asynctask.entity.TaskDetail.StepStatus;
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
 * StepStatus枚举类型处理器
 * 
 * @author System
 */
@Component
@MappedTypes(StepStatus.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class StepStatusTypeHandler extends BaseTypeHandler<StepStatus> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, StepStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }
    
    @Override
    public StepStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : StepStatus.valueOf(value);
    }
    
    @Override
    public StepStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : StepStatus.valueOf(value);
    }
    
    @Override
    public StepStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : StepStatus.valueOf(value);
    }
}