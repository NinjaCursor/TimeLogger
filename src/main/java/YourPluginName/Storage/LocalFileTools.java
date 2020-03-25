package YourPluginName.Storage;

import YourPluginName.Main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class LocalFileTools<K, T extends ConfigurationSerializable & KeyValuePair<K, T>> implements GeneralDataTools<K, T> {

    private BlockingQueueHandler blockingQueueHandler;
    private FileConfiguration config;
    protected String fileName;
    private File file;
    private String path;
    private Function<String, K> keyMaker;

    /* LocalFileTools Constructor
     * @precondition: none
     * @postcondition: starts blockingqueuehandler
     */
    public LocalFileTools(String fileName, File file, String path, Function<String, K> keyMaker) {
        this.fileName = fileName;
        this.file = file;
        this.path = path;
        this.keyMaker = keyMaker;
        this.blockingQueueHandler = new BlockingQueueHandler();
    }

    /* setup() loads the files
     * @precondition: none
     * returns true if successful, else false
     */
    public boolean setup() {
        try {
            this.loadFile(file, fileName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void update(T data) {
        set(data.getKey(), data.getValue());
    }

    /* getUpdateRunnable() returns runnable to be run on every update
     * @precondition: none
     * returns: runnable that runs a file save of the config
     */
    @Override
    public Runnable getUpdateRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                saveToFile();
            }
        };
    }

    /* loadFile loads file if exists or creates default one
     * @postcondition: config and dataFile is initialized
     */
    private void loadFile(File homeDirectory, String fileName) throws Exception {
        this.fileName = fileName;
        try {
            file = new File(homeDirectory, String.format("%s.yml", fileName));
            Main.log().log("Path: " + path);
            if (!file.exists()) {
                Main.log().log(String.format("%s.yml not found, creating", fileName));
                file.createNewFile();
                config = YamlConfiguration.loadConfiguration(file);
                config.set("created", System.currentTimeMillis());
            } else {
                Main.log().log(fileName + ".yml found, loading...");
                config = YamlConfiguration.loadConfiguration(file);
            }
            if (config != null) {
                Main.log().log("It is not null!!!!!!!!!!!!!!!!!!!!!!!!");
            } else {
                Main.log().log("It is null!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(String.format("%s.yml could not be loaded nor created...", fileName));
        }
    };

    /* saveToFile() saves the current config to file
     * @params: none
     */
    private void saveToFile() {
        blockingQueueHandler.addRunnable(new SequentialRunnable<T>() {
            @Override
            public boolean run() {
                try {
                    Main.log().log(String.format("Saving to file \"%s.yml\"", fileName));
                    config.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    };

    /* run() runs a runnable in the thread
     * @params: runnable
     * @precondition: blockingQueueHandler is initialized
     * @postcondition: the blocking queue receives the runnable
     */
    public CompletableFuture<T> run(SequentialRunnable runnable) {
        blockingQueueHandler.addRunnable(runnable);
        return runnable.getFuture();
    }

    /* set() sets a key, value pair
     * @params: key and value
     * @precondition: none
     * @postcondition: config will contain either new or updated key value pair
     */
    private void set(K key, T data) {
        SequentialRunnable runnable = new SequentialRunnable() {
            @Override
            public boolean run() {
                //F must be used since i can't save numbers as normal keys without doing this
                //running configurationSection does not recognize numbers written as '1' or '2' with the quotes put on it
                //which is what seems to happen if i just remove the character F
                //i know this makes no sense but it works so who cares
                config.set(path + ".F" + data.getKey(), data);
                return true;
            }
        };
        blockingQueueHandler.addRunnable(runnable);
    }

    /* getData() loads data and returns map
     * @params: none
     * @precondition: the file has been loaded to the config file
     * returns: map with keys of type K and values of type T
     */
    public CompletableFuture<HashMap<K, T>> getData() {
        Main.log().log("getData for path " + path);

        SequentialRunnable<HashMap<K, T>> runnable = new SequentialRunnable() {
            @Override
            public boolean run() {
            HashMap<K, T> map = new HashMap<>();

            if (config.getConfigurationSection(path) != null) {
                for (String key : config.getConfigurationSection(path).getKeys(false)) {
                    map.put(keyMaker.apply(key.substring(1)), (T) config.get(path + "." + key));
                }
            }

            completableFuture.complete(map);
            return true;
            }
        };
        blockingQueueHandler.addRunnable(runnable);
        return runnable.getFuture();
    };

}
