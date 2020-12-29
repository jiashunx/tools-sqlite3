package io.github.jiashunx.tools.sqlite3;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author jiashunx
 */
public class SQLite3Test {

    @Test
    public void test() {
        SQLite3JdbcTemplate jdbcTemplate = new SQLite3JdbcTemplate("test-sqlite.db");
        SQLite3JdbcTemplate jdbcTemplate1 = new SQLite3JdbcTemplate(SQLite3Manager.getConnectionPool("test-sqlite.db", 20));
        assertEquals(jdbcTemplate.getConnectionPool(), jdbcTemplate1.getConnectionPool());
    }

}
