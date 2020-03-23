package YourPluginName.Storage;

import YourPluginName.Main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class LocalFileTools<T> implements GeneralDataTools<T>{

    protected FileConfiguration dataFile;
    private File file;
    protected BlockingQueueHandler blockingQueueHandler;
    private String fileName;

    public LocalFileTools(String fileName) {
        this.fileName = fileName;
        blockingQueueHandler = new BlockingQueueHandler();
    }

    public boolean setupFile(String name) {
        try {
            File pluginFolder = Main.getPlugin().getDataFolder();

            //todo: this may not be needed here maybe make it a precondition?
            //data files
            //create plugin sub directory if it does not already exist
            File userDataFiles = new File(pluginFolder + File.separator + "UserData");
            if (!userDataFiles.exists())
                userDataFiles.mkdirs();

            //log of logins and logouts
            file = new File(userDataFiles, String.format("%s.yml", name));

            if (!file.exists()) {
                Main.log().log("log.yml not found, creating");
                file.createNewFile();
                dataFile = YamlConfiguration.loadConfiguration(file);
                dataFile.set("created", System.currentTimeMillis());
            } else {
                dataFile = YamlConfiguration.loadConfiguration(file);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    };

    public void saveToFile() throws IOException {
        blockingQueueHandler.addRunnable(new SequentialRunnable<T>() {
            @Override
            boolean run() {
                try {
                    dataFile.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        dataFile.save(file);
    };

}
