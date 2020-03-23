package YourPluginName.Storage.Log;

import YourPluginName.Main.Main;
import YourPluginName.Main.TimePlayer;
import YourPluginName.Storage.GeneralDataTools;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class LogHandler implements GeneralDataTools<LogData, ArrayList<LogData>> {

    private GeneralDataTools<LogData, ArrayList<LogData>> usedStorageTools;
    private String name;
    private File homeDirectory;

    public LogHandler(String name, File homeDirectory) {
        this.name = name;
        this.homeDirectory = homeDirectory;
    }

    @Override
    public boolean setup() {
        String newName = name + "Log";
        if (Main.getPlugin().getConfig().getBoolean("use-database"))
            usedStorageTools = new LogDatabaseTools();
        else
            usedStorageTools = new LogLocalTools(homeDirectory, newName);

        boolean success = usedStorageTools.setup();
        Main.log().log(String.format("loginHandler Succeeded? " + success));
        return success;
    }

    @Override
    public CompletableFuture<ArrayList<LogData>> getData() {
        return usedStorageTools.getData();
    }

    @Override
    public void update(LogData data) {
        Main.log().log("Update at log handler");
        usedStorageTools.update(data);
    }

}
