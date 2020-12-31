package io.github.jiashunx.tools.sqlite3.connection;

import io.github.jiashunx.tools.sqlite3.exception.SQLite3SQLException;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class SQLite3PreparedStatement implements AutoCloseable {

    private final PreparedStatement preparedStatement;

    public SQLite3PreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = Objects.requireNonNull(preparedStatement);
    }

    public ResultSet executeQuery() throws SQLite3SQLException {
        try {
            return preparedStatement.executeQuery();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int executeUpdate() throws SQLite3SQLException {
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLite3SQLException {
        try {
            preparedStatement.setNull(parameterIndex, sqlType);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLite3SQLException {
        try {
            preparedStatement.setBoolean(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setByte(int parameterIndex, byte x) throws SQLite3SQLException {
        try {
            preparedStatement.setByte(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setShort(int parameterIndex, short x) throws SQLite3SQLException {
        try {
            preparedStatement.setShort(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setInt(int parameterIndex, int x) throws SQLite3SQLException {
        try {
            preparedStatement.setInt(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setLong(int parameterIndex, long x) throws SQLite3SQLException {
        try {
            preparedStatement.setLong(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLite3SQLException {
        try {
            preparedStatement.setFloat(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setDouble(int parameterIndex, double x) throws SQLite3SQLException {
        try {
            preparedStatement.setDouble(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLite3SQLException {
        try {
            preparedStatement.setBigDecimal(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setString(int parameterIndex, String x) throws SQLite3SQLException {
        try {
            preparedStatement.setString(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLite3SQLException {
        try {
            preparedStatement.setBytes(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setDate(int parameterIndex, Date x) throws SQLite3SQLException {
        try {
            preparedStatement.setDate(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setTime(int parameterIndex, Time x) throws SQLite3SQLException {
        try {
            preparedStatement.setTime(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLite3SQLException {
        try {
            preparedStatement.setTimestamp(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLite3SQLException {
        try {
            preparedStatement.setAsciiStream(parameterIndex, x, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLite3SQLException {
        try {
            preparedStatement.setUnicodeStream(parameterIndex, x, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLite3SQLException {
        try {
            preparedStatement.setBinaryStream(parameterIndex, x, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void clearParameters() throws SQLite3SQLException {
        try {
            preparedStatement.clearParameters();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLite3SQLException {
        try {
            preparedStatement.setObject(parameterIndex, x, targetSqlType);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setObject(int parameterIndex, Object x) throws SQLite3SQLException {
        try {
            preparedStatement.setObject(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean execute() throws SQLite3SQLException {
        try {
            return preparedStatement.execute();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void addBatch() throws SQLite3SQLException {
        try {
            preparedStatement.addBatch();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLite3SQLException {
        try {
            preparedStatement.setCharacterStream(parameterIndex, reader, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setRef(int parameterIndex, Ref x) throws SQLite3SQLException {
        try {
            preparedStatement.setRef(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setBlob(int parameterIndex, Blob x) throws SQLite3SQLException {
        try {
            preparedStatement.setBlob(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setClob(int parameterIndex, Clob x) throws SQLite3SQLException {
        try {
            preparedStatement.setClob(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setArray(int parameterIndex, Array x) throws SQLite3SQLException {
        try {
            preparedStatement.setArray(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public ResultSetMetaData getMetaData() throws SQLite3SQLException {
        try {
            return preparedStatement.getMetaData();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLite3SQLException {
        try {
            preparedStatement.setDate(parameterIndex, x, cal);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLite3SQLException {
        try {
            preparedStatement.setTime(parameterIndex, x, cal);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLite3SQLException {
        try {
            preparedStatement.setTimestamp(parameterIndex, x, cal);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLite3SQLException {
        try {
            preparedStatement.setNull(parameterIndex, sqlType, typeName);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setURL(int parameterIndex, URL x) throws SQLite3SQLException {
        try {
            preparedStatement.setURL(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public ParameterMetaData getParameterMetaData() throws SQLite3SQLException {
        try {
            return preparedStatement.getParameterMetaData();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLite3SQLException {
        try {
            preparedStatement.setRowId(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setNString(int parameterIndex, String value) throws SQLite3SQLException {
        try {
            preparedStatement.setNString(parameterIndex, value);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLite3SQLException {
        try {
            preparedStatement.setNCharacterStream(parameterIndex, value, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLite3SQLException {
        try {
            preparedStatement.setNClob(parameterIndex, value);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLite3SQLException {
        try {
            preparedStatement.setClob(parameterIndex, reader, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLite3SQLException {
        try {
            preparedStatement.setBlob(parameterIndex, inputStream, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLite3SQLException {
        try {
            preparedStatement.setNClob(parameterIndex, reader, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLite3SQLException {
        try {
            preparedStatement.setSQLXML(parameterIndex, xmlObject);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLite3SQLException {
        try {
            preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLite3SQLException {
        try {
            preparedStatement.setAsciiStream(parameterIndex, x, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLite3SQLException {
        try {
            preparedStatement.setBinaryStream(parameterIndex, x, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLite3SQLException {
        try {
            preparedStatement.setCharacterStream(parameterIndex, reader, length);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLite3SQLException {
        try {
            preparedStatement.setAsciiStream(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLite3SQLException {
        try {
            preparedStatement.setBinaryStream(parameterIndex, x);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLite3SQLException {
        try {
            preparedStatement.setCharacterStream(parameterIndex, reader);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLite3SQLException {
        try {
            preparedStatement.setNCharacterStream(parameterIndex, value);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLite3SQLException {
        try {
            preparedStatement.setClob(parameterIndex, reader);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLite3SQLException {
        try {
            preparedStatement.setBlob(parameterIndex, inputStream);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLite3SQLException {
        try {
            preparedStatement.setNClob(parameterIndex, reader);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public ResultSet executeQuery(String sql) throws SQLite3SQLException {
        try {
            return preparedStatement.executeQuery(sql);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int executeUpdate(String sql) throws SQLite3SQLException {
        try {
            return preparedStatement.executeUpdate(sql);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void close() throws SQLite3SQLException {
        try {
            preparedStatement.close();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int getMaxFieldSize() throws SQLite3SQLException {
        try {
            return preparedStatement.getMaxFieldSize();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setMaxFieldSize(int max) throws SQLite3SQLException {
        try {
            preparedStatement.setMaxFieldSize(max);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int getMaxRows() throws SQLite3SQLException {
        try {
            return preparedStatement.getMaxRows();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setMaxRows(int max) throws SQLite3SQLException {
        try {
            preparedStatement.setMaxRows(max);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setEscapeProcessing(boolean enable) throws SQLite3SQLException {
        try {
            preparedStatement.setEscapeProcessing(enable);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int getQueryTimeout() throws SQLite3SQLException {
        try {
            return preparedStatement.getQueryTimeout();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setQueryTimeout(int seconds) throws SQLite3SQLException {
        try {
            preparedStatement.setQueryTimeout(seconds);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void cancel() throws SQLite3SQLException {
        try {
            preparedStatement.cancel();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public SQLWarning getWarnings() throws SQLite3SQLException {
        try {
            return preparedStatement.getWarnings();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void clearWarnings() throws SQLite3SQLException {
        try {
            preparedStatement.clearWarnings();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setCursorName(String name) throws SQLite3SQLException {
        try {
            preparedStatement.setCursorName(name);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean execute(String sql) throws SQLite3SQLException {
        try {
            return preparedStatement.execute(sql);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public ResultSet getResultSet() throws SQLite3SQLException {
        try {
            return preparedStatement.getResultSet();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int getUpdateCount() throws SQLite3SQLException {
        try {
            return preparedStatement.getUpdateCount();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean getMoreResults() throws SQLite3SQLException {
        try {
            return preparedStatement.getMoreResults();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setFetchDirection(int direction) throws SQLite3SQLException {
        try {
            preparedStatement.setFetchDirection(direction);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int getFetchDirection() throws SQLite3SQLException {
        try {
            return preparedStatement.getFetchDirection();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setFetchSize(int rows) throws SQLite3SQLException {
        try {
            preparedStatement.setFetchSize(rows);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int getFetchSize() throws SQLite3SQLException {
        try {
            return preparedStatement.getFetchSize();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int getResultSetConcurrency() throws SQLite3SQLException {
        try {
            return preparedStatement.getResultSetConcurrency();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int getResultSetType() throws SQLite3SQLException {
        try {
            return preparedStatement.getResultSetType();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void addBatch(String sql) throws SQLite3SQLException {
        try {
            preparedStatement.addBatch(sql);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void clearBatch() throws SQLite3SQLException {
        try {
            preparedStatement.clearBatch();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int[] executeBatch() throws SQLite3SQLException {
        try {
            return preparedStatement.executeBatch();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public Connection getConnection() throws SQLite3SQLException {
        try {
            return preparedStatement.getConnection();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean getMoreResults(int current) throws SQLite3SQLException {
        try {
            return preparedStatement.getMoreResults(current);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public ResultSet getGeneratedKeys() throws SQLite3SQLException {
        try {
            return preparedStatement.getGeneratedKeys();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLite3SQLException {
        try {
            return preparedStatement.executeUpdate(sql, autoGeneratedKeys);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLite3SQLException {
        try {
            return preparedStatement.executeUpdate(sql, columnIndexes);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLite3SQLException {
        try {
            return preparedStatement.executeUpdate(sql, columnNames);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLite3SQLException {
        try {
            return preparedStatement.execute(sql, autoGeneratedKeys);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLite3SQLException {
        try {
            return preparedStatement.execute(sql, columnIndexes);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean execute(String sql, String[] columnNames) throws SQLite3SQLException {
        try {
            return preparedStatement.execute(sql, columnNames);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public int getResultSetHoldability() throws SQLite3SQLException {
        try {
            return preparedStatement.getResultSetHoldability();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean isClosed() throws SQLite3SQLException {
        try {
            return preparedStatement.isClosed();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void setPoolable(boolean poolable) throws SQLite3SQLException {
        try {
            preparedStatement.setPoolable(poolable);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean isPoolable() throws SQLite3SQLException {
        try {
            return preparedStatement.isPoolable();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public void closeOnCompletion() throws SQLite3SQLException {
        try {
            preparedStatement.closeOnCompletion();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean isCloseOnCompletion() throws SQLite3SQLException {
        try {
            return preparedStatement.isCloseOnCompletion();
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public <T> T unwrap(Class<T> iface) throws SQLite3SQLException {
        try {
            return preparedStatement.unwrap(iface);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLite3SQLException {
        try {
            return preparedStatement.isWrapperFor(iface);
        } catch (SQLException exception) {
            throw new SQLite3SQLException(exception);
        }
    }
}
