package TimeSheet.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LogData implements Comparable<LogData> {

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

    public static LogData deserialize(ResultSet results) throws SQLException {
        String uuidString = results.getString("UUID");
        UUID uuid = UUID.fromString(uuidString);
        String event = results.getString("LOG_EVENT");
        long timeStamp = results.getLong("TIME_STAMP");
        long id = results.getLong("ID");
        return new LogData(LogType.valueOf(event), uuid, timeStamp, id);
    }

}
