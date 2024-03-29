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
    private String groupId;
    /**
     * groupName.
     */
    private String groupName;
    /**
     * key - sqlid
     */
    private final Map<String, SQL> dql = new LinkedHashMap<>();
    /**
     * key - sqlid
     */
    private final Map<String, SQL> dml = new LinkedHashMap<>();
    /**
     * key - tableName
     */
    private final Map<String, List<Column>> columnDDL = new LinkedHashMap<>();
    /**
     * key - tableName_columnName
     */
    private final Map<String, Column> columnMap = new LinkedHashMap<>();
    /**
     * key - tableName
     */
    private final Map<String, List<Index>> indexDDL = new LinkedHashMap<>();
    /**
     * key - tableName_columnName
     */
    private final Map<String, Index> indexMap = new LinkedHashMap<>();
    /**
     * key - viewName
     */
    private final Map<String, View> viewDDL = new LinkedHashMap<>();
    /**
     * key - triggerName
     */
    private final Map<String, Trigger> triggerDDL = new LinkedHashMap<>();

    public SQLPackage() {}

    public List<String> getTableNames() {
        return new ArrayList<>(columnDDL.keySet());
    }

    public List<String> getIndexTableNames() {
        return new ArrayList<>(indexDDL.keySet());
    }

    public List<String> getViewNames() {
        return new ArrayList<>(viewDDL.keySet());
    }

    public List<String> getTriggerNames() {
        return new ArrayList<>(triggerDDL.keySet());
    }

    public String getTableDefineSQL(String tableName) {
        List<Column> columns = getColumns(tableName);
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder("CREATE TABLE ").append(tableName).append("(");
        List<String> primaryColumns = new ArrayList<>();
        List<String> columnDefList = new ArrayList<>();
        List<String> foreignKeyDefList = new ArrayList<>();
        columns.forEach(column -> {
            if (column.isPrimary()) {
                primaryColumns.add(column.getColumnName());
            }
            String lengthStr = "";
            if (column.getLength() != Integer.MIN_VALUE){
                lengthStr = "(" + column.getLength() + ")";
            }
            String notNull = "";
            if (column.isNotNull()) {
                notNull = " NOT NULL ";
            }
            String defaultValue = "";
            if (!column.getDefaultValue().isEmpty()) {
                defaultValue = " DEFAULT " + column.getDefaultValue();
            }
            columnDefList.add(column.getColumnName() + " " + column.getColumnType() + lengthStr + notNull + defaultValue);
            if (column.getForeignTable() != null && column.getForeignColumn() != null
                    && !column.getForeignTable().trim().isEmpty() && !column.getForeignColumn().trim().isEmpty()) {
                foreignKeyDefList.add("FOREIGN KEY (" + column.getColumnName() + ") REFERENCES " + column.getForeignTable().trim() + "(" + column.getForeignColumn().trim() + ")");
            }
        });
        builder.append(StringUtils.join(columnDefList, ","));
        if (!primaryColumns.isEmpty()) {
            builder.append(",")
                    .append(" PRIMARY KEY(")
                    .append(StringUtils.join(primaryColumns, ","))
                    .append(")");
        }
        if (!foreignKeyDefList.isEmpty()) {
            builder.append(",")
                    .append(StringUtils.join(foreignKeyDefList, ","));
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
        builder.append(" VIEW ");
        builder.append(viewName).append(" AS ").append(view.getContent());
        return builder.toString();
    }

    public String getColumnDefineSQL(String tableName, String columnName) {
        Column column = getColumn(tableName, columnName);
        if (column == null) {
            return null;
        }
        String lengthStr = "";
        if (column.getLength() != Integer.MIN_VALUE){
            lengthStr = "(" + column.getLength() + ")";
        }
        String notNull = "";
        if (column.isNotNull()) {
            notNull = " NOT NULL ";
        }
        String defaultValue = "";
        if (!column.getDefaultValue().isEmpty()) {
            defaultValue = " DEFAULT " + column.getDefaultValue();
        }
        return "ALTER TABLE " + tableName + " ADD COLUMN " +
                columnName +
                " " +
                column.getColumnType() + lengthStr + notNull + defaultValue;
    }

    public String getIndexDefineSQL(String tableName, String indexName) {
        Index index = getIndex(tableName, indexName);
        if (index == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder("CREATE ");
        if (index.isUnique()) {
            builder.append(" UNIQUE ");
        }
        builder.append(" INDEX ")
                .append(indexName).append(" ON ").append(tableName)
                .append("(")
                .append(StringUtils.join(index.getColumnNames(), ","))
                .append(")");
        return builder.toString();
    }

    public String getTriggerDefineSQL(String triggerName) {
        Trigger trigger = getTrigger(triggerName);
        if (trigger == null) {
            return null;
        }
        return trigger.getTriggerSQL();
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

    public List<Index> getIndexes(String tableName) {
        return indexDDL.get(tableName);
    }

    public Index getIndex(String tableName, String indexName) {
        return indexMap.get(tableName + "_" + indexName);
    }

    public View getView(String viewName) {
        return viewDDL.get(viewName);
    }

    public Trigger getTrigger(String triggerName) {
        return triggerDDL.get(triggerName);
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

    public synchronized void addIndexDDL(Index index) {
        indexDDL.computeIfAbsent(index.getTableName(), k -> new ArrayList<>()).add(index);
        indexMap.put(index.getTableName() + "_" + index.getIndexName(), index);
    }

    public synchronized void addViewDDL(View view) {
        viewDDL.put(view.getViewName(), view);
    }

    public synchronized void addTriggerDDL(Trigger trigger) {
        triggerDDL.put(trigger.getTriggerName(), trigger);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = Objects.requireNonNull(groupId);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
