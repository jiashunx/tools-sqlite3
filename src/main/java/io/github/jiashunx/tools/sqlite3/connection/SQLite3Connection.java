package io.github.jiashunx.tools.sqlite3.connection;

import io.github.jiashunx.tools.sqlite3.SQLite3ConnectionPool;
import io.github.jiashunx.tools.sqlite3.exception.ConnectionStatusChangedException;
import io.github.jiashunx.tools.sqlite3.function.VoidFunc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author jiashunx
 */
public abstract class SQLite3Connection {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3Connection.class);

    private static final byte[] DEFAULT_RETURN_VALUE = new byte[0];

    private final SQLite3ConnectionPool connectionPool;
    private final Connection connection;
    private volatile boolean closed;
    private final ReentrantReadWriteLock statusLock = new ReentrantReadWriteLock();
    private String name;

    public SQLite3Connection(SQLite3ConnectionPool connectionPool, Connection connection) {
        this.connectionPool = Objects.requireNonNull(connectionPool);
        this.connection = Objects.requireNonNull(connection);
    }

    public void release() {
        this.connectionPool.release(this);
    }

    public void read(Consumer<Connection> consumer) throws ConnectionStatusChangedException {
        read(c -> {
            consumer.accept(c);
            return DEFAULT_RETURN_VALUE;
        });
    }

    public <R> R read(Function<Connection, R> function) throws ConnectionStatusChangedException {
        AtomicReference<R> reference = new AtomicReference<>();
        checkStatus(() -> {
            connectionPool.getActionReadLock().lock();
            try {
                reference.set(function.apply(this.connection));
            } finally {
                connectionPool.getActionReadLock().unlock();;
            }
        });
        return reference.get();
    }

    public void write(Consumer<Connection> consumer) throws ConnectionStatusChangedException {
        write(c -> {
            consumer.accept(c);
            return DEFAULT_RETURN_VALUE;
        });
    }

    public <R> R write(Function<Connection, R> function) throws ConnectionStatusChangedException {
        AtomicReference<R> reference = new AtomicReference<>();
        checkStatus(() -> {
            connectionPool.getActionWriteLock().lock();
            try {
                reference.set(function.apply(connection));
            } finally {
                connectionPool.getActionWriteLock().unlock();
            }
        });
        return reference.get();
    }

    public synchronized void close() throws ConnectionStatusChangedException {
        checkStatus(() -> {});
        writeStatus(() -> {
            try {
                connection.close();
            } catch (Throwable throwable) {
                if (logger.isErrorEnabled()) {
                    logger.error("connection [{}] close failed.", getName(), throwable);
                }
            } finally {
                closed = true;
            }
        });
    }

    private void checkStatus(VoidFunc voidFunc) throws ConnectionStatusChangedException {
        readStatus(() -> {
            if (closed) {
                throw new ConnectionStatusChangedException("connection is closed.");
            }
            if (voidFunc != null) {
                voidFunc.apply();
            }
        });
    }

    public void readStatus(VoidFunc voidFunc) {
        statusLock.readLock().lock();
        try {
            if (voidFunc != null) {
                voidFunc.apply();
            }
        } finally {
            statusLock.readLock().unlock();
        }
    }
    
    public void writeStatus(VoidFunc voidFunc) {
        statusLock.writeLock().lock();
        try {
            if (voidFunc != null) {
                voidFunc.apply();
            }
        } finally {
            statusLock.writeLock().unlock();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
