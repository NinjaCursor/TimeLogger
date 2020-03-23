package YourPluginName.Storage.CrashProtection;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("CrashData")
public class CrashData implements ConfigurationSerializable  {
    private ArrayList<UUID> uuids;
    private long lastTimeStamp;

    public CrashData(ArrayList<UUID> uuids, long lastTimeStamp) {
        this.uuids = uuids;
        this.lastTimeStamp = lastTimeStamp;
    }

    public ArrayList<UUID> getUuids() {
        return uuids;
    }

    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    public static CrashData deserialize(Map<String, Object> map) {
        return new CrashData((ArrayList) map.get("uuids"), ((Number) map.get("last_time_stamp")).longValue());
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("uuids", uuids);
        map.put("last_time_stamp", lastTimeStamp);
        return map;
    }
}
