package io.github.jiashunx.tools.sqlite3.model;

import io.github.jiashunx.tools.sqlite3.exception.SQLite3MappingException;

import java.lang.reflect.Field;

/**
 * @author jiashunx
 */
public class TableColumnModel {

    private String klassName;
    private String tableName;
    private boolean idColumn;
    private String columnName;
    private String fieldName;
    private Class<?> fieldType;
    private Field field;

    public Object getFieldValue(Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (Throwable throwable) {
            throw new SQLite3MappingException(String.format(
                    "get field %s[class: %s, table: %s] value failed.", fieldName, klassName, tableName));
        }
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

    public boolean isIdColumn() {
        return idColumn;
    }

    public void setIdColumn(boolean idColumn) {
        this.idColumn = idColumn;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
