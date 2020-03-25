package YourPluginName.Storage.CrashProtection;

import YourPluginName.Storage.KeyValuePair;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("CrashData")
public class CrashData implements ConfigurationSerializable, KeyValuePair<String, CrashData> {
    private ArrayList<UUID> uuidList;
    private long lastTimeStamp;

    public CrashData(ArrayList<UUID> uuids, long lastTimeStamp) {
        this.uuidList = uuids;
        this.lastTimeStamp = lastTimeStamp;
    }

    public ArrayList<UUID> getUuids() {
        return uuidList;
    }

    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    public static CrashData deserialize(Map<String, Object> map) {
        return new CrashData(getUUID((ArrayList<String>) map.get("uuids")), ((Number) map.get("last_time_stamp")).longValue());
    }

    //todo: put these in their own file
    private static ArrayList<UUID> getUUID(ArrayList<String> strings) {
        ArrayList<UUID> uuids = new ArrayList<>();
        for (String string : strings)
            uuids.add(UUID.fromString(string));
        return uuids;
    }

    //todo: put these in their own file
    private static ArrayList<String> getStrings(ArrayList<UUID> uuids) {
        ArrayList<String> uuidStrings = new ArrayList<>();

        for (UUID uuid : uuids)
            uuidStrings.add(uuid.toString());
        return uuidStrings;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("uuids", getStrings(getUuids()));
        map.put("last_time_stamp", lastTimeStamp);
        return map;
    }


    @Override
    public String getKey() {
        return "CrashData";
    }

    @Override
    public CrashData getValue() {
        return this;
    }
}
