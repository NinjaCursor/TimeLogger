package YourPluginName.Storage;

import YourPluginName.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocalFileTools<T extends ConfigurationSerializable> {

    private BlockingQueueHandler blockingQueueHandler;
    private FileConfiguration dataConfig;
    private String fileName;
    private File file;

    public LocalFileTools(String fileName, File file, long autoSaveFrequency) throws Exception {
        this.fileName = fileName;
        this.file = file;
        this.loadFile(file, fileName);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
            @Override
            public void run() {
                saveToFile();
            }
        }, autoSaveFrequency, autoSaveFrequency);
    }

    private void loadFile(File homeDirectory, String fileName) throws Exception {
        this.fileName = fileName;
        try {
            file = new File(homeDirectory, String.format("%s.yml", fileName));

            if (!file.exists()) {
                Main.log().log(String.format("%s.yml not found, creating", fileName));
                file.createNewFile();
                dataConfig = YamlConfiguration.loadConfiguration(file);
                dataConfig.set("created", System.currentTimeMillis());
            } else {
                dataConfig = YamlConfiguration.loadConfiguration(file);
            }
            blockingQueueHandler = new BlockingQueueHandler();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(String.format("%s.yml could not be loaded nor created...", fileName));
        }

    };

    private void saveToFile() {
        blockingQueueHandler.addRunnable(new SequentialRunnable<T>() {
            @Override
            public boolean run() {
                try {
                    Main.log().log(String.format("Saving to file \"%s.yml\"", fileName));
                    dataConfig.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    };

    public CompletableFuture<T> run(SequentialRunnable runnable) {
        blockingQueueHandler.addRunnable(runnable);
        return runnable.getFuture();
    }

    public void set(String key, Object data) {
        SequentialRunnable runnable = new SequentialRunnable() {
            @Override
            public boolean run() {
                dataConfig.set(key, data);
                return true;
            }
        };
    }

    public CompletableFuture<ArrayList<T>> getData(Function<Map<String,Object>, T> deserializer, String path) {
        SequentialRunnable<ArrayList<T>> runnable = new SequentialRunnable() {
            @Override
            public boolean run() {
                completableFuture.complete(dataConfig.getMapList(path).stream().map(serializedData -> deserializer.apply((Map<String, Object>)serializedData)).collect(Collectors.toList()));
                return true;
            }
        };
        blockingQueueHandler.addRunnable(runnable);
        return runnable.getFuture();
    };

}
