package io.github.jiashunx.tools.sqlite3.model;

import io.github.jiashunx.tools.sqlite3.exception.SQLite3MappingException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author jiashunx
 */
public class TableModel {
    /**
     * class对象.
     */
    private Class<?> klass;
    /**
     * class名称.
     */
    private String klassName;
    /**
     * table名称.
     */
    private String tableName;
    /**
     * table主键字段名称对应的table字段模型.
     */
    private TableColumnModel idColumnModel;
    /**
     * table字段名称与对应table字段模型的映射
     */
    private Map<String, TableColumnModel> columnModelMap;
    /**
     * table字段模型列表(按照搜索顺序顺序排列, id字段放至最后).
     */
    private List<TableColumnModel> columnModelList;

    /**
     * 数据表字段定义信息.
     */
    private Map<String, ColumnMetadata> columnMetadata;

    public String getInsertSQL() {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(tableName).append("(");
        columnModelList.forEach(columnModel -> {
            builder.append(columnModel.getColumnName()).append(",");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append(") VALUES(");
        columnModelList.forEach(columnModel -> {
            builder.append("?,");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        return builder.toString();
    }

    public String getUpdateSQL() {
        StringBuilder builder = new StringBuilder("UPDATE ");
        builder.append(tableName).append(" SET ");
        columnModelList.forEach(columnModel -> {
            if (!columnModel.isIdColumn()) {
                builder.append(columnModel.getColumnName()).append("=?,");
            }
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append(" WHERE ");
        builder.append(idColumnModel.getColumnName()).append("=?");
        return builder.toString();
    }

    public String getSelectAllSQL() {
        return getSelectAllSQL(builder -> {});
    }

    public String getSelectAllSQL(Consumer<StringBuilder> consumer) {
        StringBuilder builder = new StringBuilder("SELECT * FROM " + tableName + " ");
        consumer.accept(builder);
        return builder.toString();
    }

    public String getSelectSQL() {
        return getSelectSQL(builder -> {});
    }

    public String getSelectSQL(Consumer<StringBuilder> consumer) {
        StringBuilder builder = new StringBuilder(getSelectAllSQL() + " WHERE " + idColumnModel.getColumnName() + "=? ");
        consumer.accept(builder);
        return builder.toString();
    }

    public String getDeleteSQL() {
        return getDeleteSQL(builder -> {});
    }

    public String getDeleteSQL(Consumer<StringBuilder> consumer) {
        StringBuilder builder = new StringBuilder("DELETE FROM " + tableName + " WHERE " + idColumnModel.getColumnName() + "=? ");
        consumer.accept(builder);
        return builder.toString();
    }

    public Object newInstance() {
        try {
            return klass.newInstance();
        } catch (Throwable throwable) {
            throw new SQLite3MappingException(String.format("create class [%s] instance failed.", klassName), throwable);
        }
    }

    public Object getIdFieldValue(Object object) {
        Field idField = idColumnModel.getField();
        try {
            idField.setAccessible(true);
            return idField.get(object);
        } catch (Throwable throwable) {
            throw new SQLite3MappingException(String.format("get id field[%s] value faild.", idField), throwable);
        }
    }

    public Class<?> getKlass() {
        return klass;
    }

    public void setKlass(Class<?> klass) {
        this.klass = klass;
    }

    public String getKlassName() {
        return klassName;
    }

    public void setKlassName(String klassName) {
        this.klassName = klassName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public TableColumnModel getIdColumnModel() {
        return idColumnModel;
    }

    public void setIdColumnModel(TableColumnModel idColumnModel) {
        this.idColumnModel = idColumnModel;
    }

    public Map<String, TableColumnModel> getColumnModelMap() {
        return columnModelMap;
    }

    public void setColumnModelMap(Map<String, TableColumnModel> columnModelMap) {
        this.columnModelMap = columnModelMap;
    }

    public List<TableColumnModel> getColumnModelList() {
        return columnModelList;
    }

    public void setColumnModelList(List<TableColumnModel> columnModelList) {
        this.columnModelList = columnModelList;
    }

    public Map<String, ColumnMetadata> getColumnMetadata() {
        return columnMetadata;
    }

    public void setColumnMetadata(Map<String, ColumnMetadata> columnMetadata) {
        this.columnMetadata = columnMetadata;
    }
}
