package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.table.SQLPackage;
import io.github.jiashunx.tools.sqlite3.util.SQLite3SQLHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class SQLite3SQLHelperTest {

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

}
