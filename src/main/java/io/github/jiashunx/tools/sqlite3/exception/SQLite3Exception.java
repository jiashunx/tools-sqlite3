package io.github.jiashunx.tools.sqlite3.exception;

/**
 * @author jiashunx
 */
public class SQLite3Exception extends RuntimeException {
    public SQLite3Exception() {
        super();
    }
    public SQLite3Exception(String message) {
        super(message);
    }
    public SQLite3Exception(String message, Throwable cause) {
        super(message, cause);
    }
    public SQLite3Exception(Throwable cause) {
        super(cause);
    }
}
