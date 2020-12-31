package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.exception.DataAccessException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author jiashunx
 */
public class SQLite3Test {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3Test.class);

    @Test
    public void test() throws InterruptedException {
        SQLite3JdbcTemplate jdbcTemplate = new SQLite3JdbcTemplate("test-sqlite.db");
        SQLite3JdbcTemplate jdbcTemplate1 = new SQLite3JdbcTemplate(SQLite3Manager.getConnectionPool("test-sqlite.db", 20));
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
                try {
                    statement.setString(1, mynameArr[index]);
                } catch (SQLException exception) {
                    throw new DataAccessException(exception);
                }
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
                try {
                    statement.setString(1, mynameArr1[index]);
                } catch (SQLException exception) {
                    throw new DataAccessException(exception);
                }
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
