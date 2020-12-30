package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.exception.PoolStatusChangedException;
import io.github.jiashunx.tools.sqlite3.model.ConnectionPoolStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
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

    private volatile ConnectionPoolStatus poolStatus;

    private final String poolName;

    private static AtomicInteger counter = new AtomicInteger(0);

    public SQLite3ConnectionPool(SQLite3Connection[] connections) {
        SQLite3Connection[] arr = Objects.requireNonNull(connections);
        pool = new LinkedList<>();
        poolName = "sqlite-pool-" + counter.incrementAndGet();
        for (int i = 0; i < arr.length; i++) {
            SQLite3Connection $connection = Objects.requireNonNull(arr[i]);
            synchronized ($connection) {
                if ($connection.getPool() != null) {
                    throw new IllegalArgumentException(String.format("connection [%s] has already assign to connection pool [%s]"
                            , $connection.getName(), $connection.getPool().getPoolName()));
                }
                $connection.setPool(this);
                $connection.setName(poolName + "-connection-" + (i + 1));
            }
            pool.addLast($connection);
        }
        if (pool.isEmpty()) {
            throw new IllegalArgumentException(String.format("connection pool [%s] has no connections.", poolName));
        }
        poolSize = pool.size();
        poolStatus = ConnectionPoolStatus.RUNNING;
    }

    public synchronized void addConnection(SQLite3Connection connection) {
        poolStatusCheck();
        if (connection != null) {
            synchronized (connection) {
                if (connection.getPool() != null) {
                    throw new IllegalArgumentException(String.format("connection [%s] has already assign to connection pool [%s]"
                            , connection.getName(), connection.getPool().getPoolName()));
                }
                connection.setPool(this);
                connection.setName(getPoolName() + "-connection-" + (poolSize + 1));
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
            poolStatus = ConnectionPoolStatus.CLOSING;
            while (pool.size() != poolSize) {
                pool.wait();
            }
            for (SQLite3Connection connection: pool) {
                connection.close();
            }
            poolStatus = ConnectionPoolStatus.SHUTDOWN;
        }
    }

    public SQLite3Connection fetch() {
        try {
            return fetch(0);
        } catch (InterruptedException exception) {
            if (logger.isErrorEnabled()) {
                logger.error("fetch connection from pool [{}] failed", getPoolName(), exception);
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
        if (poolStatus == ConnectionPoolStatus.CLOSING) {
            throw new PoolStatusChangedException(String.format("connection pool [%s] is closing.", getPoolName()));
        }
        if (poolStatus == ConnectionPoolStatus.SHUTDOWN) {
            throw new PoolStatusChangedException(String.format("connection pool [%s] is closed.", getPoolName()));
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

    public String getPoolName() {
        return poolName;
    }

}
