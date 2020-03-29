package TimeSheet.Main;
import TimeSheet.Storage.BlockingQueueThread;
import TimeSheet.Storage.SequentialRunnable;
import java.util.concurrent.ConcurrentHashMap;
import TimeSheet.Storage.SQLPool;
import org.bukkit.Bukkit;
import java.sql.PreparedStatement;
import java.util.UUID;

public class TimeManager extends BlockingQueueThread {

    private String name;
    private ConcurrentHashMap<UUID, UUID> uuidsActive;

    public TimeManager(String name) {
        super();

        this.name = name;
        this.uuidsActive = new ConcurrentHashMap<>();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(TimeSheet.getPlugin(), new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, TimeSheet.getUpdateTickDelay(), TimeSheet.getUpdateTickDelay());

    }

    private void update() {
        final long timeStamp = System.currentTimeMillis();

        SequentialRunnable runnable = new SequentialRunnable() {
            @Override
            public boolean run() {
                SQLPool.sendCommand((connection) -> {
                    for (UUID uuid : uuidsActive.values()) {
                        sendNewEvent(uuid, timeStamp, LogType.UDPATE);
                    }
                });
                return true;
            }
        };
        run(runnable);
    }

    private void sendNewEvent(UUID uuid, long timeStamp, LogType event) {
        SequentialRunnable runnable = new SequentialRunnable() {
            @Override
            public boolean run() {
                boolean success = SQLPool.sendCommand((connection) -> {
                    PreparedStatement stmt = connection.prepareStatement("CALL LOG_EVENT(?, ?, ?, ?)");
                    stmt.setString(1, uuid.toString());
                    stmt.setLong(2, timeStamp);
                    stmt.setString(3, event.toString());
                    stmt.setString(4, name);
                    stmt.execute();
                });

                return true;
            }
        };
        run(runnable);
    }

    public void start(UUID uuid, final long timeStamp) {
        sendNewEvent(uuid, timeStamp, LogType.START);
        uuidsActive.put(uuid, uuid);
    }

    public void stop(UUID uuid, final long timeStamp) {
        sendNewEvent(uuid, timeStamp, LogType.STOP);
        uuidsActive.remove(uuid);
    }

}
