package YourPluginName.Storage;

import java.util.concurrent.CompletableFuture;

public interface GeneralDataTools<T, K> {
    boolean setup();
    CompletableFuture<K> getData();
    void update(T data);
}
