package YourPluginName.Storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

public interface DatabaseTools<T> {
    CompletableFuture<DatabaseTable> setupTables(String name);
    T getData(String type, final ResultSet results);
    ThrowingConsumer<Connection> update(T data);
}
