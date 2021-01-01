package io.github.jiashunx.tools.sqlite3.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class QueryResult {

    private final Map<String, ColumnMetadata> columnMetadataMap;
    private final List<Map<String, Object>> retMapList;

    public QueryResult(Map<String, ColumnMetadata> columnMetadataMap, List<Map<String, Object>> retMapList) {
        this.columnMetadataMap = Objects.requireNonNull(columnMetadataMap);
        this.retMapList = retMapList;
    }

    public Map<String, ColumnMetadata> getColumnMetadataMap() {
        return columnMetadataMap;
    }

    public List<Map<String, Object>> getRetMapList() {
        return retMapList;
    }
}
