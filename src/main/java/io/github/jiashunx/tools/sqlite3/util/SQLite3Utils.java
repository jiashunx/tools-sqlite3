package io.github.jiashunx.tools.sqlite3.util;

import io.github.jiashunx.tools.sqlite3.model.QueryResult;
import io.github.jiashunx.tools.sqlite3.model.ResultColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jiashunx
 */
public class SQLite3Utils {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3Utils.class);

    private SQLite3Utils() {}

    public static QueryResult parseQueryResultObj(ResultSet resultSet) throws NullPointerException, SQLException {
        if (resultSet == null) {
            throw new NullPointerException();
        }
        List<Map<String, Object>> retMapList = new ArrayList<>();
        Map<String, ResultColumn> columnMap = parseResultColumnMap(resultSet);
        while (resultSet.next()) {
            Map<String, Object> rowMap = new HashMap<>();
            for (Map.Entry<String, ResultColumn> entry: columnMap.entrySet()) {
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
                        columnValue = transferDate(resultSet.getDate(columnLabel));
                        break;
                    case Types.TIME:
                        columnValue = transferTime(resultSet.getTime(columnLabel));
                        break;
                    case Types.TIMESTAMP:
                        columnValue = transferTimestamp(resultSet.getTimestamp(columnLabel));
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
        return new QueryResult(columnMap, retMapList);
    }

    public static Map<String, ResultColumn> parseResultColumnMap(ResultSet resultSet) throws NullPointerException, SQLException {
        if (resultSet == null) {
            throw new NullPointerException();
        }
        Map<String, ResultColumn> retMap = new HashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
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
        return retMap;
    }

    public static void close(Statement statement) {
        close((AutoCloseable) statement);
    }

    public static void close(ResultSet resultSet) {
        close((AutoCloseable) resultSet);
    }

    public static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception exception) {
            if (logger.isErrorEnabled()) {
                logger.error("close AutoCloseable object [{}] failed.", closeable.getClass(), exception);
            }
        }
    }

    public static java.util.Date transferDate(java.sql.Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        return new java.util.Date(sqlDate.getTime());
    }

    public static java.util.Date transferTime(java.sql.Time sqlTime) {
        if (sqlTime == null) {
            return null;
        }
        return new java.util.Date(sqlTime.getTime());
    }

    public static java.util.Date transferTimestamp(java.sql.Timestamp sqlTimestamp) {
        if (sqlTimestamp == null) {
            return null;
        }
        return new java.util.Date(sqlTimestamp.getTime());
    }

    public java.sql.Date transferToSQLDate(java.util.Date utilDate) {
        if (utilDate == null) {
            return null;
        }
        return new java.sql.Date(utilDate.getTime());
    }

    public java.sql.Time transferToSQLTime(java.util.Date utilDate) {
        if (utilDate == null) {
            return null;
        }
        return new java.sql.Time(utilDate.getTime());
    }

    public java.sql.Timestamp transferToSQLTimestamp(java.util.Date utilDate) {
        if (utilDate == null) {
            return null;
        }
        return new java.sql.Timestamp(utilDate.getTime());
    }

}
