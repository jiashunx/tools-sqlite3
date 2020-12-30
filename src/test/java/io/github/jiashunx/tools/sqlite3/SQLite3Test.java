package io.github.jiashunx.tools.sqlite3;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jiashunx
 */
public class SQLite3Test {

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
        }
        tableExists = jdbcTemplate.isTableExists("TEST_LEE");
        System.out.println("table TEST_LEE exists ? " + tableExists);
        jdbcTemplate.getConnectionPool().close();
    }

}
