package io.github.jiashunx.tools.sqlite3.table;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class SQLMetadata {

    private final String id;
    private final String name;
    private final String content;

    public SQLMetadata(String id, String content) {
        this(id, null, content);
    }

    public SQLMetadata(String id, String name, String content) {
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.content = Objects.requireNonNull(content);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
