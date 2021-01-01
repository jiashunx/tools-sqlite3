package io.github.jiashunx.tools.sqlite3.model;

import java.util.Map;

/**
 * @author jiashunx
 */
public class TableModel {
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
}
