package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.model.ResultColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author jiashunx
 */
public class SQLite3JdbcTemplate {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3JdbcTemplate.class);

    private SQLite3ConnectionPool connectionPool;

    public SQLite3JdbcTemplate(String fileName) {
        this(SQLite3Manager.getConnectionPool(fileName));
    }

    public SQLite3JdbcTemplate(SQLite3ConnectionPool pool) {
        this();
        this.connectionPool = Objects.requireNonNull(pool);
    }

    public SQLite3JdbcTemplate() {}

    public SQLite3ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public void query(Consumer<Connection> consumer) {
        query(getConnectionPool().fetch(), consumer);
    }

    public void query(SQLite3Connection connection, Consumer<Connection> consumer) {
        connection.read(c -> {
            try {
                consumer.accept(c);
            } finally {
                connection.release();
            }
        });
    }

    public <R> R query(Function<Connection, R> function) {
        return query(getConnectionPool().fetch(), function);
    }

    public <R> R query(SQLite3Connection connection, Function<Connection, R> function) {
        return connection.read(c -> {
            try {
                return function.apply(c);
            } finally {
                connection.release();
            }
        });
    }

    public void write(Consumer<Connection> consumer) {
        write(getConnectionPool().fetch(), consumer);
    }

    public void write(SQLite3Connection connection, Consumer<Connection> consumer) {
        connection.write(c -> {
            try {
                consumer.accept(c);
            } finally {
                connection.release();
            }
        });
    }

    public <R> R write(Function<Connection, R> function) {
        return write(getConnectionPool().fetch(), function);
    }

    public <R> R write(SQLite3Connection connection, Function<Connection, R> function) {
        return connection.write(c -> {
            try {
                return function.apply(c);
            } finally {
                connection.release();
            }
        });
    }

    public int executeUpdate(String sql) {
        return write(connection -> {
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.createStatement();
                return statement.executeUpdate(sql);
            } catch (Throwable throwable) {
                if (logger.isErrorEnabled()) {
                    logger.error("sql update failed: {}", sql, throwable);
                }
            } finally {
                close(resultSet);
                close(statement);
            }
           return -1;
        });
    }

    public Map<String, Object> queryForMap(String sql) {
        List<Map<String, Object>> mapList = queryForList(sql);
        Map<String, Object> retMap = null;
        if (mapList != null && !mapList.isEmpty()) {
            if (mapList.size() == 1) {
                return mapList.get(0);
            }
            // TODO 自定义相关异常
            throw new RuntimeException("more than one row returned.");
        }
        return retMap;
    }

    public List<Map<String, Object>> queryForList(String sql) {
        return query(connection -> {
            List<Map<String, Object>> retMapList = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(sql);
                retMapList = getResultMapList(resultSet);
            } catch (Throwable throwable) {
                if (logger.isErrorEnabled()) {
                    logger.error("sql query failed: {}", sql, throwable);
                }
            } finally {
                close(resultSet);
                close(statement);
            }
            return retMapList;
        });
    }

    public boolean isTableExists(String tableName) {
        // TODO 后续调整为预编译
        Map<String, Object> resultMap = queryForMap(String.format("SELECT COUNT(1) COUNT FROM %s LIMIT 1", tableName));
        return resultMap != null && resultMap.get("COUNT") != null && Integer.parseInt(resultMap.get("COUNT").toString()) >= 0;
    }

    public static List<Map<String, Object>> getResultMapList(ResultSet resultSet) throws SQLException {
        if (resultSet == null) {
            return null;
        }
        List<Map<String, Object>> retMapList = new ArrayList<>();
        Map<String, ResultColumn> resultColumnMap = getResultColumnMap(resultSet);
        while (resultSet.next()) {
            Map<String, Object> rowMap = new HashMap<>();
            for (Map.Entry<String, ResultColumn> entry: resultColumnMap.entrySet()) {
                String columnName = entry.getKey();
                ResultColumn columnModel = entry.getValue();
                String columnLabel = columnModel.getColumnLabel();
                Object columnValue = null;
                switch (columnModel.getColumnType()) {
                    case Types.BIT:
                        columnValue = resultSet.getBoolean(columnLabel);
                        break;
                    case Types.TINYINT:
                        columnValue = resultSet.getByte(columnLabel);
                        break;
                    case Types.SMALLINT:
                        columnValue = resultSet.getShort(columnLabel);
                        break;
                    case Types.INTEGER:
                        columnValue = resultSet.getInt(columnLabel);
                        break;
                    case Types.BIGINT:
                        columnValue = resultSet.getLong(columnLabel);
                        break;
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                        columnValue = resultSet.getDouble(columnLabel);
                        break;
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        columnValue = resultSet.getBigDecimal(columnLabel);
                        break;
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                        columnValue = resultSet.getString(columnLabel);
                        break;
                    case Types.DATE:
                        columnValue = resultSet.getDate(columnLabel);
                        break;
                    case Types.TIME:
                        columnValue = resultSet.getTime(columnLabel);
                        break;
                    case Types.TIMESTAMP:
                        columnValue = resultSet.getTimestamp(columnLabel);
                        break;
                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        columnValue = resultSet.getBytes(columnLabel);
                        break;
                    default:
                        columnValue = resultSet.getObject(columnLabel);
                        break;
                }
                rowMap.put(columnName, columnValue);
            }
            retMapList.add(rowMap);
        }
        return retMapList;
    }

    public static Map<String, ResultColumn> getResultColumnMap(ResultSet resultSet) throws SQLException {
        Map<String, ResultColumn> retMap = null;
        if (resultSet != null) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            retMap = new HashMap<>();
            for (int index = 1; index <= columnCount; index++) {
                ResultColumn resultColumn = new ResultColumn();
                String columnName = metaData.getColumnName(index);
                resultColumn.setColumnName(columnName);
                resultColumn.setColumnLabel(metaData.getColumnLabel(index));
                resultColumn.setColumnType(metaData.getColumnType(index));
                resultColumn.setColumnTypeName(metaData.getColumnTypeName(index));
                resultColumn.setColumnClassName(metaData.getColumnClassName(index));
                resultColumn.setColumnDisplaySize(metaData.getColumnDisplaySize(index));
                resultColumn.setCatalogName(metaData.getCatalogName(index));
                resultColumn.setPrecision(metaData.getPrecision(index));
                resultColumn.setScale(metaData.getScale(index));
                resultColumn.setSchemaName(metaData.getSchemaName(index));
                resultColumn.setTableName(metaData.getTableName(index));
                resultColumn.setAutoIncrement(metaData.isAutoIncrement(index));
                resultColumn.setCaseSensitive(metaData.isCaseSensitive(index));
                resultColumn.setCurrency(metaData.isCurrency(index));
                resultColumn.setDefinitelyWritable(metaData.isDefinitelyWritable(index));
                resultColumn.setNullable(metaData.isNullable(index));
                resultColumn.setReadOnly(metaData.isReadOnly(index));
                resultColumn.setWritable(metaData.isWritable(index));
                resultColumn.setSearchable(metaData.isSearchable(index));
                resultColumn.setSigned(metaData.isSigned(index));
                retMap.put(columnName, resultColumn);
            }
        }
        return retMap;
    }

    public static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("close AutoCloseable object failed.", throwable);
            }
        }
    }

}
