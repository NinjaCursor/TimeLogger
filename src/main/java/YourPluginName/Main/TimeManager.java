package YourPluginName.Main;

import YourPluginName.Storage.CrashProtection.CrashData;
import YourPluginName.Storage.CrashProtection.CrashProtectionHandler;
import YourPluginName.Storage.Log.LogData;
import YourPluginName.Storage.Log.LogHandler;
import YourPluginName.Storage.Summary.AccumulatedData;
import YourPluginName.Storage.Summary.AccumulatedHandler;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TimeManager {

    private HashMap<UUID, TimePlayer> timePlayers;
    private LogHandler logHandler;
    private AccumulatedHandler accHandler;
    private CrashProtectionHandler crashHandler;
    private boolean loaded = false;
    private long lastID = -1;

    private long updateIntervalTicks = 20*60;

    public TimeManager(String name) throws Exception {
        File homeDirectory = new File(Main.getPlugin().getDataFolder() + File.separator + name);
        if (!homeDirectory.exists())
            homeDirectory.mkdirs();

        timePlayers = new HashMap<>();
        logHandler = new LogHandler(name, homeDirectory);
        accHandler = new AccumulatedHandler();
        crashHandler = new CrashProtectionHandler(name, homeDirectory);

        if (logHandler.setup() && accHandler.setup() && crashHandler.setup()) {
            logHandler.getData().thenCombine(accHandler.getData(), (logList, accList) -> {
               crashHandler.getData().thenAccept((crashData) -> {

               });
               return null;
            });
        } else {
            throw new Exception(String.format("Could not load handlers for %s TimeManager", name));
        }


        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> update(), 0, updateIntervalTicks);

    }

    public void recover(CrashData crashData) {

    }

    public void update() {
        //filter out non active players
        ArrayList<UUID> onlyActive = (ArrayList) timePlayers.entrySet().stream()
                .filter(entry -> entry.getValue().getAccData().isActive())
                .map(entry -> entry.getValue().getUuid())
                .collect(Collectors.toList());

        //update crashHandler storage to keep track of who is currently on server in case of server crash
        try {
            crashHandler.update(new CrashData(onlyActive, System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public TimePlayer getTimePlayer(UUID uuid) {
        TimePlayer timePlayer = timePlayers.get(uuid);
        if (timePlayer == null) {
            timePlayer = new TimePlayer(uuid);
            timePlayers.put(uuid, timePlayer);
        }
        return timePlayer;
    }

    public void start(UUID uuid) {
        lastID++;
        TimePlayer timePlayer = getTimePlayer(uuid);
        try {
            logHandler.update(timePlayer.start(lastID));
            accHandler.update(timePlayer.getAccData());
            Main.log().log("Start!!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(UUID uuid) {
        TimePlayer timePlayer = getTimePlayer(uuid);
        lastID++;
        try {
            logHandler.update(timePlayer.stop(lastID));
            accHandler.update(timePlayer.getAccData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
