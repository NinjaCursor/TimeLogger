package YourPluginName.Main;
import YourPluginName.Commands.ExampleCommand;
import YourPluginName.Listeners.LogInListener;
import YourPluginName.Storage.CrashProtection.CrashData;
import YourPluginName.Storage.Summary.AccumulatedData;
import YourPluginName.Storage.Log.LogData;
import YourPluginName.Storage.SQLPool;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Main extends JavaPlugin {

    private static JavaPlugin plugin;
    private static VertXLogger logger;
    private static TimeManager timeManager;

    static {
        ConfigurationSerialization.registerClass(LogData.class);
        ConfigurationSerialization.registerClass(AccumulatedData.class);
        ConfigurationSerialization.registerClass(CrashData.class);
    }

    public static VertXLogger log() {
        return logger;
    }
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static TimeManager getTimeManager() {
        return timeManager;
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = new VertXLogger("TimeLogger");
        createConfig();

        boolean usingDatabase = getConfig().getBoolean("use-database");
        try {
            timeManager = new TimeManager("logins");
        } catch (Exception e) {
            e.printStackTrace();
        }

        getCommand("example-command").setExecutor(new ExampleCommand("example-command", "time.total"));

        getServer().getPluginManager().registerEvents(new LogInListener(), this);

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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDisable() {
        SQLPool.close();
    }
}
