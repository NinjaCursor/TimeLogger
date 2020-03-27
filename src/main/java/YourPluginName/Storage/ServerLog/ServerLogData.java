package YourPluginName.Storage.ServerLog;

import YourPluginName.Storage.KeyValuePair;
import YourPluginName.Storage.Log.LogData;
import YourPluginName.Storage.Log.LogType;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("ServerLogData")
public class ServerLogData implements ConfigurationSerializable, KeyValuePair {

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("timestamp", getTimeStamp());
        map.put("event", getEvent().toString());
        return map;
    }

    public static ServerLogData deserialize(Map<String, Object> args) {
        return new ServerLogData(ServerEvent.valueOf((String) args.get("event")), ((Number) args.get("timestamp")).longValue());
    }

    @Override
    public Object getKey() {
        return null;
    }

    @Override
    public Object getValue() {
        return null;
    }
    private ServerEvent event;
    private long timeStamp;

    public ServerLogData(ServerEvent event, long timeStamp) {
        this.event = event;
        this.timeStamp = timeStamp;
    }

    public ServerEvent getEvent() {
        return event;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
