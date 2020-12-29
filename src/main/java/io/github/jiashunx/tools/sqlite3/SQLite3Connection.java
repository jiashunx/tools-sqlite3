package io.github.jiashunx.tools.sqlite3;

import java.sql.Connection;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class SQLite3Connection {

    private volatile SQLite3ConnectionPool pool;
    private final Connection connection;

    public SQLite3Connection(Connection connection) {
        this.connection = Objects.requireNonNull(connection);
        // TODO 使用pool的读写锁实现并发读，串行写
    }

    public synchronized SQLite3ConnectionPool getPool() {
        return pool;
    }

    public synchronized void setPool(SQLite3ConnectionPool pool) {
        this.pool = Objects.requireNonNull(pool);
    }
}
