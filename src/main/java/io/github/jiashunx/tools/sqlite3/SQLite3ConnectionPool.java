package io.github.jiashunx.tools.sqlite3;

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

    /**
     * 数据库连接读写锁.
     */
    private final ReentrantReadWriteLock actionLock = new ReentrantReadWriteLock();

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
    }

    public void release(SQLite3Connection connection) {
        if (connection != null) {
            synchronized (pool) {
                // 连接释放后通知消费者连接池已归还连接
                pool.add(connection);
                pool.notifyAll();
            }
        }
    }

    public SQLite3Connection fetch() throws InterruptedException {
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
                return pool.removeFirst();
            } else {
                long future = System.currentTimeMillis() + timeoutMillis;
                long remaining = timeoutMillis;
                while (pool.isEmpty() && remaining > 0) {
                    pool.wait(remaining);
                    remaining = future - System.currentTimeMillis();
                }
                SQLite3Connection connection = null;
                if (!pool.isEmpty()) {
                    connection = pool.removeFirst();
                }
                return connection;
            }
        }
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
