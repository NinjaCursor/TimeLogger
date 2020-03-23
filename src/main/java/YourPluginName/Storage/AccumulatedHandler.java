package YourPluginName.Storage;

import YourPluginName.Main.Main;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AccumulatedHandler implements GeneralDataTools<AccumulatedData> {

    public class DatabaseTools implements GeneralDataTools<AccumulatedData> {

        private DatabaseTable.ColumnWrapper uuidColumn, totalTimeColumn, totalLoginsColumn, firstTimeStampColumn;
        private DatabaseTable table;
        private String name;

        private BlockingQueueHandler queueHandler;


        public DatabaseTools(String name) {
            this.name = name;
            this.queueHandler = new BlockingQueueHandler();
        }

        @Override
        public boolean setup(String name) {
            uuidColumn = new DatabaseTable.ColumnWrapper("UUID", "VARCHAR(36) NOT NULL PRIMARY KEY", "");//todo: figure out how to set primary key
            totalTimeColumn = new DatabaseTable.ColumnWrapper("TOTAL_TIME", "BIGINT NOT NULL", "");
            totalLoginsColumn = new DatabaseTable.ColumnWrapper("TOTAL_START_COUNT", "INT NOT NULL", "");
            firstTimeStampColumn = new DatabaseTable.ColumnWrapper("FIRST_START_TMSTMP", "BIGINT NOT NULL", "");
            table = new DatabaseTable(name, uuidColumn, totalTimeColumn, totalLoginsColumn, firstTimeStampColumn);
            return table.create();
        }

        private ArrayList<AccumulatedData> loadData() {
            ArrayList<AccumulatedData> list = new ArrayList<>();
            try (Connection connection = SQLPool.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement(String.format("SELECT * FROM %s", table.getName()));
                ResultSet results = stmt.executeQuery();
                while (results.next()) {
                    String uuidString = results.getString("UUID");
                    UUID uuid = UUID.fromString(uuidString);

                    long total = results.getLong("TOTAL_TIME");
                    long starts = results.getLong("TOTAL_START_COUNT");
                    long firstLoginTimeStamp = results.getLong("FIRST_START_TMSTMP");

                    list.add(new AccumulatedData(uuid, total, starts, firstLoginTimeStamp));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        public CompletableFuture<List<AccumulatedData>> getData() throws Exception {
            SequentialRunnable<List<AccumulatedData>> runnable = new SequentialRunnable<List<AccumulatedData>>() {
                @Override
                boolean run() {
                    completableFuture.complete(loadData());
                    return true;
                }
            };
            queueHandler.addRunnable(runnable);
            return runnable.getFuture();
        }

        @Override
        public void update(AccumulatedData data) throws Exception {
            final AccumulatedData finalData = data;
            SequentialRunnable runnable = new SequentialRunnable() {
                @Override
                boolean run() {
                    try (Connection connection = SQLPool.getConnection()) {
                        PreparedStatement acc = connection.prepareStatement("UPDATE " + table.getName() + " SET " + totalTimeColumn.getName() + "=?, SET " + totalLoginsColumn.getName() +"=?, SET " + firstTimeStampColumn.getName() + "=? WHERE " + uuidColumn.getName() + "=?");
                        acc.setLong(1, finalData.getTotal());
                        acc.setLong(2, finalData.getStarts());
                        acc.setLong(3, finalData.getFirstLoginTimeStamp());
                        acc.setString(2, finalData.getUuid().toString());
                        acc.execute();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }
            };
            queueHandler.addRunnable(runnable);
        }
    }

    public class LocalTools extends LocalFileTools<AccumulatedData> {

        int timeBeforeStoring = 60*20;

        public LocalTools(String fileName) {
            super(fileName);
        }

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
        public CompletableFuture<List<AccumulatedData>> getData() throws Exception {
            SequentialRunnable runnable = new SequentialRunnable<List<AccumulatedData>>() {
                @Override
                boolean run() {
                    List<AccumulatedData> loadedData = dataFile.getMapList("LogData").stream().map(serializedData -> AccumulatedData.deserialize((Map<String, Object>)serializedData)).collect(Collectors.toList());
                    completableFuture.complete(loadedData);
                    return true;
                }
            };

            blockingQueueHandler.addRunnable(runnable);

            return runnable.getFuture();
        }

        @Override
        public void update(AccumulatedData data) throws Exception {

        }
    }

    private GeneralDataTools tools;

    @Override
    public boolean setup(String name) {
        String newName = name + "Accumulator";
        if (Main.getPlugin().getConfig().getBoolean("using-database")) {
            tools = new DatabaseTools(newName);
        } else {
            tools = new LocalTools(newName);
        }
        boolean success = tools.setup(newName);
        Main.log().log(String.format("AccumulatedHandler Succeeded? " + success));
        return success;
    }

    @Override
    public CompletableFuture<List<AccumulatedData>> getData() throws Exception {
        return tools.getData();
    }

    @Override
    public void update(AccumulatedData data) throws Exception {
        tools.update(data);
    }
}
