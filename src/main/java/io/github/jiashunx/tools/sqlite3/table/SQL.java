package io.github.jiashunx.tools.sqlite3.table;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class SQL {

    private final String id;
    private final String content;
    private final String desc;
    private final String className;

    public SQL(String id, String content) {
        this(id, content, null);
    }

    public SQL(String id, String content, String desc) {
        this(id, content, desc, null);
    }

    public SQL(String id, String content, String desc, String className) {
        this.id = Objects.requireNonNull(id);
        this.content = Objects.requireNonNull(content);
        this.desc = desc;
        this.className = className;
    }

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public String getContent() {
        return content;
    }

    public String getClassName() {
        return className;
    }
}
