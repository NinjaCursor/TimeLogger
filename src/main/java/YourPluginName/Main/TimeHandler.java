package YourPluginName.Main;

import YourPluginName.Storage.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class TimeHandler {
    private String tableName, type;
    private BlockingQueueHandler mysqlHandler;
    private ArrayList<UUID> doingEvent;
    private Response databaseReady;

    private void setupFiles() {
        try {
            //create plugin directory if it does not already exist
            /*if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            */

            File typeFolder = new File(Main.getPlugin().getDataFolder() + File.separator + "");


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


    public TimeHandler(String type) {
        this.type = type;
        this.tableName = this.type + "_log";

        databaseReady = Response.WAITING;
        mysqlHandler = new BlockingQueueHandler();
        LogData.setupTables(this.tableName).thenAcceptAsync((success) -> {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if (success)
                        mysqlHandler.start();
                    else
                        Main.getLog().log("Could not setup database table for type \"" + type +"\"");
                }
            });
        });
    }

    public void timeIn(UUID uuid) {
        doingEvent.add(uuid);

        long currentTime = System.currentTimeMillis();
        String thisType = this.type;

        if (databaseReady != Response.FAIL) {
            mysqlHandler.addRunnable(new SequentialRunnable() {
                @Override
                public boolean run() {
                    SQLPool.sendCommand(new LogData(thisType, uuid, currentTime).updateTable());//todo: change to .firstLog
                    return true;
                }
            });
        }

    }

    public void updateTimeOut(UUID uuid) {

        long currentTime = System.currentTimeMillis();
        String thisType = this.type;

        if (databaseReady != Response.FAIL) {
            mysqlHandler.addRunnable(new SequentialRunnable() {
                @Override
                public boolean run() {
                    SQLPool.sendCommand(new LogData(thisType, uuid, currentTime).updateTable());//todo: change to .firstLog
                    return true;
                }
            });
        }


    }

    public void timeOut(UUID uuid) {
        doingEvent.remove(uuid);

        updateTimeOut(uuid);


    }

}
