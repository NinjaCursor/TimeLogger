package YourPluginName.Storage.Log;

import YourPluginName.Main.Main;
import YourPluginName.Storage.GeneralDataTools;
import YourPluginName.Storage.LocalFileTools;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class LogHandler implements GeneralDataTools<Long, LogData> {

    private GeneralDataTools<Long, LogData> tools;

    public LogHandler(File homeDirectory) {
        tools = new LocalFileTools<Long, LogData>("Log", homeDirectory, "Log", (string) -> Long.parseLong(string));
    }

    @Override
    public boolean setup() {
        boolean success = tools.setup();
        Main.log().log(String.format("loginHandler Succeeded? " + success));
        return success;
    }

    @Override
    public CompletableFuture<HashMap<Long, LogData>> getData() {
        return tools.getData();
    }

    @Override
    public void update(LogData data) {
        Main.log().log("Update at log handler");
        tools.update(data);
    }

    @Override
    public Runnable getUpdateRunnable() {
        return tools.getUpdateRunnable();
    }

}
