package YourPluginName.Main;

import YourPluginName.Storage.*;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeManager {

    private HashMap<UUID, TimePlayer> timePlayers;
    private LogHandler logHandler;
    private AccumulatedHandler accHandler;

    private long updateIntervalTicks = 20*60;

    public TimeManager(String name) {
        timePlayers = new HashMap<>();
        logHandler = new LogHandler();
        accHandler = new AccumulatedHandler();

        if (accHandler.setup(name) && logHandler.setup(name)) {

        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, 0, updateIntervalTicks);

    }

    public void update() {
        long currentTime = System.currentTimeMillis();
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
        TimePlayer timePlayer = getTimePlayer(uuid);
        try {
            logHandler.update(timePlayer.start());
            accHandler.update(timePlayer.getAccData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(UUID uuid) {
        TimePlayer timePlayer = getTimePlayer(uuid);
        try {
            logHandler.update(timePlayer.stop());
            accHandler.update(timePlayer.getAccData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
