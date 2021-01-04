package io.github.jiashunx.tools.sqlite3.table;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class ColumnMetadata {

    private final String tableName;
    private final String columnName;
    private final String columnType;
    private final boolean primary;

    public ColumnMetadata(String tableName, String columnName, String columnType, boolean primary) {
        this.tableName = Objects.requireNonNull(tableName);
        this.columnName = Objects.requireNonNull(columnName);
        this.columnType = Objects.requireNonNull(columnType);
        this.primary = primary;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public boolean isPrimary() {
        return primary;
    }
}
