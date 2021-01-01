package io.github.jiashunx.tools.sqlite3.mapping;

import java.lang.annotation.*;

/**
 * @author jiashunx
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SQLite3Table {
    /**
     * table name.
     */
    String tableName();
}
