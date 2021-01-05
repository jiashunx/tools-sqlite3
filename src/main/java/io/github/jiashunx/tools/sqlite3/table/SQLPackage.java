package io.github.jiashunx.tools.sqlite3.table;

import org.sqlite.util.StringUtils;

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
    private final Map<String, List<Column>> columnDDL = new HashMap<>();
    /**
     * key - tableName_columnName
     */
    private final Map<String, Column> columnMap = new HashMap<>();
    /**
     * key - viewName
     */
    private final Map<String, View> viewDDL = new HashMap<>();

    public SQLPackage(String groupId) {
        this.groupId = Objects.requireNonNull(groupId);
    }

    public String getTableDefineSQL(String tableName) {
        List<Column> columns = getColumns(tableName);
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder("CREATE TABLE ").append(tableName).append("(");
        List<String> primaryColumns = new ArrayList<>();
        List<String> columnDefList = new ArrayList<>();
        columns.forEach(column -> {
            if (column.isPrimary()) {
                primaryColumns.add(column.getColumnName());
            }
            columnDefList.add(column.getColumnName() + " " + column.getColumnType());
        });
        builder.append(StringUtils.join(columnDefList, ","));
        if (!primaryColumns.isEmpty()) {
            builder.append(",")
                    .append(" PRIMARY KEY(")
                    .append(StringUtils.join(primaryColumns, ","))
                    .append(")");
        }
        builder.append(")");
        return builder.toString();
    }

    public String getViewDefineSQL(String viewName) {
        View view = getView(viewName);
        if (view == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder("CREATE ");
        if (view.isTemporary()) {
            builder.append(" TEMPORARY ");
        }
        builder.append(viewName).append(" AS ").append(view.getContent());
        return builder.toString();
    }

    public String getColumnDefineSQL(String tableName, String columnName) {
        Column column = getColumn(tableName, columnName);
        if (column == null) {
            return null;
        }
        return "ALTER TABLE ADD COLUMN " +
                columnName +
                " " +
                column.getColumnType();
    }

    public SQL getDQL(String sqlId) {
        return dql.get(sqlId);
    }

    public SQL getDML(String sqlId) {
        return dml.get(sqlId);
    }

    public List<Column> getColumns(String tableName) {
        return columnDDL.get(tableName);
    }

    public Column getColumn(String tableName, String columnName) {
        return columnMap.get(tableName + "_" + columnName);
    }

    public View getView(String viewName) {
        return viewDDL.get(viewName);
    }

    public String getGroupId() {
        return groupId;
    }

    public synchronized void addDQL(SQL sql) {
        dql.put(sql.getId(), sql);
    }

    public synchronized void addDML(SQL sql) {
        dml.put(sql.getId(), sql);
    }

    public synchronized void addColumnDDL(Column column) {
        columnDDL.computeIfAbsent(column.getTableName(), k -> new ArrayList<>()).add(column);
        columnMap.put(column.getTableName() + "_" + column.getColumnName(), column);
    }

    public synchronized void addViewDDL(View view) {
        viewDDL.put(view.getViewName(), view);
    }

}
