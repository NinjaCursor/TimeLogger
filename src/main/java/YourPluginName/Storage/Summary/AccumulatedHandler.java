package YourPluginName.Storage.Summary;

import YourPluginName.Storage.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AccumulatedHandler implements GeneralHandler<UUID, AccumulatedData> {

    private Handler<UUID, AccumulatedData> tools;

    //todo: add database handler hee hee
    public AccumulatedHandler(File homeDirectory) {
        tools = new AccumulatedDatabase("Summary");
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
