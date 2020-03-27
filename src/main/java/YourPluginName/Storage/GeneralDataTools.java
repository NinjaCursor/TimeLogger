package YourPluginName.Storage;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public interface GeneralDataTools<K, T> {
    CompletableFuture<HashMap<K, T>> getData();
    void update(T data);
    Runnable getUpdateRunnable();
    HashMap<K, T> handleRecovery(HashMap<K, T> rawData);
}
