package YourPluginName.Main;

import YourPluginName.Storage.Summary.AccumulatedData;
import YourPluginName.Storage.Log.LogData;
import YourPluginName.Storage.Log.LogType;

import java.util.ArrayList;
import java.util.UUID;

public class TimePlayer {

    private AccumulatedData accData;
    private ArrayList<LogData> log;
    private UUID uuid;
    private boolean updatedInSession;

    public TimePlayer(UUID uuid) {
        this.uuid = uuid;
        this.log = new ArrayList<>();
        this.accData = new AccumulatedData(uuid);
        this.updatedInSession = false;
    }

    public TimePlayer(AccumulatedData accData, UUID uuid, ArrayList<LogData> log) {
        this.accData = accData;
        this.uuid = uuid;
        this.log = log;
        this.updatedInSession = false;
    }

    public LogData start(long id) {
        accData.start();
        LogData logData = new LogData(LogType.START, uuid, System.currentTimeMillis(), id);
        log.add(logData);
        return logData;
    }

    public LogData getUpdateLog() {
        this.updatedInSession = true;
        return new LogData(LogType.UPDATE_ACTIVE, uuid, System.currentTimeMillis(), log.get(log.size()-1).getId());
    }

    public LogData stop(long id) {
        accData.stop();
        LogData logData = new LogData(LogType.STOP, uuid, System.currentTimeMillis(), id);
        log.add(logData);
        this.updatedInSession = false;
        return logData;
    }

    public AccumulatedData getAccData() {
        return this.accData;
    }

    public UUID getUuid() {
        return this.uuid;
    }
}
