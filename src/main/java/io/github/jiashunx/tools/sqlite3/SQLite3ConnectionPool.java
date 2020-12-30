package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.exception.SQLite3ConnectionPoolStatusChangedException;
import io.github.jiashunx.tools.sqlite3.model.SQLite3ConnectionPoolStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jiashunx
 */
public class SQLite3ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3ConnectionPool.class);

    private final LinkedList<SQLite3Connection> pool;

    private int poolSize;

    /**
     * 数据库连接读写锁.
     */
    private final ReentrantReadWriteLock actionLock = new ReentrantReadWriteLock();

    private volatile SQLite3ConnectionPoolStatus poolStatus;

    public SQLite3ConnectionPool(SQLite3Connection[] connections) {
        SQLite3Connection[] arr = Objects.requireNonNull(connections);
        pool = new LinkedList<>();
        for (SQLite3Connection connection: arr) {
            SQLite3Connection $connection = Objects.requireNonNull(connection);
            synchronized ($connection) {
                if ($connection.getPool() != null) {
                    throw new IllegalArgumentException("connection has already assign connection pool.");
                }
                $connection.setPool(this);
            }
            pool.addLast($connection);
        }
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("connection pool do not have sqlite connections.");
        }
        poolSize = pool.size();
        poolStatus = SQLite3ConnectionPoolStatus.RUNNING;
    }

    public synchronized void addConnection(SQLite3Connection connection) {
        poolStatusCheck();
        if (connection != null) {
            synchronized (connection) {
                if (connection.getPool() != null) {
                    throw new IllegalArgumentException("connection has already assign connection pool.");
                }
                connection.setPool(this);
            }
            synchronized (pool) {
                pool.addLast(connection);
                pool.notifyAll();
            }
            poolSize++;
        }
    }

    public void release(SQLite3Connection connection) {
        if (connection != null) {
            synchronized (pool) {
                // 连接释放后通知消费者连接池已归还连接
                pool.addLast(connection);
                pool.notifyAll();
            }
        }
    }

    public synchronized void close() throws InterruptedException {
        poolStatusCheck();
        synchronized (pool) {
            poolStatus = SQLite3ConnectionPoolStatus.CLOSING;
            while (pool.size() != poolSize) {
                pool.wait();
            }
            for (SQLite3Connection connection: pool) {
                connection.close();
            }
            poolStatus = SQLite3ConnectionPoolStatus.SHUTDOWN;
        }
    }

    public SQLite3Connection fetch() {
        try {
            return fetch(0);
        } catch (InterruptedException exception) {
            if (logger.isErrorEnabled()) {
                logger.error("fetch sqlite connection failed", exception);
            }
        }
        return null;
    }

    public SQLite3Connection fetch(long timeoutMillis) throws InterruptedException {
        synchronized (pool) {
            if (timeoutMillis <= 0) {
                while (pool.isEmpty()) {
                    pool.wait();
                }
                poolStatusCheck();
                return pool.removeFirst();
            } else {
                long future = System.currentTimeMillis() + timeoutMillis;
                long remaining = timeoutMillis;
                while (pool.isEmpty() && remaining > 0) {
                    pool.wait(remaining);
                    remaining = future - System.currentTimeMillis();
                }
                poolStatusCheck();
                SQLite3Connection connection = null;
                if (!pool.isEmpty()) {
                    connection = pool.removeFirst();
                }
                return connection;
            }
        }
    }

    private void poolStatusCheck() {
        if (poolStatus == SQLite3ConnectionPoolStatus.CLOSING) {
            throw new SQLite3ConnectionPoolStatusChangedException("connection pool is closing.");
        }
        if (poolStatus == SQLite3ConnectionPoolStatus.SHUTDOWN) {
            throw new SQLite3ConnectionPoolStatusChangedException("connection pool is closed.");
        }
    }

    public synchronized int poolSize() {
        return poolSize;
    }

    public ReentrantReadWriteLock getActionLock() {
        return actionLock;
    }

    public ReentrantReadWriteLock.ReadLock getActionReadLock() {
        return getActionLock().readLock();
    }

    public ReentrantReadWriteLock.WriteLock getActionWriteLock() {
        return getActionLock().writeLock();
    }

}
