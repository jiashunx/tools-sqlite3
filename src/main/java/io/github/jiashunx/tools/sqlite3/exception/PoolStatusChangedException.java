package io.github.jiashunx.tools.sqlite3.exception;

/**
 * @author jiashunx
 */
public class PoolStatusChangedException extends RuntimeException {
    public PoolStatusChangedException() {
        super();
    }
    public PoolStatusChangedException(String message) {
        super(message);
    }
    public PoolStatusChangedException(String message, Throwable cause) {
        super(message, cause);
    }
    public PoolStatusChangedException(Throwable cause) {
        super(cause);
    }
}
