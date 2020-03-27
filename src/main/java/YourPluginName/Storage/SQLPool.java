package YourPluginName.Storage;

import YourPluginName.Main.Main;
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
            FileConfiguration config = Main.getPlugin().getConfig();
            host = config.getString("host");
            port = config.getInt("port");
            database = config.getString("database");
            username = config.getString("username");
            password = config.getString("password");
        } catch (Exception e) {
            e.printStackTrace();
            Main.log().error("Could not load MySQL data from configuration file");
        }

        if (host == null) {
            Main.log().error("Host is null");
        }
        if (database == null) {
            Main.log().error("Database is null");
        }
        if (username == null) {
            Main.log().error("Username is null");
        }
        if (password == null) {
            Main.log().error("Password is null");
        }

        Main.log().log("Connecting to HOST: " + host + " on PORT: " + port + " FOR DATABASE " + database);
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
        Main.log().log("DATABASE IS WORKING!");
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static boolean sendCommand(ThrowingConsumer<Connection> function) {
        try (Connection connection = SQLPool.getConnection()) {
            try {
                connection.setAutoCommit(false);
                function.acceptThrows(connection);
                connection.commit();
                Main.log().log("Successful database transaction");
                return true;
            } catch (Exception e) {
                Main.log().error("Rolled back database transaction");
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Main.log().error("Unsuccessful database transaction");
        return false;
    }

    public static void close() {
        try {
            ds.close();
        } catch (Exception e) {
            Main.log().error("A SQLException was caught" + e);
        }
    }
}
