package YourPluginName.Storage;

import YourPluginName.Main.Main;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseSetup {

    public static boolean setupTables() {

        try (Connection connection = SQLPool.getConnection()) {
            System.out.println("Connection established......");
            //Initialize the script runner
            ScriptRunner sr = new ScriptRunner(connection);
            //Creating a reader object
            Reader reader = new BufferedReader(new InputStreamReader(Main.getPlugin().getClass().getResourceAsStream("/SQL_SETUP.sql")));
            //Running the script
            sr.setDelimiter("#");

            sr.runScript(reader);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return true;
    }

    private static File getFile(String fileName) {
        ClassLoader classLoader = Main.getPlugin().getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file;
    }
}
