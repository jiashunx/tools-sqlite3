package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.connection.SQLite3Connection;
import io.github.jiashunx.tools.sqlite3.connection.SQLite3ConnectionManager;
import io.github.jiashunx.tools.sqlite3.connection.SQLite3ConnectionPool;
import io.github.jiashunx.tools.sqlite3.connection.SQLite3PreparedStatement;
import io.github.jiashunx.tools.sqlite3.exception.SQLite3MappingException;
import io.github.jiashunx.tools.sqlite3.exception.SQLite3SQLException;
import io.github.jiashunx.tools.sqlite3.model.QueryResult;
import io.github.jiashunx.tools.sqlite3.model.TableColumnModel;
import io.github.jiashunx.tools.sqlite3.model.TableModel;
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



    /********************************************* ↓ 基础API ↓ *********************************************/

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

    /********************************************* ↑ 基础API ↑ *********************************************/



    /********************************************* ↓ 通用API ↓ *********************************************/

    public int insert(Object object) throws SQLite3SQLException, SQLite3MappingException {
        List<Object> objectList = new ArrayList<>(1);
        objectList.add(object);
        return batchInsert(objectList);
    }

    public int batchInsert(List<?> objList) throws SQLite3SQLException, SQLite3MappingException {
        List<?> $objList = Objects.requireNonNull(objList);
        if (!$objList.isEmpty()) {
            List<String> sqlList = new ArrayList<>();
            List<Consumer<SQLite3PreparedStatement>> consumerList = new ArrayList<>();
            $objList.forEach(object -> {
                Object $object = Objects.requireNonNull(object);
                Class<?> objClass = $object.getClass();
                TableModel tableModel = SQLite3Utils.getClassTableModel(objClass);
                sqlList.add(tableModel.getInsertSQL());
                consumerList.add(statement -> {
                    List<TableColumnModel> columnModelList = tableModel.getColumnModelList();
                    for (int index = 0, size = columnModelList.size(); index < size; index++) {
                        TableColumnModel columnModel = columnModelList.get(index);
                        int insertIndex = index + 1;
                        if (columnModel.getFieldType() == String.class) {
                            statement.setString(insertIndex, (String) columnModel.getFieldValue($object));
                        }
                        // TODO 补充其他类型
                    }
                });
            });
            batchUpdate(sqlList.toArray(new String[0]), (index, statement) -> {
                consumerList.get(index).accept(statement);
            });
        }
        return 0;
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

    public int batchInsert(String sql, int rowCount, BiConsumer<Integer, SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
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
                            "execute batch insert failed (rollback failed, reason: %s.), sql: %s"
                            , exception1.getMessage(), sql), exception);
                }
                throw new SQLite3SQLException(String.format("execute batch insert failed(rollback success), sql: %s", sql), exception);
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

    public int queryTableRowCount(String tableName) throws SQLite3SQLException {
        if (!isTableExists(tableName)) {
            return 0;
        }
        return queryForInt("SELECT COUNT(1) FROM " + tableName);
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
            throw new SQLite3SQLException("query result is null");
        }
        if (resultMap.size() > 1) {
            throw new SQLite3SQLException("query result contains more than one column");
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
            throw new SQLite3SQLException("query result contains more than one row");
        }
        return retMap;
    }

    public List<Map<String, Object>> queryForList(String sql) throws SQLite3SQLException {
        return queryForList(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public List<Map<String, Object>> queryForList(String sql, Consumer<SQLite3PreparedStatement> consumer) throws SQLite3SQLException {
        return queryForResult(sql, consumer).getResultList();
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

    /********************************************* ↑ 通用API ↑ *********************************************/


}
