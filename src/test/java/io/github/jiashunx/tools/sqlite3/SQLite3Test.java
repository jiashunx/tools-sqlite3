package io.github.jiashunx.tools.sqlite3;

import org.junit.Test;

import java.sql.ResultSet;
import java.sql.Statement;

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

        jdbcTemplate.query(connection -> {
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery("SELECT COUNT (1) FROM LEE_TEST");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                try {
                    statement.close();
                } catch (Throwable throwable) {}
            }
            System.out.println(resultSet);
        });
    }

}
