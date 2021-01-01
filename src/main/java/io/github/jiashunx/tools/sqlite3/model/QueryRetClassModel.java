package io.github.jiashunx.tools.sqlite3.model;

import java.util.Map;

/**
 * @author jiashunx
 */
public class QueryRetClassModel {
    private Class<?> klass;
    private Map<String, QueryRetColumnModel> retColumnModelMap;

    public Class<?> getKlass() {
        return klass;
    }

    public void setKlass(Class<?> klass) {
        this.klass = klass;
    }

    public Map<String, QueryRetColumnModel> getRetColumnModelMap() {
        return retColumnModelMap;
    }

    public void setRetColumnModelMap(Map<String, QueryRetColumnModel> retColumnModelMap) {
        this.retColumnModelMap = retColumnModelMap;
    }
}
