package YourPluginName.Storage.Summary;
import YourPluginName.Storage.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class AccumulatedDatabase extends HandlerClass<UUID, AccumulatedData> {

    private DatabaseTable.ColumnWrapper uuidColumn, firstTimeColumn, totalColumn, countColumn;
    private DatabaseTable table;
    private String name;

    public AccumulatedDatabase(String name) {
        super();
        this.name = name;
    }

    @Override
    public boolean setupSync() {
        uuidColumn = new DatabaseTable.ColumnWrapper("UUID", "VARCHAR(36) NOT NULL PRIMARY KEY", "");
        firstTimeColumn = new DatabaseTable.ColumnWrapper("FIRST_START", "BIGINT NOT NULL", "");
        totalColumn = new DatabaseTable.ColumnWrapper("TOTAL", "BIGINT NOT NULL", "");
        countColumn = new DatabaseTable.ColumnWrapper("COUNT_IN", "BIGINT NOT NULL", "");

        table = new DatabaseTable(name + "_SUMMARY");
        return table.create();
    }

    @Override
    public HashMap<UUID, AccumulatedData> dataSync() {
        HashMap<UUID, AccumulatedData> map = new HashMap<>();
        try (Connection connection = SQLPool.getConnection()) {
            String sql = String.format("SELECT * FROM %s", table.getName());
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                String uuidString = results.getString(uuidColumn.getName());
                UUID uuid = UUID.fromString(uuidString);
                long firstTime = results.getLong(firstTimeColumn.getName());
                long totalTime = results.getLong(totalColumn.getName());
                long count = results.getLong(countColumn.getName());
                map.put(uuid, new AccumulatedData(uuid, totalTime, count, firstTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public void update(AccumulatedData data) {
        SequentialRunnable runnable = new SequentialRunnable() {
            @Override
            public boolean run() {
                try (Connection connection = SQLPool.getConnection()) {
                    String sql = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE %s=?, %s=?", table.getName(), uuidColumn.getName(), firstTimeColumn.getName(), totalColumn.getName(), countColumn.getName(), totalColumn.getName(), countColumn.getName());
                    PreparedStatement stmt = connection.prepareStatement(sql);

                    //on insert
                    stmt.setString(1, data.getUuid().toString());
                    stmt.setLong(2, data.getFirstLoginTimeStamp());
                    stmt.setLong(3, data.getTotal());//todo: make sure this is returning right value XD
                    stmt.setLong(4, data.getStarts());

                    //on update
                    stmt.setLong(5, data.getTotal());//todo: make sure this is returning right value XD
                    stmt.setLong(6, data.getStarts());

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
        return null;
    }

    @Override
    public HashMap<UUID, AccumulatedData> handleRecovery(HashMap<UUID, AccumulatedData> rawData) {
        return null;
    }

}
