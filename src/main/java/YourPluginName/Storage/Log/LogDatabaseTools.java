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
    private DatabaseTable logTable;

    private DatabaseTable.ColumnWrapper idColumn, rawEventColumn, deltaColumn;
    private DatabaseTable rawLogTable;

    private DatabaseTable.ColumnWrapper uuidPrimaryColumn, totalColumn, startCountColumn, firstTimeColumn;
    private DatabaseTable sumTable;

    public LogDatabaseTools() {
        super();
    }

    @Override
    public boolean setupSync() {

        sumTable = new DatabaseTable(name + "_Summary", uuidPrimaryColumn, totalColumn, startCountColumn, firstTimeColumn);
        sumTable.create();

        if (sumTable.successfulInit() && logTable.successfulInit()) {

            try (Connection connection = SQLPool.getConnection()) {
               // connection.
            } catch (SQLException e) {

            }


            boolean insertTriggerSuccess = SQLPool.sendCommand((connection) -> {
                String sql = String.format("delimiter # CREATE TRIGGER %1$s_LOG_INSERT_TRIGGER AFTER INSERT ON %1$s " +
                        "for each row begin " +
                        "IF (new.EVENT = 'START') THEN " +
                        "insert into %2$s(UUID, TIMESTAMP, EVENT) VALUES(new.UUID, new.TIMESTAMP, new.EVENT);" +
                        "insert into %3$s(%3$s, %4$s, %5$s, %6$s) VALUES(new.UUID, 0, 1, new.TIMESTAMP) on duplicate key UPDATE START_COUNT=(START_COUNT+1);" +
                        "ELSEIF (new.EVENT = 'STOP') THEN " +
                        "insert into %2$s(UUID, TIMESTAMP, EVENT) VALUES(new.UUID, new.TIMESTAMP, new.EVENT);" +
                        "UDPATE %2$s SET TOTAL=(TOTAL+new.DELTA)" +
                        "ELSE THEN" +
                        "" +
                        "END IF;", rawLogTable.getName(), logTable.getName(), sumTable.getName());
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.execute();
            });

            boolean updateTriggerSuccess = SQLPool.sendCommand((connection) -> {
                String sql = String.format("delimiter # CREATE TRIGGER %1$s_LOG_UPDATE_TRIGGER AFTER UDPATE ON %1$s " +
                        "for each row begin " +
                        "IF (new.EVENT = 'START') THEN " +
                        "insert into %2$s(%3$s, %4$s, %5$s, %6$s) VALUES(new.UUID, 0, 1, new.TIMESTAMP) on duplicate key UPDATE %5$s=%5$s+1;" +
                        "ELSE THEN " +
                        "UDPATE %2$s SET %4$s=(%4$s+new.DELTA)" +
                        "END IF;", logTable.getName(), sumTable.getName(), uuidPrimaryColumn.getName(), totalColumn.getName(), startCountColumn.getName(), firstTimeColumn.getName());
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.execute();
            });

        }


        return logTable.successfulInit();
    }

    @Override
    public HashMap<Long, LogData> dataSync() {
        String sql = String.format("SELECT * FROM %s", logTable.getName());
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

                sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES(?, ?, ?)", logTable.getName(), uuidColumn.getName(), timeColumn.getName(), eventColumn.getName());
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
