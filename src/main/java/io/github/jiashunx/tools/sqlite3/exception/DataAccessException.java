package io.github.jiashunx.tools.sqlite3.exception;

/**
 * @author jiashunx
 */
public class DataAccessException extends SQLite3Exception {
    public DataAccessException() {
        super();
    }
    public DataAccessException(String message) {
        super(message);
    }
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    public DataAccessException(Throwable cause) {
        super(cause);
    }
}
