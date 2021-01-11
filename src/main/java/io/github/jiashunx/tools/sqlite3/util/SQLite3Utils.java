package io.github.jiashunx.tools.sqlite3.util;

import io.github.jiashunx.tools.sqlite3.connection.SQLite3PreparedStatement;
import io.github.jiashunx.tools.sqlite3.exception.SQLite3MappingException;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Column;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Id;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Table;
import io.github.jiashunx.tools.sqlite3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author jiashunx
 */
public class SQLite3Utils {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3Utils.class);

    private static final Map<String, QueryRetClassModel> QUERY_RET_CLASS_MAP = new HashMap<>();

    private static final Map<String, TableModel> CLASS_TABLE_MAP = new HashMap<>();

    private SQLite3Utils() {}

    public static <R> List<R> parseQueryResult(QueryResult queryResult, Class<R> klass) throws NullPointerException, SQLite3MappingException {
        if (queryResult == null || klass == null) {
            throw new NullPointerException();
        }
        String klassName = klass.getName();
        QueryRetClassModel retClassModel = getClassQueryRetModel(klass);
        Map<String, QueryRetColumnModel> retColumnModelMap = retClassModel.getRetColumnModelMap();
        List<Map<String, Object>> retMapList = queryResult.getRetMapList();
        Map<String, ColumnMetadata> columnMetadataMap = queryResult.getColumnMetadataMap();
        List<R> retObjList = null;
        if (retMapList != null) {
            AtomicReference<List<R>> retObjListRef = new AtomicReference<>(new ArrayList<>(retMapList.size()));
            retMapList.forEach(rowMap -> {
                R instance = null;
                try {
                    instance = klass.newInstance();
                } catch (Throwable throwable) {
                    throw new SQLite3MappingException(String.format("create class[%s] instance failed.", klassName), throwable);
                }
                AtomicReference<R> instanceRef = new AtomicReference<>(instance);
                rowMap.forEach((columnName, columnValue) -> {
                    QueryRetColumnModel retColumnModel = retColumnModelMap.get(columnName);
                    if (retColumnModel != null) {
                        ColumnMetadata columnMetadata = columnMetadataMap.get(columnName);
                        Class<?> fieldType = retColumnModel.getFieldType();
                        if (fieldType == String.class) {
                            retColumnModel.setFieldValue(instanceRef.get(), (String) columnValue);
                        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                            retColumnModel.setFieldValue(instanceRef.get(), (Boolean) columnValue);
                        }
                        if (fieldType == java.util.Date.class) {
                            int columnTypeOfMetadata = columnMetadata.getColumnType();
                            switch (columnTypeOfMetadata) {
                                case Types.DATE:
                                    retColumnModel.setFieldValue(instanceRef.get(), transferDate((java.sql.Date) columnValue));
                                    break;
                                case Types.TIME:
                                    retColumnModel.setFieldValue(instanceRef.get(), transferTime((java.sql.Time) columnValue));
                                    break;
                                case Types.TIMESTAMP:
                                    retColumnModel.setFieldValue(instanceRef.get(), transferTimestamp((java.sql.Timestamp) columnValue));
                                    break;
                                default:
                                    retColumnModel.setFieldValue(instanceRef.get(), columnValue);
                                    break;
                            }
                        } else if (fieldType == BigDecimal.class) {
                            retColumnModel.setFieldValue(instanceRef.get(), BigDecimal.valueOf(Double.parseDouble(String.valueOf(columnValue))));
                        } else {
                            retColumnModel.setFieldValue(instanceRef.get(), columnValue);
                        }
                    }
                });
                retObjListRef.get().add(instanceRef.get());
            });
            retObjList = retObjListRef.get();
        }
        return retObjList;
    }

    public static QueryRetClassModel getClassQueryRetModel(Class<?> klass) throws NullPointerException, SQLite3MappingException {
        if (klass == null) {
            throw new NullPointerException();
        }
        String klassName = klass.getName();
        QueryRetClassModel retClassModel = QUERY_RET_CLASS_MAP.get(klassName);
        if (retClassModel == null) {
            synchronized (SQLite3Utils.class) {
                retClassModel = QUERY_RET_CLASS_MAP.get(klassName);
                if (retClassModel == null) {
                    try {
                        Field[] fields = klass.getDeclaredFields();
                        if (fields.length == 0) {
                            throw new SQLite3MappingException(String.format("class[%s] has no declared fields", klassName));
                        }
                        Map<String, QueryRetColumnModel> retColumnModelMap = new HashMap<>();
                        for (Field field: fields) {
                            String fieldName = field.getName();
                            SQLite3Column columnAnnotation = field.getAnnotation(SQLite3Column.class);
                            if (columnAnnotation != null) {
                                String columnName = columnAnnotation.columnName().trim();
                                if (columnName.isEmpty()) {
                                    throw new SQLite3MappingException(String.format(
                                            "class[%s] field [%s] has @SQLite3Column annotation, but columnName is empty"
                                            , klassName, fieldName));
                                }
                                QueryRetColumnModel retColumnModel = new QueryRetColumnModel();
                                retColumnModel.setKlassName(klassName);
                                retColumnModel.setColumnName(columnName);
                                retColumnModel.setField(field);
                                retColumnModel.setFieldName(fieldName);
                                retColumnModel.setFieldType(field.getType());
                                retColumnModelMap.put(columnName, retColumnModel);
                            }
                        }
                        if (retColumnModelMap.isEmpty()) {
                            throw new SQLite3MappingException(String.format("class[%s] has no field with annotation: @SQLite3Column", klassName));
                        }
                        retClassModel = new QueryRetClassModel();
                        retClassModel.setKlass(klass);
                        retClassModel.setRetColumnModelMap(retColumnModelMap);
                        QUERY_RET_CLASS_MAP.put(klassName, retClassModel);
                    } catch (SecurityException exception) {
                        throw new SQLite3MappingException(String.format("visit class[%s] fields failed.", klassName), exception);
                    } catch (Throwable throwable) {
                        if (throwable instanceof SQLite3MappingException) {
                            throw (SQLite3MappingException) throwable;
                        }
                        throw new SQLite3MappingException(throwable);
                    }
                }
            }
        }
        return retClassModel;
    }

    public static TableModel getClassTableModel(Class<?> klass) throws NullPointerException, SQLite3MappingException {
        if (klass == null) {
            throw new NullPointerException();
        }
        String klassName = klass.getName();
        TableModel tableModel = CLASS_TABLE_MAP.get(klassName);
        if (tableModel == null) {
            synchronized (SQLite3Utils.class) {
                tableModel = CLASS_TABLE_MAP.get(klassName);
                if (tableModel == null) {
                    try {
                        SQLite3Table tableAnnotation = klass.getAnnotation(SQLite3Table.class);
                        if (tableAnnotation == null) {
                            throw new SQLite3MappingException(String.format("class[%s] doesn't have @SQLite3Table annotation", klassName));
                        }
                        String tableName = tableAnnotation.tableName().trim();
                        if (tableName.isEmpty()) {
                            throw new SQLite3MappingException(String.format("class[%s] has @SQLite3Table annotation, but tableName is empty", klassName));
                        }
                        Field[] fields = klass.getDeclaredFields();
                        if (fields.length == 0) {
                            throw new SQLite3MappingException(String.format("class[%s] has no declared fields", klassName));
                        }
                        TableColumnModel idColumnModel = null;
                        Map<String, TableColumnModel> columnModelMap = new HashMap<>();
                        for (Field field: fields) {
                            String fieldName = field.getName();
                            SQLite3Column columnAnnotation = field.getAnnotation(SQLite3Column.class);
                            SQLite3Id idAnnotation = field.getAnnotation(SQLite3Id.class);
                            if (idAnnotation != null && columnAnnotation == null) {
                                throw new SQLite3MappingException(String.format(
                                        "class[%s] field [%s] has @SQLite3Id annotation, but has no @SQLite3Column annotation"
                                        , klassName, fieldName));
                            }
                            if (columnAnnotation != null) {
                                String columnName = columnAnnotation.columnName().trim();
                                if (columnName.isEmpty()) {
                                    throw new SQLite3MappingException(String.format(
                                            "class[%s] field [%s] has @SQLite3Column annotation, but columnName is empty"
                                            , klassName, fieldName));
                                }
                                TableColumnModel columnModel = new TableColumnModel();
                                columnModel.setKlassName(klassName);
                                columnModel.setTableName(tableName);
                                columnModel.setColumnName(columnName);
                                columnModel.setField(field);
                                columnModel.setFieldName(fieldName);
                                columnModel.setFieldType(field.getType());
                                columnModel.setIdColumn(false);
                                if (columnModelMap.containsKey(columnName)) {
                                    throw new SQLite3MappingException(String.format(
                                            "class[%s] has more than one field mapping to table column: %s"
                                            , klassName, columnName));
                                }
                                if (idAnnotation != null) {
                                    if (idColumnModel != null) {
                                        throw new SQLite3MappingException(String.format(
                                                "class[%s] has more than one field with @SQLite3Id annotation, such as %s, %s"
                                                , klassName, idColumnModel.getFieldName(), fieldName));
                                    }
                                    columnModel.setIdColumn(true);
                                    idColumnModel = columnModel;
                                }
                                columnModelMap.put(columnName, columnModel);
                            }
                        }
                        if (idColumnModel == null) {
                            throw new SQLite3MappingException(String.format("class[%s] has no field with annotation: @SQLite3Id", klassName));
                        }
                        List<TableColumnModel> columnModelList = new ArrayList<>(columnModelMap.size());
                        columnModelMap.values().forEach(columnModel -> {
                            if (columnModel.isIdColumn()) {
                                return;
                            }
                            columnModelList.add(columnModel);
                        });
                        columnModelList.add(idColumnModel);
                        tableModel = new TableModel();
                        tableModel.setKlass(klass);
                        tableModel.setKlassName(klassName);
                        tableModel.setTableName(tableName);
                        tableModel.setIdColumnModel(idColumnModel);
                        tableModel.setColumnModelMap(columnModelMap);
                        tableModel.setColumnModelList(columnModelList);
                        CLASS_TABLE_MAP.put(klassName, tableModel);
                    } catch (SecurityException exception) {
                        throw new SQLite3MappingException(String.format("visit class[%s] fields failed.", klassName), exception);
                    } catch (Throwable throwable) {
                        if (throwable instanceof SQLite3MappingException) {
                            throw (SQLite3MappingException) throwable;
                        }
                        throw new SQLite3MappingException(throwable);
                    }
                }
            }
        }
        return tableModel;

    }

    public static Consumer<SQLite3PreparedStatement> buildTableConsumer(Object object, TableModel tableModel) {
        if (object == null || tableModel == null) {
            throw new NullPointerException();
        }
        return statement -> {
            String tableName = tableModel.getTableName();
            Map<String, ColumnMetadata> columnMetadataMap = tableModel.getColumnMetadata();
            List<TableColumnModel> columnModelList = tableModel.getColumnModelList();
            for (int index = 0, size = columnModelList.size(); index < size; index++) {
                TableColumnModel columnModel = columnModelList.get(index);
                String columnName = columnModel.getColumnName();
                ColumnMetadata columnMetadata = columnMetadataMap.get(columnName);
                if (columnMetadata == null) {
                    throw new SQLite3MappingException(String.format("table[%s] has no field: %s", tableName, columnName));
                }
                int insertIndex = index + 1;
                Object value = columnModel.getFieldValue(object);
                String stringValue = String.valueOf(value);
                Class<?> fieldType = columnModel.getFieldType();
                if (fieldType == String.class || fieldType == char.class || fieldType == Character.class) {
                    statement.setString(insertIndex, (String) value);
                } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                    statement.setBoolean(insertIndex, Boolean.parseBoolean(stringValue));
                } else if (fieldType == byte.class || fieldType == Byte.class) {
                    statement.setByte(insertIndex, Byte.parseByte(stringValue));
                } else if (fieldType == short.class || fieldType == Short.class) {
                    statement.setShort(insertIndex, Short.parseShort(stringValue));
                } else if (fieldType == int.class || fieldType == Integer.class) {
                    statement.setInt(insertIndex, Integer.parseInt(stringValue));
                } else if (fieldType == float.class || fieldType == Float.class) {
                    statement.setFloat(insertIndex, Float.parseFloat(stringValue));
                } else if (fieldType == double.class || fieldType == Double.class) {
                    statement.setDouble(insertIndex, Double.parseDouble(stringValue));
                } else if (fieldType == long.class || fieldType == Long.class) {
                    statement.setLong(insertIndex, Long.parseLong(stringValue));
                } else if (fieldType == BigDecimal.class) {
                    statement.setBigDecimal(insertIndex, (BigDecimal) value);
                } else if (fieldType == byte[].class) {
                    statement.setBytes(insertIndex, (byte[]) value);
                } else if (InputStream.class.isAssignableFrom(fieldType)) {
                    statement.setBlob(insertIndex, (InputStream) value);
                } else if (fieldType == Blob.class) {
                    statement.setBlob(insertIndex, (Blob) value);
                } else if (Reader.class.isAssignableFrom(fieldType)) {
                    statement.setClob(insertIndex, (Reader) value);
                } else if (fieldType == Clob.class) {
                    statement.setClob(insertIndex, (Clob) value);
                } else if (fieldType == java.util.Date.class) {
                    switch (columnMetadata.getColumnTypeName()) {
                        case "DATE":
                            statement.setDate(insertIndex, transferToSQLDate((java.util.Date) value));
                            break;
                        case "TIME":
                            statement.setTime(insertIndex, transferToSQLTime((java.util.Date) value));
                            break;
                        case "TIMESTAMP":
                            statement.setTimestamp(insertIndex, transferToSQLTimestamp((java.util.Date) value));
                            break;
                        default:
                            statement.setObject(insertIndex, value);
                            break;
                    }
                } else if (fieldType == java.sql.Date.class) {
                    statement.setDate(insertIndex, (java.sql.Date) value);
                } else if (fieldType == java.sql.Time.class) {
                    statement.setTime(insertIndex, (java.sql.Time) value);
                } else if (fieldType == java.sql.Timestamp.class) {
                    statement.setTimestamp(insertIndex, (java.sql.Timestamp) value);
                } else {
                    statement.setObject(insertIndex, value);
                }
            }
        };
    }

    public static QueryResult parseQueryResultObj(ResultSet resultSet) throws NullPointerException, SQLException {
        if (resultSet == null) {
            throw new NullPointerException();
        }
        List<Map<String, Object>> retMapList = new ArrayList<>();
        Map<String, ColumnMetadata> columnMap = parseTableColumnMetadata(resultSet);
        while (resultSet.next()) {
            Map<String, Object> rowMap = new HashMap<>();
            for (Map.Entry<String, ColumnMetadata> entry: columnMap.entrySet()) {
                String columnName = entry.getKey();
                ColumnMetadata columnMetadata = entry.getValue();
                String columnLabel = columnMetadata.getColumnLabel();
                Object columnValue = null;
                switch (columnMetadata.getColumnTypeName()) {
                    case "BOOLEAN":
                        columnValue = resultSet.getBoolean(columnLabel);
                        break;
                    case "BIT":
                        columnValue = resultSet.getBoolean(columnLabel);
                        break;
                    case "TINYINT":
                        columnValue = resultSet.getByte(columnLabel);
                        break;
                    case "SMALLINT":
                        columnValue = resultSet.getShort(columnLabel);
                        break;
                    case "INTEGER":
                        columnValue = resultSet.getInt(columnLabel);
                        break;
                    case "BIGINT":
                        columnValue = resultSet.getLong(columnLabel);
                        break;
                    case "FLOAT":
                        columnValue = resultSet.getFloat(columnLabel);
                        break;
                    case "REAL":
                    case "DOUBLE":
                        columnValue = resultSet.getDouble(columnLabel);
                        break;
                    case "NUMERIC":
                    case "DECIMAL":
                        columnValue = resultSet.getBigDecimal(columnLabel);
                        break;
                    case "CHAR":
                    case "VARCHAR":
                    case "LONGVARCHAR":
                    case "NCHAR":
                    case "NVARCHAR":
                    case "LONGNVARCHAR":
                        columnValue = resultSet.getString(columnLabel);
                        break;
                    case "DATE":
                        columnValue = resultSet.getDate(columnLabel);
                        break;
                    case "TIME":
                        columnValue = resultSet.getTime(columnLabel);
                        break;
                    case "TIMESTAMP":
                        columnValue = resultSet.getTimestamp(columnLabel);
                        break;
                    case "BINARY":
                    case "VARBINARY":
                    case "LONGVARBINARY":
                        columnValue = resultSet.getBytes(columnLabel);
                        break;
                    case "CLOB":
                    case "NCLOB":
                        columnValue = resultSet.getObject(columnLabel);
                        break;
                    case "BLOB":
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try (InputStream inputStream = resultSet.getBinaryStream(columnLabel);) {
                            if (inputStream != null) {
                                byte[] buffer = new byte[1024];
                                int temp = 0;
                                while ((temp = inputStream.read(buffer)) >= 0) {
                                    bos.write(buffer, 0, temp);
                                }
                            }
                        } catch (Throwable throwable) {
                            throw new SQLite3MappingException(String.format("read blob column[%s] failed.", columnName), throwable);
                        }
                        columnValue = bos.toByteArray();
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

    public static Map<String, ColumnMetadata> parseTableColumnMetadata(ResultSet resultSet) throws NullPointerException, SQLException {
        if (resultSet == null) {
            throw new NullPointerException();
        }
        Map<String, ColumnMetadata> retMap = new HashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int index = 1; index <= columnCount; index++) {
            ColumnMetadata columnMetadata = new ColumnMetadata();
            String columnName = metaData.getColumnName(index);
            columnMetadata.setColumnName(columnName);
            columnMetadata.setColumnLabel(metaData.getColumnLabel(index));
            columnMetadata.setColumnType(metaData.getColumnType(index));
            columnMetadata.setColumnTypeName(metaData.getColumnTypeName(index));
            columnMetadata.setColumnClassName(metaData.getColumnClassName(index));
            columnMetadata.setColumnDisplaySize(metaData.getColumnDisplaySize(index));
            columnMetadata.setCatalogName(metaData.getCatalogName(index));
            columnMetadata.setPrecision(metaData.getPrecision(index));
            columnMetadata.setScale(metaData.getScale(index));
            columnMetadata.setSchemaName(metaData.getSchemaName(index));
            columnMetadata.setTableName(metaData.getTableName(index));
            columnMetadata.setAutoIncrement(metaData.isAutoIncrement(index));
            columnMetadata.setCaseSensitive(metaData.isCaseSensitive(index));
            columnMetadata.setCurrency(metaData.isCurrency(index));
            columnMetadata.setDefinitelyWritable(metaData.isDefinitelyWritable(index));
            columnMetadata.setNullable(metaData.isNullable(index));
            columnMetadata.setReadOnly(metaData.isReadOnly(index));
            columnMetadata.setWritable(metaData.isWritable(index));
            columnMetadata.setSearchable(metaData.isSearchable(index));
            columnMetadata.setSigned(metaData.isSigned(index));
            retMap.put(columnName, columnMetadata);
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

    public static java.sql.Date transferToSQLDate(java.util.Date utilDate) {
        if (utilDate == null) {
            return null;
        }
        return new java.sql.Date(utilDate.getTime());
    }

    public static java.sql.Time transferToSQLTime(java.util.Date utilDate) {
        if (utilDate == null) {
            return null;
        }
        return new java.sql.Time(utilDate.getTime());
    }

    public static java.sql.Timestamp transferToSQLTimestamp(java.util.Date utilDate) {
        if (utilDate == null) {
            return null;
        }
        return new java.sql.Timestamp(utilDate.getTime());
    }

}
