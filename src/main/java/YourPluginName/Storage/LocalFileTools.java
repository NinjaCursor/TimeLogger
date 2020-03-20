package YourPluginName.Storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

public interface LocalFileTools<T> {
    boolean setupFile(String name);
    T getData(String type, final ResultSet results);
    ThrowingConsumer<Connection> update(T data);
}
