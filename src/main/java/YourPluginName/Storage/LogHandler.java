package YourPluginName.Storage;

import YourPluginName.Main.Main;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LogHandler implements GeneralDataTools<LogData> {

    private static class LogDatabaseTools implements GeneralDataTools<LogData> {

        private final String name = "LogData";

        private DatabaseTable.ColumnWrapper uuidColumn, loginColumn, logoutColumn, autoIncrementColumn;
        private DatabaseTable table;

        @Override
        public boolean setup(String name) {
            autoIncrementColumn = new DatabaseTable.ColumnWrapper("ID", "INT NOT NULL AUTO_INCREMENT", "PRIMARY KEY");
            uuidColumn = new DatabaseTable.ColumnWrapper("uuid", "VARCHAR(36) NOT NULL", "");
            loginColumn = new DatabaseTable.ColumnWrapper("login", "BIGINT NOT NULL", "");
            logoutColumn = new DatabaseTable.ColumnWrapper("logout", "BIGINT", "");

            table = new DatabaseTable(name, autoIncrementColumn, uuidColumn, loginColumn, logoutColumn);
            table.create();
            return table.successfulInit();
        }

        @Override
        public CompletableFuture<List<LogData>> getData() throws Exception {
            if (table.successfulInit()) {
                return null;
            } else {
                throw new Exception("Cannot getData from unsuccessfully created or initialized database.");
            }
        }

        @Override
        public void update(LogData data) throws Exception {
            if (table.successfulInit()) {

            } else {
                throw new Exception("Cannot update unsuccessfully created or initialized database.");
            }
        }

    }

    private static class LogLocalTools extends LocalFileTools<LogData> {

        private final String fileName = "logData";
        private final int timeBeforeStoring = 20*60;

        public LogLocalTools() {
            super("logData");
        }

        /* setup() sets up the local file storage
         * 1. sets up file and loads file if exists
         * 2. if successful with file loading, start repeating timer that saves to file on a separate thread
         * returns: success of setup
         */
        @Override
        public boolean setup(String name) {

            boolean success = setupFile(fileName);

            if (success) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        blockingQueueHandler.addRunnable(new SequentialRunnable() {
                            @Override
                            boolean run() {
                                try {
                                    saveToFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }
                        });
                    }
                }, timeBeforeStoring, timeBeforeStoring);
            }

            return setupFile(fileName);
        }

        @Override
        public CompletableFuture<List<LogData>> getData() throws Exception {
            SequentialRunnable runnable = new SequentialRunnable<List<LogData>>() {
                @Override
                boolean run() {
                    List<LogData> loadedData = dataFile.getMapList("LogData").stream().map(serializedData -> LogData.deserialize((Map<String, Object>)serializedData)).collect(Collectors.toList());
                    completableFuture.complete(loadedData);
                    return true;
                }
            };

            blockingQueueHandler.addRunnable(runnable);

            return runnable.getFuture();
        }

        @Override
        public void update(LogData data) throws Exception {
            SequentialRunnable runnable = new SequentialRunnable() {
                @Override
                boolean run() {
                dataFile.set(data.getUuid().toString(), data);
                return true;
                }
            };
            blockingQueueHandler.addRunnable(runnable);
        }
    }

    private GeneralDataTools<LogData> usedStorageTools;

    @Override
    public boolean setup(String name) {
        String newName = name + "Log";
        if (Main.getPlugin().getConfig().getBoolean("use-database"))
            usedStorageTools = new LogDatabaseTools();
        else
            usedStorageTools = new LogLocalTools();
        return usedStorageTools.setup(newName);
    }

    @Override
    public CompletableFuture<List<LogData>> getData() throws Exception {
        return usedStorageTools.getData();
    }

    @Override
    public void update(LogData data) throws Exception {
        usedStorageTools.update(data);
    }

}
