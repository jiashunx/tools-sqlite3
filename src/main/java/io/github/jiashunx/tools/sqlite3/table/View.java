package io.github.jiashunx.tools.sqlite3.table;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class View {

    private String viewName;
    private boolean temporary;
    private String content;
    private String viewDesc;

    public View() {}

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = Objects.requireNonNull(viewName);
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = Objects.requireNonNull(content);
    }

    public String getViewDesc() {
        return viewDesc;
    }

    public void setViewDesc(String viewDesc) {
        this.viewDesc = viewDesc;
    }
}
