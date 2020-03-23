package YourPluginName.Storage.CrashProtection;
import YourPluginName.Storage.GeneralDataTools;
import java.util.concurrent.CompletableFuture;

public class DatabaseCrashTools implements GeneralDataTools<CrashData, CrashData> {
    @Override
    public boolean setup() {
        return false;
    }

    @Override
    public CompletableFuture<CrashData> getData() {
        return null;
    }

    @Override
    public void update(CrashData data) {

    }
}
