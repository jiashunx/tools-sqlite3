package io.github.jiashunx.tools.sqlite3.table;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class SQL {

    private String id;
    private String content;
    private String desc;
    private String className;

    public SQL() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = Objects.requireNonNull(content);
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
