package YourPluginName.Storage;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public abstract class HandlerClass<K, T> extends BlockingQueueThread implements Handler<K, T> {

    private CompletableFuture<Boolean> setupFuture;
    private CompletableFuture<HashMap<K, T>> dataFuture;

    public HandlerClass() {
        super();

        dataFuture = new CompletableFuture<>();
        setupFuture = new CompletableFuture<>();

        run(new SequentialRunnable(setupFuture) {
            @Override
            public boolean run() {
                //Main.log().log("SETUP IN HANDLER CLASS");
                completableFuture.complete(setupSync());
                return true;
            }
        });

        run(new SequentialRunnable(dataFuture) {
            @Override
            public boolean run() {
                HashMap<K, T> map = dataSync();
                completableFuture.complete(map);
                return true;
            }
        });

    }

    public CompletableFuture<Boolean> setup() {
        return setupFuture;
    }

    public CompletableFuture<HashMap<K, T>> getData() {
        return dataFuture;
    }

    public abstract boolean setupSync();

    public abstract HashMap<K, T> dataSync();

}
