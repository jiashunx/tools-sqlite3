package io.github.jiashunx.tools.sqlite3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author jiashunx
 */
public class SQLite3Connection {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3Connection.class);

    private static final byte[] DEFAULT_RETURN_VALUE = new byte[0];

    private volatile SQLite3ConnectionPool pool;
    private final Connection connection;
    private volatile boolean closed;
    private String name;

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

    public synchronized void release() {
        getPool().release(this);
    }

    public synchronized void read(Consumer<Connection> consumer) {
        read(c -> {
           consumer.accept(c);
           return DEFAULT_RETURN_VALUE;
        });
    }

    public synchronized <R> R read(Function<Connection, R> function) {
        getPool().getActionReadLock().lock();
        try {
            return function.apply(this.connection);
        } finally {
            getPool().getActionReadLock().unlock();;
        }
    }

    public synchronized void write(Consumer<Connection> consumer) {
        write(c -> {
            consumer.accept(c);
            return DEFAULT_RETURN_VALUE;
        });
    }

    public synchronized <R> R write(Function<Connection, R> function) {
        getPool().getActionWriteLock().lock();
        try {
            return function.apply(connection);
        } finally {
            getPool().getActionWriteLock().unlock();
        }
    }

    synchronized void close() {
        if (closed) {
            return;
        }
        try {
            connection.close();
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("sqlite connection close failed.", throwable);
            }
        } finally {
            closed = true;
        }
    }

    void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
