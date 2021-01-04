package io.github.jiashunx.tools.sqlite3.table;

import java.util.*;

/**
 * @author jiashunx
 */
public class TableMetadata {
    /**
     * groupId.
     */
    private final String groupId;
    /**
     * key - sqlid
     */
    private final Map<String, SQLMetadata> dql = new HashMap<>();
    /**
     * key - sqlid
     */
    private final Map<String, SQLMetadata> dml = new HashMap<>();
    /**
     * key - tableName
     */
    private final Map<String, List<ColumnMetadata>> tableDDL = new HashMap<>();
    /**
     * key - viewName
     */
    private final Map<String, ViewMetadata> viewDDL = new HashMap<>();

    public TableMetadata(String groupId) {
        this.groupId = Objects.requireNonNull(groupId);
    }

    public SQLMetadata getDQL(String sqlId) {
        return dql.get(sqlId);
    }

    public SQLMetadata getDML(String sqlId) {
        return dml.get(sqlId);
    }

    public List<ColumnMetadata> getTableColumns(String tableName) {
        return tableDDL.get(tableName);
    }

    public ViewMetadata getView(String viewName) {
        return viewDDL.get(viewName);
    }

    public String getGroupId() {
        return groupId;
    }

    public void addDQL(SQLMetadata sqlMetadata) {
        dql.put(sqlMetadata.getId(), sqlMetadata);
    }

    public void addDML(SQLMetadata sqlMetadata) {
        dml.put(sqlMetadata.getId(), sqlMetadata);
    }

    public void addTableColumn(ColumnMetadata columnMetadata) {
        tableDDL.computeIfAbsent(columnMetadata.getTableName(), k -> new ArrayList<>()).add(columnMetadata);
    }

    public void addViewDDL(ViewMetadata viewMetadata) {
        viewDDL.put(viewMetadata.getViewName(), viewMetadata);
    }

}
