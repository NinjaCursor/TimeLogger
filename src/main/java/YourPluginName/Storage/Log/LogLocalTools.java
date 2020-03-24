package YourPluginName.Storage.Log;
import YourPluginName.Storage.GeneralDataTools;
import YourPluginName.Storage.LocalFileTools;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

//todo extend LocalFileTools instead of instancing
public class LogLocalTools extends LocalFileTools<LogData> implements GeneralDataTools<LogData, ArrayList<LogData>> {

    private final int timeBeforeStoring = 20*20;

    public LogLocalTools(File homeDirectory, String name) throws Exception {
        super(name, homeDirectory, 20 * 60);
    }

    /* setup() sets up the local file storage
     * 1. sets up file and loads file if exists
     * 2. if successful with file loading, start repeating timer that saves to file on a separate thread
     * returns: success of setup
     */
    @Override
    public boolean setup() { return true; }

    public HashMap<UUID, ArrayList<LogData>> getSortedData(ArrayList<LogData> logDataArrayList) {
        HashMap<UUID, ArrayList<LogData>> uuidToLogs = new HashMap<>();

        //sort by UUID
        for (LogData logData : logDataArrayList) {
            UUID currentUUID = logData.getUuid();
            ArrayList<LogData> logList = uuidToLogs.get(logData.getUuid());

            if (logList == null)
                logList = new ArrayList<>();

            logList.add(logData);
            uuidToLogs.put(currentUUID, logList);
        }

        //sort by id by UUID
        for (Map.Entry<UUID, ArrayList<LogData>> entry : uuidToLogs.entrySet()) {
            UUID currentUUID = entry.getKey();
            ArrayList<LogData> logList = entry.getValue();
            Collections.sort(logList);
        }
        return uuidToLogs;
    }

    @Override
    public CompletableFuture<ArrayList<LogData>> getData(){
        return getData((map -> LogData.deserialize(map)), "LogData");
    }

    @Override
    public void update(LogData data) {
        set(data.getId() + "", data);
    }

}
