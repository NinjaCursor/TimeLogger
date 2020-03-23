package YourPluginName.Storage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GeneralDataTools<T> {
    boolean setup(String name);
    CompletableFuture<List<T>> getData() throws Exception;
    void update(T data) throws Exception;
}
