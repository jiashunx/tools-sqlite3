package io.github.jiashunx.tools.sqlite3.exception;

/**
 * @author jiashunx
 */
public class SQLite3SQLException extends SQLite3Exception {
    public SQLite3SQLException() {
        super();
    }
    public SQLite3SQLException(String message) {
        super(message);
    }
    public SQLite3SQLException(String message, Throwable cause) {
        super(message, cause);
    }
    public SQLite3SQLException(Throwable cause) {
        super(cause);
    }
}
