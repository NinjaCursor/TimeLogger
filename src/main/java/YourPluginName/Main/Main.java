package YourPluginName.Main;
import YourPluginName.Storage.LogData;
import YourPluginName.Storage.SQLPool;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Main extends JavaPlugin {

    static {
        //ConfigurationSerialization.registerClass(Nest.class, "Nest");
    }

    private static JavaPlugin plugin;
    private static VertXLogger logger;
    private static FileConfiguration logDataConfig, accDataConfig;
    private static HashMap<String, TimeHandler> handlers;

    public static VertXLogger log() {
        return logger;
    }

    public static FileConfiguration getLogDataConfig() {
        return logDataConfig;
    }

    public static FileConfiguration getAccDataConfig() {
        return accDataConfig;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static VertXLogger getLog() {
        return logger;
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = new VertXLogger("VertXExamplePlugin");
        createConfig();

        LogData.setupTables("login_log").thenAccept((success) -> {

        });

    }

    private void createConfig() {
        getLogger().info("Creating configuration files...");
        try {
            //create plugin directory if it does not already exist
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            //main config file
            //create main config file if doesn't exist, or load if it does
            File mainConfigFile = new File(getDataFolder(), "config.yml");
            FileConfiguration config;
            if (!mainConfigFile.exists()) {
                getLogger().info("config.yml not found, creating");
                saveDefaultConfig();
                config = this.getConfig();
            } else {
                getLogger().info("config.yml discovered, loading");
                config = YamlConfiguration.loadConfiguration(mainConfigFile);
            }

            //data files
            //create plugin sub directory if it does not already exist
            File userDataFiles = new File(getDataFolder() + File.separator + "UserData");
            if (!userDataFiles.exists()) {
                userDataFiles.mkdirs();
            }

            //log of logins and logouts
            File logFile = new File(userDataFiles, "log.yml");
            if (!logFile.exists()) {
                getLogger().info("log.yml not found, creating");
                logFile.createNewFile();
                logDataConfig = YamlConfiguration.loadConfiguration(logFile);
                logDataConfig.set("created", System.currentTimeMillis());
            } else {
                logDataConfig = YamlConfiguration.loadConfiguration(logFile);
            }

            //acc of data e.g total time
            File accFile = new File(userDataFiles, "general.yml");
            if (!accFile.exists()) {
                getLogger().info("general.yml not found, creating");
                accFile.createNewFile();
                accDataConfig = YamlConfiguration.loadConfiguration(accFile);
                accDataConfig.set("created", System.currentTimeMillis());
            } else {
                accDataConfig = YamlConfiguration.loadConfiguration(logFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDisable() {
        SQLPool.close();
    }
}
