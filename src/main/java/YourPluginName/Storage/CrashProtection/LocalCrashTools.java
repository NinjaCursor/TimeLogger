package YourPluginName.Storage.CrashProtection;

import YourPluginName.Main.TimePlayer;
import YourPluginName.Storage.GeneralDataTools;
import YourPluginName.Storage.LocalFileTools;
import YourPluginName.Storage.Log.LogData;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class LocalCrashTools extends LocalFileTools<CrashData> implements GeneralDataTools<CrashData, CrashData> {

    public LocalCrashTools(String fileName, File file, long autoSaveFrequency) throws Exception {
        super(fileName, file, autoSaveFrequency);
    }

    @Override
    public boolean setup() {
        return true;
    }

    @Override
    public CompletableFuture<CrashData> getData() {
        CompletableFuture<ArrayList<CrashData>> future = getData(((map -> {
            return CrashData.deserialize(map);
        })), "CrashData");

        return future.thenApply((list) -> {
            return list.get(0);
        });
    }

    @Override
    public void update(CrashData data) {
        set("CrashData", data);
    }
}
