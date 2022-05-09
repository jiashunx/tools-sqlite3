package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.connection.SQLite3ConnectionManager;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Column;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Id;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Table;
import io.github.jiashunx.tools.sqlite3.table.SQLPackage;
import io.github.jiashunx.tools.sqlite3.util.SQLite3SQLHelper;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author jiashunx
 */
public class SQLite3Test {

    @Test
    public void test() {
        SQLPackage sqlPackage = SQLite3SQLHelper.loadSQLPackageFromClasspath("table-constructure.xml");
        Assert.assertNotNull(sqlPackage.getTableDefineSQL("TABLE_NAME"));
        Assert.assertNotNull(sqlPackage.getViewDefineSQL("VIEW_NAME"));
        Assert.assertNotNull(sqlPackage.getColumnDefineSQL("TABLE_NAME", "COLUMN_NAME2"));
    }

    @Test
    public void test2() {
        SQLPackage sqlPackage = SQLite3SQLHelper.loadSQLPackageFromClasspath("unittest.xml");
        Assert.assertNotNull(sqlPackage);
        SQLite3JdbcTemplate jdbcTemplate = new SQLite3JdbcTemplate("test/test.db");
        jdbcTemplate.initSQLPackage(sqlPackage);
        Assert.assertTrue(jdbcTemplate.isTableExists("MY_TABLE3"));
        Assert.assertTrue(jdbcTemplate.isViewExists("MY_TABLE3_VIEW"));
        Assert.assertTrue(jdbcTemplate.isTableColumnExists("MY_TABLE3", "NNNNNAME"));
    }

