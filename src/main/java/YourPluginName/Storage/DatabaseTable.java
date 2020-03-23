package YourPluginName.Storage;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

public class DatabaseTable {

    private HashMap<String, ColumnWrapper> columns = new HashMap();
    private boolean initializedSuccessfully = false;

    public final ColumnWrapper getColumnByName(String name) {
        return columns.get(name);
    }

    public static class ColumnWrapper {
        private String name, creationString, type;
        private boolean canBeNull;
        private boolean isPrimaryKey;

        public ColumnWrapper(String name, String type, String extraParams) {
            this.name = name;
            this.canBeNull = true;
            this.creationString = name + " " + type + " " + extraParams;

            if (type.contains("VARCHAR")) {
                this.type = "string";
            } else if (type.contains("BIGINT")) {
                this.type = "bigint";
            } else if (type.contains("INT")) {
                this.type = "int";
            } else if (type.contains("BOOLEAN")) {
                this.type = "boolean";
            }

            if (extraParams.contains("NOT NULL"))
                this.canBeNull = false;

            this.isPrimaryKey = false;
            if (extraParams.contains("PRIMARY KEY"))
                this.isPrimaryKey = true;

        }

        public String getCurrentValue(final ResultSet resultSet) {
            try {
                switch(this.type) {
                    case "int":
                        return resultSet.getInt(getName()) + "";
                    case "bigint":
                        return resultSet.getLong(getName()) + "";
                    case "string":
                        return resultSet.getString(getName()) + "";
                    case "boolean":
                        return resultSet.getBoolean(getName()) + "";
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        final public String getName() {
            return this.name;
        }

        public boolean isCanBeNull() {
            return this.canBeNull;
        }

        public boolean isPrimaryKey() {
            return this.isPrimaryKey;
        }

        public String getSetupString() {
            return this.creationString;
        }
    }
    private String tableName;

    /* isSetCorrectly
     * @precondition: assumes table exists
     * @params: none
     * @returns: true if table has columns with same names as those inputted
     */
    private boolean isSetCorrectly() {
        List<ColumnWrapper> list = new ArrayList<ColumnWrapper>(columns.values());
        try (Connection connection = SQLPool.getConnection()){
            for (ColumnWrapper column : list) {
                Bukkit.getLogger().info(" - " + column.getName());
                try {
                    DatabaseMetaData md = connection.getMetaData();
                    ResultSet rs = md.getColumns(null, null, getName(), column.getName());
                    if (rs.next())
                        continue;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Bukkit.getLogger().info("MySQLTable->isSetCorrectly: Something went wrong while checking columns.");
                    return false;
                }
                Bukkit.getLogger().info("   --> No column found. Please either remove this table entirely or add this column to the table.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /* exists
     * @precondition: name is given
     * @params: none
     * @returns: true only if table exists
     */
    private boolean exists() {
        ResultSet tables = null;
        try (Connection connection = SQLPool.getConnection()){
            tables = connection.getMetaData().getTables(null, null, getName(), null);
            return tables.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean createTable() {
        Bukkit.getLogger().info("Creating Table " + getName());
        if (exists()) {
            if (!isSetCorrectly()) {
                Bukkit.getLogger().info("Table with same name exists, but is not setup correctly. Please remove this table or change the name of the given table in the code. \nResolving by ignoring data");
                return false;
            }
            return true;
        }

        List<ColumnWrapper> list = new ArrayList<ColumnWrapper>(columns.values());
        int commas = columns.size()-1;
        //create table creation statement
        String sql = "";
        for (ColumnWrapper column : list) {
            String columnCreator = column.getSetupString();

            if (commas >= 1)
                sql += columnCreator + ",";
            else
                sql += columnCreator;

            commas--;
        }

        Bukkit.getLogger().info("Creating table: \"CREATE TABLE " + getName() + "(" + sql + ")\"");

        Statement statement = null;
        try (Connection connection = SQLPool.getConnection()){
            statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE " + getName() + "(" + sql + ")");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().info("Failed to create new table");
            return false;
        }

        return true;

    }

    /* create
     * @precondition: none
     * @params: none
     * @returns: none
     * @desc: creates table if not created; if created, checks if setup right
     */
    public boolean create() {
        this.initializedSuccessfully = createTable();
        return this.initializedSuccessfully;
    }

    public boolean successfulInit() {
        return initializedSuccessfully;
    }

    public String getName() {
        return tableName;
    }

    public DatabaseTable(String tableName, ColumnWrapper ...columnWrappers) {
        this.tableName = tableName;
        for (ColumnWrapper column : columnWrappers) {
            if (this.columns.containsKey(column.getName())) {
                throw new IllegalArgumentException("Cannot have two or more columns with the same name");
            }
            this.columns.put(column.getName(), column);
        }
    }
}


