package YourPluginName.Main;

import YourPluginName.Storage.*;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeManager {

    private HashMap<UUID, TimePlayer> timePlayers;
    private LogHandler logHandler;
    private AccumulatedHandler accHandler;
    private boolean loaded = false;
    private long lastID = -1;

    private long updateIntervalTicks = 20*60;

    public TimeManager(String name) throws Exception {
        timePlayers = new HashMap<>();
        logHandler = new LogHandler();
        accHandler = new AccumulatedHandler();

        if (accHandler.setup(name) && logHandler.setup(name)) {
            accHandler.getData().thenCombine(logHandler.getData(), (accDataList, logDataList) -> {
                HashMap<UUID, ArrayList<LogData>> logMap = new HashMap<>();
                //sort logs by UUID
                for (LogData log : logDataList) {
                    UUID currentUUID = log.getUuid();
                    ArrayList<LogData> currentLogs = new ArrayList<>();
                    if (logMap.containsKey(currentUUID)) {
                        currentLogs = logMap.get(log.getUuid());
                    }
                    logMap.put(currentUUID, currentLogs);
                }

                //populate timeplayers with data
                for (AccumulatedData accData : accDataList) {
                    UUID currentUUID = accData.getUuid();
                    ArrayList<LogData> currentLogs = new ArrayList<>();
                    timePlayers.put(accData.getUuid(), new TimePlayer(accData, currentUUID, logMap.get(currentUUID)));
                }

                lastID = logDataList.size();

                loaded = true;
                return null;
            });


        } else
            throw new Exception("Could not load data");

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, 0, updateIntervalTicks);

    }

    public void update() {
        for (Map.Entry<UUID, TimePlayer> timePlayer : timePlayers.entrySet()) {
            try {
                logHandler.update(timePlayer.getValue().getUpdateLog());
                accHandler.update(timePlayer.getValue().getAccData());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
