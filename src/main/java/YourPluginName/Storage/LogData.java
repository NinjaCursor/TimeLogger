package YourPluginName.Storage;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("LogData")
public class LogData implements ConfigurationSerializable {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMMM yyyy HH:mm:ss.SSSZ");
    private LogType logType;
    private UUID uuid;
    private long timeStamp;
    private long id;

    public LogData(LogType logType, UUID uuid, long timeStamp, long id) {
        this.logType = logType;
        this.uuid = uuid;
        this.timeStamp = timeStamp;
        this.id = id;
    }

    public LogType getLogType() {
        return logType;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getId() {
        return this.id;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uuid", getUuid().toString());
        hashMap.put("time_stamp_epoch", getTimeStamp());
        hashMap.put("time_stamp_formatted", simpleDateFormat.format(new Date(getTimeStamp())));
        hashMap.put("description", logType.toString());
        hashMap.put("id", this.id);
        return hashMap;
    }

    public static LogData deserialize(Map<String, Object> args) {
        return new LogData(LogType.valueOf((String) args.get("description")), UUID.fromString((String) args.get("uuid")), (long) args.get("time_stamp_epoch"), (long) args.get("id"));
    }

}
