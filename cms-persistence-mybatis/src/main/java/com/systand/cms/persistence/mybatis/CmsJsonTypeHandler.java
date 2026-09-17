package com.systand.cms.persistence.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import tools.jackson.databind.ObjectMapper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** PostgreSQL jsonb mapping owned by CMS, without a platform type handler. */
@MappedJdbcTypes(JdbcType.OTHER)
public final class CmsJsonTypeHandler<T> extends BaseTypeHandler<T> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Class<T> type;

    public CmsJsonTypeHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement statement, int index, T value, JdbcType jdbcType)
            throws SQLException {
        statement.setObject(index, MAPPER.writeValueAsString(value), Types.OTHER);
    }

    @Override
    public T getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return read(resultSet.getString(columnName));
    }

    @Override
    public T getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return read(resultSet.getString(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement statement, int columnIndex) throws SQLException {
        return read(statement.getString(columnIndex));
    }

    private T read(String json) {
        return json == null ? null : MAPPER.readValue(json, type);
    }
}
