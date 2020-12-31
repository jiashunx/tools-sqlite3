package io.github.jiashunx.tools.sqlite3.connection;

import io.github.jiashunx.tools.sqlite3.exception.ConnectionStatusChangedException;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author jiashunx
 */
public class SQLite3ReadOnlyConnection extends SQLite3Connection {

    public SQLite3ReadOnlyConnection(SQLite3ConnectionPool connectionPool, Connection connection) {
        super(connectionPool, connection);
    }

    @Override
    public void write(Consumer<Connection> consumer) throws ConnectionStatusChangedException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R write(Function<Connection, R> function) throws ConnectionStatusChangedException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
