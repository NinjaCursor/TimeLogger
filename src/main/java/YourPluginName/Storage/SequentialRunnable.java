package YourPluginName.Storage;

import java.util.concurrent.CompletableFuture;

public abstract class SequentialRunnable<T> {

    protected CompletableFuture<T> completableFuture;

    public SequentialRunnable() {
        completableFuture = new CompletableFuture<>();
    }

    public CompletableFuture<T> getFuture() {
        return completableFuture;
    }

    abstract boolean run();
}
