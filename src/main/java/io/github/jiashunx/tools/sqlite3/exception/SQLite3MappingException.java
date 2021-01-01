package io.github.jiashunx.tools.sqlite3.exception;

/**
 * @author jiashunx
 */
public class SQLite3MappingException extends SQLite3Exception {
    public SQLite3MappingException() {
        super();
    }
    public SQLite3MappingException(String message) {
        super(message);
    }
    public SQLite3MappingException(String message, Throwable cause) {
        super(message, cause);
    }
    public SQLite3MappingException(Throwable cause) {
        super(cause);
    }
}
