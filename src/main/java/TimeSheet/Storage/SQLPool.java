package TimeSheet.Storage;

import TimeSheet.Main.TimeSheet;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLPool {

    private static String host, username, database, password;
    private static int port;

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        try {
            FileConfiguration config = TimeSheet.getPlugin().getConfig();
            host = config.getString("host");
            port = config.getInt("port");
            database = config.getString("database");
            username = config.getString("username");
            password = config.getString("password");
        } catch (Exception e) {
            e.printStackTrace();
            TimeSheet.log().error("Could not load MySQL data from configuration file");
        }

        if (host == null) {
            TimeSheet.log().error("Host is null");
        }
        if (database == null) {
            TimeSheet.log().error("Database is null");
        }
        if (username == null) {
            TimeSheet.log().error("Username is null");
        }
        if (password == null) {
            TimeSheet.log().error("Password is null");
        }

        TimeSheet.log().log("Connecting to HOST: " + host + " on PORT: " + port + " FOR DATABASE " + database);
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "");
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.jdbc.Driver");

        config.addDataSourceProperty("autoReconnect", true);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);

        config.setConnectionTimeout(3000);

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static boolean sendCommand(ThrowingConsumer<Connection> function) {
        try (Connection connection = SQLPool.getConnection()) {
            try {
                connection.setAutoCommit(false);
                connection.commit();
                function.acceptThrows(connection);
                return true;
            } catch (Exception e) {
                TimeSheet.log().error("Rolled back database transaction");
                e.printStackTrace();
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        TimeSheet.log().error("Unsuccessful database transaction");
        return false;
    }

    public static void close() {
        try {
            ds.close();
        } catch (Exception e) {
            TimeSheet.log().error("A SQLException was caught" + e);
        }
    }
}
