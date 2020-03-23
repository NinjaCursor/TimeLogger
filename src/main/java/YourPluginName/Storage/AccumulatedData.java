package YourPluginName.Storage;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("AccumulatedData")
public class AccumulatedData implements ConfigurationSerializable {

    private long lastStartTime;
    private boolean active;

    private UUID uuid;
    private long total;
    private long starts;
    private long firstLoginTimeStamp;
    private long averageTime;

    public void start() {
        this.starts++;
        this.active = true;
        this.lastStartTime = System.currentTimeMillis();
        if (this.starts == 1)
            this.firstLoginTimeStamp = this.lastStartTime;
    }

    public void stop() {
        this.total += (System.currentTimeMillis() - this.lastStartTime);
        this.active = false;
    }

    public AccumulatedData(UUID uuid) {
        this.uuid = uuid;
        this.starts = 1;
        this.total = 0;
        this.firstLoginTimeStamp = 0;
        this.averageTime = 0;
        this.active = false;
    }

    public AccumulatedData(UUID uuid, long total, long starts, long firstLoginTimeStamp) {
        this.uuid = uuid;
        this.total = total;
        this.starts = starts;
        this.firstLoginTimeStamp = firstLoginTimeStamp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getTotal() {
        if (active)
            return total + (System.currentTimeMillis() - lastStartTime);
        return total;
    }

    public long getStarts() {
        return starts;
    }

    public long getFirstLoginTimeStamp() {
        return firstLoginTimeStamp;
    }

    public long getAverageTime() {
        return getTotal() / this.starts;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uuid", getUuid());
        hashMap.put("total", getTotal());
        hashMap.put("punched_in_count", getStarts());
        hashMap.put("first_punch_in", getFirstLoginTimeStamp());
        return hashMap;
    }

    public static AccumulatedData deserialize(Map<String, Object> args) {
        AccumulatedData aData = new AccumulatedData(
                (UUID) args.get("uuid"),
                (long) args.get("total"),
                (long) args.get("punched_in_count"),
                (long) args.get("first_punch_in")
        );
        return aData;
    }

}
