package TimeSheet.Main;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SummaryData {

    private long lastStartTime;
    private boolean active;

    private UUID uuid;

    private long total;

    private long starts;
    private long firstLoginTimeStamp;

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

    public SummaryData(UUID uuid) {
        this.uuid = uuid;
        this.total = 0;
        this.starts = 1;
        this.active = false;
        this.firstLoginTimeStamp = 0;
    }

    public SummaryData(UUID uuid, long total, long starts, long firstLoginTimeStamp, boolean currentlyActive) {
        this.uuid = uuid;
        this.total = total;
        this.starts = starts;
        this.active = currentlyActive;
        this.firstLoginTimeStamp = firstLoginTimeStamp;
    }

    public long getLastStartTime() {
        return lastStartTime;
    }

    public long getTotal() {
        return total;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getStarts() {
        return starts;
    }

    public long getFirstLoginTimeStamp() {
        return firstLoginTimeStamp;
    }

    public boolean isActive() {
        return active;
    }

    public static SummaryData deserialize(ResultSet results) throws SQLException {
        try {
            String uuidString = results.getString("UUID");
            UUID uuid = UUID.fromString(uuidString);
            long totalTime = results.getLong("TOTAL");
            long startCount = results.getLong("COUNT");
            long firstStart = results.getLong("FIRST_START");
            boolean activeNow = results.getBoolean("ACTIVE_NOW");
            return new SummaryData(uuid, totalTime, startCount, firstStart, activeNow);
        } catch (SQLException e) {
            e.printStackTrace();
            TimeSheet.log().log("An error occurred in deserializing Summary Data");
            return null;
        }
    }

}
