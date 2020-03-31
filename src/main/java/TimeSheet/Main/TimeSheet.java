package TimeSheet.Main;
import TimeSheet.Commands.TotalTimeCommand;
import TimeSheet.Listeners.LogInListener;
import TimeSheet.Storage.DatabaseSetup;
import TimeSheet.Storage.SQLPool;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TimeSheet extends JavaPlugin {

    private static JavaPlugin plugin;
    private static VertXLogger logger;
    private static long updateTickDelay;
    private static CompletableFuture<TimeSheetAPI> pluginInterface;

    public static VertXLogger log() {
        return logger;
    }
    public static JavaPlugin getPlugin() {
        return plugin;
    }
    public static long getUpdateTickDelay() { return updateTickDelay; }
    public static CompletableFuture<TimeSheetAPI> getAPI() { return pluginInterface; }

    @Override
    public void onEnable() {
        plugin = this;
        logger = new VertXLogger("TimeSheet");

        createConfig();
        updateTickDelay = getConfig().getLong("update_delay");

        pluginInterface = new CompletableFuture<>();

        DatabaseSetup setup = new DatabaseSetup();

        setup.setup().thenAccept((success) -> {
           if (!success) {
               getServer().getPluginManager().disablePlugin(this);
           } else {
               setup.setupHandlers().thenAccept((handlerMap) -> {
                   if (handlerMap == null)
                        pluginInterface.complete(null);
                   else {
                       pluginInterface.complete(new TimeSheetAPI(handlerMap));
                   }

               });
           }
        });

        //handle server start
        final long timeStamp = System.currentTimeMillis();
        pluginInterface.thenCompose(api -> api.createHandler("SERVER_ONLINE")).thenAccept((success) -> {
            pluginInterface.thenAccept(api -> api.start("SERVER_ONLINE", "SERVER", timeStamp));
        });

        getCommand("totaltime").setExecutor(new TotalTimeCommand("totaltime", "time.total"));

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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDisable() {

        //handle server stop
        final long timeStamp = System.currentTimeMillis();
        pluginInterface.thenAccept(api -> {
            api.disable(timeStamp).thenAccept(f -> {
                SQLPool.close();
            });
        });
    }

}
