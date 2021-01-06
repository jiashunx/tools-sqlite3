package io.github.jiashunx.tools.sqlite3.table;

import java.util.List;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class Index {

    private final String tableName;
    private final String indexName;
    private final boolean unique;
    private final List<String> columnNames;

    public Index(String tableName, String indexName, boolean unique, List<String> columnNames) {
        this.tableName = Objects.requireNonNull(tableName);
        this.indexName = Objects.requireNonNull(indexName);
        this.unique = unique;
        this.columnNames = Objects.requireNonNull(columnNames);
    }

    public String getTableName() {
        return tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public boolean isUnique() {
        return unique;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }
}
