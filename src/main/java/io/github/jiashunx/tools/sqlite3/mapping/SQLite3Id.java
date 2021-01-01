package io.github.jiashunx.tools.sqlite3.mapping;

import java.lang.annotation.*;

/**
 * @author jiashunx
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SQLite3Id {
}
