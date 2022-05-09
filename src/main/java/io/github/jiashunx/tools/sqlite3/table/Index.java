package io.github.jiashunx.tools.sqlite3.table;

import java.util.List;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class Index {

    private String tableName;
    private String indexName;
    private boolean unique;
    private List<String> columnNames;

    public Index() {}

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = Objects.requireNonNull(tableName);
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = Objects.requireNonNull(indexName);
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = Objects.requireNonNull(columnNames);
    }
}
