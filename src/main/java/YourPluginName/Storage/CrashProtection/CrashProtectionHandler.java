package YourPluginName.Storage.CrashProtection;
import YourPluginName.Main.Main;
import YourPluginName.Main.TimePlayer;
import YourPluginName.Storage.GeneralDataTools;
import YourPluginName.Storage.LocalFileTools;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class CrashProtectionHandler implements GeneralDataTools<String, CrashData> {

    private GeneralDataTools<String, CrashData> tools;

    public CrashProtectionHandler(File homeDirectory) {
        tools = new LocalFileTools("CrashProtection", homeDirectory, "CrashProtection", (string1) -> string1);
    }

    @Override
    public boolean setup() {
        return tools.setup();
    }

    @Override
    public CompletableFuture<HashMap<String, CrashData>> getData() {
        return tools.getData();
    }


    @Override
    public void update(CrashData data) {
        tools.update(data);
    }

    @Override
    public Runnable getUpdateRunnable() {
        return tools.getUpdateRunnable();
    }

}
