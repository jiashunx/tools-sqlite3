package io.github.jiashunx.tools.sqlite3.model;

import io.github.jiashunx.tools.sqlite3.exception.SQLite3MappingException;

import java.lang.reflect.Field;

/**
 * @author jiashunx
 */
public class QueryRetColumnModel {

    private String klassName;
    private String columnName;
    private String fieldName;
    private Class<?> fieldType;
    private Field field;

    public void setFieldValue(Object object, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (Throwable throwable) {
            throw new SQLite3MappingException(String.format(
                    "set class[%s] field[%s] value faile.", klassName, fieldName), throwable);
        }
    }

    public String getKlassName() {
        return klassName;
    }

    public void setKlassName(String klassName) {
        this.klassName = klassName;
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
