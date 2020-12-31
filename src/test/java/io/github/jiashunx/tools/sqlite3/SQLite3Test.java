package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.connection.SQLite3ConnectionManager;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author jiashunx
 */
public class SQLite3Test {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3Test.class);

    /**
     * sqlite数据库连接池并发测试
     */
    public static void main(String[] args) throws InterruptedException {
        SQLite3JdbcTemplate jdbcTemplate = new SQLite3JdbcTemplate(SQLite3ConnectionManager.getConnectionPool("concurrency.db"));
        boolean table1Exists = jdbcTemplate.isTableExists("TABLE_1");
        logger.info("TABLE_1 exists ? {}", table1Exists);
        if (table1Exists) {
            jdbcTemplate.executeUpdate("DROP TABLE TABLE_1");
            logger.info("DROP TABLE TABLE_1");
        }
        jdbcTemplate.executeUpdate("CREATE TABLE TABLE_1(LEE_NAME VARCHAR, LEE_AGE INT)");
        logger.info("CREATE TABLE TABLE_1");
        boolean table2Exists = jdbcTemplate.isTableExists("TABLE_2");
        logger.info("TABLE_2 exists ? {}", table1Exists);
        if (table2Exists) {
            jdbcTemplate.executeUpdate("DROP TABLE TABLE_2");
            logger.info("DROP TABLE TABLE_2");
        }
        jdbcTemplate.executeUpdate("CREATE TABLE TABLE_2(LEE_NAME VARCHAR, LEE_AGE INT)");
        logger.info("CREATE TABLE TABLE_2");

        jdbcTemplate.executeUpdate("INSERT INTO TABLE_1(LEE_NAME, LEE_AGE) VALUES(?,?)", statement -> {
            statement.setString(1, "jiashunx");
            statement.setInt(2, 21);
        });
        jdbcTemplate.executeUpdate("INSERT INTO TABLE_2(LEE_NAME, LEE_AGE) VALUES(?,?)", statement -> {
            statement.setString(1, "jiashunx");
            statement.setInt(2, 22);
        });
        List<Thread> threadList = new ArrayList<>();
        // table 1 读
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    int table1RowCount = jdbcTemplate.queryForInt("SELECT COUNT(1) COUNT FROM TABLE_1");
                    logger.info("{} - row count: {}", Thread.currentThread().getName(), table1RowCount);
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            });
            thread.setName("table-1-read-" + (i + 1));
            threadList.add(thread);
            thread.start();
        }
        // table 1 写
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    int effectedRowCount = jdbcTemplate.executeUpdate("INSERT INTO TABLE_1(LEE_NAME, LEE_AGE) VALUES(?,?)", statement -> {
                        statement.setString(1, UUID.randomUUID().toString());
                        statement.setInt(2, 22);
                    });
                    logger.info("{} - effected row count: {}", Thread.currentThread().getName(), effectedRowCount);
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            });
            thread.setName("table-1-write-" + (i + 1));
            threadList.add(thread);
            thread.start();
        }
        // table 2 读
        // table 2 写
        // table 1&2 连接读
        // table 1&2 连接写
        for (Thread thread: threadList) {
            thread.join();
        }
    }

    @Test
    public void test() throws InterruptedException {
        SQLite3JdbcTemplate jdbcTemplate = new SQLite3JdbcTemplate("test-sqlite.db");
        SQLite3JdbcTemplate jdbcTemplate1 = new SQLite3JdbcTemplate(SQLite3ConnectionManager.getConnectionPool("test-sqlite.db"));
        assertEquals(jdbcTemplate.getConnectionPool(), jdbcTemplate1.getConnectionPool());

        boolean tableExists = jdbcTemplate.isTableExists("TEST_LEE");
        System.out.println("table TEST_LEE exists ? " + tableExists);
        if (!tableExists) {
            int updatedRow = jdbcTemplate.executeUpdate("CREATE TABLE TEST_LEE(LEE_NAME VARCHAR)");
            System.out.println("create table return value ? " + updatedRow);
            String[] mynameArr = new String[]{
                    "xxxx",
                    "yyy",
                    "zzz"
            };
            String sql = "INSERT INTO TEST_LEE(LEE_NAME) VALUES(?)";
            int insertRowCount = jdbcTemplate.batchInsert(sql, mynameArr.length, (index, statement) -> {
                statement.setString(1, mynameArr[index]);
            });
            System.out.println("batch insert row: " + insertRowCount);
            String[] mynameArr1 = new String[]{
                    "0xxxx",
                    "0yyy",
                    "0zzz"
            };
            jdbcTemplate.batchUpdate(new String[] {
                    sql, sql, sql
            }, (index, statement) -> {
                statement.setString(1, mynameArr1[index]);
            });
        }
        tableExists = jdbcTemplate.isTableExists("TEST_LEE");
        System.out.println("table TEST_LEE exists ? " + tableExists);
        List<Map<String, Object>> rowList = jdbcTemplate.queryForList("SELECT * FROM TEST_LEE");
        logger.info("rowList: {}", rowList);
        jdbcTemplate.getConnectionPool().close();
        assertEquals(6, rowList.size());
    }

}
