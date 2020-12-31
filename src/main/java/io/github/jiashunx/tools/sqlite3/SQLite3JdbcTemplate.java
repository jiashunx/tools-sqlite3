package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.connection.SQLite3Connection;
import io.github.jiashunx.tools.sqlite3.exception.DataAccessException;
import io.github.jiashunx.tools.sqlite3.model.QueryResult;
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

    private static final Consumer<PreparedStatement> PREPARED_STATEMENT_CONSUMER = statement -> {};

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

    public int[] batchUpdate(String[] sqlArr) throws DataAccessException {
        return batchUpdate(sqlArr, (index, statement) -> {});
    }

    public int[] batchUpdate(String[] sqlArr, BiConsumer<Integer, PreparedStatement> consumer) throws DataAccessException {
        return write(connection -> {
            int[] effectedRowArr = new int[sqlArr.length];
            PreparedStatement statement = null;
            try {
                connection.setAutoCommit(false);
                for (int index = 0; index < sqlArr.length; index++) {
                    String sql = sqlArr[index];
                    try {
                        statement = connection.prepareStatement(sql);
                        if (consumer != null) {
                            consumer.accept(index, statement);
                        }
                        effectedRowArr[index] = statement.executeUpdate();
                    } catch (Throwable exception) {
                        throw new DataAccessException(String.format("execute batch update failed, single sql: %s", sql), exception);
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
                    throw new DataAccessException(String.format(
                            "execute batch update failed (rollback failed, reason: %s.)"
                            , exception1.getMessage()), exception);
                }
                throw new DataAccessException("execute batch update failed(rollback success)", exception);
            } finally {
                SQLite3Utils.close(statement);
            }
        });
    }

    public int batchInsert(String sql, int rowCount, BiConsumer<Integer, PreparedStatement> consumer) throws DataAccessException {
        return write(connection -> {
            int effectedRowCount = 0;
            PreparedStatement statement = null;
            try {
                connection.setAutoCommit(false);
                statement = connection.prepareStatement(sql);
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
                    throw new DataAccessException(String.format(
                            "execute batch insert failed (rollback failed, reason: %s.), sql: %s"
                            , exception1.getMessage(), sql), exception);
                }
                throw new DataAccessException(String.format("execute batch insert failed(rollback success), sql: %s", sql), exception);
            } finally {
                SQLite3Utils.close(statement);
            }
        });
    }

    public int executeUpdate(String sql) throws DataAccessException {
        return executeUpdate(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public int executeUpdate(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return write(connection -> {
            PreparedStatement statement = null;
            try {
                connection.setAutoCommit(false);
                statement = connection.prepareStatement(sql);
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
                    throw new DataAccessException(String.format(
                            "execute update failed (rollback failed, reason: %s.), sql: %s"
                            , exception1.getMessage(), sql), exception);
                }
                throw new DataAccessException(String.format("execute update failed(rollback success), sql: %s", sql), exception);
            } finally {
                SQLite3Utils.close(statement);
            }
        });
    }

    public boolean isTableExists(String tableName) throws DataAccessException {
        try {
            return queryForInt(String.format("SELECT COUNT(1) FROM %s LIMIT 1", tableName)) >= 0;
        } catch (DataAccessException exception) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("query table [%s] existence failed.", tableName), exception);
            }
        }
        return false;
    }

    public boolean queryForBoolean(String sql) throws DataAccessException {
        return queryForBoolean(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public boolean queryForBoolean(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return Boolean.parseBoolean(queryForString(sql, consumer));
    }

    public byte queryForByte(String sql) throws DataAccessException {
        return queryForByte(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public byte queryForByte(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return Byte.parseByte(queryForString(sql, consumer));
    }

    public short queryForShort(String sql) throws DataAccessException {
        return queryForShort(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public short queryForShort(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return Short.parseShort(queryForString(sql, consumer));
    }

    public int queryForInt(String sql) throws DataAccessException {
        return queryForInt(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public int queryForInt(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return Integer.parseInt(queryForString(sql, consumer));
    }

    public float queryForFloat(String sql) throws DataAccessException {
        return queryForFloat(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public float queryForFloat(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return Float.parseFloat(queryForString(sql, consumer));
    }

    public double queryForDouble(String sql) throws DataAccessException {
        return queryForDouble(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public double queryForDouble(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return Double.parseDouble(queryForString(sql, consumer));
    }

    public String queryForString(String sql) throws DataAccessException {
        return queryForString(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public String queryForString(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return queryForOneValue(sql, consumer).toString();
    }

    public Object queryForOneValue(String sql) throws DataAccessException {
        return queryForOneValue(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public Object queryForOneValue(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        Map<String, Object> resultMap = queryForMap(sql, consumer);
        if (resultMap == null || resultMap.isEmpty()) {
            throw new DataAccessException("query result is null");
        }
        if (resultMap.size() > 1) {
            throw new DataAccessException("query result contains more than one column");
        }
        return resultMap.get(resultMap.keySet().toArray(new String[0])[0]);
    }

    public Map<String, Object> queryForMap(String sql) throws DataAccessException {
        return queryForMap(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public Map<String, Object> queryForMap(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        List<Map<String, Object>> mapList = queryForList(sql, consumer);
        Map<String, Object> retMap = null;
        if (mapList != null && !mapList.isEmpty()) {
            if (mapList.size() == 1) {
                return mapList.get(0);
            }
            throw new DataAccessException("query result contains more than one row");
        }
        return retMap;
    }

    public List<Map<String, Object>> queryForList(String sql) throws DataAccessException {
        return queryForList(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public List<Map<String, Object>> queryForList(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return queryForResult(sql, consumer).getResultList();
    }

    public QueryResult queryForResult(String sql) throws DataAccessException {
        return queryForResult(sql, PREPARED_STATEMENT_CONSUMER);
    }

    public QueryResult queryForResult(String sql, Consumer<PreparedStatement> consumer) throws DataAccessException {
        return query(connection -> {
            List<Map<String, Object>> retMapList = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.prepareStatement(sql);
                if (consumer != null) {
                    consumer.accept(statement);
                }
                resultSet = statement.executeQuery();
                return SQLite3Utils.parseQueryResultObj(resultSet);
            } catch (Throwable exception) {
                throw new DataAccessException(String.format("execute query failed, sql: %s", sql), exception);
            } finally {
                SQLite3Utils.close(resultSet);
                SQLite3Utils.close(statement);
            }
        });
    }

    /********************************************* ↑ 通用API ↑ *********************************************/


}
