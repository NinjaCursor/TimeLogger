package YourPluginName.Storage.Log;

import YourPluginName.Storage.KeyValuePair;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class LogData implements Comparable<LogData>, KeyValuePair<Long, LogData> {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    private LogType logType;
    private long timeStamp;
    private UUID uuid;
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
    public int compareTo(LogData o) {
        if (this.id < o.getId())
            return -1;
        else if (this.id > o.getId())
            return 1;
        else
            return 0;
    }

    @Override
    public Long getKey() {
        return getId();
    }

    @Override
    public LogData getValue() {
        return this;
    }
}