    /**
     * 并发测试.
     */
    @Test
    public void test3() throws Throwable {
        SQLite3JdbcTemplate jdbcTemplate = new SQLite3JdbcTemplate(SQLite3ConnectionManager.getConnectionPool("test/concurrency.db"));
        boolean table1Exists = jdbcTemplate.isTableExists("TABLE_1");
        System.out.println("TABLE_1 exists ? " + table1Exists);
        if (table1Exists) {
            jdbcTemplate.executeUpdate("DROP TABLE TABLE_1");
            System.out.println("DROP TABLE TABLE_1");
        }
        jdbcTemplate.executeUpdate("CREATE TABLE TABLE_1(LEE_NAME VARCHAR, LEE_AGE INT)");
        System.out.println("CREATE TABLE TABLE_1");
        boolean table2Exists = jdbcTemplate.isTableExists("TABLE_2");
        System.out.println("TABLE_2 exists ? " + table1Exists);
        if (table2Exists) {
            jdbcTemplate.executeUpdate("DROP TABLE TABLE_2");
            System.out.println("DROP TABLE TABLE_2");
        }
        jdbcTemplate.executeUpdate("CREATE TABLE TABLE_2(LEE_NAME VARCHAR, LEE_AGE INT)");
        System.out.println("CREATE TABLE TABLE_2");

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
                    System.out.println(Thread.currentThread().getName() + " - row count: " + table1RowCount);
                }
            });
            thread.setName("table-1-read-" + (i + 1));
            threadList.add(thread);
        }
        // table 1 写
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    int effectedRowCount = jdbcTemplate.executeUpdate("INSERT INTO TABLE_1(LEE_NAME, LEE_AGE) VALUES(?,?)", statement -> {
                        statement.setString(1, UUID.randomUUID().toString());
                        statement.setInt(2, 22);
                    });
                    System.out.println(Thread.currentThread().getName() + " - effected row count: " + effectedRowCount);
                    int table1RowCount = jdbcTemplate.queryForInt("SELECT COUNT(1) COUNT FROM TABLE_1");
                    System.out.println(Thread.currentThread().getName() + " - row count: " + table1RowCount);
                }
            });
            thread.setName("table-1-write-" + (i + 1));
            threadList.add(thread);
            thread.start();
        }
        // table 2 读
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    int table1RowCount = jdbcTemplate.queryForInt("SELECT COUNT(1) COUNT FROM TABLE_2");
                    System.out.println(Thread.currentThread().getName() + " - row count: " + table1RowCount);
                }
            });
            thread.setName("table-2-read-" + (i + 1));
            threadList.add(thread);
        }
        // table 2 写
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    int effectedRowCount = jdbcTemplate.executeUpdate("INSERT INTO TABLE_2(LEE_NAME, LEE_AGE) VALUES(?,?)", statement -> {
                        statement.setString(1, UUID.randomUUID().toString());
                        statement.setInt(2, 220);
                    });
                    System.out.println(Thread.currentThread().getName() + " - effected row count: " + effectedRowCount);
                    int table1RowCount = jdbcTemplate.queryForInt("SELECT COUNT(1) COUNT FROM TABLE_2");
                    System.out.println(Thread.currentThread().getName() + " - row count: " + table1RowCount);
                }
            });
            thread.setName("table-2-write-" + (i + 1));
            threadList.add(thread);
            thread.start();
        }
        // table 1&2 连接读
        // table 1&2 连接写
        for (Thread thread: threadList) {
            thread.join();
        }
        Assert.assertEquals(5001, jdbcTemplate.queryForInt("SELECT COUNT(1) COUNT FROM TABLE_1"));
        Assert.assertEquals(5001, jdbcTemplate.queryForInt("SELECT COUNT(1) COUNT FROM TABLE_2"));
    }

    @Test
    public void mappingTest() {
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
        System.out.println(jdbcTemplate0.getTableDefineSQL("MY_TABLE2"));
        System.out.println(jdbcTemplate0.getViewDefineSQL("MY_TABLE2_VIEW"));

    }

    @SQLite3Table(tableName = "MY_TABLE")
    public static class MyEntity {

        @SQLite3Id
        @SQLite3Column(columnName = "MYID")
        private String myid;

        @SQLite3Column(columnName = "MYNAME")
        private String myname;

        public String getMyid() {
            return myid;
        }

        public void setMyid(String myid) {
            this.myid = myid;
        }

        public String getMyname() {
            return myname;
        }

        public void setMyname(String myname) {
            this.myname = myname;
        }
    }
    @SQLite3Table(tableName = "MY_TABLE2")
    public static class MyEntity2 {

        @SQLite3Id
        @SQLite3Column(columnName = "ID")
        private String id;

        @SQLite3Column(columnName = "VARCHAR")
        private String mVARCHAR;

        @SQLite3Column(columnName = "NVARCHAR")
        private String mNVARCHAR;

        @SQLite3Column(columnName = "INTEGER")
        private int mINTEGER;

        @SQLite3Column(columnName = "TEXT")
        private String mTEXT;

        @SQLite3Column(columnName = "FLOAT")
        private float mFLOAT;

        @SQLite3Column(columnName = "BLOB")
        private byte[] mBLOB;

        @SQLite3Column(columnName = "BOOLEAN")
        private boolean mBOOLEAN;

        @SQLite3Column(columnName = "NUMERIC")
        private BigDecimal mNUMERIC;

        @SQLite3Column(columnName = "DATE")
        private Date mDATE;

        @SQLite3Column(columnName = "TIME")
        private Date mTIME;

        @SQLite3Column(columnName = "TIMESTAMP")
        private Date mTIMESTAMP;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getmVARCHAR() {
            return mVARCHAR;
        }

        public void setmVARCHAR(String mVARCHAR) {
            this.mVARCHAR = mVARCHAR;
        }

        public String getmNVARCHAR() {
            return mNVARCHAR;
        }

        public void setmNVARCHAR(String mNVARCHAR) {
            this.mNVARCHAR = mNVARCHAR;
        }

        public int getmINTEGER() {
            return mINTEGER;
        }

        public void setmINTEGER(int mINTEGER) {
            this.mINTEGER = mINTEGER;
        }

        public String getmTEXT() {
            return mTEXT;
        }

        public void setmTEXT(String mTEXT) {
            this.mTEXT = mTEXT;
        }

        public float getmFLOAT() {
            return mFLOAT;
        }

        public void setmFLOAT(float mFLOAT) {
            this.mFLOAT = mFLOAT;
        }

        public byte[] getmBLOB() {
            return mBLOB;
        }

        public void setmBLOB(byte[] mBLOB) {
            this.mBLOB = mBLOB;
        }

        public boolean ismBOOLEAN() {
            return mBOOLEAN;
        }

        public void setmBOOLEAN(boolean mBOOLEAN) {
            this.mBOOLEAN = mBOOLEAN;
        }

        public BigDecimal getmNUMERIC() {
            return mNUMERIC;
        }

        public void setmNUMERIC(BigDecimal mNUMERIC) {
            this.mNUMERIC = mNUMERIC;
        }

        public Date getmDATE() {
            return mDATE;
        }

        public void setmDATE(Date mDATE) {
            this.mDATE = mDATE;
        }

        public Date getmTIME() {
            return mTIME;
        }

        public void setmTIME(Date mTIME) {
            this.mTIME = mTIME;
        }

        public Date getmTIMESTAMP() {
            return mTIMESTAMP;
        }

        public void setmTIMESTAMP(Date mTIMESTAMP) {
            this.mTIMESTAMP = mTIMESTAMP;
        }
    }

}
