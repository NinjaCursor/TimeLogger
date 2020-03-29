package TimeSheet.Main;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TimeHandlerAPI {
    CompletableFuture<ArrayList<LogData>> getLogs(UUID uuid);
    CompletableFuture<SummaryData> getAccumulated(UUID uuid);
}
