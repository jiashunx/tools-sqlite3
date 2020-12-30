package io.github.jiashunx.tools.sqlite3.exception;

/**
 * @author jiashunx
 */
public class ConnectionStatusChangedException extends SQLite3Exception {
    public ConnectionStatusChangedException() {
        super();
    }
    public ConnectionStatusChangedException(String message) {
        super(message);
    }
    public ConnectionStatusChangedException(String message, Throwable cause) {
        super(message, cause);
    }
    public ConnectionStatusChangedException(Throwable cause) {
        super(cause);
    }
}
