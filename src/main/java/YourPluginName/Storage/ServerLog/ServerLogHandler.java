package YourPluginName.Storage.ServerLog;

import YourPluginName.Main.Main;
import YourPluginName.Storage.*;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ServerLogHandler implements GeneralHandler<Long, ServerLogData> {

    private ServerLogDatabase tools;
    private CompletableFuture<HashMap<Long, ServerLogData>> data;

    @Override
    public Handler<Long, ServerLogData> getHandler() {
        return tools;
    }

    public class RecoveryResults {
        private long timeStamp;
        private boolean crash;

        public RecoveryResults(long timeStamp, boolean crash) {
            this.timeStamp = timeStamp;
            this.crash = crash;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public boolean isCrash() {
            return crash;
        }
    }

    public ServerLogHandler(File homeDirectory) {
        //tools = new LocalFileTools<Long, ServerLogData>("ServerLog", homeDirectory, "ServerLog", (string1) -> Long.parseLong(string1));
        tools = new ServerLogDatabase();
        data = new CompletableFuture<>();
    }

    @Override
    public CompletableFuture<Boolean> setup() {
        return tools.setup();
    }

    public void start() {
        tools.update(new ServerLogData(ServerEvent.START, System.currentTimeMillis()));
    }

    public void stop() {
        tools.update(new ServerLogData(ServerEvent.SAFE_STOP, System.currentTimeMillis()));
    }

    private void update() {
        tools.update(new ServerLogData(ServerEvent.UPDATE_ALIVE, System.currentTimeMillis()));
    }

    public CompletableFuture<RecoveryResults> getCrashReport() {

        return tools.setup().thenCombine(tools.getData(), (success, data) -> {
            try {
                if (data == null)
                    return null;
                else if (data.size() < 1) {
                    return new RecoveryResults(-1, false);
                } else {
                    ServerLogData lastLog = data.get(data.size());
                    if (lastLog != null) {
                        tools.update(new ServerLogData(ServerEvent.CRASH_STOP, -1));
                        return new RecoveryResults(lastLog.getTimeStamp(), lastLog.getEvent() == ServerEvent.UPDATE_ALIVE);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

}
