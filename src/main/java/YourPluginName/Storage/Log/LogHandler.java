package YourPluginName.Storage.Log;
import YourPluginName.Storage.GeneralHandler;
import YourPluginName.Storage.Handler;
import YourPluginName.Storage.LocalFileTools;
import java.util.concurrent.CompletableFuture;
import java.io.File;

public class LogHandler implements GeneralHandler<Long, LogData> {

    private Handler<Long, LogData> tools;

    public LogHandler(File homeDirectory) {
        //tools = new LocalFileTools<Long, LogData>("Log", homeDirectory, "Log", (string) -> Long.parseLong(string));
        tools = new LogDatabaseTools();
    }

    @Override
    public CompletableFuture<Boolean> setup() {
        return tools.setup();
    }

    @Override
    public Handler<Long, LogData> getHandler() {
        return tools;
    }

}
