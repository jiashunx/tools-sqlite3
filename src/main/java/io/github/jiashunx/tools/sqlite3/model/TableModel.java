package io.github.jiashunx.tools.sqlite3.model;

import java.util.List;
import java.util.Map;

/**
 * @author jiashunx
 */
public class TableModel {
    /**
     * class对象.
     */
    private Class<?> klass;
    /**
     * class名称.
     */
    private String klassName;
    /**
     * table名称.
     */
    private String tableName;
    /**
     * table主键字段名称对应的table字段模型.
     */
    private TableColumnModel idColumnModel;
    /**
     * table字段名称与对应table字段模型的映射
     */
    private Map<String, TableColumnModel> columnModelMap;
    /**
     * table字段模型列表(按照搜索顺序顺序排列, id字段放至最后).
     */
    private List<TableColumnModel> columnModelList;

    /**
     * 数据表字段定义信息.
     */
    private Map<String, ColumnMetadata> columnMetadata;

    public String getInsertSQL() {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(tableName).append("(");
        columnModelList.forEach(columnModel -> {
            builder.append(columnModel.getColumnName()).append(",");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append(") VALUES(");
        columnModelList.forEach(columnModel -> {
            builder.append("?,");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        return builder.toString();
    }

    public String getUpdateSQL() {
        StringBuilder builder = new StringBuilder("UPDATE ");
        builder.append(tableName).append(" SET ");
        columnModelList.forEach(columnModel -> {
            if (!columnModel.isIdColumn()) {
                builder.append(columnModel.getColumnName()).append("=?,");
            }
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append(" WHERE ");
        builder.append(idColumnModel.getColumnName()).append("=?");
        return builder.toString();
    }

    public Class<?> getKlass() {
        return klass;
    }

    public void setKlass(Class<?> klass) {
        this.klass = klass;
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

    public TableColumnModel getIdColumnModel() {
        return idColumnModel;
    }

    public void setIdColumnModel(TableColumnModel idColumnModel) {
        this.idColumnModel = idColumnModel;
    }

    public Map<String, TableColumnModel> getColumnModelMap() {
        return columnModelMap;
    }

    public void setColumnModelMap(Map<String, TableColumnModel> columnModelMap) {
        this.columnModelMap = columnModelMap;
    }

    public List<TableColumnModel> getColumnModelList() {
        return columnModelList;
    }

    public void setColumnModelList(List<TableColumnModel> columnModelList) {
        this.columnModelList = columnModelList;
    }

    public Map<String, ColumnMetadata> getColumnMetadata() {
        return columnMetadata;
    }

    public void setColumnMetadata(Map<String, ColumnMetadata> columnMetadata) {
        this.columnMetadata = columnMetadata;
    }
}
