package io.github.jiashunx.tools.sqlite3.table;

import java.util.*;

/**
 * @author jiashunx
 */
public class SQLPackage {
    /**
     * groupId.
     */
    private final String groupId;
    /**
     * key - sqlid
     */
    private final Map<String, SQL> dql = new HashMap<>();
    /**
     * key - sqlid
     */
    private final Map<String, SQL> dml = new HashMap<>();
    /**
     * key - tableName
     */
    private final Map<String, List<Column>> tableDDL = new HashMap<>();
    /**
     * key - viewName
     */
    private final Map<String, View> viewDDL = new HashMap<>();

    public SQLPackage(String groupId) {
        this.groupId = Objects.requireNonNull(groupId);
    }

    public SQL getDQL(String sqlId) {
        return dql.get(sqlId);
    }

    public SQL getDML(String sqlId) {
        return dml.get(sqlId);
    }

    public List<Column> getColumns(String tableName) {
        return tableDDL.get(tableName);
    }

    public View getView(String viewName) {
        return viewDDL.get(viewName);
    }

    public String getGroupId() {
        return groupId;
    }

    public void addDQL(SQL sql) {
        dql.put(sql.getId(), sql);
    }

    public void addDML(SQL sql) {
        dml.put(sql.getId(), sql);
    }

    public void addColumnDDL(Column column) {
        tableDDL.computeIfAbsent(column.getTableName(), k -> new ArrayList<>()).add(column);
    }

    public void addViewDDL(View view) {
        viewDDL.put(view.getViewName(), view);
    }

}
