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
        assertNotNull(jdbcTemplate);
    }

}
