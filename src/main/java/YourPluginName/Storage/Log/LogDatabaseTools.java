package YourPluginName.Storage.Log;
import YourPluginName.Main.Main;
import YourPluginName.Storage.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class LogDatabaseTools extends HandlerClass<Long, LogData> {

    private final String name = "LogData";

    private DatabaseTable.ColumnWrapper uuidColumn, timeColumn, eventColumn, autoIncrementColumn;
    private DatabaseTable table;

    public LogDatabaseTools() {
        super();
    }

    @Override
    public boolean setupSync() {
        autoIncrementColumn = new DatabaseTable.ColumnWrapper("ID", "INT NOT NULL AUTO_INCREMENT", "PRIMARY KEY");
        uuidColumn = new DatabaseTable.ColumnWrapper("UUID", "VARCHAR(36) NOT NULL", "");
        timeColumn = new DatabaseTable.ColumnWrapper("TIMESTAMP", "BIGINT NOT NULL", "");
        eventColumn = new DatabaseTable.ColumnWrapper("EVENT", "ENUM('START', 'STOP') NOT NULL", "");

        table = new DatabaseTable(name, autoIncrementColumn, uuidColumn, timeColumn, eventColumn);
        table.create();

        return table.successfulInit();
    }

    @Override
    public HashMap<Long, LogData> dataSync() {
        String sql = String.format("SELECT * FROM %s", table.getName());
        HashMap<Long, LogData> logs = new HashMap<>();

        try (Connection connection = SQLPool.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();

            Main.log().log("Database::getData::getConnection");

            while (results.next()) {
                int id = results.getInt(autoIncrementColumn.getName());
                String uuidString = results.getString(uuidColumn.getName());
                UUID uuid = UUID.fromString(uuidString);
                long timeStamp = results.getLong(timeColumn.getName());
                String type = results.getString(eventColumn.getName());
                LogType logType;
                if (type.equals("START"))
                    logType = LogType.START;
                else if (type.equals("STOP"))
                    logType = LogType.STOP;
                else {
                    Main.log().log("Unexpected Event Type " + type);
                    Main.log().log(String.format("\"%s\" == \"%s\" is %s", type, "START", type.equals("START")));
                    continue;
                }
                Main.log().log("Database::getData::getConnection::log::" + id);
                LogData logData = new LogData(logType, uuid, timeStamp, id);
                logs.put((long) id, logData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    @Override
    public void update(LogData data) {
        SequentialRunnable runnable = new SequentialRunnable() {
            @Override
            public boolean run() {
            try (Connection connection = SQLPool.getConnection()) {
                Main.log().log("================================================");
                String event;
                if (data.getLogType() == LogType.START)
                    event = "START";
                else
                    event = "STOP";

                String sql;

                sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES(?, ?, ?)", table.getName(), uuidColumn.getName(), timeColumn.getName(), eventColumn.getName());
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, data.getUuid().toString());
                stmt.setLong(2, data.getTimeStamp());
                stmt.setString(3, event);
                stmt.execute();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
            }
        };
        run(runnable);
    }

    @Override
    public Runnable getUpdateRunnable() {
        return new Runnable() {
            @Override
            public void run() {

            }
        };
    }

    @Override
    public HashMap<Long, LogData> handleRecovery(HashMap<Long, LogData> rawData) {
        return null;
    }


}
