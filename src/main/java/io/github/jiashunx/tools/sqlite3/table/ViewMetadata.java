package io.github.jiashunx.tools.sqlite3.table;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class ViewMetadata {

    private final String viewName;
    private final boolean temporary;
    private final String content;

    public ViewMetadata(String viewName, boolean temporary, String content) {
        this.viewName = Objects.requireNonNull(viewName);
        this.temporary = temporary;
        this.content = Objects.requireNonNull(content);
    }

    public String getViewName() {
        return viewName;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public String getContent() {
        return content;
    }
}
