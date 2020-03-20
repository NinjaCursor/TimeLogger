package YourPluginName.Storage;

import YourPluginName.Main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

public class LogHandler implements GeneralDataTools {

    private class LogDatabaseTools implements DatabaseTools<LogData> {

        private DatabaseTable.ColumnWrapper uuidColumn, loginColumn, logoutColumn, autoIncrementColumn;
        private DatabaseTable table;

        @Override
        public CompletableFuture<DatabaseTable> setupTables(String name) {
            autoIncrementColumn = new DatabaseTable.ColumnWrapper("ID", "INT NOT NULL AUTO_INCREMENT", "PRIMARY KEY");
            uuidColumn = new DatabaseTable.ColumnWrapper("uuid", "VARCHAR(36) NOT NULL", "");
            loginColumn = new DatabaseTable.ColumnWrapper("login", "BIGINT NOT NULL", "");
            logoutColumn = new DatabaseTable.ColumnWrapper("logout", "BIGINT", "");

            table = new DatabaseTable(name, autoIncrementColumn, uuidColumn, loginColumn, logoutColumn);
            return CompletableFuture.supplyAsync(() -> {
                table.create();
                return table;
            });
        }

        @Override
        public LogData getData(String type, ResultSet results) {

        }

        @Override
        public ThrowingConsumer<Connection> update(LogData data) {

        }

    }

    private class LogLocalTools implements LocalFileTools<LogData> {

        private FileConfiguration dataFile;

        @Override
        public boolean setupFile(String name) {
            try {
                File typeFolder = new File(Main.getPlugin().getDataFolder() + File.separator + "");
                File pluginFolder = Main.getPlugin().getDataFolder();

                //data files
                //create plugin sub directory if it does not already exist
                File userDataFiles = new File(pluginFolder + File.separator + "UserData");
                if (!userDataFiles.exists())
                    userDataFiles.mkdirs();

                //log of logins and logouts
                File logFile = new File(userDataFiles, "log.yml");

                if (!logFile.exists()) {
                    Main.log().log("log.yml not found, creating");
                    logFile.createNewFile();
                    dataFile = YamlConfiguration.loadConfiguration(logFile);
                    dataFile.set("created", System.currentTimeMillis());
                } else {
                    dataFile = YamlConfiguration.loadConfiguration(logFile);
                }

                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public LogData getData(String type, ResultSet results) {

        }

        @Override
        public ThrowingConsumer<Connection> update(LogData data) {
            return null;
        }
    }

    private LogDatabaseTools databaseTools;
    private LogLocalTools localTools;

    @Override
    public DatabaseTools getDatabaseTools() {
        return databaseTools;
    }

    @Override
    public LocalFileTools getLocalFileTools() {
        return localTools;
    }
}
