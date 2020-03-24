package YourPluginName.Storage.Summary;

import YourPluginName.Storage.GeneralDataTools;
import YourPluginName.Storage.LocalFileTools;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class AccumulatedLocal extends LocalFileTools<AccumulatedData> implements GeneralDataTools<AccumulatedData, ArrayList<AccumulatedData>> {

    public AccumulatedLocal(String fileName, File file, long autoSaveFrequency) throws Exception {
        super(fileName, file, autoSaveFrequency);
    }

    @Override
    public boolean setup() {
        return true;
    }

    @Override
    public CompletableFuture<ArrayList<AccumulatedData>> getData() {
            CompletableFuture<ArrayList<AccumulatedData>> future = getData((map -> {
                return AccumulatedData.deserialize(map);
            }), "AccumulatedData");

            return future;
    }

    @Override
    public void update(AccumulatedData data) {
        set(data.getUuid().toString(), data);
    }
}
