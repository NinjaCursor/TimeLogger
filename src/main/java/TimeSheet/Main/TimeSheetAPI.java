package TimeSheet.Main;

import TimeSheet.Storage.BlockingQueueThread;
import TimeSheet.Storage.SQLPool;
import TimeSheet.Storage.SequentialRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TimeSheetAPI extends BlockingQueueThread implements PluginInterfacePublic {

    private HashMap<String, TimeManager> managers;

    public TimeSheetAPI(HashMap<String, TimeManager> managers) {
        this.managers = managers;
    }

    public CompletableFuture<PlayerTimePackage> getTimePackage(String id, String eventName) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerTimePackage timePackage = null;

            try (Connection connection = SQLPool.getConnection()) {
                ArrayList<LogData> logs = new ArrayList<>();
                PreparedStatement query = connection.prepareStatement("CALL GET_LOG_DATA(?, ?);");
                query.setString(1, id);
                query.setString(2, eventName);
                ResultSet results = query.executeQuery();
                while (results.next()) {
                    LogData log = LogData.deserialize(results);
                    logs.add(log);
                }

                PreparedStatement accQuery = connection.prepareStatement("CALL GET_SUM_DATA(?, ?);");
                accQuery.setString(1, id);
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

    public void start(String name,String id, final long timeStamp) {
        try {
            managers.get(name).start(id, timeStamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(String name, String id, final long timeStamp) {
        try {
            managers.get(name).stop(id, timeStamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Boolean> createHandler(String name) {
        if (managers.containsKey(name))
            return CompletableFuture.completedFuture(false);
        else {
            managers.put(name, new TimeManager(name));
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            SequentialRunnable runnable = new SequentialRunnable(future) {
                @Override
                public boolean run() {
                    TimeSheet.log().log("Creating new event_name called " + name);
                    TimeSheet.log().log("MARKER 4");
                    try (Connection connection = SQLPool.getConnection()) {
                        PreparedStatement stmt = connection.prepareStatement("CALL REGISTER_EVENT(?);");
                        stmt.setString(1, name);
                        stmt.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    completableFuture.complete(true);
                    return true;
                }
            };
            run(runnable);
            return future;
        }
    }

    public CompletableFuture<Void> disable(final long timeStamp) {
        List<CompletableFuture> futures = new ArrayList<>();
        for (Map.Entry<String, TimeManager> entry : managers.entrySet()) {
            futures.add(entry.getValue().disable(timeStamp));
            TimeSheet.log().log("Logging stop for all players in " + entry.getKey());
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).thenAccept(f -> close());
    }
}
