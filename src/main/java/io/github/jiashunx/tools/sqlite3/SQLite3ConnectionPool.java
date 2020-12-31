package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.exception.ConnectionStatusChangedException;
import io.github.jiashunx.tools.sqlite3.exception.PoolStatusChangedException;
import io.github.jiashunx.tools.sqlite3.model.ConnectionPoolStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jiashunx
 */
public class SQLite3ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3ConnectionPool.class);

    private static final AtomicInteger counter = new AtomicInteger(0);
    private final ReentrantReadWriteLock actionLock = new ReentrantReadWriteLock();

    private final String poolName;

    private final LinkedList<SQLite3Connection> readConnectionPool = new LinkedList<>();
    private int readConnectionPoolSize;
    private volatile ConnectionPoolStatus readConnectionPoolStatus;

    private final LinkedList<SQLite3Connection> writeConnectionPool = new LinkedList<>();
    private final int writeConnectionPoolSize;
    private volatile ConnectionPoolStatus writeConnectionPoolStatus;

    public SQLite3ConnectionPool(Connection writeConnection, Connection... readConnectionArr) throws SQLException {
        if (readConnectionArr.length == 0) {
            throw new IllegalArgumentException("there is no read-only connection");
        }
        this.poolName = "sqlite-pool-" + counter.incrementAndGet();
        SQLite3Connection _writeConnection = new SQLite3WriteOnlyConnection(this, Objects.requireNonNull(writeConnection));
        _writeConnection.setName(this.poolName + "-write-1");
        this.writeConnectionPool.add(_writeConnection);
        this.writeConnectionPoolSize = this.writeConnectionPool.size();
        this.writeConnectionPoolStatus = ConnectionPoolStatus.RUNNING;
        for (int index = 0; index < readConnectionArr.length; index++) {
            Connection connection = Objects.requireNonNull(readConnectionArr[index]);
//            connection.setReadOnly(true);
            SQLite3ReadOnlyConnection readConnection = new SQLite3ReadOnlyConnection(this, connection);
            readConnection.setName(this.poolName + "-read-" + (index + 1));
            this.readConnectionPool.add(readConnection);
        }
        this.readConnectionPoolSize = this.readConnectionPool.size();
        this.readConnectionPoolStatus = ConnectionPoolStatus.RUNNING;
    }

    public synchronized void addConnection(Connection connection) throws PoolStatusChangedException {
        readConnectionPoolStatusCheck();
        if (connection != null) {
            SQLite3ReadOnlyConnection _connection = new SQLite3ReadOnlyConnection(this, connection);
            synchronized (readConnectionPool) {
                _connection.setName(getPoolName() + "-read-" + (readConnectionPool.size() + 1));
                readConnectionPool.addLast(_connection);
                readConnectionPool.notifyAll();
                readConnectionPoolSize++;
            }
        }
    }

    public int getReadConnectionPoolSize() {
        return readConnectionPoolSize;
    }

    public void release(SQLite3Connection connection) {
        if (connection instanceof SQLite3ReadOnlyConnection) {
            release(readConnectionPool, connection);
            return;
        }
        if (connection instanceof SQLite3WriteOnlyConnection) {
            release(writeConnectionPool, connection);
        }
    }

    private void release(LinkedList<SQLite3Connection> pool, SQLite3Connection connection) {
        if (pool != null && connection != null) {
            synchronized (pool) {
                // 连接释放后通知消费者连接池已归还连接
                if (!pool.contains(connection)) {
                    pool.addLast(connection);
                }
                pool.notifyAll();
            }
        }
    }

    public synchronized void close() throws InterruptedException, PoolStatusChangedException, ConnectionStatusChangedException {
        synchronized (writeConnectionPool) {
            writeConnectionPoolStatus = ConnectionPoolStatus.CLOSING;
            while (writeConnectionPool.size() != writeConnectionPoolSize) {
                writeConnectionPool.wait();
            }
            for (SQLite3Connection connection: writeConnectionPool) {
                connection.close();
            }
            writeConnectionPoolStatus = ConnectionPoolStatus.SHUTDOWN;
        }
        synchronized (readConnectionPool) {
            readConnectionPoolStatus = ConnectionPoolStatus.CLOSING;
            while (readConnectionPool.size() != readConnectionPoolSize) {
                readConnectionPool.wait();
            }
            for (SQLite3Connection connection: readConnectionPool) {
                connection.close();
            }
            readConnectionPoolStatus = ConnectionPoolStatus.SHUTDOWN;
        }
    }

    public SQLite3Connection fetchWriteConnection() throws PoolStatusChangedException {
        try {
            return fetchWriteConnection(0);
        } catch (InterruptedException exception) {
            if (logger.isErrorEnabled()) {
                logger.error("fetch write connection from pool [{}] failed", getPoolName(), exception);
            }
        }
        return null;
    }

    public SQLite3Connection fetchWriteConnection(long timeoutMillis) throws InterruptedException, PoolStatusChangedException {
        return fetchConnection(writeConnectionPool, timeoutMillis, this::writeConnectionPoolStatusCheck);
    }

    public SQLite3Connection fetchReadConnection() throws PoolStatusChangedException {
        try {
            return fetchReadConnection(0);
        } catch (InterruptedException exception) {
            if (logger.isErrorEnabled()) {
                logger.error("fetch read connection from pool [{}] failed", getPoolName(), exception);
            }
        }
        return null;
    }

    public SQLite3Connection fetchReadConnection(long timeoutMillis) throws InterruptedException, PoolStatusChangedException {
        return fetchConnection(readConnectionPool, timeoutMillis, this::readConnectionPoolStatusCheck);
    }

    private SQLite3Connection fetchConnection(LinkedList<SQLite3Connection> pool, long timeoutMillis, VoidFunc statusChecker)
            throws InterruptedException, PoolStatusChangedException {
        synchronized (pool) {
            if (timeoutMillis <= 0) {
                while (pool.isEmpty()) {
                    pool.wait();
                }
                statusChecker.apply();
                return pool.removeFirst();
            } else {
                long future = System.currentTimeMillis() + timeoutMillis;
                long remaining = timeoutMillis;
                while (pool.isEmpty() && remaining > 0) {
                    pool.wait(remaining);
                    remaining = future - System.currentTimeMillis();
                }
                statusChecker.apply();
                SQLite3Connection connection = null;
                if (!pool.isEmpty()) {
                    connection = pool.removeFirst();
                }
                return connection;
            }
        }
    }

    private void readConnectionPoolStatusCheck() throws PoolStatusChangedException {
        if (readConnectionPoolStatus == ConnectionPoolStatus.CLOSING) {
            throw new PoolStatusChangedException(String.format("connection pool [%s] for reading is closing.", getPoolName()));
        }
        if (readConnectionPoolStatus == ConnectionPoolStatus.SHUTDOWN) {
            throw new PoolStatusChangedException(String.format("connection pool [%s] for reading is closed.", getPoolName()));
        }
    }

    private void writeConnectionPoolStatusCheck() throws PoolStatusChangedException {
        if (writeConnectionPoolStatus == ConnectionPoolStatus.CLOSING) {
            throw new PoolStatusChangedException(String.format("connection pool [%s] for writing is closing.", getPoolName()));
        }
        if (writeConnectionPoolStatus == ConnectionPoolStatus.SHUTDOWN) {
            throw new PoolStatusChangedException(String.format("connection pool [%s] for writing is closed.", getPoolName()));
        }
    }

    public String getPoolName() {
        return poolName;
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
