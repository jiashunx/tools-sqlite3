package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.connection.SQLite3ConnectionManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

        MyEntity myEntity = jdbcTemplate0.queryForObj("SELECT * FROM MY_TABLE WHERE MYID=?", statement -> {
            statement.setString(1, entity.getMyid());
        }, MyEntity.class);
        System.out.println(myEntity);

        List<MyEntity> myEntityList = jdbcTemplate0.queryForList("SELECT * FROM MY_TABLE", MyEntity.class);
        System.out.println(myEntityList.size());

        String tableName2 = "MY_TABLE2";
        boolean table2Exists = jdbcTemplate0.isTableExists(tableName2);
        if (table2Exists) {
            jdbcTemplate0.executeUpdate("DROP TABLE MY_TABLE2");
        }
        String createTableSQL =
                "CREATE TABLE MY_TABLE2(" +
                    "    ID VARCHAR," +
                    "    VARCHAR VARCHAR," +
                    "    NVARCHAR NVARCHAR," +
                    "    INTEGER INTEGER," +
                    "    TEXT TEXT," +
                    "    FLOAT FLOAT," +
                    "    BLOB BLOB," +
                    "    CLOB CLOB," +
                    "    BOOLEAN BOOLEAN," +
                    "    NUMERIC NUMERIC(10,5)," +
                    "    DATE DATE," +
                    "    TIME TIME," +
                    "    TIMESTAMP TIMESTAMP" +
                    ")";
        jdbcTemplate0.executeUpdate(createTableSQL);
        MyEntity2 entity2 = new MyEntity2();
        entity2.setId(UUID.randomUUID().toString());
        entity2.setmVARCHAR("varchar,hhh");
        entity2.setmNVARCHAR("nvarchar 哈哈哈");
        entity2.setmINTEGER(10);
        entity2.setmTEXT("this is a long text.");
        entity2.setmFLOAT(1.25f);
        entity2.setmBLOB("hhhhhhhhhhhhhhhhhhhhhhh".getBytes());
        entity2.setmBOOLEAN(true);
        entity2.setmNUMERIC(BigDecimal.TEN);
        entity2.setmDATE(new Date());
        entity2.setmTIME(new Date());
        entity2.setmTIMESTAMP(new Date());
        jdbcTemplate0.insert(entity2);
        int table2RowCount = jdbcTemplate0.queryTableRowCount(tableName2);
        System.out.println("table2 row count: " + table2RowCount);
        List<Map<String, Object>> entity2MapList = jdbcTemplate0.queryForList("SELECT * FROM MY_TABLE2");
        List<MyEntity2> entity2List = jdbcTemplate0.queryForList("SELECT * FROM MY_TABLE2", MyEntity2.class);
        System.out.println(entity2List.size());
        System.out.println("MY_TABLE2 contains column: TIMESTAMP ?"
                + jdbcTemplate0.isTableColumnExists("MY_TABLE2", "TIMESTAMP"));
        System.out.println("MY_TABLE2 contains column: TIMESTAMPXX ?"
                + jdbcTemplate0.isTableColumnExists("MY_TABLE2", "TIMESTAMPXX"));
        System.out.println("is view MY_TABLE2_VIEW exists ? "
                + jdbcTemplate0.isViewExists("MY_TABLE2_VIEW"));
        System.out.println("is view MY_TABLE2_VIEW exists ? "
                + jdbcTemplate0.isViewExists("MY_TABLE2_VIEW22"));
    }
}
