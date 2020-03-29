package TimeSheet.Main;

import TimeSheet.Storage.BlockingQueueThread;
import TimeSheet.Storage.SQLPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PluginInterface extends BlockingQueueThread implements PluginInterfacePublic {

    private HashMap<String, TimeManager> managers;

    public PluginInterface(HashMap<String, TimeManager> managers) {
        this.managers = managers;
    }

    public CompletableFuture<PlayerTimePackage> getTimePackage(UUID uuid, String eventName) {
        return new CompletableFuture<PlayerTimePackage>().thenApplyAsync((success) -> {
            PlayerTimePackage timePackage = null;

            try (Connection connection = SQLPool.getConnection()) {
                ArrayList<LogData> logs = new ArrayList<>();
                PreparedStatement query = connection.prepareStatement("CALL GET_LOG_DATA(?, ?);");
                query.setString(1, uuid.toString());
                query.setString(2, eventName);
                ResultSet results = query.executeQuery();
                while (results.next()) {
                    LogData log = LogData.deserialize(results);
                    logs.add(log);
                }

                PreparedStatement accQuery = connection.prepareStatement("CALL GET_SUM_DATA(?, ?);");
                accQuery.setString(1, uuid.toString());
                accQuery.setString(2, eventName);

                ResultSet accResults = accQuery.executeQuery();
                SummaryData data = null;
                if (accResults.next()) {
                    data = SummaryData.deserialize(accResults);
                }

                timePackage = new PlayerTimePackage(logs, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return timePackage;
        });

    };

    public void start(String name, UUID uuid, final long timeStamp) {
        try {
            managers.get(name).start(uuid, timeStamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(String name, UUID uuid, final long timeStamp) {
        try {
            managers.get(name).stop(uuid, timeStamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Boolean> createHandler(String name) {
        return (new CompletableFuture<Boolean>()).thenApplyAsync((success) -> {
            if (!success)
                return false;

            if (managers.containsKey(name))
                return false;
            else {
                managers.put(name, new TimeManager(name));
                CompletableFuture.runAsync(() -> {
                    SQLPool.sendCommand((connection) -> {
                        PreparedStatement stmt = connection.prepareStatement("CALL REGISTER_EVENT(?)");
                        stmt.setString(1, name);
                        stmt.execute();
                    });
                });
                return true;
            }
        });
    }

    public void close() {
        for (TimeManager manager : managers.values()) {
            manager.close();
        }
    }
}
