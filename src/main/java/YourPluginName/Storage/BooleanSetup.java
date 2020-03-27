package YourPluginName.Storage;

import java.util.concurrent.CompletableFuture;

public interface BooleanSetup {
    CompletableFuture<Boolean> setup();
}
