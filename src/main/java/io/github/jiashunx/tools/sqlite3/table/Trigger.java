package io.github.jiashunx.tools.sqlite3.table;

/**
 * @author jiashunx
 */
public class Trigger {

    private String triggerName;
    private String triggerSQL;
    private String description;

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTriggerSQL() {
        return triggerSQL;
    }

    public void setTriggerSQL(String triggerSQL) {
        this.triggerSQL = triggerSQL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
