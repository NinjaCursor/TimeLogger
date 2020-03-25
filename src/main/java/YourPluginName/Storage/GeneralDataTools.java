package YourPluginName.Storage;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public interface GeneralDataTools<K, T> {
    boolean setup();
    CompletableFuture<HashMap<K, T>> getData();
    void update(T data);
    Runnable getUpdateRunnable();
}
