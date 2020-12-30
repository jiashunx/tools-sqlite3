package io.github.jiashunx.tools.sqlite3.exception;

/**
 * @author jiashunx
 */
public class SQLite3ConnectionPoolStatusChangedException extends RuntimeException {
    public SQLite3ConnectionPoolStatusChangedException() {
        super();
    }
    public SQLite3ConnectionPoolStatusChangedException(String message) {
        super(message);
    }
    public SQLite3ConnectionPoolStatusChangedException(String message, Throwable cause) {
        super(message, cause);
    }
    public SQLite3ConnectionPoolStatusChangedException(Throwable cause) {
        super(cause);
    }
}
