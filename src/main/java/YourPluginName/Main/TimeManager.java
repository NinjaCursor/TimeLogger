package YourPluginName.Main;

import YourPluginName.Storage.CrashProtection.CrashData;
import YourPluginName.Storage.CrashProtection.CrashProtectionHandler;
import YourPluginName.Storage.Log.LogData;
import YourPluginName.Storage.Log.LogHandler;
import YourPluginName.Storage.Log.LogType;
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

    private long updateIntervalTicks = 20*30;

    public TimeManager(String name) throws Exception {
        //create folder for this time manager
        File homeDirectory = new File(Main.getPlugin().getDataFolder() + File.separator + name);
        if (!homeDirectory.exists())
            homeDirectory.mkdirs();

        //setup handlers
        timePlayers = new HashMap<>();
        logHandler = new LogHandler(homeDirectory);
        accHandler = new AccumulatedHandler(homeDirectory);
        crashHandler = new CrashProtectionHandler(homeDirectory);

        //load data
        if (logHandler.setup() && accHandler.setup() && crashHandler.setup()) {
            logHandler.getData().thenCombine(accHandler.getData(), (logMap, accMap) -> {
                Main.log().log("Regular Storage has been loaded!");
                crashHandler.getData().thenAccept((crashData) -> {
                    recover(logMap, accMap, crashData);
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> update(), 0, updateIntervalTicks);
                    loaded = true;
                });
               return null;
            });
        } else {
            throw new Exception(String.format("Could not load handlers for %s TimeManager", name));
        }



    }

    public void recover(HashMap<Long, LogData> logMap, HashMap<UUID, AccumulatedData> accMap, HashMap<String, CrashData> crashData) {

        HashMap<UUID, ArrayList<LogData>> logArrayMap = new HashMap<>();

        int count = 0;

        //sort logMap by ids and count logs
        for (Map.Entry<Long, LogData> logEntry : logMap.entrySet()) {
            count++;

            Long id = logEntry.getKey();
            LogData log = logEntry.getValue();

            ArrayList<LogData> logs = logArrayMap.get(log.getUuid());
            if (logs == null)
                logs = new ArrayList<>();

            logArrayMap.put(log.getUuid(), logs);
        }

        //sort logArrayMap
        for (Map.Entry<UUID, ArrayList<LogData>> logEntry : logArrayMap.entrySet()) {
            Collections.sort(logEntry.getValue());
        }

        //fill in missing data from crash
        for (Map.Entry<String, CrashData> entry : crashData.entrySet()) {
            CrashData crash = entry.getValue();
            for (UUID uuid : crash.getUuids()) {
                //create missing log
                LogData missingLog = new LogData(LogType.STOP, uuid, crash.getLastTimeStamp(), ++count);

                //write to offline storage
                logHandler.update(missingLog);

                //add to on server storage
                logArrayMap.get(uuid).add(missingLog);

            }
        }

        //populate timeplayers with loaded data
        for (Map.Entry<UUID, AccumulatedData> entry : accMap.entrySet()) {
            UUID uuid = entry.getKey();
            AccumulatedData accData = entry.getValue();

            TimePlayer newPlayer = new TimePlayer(accData, uuid, logArrayMap.get(uuid));

            timePlayers.put(uuid, newPlayer);
        }

        //do not need to recover accumulated storage since it is updated at the same time as crash data (well close enough anyway)
        //todo: figure out what to do so that writing to storages is kind of in sync
        //todo: or decide to give up and just assume it works fine 99% of the time XD
    }

    public void update() {
        //filter out non active players
        ArrayList<UUID> onlyActive = (ArrayList) timePlayers.entrySet().stream()
                .filter(entry -> entry.getValue().getAccData().isActive())
                .map(entry -> entry.getValue().getUuid())
                .collect(Collectors.toList());

        //update crashHandler storage to keep track of who is currently on server in case of server crash
        Main.log().log(String.format("Saving %s players to crash protection record!", onlyActive.size()));
        crashHandler.update(new CrashData(onlyActive, System.currentTimeMillis()));
        logHandler.getUpdateRunnable().run();
        crashHandler.getUpdateRunnable().run();
        accHandler.getUpdateRunnable().run();

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
        Main.log().log("Start!");
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

        Main.log().log("Stop!");
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
