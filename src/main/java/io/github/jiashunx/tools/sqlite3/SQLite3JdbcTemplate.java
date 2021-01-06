package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.connection.SQLite3Connection;
import io.github.jiashunx.tools.sqlite3.connection.SQLite3ConnectionManager;
import io.github.jiashunx.tools.sqlite3.connection.SQLite3ConnectionPool;
import io.github.jiashunx.tools.sqlite3.connection.SQLite3PreparedStatement;
import io.github.jiashunx.tools.sqlite3.exception.SQLite3MappingException;
import io.github.jiashunx.tools.sqlite3.exception.SQLite3SQLException;
import io.github.jiashunx.tools.sqlite3.model.*;
import io.github.jiashunx.tools.sqlite3.table.SQLPackage;
import io.github.jiashunx.tools.sqlite3.util.SQLite3Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author jiashunx
 */
public class SQLite3JdbcTemplate {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3JdbcTemplate.class);

    private static final Consumer<SQLite3PreparedStatement> PREPARED_STATEMENT_CONSUMER = statement -> {};

    private SQLite3ConnectionPool connectionPool;

    public SQLite3JdbcTemplate(String fileName) {
        this(SQLite3ConnectionManager.getConnectionPool(fileName));
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
        query(getConnectionPool().fetchReadConnection(), consumer);
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
        return query(getConnectionPool().fetchReadConnection(), function);
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
        write(getConnectionPool().fetchWriteConnection(), consumer);
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
        return write(getConnectionPool().fetchWriteConnection(), function);
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

    public <R> R queryForObj(String sql, Class<R> klass) throws SQLite3SQLException, SQLite3MappingException {
        return queryForObj(sql, statement -> {}, klass);
    }

    public <R> R queryForObj(String sql, Consumer<SQLite3PreparedStatement> consumer, Class<R> klass)
            throws SQLite3SQLException, SQLite3MappingException {
        List<R> retList = queryForList(sql, consumer, klass);
        if (retList == null || retList.isEmpty()) {
            return null;
        }
        if (retList.size() > 1) {
            throw new SQLite3SQLException(String.format("query result contains more than one column, sql: %s", sql));
        }
        return retList.get(0);
    }

    public <R> List<R> queryForList(String sql, Class<R> klass) throws SQLite3SQLException, SQLite3MappingException {
        return queryForList(sql, statement -> {}, klass);
    }

    public <R> List<R> queryForList(String sql, Consumer<SQLite3PreparedStatement> consumer, Class<R> klass)
            throws SQLite3SQLException, SQLite3MappingException {
        return SQLite3Utils.parseQueryResult(queryForResult(sql, consumer), klass);
    }

    public int update(Object object) throws SQLite3SQLException, SQLite3MappingException {
        List<Object> objectList = new ArrayList<>(1);
        objectList.add(object);
        return update(objectList);
    }

    public int update(List<?> objList) throws SQLite3SQLException, SQLite3MappingException {
        return updateOrInsert(objList, TableModel::getUpdateSQL);
    }

    private int updateOrInsert(List<?> objList, Function<TableModel, String> sqlFunc) throws SQLite3SQLException, SQLite3MappingException {
        int retValue = 0;
        List<?> $objList = Objects.requireNonNull(objList);
        if (!$objList.isEmpty()) {
            List<String> sqlList = new ArrayList<>();
            List<Consumer<SQLite3PreparedStatement>> consumerList = new ArrayList<>();
            $objList.forEach(object -> {
                Object $object = Objects.requireNonNull(object);
                Class<?> objClass = $object.getClass();
                TableModel tableModel = SQLite3Utils.getClassTableModel(objClass);
                Map<String, ColumnMetadata> columnMetadata = tableModel.getColumnMetadata();
                if (columnMetadata == null) {
                    columnMetadata = queryTableColumnMetadata(tableModel.getTableName());
                    tableModel.setColumnMetadata(columnMetadata);
                }
                sqlList.add(sqlFunc.apply(tableModel));
                consumerList.add(SQLite3Utils.buildTableConsumer($object, tableModel));
            });
            int[] intArr = batchUpdate(sqlList.toArray(new String[0]), (index, statement) -> {
                consumerList.get(index).accept(statement);
            });
            for (int value: intArr) {
                retValue += value;
            }
        }
        return retValue;
    }

    public int insert(Object object) throws SQLite3SQLException, SQLite3MappingException {
        List<Object> objectList = new ArrayList<>(1);
        objectList.add(object);
        return insert(objectList);
    }

    public int insert(List<?> objList) throws SQLite3SQLException, SQLite3MappingException {
        return updateOrInsert(objList, TableModel::getInsertSQL);
    }

    public int[] batchUpdate(String[] sqlArr) throws SQLite3SQLException {
        return batchUpdate(sqlArr, (index, statement) -> {});
    }

    public int[] batchUpdate(String[] sqlArr, BiConsumer<Integer, SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return write(connection -> {
            int[] effectedRowArr = new int[sqlArr.length];
            SQLite3PreparedStatement statement = null;
            try {
                connection.setAutoCommit(false);
                for (int index = 0; index < sqlArr.length; index++) {
                    String sql = sqlArr[index];
                    try {
                        statement = new SQLite3PreparedStatement(connection.prepareStatement(sql));
                        if (consumer != null) {
                            consumer.accept(index, statement);
                        }
                        effectedRowArr[index] = statement.executeUpdate();
                    } catch (Throwable exception) {
                        throw new SQLite3SQLException(String.format("execute batch update failed, single sql: %s", sql), exception);
                    } finally {
                        SQLite3Utils.close(statement);
                        statement = null;
                    }
                }
                connection.commit();
                return effectedRowArr;
            } catch (Throwable exception) {
                try {
                    connection.rollback();
                } catch (SQLException exception1) {
                    throw new SQLite3SQLException(String.format(
                            "execute batch update failed (rollback failed, reason: %s.)"
                            , exception1.getMessage()), exception);
                }
                throw new SQLite3SQLException("execute batch update failed(rollback success)", exception);
            } finally {
                SQLite3Utils.close(statement);
            }
        });
    }

    public int batchUpdate(String sql, int rowCount, BiConsumer<Integer, SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return write(connection -> {
            int effectedRowCount = 0;
            SQLite3PreparedStatement statement = null;
            try {
                connection.setAutoCommit(false);
                statement = new SQLite3PreparedStatement(connection.prepareStatement(sql));
                for (int i = 0; i < rowCount; i++) {
                    if (consumer != null) {
                        consumer.accept(i, statement);
                    }
                    effectedRowCount += statement.executeUpdate();
                    statement.clearParameters();
                }
                connection.commit();
                return effectedRowCount;
            } catch (Throwable exception) {
                try {
                    connection.rollback();
                } catch (SQLException exception1) {
                    throw new SQLite3SQLException(String.format(
                            "execute batch update failed (rollback failed, reason: %s.), sql: %s"
                            , exception1.getMessage(), sql), exception);
                }
                throw new SQLite3SQLException(String.format("execute batch update failed(rollback success), sql: %s", sql), exception);
            } finally {
                SQLite3Utils.close(statement);
            }
        });
    }

    public int executeUpdate(String sql) throws SQLite3SQLException {
        return executeUpdate(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public int executeUpdate(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return write(connection -> {
            SQLite3PreparedStatement statement = null;
            try {
                connection.setAutoCommit(false);
                statement = new SQLite3PreparedStatement(connection.prepareStatement(sql));
                if (consumer != null) {
                    consumer.accept(statement);
                }
                int rowCount = statement.executeUpdate();
                connection.commit();
                return rowCount;
            } catch (Throwable exception) {
                try {
                    connection.rollback();
                } catch (SQLException exception1) {
                    throw new SQLite3SQLException(String.format(
                            "execute update failed (rollback failed, reason: %s.), sql: %s"
                            , exception1.getMessage(), sql), exception);
                }
                throw new SQLite3SQLException(String.format("execute update failed(rollback success), sql: %s", sql), exception);
            } finally {
                SQLite3Utils.close(statement);
            }
        });
    }

    public boolean isTableExists(String tableName) throws SQLite3SQLException {
        return queryForInt("SELECT COUNT(1) FROM sqlite_master M WHERE M.type='table' AND M.name=?", statement -> {
            statement.setString(1, tableName);
        }) == 1;
    }

    public boolean isTableColumnExists(String tableName, String columnName) throws SQLite3SQLException {
        if (isTableExists(tableName)) {
            return queryForString("SELECT M.sql FROM sqlite_master M WHERE M.type='table' AND M.name=?", statement -> {
                statement.setString(1, tableName);
            }).contains(columnName);
        }
        return false;
    }

    public boolean isViewExists(String viewName) throws SQLite3SQLException {
        return queryForInt("SELECT COUNT(1) FROM sqlite_master M WHERE M.type='view' AND M.name=?", statement -> {
            statement.setString(1, viewName);
        }) == 1;
    }

    public boolean isIndexExists(String indexName) throws SQLite3SQLException {
        return queryForInt("SELECT COUNT(1) FROM sqlite_master M WHERE M.type='index' AND M.name=?", statement -> {
            statement.setString(1, indexName);
        }) == 1;
    }

    public String getTableDefineSQL(String tableName) throws SQLite3SQLException {
        if (isTableExists(tableName)) {
            return queryForString("SELECT M.sql FROM sqlite_master M WHERE M.type='table' AND M.name=?", statement -> {
                statement.setString(1, tableName);
            });
        }
        return null;
    }

    public String getViewDefineSQL(String viewName) throws SQLite3SQLException {
        if (isViewExists(viewName)) {
            return queryForString("SELECT M.sql FROM sqlite_master M WHERE M.type='view' AND M.name=?", statement -> {
                statement.setString(1, viewName);
            });
        }
        return null;
    }

    public void initSQLPackage(SQLPackage sqlPackage) throws NullPointerException, SQLite3SQLException {
        if (sqlPackage == null) {
            throw new NullPointerException();
        }
        try {
            sqlPackage.getTableNames().forEach(tableName -> {
                if (!isTableExists(tableName)) {
                    String tableDefineSQL = sqlPackage.getTableDefineSQL(tableName);
                    if (logger.isWarnEnabled()) {
                        logger.warn("table[{}] no exists, prepare create it, sql: {}", tableName, tableDefineSQL);
                    }
                    executeUpdate(tableDefineSQL);
                }
                sqlPackage.getColumns(tableName).forEach(column -> {
                    String columnName = column.getColumnName();
                    if (!isTableColumnExists(tableName, columnName)) {
                        String columnDefineSQL = sqlPackage.getColumnDefineSQL(tableName, columnName);
                        if (logger.isWarnEnabled()) {
                            logger.warn("table[{}] column[{}] not exists, prepare create it, sql: {}", tableName, columnName, columnDefineSQL);
                        }
                        executeUpdate(columnDefineSQL);
                    }
                });
            });
            sqlPackage.getViewNames().forEach(viewName -> {
                if (!isViewExists(viewName)) {
                    String viewDefineSQL = sqlPackage.getViewDefineSQL(viewName);
                    if (logger.isWarnEnabled()) {
                        logger.warn("view[{}] not exists, prepare create it, sql: {}", viewName, viewDefineSQL);
                    }
                    executeUpdate(viewDefineSQL);
                }
            });
        } catch (Throwable throwable) {
            throw new SQLite3SQLException(String.format("init sql package failed, groupId: %s", sqlPackage.getGroupId()), throwable);
        }
    }

    public int queryTableRowCount(String tableName) throws SQLite3SQLException {
        if (!isTableExists(tableName)) {
            return 0;
        }
        return queryForInt("SELECT COUNT(1) FROM " + tableName);
    }

    public Map<String, ColumnMetadata> queryTableColumnMetadata(String tableName) throws SQLite3SQLException {
        String sql = String.format("SELECT * FROM %s LIMIT 0", tableName);
        return query(connection -> {
            SQLite3PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                statement = new SQLite3PreparedStatement(connection.prepareStatement(sql));
                resultSet = statement.executeQuery();
                return SQLite3Utils.parseTableColumnMetadata(resultSet);
            } catch (Throwable exception) {
                throw new SQLite3SQLException(String.format("query table column message failed, sql: %s", sql), exception);
            } finally {
                SQLite3Utils.close(resultSet);
                SQLite3Utils.close(statement);
            }
        });
    }

    public boolean queryForBoolean(String sql) throws SQLite3SQLException {
        return queryForBoolean(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public boolean queryForBoolean(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return Boolean.parseBoolean(queryForString(sql, consumer));
    }

    public byte queryForByte(String sql) throws SQLite3SQLException {
        return queryForByte(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public byte queryForByte(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return Byte.parseByte(queryForString(sql, consumer));
    }

    public short queryForShort(String sql) throws SQLite3SQLException {
        return queryForShort(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public short queryForShort(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return Short.parseShort(queryForString(sql, consumer));
    }

    public int queryForInt(String sql) throws SQLite3SQLException {
        return queryForInt(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public int queryForInt(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return Integer.parseInt(queryForString(sql, consumer));
    }

    public float queryForFloat(String sql) throws SQLite3SQLException {
        return queryForFloat(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public float queryForFloat(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return Float.parseFloat(queryForString(sql, consumer));
    }

    public double queryForDouble(String sql) throws SQLite3SQLException {
        return queryForDouble(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public double queryForDouble(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return Double.parseDouble(queryForString(sql, consumer));
    }

    public String queryForString(String sql) throws SQLite3SQLException {
        return queryForString(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public String queryForString(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return queryForOneValue(sql, consumer).toString();
    }

    public Object queryForOneValue(String sql) throws SQLite3SQLException {
        return queryForOneValue(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public Object queryForOneValue(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        Map<String, Object> resultMap = queryForMap(sql, consumer);
        if (resultMap == null || resultMap.isEmpty()) {
            throw new SQLite3SQLException(String.format("query result is null, sql: %s", sql));
        }
        if (resultMap.size() > 1) {
            throw new SQLite3SQLException(String.format("query result contains more than one column, sql: %s", sql));
        }
        return resultMap.get(resultMap.keySet().toArray(new String[0])[0]);
    }

    public Map<String, Object> queryForMap(String sql) throws SQLite3SQLException {
        return queryForMap(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public Map<String, Object> queryForMap(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        List<Map<String, Object>> mapList = queryForList(sql, consumer);
        Map<String, Object> retMap = null;
        if (mapList != null && !mapList.isEmpty()) {
            if (mapList.size() == 1) {
                return mapList.get(0);
            }
            throw new SQLite3SQLException(String.format("query result contains more than one row, sql: %s", sql));
        }
        return retMap;
    }

    public List<Map<String, Object>> queryForList(String sql) throws SQLite3SQLException {
        return queryForList(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public List<Map<String, Object>> queryForList(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return queryForResult(sql, consumer).getRetMapList();
    }

    public QueryResult queryForResult(String sql) throws SQLite3SQLException {
        return queryForResult(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public QueryResult queryForResult(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return query(connection -> {
            SQLite3PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                statement = new SQLite3PreparedStatement(connection.prepareStatement(sql));
                if (consumer != null) {
                    consumer.accept(statement);
                }
                resultSet = statement.executeQuery();
                return SQLite3Utils.parseQueryResultObj(resultSet);
            } catch (Throwable exception) {
                throw new SQLite3SQLException(String.format("execute query failed, sql: %s", sql), exception);
            } finally {
                SQLite3Utils.close(resultSet);
                SQLite3Utils.close(statement);
            }
        });
    }

}
