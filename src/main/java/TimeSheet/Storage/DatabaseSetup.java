package TimeSheet.Storage;

import TimeSheet.Main.TimeSheet;
import TimeSheet.Main.TimeManager;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DatabaseSetup extends BlockingQueueThread {

    public CompletableFuture<Boolean> setup() {
        CompletableFuture<Boolean> succeeded = new CompletableFuture<>();
        SequentialRunnable runnable = new SequentialRunnable(succeeded) {
            @Override
            public boolean run() {
                try (Connection connection = SQLPool.getConnection()) {
                    System.out.println("Connection established......");

                    //Initialize the script runner
                    ScriptRunner sr = new ScriptRunner(connection);

                    //Creating a reader object
                    Reader reader = new BufferedReader(new InputStreamReader(TimeSheet.getPlugin().getClass().getResourceAsStream("/SQL_SETUP.sql")));

                    sr.setDelimiter("#");
                    sr.runScript(reader);

                    completableFuture.complete(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                    completableFuture.complete(false);
                }

                return true;
            }
        };
        run(runnable);
        return succeeded;
    }

    public CompletableFuture<HashMap<String, TimeManager>> setupHandlers() {
        CompletableFuture<HashMap<String, TimeManager>> future = new CompletableFuture<>();
        SequentialRunnable runnable = new SequentialRunnable(future) {
            @Override
            public boolean run() {


                try (Connection connection = SQLPool.getConnection()) {
                    HashMap<String, TimeManager> map = new HashMap<>();
                    PreparedStatement query = connection.prepareStatement("SELECT * FROM EVENT_LIST;");
                    ResultSet results = query.executeQuery();

                    while (results.next()) {
                        String name = results.getString("EVENT_NAME");
                        map.put(name, new TimeManager(name));
                    }

                    completableFuture.complete(map);

                } catch (SQLException e) {
                    e.printStackTrace();
                    completableFuture.complete(null);
                }

                return true;
            }
        };
        run(runnable);
        return future;
    }

}
