package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.connection.SQLite3ConnectionManager;

import java.util.UUID;

/**
 * @author jiashunx
 */
public class MappingTest {
    public static void main(String[] args) {
        SQLite3JdbcTemplate jdbcTemplate0 = new SQLite3JdbcTemplate(SQLite3ConnectionManager.getConnectionPool("test/test.db"));
        String tableName = "MY_TABLE";
        boolean myTableExists = jdbcTemplate0.isTableExists(tableName);
        System.out.println(tableName + " exists ? " + myTableExists);
        if (!myTableExists) {
            jdbcTemplate0.executeUpdate("CREATE TABLE MY_TABLE(MYID VARCHAR, MYNAME VARCHAR)");
        }
        int myTableRowCount = jdbcTemplate0.queryTableRowCount(tableName);
        System.out.println(tableName + " row count ? " + myTableRowCount);
        MyEntity entity = new MyEntity();
        entity.setMyid(UUID.randomUUID().toString());
        entity.setMyname(UUID.randomUUID().toString());

        // 插入测试
        jdbcTemplate0.insert(entity);
        myTableRowCount = jdbcTemplate0.queryTableRowCount(tableName);
        System.out.println(tableName + " row count ? " + myTableRowCount);
        System.out.println(jdbcTemplate0.queryForMap("SELECT * FROM MY_TABLE WHERE MYID=?", statement -> {
            statement.setString(1, entity.getMyid());
        }));

        // 更新测试
        entity.setMyname("hhhhhhhhhhhhhhhhhhhhhhhhh");
        jdbcTemplate0.update(entity);
        myTableRowCount = jdbcTemplate0.queryTableRowCount(tableName);
        System.out.println(tableName + " row count ? " + myTableRowCount);
        System.out.println(jdbcTemplate0.queryForMap("SELECT * FROM MY_TABLE WHERE MYID=?", statement -> {
            statement.setString(1, entity.getMyid());
        }));
    }
}
