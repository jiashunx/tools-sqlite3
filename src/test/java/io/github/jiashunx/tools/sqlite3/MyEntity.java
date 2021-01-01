package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Column;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Id;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Table;

/**
 * @author jiashunx
 */
@SQLite3Table(tableName = "MY_TABLE")
public class MyEntity {

    @SQLite3Id
    @SQLite3Column(columnName = "MYID")
    private String myid;

    @SQLite3Column(columnName = "MYNAME")
    private String myname;

    public String getMyid() {
        return myid;
    }

    public void setMyid(String myid) {
        this.myid = myid;
    }

    public String getMyname() {
        return myname;
    }

    public void setMyname(String myname) {
        this.myname = myname;
    }
}
