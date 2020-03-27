package YourPluginName.Storage.ServerLog;
import java.sql.PreparedStatement;
import YourPluginName.Main.Main;
import YourPluginName.Storage.*;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

public class ServerLogDatabase extends HandlerClass<Long, ServerLogData> {

    private DatabaseTable.ColumnWrapper autoIncrementColumn, timeColumn, eventColumn;
    private DatabaseTable table;

    public ServerLogDatabase() {
        super();
    }

    @Override
    public boolean setupSync() {
        Main.log().log("ServerLogDatabase: setupSync()");
        autoIncrementColumn = new DatabaseTable.ColumnWrapper("ID", "INT NOT NULL AUTO_INCREMENT", "PRIMARY KEY");
        timeColumn = new DatabaseTable.ColumnWrapper("TIMESTAMP", "BIGINT NOT NULL", "");
        eventColumn = new DatabaseTable.ColumnWrapper("EVENT", "ENUM('START', 'SAFE_STOP', 'CRASH_STOP', 'UPDATE_ALIVE')", "");

        table = new DatabaseTable("SERVER_LOG", autoIncrementColumn, timeColumn, eventColumn);

        return table.create();
    }

    @Override
    public HashMap<Long, ServerLogData> dataSync() {
        Main.log().log("ServerLogDatabase: dataSync()");

        String sql = String.format("SELECT * FROM %s", table.getName());
        HashMap<Long, ServerLogData> logs = new HashMap<>();

        try (Connection connection = SQLPool.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                long id = results.getInt(autoIncrementColumn.getName());
                long timeStamp = results.getLong(timeColumn.getName());
                String eventString = results.getString(eventColumn.getName());
                ServerEvent serverEvent = ServerEvent.valueOf(eventString);
                logs.put(id, new ServerLogData(serverEvent, timeStamp));
            }

        } catch (SQLException e) {
            Main.log().log("An error occurred while loading the data");
        }

        Main.log().log("The Size of the server log is " + logs.size());

       return handleRecovery(logs);
    }

    private void storeStart(Connection connection, final long timeStamp) throws SQLException {
        //insert start
        String sql1 = String.format("INSERT INTO %s (%s, %s) VALUES(?, ?)", table.getName(), timeColumn.getName(), eventColumn.getName());
        PreparedStatement stmt = connection.prepareStatement(sql1);
        stmt.setLong(1, timeStamp);
        stmt.setString(2, "START");
        stmt.execute();

        //insert placeholder UPDATE_ALIVE event so that to update UPDATE_ALIVE, i just update last row with update_alive
        String sql2 = String.format("INSERT INTO %s (%s, %s) VALUES(?, ?)", table.getName(), timeColumn.getName(), eventColumn.getName());
        PreparedStatement stmt2 = connection.prepareStatement(sql2);
        stmt2.setLong(1, timeStamp);
        stmt2.setString(2, "UPDATE_ALIVE");
        stmt2.execute();
    }

    private void updateAlive(Connection connection, final long timeStamp) throws SQLException {
        String sql = String.format("UDPATE %s SET %s=? ORDER BY %s DESC LIMIT 1", table.getName(), timeColumn.getName(), autoIncrementColumn.getName());
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setLong(1, timeStamp);
        stmt.execute();
    }

    private void storeSafeStop(Connection connection) throws SQLException {
        String sql = String.format("UDPATE %s SET %s=? ORDER BY %s DESC LIMIT 1", table.getName(), eventColumn.getName(), autoIncrementColumn.getName());
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, "SAFE_STOP");
        stmt.execute();
    }

    private void storeCrashStop(Connection connection) throws SQLException {
        String sql = String.format("UDPATE %s SET %s=? ORDER BY %s DESC LIMIT 1", table.getName(), eventColumn.getName(), autoIncrementColumn.getName());
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, "CRASH_STOP");
        stmt.execute();
    }

    @Override
    public void update(ServerLogData data) {
        SequentialRunnable runnable = new SequentialRunnable() {
            @Override
            public boolean run() {

                final long timeStamp = data.getTimeStamp();

                try (Connection connection = SQLPool.getConnection()) {
                    if (data.getEvent() == ServerEvent.START) {
                        storeStart(connection, timeStamp);
                    } else if (data.getEvent() == ServerEvent.UPDATE_ALIVE) {
                        updateAlive(connection, timeStamp);
                    } else if (data.getEvent() == ServerEvent.SAFE_STOP) {
                        storeSafeStop(connection);
                    } else if (data.getEvent() == ServerEvent.CRASH_STOP) {
                        storeCrashStop(connection);
                    }
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
        return null;
    }

    @Override
    public HashMap<Long, ServerLogData> handleRecovery(HashMap<Long, ServerLogData> rawData) {
        ServerLogData lastLogData = rawData.get(rawData.size());

        if (lastLogData == null)
            return rawData;

        if (lastLogData.getEvent() == ServerEvent.UPDATE_ALIVE) {
            //crash occurred

            //fix crash
            lastLogData = new ServerLogData(ServerEvent.CRASH_STOP, lastLogData.getTimeStamp());

            //restore local copy
            rawData.put((long) rawData.size(), lastLogData);

            ServerLogData finalLastLogData = lastLogData;
            update(finalLastLogData);
        }
        return rawData;
    }

}
