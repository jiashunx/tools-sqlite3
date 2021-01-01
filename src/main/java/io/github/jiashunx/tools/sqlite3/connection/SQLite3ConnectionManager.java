package io.github.jiashunx.tools.sqlite3.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiashunx
 */
public class SQLite3ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3ConnectionManager.class);

    private static final Map<String, SQLite3ConnectionPool> POOL_MAP  = new HashMap<>();

    public static final int DEFAULT_POOL_SIZE = 16;
    public static final int MAX_POOL_SIZE = 256;
    public static final int MIN_POOL_SIZE = 2;
    public static final String DEFAULT_USERNAME = "sqlite";
    public static final String DEFAULT_PASSWORD = "sqlite";

    private SQLite3ConnectionManager() {}

    public static SQLite3ConnectionPool getConnectionPool(String fileName) {
        return getConnectionPool(fileName, DEFAULT_POOL_SIZE);
    }

    public static SQLite3ConnectionPool getConnectionPool(String fileName, int poolSize) {
        return getConnectionPool(fileName, poolSize, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public static SQLite3ConnectionPool getConnectionPool(String fileName, String username, String password) {
        return getConnectionPool(fileName, DEFAULT_POOL_SIZE, username, password);
    }

    public synchronized static SQLite3ConnectionPool getConnectionPool(String fileName, int poolSize
            , String username, String password) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("sqlite db filename can not be null or empty");
        }
        if (poolSize > MAX_POOL_SIZE || poolSize < MIN_POOL_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "sqlite db pool size can not less than %d and not large than %d"
                    , MIN_POOL_SIZE, MAX_POOL_SIZE));
        }
        try {
            File dbFile = new File(fileName);
            String dbFilePath = dbFile.getAbsolutePath().replace("\\", "/");
            File dbFileDir = dbFile.getParentFile();
            if (!dbFileDir.exists()) {
                dbFileDir.mkdirs();
            }
            String $url = "jdbc:sqlite:" + dbFilePath;
            String $username = String.valueOf(username);
            String $password = String.valueOf(password);
            SQLite3ConnectionPool pool = POOL_MAP.get(dbFilePath);
            if (pool != null) {
                if (poolSize > pool.getReadConnectionPoolSize()) {
                    pringLog($url, $username, $password);
                    for (int i = 0, size = poolSize - pool.getReadConnectionPoolSize(); i < size; i++) {
                        pool.addConnection(DriverManager.getConnection($url, $username, $password));
                    }
                }
                return pool;
            }
            pringLog($url, $username, $password);
            Connection writeConnection = DriverManager.getConnection($url, $username, $password);
            Connection[] readConnectionArr = new Connection[poolSize - 1];
            for (int i = 0 ; i < poolSize - 1; i++) {
                readConnectionArr[i] = DriverManager.getConnection($url, $username, $password);;
            }
            pool = new SQLite3ConnectionPool(writeConnection, readConnectionArr);
            POOL_MAP.put(dbFilePath, pool);
            return pool;
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("create sqlite connection failed.", throwable);
            }
        }
        return null;
    }

    private static void pringLog(String $url, String $username, String $password) {
        if (logger.isInfoEnabled()) {
            logger.info("create sqlite connection, url: {}, username: {}, password: {}", $url, $username, $password);
        }
    }

}
