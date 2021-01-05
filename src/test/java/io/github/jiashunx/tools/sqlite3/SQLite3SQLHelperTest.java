package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.util.SQLite3SQLHelper;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class SQLite3SQLHelperTest {

    @Test
    public void test() {
        SQLite3SQLHelper.loadSQLPackageFromClasspath("table-constructure.xml");
    }
}
