package TimeSheet.Main;
import TimeSheet.Storage.BlockingQueueThread;
import TimeSheet.Storage.SequentialRunnable;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import TimeSheet.Storage.SQLPool;
import org.bukkit.Bukkit;
import java.sql.PreparedStatement;
import java.util.UUID;

public class TimeManager extends BlockingQueueThread {

    private String name;
    private ConcurrentHashMap<String, String> uuidsActive;

    public TimeManager(String name) {
        super();

        this.name = name;
        this.uuidsActive = new ConcurrentHashMap<>();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(TimeSheet.getPlugin(), new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, TimeSheet.getUpdateTickDelay()*20, TimeSheet.getUpdateTickDelay()*20);

    }

    private void update() {
        final long timeStamp = System.currentTimeMillis();

        SequentialRunnable runnable = new SequentialRunnable() {
            @Override
            public boolean run() {
                SQLPool.sendCommand((connection) -> {
                    for (String id: uuidsActive.values()) {
                        sendNewEvent(id, timeStamp, LogType.UPDATE);
                    }
                });
                return true;
            }
        };
        run(runnable);
    }

    //todo: make it void type completable future
    private CompletableFuture<Long> sendNewEvent(String playerIDString, long timeStamp, LogType event) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        SequentialRunnable runnable = new SequentialRunnable(future) {
            @Override
            public boolean run() {
                TimeSheet.log().log("SERVER SENDS STOP FOR " + name);
                boolean success = SQLPool.sendCommand((connection) -> {
                    TimeSheet.log().log("Marker 1");
                    PreparedStatement stmt = connection.prepareStatement("CALL LOG_EVENT(?, ?, ?, ?);");
                    stmt.setString(1, playerIDString);
                    stmt.setLong(2, timeStamp);
                    stmt.setString(3, event.toString());
                    stmt.setString(4, name);
                    stmt.execute();
                    TimeSheet.log().log("Marker 2");
                });
                future.complete(1L);
                TimeSheet.log().log("Marker 3");
                return true;
            }
        };
        run(runnable);
        return future;
    }

    public void start(String id, final long timeStamp) {
        sendNewEvent(id, timeStamp, LogType.START);
        uuidsActive.put(id, id);
    }

    public void stop(String id, final long timeStamp) {
        sendNewEvent(id, timeStamp, LogType.STOP);
        uuidsActive.remove(id);
    }

    public CompletableFuture<Void> disable(final long timeStamp) {
        TimeSheet.log().log(String.format("THERE ARE %s UUIDS ACTIVE IN %s", uuidsActive.size(), name));
        CompletableFuture<List<CompletableFuture>> futureList = new CompletableFuture<>();
        SequentialRunnable runnable = new SequentialRunnable(futureList) {
            @Override
            public boolean run() {
                List<CompletableFuture> listFutures = new ArrayList<>();
                SQLPool.sendCommand((connection) -> {
                    for (String id: uuidsActive.values()) {
                       listFutures.add(sendNewEvent(id, timeStamp, LogType.STOP));
                    }
                });
                futureList.complete(listFutures);
                return true;
            }
        };
        run(runnable);
        return futureList.thenCompose(list -> CompletableFuture.allOf(list.toArray(new CompletableFuture[list.size()]))).thenAccept((f) -> close());
    }

}
