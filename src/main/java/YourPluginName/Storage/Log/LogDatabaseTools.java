package YourPluginName.Storage.Log;
import YourPluginName.Storage.DatabaseTable;
import YourPluginName.Storage.GeneralDataTools;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class LogDatabaseTools  {
//
//    private final String name = "LogData";
//
//    private DatabaseTable.ColumnWrapper uuidColumn, loginColumn, logoutColumn, autoIncrementColumn;
//    private DatabaseTable table;
//
//    @Override
//    public boolean setup() {
//        autoIncrementColumn = new DatabaseTable.ColumnWrapper("ID", "INT NOT NULL AUTO_INCREMENT", "PRIMARY KEY");
//        uuidColumn = new DatabaseTable.ColumnWrapper("uuid", "VARCHAR(36) NOT NULL", "");
//        loginColumn = new DatabaseTable.ColumnWrapper("login", "BIGINT NOT NULL", "");
//        logoutColumn = new DatabaseTable.ColumnWrapper("logout", "BIGINT", "");
//
//        table = new DatabaseTable(name, autoIncrementColumn, uuidColumn, loginColumn, logoutColumn);
//        table.create();
//        return table.successfulInit();
//    }
//
//    @Override
//    public CompletableFuture<ArrayList<LogData>> getData()  {
//        if (table.successfulInit()) {
//            return null;
//        }
//        return null;
//    }
//
//    @Override
//    public void update(LogData data) {
//        if (table.successfulInit()) {
//
//        }
//    }
//
//    @Override
//    public Runnable getUpdateRunnable() {
//        return null;
//    }

}
