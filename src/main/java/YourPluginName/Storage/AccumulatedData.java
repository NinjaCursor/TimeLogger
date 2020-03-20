package YourPluginName.Storage;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SerializableAs("AccumulatedData")
public class AccumulatedData implements ConfigurationSerializable {
    private UUID uuid;
    private long totalMillis;
    private long totalLogins;

    private static DatabaseTable.ColumnWrapper uuidColumn, totalTimeColumn, totalLoginsColumn;

    static {
        uuidColumn = new DatabaseTable.ColumnWrapper("UUID", "VARCHAR(36) NOT NULL PRIMARY KEY", "");//todo: figure out how to set primary key
        totalTimeColumn = new DatabaseTable.ColumnWrapper("TOTAL_TIME", "BIGINT NOT NULL", "");
        totalLoginsColumn = new DatabaseTable.ColumnWrapper("TOTAL_LOGINS", "INT NOT NULL", "");
    }

    public static CompletableFuture<DatabaseTable> setupTables(String name) {
        DatabaseTable table = new DatabaseTable(name, uuidColumn, totalTimeColumn, totalLoginsColumn);
        return CompletableFuture.supplyAsync(() -> {
            table.create();
            return table;
        });
    };

    public static LogData getLogDataFromTable(String type, final ResultSet results) throws Exception {
        try {
            String uuidString = results.getString(uuidColumn.getName());
            UUID uuid = UUID.fromString(uuidString);
            long loginTime = results.getLong(loginColumn.getName());

            try {
                //if logout time is not null
                long logoutTime = results.getLong(logoutColumn.getName());
                return new LogData(type, uuid, loginTime, logoutTime);
            } catch(Exception e) {
                e.printStackTrace();
                //if logout time is null
                return new LogData(type, uuid, loginTime);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Could not load logData");
        }
    }

    public ThrowingConsumer<Connection> updateTable(DatabaseTable table) {
        final boolean isLogOutNull = this.logoutIsNull;
        return new ThrowingConsumer<Connection>() {
            @Override
            public void acceptThrows(Connection conn) throws Exception {
                String sql;
                if (isLogOutNull) {
                    sql = String.format("INSERT INTO %s (%s, %s) VALUES(%s, %s)", table.getName(), uuidColumn.getName(), loginColumn.getName(), uuid, loginTime);
                } else {
                    sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES(%s, %s, %s) ON DUPLICATE KEY UPDATE %s=%s");
                }

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.execute();
            }
        };
    }

    public AccumulatedData(UUID uuid, long totalMillis, long totalLogins) {
        this.uuid = uuid;
        this.totalMillis = totalMillis;
        this.totalLogins = totalLogins;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getTotalMillis() {
        return totalMillis;
    }

    public void setTotalMillis(long totalMillis) {
        this.totalMillis = totalMillis;
    }

    public long getTotalLogins() {
        return totalLogins;
    }

    public void setTotalLogins(long totalLogins) {
        this.totalLogins = totalLogins;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uuid", getUuid());
        hashMap.put("total", getTotalMillis());
        hashMap.put("login_count", getTotalLogins());
        return hashMap;
    }

    public static AccumulatedData deserialize(Map<String, Object> args) {
        AccumulatedData aData = new AccumulatedData((UUID) args.get("uuid"), (long) args.get("total"), (long) args.get("login_count"));
        return aData;
    }

}
