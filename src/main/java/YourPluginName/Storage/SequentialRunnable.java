package YourPluginName.Storage;

import java.util.concurrent.CompletableFuture;

public abstract class SequentialRunnable<T> {

    protected CompletableFuture<T> completableFuture;

    public SequentialRunnable(CompletableFuture<T> future) {
        completableFuture = future;
    }

    public SequentialRunnable() {
    }

    public abstract boolean run();
}
