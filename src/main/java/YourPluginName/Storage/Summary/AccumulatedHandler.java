package YourPluginName.Storage.Summary;

import YourPluginName.Storage.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AccumulatedHandler implements GeneralHandler<UUID, AccumulatedData> {

    private Handler<UUID, AccumulatedData> tools;

    //todo: add database handler hee hee
    public AccumulatedHandler(File homeDirectory) {
        tools = new LocalFileTools("Summary", homeDirectory, "Summary", (string1) -> UUID.fromString((String) string1));
    }

    @Override
    public CompletableFuture<Boolean> setup() {
        return tools.setup();
    }

    @Override
    public Handler<UUID, AccumulatedData> getHandler() {
        return tools;
    }

}
