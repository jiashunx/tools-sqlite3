package io.github.jiashunx.tools.sqlite3.connection;

import io.github.jiashunx.tools.sqlite3.exception.ConnectionStatusChangedException;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author jiashunx
 */
public class SQLite3WriteOnlyConnection extends SQLite3Connection {

    public SQLite3WriteOnlyConnection(SQLite3ConnectionPool connectionPool, Connection connection) {
        super(connectionPool, connection);
    }

    public void read(Consumer<Connection> consumer) throws ConnectionStatusChangedException {
        super.write(consumer);
    }

    @Override
    public <R> R read(Function<Connection, R> function) throws ConnectionStatusChangedException {
        return super.write(function);
    }

}
