package io.github.jiashunx.tools.sqlite3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jiashunx
 */
public class SQLite3Manager {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3Manager.class);

    private static final Map<String, SQLite3ConnectionPool> POOL_MAP  = new HashMap<>();

    public static final int DEFAULT_POOL_SIZE = 16;
    public static final int MAX_POOL_SIZE = 256;
    public static final int MIN_POOL_SIZE = 1;
    public static final String DEFAULT_USERNAME = "sqlite";
    public static final String DEFAULT_PASSWORD = "sqlite";

    private SQLite3Manager() {}

    public static SQLite3ConnectionPool getConnectionPool(String fileName) {
        return getConnectionPool(fileName, DEFAULT_POOL_SIZE);
    }

    public static SQLite3ConnectionPool getConnectionPool(String fileName, int poolSize) {
        return getConnectionPool(fileName, poolSize, DEFAULT_USERNAME, DEFAULT_PASSWORD);
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
            String dbFilePath = new File(fileName).getAbsolutePath().replace("\\", "/");
            if (POOL_MAP.containsKey(dbFilePath)) {
                return POOL_MAP.get(dbFilePath);
            }
            String $url = "jdbc:sqlite:" + dbFilePath;
            String $username = String.valueOf(username);
            String $password = String.valueOf(password);
            if (logger.isInfoEnabled()) {
                logger.info("create sqlite connection, url: {}, username: {}, password: {}", $url, $username, $password);
            }
            List<SQLite3Connection> connectionList = new ArrayList<>(poolSize);
            for (int i = 0 ; i < poolSize; i++) {
                connectionList.add(new SQLite3Connection(DriverManager.getConnection($url, $username, $password)));
            }
            SQLite3ConnectionPool pool = new SQLite3ConnectionPool(connectionList.toArray(new SQLite3Connection[0]));
            POOL_MAP.put(dbFilePath, pool);
            return pool;
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("create sqlite connection failed.", throwable);
            }
        }
        return null;
    }

}
