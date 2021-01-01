package io.github.jiashunx.tools.sqlite3.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class QueryResult {

    private final Map<String, TableColumnMetadata> columnMap;
    private final List<Map<String, Object>> resultList;

    public QueryResult(Map<String, TableColumnMetadata> columnMap, List<Map<String, Object>> resultList) {
        this.columnMap = Objects.requireNonNull(columnMap);
        this.resultList = resultList;
    }

    public Map<String, TableColumnMetadata> getColumnMap() {
        return columnMap;
    }

    public List<Map<String, Object>> getResultList() {
        return resultList;
    }
}
