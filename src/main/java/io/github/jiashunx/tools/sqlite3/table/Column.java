package io.github.jiashunx.tools.sqlite3.table;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class Column {

    private String tableName;
    private String columnName;
    private String columnType;
    private boolean primary;
    private String tableDesc;
    private String columnComment;
    private int length;

    public Column() {}

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = Objects.requireNonNull(tableName);
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = Objects.requireNonNull(columnName);
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = Objects.requireNonNull(columnType);
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getTableDesc() {
        return tableDesc;
    }

    public void setTableDesc(String tableDesc) {
        this.tableDesc = tableDesc;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
