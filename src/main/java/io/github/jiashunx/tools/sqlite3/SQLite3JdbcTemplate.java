package io.github.jiashunx.tools.sqlite3;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class SQLite3JdbcTemplate {

    private final SQLite3ConnectionPool connectionPool;

    public SQLite3JdbcTemplate(String fileName) {
        this(SQLite3Manager.getConnectionPool(fileName));
    }

    public SQLite3JdbcTemplate(SQLite3ConnectionPool pool) {
        this.connectionPool = Objects.requireNonNull(pool);
    }

    // TODO 封装API，主要做模型与查询结果的转换.

    public SQLite3ConnectionPool getConnectionPool() {
        return connectionPool;
    }

}
