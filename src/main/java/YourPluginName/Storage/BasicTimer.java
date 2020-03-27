package YourPluginName.Storage;

import java.util.UUID;

public interface BasicTimer {
    void start(UUID uuid);
    void stop(UUID uuid);
}
