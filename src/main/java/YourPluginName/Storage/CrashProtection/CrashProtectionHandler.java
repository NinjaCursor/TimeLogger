package YourPluginName.Storage.CrashProtection;
import YourPluginName.Main.Main;
import YourPluginName.Main.TimePlayer;
import YourPluginName.Storage.GeneralDataTools;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class CrashProtectionHandler implements GeneralDataTools<CrashData, CrashData> {

    private GeneralDataTools<CrashData, CrashData> tools;

    public CrashProtectionHandler(String name, File homeDirectory) {
        try {
            tools = new LocalCrashTools(name, homeDirectory, 60 * 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean setup() {
        if (Main.getPlugin().getConfig().getBoolean("using-database"))
            tools = new DatabaseCrashTools();

        return tools.setup();
    }

    @Override
    public CompletableFuture<CrashData> getData() {
        return tools.getData();
    }

    @Override
    public void update(CrashData data) {
        tools.update(data);
    }

}
