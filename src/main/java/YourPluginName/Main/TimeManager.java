package YourPluginName.Main;
import YourPluginName.Storage.BasicTimer;
import YourPluginName.Storage.BooleanSetup;
import YourPluginName.Storage.Log.LogData;
import YourPluginName.Storage.Log.LogHandler;
import YourPluginName.Storage.Log.LogType;
import YourPluginName.Storage.ServerLog.ServerLogHandler;
import YourPluginName.Storage.Summary.AccumulatedData;
import YourPluginName.Storage.Summary.AccumulatedHandler;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TimeManager implements BasicTimer, BooleanSetup {

    private CompletableFuture<HashMap<UUID, TimePlayer>> timePlayers;
    private LogHandler logHandler;
    private AccumulatedHandler accHandler;
    private long lastID = -1;

    public TimeManager(String name) {
        Main.log().log("TimeManager Constructor");
        //create folder for this time manager
        File homeDirectory = new File(Main.getPlugin().getDataFolder() + File.separator + name);
        if (!homeDirectory.exists())
            homeDirectory.mkdirs();

        //setup handlers
        timePlayers = new CompletableFuture<>();
        logHandler = new LogHandler(homeDirectory);
        accHandler = new AccumulatedHandler(homeDirectory);

    }

    //todo make BasicTImer parametized
    @Override
    public void start(UUID uuid) {
        Main.log().log("START() CALLED");
        timePlayers.thenAccept((players) -> {
            Main.log().log("START FINISHED");
            TimePlayer player = players.get(uuid);
            logHandler.getHandler().update(player.start(++lastID));
            accHandler.getHandler().update(player.getAccData());
        });
    }

    @Override
    public void stop(UUID uuid) {
        timePlayers.thenAccept((players) -> {
            TimePlayer player = players.get(uuid);
            logHandler.getHandler().update(player.stop(++lastID));
            accHandler.getHandler().update(player.getAccData());
        });
    }

    private HashMap<UUID, ArrayList<LogData>> getSorted(HashMap<Long, LogData> logs) {
        HashMap<UUID, ArrayList<LogData>> map = new HashMap<>();

        for (LogData log : logs.values()) {
            ArrayList<LogData> array = map.get(log.getUuid());
            if (array == null)
                array = new ArrayList<>();

            array.add(log);

            map.put(log.getUuid(), array);
        }

        for (ArrayList<LogData> logArray : map.values()) {
            Collections.sort(logArray);
        }

        return map;
    }

    private boolean recoverIndividual(UUID uuid, ArrayList<LogData> logs, ServerLogHandler.RecoveryResults results) {
        if (results == null)
            return false;

        if (logs.get(logs.size()-1).getLogType() == LogType.START) {
            if (results.isCrash()) {

                Main.log().log("Recovering stop data");
                return true;
            } else {
                // something weird happened
               // Main.log().log("Unexpected START event without a stop and without a crash. Please contact developer about this bug and shut down your server");
                return false;
            }
        } else
            return false;
    }

    private void loadData(HashMap<Long, LogData> logMap, HashMap<UUID, AccumulatedData> accs, ServerLogHandler.RecoveryResults results) {
        Main.log().log("HELLO");

        try {
            HashMap<UUID, ArrayList<LogData>> sortedLogs = getSorted(logMap);

            lastID = logMap.size();

            for (Map.Entry<UUID, ArrayList<LogData>> entry : sortedLogs.entrySet()) {
                if (recoverIndividual(entry.getKey(), entry.getValue(), results))
                    lastID++;
            }

            HashMap<UUID, TimePlayer> timePlayerHashMap = new HashMap<>();

            for (AccumulatedData accumulatedData : accs.values()) {
                timePlayerHashMap.put(accumulatedData.getUuid(), new TimePlayer(accumulatedData, accumulatedData.getUuid(), sortedLogs.get(accumulatedData.getUuid())));
            }


            timePlayers.complete(timePlayerHashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ManagerData {
        private HashMap<Long, LogData> logs;
        private HashMap<UUID, AccumulatedData> accs;

        public ManagerData(HashMap<Long, LogData> logs, HashMap<UUID, AccumulatedData> accs) {
            this.logs = logs;
            this.accs = accs;
        }

        public HashMap<Long, LogData> getLogs() {
            return logs;
        }

        public HashMap<UUID, AccumulatedData> getAccs() {
            return accs;
        }

    }

    @Override
    public CompletableFuture<Boolean> setup() {

        CompletableFuture<ManagerData> dataFuture = logHandler.getHandler().getData().thenCombine(accHandler.getHandler().getData(), (logData, accData) -> new ManagerData(logData, accData));

        CompletableFuture<Boolean> dataLoaded = dataFuture.thenCombine(Main.getServerLog().getCrashReport(), (managerData, recoveryResults) -> {
            Main.log().log("===========================================================MARKER !");
            if (managerData.getLogs() == null || managerData.getAccs() == null)
                return false;
            else {
                loadData(managerData.getLogs(), managerData.getAccs(), recoveryResults);
                return true;
            }
        });

        CompletableFuture<Boolean> setupLoaded = logHandler.getHandler().setup().thenCombine(accHandler.setup(), (logSetup, accSetup) -> logSetup && accSetup);

        return setupLoaded.thenCombine(dataLoaded, (dataSuccess, setupSuccess) -> dataSuccess && setupSuccess);

    }
}
